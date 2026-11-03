package com.autoescuela.erp.flota.controller;

import java.util.List;

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

import com.autoescuela.erp.core.enums.EstadoVehiculo;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.flota.dto.AltaVehiculoDTO;
import com.autoescuela.erp.flota.dto.EditarVehiculoDTO;
import com.autoescuela.erp.flota.dto.ReportarIncidenciaDTO;
import com.autoescuela.erp.flota.dto.VehiculoDetalleDTO;
import com.autoescuela.erp.flota.dto.VehiculoResumenDTO;
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
 * Controlador de dominio para la gestión del parque móvil de vehículos de la autoescuela.
 * Atiende las peticiones de Administrador (/admin/flota) y Profesor (/profesor/vehiculo).
 */
@Controller
@RequiredArgsConstructor
public class VehiculoController
{
    private final FlotaService flotaService;
    private final ProfesorService profesorService;
    private final IncidenciaVehiculoService incidenciaVehiculoService;

    // =========================================================================
    // 1. ENDPOINTS DE ADMINISTRADOR (/admin/flota)
    // =========================================================================

    /**
     * Muestra la vista de gestión de flota de vehículos con datos reales del parque móvil.
     */
    @GetMapping("/admin/flota")
    public String mostrarFlota(Model model)
    {
        this.cargarDatosMetricasFlota(model);
        if (!model.containsAttribute("altaVehiculoDTO"))
        {
            model.addAttribute("altaVehiculoDTO", new AltaVehiculoDTO());
        }

        return "admin/flota";
    }

    /**
     * Procesa el formulario de alta de un nuevo vehículo en el parque móvil de la autoescuela.
     */
    @PostMapping("/admin/flota/alta")
    public String darAltaVehiculo(
            @Valid @ModelAttribute("altaVehiculoDTO") AltaVehiculoDTO altaVehiculoDTO,
            BindingResult bindingResult,
            Model model,
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes)
    {
        boolean esPeticionHtmx = "true".equals(request.getHeader("HX-Request"));

        if (bindingResult.hasErrors())
        {
            this.cargarDatosMetricasFlota(model);
            model.addAttribute("abrirModalAltaVehiculo", true);
            if (esPeticionHtmx)
            {
                return "admin/fragments/modal-alta-vehiculo :: #modal-alta-vehiculo";
            }
            return "admin/flota";
        }

        try
        {
            Vehiculo vehiculoRegistrado = this.flotaService.darAltaVehiculo(altaVehiculoDTO);
            String mensajeExito = "El vehículo " + vehiculoRegistrado.getMarca() + " " + vehiculoRegistrado.getModelo()
                    + " (" + vehiculoRegistrado.getMatricula() + ") ha sido dado de alta correctamente en estado DISPONIBLE.";

            redirectAttributes.addFlashAttribute("mensajeExito", mensajeExito);

            String contextPath = request.getContextPath() != null ? request.getContextPath() : "";
            String destinoRedireccion = contextPath + "/admin/flota";

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

            return "redirect:/admin/flota";
        }
        catch (ReglaNegocioException ex)
        {
            this.cargarDatosMetricasFlota(model);
            bindingResult.rejectValue("matricula", "error.matricula", ex.getMessage());
            model.addAttribute("errorAltaVehiculo", ex.getMessage());
            model.addAttribute("abrirModalAltaVehiculo", true);
            if (esPeticionHtmx)
            {
                return "admin/fragments/modal-alta-vehiculo :: #modal-alta-vehiculo";
            }
            return "admin/flota";
        }
    }

    /**
     * Endpoint HTMX para validar de forma temprana el formato y unicidad de la matrícula en la BD.
     */
    @PostMapping("/admin/flota/validar-matricula")
    public String validarMatricula(
            @RequestParam(name = "matricula", required = false) String matricula,
            @RequestParam(name = "id", required = false) Long id,
            Model model)
    {
        if (matricula == null || matricula.isBlank())
        {
            model.addAttribute("mensaje", "La matrícula es obligatoria.");
            return "auth/registro :: mensaje-error";
        }

        String limpia = matricula.trim().toUpperCase().replace(" ", "").replace("-", "");
        if (!limpia.matches("^[0-9]{4}[A-Za-z]{3}$"))
        {
            model.addAttribute("mensaje", "Formato de matrícula inválido (ej. 1234-LMN o 1234LMN).");
            return "auth/registro :: mensaje-error";
        }

        if (this.flotaService.existeMatriculaOtroVehiculo(matricula, id))
        {
            model.addAttribute("mensaje", "La matrícula ya está registrada en otro vehículo del sistema.");
            return "auth/registro :: mensaje-error";
        }

        return "auth/registro :: fragmento-vacio";
    }

    /**
     * Endpoint HTMX para cargar los datos del vehículo en el modal de edición.
     */
    @GetMapping("/admin/flota/editar/{id}")
    public String cargarModalEditarVehiculo(@PathVariable("id") Long id, Model model)
    {
        EditarVehiculoDTO dto = this.flotaService.obtenerVehiculoParaEdicion(id);
        VehiculoDetalleDTO detalle = this.flotaService.obtenerVehiculoParaDetalle(id);
        boolean tieneProfesorAsignado = detalle.profesorId() != null;

        model.addAttribute("editarVehiculoDTO", dto);
        model.addAttribute("tieneProfesorAsignado", tieneProfesorAsignado);
        model.addAttribute("profesorAsignadoNombre", detalle.profesorNombreCompleto());
        model.addAttribute("profesorAsignadoTurno", detalle.profesorTurno());
        model.addAttribute("estadoVehiculo", detalle.estado());
        model.addAttribute("abrirModalEditarVehiculo", true);

        return "admin/fragments/modal-editar-vehiculo :: modal-editar-vehiculo";
    }

    /**
     * Procesa la modificación de los datos de un vehículo existente en el parque móvil.
     */
    @PostMapping("/admin/flota/editar")
    public String editarVehiculo(
            @Valid @ModelAttribute("editarVehiculoDTO") EditarVehiculoDTO editarVehiculoDTO,
            BindingResult bindingResult,
            Model model,
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes)
    {
        boolean esPeticionHtmx = "true".equals(request.getHeader("HX-Request"));
        Long vehiculoId = editarVehiculoDTO != null ? editarVehiculoDTO.id() : null;

        if (bindingResult.hasErrors())
        {
            if (vehiculoId != null)
            {
                VehiculoDetalleDTO detalle = this.flotaService.obtenerVehiculoParaDetalle(vehiculoId);
                model.addAttribute("tieneProfesorAsignado", detalle.profesorId() != null);
                model.addAttribute("profesorAsignadoNombre", detalle.profesorNombreCompleto());
                model.addAttribute("profesorAsignadoTurno", detalle.profesorTurno());
                model.addAttribute("estadoVehiculo", detalle.estado());
            }
            model.addAttribute("abrirModalEditarVehiculo", true);

            if (esPeticionHtmx)
            {
                return "admin/fragments/modal-editar-vehiculo :: modal-editar-vehiculo";
            }
            this.cargarDatosMetricasFlota(model);
            if (!model.containsAttribute("altaVehiculoDTO"))
            {
                model.addAttribute("altaVehiculoDTO", new AltaVehiculoDTO());
            }
            return "admin/flota";
        }

        try
        {
            Vehiculo vehiculoModificado = this.flotaService.modificarVehiculo(editarVehiculoDTO);
            String mensajeExito = "El vehículo " + vehiculoModificado.getMarca() + " " + vehiculoModificado.getModelo()
                    + " (" + vehiculoModificado.getMatricula() + ") ha sido modificado correctamente.";

            redirectAttributes.addFlashAttribute("mensajeExito", mensajeExito);

            String contextPath = request.getContextPath() != null ? request.getContextPath() : "";
            String destinoRedireccion = contextPath + "/admin/flota";

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

            return "redirect:/admin/flota";
        }
        catch (ReglaNegocioException ex)
        {
            if (vehiculoId != null)
            {
                VehiculoDetalleDTO detalle = this.flotaService.obtenerVehiculoParaDetalle(vehiculoId);
                model.addAttribute("tieneProfesorAsignado", detalle.profesorId() != null);
                model.addAttribute("profesorAsignadoNombre", detalle.profesorNombreCompleto());
                model.addAttribute("profesorAsignadoTurno", detalle.profesorTurno());
                model.addAttribute("estadoVehiculo", detalle.estado());
            }
            model.addAttribute("errorEditarVehiculo", ex.getMessage());
            model.addAttribute("abrirModalEditarVehiculo", true);

            if (esPeticionHtmx)
            {
                return "admin/fragments/modal-editar-vehiculo :: modal-editar-vehiculo";
            }
            this.cargarDatosMetricasFlota(model);
            if (!model.containsAttribute("altaVehiculoDTO"))
            {
                model.addAttribute("altaVehiculoDTO", new AltaVehiculoDTO());
            }
            return "admin/flota";
        }
    }

    /**
     * Endpoint HTMX para cargar y presentar los datos informativos del vehículo en el modal de detalle.
     */
    @GetMapping("/admin/flota/detalle/{id}")
    public String cargarModalDetalleVehiculo(@PathVariable("id") Long id, Model model)
    {
        VehiculoDetalleDTO dto = this.flotaService.obtenerVehiculoParaDetalle(id);
        model.addAttribute("vehiculo", dto);
        model.addAttribute("abrirModalDetalleVehiculo", true);

        return "admin/fragments/modal-detalle-vehiculo :: modal-detalle-vehiculo";
    }

    /**
     * Endpoint HTMX para cargar el modal de baja lógica y confirmación de un vehículo de la flota.
     */
    @GetMapping("/admin/flota/baja/{id}")
    public String cargarModalBajaVehiculo(@PathVariable("id") Long id, Model model)
    {
        VehiculoDetalleDTO vehiculo = this.flotaService.obtenerVehiculoParaDetalle(id);
        boolean tieneProfesorAsignado = vehiculo.profesorId() != null;
        boolean puedeDarBaja = !tieneProfesorAsignado && vehiculo.estado() == EstadoVehiculo.DISPONIBLE;

        model.addAttribute("vehiculo", vehiculo);
        model.addAttribute("tieneProfesorAsignado", tieneProfesorAsignado);
        model.addAttribute("puedeDarBaja", puedeDarBaja);
        model.addAttribute("abrirModalBajaVehiculo", true);

        return "admin/fragments/modal-baja-vehiculo :: modal-baja-vehiculo";
    }

    /**
     * Procesa la solicitud de baja lógica (borrado lógico) de un vehículo del parque móvil.
     */
    @PostMapping("/admin/flota/baja/{id}")
    public String darBajaVehiculo(
            @PathVariable("id") Long id,
            Model model,
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes)
    {
        boolean esPeticionHtmx = "true".equals(request.getHeader("HX-Request"));

        try
        {
            Vehiculo vehiculoBaja = this.flotaService.darBajaVehiculo(id);
            String mensajeExito = "El vehículo " + vehiculoBaja.getMarca() + " " + vehiculoBaja.getModelo()
                    + " (" + vehiculoBaja.getMatricula() + ") ha sido dado de baja correctamente (estado INACTIVO).";

            redirectAttributes.addFlashAttribute("mensajeExito", mensajeExito);

            String contextPath = request.getContextPath() != null ? request.getContextPath() : "";
            String destinoRedireccion = contextPath + "/admin/flota";

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

            return "redirect:/admin/flota";
        }
        catch (ReglaNegocioException ex)
        {
            VehiculoDetalleDTO vehiculo = this.flotaService.obtenerVehiculoParaDetalle(id);
            boolean tieneProfesorAsignado = vehiculo.profesorId() != null;
            boolean puedeDarBaja = !tieneProfesorAsignado && vehiculo.estado() == EstadoVehiculo.DISPONIBLE;

            model.addAttribute("vehiculo", vehiculo);
            model.addAttribute("tieneProfesorAsignado", tieneProfesorAsignado);
            model.addAttribute("puedeDarBaja", puedeDarBaja);
            model.addAttribute("errorBajaVehiculo", ex.getMessage());
            model.addAttribute("abrirModalBajaVehiculo", true);

            if (esPeticionHtmx)
            {
                return "admin/fragments/modal-baja-vehiculo :: modal-baja-vehiculo";
            }
            this.cargarDatosMetricasFlota(model);
            if (!model.containsAttribute("altaVehiculoDTO"))
            {
                model.addAttribute("altaVehiculoDTO", new AltaVehiculoDTO());
            }
            return "admin/flota";
        }
    }

    // =========================================================================
    // 2. ENDPOINTS DE PROFESOR (/profesor/vehiculo)
    // =========================================================================

    /**
     * Muestra la vista con la ficha técnica del vehículo asignado al profesor y sus incidencias mecánicas.
     */
    @GetMapping("/profesor/vehiculo")
    public String mostrarVehiculoProfesor(@AuthenticationPrincipal Object principal, Model model)
    {
        Profesor profesor = this.profesorService.obtenerProfesorActual(principal);
        if (profesor == null)
        {
            return "redirect:/login";
        }

        Vehiculo vehiculo = profesor.getVehiculo();
        List<IncidenciaVehiculo> incidencias = vehiculo != null
                ? this.incidenciaVehiculoService.obtenerIncidenciasPorVehiculoDesc(vehiculo)
                : this.incidenciaVehiculoService.obtenerIncidenciasPorProfesorDesc(profesor);

        model.addAttribute("profesor", profesor);
        model.addAttribute("vehiculo", vehiculo);
        model.addAttribute("incidencias", incidencias);
        model.addAttribute("reportarIncidenciaDTO", new ReportarIncidenciaDTO());

        return "profesor/vehiculo";
    }

    // =========================================================================
    // MÉTODOS AUXILIARES
    // =========================================================================

    private void cargarDatosMetricasFlota(Model model)
    {
        List<VehiculoResumenDTO> vehiculos = this.flotaService.obtenerTodosLosVehiculos();
        model.addAttribute("vehiculos", vehiculos);
        model.addAttribute("totalVehiculos", vehiculos.size());
        model.addAttribute("totalDisponibles", vehiculos.stream().filter(v -> v.estado() == EstadoVehiculo.DISPONIBLE).count());
        model.addAttribute("totalOcupados", vehiculos.stream().filter(v -> v.estado() == EstadoVehiculo.OCUPADO).count());
        model.addAttribute("totalMantenimiento", vehiculos.stream().filter(v -> v.estado() == EstadoVehiculo.MANTENIMIENTO).count());
    }
}

