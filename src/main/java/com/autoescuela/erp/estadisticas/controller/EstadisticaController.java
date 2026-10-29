package com.autoescuela.erp.estadisticas.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import com.autoescuela.erp.academico.model.Matricula;
import com.autoescuela.erp.academico.service.AcademicoService;
import com.autoescuela.erp.estadisticas.dto.EstadisticasAdminDTO;
import com.autoescuela.erp.estadisticas.dto.EstadisticasAlumnoDTO;
import com.autoescuela.erp.estadisticas.dto.EstadisticasProfesorDTO;
import com.autoescuela.erp.estadisticas.service.EstadisticaService;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.model.Profesor;
import com.autoescuela.erp.usuarios.service.AlumnoService;
import com.autoescuela.erp.usuarios.service.ProfesorService;

import lombok.RequiredArgsConstructor;

/**
 * Controlador de dominio unificado para las métricas, informes y analíticas de rendimiento.
 * Atiende las consultas estadísticas de Administrador, Alumno y Profesor.
 */
@Controller
@RequiredArgsConstructor
public class EstadisticaController
{
    private final EstadisticaService estadisticaService;
    private final ProfesorService profesorService;
    private final AlumnoService alumnoService;
    private final AcademicoService academicoService;

    // =========================================================================
    // 1. ENDPOINTS DE ADMINISTRADOR (/admin/estadisticas)
    // =========================================================================

    /**
     * Muestra el panel de estadísticas globales y métricas de la autoescuela.
     */
    @GetMapping("/admin/estadisticas")
    public String mostrarEstadisticasAdmin(Model model)
    {
        EstadisticasAdminDTO stats = this.estadisticaService.obtenerEstadisticasAdmin();
        model.addAttribute("stats", stats);
        return "admin/estadisticas";
    }

    /**
     * Endpoint HTMX para cargar el modal con el desglose completo del rendimiento de todos los profesores.
     */
    @GetMapping("/admin/estadisticas/profesores-modal")
    public String cargarModalEstadisticasProfesores(Model model)
    {
        EstadisticasAdminDTO stats = this.estadisticaService.obtenerEstadisticasAdmin();
        model.addAttribute("profesores", stats.todosProfesores());
        return "admin/fragments/modal-estadisticas-profesores :: modal-estadisticas-profesores";
    }

    /**
     * Endpoint HTMX para cargar el modal con el desglose completo de la demanda de todos los permisos de conducir.
     */
    @GetMapping("/admin/estadisticas/carnets-modal")
    public String cargarModalEstadisticasCarnets(Model model)
    {
        EstadisticasAdminDTO stats = this.estadisticaService.obtenerEstadisticasAdmin();
        model.addAttribute("carnets", stats.todosCarnets());
        return "admin/fragments/modal-estadisticas-carnets :: modal-estadisticas-carnets";
    }

    // =========================================================================
    // 2. ENDPOINTS DE ALUMNO (/alumno/estadisticas)
    // =========================================================================

    /**
     * Muestra las estadísticas personales de progreso, clases y gasto del alumno.
     */
    @GetMapping("/alumno/estadisticas")
    public String mostrarEstadisticasAlumno(
            @AuthenticationPrincipal Object principal,
            @RequestHeader(value = "HX-Request", required = false) String hxRequest,
            Model model)
    {
        Alumno alumno = this.alumnoService.obtenerAlumnoActual(principal);
        if (alumno == null)
        {
            return "redirect:/login";
        }

        Matricula matricula = this.academicoService.buscarMatriculaActiva(alumno).orElse(null);
        EstadisticasAlumnoDTO stats = this.estadisticaService.obtenerEstadisticasAlumno(alumno);

        model.addAttribute("alumno", alumno);
        model.addAttribute("matricula", matricula);
        model.addAttribute("estadisticas", stats);
        model.addAttribute("esVehiculoPesado", stats.esVehiculoPesado());
        model.addAttribute("tarifaClase", stats.tarifaClase());
        model.addAttribute("totalMatriculas", stats.totalMatriculas());
        model.addAttribute("saldoClasesActual", stats.saldoClasesActual());
        model.addAttribute("totalClasesContratadas", stats.totalClasesContratadas());
        model.addAttribute("gastoClases", stats.gastoClases());

        if (hxRequest != null)
        {
            return "alumno/estadisticas :: contenedorEstadisticas";
        }

        return "alumno/estadisticas";
    }

    // =========================================================================
    // 3. ENDPOINTS DE PROFESOR (/profesor/estadisticas)
    // =========================================================================

    /**
     * Muestra el resumen de métricas y rendimiento del profesor conectado.
     */
    @GetMapping("/profesor/estadisticas")
    public String mostrarEstadisticasProfesor(@AuthenticationPrincipal Object principal, Model model)
    {
        Profesor profesor = this.profesorService.obtenerProfesorActual(principal);
        if (profesor == null)
        {
            return "redirect:/login";
        }

        EstadisticasProfesorDTO estadisticas = this.estadisticaService.obtenerEstadisticasProfesor(profesor);

        model.addAttribute("profesor", profesor);
        model.addAttribute("estadisticas", estadisticas);

        return "profesor/estadisticas";
    }
}

