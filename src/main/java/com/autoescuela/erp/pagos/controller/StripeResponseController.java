package com.autoescuela.erp.pagos.controller;

import java.util.Locale;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.autoescuela.erp.academico.model.Matricula;
import com.autoescuela.erp.academico.service.AcademicoService;
import com.autoescuela.erp.auth.service.AuthenticationService;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador para la visualización de la pasarela y gestión de las pantallas de retorno
 * (éxito y cancelación) tras el flujo de Stripe Checkout para matrículas, clases prácticas y renovaciones.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/pagos")
public class StripeResponseController
{
    private final AcademicoService academicoService;
    private final AuthenticationService autenticacionService;

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
        if (tipoCarnet == null)
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
     * @return Nombre de la vista de éxito ("pagos/matricula-success").
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

    /**
     * Página de confirmación tras la renovación exitosa de matrícula en Stripe Checkout.
     * Restablece las 2 convocatorias oficiales a examen y confirma la regularización del expediente.
     *
     * @param sessionId Identificador de la sesión de Stripe devuelto en la URL de retorno.
     * @param model Modelo para abastecer a la plantilla Thymeleaf.
     * @param sesion Sesión HTTP del usuario.
     * @return Nombre de la vista ("pagos/renovacion-success").
     */
    @GetMapping("/renovacion/success")
    public String mostrarExitoPagoRenovacion(@RequestParam(name = "session_id", required = false) String sessionId, Model model, HttpSession sesion)
    {
        String dniAlumno = (String) sesion.getAttribute("pagoRenovacion_dni");
        Float importeTotal = (Float) sesion.getAttribute("pagoRenovacion_importe");
        String tipoCarnetStr = (String) sesion.getAttribute("pagoRenovacion_tipoCarnet");

        if (dniAlumno != null && !dniAlumno.isBlank())
        {
            Matricula matricula = this.academicoService.renovarMatriculaTrasPago(dniAlumno, sessionId, importeTotal);

            sesion.removeAttribute("pagoRenovacion_dni");
            sesion.removeAttribute("pagoRenovacion_importe");
            sesion.removeAttribute("pagoRenovacion_tipoCarnet");

            model.addAttribute("dniAlumno", dniAlumno);
            model.addAttribute("convocatorias", matricula != null ? matricula.getConvocatorias() : 2);
            model.addAttribute("importeTotal", importeTotal != null ? String.format(Locale.GERMAN, "%.2f", importeTotal) : "250,00");
            model.addAttribute("tipoCarnet", matricula != null && matricula.getPermisoCarnet() != null ? matricula.getPermisoCarnet().getDescripcion() : tipoCarnetStr);
            model.addAttribute("nombreAlumno", matricula != null && matricula.getAlumno() != null ? matricula.getAlumno().getNombre() : "Alumno");
        }
        else
        {
            model.addAttribute("convocatorias", 2);
            model.addAttribute("importeTotal", importeTotal != null ? String.format(Locale.GERMAN, "%.2f", importeTotal) : "250,00");
            model.addAttribute("tipoCarnet", tipoCarnetStr != null ? tipoCarnetStr : "Permiso de Conducir");
            model.addAttribute("nombreAlumno", "Alumno");
        }

        model.addAttribute("sessionId", sessionId);
        return "pagos/renovacion-success";
    }

    /**
     * Página informativa tras la cancelación voluntaria de la renovación de matrícula en Stripe Checkout.
     *
     * @param sesion Sesión HTTP del usuario.
     * @return Nombre de la vista ("pagos/renovacion-cancel").
     */
    @GetMapping("/renovacion/cancel")
    public String mostrarCancelacionPagoRenovacion(HttpSession sesion)
    {
        sesion.removeAttribute("pagoRenovacion_dni");
        sesion.removeAttribute("pagoRenovacion_importe");
        sesion.removeAttribute("pagoRenovacion_tipoCarnet");

        return "pagos/renovacion-cancel";
    }
}
