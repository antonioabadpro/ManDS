package com.autoescuela.erp.pagos.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.autoescuela.erp.academico.service.AcademicoService;
import com.autoescuela.erp.auth.service.AuthenticationService;
import com.autoescuela.erp.core.enums.ModalidadMatricula;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
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
    private final AuthenticationService autenticacionService;

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
        catch (RuntimeException e)
        {
            log.error("Payload JSON no válido en solicitud de webhook de Stripe: {}", e.getMessage());
            return ResponseEntity.status(400).body("Payload del webhook no válido");
        }

        switch (event.getType())
        {
            case "checkout.session.completed": // El pago se ha cobrado con éxito
            {
                respuesta = this.procesarCheckoutSessionCompleted(event);
            }; break;
            case "checkout.session.expired": // La sesión de Checkout ha expirado (abandono o cancelación)
            {
                respuesta = this.procesarCheckoutSessionExpired(event);
            }; break;
            default: // Para cualquier otro tipo de evento desconocido, registramos el evento pero no realizamos acción inmediata
            {
                log.info("Evento de Stripe recibido (no gestionado): {}", event.getType());
                respuesta = ResponseEntity.ok("Evento recibido y NO procesado");
            }; break;
        }
        return respuesta;
    }

    /**
     * Procesa un evento {@code checkout.session.completed} de forma resiliente ante metadatos incompletos
     * o errores permanentes de reglas de negocio en reintentos obsoletos.
     */
    private ResponseEntity<String> procesarCheckoutSessionCompleted(Event event)
    {
        log.info("Evento de Stripe recibido: checkout.session.completed");
        Session session = this.extraerSession(event);
        if (session == null || session.getMetadata() == null)
        {
            log.warn("Evento checkout.session.completed recibido sin objeto Session o sin metadata.");
            return ResponseEntity.ok("Evento recibido sin metadata procesable");
        }

        Map<String, String> metadata = session.getMetadata();
        String tipoOperacion = metadata.get("tipoOperacion");
        String tipoCarnet = metadata.get("tipoCarnet");
        String alumnoIdStr = metadata.get("alumnoId");
        Long alumnoId = this.parsearLongSeguro(alumnoIdStr, "alumnoId");
        ModalidadMatricula modalidad = this.parsearModalidad(metadata.get("modalidad"));
        float importeTotal = this.obtenerImporteTotalEuros(session);

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
                    catch (ReglaNegocioException ex)
                    {
                        log.warn("Evento MATRICULA omitido por regla de negocio (alumnoId={}): {}", alumnoId, ex.getMessage());
                    }
                }
                return ResponseEntity.ok("Evento de matrícula procesado correctamente");
            }
            case "CLASES_PRACTICAS": // Compra de clases prácticas
            {
                int numeroClases = this.parsearIntSeguro(metadata.get("numeroClases"), "numeroClases");
                String idSesionStripe = session.getId();
                log.info("Evento checkout.session.completed recibido para CLASES_PRACTICAS. ID: {}, Clases: {}, Importe: {} €",
                        alumnoId, numeroClases, importeTotal);
                if (alumnoId != null && numeroClases > 0)
                {
                    try
                    {
                        this.academicoService.recargarSaldoClasesTrasPago(alumnoId, numeroClases, idSesionStripe);
                    }
                    catch (ReglaNegocioException ex)
                    {
                        log.warn("Evento CLASES_PRACTICAS omitido por regla de negocio (alumnoId={}): {}", alumnoId, ex.getMessage());
                    }
                }
                return ResponseEntity.ok("Evento de clases prácticas procesado correctamente");
            }
            case "RENOVACION_MATRICULA": // Renovación de matrícula tras agotar convocatorias
            {
                String idSesionStripe = session.getId();
                log.info("Evento checkout.session.completed recibido para RENOVACION_MATRICULA. Alumno ID: {}, Carnet: {}, Importe: {} €",
                        alumnoIdStr, tipoCarnet, importeTotal);
                if (alumnoId != null)
                {
                    try
                    {
                        this.academicoService.renovarMatriculaTrasPago(alumnoId, idSesionStripe, importeTotal);
                    }
                    catch (ReglaNegocioException ex)
                    {
                        log.warn("Evento RENOVACION_MATRICULA omitido por regla de negocio (alumnoId={}): {}", alumnoId, ex.getMessage());
                    }
                }
                return ResponseEntity.ok("Evento de renovación de matrícula procesado correctamente");
            }
            default: // Para cualquier otro tipo de operación desconocida, registramos el evento pero no realizamos acción inmediata
            {
                log.info("Operación '{}' no requiere procesamiento en este evento.", tipoOperacion);
                return ResponseEntity.ok("Evento recibido y registrado, pero NO procesado");
            }
        }
    }

    /**
     * Procesa la expiración de una sesión de Stripe Checkout purgando al alumno huérfano inactivo si procede.
     */
    private ResponseEntity<String> procesarCheckoutSessionExpired(Event event)
    {
        log.info("Evento de Stripe recibido: checkout.session.expired");
        Session session = this.extraerSession(event);

        if (session != null && session.getMetadata() != null)
        {
            String tipoOperacion = session.getMetadata().get("tipoOperacion");
            String alumnoIdStr = session.getMetadata().get("alumnoId");
            if ("MATRICULA".equals(tipoOperacion) && alumnoIdStr != null && !alumnoIdStr.isBlank())
            {
                Long alumnoId = this.parsearLongSeguro(alumnoIdStr, "alumnoId");
                if (alumnoId != null)
                {
                    try
                    {
                        this.autenticacionService.eliminarAlumnoInactivoPorId(alumnoId);
                        log.info("Sesión de Stripe expirada: Alumno huérfano inactivo con ID {} purgado satisfactoriamente.", alumnoId);
                    }
                    catch (ReglaNegocioException ex)
                    {
                        log.warn("No se pudo purgar el alumno huérfano tras expiración (alumnoId={}): {}", alumnoId, ex.getMessage());
                    }
                }
            }
        }
        return ResponseEntity.ok("Evento de sesión de checkout expirada procesado correctamente");
    }

    /**
     * Deserializa de forma segura el objeto {@link Session} contenido en un evento de Stripe,
     * aplicando deserialización flexible cuando la versión de API del evento difiere de la librería.
     */
    private Session extraerSession(Event event)
    {
        if (event == null || event.getDataObjectDeserializer() == null)
        {
            return null;
        }
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        try
        {
            StripeObject objeto = deserializer.getObject().isPresent()
                    ? deserializer.getObject().get()
                    : deserializer.deserializeUnsafe();
            return (objeto instanceof Session session) ? session : null;
        }
        catch (Exception ex)
        {
            log.warn("Aviso al deserializar el objeto Session de Stripe en evento {}: {}", event.getType(), ex.getMessage());
            try
            {
                StripeObject objeto = deserializer.deserializeUnsafe();
                return (objeto instanceof Session session) ? session : null;
            }
            catch (Exception ignored)
            {
                return null;
            }
        }
    }

    private Long parsearLongSeguro(String valor, String nombreCampo)
    {
        if (valor == null || valor.isBlank())
        {
            return null;
        }
        try
        {
            return Long.valueOf(valor.trim());
        }
        catch (NumberFormatException e)
        {
            log.error("ID numérico no válido en metadata '{}' de Stripe: {}", nombreCampo, valor, e);
            return null;
        }
    }

    private int parsearIntSeguro(String valor, String nombreCampo)
    {
        if (valor == null || valor.isBlank())
        {
            return 0;
        }
        try
        {
            return Integer.parseInt(valor.trim());
        }
        catch (NumberFormatException e)
        {
            log.error("Valor entero no válido en metadata '{}' de Stripe: {}", nombreCampo, valor, e);
            return 0;
        }
    }

    private ModalidadMatricula parsearModalidad(String modalidadStr)
    {
        if (modalidadStr == null || modalidadStr.isBlank())
        {
            return null;
        }
        try
        {
            return ModalidadMatricula.valueOf(modalidadStr.trim().toUpperCase());
        }
        catch (IllegalArgumentException ignored)
        {
            return ModalidadMatricula.TEORICO_PRACTICA;
        }
    }

    private float obtenerImporteTotalEuros(Session session)
    {
        if (session == null || session.getAmountTotal() == null)
        {
            return 0.0f;
        }
        return session.getAmountTotal() / 100.0f;
    }
}
