package com.autoescuela.erp.pagos.controller;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.autoescuela.erp.academico.service.AcademicoService;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.pagos.dto.SesionPagoDTO;
import com.autoescuela.erp.pagos.service.PagoStripeService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador para la gestión de pagos, pasarela de matriculación y webhooks de Stripe.
 * Gestiona el inicio de sesiones de checkout, páginas de retorno (éxito/cancelación),
 * la visualización de la pasarela y la recepción asíncrona de eventos de Stripe.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/pagos")
public class StripeWebhookController
{
    private final AcademicoService academicoService;
    private final PagoStripeService pagoStripeService;

    @Value("${stripe.webhook.secret:whsec_placeholder}")
    private String webhookSecret;

    /**
     * Muestra la interfaz de pasarela de pago para el abono de la tasa de matrícula.
     * Aplica la regla de negocio de precios según el tipo de permiso:
     * - Permisos C y D (vehículos grandes y pesados): 450,00 €
     * - Resto de permisos: 250,00 €
     *
     * @param tipoCarnet_string Nombre del enum {@link TipoCarnet} recibido como parámetro.
     * @param model Modelo para abastecer a la plantilla Thymeleaf.
     * @return Nombre de la vista de checkout ("pagos/checkout").
     */
    @GetMapping("/checkout")
    public String mostrarCheckoutMatricula(@RequestParam(name = "tipoCarnet", required = false) String tipoCarnet_string, Model model)
    {
        TipoCarnet tipoCarnet = TipoCarnet.PERMISO_B;
        if (tipoCarnet_string != null && !tipoCarnet_string.isBlank())
        {
            try
            {
                tipoCarnet = TipoCarnet.valueOf(tipoCarnet_string.trim().toUpperCase());
            }
            catch (IllegalArgumentException ex)
            {
                log.warn("Tipo de carnet no reconocido en pasarela: '{}'. Se aplica PERMISO_B por defecto.", tipoCarnet_string);
            }
        }

        // Regla de Negocio: Permisos C y D (450€), resto (250€)
        boolean esPesado = tipoCarnet.name().startsWith("PERMISO_C") || tipoCarnet.name().startsWith("PERMISO_D");
        double importeTotal = esPesado ? 450.00 : 250.00;
        double tasaDgt = 94.05;
        double cuotaAutoescuela = importeTotal - tasaDgt;

        model.addAttribute("tipoCarnet", tipoCarnet);
        model.addAttribute("descripcionCarnet", tipoCarnet.getDescripcion());
        model.addAttribute("esPesado", esPesado);
        model.addAttribute("importeTotal", String.format(Locale.GERMAN, "%.2f", importeTotal));
        model.addAttribute("cuotaAutoescuela", String.format(Locale.GERMAN, "%.2f", cuotaAutoescuela));
        model.addAttribute("tasaDgt", String.format(Locale.GERMAN, "%.2f", tasaDgt));

        return "pagos/checkout";
    }

    /**
     * Inicia una sesión de pago oficial en Stripe Checkout y redirige a la pasarela alojada.
     *
     * @param tipoCarnet_string Tipo de permiso para el que se solicita la matrícula.
     * @param dni DNI opcional del alumno.
     * @return Redirección HTTP hacia la URL de Stripe Checkout.
     */
    @GetMapping("/iniciar-stripe")
    public String iniciarStripeCheckout(@RequestParam(name = "tipoCarnet", required = false) String tipoCarnetStr, @RequestParam(name = "dni", required = false) String dni)
    {
        TipoCarnet tipoCarnet = TipoCarnet.PERMISO_B;
        if (tipoCarnetStr != null && !tipoCarnetStr.isBlank())
        {
            try
            {
                tipoCarnet = TipoCarnet.valueOf(tipoCarnetStr.trim().toUpperCase());
            }
            catch (IllegalArgumentException ex)
            {
                log.warn("Tipo de carnet no reconocido al iniciar Stripe: '{}'. Aplicando PERMISO_B.", tipoCarnetStr);
            }
        }

        SesionPagoDTO sesion = this.pagoStripeService.crearSesionPagoMatricula(tipoCarnet, dni);
        log.info("Redirigiendo a Stripe Checkout: {}", sesion.urlStripe());
        return "redirect:" + sesion.urlStripe();
    }

    /**
     * Página de confirmación tras un pago exitoso en Stripe Checkout.
     *
     * @param sessionId Identificador de la sesión devuelto por Stripe.
     * @param model Modelo para la vista Thymeleaf.
     * @return Nombre de la vista de éxito ("pagos/success").
     */
    @GetMapping("/checkout/success")
    public String mostrarExitoCheckout(@RequestParam(name = "session_id", required = false) String sessionId, Model model)
    {
        model.addAttribute("sessionId", sessionId);
        return "pagos/success";
    }

    /**
     * Página informativa tras la cancelación voluntaria del pago en Stripe Checkout.
     *
     * @return Nombre de la vista de cancelación ("pagos/cancel").
     */
    @GetMapping("/checkout/cancel")
    public String mostrarCancelacionCheckout()
    {
        return "pagos/cancel";
    }

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

        switch(event.getType())
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
                String dniAlumno = (session != null) ? session.getMetadata().get("dniAlumno") : null;

                // Obtenemos el importe real verificado en céntimos desde Stripe
                float importeTotal = (session != null) ? (session.getAmountTotal() / 100.0f) : 0.0f;

                switch(tipoOperacion != null ? tipoOperacion : "")
                {
                    case "MATRICULA":
                    {
                        log.info("Evento checkout.session.completed recibido para MATRICULA. DNI: {}, Carnet: {}, Importe: {} €",
                            dniAlumno, tipoCarnet, importeTotal);

                        if (dniAlumno != null && !dniAlumno.isBlank() && !"PENDIENTE".equalsIgnoreCase(dniAlumno))
                        {
                            try
                            {
                                this.academicoService.matricularTrasPago(dniAlumno, tipoCarnet, importeTotal);
                            }
                            catch (ReglaNegocioException ex)
                            {
                                log.warn("Aviso de negocio al formalizar matrícula en webhook: {}", ex.getMessage());
                            }
                        }
                        else
                        {
                            log.info("Pago de matrícula registrado para alta pendiente con carnet {}. Se conciliará al completar el registro.", tipoCarnet);
                        }
                        respuesta = ResponseEntity.ok("Evento de matrícula procesado correctamente");
                    }; break;
                    default: // Para cualquier otro tipo de operación desconocida, registramos el evento pero no realizamos acción inmediata
                    {
                        log.info("Operación '{}' no requiere matriculación inmediata en este evento.", tipoOperacion);
                        respuesta = ResponseEntity.ok("Evento recibido y registrado, pero NO procesado");
                    }; break;
                }
            }; break;
            case "checkout.session.payment_failed": // El pago ha fallado
            {
                log.warn("Evento de Stripe recibido: checkout.session.payment_failed");

                log.warn("Evento de pago fallido recibido en webhook: checkout.session.payment_failed");
                respuesta = ResponseEntity.ok("Evento de pago fallido registrado");
            }; break;
            default: // Para cualquier otro tipo de evento desconocido, registramos el evento pero no realizamos acción inmediata
            {
                log.info("Evento de Stripe recibido (no gestionado): {}", event.getType());

                log.info("Evento de Stripe no gestionado recibido en webhook: {}", event.getType());
                respuesta = ResponseEntity.ok("Evento recibido y NO procesado");
            }; break;
        }
        return respuesta;
    }
}
