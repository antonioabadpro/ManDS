package com.autoescuela.erp.pagos.controller;

import java.util.Locale;

import jakarta.servlet.http.HttpSession;

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

import com.autoescuela.erp.academico.model.Matricula;
import com.autoescuela.erp.academico.service.AcademicoService;
import com.autoescuela.erp.auth.service.AuthenticationService;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
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
    private final AuthenticationService autenticacionService;

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
        TipoCarnet tipoCarnet = null;
        if (tipoCarnet_string != null && !tipoCarnet_string.isBlank())
        {
            try
            {
                tipoCarnet = TipoCarnet.valueOf(tipoCarnet_string.trim().toUpperCase());
            }
            catch (IllegalArgumentException ex)
            {
                log.warn("Tipo de carnet no reconocido en pasarela: '{}'. Se aplica PERMISO_B por defecto.", tipoCarnet_string);
                throw new ReglaNegocioException("Tipo de carnet no reconocido: " + tipoCarnet_string);
            }
        }

        // Regla de Negocio: Permisos C y D (450€), resto (250€)
        if(tipoCarnet == null)
        {
            throw new ReglaNegocioException("El tipo de carnet es obligatorio para iniciar el proceso de pago.");
        }

        boolean esVehiculoPesado = tipoCarnet.name().startsWith("PERMISO_C") || tipoCarnet.name().startsWith("PERMISO_D");
        double importeTotal = esVehiculoPesado ? 450.00 : 250.00;
        double tasaDgt = 94.05;
        double cuotaAutoescuela = importeTotal - tasaDgt;

        model.addAttribute("tipoCarnet", tipoCarnet);
        model.addAttribute("descripcionCarnet", tipoCarnet.getDescripcion());
        model.addAttribute("esVehiculoPesado", esVehiculoPesado);
        model.addAttribute("importeTotal", String.format(Locale.GERMAN, "%.2f", importeTotal));
        model.addAttribute("cuotaAutoescuela", String.format(Locale.GERMAN, "%.2f", cuotaAutoescuela));
        model.addAttribute("tasaDgt", String.format(Locale.GERMAN, "%.2f", tasaDgt));

        return "pagos/checkout";
    }

    /**
     * Página de confirmación tras un pago exitoso en Stripe Checkout.
     * Recupera el registro pendiente y el importe abonado desde la sesión para formalizar
     * el alta del nuevo alumno y su matrícula asociada.
     *
     * @param sessionId Identificador de la sesión devuelto por Stripe.
     * @param model Modelo para la vista Thymeleaf.
     * @param sesion Sesión HTTP del usuario.
     * @return Nombre de la vista de éxito ("pagos/success").
     */
    @GetMapping("/checkout/success")
    public String mostrarExitoCheckout(@RequestParam(name = "session_id", required = false) String sessionId, Model model, HttpSession sesion)
    {
        // Obtenemos los datos del alumno y del importe abonado desde la sesión HTTP
        String dniAlumno = (String) sesion.getAttribute("dniAlumno");
        String tipoCarnetStr = (String) sesion.getAttribute("tipoCarnet");
        Float importeTotal = (Float) sesion.getAttribute("importeTotal");

        if (dniAlumno != null && !dniAlumno.isBlank())
        {
            // Formalizamos la matrícula y activamos la cuenta de forma idempotente
            Matricula matricula = this.academicoService.matricularTrasPago(dniAlumno, tipoCarnetStr, importeTotal);

            sesion.removeAttribute("dniAlumno");
            sesion.removeAttribute("tipoCarnet");
            sesion.removeAttribute("importeTotal");

            model.addAttribute("importeTotal", String.format(Locale.GERMAN, "%.2f", importeTotal));
            model.addAttribute("tipoCarnet", matricula != null && matricula.getPermisoCarnet() != null ? matricula.getPermisoCarnet().getDescripcion() : tipoCarnetStr);
            model.addAttribute("nombreAlumno", matricula != null && matricula.getAlumno() != null ? matricula.getAlumno().getNombre() : "Alumno");
        }
        else
        {
            // Si la página se recarga o ya se procesó la sesión, mostramos valores por defecto
            model.addAttribute("importeTotal", "250,00");
        }

        model.addAttribute("sessionId", sessionId);
        return "pagos/matricula-success";
    }

    /**
     * Página informativa tras la cancelación voluntaria del pago en Stripe Checkout.
     * Si existía un registro de alumno pendiente e inactivo, se elimina de la base de datos
     * para liberar su DNI, correo y nombre de usuario de inmediato.
     *
     * @param sesion Sesión HTTP del usuario.
     * @return Nombre de la vista de cancelación ("pagos/matricula-cancel").
     */
    @GetMapping("/checkout/cancel")
    public String mostrarCancelacionCheckout(HttpSession sesion)
    {
        String dniAlumno = (String) sesion.getAttribute("dniAlumno");
        if (dniAlumno != null && !dniAlumno.isBlank())
        {
            this.autenticacionService.eliminarAlumnoInactivoPorDni(dniAlumno);
            sesion.removeAttribute("dniAlumno");
            sesion.removeAttribute("tipoCarnet");
            sesion.removeAttribute("importeTotal");
            log.info("Cancelación de pago procesada: Alumno inactivo con DNI {} purgado satisfactoriamente.", dniAlumno);
        }
        return "pagos/matricula-cancel";
    }

    /**
     * Página de confirmación tras la compra exitosa de clases prácticas en Stripe Checkout.
     * Recupera la sesión o los parámetros de la compra y confirma la recarga de saldo.
     *
     * @param sessionId Identificador de la sesión de Stripe devuelto en la URL de retorno.
     * @param model Modelo para abastecer a la plantilla Thymeleaf.
     * @param sesion Sesión HTTP del usuario.
     * @return Nombre de la vista ("pagos/clases-success").
     */
    @GetMapping("/clases/success")
    public String mostrarExitoPagoClases(@RequestParam(name = "session_id", required = false) String sessionId, Model model, HttpSession sesion)
    {
        String dniAlumno = (String) sesion.getAttribute("pagoClases_dni");
        Integer numeroClases = (Integer) sesion.getAttribute("pagoClases_numero");
        Float importeTotal = (Float) sesion.getAttribute("pagoClases_importe");
        String tipoCarnetStr = (String) sesion.getAttribute("pagoClases_tipoCarnet");

        if (dniAlumno != null && !dniAlumno.isBlank() && numeroClases != null && numeroClases > 0)
        {
            Matricula matricula = this.academicoService.recargarSaldoClasesTrasPago(dniAlumno, numeroClases, sessionId);

            sesion.removeAttribute("pagoClases_dni");
            sesion.removeAttribute("pagoClases_numero");
            sesion.removeAttribute("pagoClases_importe");
            sesion.removeAttribute("pagoClases_tipoCarnet");

            model.addAttribute("dniAlumno", dniAlumno);
            model.addAttribute("numeroClases", numeroClases);
            model.addAttribute("saldoTotal", matricula != null ? matricula.getSaldoClases() : numeroClases);
            model.addAttribute("importeTotal", importeTotal != null ? String.format(Locale.GERMAN, "%.2f", importeTotal) : "0,00");
            model.addAttribute("tipoCarnet", matricula != null && matricula.getPermisoCarnet() != null ? matricula.getPermisoCarnet().getDescripcion() : tipoCarnetStr);
            model.addAttribute("nombreAlumno", matricula != null && matricula.getAlumno() != null ? matricula.getAlumno().getNombre() : "Alumno");
        }
        else
        {
            model.addAttribute("numeroClases", numeroClases != null ? numeroClases : 0);
            model.addAttribute("saldoTotal", numeroClases != null ? numeroClases : 0);
            model.addAttribute("importeTotal", importeTotal != null ? String.format(Locale.GERMAN, "%.2f", importeTotal) : "0,00");
            model.addAttribute("tipoCarnet", tipoCarnetStr != null ? tipoCarnetStr : "Permiso de Conducir");
            model.addAttribute("nombreAlumno", "Alumno");
        }

        model.addAttribute("sessionId", sessionId);
        return "pagos/clases-success";
    }

    /**
     * Página informativa tras la cancelación voluntaria del pago de clases prácticas en Stripe Checkout.
     * Muestra aviso indicando que no se ha efectuado ningún cobro y proporciona retorno seguro al panel.
     *
     * @param sesion Sesión HTTP del usuario.
     * @return Nombre de la vista ("pagos/clases-cancel").
     */
    @GetMapping("/clases/cancel")
    public String mostrarCancelacionPagoClases(HttpSession sesion)
    {
        sesion.removeAttribute("pagoClases_dni");
        sesion.removeAttribute("pagoClases_numero");
        sesion.removeAttribute("pagoClases_importe");
        sesion.removeAttribute("pagoClases_tipoCarnet");
        return "pagos/clases-cancel";
    }

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
                    case "MATRICULA": // Pago de la matrícula de un nuevo alumno tras finalizar el proceso de registro
                    {
                        log.info("Evento checkout.session.completed recibido para MATRICULA. DNI: {}, Carnet: {}, Importe: {} €",
                            dniAlumno, tipoCarnet, importeTotal);
                        if (dniAlumno != null && !dniAlumno.isBlank())
                        {
                            this.academicoService.matricularTrasPago(dniAlumno, tipoCarnet, importeTotal);
                        }
                        respuesta = ResponseEntity.ok("Evento de matrícula procesado correctamente");
                    }; break;
                    case "CLASES_PRACTICAS": // Compra de clases prácticas
                    {
                        String numeroClases_string = (session != null) ? session.getMetadata().get("numeroClases") : null;

                        int numeroClases = (numeroClases_string != null) ? Integer.parseInt(numeroClases_string) : 0;

                        String idSesionStripe = (session != null) ? session.getId() : null;
                        log.info("Evento checkout.session.completed recibido para CLASES_PRACTICAS. DNI: {}, Clases: {}, Importe: {} €",
                            dniAlumno, numeroClases, importeTotal);
                        if (dniAlumno != null && !dniAlumno.isBlank() && numeroClases > 0)
                        {
                            this.academicoService.recargarSaldoClasesTrasPago(dniAlumno, numeroClases, idSesionStripe);
                        }
                        respuesta = ResponseEntity.ok("Evento de clases prácticas procesado correctamente");
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

                log.info("Evento de Stripe no gestionado recibido en webhook: {}", event.getType());
                respuesta = ResponseEntity.ok("Evento recibido y NO procesado");
            }; break;
        }
        return respuesta;
    }
}
