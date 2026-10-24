package com.autoescuela.erp.academico.controller;

import java.util.Optional;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.autoescuela.erp.academico.model.Matricula;
import com.autoescuela.erp.academico.service.AcademicoService;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.pagos.dto.SesionPagoDTO;
import com.autoescuela.erp.pagos.service.PagoStripeService;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.service.AlumnoService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

/**
 * Controlador de dominio para los trámites académicos de matriculación, renovación y compra de saldo de clases.
 * Integra las peticiones del portal del alumno con la pasarela Stripe Checkout.
 */
@Controller
@RequiredArgsConstructor
public class MatriculaController
{
    private final AcademicoService academicoService;
    private final AlumnoService alumnoService;
    private final PagoStripeService pagoStripeService;

    /**
     * Muestra el panel de adquisición de clases prácticas, bonos y renovación de matrícula.
     */
    @GetMapping("/alumno/pagos")
    public String mostrarPagos(@AuthenticationPrincipal Object principal, Model model)
    {
        Alumno alumno = this.alumnoService.obtenerAlumnoActual(principal);
        if (alumno == null)
        {
            return "redirect:/login";
        }

        Matricula matricula = this.academicoService.buscarMatriculaActiva(alumno).orElse(null);
        boolean esVehiculoPesado = matricula != null && matricula.getPermisoCarnet() != null &&
                (matricula.getPermisoCarnet().name().startsWith("PERMISO_C") || matricula.getPermisoCarnet().name().startsWith("PERMISO_D"));
        boolean convocatoriasAgotadas = matricula != null && matricula.tieneConvocatoriasAgotadas();

        model.addAttribute("matricula", matricula);
        model.addAttribute("esVehiculoPesado", esVehiculoPesado);
        model.addAttribute("convocatoriasAgotadas", convocatoriasAgotadas);

        return "alumno/pagos";
    }

    /**
     * Inicia el proceso de pago para la compra de saldo de clases prácticas o bonos a través de Stripe Checkout.
     */
    @PostMapping("/alumno/pagos/comprar-clases")
    public String comprarClasesPracticas(
            @AuthenticationPrincipal Object principal,
            @RequestParam("tipoProducto") String tipoProducto,
            @RequestParam(value = "cantidadClases", defaultValue = "1") Integer cantidadClases,
            RedirectAttributes redirectAttributes,
            HttpSession sesion)
    {
        Alumno alumno = this.alumnoService.obtenerAlumnoActual(principal);
        if (alumno == null)
        {
            return "redirect:/login";
        }

        int clasesCompradas = 1;
        boolean esOferta = false;

        Matricula matricula = this.academicoService.buscarMatriculaActiva(alumno).orElse(null);
        if (matricula == null)
        {
            redirectAttributes.addFlashAttribute("error", "No se encontró un expediente de matrícula activo para recargar saldo.");
            return "redirect:/alumno/pagos";
        }

        // Regla: Solo puede comprar saldo si tiene saldo 0 y no tiene clases pendientes
        if (matricula.getSaldoClases() != null && matricula.getSaldoClases() > 0)
        {
            redirectAttributes.addFlashAttribute("error", "No puedes adquirir nuevas clases ni bonos mientras dispongas de saldo activo en tu expediente.");
            return "redirect:/alumno/pagos";
        }

        // Regla: Bloqueo de compra de clases si ha agotado sus convocatorias
        if (matricula.tieneConvocatoriasAgotadas())
        {
            redirectAttributes.addFlashAttribute("error", "Has agotado las convocatorias de examen de tu matrícula. No puedes adquirir clases prácticas ni bonos hasta renovar tu matrícula.");
            return "redirect:/alumno/pagos";
        }

        switch (tipoProducto != null ? tipoProducto.toUpperCase() : "")
        {
            case "BONO_10":
                clasesCompradas = 10;
                esOferta = true;
                break;
            case "BONO_15":
                clasesCompradas = 15;
                esOferta = true;
                break;
            case "BONO_20":
                clasesCompradas = 20;
                esOferta = true;
                break;
            default:
                if (cantidadClases != null && cantidadClases > 0)
                {
                    clasesCompradas = cantidadClases;
                    esOferta = false;
                }
                break;
        }

        try
        {
            SesionPagoDTO sesionPago = this.pagoStripeService.crearSesionPagoClasesPracticas(matricula.getPermisoCarnet(), alumno.getDni(), clasesCompradas, esOferta);

            sesion.setAttribute("pagoClases_dni", alumno.getDni());
            sesion.setAttribute("pagoClases_numero", clasesCompradas);
            sesion.setAttribute("pagoClases_importe", sesionPago.importeTotal() / 100.0f);
            sesion.setAttribute("pagoClases_tipoCarnet", matricula.getPermisoCarnet().getDescripcion());

            return "redirect:" + sesionPago.urlStripe();
        }
        catch (ReglaNegocioException ex)
        {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/alumno/pagos";
        }
    }

    /**
     * Inicia el proceso de pago para la renovación de matrícula al agotar convocatorias oficiales conectando con Stripe Checkout (Regla 7.2).
     */
    @PostMapping("/alumno/pagos/renovar-matricula")
    public String renovarMatricula(
            @AuthenticationPrincipal Object principal,
            RedirectAttributes redirectAttributes,
            HttpSession sesion)
    {
        Alumno alumno = this.alumnoService.obtenerAlumnoActual(principal);
        if (alumno == null)
        {
            return "redirect:/login";
        }

        Matricula matricula = this.academicoService.buscarMatriculaActiva(alumno).orElse(null);
        if (matricula == null)
        {
            redirectAttributes.addFlashAttribute("error", "No se encontró un expediente de matrícula activo para tramitar la renovación.");
            return "redirect:/alumno/pagos";
        }

        if (matricula.getConvocatorias() != null && matricula.getConvocatorias() > 0)
        {
            redirectAttributes.addFlashAttribute("error", "Aún dispones de convocatorias de examen vigentes (" + matricula.getConvocatorias() + " restantes). No es necesario renovar tu matrícula.");
            return "redirect:/alumno/pagos";
        }

        try
        {
            SesionPagoDTO sesionPago = this.pagoStripeService.crearSesionPagoRenovacionMatricula(matricula.getPermisoCarnet(), alumno.getDni());

            sesion.setAttribute("pagoRenovacion_dni", alumno.getDni());
            sesion.setAttribute("pagoRenovacion_importe", sesionPago.importeTotal() / 100.0f);
            sesion.setAttribute("pagoRenovacion_tipoCarnet", matricula.getPermisoCarnet().getDescripcion());

            return "redirect:" + sesionPago.urlStripe();
        }
        catch (ReglaNegocioException ex)
        {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/alumno/pagos";
        }
    }

    /**
     * Inicia el proceso de pago para matricularse en un nuevo permiso de conducir (1 carnet activo simultáneo).
     */
    @PostMapping("/alumno/matricular")
    public String matricularNuevoCarnet(
            @RequestParam("tipoCarnet") TipoCarnet tipoCarnet,
            @AuthenticationPrincipal Object principal,
            RedirectAttributes redirectAttributes,
            HttpSession sesion)
    {
        Alumno alumno = this.alumnoService.obtenerAlumnoActual(principal);
        if (alumno == null)
        {
            return "redirect:/login";
        }

        if (tipoCarnet == null)
        {
            redirectAttributes.addFlashAttribute("error", "Debes seleccionar un tipo de permiso para formalizar la matrícula.");
            return "redirect:/alumno/dashboard";
        }

        Optional<Matricula> matriculaActiva = this.academicoService.buscarMatriculaActiva(alumno);
        if (matriculaActiva.isPresent())
        {
            redirectAttributes.addFlashAttribute("error", "Ya tienes una matrícula activa para el " +
                    matriculaActiva.get().getPermisoCarnet().getDescripcion() + ". No puedes cursar más de un permiso simultáneamente.");
            return "redirect:/alumno/dashboard";
        }

        try
        {
            SesionPagoDTO sesionPago = this.pagoStripeService.crearSesionPagoMatricula(tipoCarnet, alumno.getDni());

            sesion.setAttribute("dniAlumno", alumno.getDni());
            sesion.setAttribute("tipoCarnet", tipoCarnet.name());
            sesion.setAttribute("importeTotal", sesionPago.importeTotal() / 100.0f);

            return "redirect:" + sesionPago.urlStripe();
        }
        catch (ReglaNegocioException ex)
        {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/alumno/dashboard";
        }
    }
}
