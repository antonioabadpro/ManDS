package com.autoescuela.erp.pagos.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.autoescuela.erp.academico.service.AcademicoService;
import com.autoescuela.erp.core.enums.ModalidadMatricula;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador para la recepción y procesamiento asíncrono de Webhooks de Stripe.
 * Valida criptográficamente las firmas HMAC y formaliza idempotentemente matrículas,
 * recargas de saldo de clases y renovaciones tras eventos de checkout completados.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/pagos")
public class StripeWebhookController
{
    private final AcademicoService academicoService;

    @Value("${stripe.webhook.secret:whsec_placeholder}")
    private String webhookSecret;

    /**
     * Endpoint receptor de Webhooks HTTP enviados por Stripe tras eventos de cobro.
     * Valida criptográficamente la firma con la clave secreta y ejecuta la lógica de negocio correspondiente.
     *
     * @param payload Contenido en texto plano del evento JSON.
     * @param sigHeader Cabecera 'Stripe-Signature' con la firma HMAC.
     * @return Respuesta HTTP indicando el resultado del procesamiento.
     */
    @PostMapping("/webhook")
    @ResponseBody
    public ResponseEntity<String> procesarWebhookStripe(@RequestBody String payload, @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader)
    {
        ResponseEntity<String> respuesta = null;

        if (sigHeader == null || sigHeader.isBlank())
        {
            log.error("Falta la cabecera 'Stripe-Signature' en la solicitud de webhook.");
            return ResponseEntity.status(400).body("Falta la firma del webhook");
        }

        Event event;
        try
        {
            // Verificamos la firma criptografica del webhook para asegurar autenticidad (proviene de Stripe) e integridad (NO ha sido alterado).
            event = Webhook.constructEvent(payload, sigHeader, this.webhookSecret);
        }
        catch (SignatureVerificationException e)
        {
            log.error("Error al verificar la firma del webhook de Stripe: {}", e.getMessage(), e);
            return ResponseEntity.status(400).body("Firma no válida");
        }

        switch (event.getType())
        {
            case "checkout.session.completed": // El pago se ha cobrado con éxito
            {
                log.info("Evento de Stripe recibido: checkout.session.completed");

                EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
                Session session = null;
                try
                {
                    if (deserializer.getObject().isPresent())
                    {
                        session = (Session) deserializer.getObject().get();
                    }
                    else
                    {
                        session = (Session) deserializer.deserializeUnsafe();
                    }
                }
                catch (Exception ex)
                {
                    log.warn("Aviso al deserializar el objeto Session de Stripe: {}", ex.getMessage());
                    try
                    {
                        session = (Session) deserializer.deserializeUnsafe();
                    }
                    catch (Exception ignored)
                    {
                        // Se maneja con session == null a continuación
                    }
                }

                String tipoOperacion = (session != null) ? session.getMetadata().get("tipoOperacion") : null;
                String tipoCarnet = (session != null) ? session.getMetadata().get("tipoCarnet") : null;
                String alumnoIdStr = (session != null) ? session.getMetadata().get("alumnoId") : null;
                Long alumnoId = Long.valueOf(alumnoIdStr);
                String modalidadStr = (session != null) ? session.getMetadata().get("modalidad") : null;

                ModalidadMatricula modalidad = null;
                if (modalidadStr != null && !modalidadStr.isBlank())
                {
                    try
                    {
                        modalidad = ModalidadMatricula.valueOf(modalidadStr.trim().toUpperCase());
                    }
                    catch (IllegalArgumentException ignored)
                    {
                        modalidad = ModalidadMatricula.TEORICO_PRACTICA;
                    }
                }

                // Obtenemos el importe real verificado en céntimos desde Stripe
                float importeTotal = (session != null) ? (session.getAmountTotal() / 100.0f) : 0.0f;

                switch (tipoOperacion != null ? tipoOperacion : "")
                {
                    case "MATRICULA": // Pago de la matrícula de un nuevo alumno tras finalizar el proceso de registro
                    {
                        log.info("Evento checkout.session.completed recibido para MATRICULA. Alumno ID: {}, Carnet: {}, Modalidad: {}, Importe: {} €",
                                alumnoId, tipoCarnet, modalidad, importeTotal);
                        if (alumnoId != null)
                        {
                            try
                            {
                                this.academicoService.matricularTrasPago(alumnoId, tipoCarnet, modalidad, importeTotal);
                            }
                            catch (NumberFormatException e)
                            {
                                log.error("ID de alumno no válido en metadata de Stripe: {}", alumnoIdStr, e);
                            }
                        }
                        respuesta = ResponseEntity.ok("Evento de matrícula procesado correctamente");
                    }; break;
                    case "CLASES_PRACTICAS": // Compra de clases prácticas
                    {
                        String numeroClases_string = (session != null) ? session.getMetadata().get("numeroClases") : null;

                        int numeroClases = (numeroClases_string != null) ? Integer.parseInt(numeroClases_string) : 0;

                        String idSesionStripe = (session != null) ? session.getId() : null;
                        log.info("Evento checkout.session.completed recibido para CLASES_PRACTICAS. ID: {}, Clases: {}, Importe: {} €", alumnoId, numeroClases, importeTotal);
                        if (alumnoId != null && numeroClases > 0)
                        {
                            this.academicoService.recargarSaldoClasesTrasPago(alumnoId, numeroClases, idSesionStripe);
                        }
                        respuesta = ResponseEntity.ok("Evento de clases prácticas procesado correctamente");
                    }; break;
                    case "RENOVACION_MATRICULA": // Renovación de matrícula tras agotar convocatorias
                    {
                        String idSesionStripe = (session != null) ? session.getId() : null;
                        log.info("Evento checkout.session.completed recibido para RENOVACION_MATRICULA. Alumno ID: {}, Carnet: {}, Importe: {} €",
                                alumnoIdStr, tipoCarnet, importeTotal);
                        if (alumnoIdStr != null)
                        {
                            try
                            {
                                this.academicoService.renovarMatriculaTrasPago(alumnoId, idSesionStripe, importeTotal);
                            }
                            catch (NumberFormatException e)
                            {
                                log.error("ID de alumno no válido en metadata de renovación de Stripe: {}", alumnoIdStr, e);
                            }
                        }
                        respuesta = ResponseEntity.ok("Evento de renovación de matrícula procesado correctamente");
                    }; break;
                    default: // Para cualquier otro tipo de operación desconocida, registramos el evento pero no realizamos acción inmediata
                    {
                        log.info("Operación '{}' no requiere procesamiento en este evento.", tipoOperacion);
                        respuesta = ResponseEntity.ok("Evento recibido y registrado, pero NO procesado");
                    }; break;
                }
            }; break;
            case "checkout.session.payment_failed": // El pago ha fallado
            {
                log.warn("Evento de pago fallido recibido en webhook: checkout.session.payment_failed");
                respuesta = ResponseEntity.ok("Evento de pago fallido registrado");
            }; break;
            default: // Para cualquier otro tipo de evento desconocido, registramos el evento pero no realizamos acción inmediata
            {
                log.info("Evento de Stripe recibido (no gestionado): {}", event.getType());
                respuesta = ResponseEntity.ok("Evento recibido y NO procesado");
            }; break;
        }
        return respuesta;
    }
}
