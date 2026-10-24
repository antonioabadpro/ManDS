package com.autoescuela.erp.examenes.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.autoescuela.erp.academico.model.Matricula;
import com.autoescuela.erp.academico.service.AcademicoService;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.examenes.dto.CalificarExamenDTO;
import com.autoescuela.erp.examenes.model.Examen;
import com.autoescuela.erp.examenes.model.SolicitudExamen;
import com.autoescuela.erp.examenes.service.ExamenService;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.model.Profesor;
import com.autoescuela.erp.usuarios.service.AlumnoService;
import com.autoescuela.erp.usuarios.service.ProfesorService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador de dominio unificado para la gestión de convocatorias, actas y notas de examen DGT.
 * Atiende las peticiones de los distintos perfiles (Administrador, Profesor y Alumno) bajo Package-by-Features.
 */
@Controller
@RequiredArgsConstructor
public class ExamenController
{
    private final ExamenService examenService;
    private final ProfesorService profesorService;
    private final AlumnoService alumnoService;
    private final AcademicoService academicoService;

    // =========================================================================
    // 1. ENDPOINTS DE ADMINISTRADOR (/admin/examenes)
    // =========================================================================

    /**
     * Muestra la gestión de convocatorias y solicitudes DGT (cola FIFO y cupos).
     */
    @GetMapping("/admin/examenes")
    public String mostrarExamenesAdmin()
    {
        return "admin/examenes";
    }

    /**
     * Fija las dos fechas oficiales de examen DGT del mes y bloquea las agendas de los vehículos.
     */
    @PostMapping("/admin/examenes/fijar-fechas-examen")
    public String guardarConvocatoriasDgt(
            @RequestParam("mesAnio") String mesAnio,
            @RequestParam("fechaConvocatoria1") LocalDate fecha1,
            @RequestParam("horaConvocatoria1") String hora1,
            @RequestParam("fechaConvocatoria2") LocalDate fecha2,
            @RequestParam("horaConvocatoria2") String hora2,
            RedirectAttributes redirectAttributes)
    {
        redirectAttributes.addFlashAttribute("mensajeExito", "Convocatorias DGT fijadas correctamente.");
        return "redirect:/admin/examenes";
    }

    // =========================================================================
    // 2. ENDPOINTS DE PROFESOR (/profesor/examenes)
    // =========================================================================

    /**
     * Muestra el listado de exámenes oficiales de los alumnos asignados al profesor.
     */
    @GetMapping("/profesor/examenes")
    public String mostrarExamenesProfesor(@AuthenticationPrincipal Object principal, Model model)
    {
        Profesor profesor = this.profesorService.obtenerProfesorActual(principal);
        if (profesor == null)
        {
            return "redirect:/login";
        }

        List<Examen> misExamenes = this.examenService.obtenerExamenesPorProfesor(profesor);
        model.addAttribute("profesor", profesor);
        model.addAttribute("examenes", misExamenes);
        model.addAttribute("calificarExamenDTO", new CalificarExamenDTO());

        return "profesor/examenes";
    }

    /**
     * Devuelve el fragmento Thymeleaf con los detalles de una jornada oficial DGT para su carga dinámica con HTMX.
     */
    @GetMapping("/profesor/examenes/{id}/modal")
    public String obtenerModalExamenDgt(@PathVariable("id") Long id, @AuthenticationPrincipal Object principal, Model model)
    {
        Profesor profesor = this.profesorService.obtenerProfesorActual(principal);
        if (profesor == null)
        {
            return "redirect:/login";
        }

        Optional<Examen> examenOptional = this.examenService.buscarExamenPorId(id);
        if (examenOptional.isEmpty())
        {
            return "error/404";
        }

        Examen examen = examenOptional.get();
        List<Examen> examenes = this.examenService.obtenerExamenesPorProfesor(profesor);

        model.addAttribute("examen", examen);
        model.addAttribute("alumnosCitados", examenes.size());
        return "profesor/fragments/modal-detalle-examen :: modalDetalleExamen";
    }

    /**
     * Asienta la calificación oficial de un examen práctico DGT (CU-025).
     */
    @PostMapping("/profesor/examenes/calificar")
    public String calificarExamen(
            @AuthenticationPrincipal Object principal,
            @Valid @ModelAttribute("calificarExamenDTO") CalificarExamenDTO dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes)
    {
        if (bindingResult.hasErrors())
        {
            redirectAttributes.addFlashAttribute("error", "Debes especificar si el resultado es Apto o No Apto.");
            return "redirect:/profesor/examenes";
        }

        try
        {
            this.examenService.calificarExamenOficial(dto);
            redirectAttributes.addFlashAttribute("mensajeExito", "Calificación registrada con éxito. Resultado: "
                    + (Boolean.TRUE.equals(dto.esApto()) ? "APTO" : "NO APTO") + ".");
        }
        catch (ReglaNegocioException ex)
        {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/profesor/examenes";
    }

    // =========================================================================
    // 3. ENDPOINTS DE ALUMNO (/alumno/examenes y /alumno/notas)
    // =========================================================================

    /**
     * Consulta de histórico y estado de solicitudes y convocatorias del alumno.
     */
    @GetMapping("/alumno/examenes")
    public String mostrarExamenesAlumno(@AuthenticationPrincipal Object principal, Model model)
    {
        Alumno alumno = this.alumnoService.obtenerAlumnoActual(principal);
        if (alumno == null)
        {
            return "redirect:/login";
        }

        Matricula matricula = this.academicoService.buscarMatriculaActiva(alumno)
                .orElseGet(() -> this.academicoService.obtenerUltimaMatricula(alumno).orElse(null));
        List<SolicitudExamen> solicitudes = matricula != null
                ? this.examenService.obtenerSolicitudesPorMatriculaDesc(matricula)
                : this.examenService.obtenerSolicitudesPorAlumnoDesc(alumno);

        model.addAttribute("alumno", alumno);
        model.addAttribute("matricula", matricula);
        model.addAttribute("solicitudes", solicitudes);

        return "alumno/examenes";
    }

    /**
     * Consulta de actas y calificaciones oficiales de exámenes DGT del alumno.
     */
    @GetMapping("/alumno/notas")
    public String mostrarNotasAlumno(@AuthenticationPrincipal Object principal, Model model)
    {
        Alumno alumno = this.alumnoService.obtenerAlumnoActual(principal);
        if (alumno == null)
        {
            return "redirect:/login";
        }

        Matricula matricula = this.academicoService.buscarMatriculaActiva(alumno).orElse(null);
        List<Examen> examenes = this.examenService.obtenerExamenesPorAlumno(alumno);

        long aptos = this.examenService.contarAptosPorAlumno(alumno);
        long noAptos = this.examenService.contarNoAptosPorAlumno(alumno);

        model.addAttribute("matricula", matricula);
        model.addAttribute("examenes", examenes);
        model.addAttribute("totalExamenes", examenes.size());
        model.addAttribute("examenesAprobados", aptos);
        model.addAttribute("examenesSuspensos", noAptos);

        return "alumno/notas";
    }
}

