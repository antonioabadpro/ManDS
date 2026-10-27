package com.autoescuela.erp.examenes.controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.format.annotation.DateTimeFormat;
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
import com.autoescuela.erp.core.enums.EstadoSolicitud;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.enums.TipoExamen;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.examenes.dto.CalificarExamenDTO;
import com.autoescuela.erp.examenes.dto.CupoExamenDTO;
import com.autoescuela.erp.examenes.dto.FijarFechasExamenDTO;
import com.autoescuela.erp.examenes.dto.GestionarSolicitudExamenDTO;
import com.autoescuela.erp.examenes.model.CupoExamen;
import com.autoescuela.erp.examenes.model.Examen;
import com.autoescuela.erp.examenes.model.FechaExamen;
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
     * Permite seleccionar y visualizar cualquier mes presente o futuro.
     * Por defecto muestra todas las solicitudes en estado PENDIENTE.
     */
    @GetMapping("/admin/examenes")
    public String mostrarExamenesAdmin(
            @RequestParam(value = "mes", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate mesParam,
            Model model,
            RedirectAttributes redirectAttributes)
    {
        // Si el usuario accede con parámetro de consulta en la URL (?mes=...), limpiamos la URL
        // redirigiendo canónicamente a /admin/examenes con Flash Attribute (patrón idéntico a AuthenticationController)
        if (mesParam != null)
        {
            redirectAttributes.addFlashAttribute("mes", mesParam.withDayOfMonth(1));
            return "redirect:/admin/examenes";
        }

        LocalDate mes = model.containsAttribute("mes")
                ? ((LocalDate) model.getAttribute("mes")).withDayOfMonth(1)
                : LocalDate.now().withDayOfMonth(1);

        Optional<FechaExamen> fechasMesOpt = this.examenService.obtenerFechasExamen(mes);
        List<CupoExamen> cuposMes = this.examenService.obtenerCuposExamen(mes);

        // Por defecto, muestra todas las solicitudes en estado PENDIENTE sin pulsar buscar (orden FIFO)
        List<SolicitudExamen> solicitudesPendientes = this.examenService.buscarSolicitudesConFiltros(null, null, null, null, EstadoSolicitud.PENDIENTE);

        model.addAttribute("fechasMes", fechasMesOpt.orElse(null));
        model.addAttribute("cuposMes", cuposMes);
        model.addAttribute("solicitudes", solicitudesPendientes);
        model.addAttribute("totalSolicitudes", solicitudesPendientes.size());
        model.addAttribute("mesSeleccionado", mes);
        model.addAttribute("mesAnioDefecto", YearMonth.from(mes).toString());
        model.addAttribute("mesSiguiente", mes.plusMonths(1));
        model.addAttribute("mesAnterior", mes.minusMonths(1));
        model.addAttribute("profesores", this.profesorService.obtenerTodosLosProfesores());
        model.addAttribute("tiposCarnet", TipoCarnet.values());
        model.addAttribute("tiposExamen", TipoExamen.values());
        model.addAttribute("estadosSolicitud", EstadoSolicitud.values());

        return "admin/examenes";
    }

    /**
     * Endpoint HTMX para buscar y filtrar solicitudes de examen DGT.
     */
    @GetMapping("/admin/examenes/buscar")
    public String buscarSolicitudesAdmin(
            @RequestParam(value = "terminoBusqueda", required = false) String terminoBusqueda,
            @RequestParam(value = "profesorId", required = false) Long profesorId,
            @RequestParam(value = "tipoPermiso", required = false) TipoCarnet tipoPermiso,
            @RequestParam(value = "tipoExamen", required = false) TipoExamen tipoExamen,
            @RequestParam(value = "estado", required = false) EstadoSolicitud estado,
            Model model)
    {
        List<SolicitudExamen> solicitudes = this.examenService.buscarSolicitudesConFiltros(
                terminoBusqueda, profesorId, tipoPermiso, tipoExamen, estado);

        model.addAttribute("solicitudes", solicitudes);
        model.addAttribute("totalSolicitudes", solicitudes.size());
        model.addAttribute("esBusquedaHtmx", true);

        return "admin/fragments/tabla-solicitudes-admin :: tablaSolicitudesFragment";
    }

    /**
     * Endpoint HTMX que devuelve exclusivamente el fragmento de convocatorias y cupos DGT del mes.
     * Permite la navegación mensual sin alterar la URL principal ni recargar la tabla de solicitudes.
     */
    @GetMapping("/admin/examenes/jornadas")
    public String obtenerJornadasDgtMes(
            @RequestParam(value = "mes", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate mesParam,
            Model model)
    {
        LocalDate mes = mesParam != null ? mesParam.withDayOfMonth(1) : LocalDate.now().withDayOfMonth(1);

        Optional<FechaExamen> fechasMesOpt = this.examenService.obtenerFechasExamen(mes);
        List<CupoExamen> cuposMes = this.examenService.obtenerCuposExamen(mes);

        model.addAttribute("fechasMes", fechasMesOpt.orElse(null));
        model.addAttribute("cuposMes", cuposMes);
        model.addAttribute("mesSeleccionado", mes);
        model.addAttribute("mesAnioDefecto", YearMonth.from(mes).toString());
        model.addAttribute("mesSiguiente", mes.plusMonths(1));
        model.addAttribute("mesAnterior", mes.minusMonths(1));
        model.addAttribute("tiposCarnet", TipoCarnet.values());

        return "admin/fragments/modal-fechas-cupos-dgt :: modal-fechas-cupos-dgt";
    }

    /**
     * Fija de forma irreversible las dos fechas oficiales de examen DGT del mes.
     */
    @PostMapping("/admin/examenes/fijar-fechas-examen")
    public String guardarConvocatoriasDgt(
            @Valid @ModelAttribute("fijarFechasDTO") FijarFechasExamenDTO dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes)
    {
        LocalDate mesNormalizado = dto.mes() != null ? dto.mes().withDayOfMonth(1) : LocalDate.now().withDayOfMonth(1);
        if (bindingResult.hasErrors())
        {
            redirectAttributes.addFlashAttribute("error", "Los datos de fechas y horas introducidos no son válidos.");
            redirectAttributes.addFlashAttribute("mes", mesNormalizado);
            return "redirect:/admin/examenes";
        }

        try
        {
            this.examenService.fijarFechasExamen(dto.mes(), dto.fechaHora1(), dto.fechaHora2());
            redirectAttributes.addFlashAttribute("mensajeExito", "Convocatorias oficiales DGT fijadas e inmutables para el mes.");
        }
        catch (ReglaNegocioException ex)
        {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        catch (Exception ex)
        {
            redirectAttributes.addFlashAttribute("error", "Formato de fecha u hora incorrecto: " + ex.getMessage());
        }

        redirectAttributes.addFlashAttribute("mes", mesNormalizado);
        return "redirect:/admin/examenes";
    }

    /**
     * Fija de forma irreversible los cupos de examen por tipo de carnet para el mes.
     */
    @PostMapping("/admin/examenes/fijar-cupos-examen")
    public String guardarCuposDgt(
            @RequestParam("mesCupos") String mesStr,
            @RequestParam Map<String, String> allParams,
            RedirectAttributes redirectAttributes)
    {
        LocalDate mesNormalizado;
        try
        {
            if (mesStr.length() == 7) // "YYYY-MM"
            {
                mesNormalizado = YearMonth.parse(mesStr).atDay(1);
            }
            else
            {
                mesNormalizado = LocalDate.parse(mesStr).withDayOfMonth(1);
            }
        }
        catch (Exception ex)
        {
            mesNormalizado = LocalDate.now().withDayOfMonth(1);
        }

        try
        {
            List<CupoExamenDTO> cupos = new ArrayList<>();
            for (TipoCarnet carnet : TipoCarnet.values())
            {
                String teoricoStr = allParams.get("cupoTeorico_" + carnet.name());
                String practicoStr = allParams.get("cupoPractico_" + carnet.name());

                int teorico = (teoricoStr != null && !teoricoStr.isBlank()) ? Integer.parseInt(teoricoStr) : 0;
                int practico = (practicoStr != null && !practicoStr.isBlank()) ? Integer.parseInt(practicoStr) : 0;

                cupos.add(new CupoExamenDTO(carnet, teorico, practico));
            }

            this.examenService.fijarCuposExamen(mesNormalizado, cupos);
            redirectAttributes.addFlashAttribute("mensajeExito", "Cupos de examen por tipo de carnet fijados con éxito.");
        }
        catch (ReglaNegocioException ex)
        {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        catch (Exception ex)
        {
            redirectAttributes.addFlashAttribute("error", "Error al procesar los cupos: " + ex.getMessage());
        }

        redirectAttributes.addFlashAttribute("mes", mesNormalizado);
        return "redirect:/admin/examenes";
    }

    /**
     * Carga el fragmento HTMX del modal de edición/resolución de una solicitud de examen con diagnóstico de cupos.
     */
    @GetMapping("/admin/examenes/{id}/editar-modal")
    public String obtenerModalEditarSolicitud(@PathVariable("id") Long id, Model model)
    {
        try
        {
            GestionarSolicitudExamenDTO diagnostico = this.examenService.obtenerGestionarSolicitud(id);
            model.addAttribute("diagnostico", diagnostico);
            model.addAttribute("solicitud", diagnostico.solicitud());
            return "admin/fragments/modal-editar-solicitud-examen :: modalEditarSolicitud";
        }
        catch (ReglaNegocioException ex)
        {
            model.addAttribute("error", ex.getMessage());
            return "error/404";
        }
    }

    /**
     * Acepta formalmente la solicitud y cita al alumno para Fecha 1 o Fecha 2 del mes.
     * Operación irreversible.
     */
    @PostMapping("/admin/examenes/{id}/aceptar")
    public String aceptarSolicitudExamenAdmin(
            @PathVariable("id") Long id,
            @RequestParam("numeroConvocatoria") int numeroConvocatoria,
            RedirectAttributes redirectAttributes)
    {
        try
        {
            this.examenService.aceptarSolicitudExamen(id, numeroConvocatoria);
            redirectAttributes.addFlashAttribute("mensajeExito", "Solicitud aceptada y alumno citado oficialmente. Jornada bloqueada en el vehículo.");
        }
        catch (ReglaNegocioException ex)
        {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/admin/examenes";
    }

    /**
     * Rechaza formalmente la solicitud de examen con justificación pedagógica obligatoria.
     * Operación irreversible.
     */
    @PostMapping("/admin/examenes/{id}/rechazar")
    public String rechazarSolicitudExamenAdmin(
            @PathVariable("id") Long id,
            @RequestParam("motivoRechazo") String motivoRechazo,
            RedirectAttributes redirectAttributes)
    {
        try
        {
            this.examenService.rechazarSolicitudExamen(id, motivoRechazo);
            redirectAttributes.addFlashAttribute("mensajeExito", "Solicitud de examen rechazada. Se ha notificado la justificación al alumno por email.");
        }
        catch (ReglaNegocioException ex)
        {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

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

