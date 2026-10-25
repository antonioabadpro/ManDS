package com.autoescuela.erp.examenes.controller;

import java.time.LocalDateTime;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.autoescuela.erp.academico.model.Matricula;
import com.autoescuela.erp.academico.service.AcademicoService;
import com.autoescuela.erp.core.enums.ModalidadMatricula;
import com.autoescuela.erp.core.enums.TipoExamen;
import com.autoescuela.erp.examenes.dto.SolicitudExamenDTO;
import com.autoescuela.erp.examenes.service.ExamenService;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.service.AlumnoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador de dominio para la tramitación de solicitudes oficiales de examen DGT.
 * Gestiona el modal HTMX y el procesamiento transaccional de peticiones del alumnado.
 */
@Controller
@RequestMapping("/alumno/examenes")
@RequiredArgsConstructor
public class SolicitudExamenController
{
    private final ExamenService examenService;
    private final AlumnoService alumnoService;
    private final AcademicoService academicoService;

    /**
     * Modal HTMX para formalizar solicitud de examen DGT con verificación de requisitos previos.
     */
    @GetMapping("/solicitar-modal")
    public String obtenerModalSolicitarExamen(@AuthenticationPrincipal Object principal, Model model)
    {
        Alumno alumno = this.alumnoService.obtenerAlumnoActual(principal);
        if (alumno == null)
        {
            return "redirect:/login";
        }

        Matricula matricula = this.academicoService.buscarMatriculaActiva(alumno).orElse(null);
        boolean teoricoAprobado = this.examenService.tieneTeoricoAprobado(alumno);
        boolean practicoAprobado = this.examenService.tienePracticoAprobado(alumno);

        boolean expedienteCerrado = (matricula == null || !matricula.getEstaActiva()) && practicoAprobado;
        boolean esMatriculaActiva = matricula == null || matricula.getEstaActiva();
        boolean convocatoriasAgotadas = matricula != null && (matricula.getConvocatorias() == null || matricula.getConvocatorias() <= 0);
        boolean tieneSolicitudPendiente = this.examenService.tieneSolicitudPendiente(alumno);
        boolean tieneSolicitudAceptada = this.examenService.tieneSolicitudAceptadaConExamenPendiente(alumno);

        boolean puedeSolicitarTeorico = matricula != null
                && matricula.getModalidad() == ModalidadMatricula.TEORICO_PRACTICA
                && !teoricoAprobado;

        boolean puedeSolicitarPractico = matricula != null
                && (matricula.getModalidad() == ModalidadMatricula.PRACTICA || teoricoAprobado);

        model.addAttribute("matricula", matricula);
        model.addAttribute("expedienteCerrado", expedienteCerrado);
        model.addAttribute("sinMatriculaActiva", esMatriculaActiva);
        model.addAttribute("convocatoriasAgotadas", convocatoriasAgotadas);
        model.addAttribute("tienePendiente", tieneSolicitudPendiente);
        model.addAttribute("tieneExamenProgramado", tieneSolicitudAceptada);
        model.addAttribute("teoricoAprobado", teoricoAprobado);
        model.addAttribute("practicoAprobado", practicoAprobado);
        model.addAttribute("puedeSolicitarTeorico", puedeSolicitarTeorico);
        model.addAttribute("puedeSolicitarPractico", puedeSolicitarPractico);

        return "alumno/fragments/modal-solicitar-examen :: modalSolicitarExamen";
    }

    /**
     * Procesa la presentación de una nueva solicitud de examen DGT (CU-038).
     */
    @PostMapping("/solicitar")
    @Transactional
    public String solicitarExamenDgt(
            @AuthenticationPrincipal Object principal,
            @Valid @ModelAttribute("solicitudDTO") SolicitudExamenDTO dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes)
    {
        Alumno alumno = this.alumnoService.obtenerAlumnoActual(principal);
        if (alumno == null)
        {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors())
        {
            redirectAttributes.addFlashAttribute("error", "Debe seleccionar un tipo de prueba oficial válido.");
            return "redirect:/alumno/examenes";
        }

        Matricula matricula = this.academicoService.buscarMatriculaActiva(alumno).orElse(null);
        if (matricula == null || !matricula.getEstaActiva())
        {
            redirectAttributes.addFlashAttribute("error", "Debes tener una matrícula activa para solicitar fecha de examen.");
            return "redirect:/alumno/examenes";
        }

        boolean teoricoAprobado = this.examenService.tieneTeoricoAprobado(alumno);
        boolean practicoAprobado = this.examenService.tienePracticoAprobado(alumno);

        if (practicoAprobado)
        {
            redirectAttributes.addFlashAttribute("error", "Has superado con éxito las pruebas oficiales de tu permiso. Tu expediente se encuentra cerrado.");
            return "redirect:/alumno/examenes";
        }

        if (matricula.getConvocatorias() == null || matricula.getConvocatorias() <= 0)
        {
            redirectAttributes.addFlashAttribute("error", "Has agotado las convocatorias de tu matrícula. Debes tramitar la renovación previamente.");
            return "redirect:/alumno/examenes";
        }

        // Comprobar si ya existe una solicitud PENDIENTE
        boolean tieneSolicitudPendiente = this.examenService.tieneSolicitudPendiente(alumno);
        if (tieneSolicitudPendiente)
        {
            redirectAttributes.addFlashAttribute("error", "Ya tienes una solicitud de examen en trámite pendiente de resolución por la Administración.");
            return "redirect:/alumno/examenes";
        }

        // Comprobar si tiene una solicitud ACEPTADA con examen futuro no celebrado
        boolean tieneSolicitudAceptada = this.examenService.tieneSolicitudAceptadaConExamenPendiente(alumno);
        if (tieneSolicitudAceptada)
        {
            redirectAttributes.addFlashAttribute("error", "Ya dispones de una convocatoria oficial asignada y pendiente de realización. No puedes realizar otra solicitud hasta haber celebrado dicho examen.");
            return "redirect:/alumno/examenes";
        }

        // Validaciones de tipo de examen según modalidad y aprobados previos
        if (dto.tipoExamen() == TipoExamen.TEORICO)
        {
            if (matricula.getModalidad() == ModalidadMatricula.PRACTICA)
            {
                redirectAttributes.addFlashAttribute("error", "No puedes solicitar examen teórico con una matrícula de modalidad práctica.");
                return "redirect:/alumno/examenes";
            }
            if (teoricoAprobado)
            {
                redirectAttributes.addFlashAttribute("error", "Ya has aprobado el examen teórico, no puedes volver a solicitarlo.");
                return "redirect:/alumno/examenes";
            }
        }
        else
        {
            if (dto.tipoExamen() == TipoExamen.PRACTICO)
            {
                if (matricula.getModalidad() == ModalidadMatricula.TEORICO_PRACTICA && !teoricoAprobado)
                {
                    redirectAttributes.addFlashAttribute("error", "Debes haber aprobado el examen teórico previamente para solicitar el examen práctico.");
                    return "redirect:/alumno/examenes";
                }
            }
        }

        this.examenService.registrarSolicitudExamen(alumno, dto.tipoExamen(), LocalDateTime.now(), matricula);

        redirectAttributes.addFlashAttribute("mensajeExito", "Solicitud de examen registrada con éxito. Ha ingresado en la cola oficial para su asignación.");
        return "redirect:/alumno/examenes";
    }
}

