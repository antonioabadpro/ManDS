package com.autoescuela.erp.flota.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.autoescuela.erp.core.enums.EstadoIncidencia;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.flota.dto.IncidenciaDetalleDTO;
import com.autoescuela.erp.flota.dto.IncidenciaResumenDTO;
import com.autoescuela.erp.flota.dto.ReportarIncidenciaDTO;
import com.autoescuela.erp.flota.model.IncidenciaVehiculo;
import com.autoescuela.erp.flota.model.Vehiculo;
import com.autoescuela.erp.flota.service.FlotaService;
import com.autoescuela.erp.flota.service.IncidenciaVehiculoService;
import com.autoescuela.erp.usuarios.model.Profesor;
import com.autoescuela.erp.usuarios.service.ProfesorService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador de dominio para la gestión de incidencias y averías mecánicas de la flota.
 * Atiende las peticiones de Administrador (/admin/incidencias) y Profesor (/profesor/vehiculo/incidencia).
 */
@Controller
@RequiredArgsConstructor
public class IncidenciaVehiculoController
{
    private final IncidenciaVehiculoService incidenciaVehiculoService;
    private final ProfesorService profesorService;
    private final FlotaService flotaService;

    @Value("${app_base_url:http://localhost:8080}")
    private String appBaseUrl;

    // =========================================================================
    // 1. ENDPOINTS DE ADMINISTRADOR (/admin/incidencias)
    // =========================================================================

    /**
     * Muestra la bandeja de incidencias y averías mecánicas reportadas.
     */
    @GetMapping("/admin/incidencias")
    public String mostrarIncidencias(Model model)
    {
        List<IncidenciaResumenDTO> incidencias = this.incidenciaVehiculoService.obtenerTodasLasIncidencias();
        long totalPendientes = this.incidenciaVehiculoService.contarPorEstado(EstadoIncidencia.PENDIENTE);
        long totalEnProceso = this.incidenciaVehiculoService.contarPorEstado(EstadoIncidencia.EN_PROCESO);
        long totalResueltas = this.incidenciaVehiculoService.contarPorEstado(EstadoIncidencia.RESUELTA);

        model.addAttribute("incidencias", incidencias);
        model.addAttribute("totalPendientes", totalPendientes);
        model.addAttribute("totalEnProceso", totalEnProceso);
        model.addAttribute("totalResueltas", totalResueltas);

        return "admin/incidencias";
    }

    /**
     * Endpoint HTMX para cargar los datos de una incidencia pendiente en el modal de gestión.
     */
    @GetMapping("/admin/incidencias/gestionar/{id}")
    public String cargarModalGestionarIncidencia(@PathVariable("id") Long id, Model model)
    {
        IncidenciaDetalleDTO dto = this.incidenciaVehiculoService.obtenerIncidenciaParaDetalle(id);
        model.addAttribute("incidencia", dto);
        model.addAttribute("abrirModalGestionarIncidencia", true);

        return "admin/fragments/modal-gestionar-incidencia :: modal-gestionar-incidencia";
    }

    /**
     * Endpoint HTMX para cargar los datos de una incidencia en el modal informativo de detalle.
     */
    @GetMapping("/admin/incidencias/detalle/{id}")
    public String cargarModalDetalleIncidencia(@PathVariable("id") Long id, Model model)
    {
        IncidenciaDetalleDTO dto = this.incidenciaVehiculoService.obtenerIncidenciaParaDetalle(id);
        model.addAttribute("incidencia", dto);
        model.addAttribute("abrirModalDetalleIncidencia", true);

        return "admin/fragments/modal-detalle-incidencia :: modal-detalle-incidencia";
    }

    /**
     * Procesa la actualización del estado de una incidencia mecánica pendiente (EN_PROCESO o RESUELTA).
     * Aplica la transición a MANTENIMIENTO, cancelación de clases y avisos por correo.
     */
    @PostMapping("/admin/incidencias/gestionar")
    public String gestionarIncidencia(
            @RequestParam("id") Long id,
            @RequestParam("estado") EstadoIncidencia nuevoEstado,
            Model model,
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes)
    {
        boolean esPeticionHtmx = "true".equals(request.getHeader("HX-Request"));

        try
        {
            IncidenciaVehiculo incidencia = this.incidenciaVehiculoService.gestionarIncidencia(id, nuevoEstado);
            String mensajeExito = (nuevoEstado == EstadoIncidencia.EN_PROCESO)
                    ? "La incidencia del vehículo " + incidencia.getVehiculo().getMatricula()
                            + " ha pasado a estado EN PROCESO. El vehículo ha entrado en MANTENIMIENTO, se han cancelado sus clases prácticas y se ha notificado por correo electrónico al profesor y a sus alumnos."
                    : "La incidencia del vehículo " + incidencia.getVehiculo().getMatricula()
                            + " ha sido RESUELTA. El vehículo ha recuperado su operatividad ("
                            + incidencia.getVehiculo().getEstado() + ") y se ha notificado por correo electrónico la reapertura de reservas de clases prácticas.";

            redirectAttributes.addFlashAttribute("mensajeExito", mensajeExito);

            String contextPath = request.getContextPath() != null ? request.getContextPath() : "";
            String destinoRedireccion = contextPath + "/admin/incidencias";

            if (esPeticionHtmx)
            {
                FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
                if (flashMap != null)
                {
                    flashMap.put("mensajeExito", mensajeExito);
                    FlashMapManager flashMapManager = RequestContextUtils.getFlashMapManager(request);
                    if (flashMapManager != null)
                    {
                        flashMapManager.saveOutputFlashMap(flashMap, request, response);
                    }
                }
                response.setHeader("HX-Redirect", destinoRedireccion);
                return null;
            }

            return "redirect:/admin/incidencias";
        }
        catch (ReglaNegocioException ex)
        {
            IncidenciaDetalleDTO dto = this.incidenciaVehiculoService.obtenerIncidenciaParaDetalle(id);
            model.addAttribute("incidencia", dto);
            model.addAttribute("errorGestionarIncidencia", ex.getMessage());
            model.addAttribute("abrirModalGestionarIncidencia", true);

            if (esPeticionHtmx)
            {
                return "admin/fragments/modal-gestionar-incidencia :: modal-gestionar-incidencia";
            }

            model.addAttribute("incidencias", this.incidenciaVehiculoService.obtenerTodasLasIncidencias());
            model.addAttribute("totalPendientes", this.incidenciaVehiculoService.contarPorEstado(EstadoIncidencia.PENDIENTE));
            model.addAttribute("totalEnProceso", this.incidenciaVehiculoService.contarPorEstado(EstadoIncidencia.EN_PROCESO));
            model.addAttribute("totalResueltas", this.incidenciaVehiculoService.contarPorEstado(EstadoIncidencia.RESUELTA));
            return "admin/incidencias";
        }
    }

    // =========================================================================
    // 2. ENDPOINTS DE PROFESOR (/profesor/vehiculo/incidencia)
    // =========================================================================

    /**
     * Registra una nueva incidencia o avería mecánica sobre el vehículo asignado (CU-027).
     * Soporta peticiones tradicionales y peticiones dinámicas HTMX.
     */
    @PostMapping("/profesor/vehiculo/incidencia")
    public String reportarIncidencia(
            @AuthenticationPrincipal Object principal,
            @Valid @ModelAttribute("reportarIncidenciaDTO") ReportarIncidenciaDTO dto,
            BindingResult bindingResult,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model,
            RedirectAttributes redirectAttributes)
    {
        Profesor profesor = this.profesorService.obtenerProfesorActual(principal);
        if (profesor == null)
        {
            return "redirect:/login";
        }

        boolean esPeticionHtmx = "true".equals(request.getHeader("HX-Request"));

        Vehiculo vehiculo = profesor.getVehiculo();
        if (vehiculo == null && dto.vehiculoId() != null)
        {
            vehiculo = this.flotaService.buscarPorId(dto.vehiculoId()).orElse(null);
        }

        if (bindingResult.hasErrors())
        {
            if (esPeticionHtmx)
            {
                model.addAttribute("errorReportarIncidencia", "La descripción de la avería debe tener entre 10 y 1000 caracteres.");
                model.addAttribute("vehiculo", vehiculo);
                model.addAttribute("abrirModalReportarIncidencia", true);
                return "profesor/fragments/modal-reportar-incidencia :: modal-reportar-incidencia";
            }
            redirectAttributes.addFlashAttribute("error", "La descripción de la avería debe tener entre 10 y 1000 caracteres.");
            return "redirect:/profesor/vehiculo";
        }

        if (vehiculo == null)
        {
            if (esPeticionHtmx)
            {
                model.addAttribute("errorReportarIncidencia", "No tienes ningún vehículo asignado para reportar incidencias.");
                model.addAttribute("abrirModalReportarIncidencia", true);
                return "profesor/fragments/modal-reportar-incidencia :: modal-reportar-incidencia";
            }
            redirectAttributes.addFlashAttribute("error", "No tienes ningún vehículo asignado para reportar incidencias.");
            return "redirect:/profesor/vehiculo";
        }

        try
        {
            this.incidenciaVehiculoService.reportarIncidencia(profesor, vehiculo, dto, this.appBaseUrl);
        }
        catch (ReglaNegocioException ex)
        {
            if (esPeticionHtmx)
            {
                model.addAttribute("errorReportarIncidencia", ex.getMessage());
                model.addAttribute("vehiculo", vehiculo);
                model.addAttribute("abrirModalReportarIncidencia", true);
                return "profesor/fragments/modal-reportar-incidencia :: modal-reportar-incidencia";
            }
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/profesor/vehiculo";
        }

        String mensajeExito = "Incidencia mecánica reportada correctamente. El Administrador ha sido notificado para su revisión.";
        redirectAttributes.addFlashAttribute("mensajeExito", mensajeExito);

        String contextPath = request.getContextPath() != null ? request.getContextPath() : "";
        String destinoRedireccion = contextPath + "/profesor/vehiculo";

        if (esPeticionHtmx)
        {
            FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
            if (flashMap != null)
            {
                flashMap.put("mensajeExito", mensajeExito);
                FlashMapManager flashMapManager = RequestContextUtils.getFlashMapManager(request);
                if (flashMapManager != null)
                {
                    flashMapManager.saveOutputFlashMap(flashMap, request, response);
                }
            }
            response.setHeader("HX-Redirect", destinoRedireccion);
            return null;
        }

        return "redirect:/profesor/vehiculo";
    }
}

