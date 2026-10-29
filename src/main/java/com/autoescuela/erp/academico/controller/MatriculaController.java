package com.autoescuela.erp.academico.controller;

import java.util.Optional;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.autoescuela.erp.academico.model.Matricula;
import com.autoescuela.erp.academico.service.AcademicoService;
import com.autoescuela.erp.core.enums.ModalidadMatricula;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.pagos.dto.SesionPagoDTO;
import com.autoescuela.erp.pagos.service.PagoStripeService;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.service.AlumnoService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

/**
 * Controlador de dominio para los trámites académicos de matriculación y renovación de expediente.
 * Integra las peticiones de formalización de matrícula con la pasarela Stripe Checkout.
 */
@Controller
@RequestMapping("/matricula")
@RequiredArgsConstructor
public class MatriculaController
{
    private final AcademicoService academicoService;
    private final AlumnoService alumnoService;
    private final PagoStripeService pagoStripeService;

    /**
     * Inicia el proceso de pago para matricularse en un nuevo permiso de conducir (1 carnet activo simultáneo).
     */
    @PostMapping("/nueva")
    public String matricularNuevoCarnet(@RequestParam("tipoCarnet") TipoCarnet tipoCarnet, @AuthenticationPrincipal Object principal, RedirectAttributes redirectAttributes, HttpSession sesion)
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
            SesionPagoDTO sesionPago = this.pagoStripeService.crearSesionPagoMatricula(alumno.getId(), tipoCarnet, ModalidadMatricula.TEORICO_PRACTICA);

            sesion.setAttribute("alumnoId", alumno.getId());
            sesion.setAttribute("tipoCarnet", tipoCarnet.name());
            sesion.setAttribute("modalidad", ModalidadMatricula.TEORICO_PRACTICA.name());
            sesion.setAttribute("importeTotal", sesionPago.importeTotal() / 100.0f);

            return "redirect:" + sesionPago.urlStripe();
        }
        catch (ReglaNegocioException ex)
        {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/alumno/dashboard";
        }
    }

    /**
     * Inicia el proceso de pago para la renovación de matrícula al agotar convocatorias oficiales conectando con Stripe Checkout
     */
    @PostMapping("/renovar")
    public String renovarMatricula(@AuthenticationPrincipal Object principal, RedirectAttributes redirectAttributes, HttpSession sesion)
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
            SesionPagoDTO sesionPago = this.pagoStripeService.crearSesionPagoRenovacionMatricula(alumno.getId(), matricula.getPermisoCarnet());

            sesion.setAttribute("pagoRenovacion_alumnoId", alumno.getId());
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
}
