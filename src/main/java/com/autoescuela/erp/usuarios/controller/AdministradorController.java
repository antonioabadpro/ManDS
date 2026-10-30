package com.autoescuela.erp.usuarios.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.autoescuela.erp.core.enums.EstadoUsuario;
import com.autoescuela.erp.core.enums.TipoTurno;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.core.security.UserDetailsImpl;
import com.autoescuela.erp.estadisticas.service.EstadisticaService;
import com.autoescuela.erp.examenes.service.ExamenService;
import com.autoescuela.erp.flota.service.FlotaService;
import com.autoescuela.erp.usuarios.dto.AltaProfesorDTO;
import com.autoescuela.erp.usuarios.dto.AlumnoDetalleDTO;
import com.autoescuela.erp.usuarios.dto.AlumnoExpedienteDTO;
import com.autoescuela.erp.usuarios.dto.AlumnoResumenDTO;
import com.autoescuela.erp.usuarios.dto.BajaProfesorDTO;
import com.autoescuela.erp.usuarios.dto.EditarPerfilAdminDTO;
import com.autoescuela.erp.usuarios.dto.EditarProfesorDTO;
import com.autoescuela.erp.usuarios.dto.ProfesorDetalleDTO;
import com.autoescuela.erp.usuarios.dto.ProfesorResumenDTO;
import com.autoescuela.erp.usuarios.dto.ReasignarAlumnoDTO;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.model.Profesor;
import com.autoescuela.erp.usuarios.service.AlumnoService;
import com.autoescuela.erp.usuarios.service.ProfesorService;
import com.autoescuela.erp.usuarios.service.UsuarioService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdministradorController
{
    private final UsuarioService usuarioService;
    private final ProfesorService profesorService;
    private final AlumnoService alumnoService;
    private final FlotaService flotaService;
    private final ExamenService examenService;
    private final EstadisticaService estadisticaService;


    /**
     * Muestra el panel de control (dashboard) del Administrador.
     *
     * @param userDetails Información del usuario autenticado inyectada por Spring Security.
     * @param model Modelo de datos para la plantilla Thymeleaf.
     * @return Nombre de la vista admin/dashboard.
     */
    @GetMapping("/dashboard")
    public String mostrarDashboard(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model)
    {
        cargarDatosDashboard(model);
        cargarCatalogosAltaProfesor(model);

        model.addAttribute("nombreCorto", userDetails != null ? userDetails.getNombre() : "Administrador");

        return "admin/dashboard";
    }

    /**
     * Procesa el formulario de alta de nuevo profesor desde el Dashboard general o desde Gestión de Profesores.
     */
    @PostMapping("/profesores/alta")
    public String darAltaProfesor(@AuthenticationPrincipal Object principal, @Valid @ModelAttribute("altaProfesorDTO") AltaProfesorDTO altaProfesorDTO, BindingResult bindingResult, Model model, HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes)
    {
        boolean esPeticionHtmx = "true".equals(request.getHeader("HX-Request"));
        String hxCurrentUrl = request.getHeader("HX-Current-URL");
        String referer = request.getHeader("Referer");
        boolean esDesdeProfesores = (hxCurrentUrl != null && hxCurrentUrl.contains("/admin/profesores"))
                || (referer != null && referer.contains("/admin/profesores"));

        if (bindingResult.hasErrors())
        {
            cargarCatalogosAltaProfesor(model);
            if (!esDesdeProfesores)
            {
                cargarDatosDashboard(model);
            }
            model.addAttribute("abrirModalAltaProfesor", true);
            if (esPeticionHtmx)
            {
                return "admin/fragments/modal-alta-profesor :: #modal-alta-profesor";
            }
            return esDesdeProfesores ? "admin/profesores" : "admin/dashboard";
        }

        try
        {
            String baseUrl = request.getRequestURL().toString().replace(request.getRequestURI(), request.getContextPath());
            this.profesorService.darAltaProfesor(altaProfesorDTO, baseUrl);
            String mensajeExito = "El profesor " + altaProfesorDTO.nombre() + " " + altaProfesorDTO.apellidos()
                    + " ha sido dado de alta correctamente. Se ha enviado un correo electrónico a "
                    + altaProfesorDTO.correo() + " para que configure sus credenciales de acceso.";

            redirectAttributes.addFlashAttribute("mensajeExito", mensajeExito);

            String contextPath = request.getContextPath() != null ? request.getContextPath() : "";
            String destinoRedireccion = contextPath + (esDesdeProfesores ? "/admin/profesores" : "/admin/dashboard");

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

            return esDesdeProfesores ? "redirect:/admin/profesores" : "redirect:/admin/dashboard";
        }
        catch (ReglaNegocioException ex)
        {
            cargarCatalogosAltaProfesor(model);
            if (!esDesdeProfesores)
            {
                cargarDatosDashboard(model);
            }
            model.addAttribute("errorAltaProfesor", ex.getMessage());
            model.addAttribute("abrirModalAltaProfesor", true);
            if (esPeticionHtmx)
            {
                return "admin/fragments/modal-alta-profesor :: #modal-alta-profesor";
            }
            return esDesdeProfesores ? "admin/profesores" : "admin/dashboard";
        }
    }

    /**
     * Muestra la vista de gestión de profesores con carga en tiempo real de todo el profesorado de la BD,
     * catálogos de flota, carnets y turnos.
     */
    @GetMapping("/profesores")
    public String mostrarProfesores(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model)
    {
        List<ProfesorResumenDTO> profesores = this.profesorService.obtenerTodosLosProfesores();
        model.addAttribute("profesores", profesores);
        model.addAttribute("totalProfesores", profesores.size());
        model.addAttribute("totalMatinal", profesores.stream().filter(p -> p.turno() == TipoTurno.MATINAL).count());
        model.addAttribute("totalTarde", profesores.stream().filter(p -> p.turno() == TipoTurno.TARDE).count());

        cargarCatalogosAltaProfesor(model);

        return "admin/profesores";
    }

    /**
     * Endpoint HTMX para cargar los datos del profesor en el modal de edición.
     */
    @GetMapping("/profesores/editar/{id}")
    public String cargarModalEditarProfesor(@PathVariable("id") Long id, Model model)
    {
        EditarProfesorDTO dto = this.profesorService.obtenerProfesorParaEdicion(id);
        boolean tieneClasesPendientes = this.profesorService.tieneClasesPracticasPendientes(id);
        model.addAttribute("editarProfesorDTO", dto);
        model.addAttribute("tieneClasesPendientes", tieneClasesPendientes);
        model.addAttribute("vehiculosDisponibles", this.flotaService.obtenerVehiculosParaEdicionProfesor(id));
        model.addAttribute("abrirModalEditarProfesor", true);

        return "admin/fragments/modal-editar-profesor :: modal-editar-profesor";
    }

    /**
     * Endpoint HTMX para cargar y presentar los datos informativos del profesor en el modal de detalle.
     */
    @GetMapping("/profesores/detalle/{id}")
    public String cargarModalDetalleProfesor(@PathVariable("id") Long id, Model model)
    {
        ProfesorDetalleDTO dto = this.profesorService.obtenerProfesorParaDetalle(id);
        model.addAttribute("profesor", dto);
        model.addAttribute("abrirModalDetalleProfesor", true);

        return "admin/fragments/modal-detalle-profesor :: modal-detalle-profesor";
    }

    /**
     * Procesa la modificación de los datos de un profesor existente.
     */
    @PostMapping("/profesores/editar")
    public String editarProfesor(@AuthenticationPrincipal Object principal, @Valid @ModelAttribute("editarProfesorDTO") EditarProfesorDTO editarProfesorDTO, BindingResult bindingResult, Model model, HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes)
    {
        boolean esPeticionHtmx = "true".equals(request.getHeader("HX-Request"));

        if (bindingResult.hasErrors())
        {
            Long profesorId = editarProfesorDTO != null ? editarProfesorDTO.id() : null;
            boolean tieneClasesPendientes = profesorId != null && this.profesorService.tieneClasesPracticasPendientes(profesorId);
            model.addAttribute("tieneClasesPendientes", tieneClasesPendientes);
            model.addAttribute("vehiculosDisponibles", this.flotaService.obtenerVehiculosParaEdicionProfesor(profesorId));
            model.addAttribute("abrirModalEditarProfesor", true);

            if (esPeticionHtmx)
            {
                return "admin/fragments/modal-editar-profesor :: modal-editar-profesor";
            }
            cargarCatalogosAltaProfesor(model);
            model.addAttribute("profesores", this.profesorService.obtenerTodosLosProfesores());
            return "admin/profesores";
        }

        try
        {
            this.profesorService.modificarProfesor(editarProfesorDTO);
            String mensajeExito = "El profesor " + editarProfesorDTO.nombre() + " " + editarProfesorDTO.apellidos()
                    + " ha sido modificado correctamente.";

            redirectAttributes.addFlashAttribute("mensajeExito", mensajeExito);

            String contextPath = request.getContextPath() != null ? request.getContextPath() : "";
            String destinoRedireccion = contextPath + "/admin/profesores";

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

            return "redirect:/admin/profesores";
        }
        catch (ReglaNegocioException ex)
        {
            Long profesorId = editarProfesorDTO != null ? editarProfesorDTO.id() : null;
            boolean tieneClasesPendientes = profesorId != null && this.profesorService.tieneClasesPracticasPendientes(profesorId);
            model.addAttribute("tieneClasesPendientes", tieneClasesPendientes);
            model.addAttribute("vehiculosDisponibles", this.flotaService.obtenerVehiculosParaEdicionProfesor(profesorId));
            model.addAttribute("errorEditarProfesor", ex.getMessage());
            model.addAttribute("abrirModalEditarProfesor", true);

            if (esPeticionHtmx)
            {
                return "admin/fragments/modal-editar-profesor :: modal-editar-profesor";
            }
            cargarCatalogosAltaProfesor(model);
            model.addAttribute("profesores", this.profesorService.obtenerTodosLosProfesores());
            return "admin/profesores";
        }
    }

    /**
     * Endpoint HTMX para cargar el modal de confirmación de baja lógica de un profesor.
     */
    @GetMapping("/profesores/baja/{id}")
    public String cargarModalBajaProfesor(@PathVariable("id") Long id, Model model)
    {
        ProfesorDetalleDTO profesor = this.profesorService.obtenerProfesorParaDetalle(id);
        List<ProfesorResumenDTO> profesoresDisponibles = profesor.vehiculoTipo() != null
                ? this.profesorService.obtenerProfesoresActivosPorCarnetExcluyendo(profesor.vehiculoTipo(), id)
                : this.profesorService.obtenerProfesoresActivosExcluyendo(id);

        model.addAttribute("profesor", profesor);
        model.addAttribute("profesoresDisponibles", profesoresDisponibles);
        model.addAttribute("bajaProfesorDTO", new BajaProfesorDTO(id, "REASIGNAR", null));
        model.addAttribute("abrirModalBajaProfesor", true);

        return "admin/fragments/modal-baja-profesor :: modal-baja-profesor";
    }

    /**
     * Procesa la solicitud de baja lógica de un profesor.
     */
    @PostMapping("/profesores/baja")
    public String darBajaProfesor(
            @Valid @ModelAttribute("bajaProfesorDTO") BajaProfesorDTO bajaProfesorDTO, BindingResult bindingResult, Model model, HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes)
    {
        boolean esPeticionHtmx = "true".equals(request.getHeader("HX-Request"));

        if (bindingResult.hasErrors())
        {
            String errorMsg = bindingResult.getAllErrors().getFirst().getDefaultMessage();
            Long profesorId = bajaProfesorDTO != null ? bajaProfesorDTO.profesorId() : null;
            if (profesorId != null)
            {
                ProfesorDetalleDTO profesor = this.profesorService.obtenerProfesorParaDetalle(profesorId);
                List<ProfesorResumenDTO> profesoresDisponibles = profesor.vehiculoTipo() != null
                        ? this.profesorService.obtenerProfesoresActivosPorCarnetExcluyendo(profesor.vehiculoTipo(), profesorId)
                        : this.profesorService.obtenerProfesoresActivosExcluyendo(profesorId);

                model.addAttribute("profesor", profesor);
                model.addAttribute("profesoresDisponibles", profesoresDisponibles);
            }
            model.addAttribute("bajaProfesorDTO", bajaProfesorDTO);
            model.addAttribute("errorBajaProfesor", errorMsg);
            model.addAttribute("abrirModalBajaProfesor", true);

            if (esPeticionHtmx)
            {
                return "admin/fragments/modal-baja-profesor :: modal-baja-profesor";
            }
            redirectAttributes.addFlashAttribute("errorBajaProfesor", errorMsg);
            redirectAttributes.addFlashAttribute("abrirModalBajaProfesor", true);
            return "redirect:/admin/profesores";
        }

        try
        {
            Profesor profesorBaja = this.profesorService.darBajaProfesor(bajaProfesorDTO);
            String mensajeExito = "El profesor " + profesorBaja.getNombre() + " " + profesorBaja.getApellidos()
                    + " ha sido dado de baja correctamente (estado INACTIVO). Su vehículo ha quedado libre y sus clases pendientes han sido canceladas.";

            redirectAttributes.addFlashAttribute("mensajeExito", mensajeExito);

            String contextPath = request.getContextPath() != null ? request.getContextPath() : "";
            String destinoRedireccion = contextPath + "/admin/profesores";

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

            return "redirect:/admin/profesores";
        }
        catch (ReglaNegocioException ex)
        {
            Long profesorId = bajaProfesorDTO != null ? bajaProfesorDTO.profesorId() : null;
            if (profesorId != null)
            {
                ProfesorDetalleDTO profesor = this.profesorService.obtenerProfesorParaDetalle(profesorId);
                List<ProfesorResumenDTO> profesoresDisponibles = profesor.vehiculoTipo() != null
                        ? this.profesorService.obtenerProfesoresActivosPorCarnetExcluyendo(profesor.vehiculoTipo(), profesorId)
                        : this.profesorService.obtenerProfesoresActivosExcluyendo(profesorId);

                model.addAttribute("profesor", profesor);
                model.addAttribute("profesoresDisponibles", profesoresDisponibles);
            }
            model.addAttribute("bajaProfesorDTO", bajaProfesorDTO);
            model.addAttribute("errorBajaProfesor", ex.getMessage());
            model.addAttribute("abrirModalBajaProfesor", true);

            if (esPeticionHtmx)
            {
                return "admin/fragments/modal-baja-profesor :: modal-baja-profesor";
            }
            cargarCatalogosAltaProfesor(model);
            model.addAttribute("profesores", this.profesorService.obtenerTodosLosProfesores());
            return "admin/profesores";
        }
    }

    /**
     * Endpoint HTMX para cargar el modal de confirmación de reactivación de un profesor inactivo.
     */
    @GetMapping("/profesores/reactivar/{id}")
    public String cargarModalReactivarProfesor(@PathVariable("id") Long id, Model model)
    {
        ProfesorDetalleDTO profesor = this.profesorService.obtenerProfesorParaDetalle(id);
        model.addAttribute("profesor", profesor);
        model.addAttribute("abrirModalReactivarProfesor", true);

        return "admin/fragments/modal-reactivar-profesor :: modal-reactivar-profesor";
    }

    /**
     * Procesa la reactivación de un profesor inactivo (alta sin vehículo asignado).
     */
    @PostMapping("/profesores/reactivar/{id}")
    public String reactivarProfesor(@PathVariable("id") Long id, Model model, HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes)
    {
        boolean esPeticionHtmx = "true".equals(request.getHeader("HX-Request"));

        try
        {
            Profesor profesorReactivado = this.profesorService.reactivarProfesor(id);
            String mensajeExito = "El profesor " + profesorReactivado.getNombre() + " " + profesorReactivado.getApellidos()
                    + " ha sido reactivado correctamente en estado ACTIVO y sin vehículo asignado. Puede editar su ficha para asignarle un vehículo si lo desea.";

            redirectAttributes.addFlashAttribute("mensajeExito", mensajeExito);

            String contextPath = request.getContextPath() != null ? request.getContextPath() : "";
            String destinoRedireccion = contextPath + "/admin/profesores";

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

            return "redirect:/admin/profesores";
        }
        catch (ReglaNegocioException ex)
        {
            model.addAttribute("profesor", this.profesorService.obtenerProfesorParaDetalle(id));
            model.addAttribute("errorReactivarProfesor", ex.getMessage());
            model.addAttribute("abrirModalReactivarProfesor", true);

            if (esPeticionHtmx)
            {
                return "admin/fragments/modal-reactivar-profesor :: modal-reactivar-profesor";
            }
            cargarCatalogosAltaProfesor(model);
            model.addAttribute("profesores", this.profesorService.obtenerTodosLosProfesores());
            return "admin/profesores";
        }
    }

    /**
     * Carga en el modelo las métricas globales y los listados resumidos del dashboard.
     */
    private void cargarDatosDashboard(Model model)
    {
        model.addAttribute("estadisticas", this.estadisticaService.obtenerEstadisticasDashboardAdmin());
        model.addAttribute("vehiculos", this.flotaService.obtenerVehiculosDashboard());
        model.addAttribute("solicitudesExamen", this.examenService.obtenerSolicitudesExamenDashboard());
    }

    /**
     * Carga en el modelo los catálogos y el DTO necesarios para renderizar el modal de alta de profesor.
     */
    private void cargarCatalogosAltaProfesor(Model model)
    {
        if (!model.containsAttribute("altaProfesorDTO"))
        {
            model.addAttribute("altaProfesorDTO", new AltaProfesorDTO());
        }

        model.addAttribute("vehiculosDisponibles", this.flotaService.obtenerVehiculosDisponiblesParaProfesor());
    }

    /**
     * Muestra la vista de gestión y expedientes de alumnos.
     * Muestra la vista de gestión y expedientes de alumnos con datos reales del servidor.
     */
    @GetMapping("/alumnos")
    public String mostrarAlumnos(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model)
    {
        List<AlumnoResumenDTO> alumnos = this.alumnoService.obtenerTodosLosAlumnos();
        model.addAttribute("alumnos", alumnos);
        model.addAttribute("totalAlumnos", alumnos.size());
        model.addAttribute("totalActivos", alumnos.stream().filter(a -> a.estado() == EstadoUsuario.ACTIVO).count());
        model.addAttribute("totalSinProfesor", alumnos.stream().filter(a -> a.profesorId() == null).count());

        return "admin/alumnos";
    }

    /**
     * Endpoint HTMX para cargar y presentar los datos informativos del alumno en el modal de detalle.
     */
    @GetMapping("/alumnos/detalle/{id}")
    public String cargarModalDetalleAlumno(@PathVariable("id") Long id, Model model)
    {
        AlumnoDetalleDTO dto = this.alumnoService.obtenerAlumnoParaDetalle(id);
        model.addAttribute("alumno", dto);
        model.addAttribute("abrirModalDetalleAlumno", true);

        return "admin/fragments/modal-detalle-alumno :: modal-detalle-alumno";
    }

    /**
     * Endpoint HTMX para cargar y presentar el expediente del alumno con su historial de clases y exámenes.
     */
    @GetMapping("/alumnos/expediente/{id}")
    public String cargarModalExpedienteAlumno(@PathVariable("id") Long id, Model model)
    {
        AlumnoExpedienteDTO expediente = this.alumnoService.obtenerExpedienteAlumno(id);
        model.addAttribute("expediente", expediente);
        model.addAttribute("abrirModalExpedienteAlumno", true);

        return "admin/fragments/modal-expediente-alumno :: modal-expediente-alumno";
    }

    /**
     * Endpoint HTMX para cargar el modal de reasignación de profesor del alumno.
     */
    @GetMapping("/alumnos/reasignar/{id}")
    public String cargarModalReasignarAlumno(@PathVariable("id") Long id, Model model)
    {
        AlumnoDetalleDTO alumno = this.alumnoService.obtenerAlumnoParaDetalle(id);
        int clasesPendientes = this.alumnoService.contarClasesPendientes(id);
        List<ProfesorResumenDTO> profesoresDisponibles = this.profesorService.obtenerProfesoresActivosPorCarnetExcluyendo(alumno.tipoCarnet(), alumno.profesorId());

        model.addAttribute("alumno", alumno);
        model.addAttribute("clasesPendientes", clasesPendientes);
        model.addAttribute("profesoresDisponibles", profesoresDisponibles);
        model.addAttribute("reasignarAlumnoDTO", new ReasignarAlumnoDTO(id, alumno.profesorId() != null ? "REASIGNAR" : "REASIGNAR", null));
        model.addAttribute("abrirModalReasignarAlumno", true);

        return "admin/fragments/modal-reasignar-alumno :: modal-reasignar-alumno";
    }

    /**
     * Procesa la reasignación de profesor o desasignación de un alumno, cancelando clases pendientes y enviando correo.
     */
    @PostMapping("/alumnos/reasignar")
    public String reasignarProfesorAlumno(@Valid @ModelAttribute("reasignarAlumnoDTO") ReasignarAlumnoDTO reasignarAlumnoDTO, BindingResult bindingResult, Model model, HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes)
    {
        boolean esPeticionHtmx = "true".equals(request.getHeader("HX-Request"));
        Long alumnoId = reasignarAlumnoDTO != null ? reasignarAlumnoDTO.alumnoId() : null;

        if (bindingResult.hasErrors())
        {
            String errorMsg = bindingResult.getAllErrors().getFirst().getDefaultMessage();
            if (alumnoId != null)
            {
                AlumnoDetalleDTO alumno = this.alumnoService.obtenerAlumnoParaDetalle(alumnoId);
                model.addAttribute("alumno", alumno);
                model.addAttribute("clasesPendientes", this.alumnoService.contarClasesPendientes(alumnoId));
                model.addAttribute("profesoresDisponibles", this.profesorService.obtenerProfesoresActivosPorCarnetExcluyendo(alumno.tipoCarnet(), alumno.profesorId()));
            }
            model.addAttribute("reasignarAlumnoDTO", reasignarAlumnoDTO);
            model.addAttribute("errorReasignarAlumno", errorMsg);
            model.addAttribute("abrirModalReasignarAlumno", true);
            if (esPeticionHtmx)
            {
                return "admin/fragments/modal-reasignar-alumno :: modal-reasignar-alumno";
            }
            redirectAttributes.addFlashAttribute("errorReasignarAlumno", errorMsg);
            redirectAttributes.addFlashAttribute("abrirModalReasignarAlumno", true);
            return "redirect:/admin/alumnos";
        }

        try
        {
            this.alumnoService.reasignarProfesor(reasignarAlumnoDTO);
            String mensajeExito = "El profesor del alumno ha sido actualizado correctamente. Se han cancelado sus clases pendientes y se le ha notificado por correo electrónico tanto al alumno como al profesor asignado.";
            redirectAttributes.addFlashAttribute("mensajeExito", mensajeExito);

            String contextPath = request.getContextPath() != null ? request.getContextPath() : "";
            String destinoRedireccion = contextPath + "/admin/alumnos";

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

            return "redirect:/admin/alumnos";
        }
        catch (ReglaNegocioException ex)
        {
            if (alumnoId != null)
            {
                AlumnoDetalleDTO alumno = this.alumnoService.obtenerAlumnoParaDetalle(alumnoId);
                model.addAttribute("alumno", alumno);
                model.addAttribute("clasesPendientes", this.alumnoService.contarClasesPendientes(alumnoId));
                model.addAttribute("profesoresDisponibles", this.profesorService.obtenerProfesoresActivosPorCarnetExcluyendo(alumno.tipoCarnet(), alumno.profesorId()));
            }
            model.addAttribute("reasignarAlumnoDTO", reasignarAlumnoDTO);
            model.addAttribute("errorReasignarAlumno", ex.getMessage());
            model.addAttribute("abrirModalReasignarAlumno", true);

            if (esPeticionHtmx)
            {
                return "admin/fragments/modal-reasignar-alumno :: modal-reasignar-alumno";
            }
            return "admin/alumnos";
        }
    }

    /**
     * Endpoint HTMX para cargar el modal de confirmación y advertencia de baja lógica de un alumno.
     */
    @GetMapping("/alumnos/baja/{id}")
    public String cargarModalBajaAlumno(@PathVariable("id") Long id, Model model)
    {
        AlumnoDetalleDTO alumno = this.alumnoService.obtenerAlumnoParaDetalle(id);
        int clasesPendientes = this.alumnoService.contarClasesPendientes(id);

        model.addAttribute("alumno", alumno);
        model.addAttribute("clasesPendientes", clasesPendientes);
        model.addAttribute("abrirModalBajaAlumno", true);

        return "admin/fragments/modal-baja-alumno :: modal-baja-alumno";
    }

    /**
     * Procesa la solicitud de baja lógica de un alumno en el sistema:
     * - Estado a INACTIVO con preservación de histórico.
     * - Desvinculación de su profesor asignado.
     * - Cancelación automática de clases prácticas pendientes.
     * - Envío de notificaciones informativas por correo tanto al alumno como al profesor asignado.
     */
    @PostMapping("/alumnos/baja/{id}")
    public String darBajaAlumno(@PathVariable("id") Long id, Model model, HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes)
    {
        boolean esPeticionHtmx = "true".equals(request.getHeader("HX-Request"));

        try
        {
            Alumno alumnoBaja = this.alumnoService.darBajaAlumno(id);
            String mensajeExito = "El alumno " + alumnoBaja.getNombre() + " " + alumnoBaja.getApellidos()
                    + " ha sido dado de baja correctamente (estado INACTIVO). Sus clases pendientes han sido canceladas y se ha notificado por correo electrónico tanto al alumno como al profesor asignado.";

            redirectAttributes.addFlashAttribute("mensajeExito", mensajeExito);

            String contextPath = request.getContextPath() != null ? request.getContextPath() : "";
            String destinoRedireccion = contextPath + "/admin/alumnos";

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

            return "redirect:/admin/alumnos";
        }
        catch (ReglaNegocioException ex)
        {
            AlumnoDetalleDTO alumno = this.alumnoService.obtenerAlumnoParaDetalle(id);
            int clasesPendientes = this.alumnoService.contarClasesPendientes(id);

            model.addAttribute("alumno", alumno);
            model.addAttribute("clasesPendientes", clasesPendientes);
            model.addAttribute("errorBajaAlumno", ex.getMessage());
            model.addAttribute("abrirModalBajaAlumno", true);

            if (esPeticionHtmx)
            {
                return "admin/fragments/modal-baja-alumno :: modal-baja-alumno";
            }
            model.addAttribute("alumnos", this.alumnoService.obtenerTodosLosAlumnos());
            return "admin/alumnos";
        }
    }

    /**
     * Muestra el formulario de Mi Perfil con los datos personales del Administrador.
     */
    @GetMapping("/perfil")
    public String mostrarPerfil(@AuthenticationPrincipal Object principal, Model model)
    {
        EditarPerfilAdminDTO dto = null;
        if (principal instanceof UserDetailsImpl userDetails)
        {
            model.addAttribute("nombreAdmin", userDetails.getNombreCompleto());
            dto = this.usuarioService.obtenerPerfilAdmin(userDetails.getId());
        }
        else if (principal instanceof UserDetails user)
        {
            model.addAttribute("nombreAdmin", user.getUsername());
            dto = this.usuarioService.obtenerPerfilAdminPorUsername(user.getUsername());
        }

        if (dto == null)
        {
            dto = new EditarPerfilAdminDTO();
        }
        model.addAttribute("perfilDTO", dto);
        return "admin/perfil";
    }

    /**
     * Procesa la actualización de datos personales del perfil del Administrador.
     */
    @PostMapping("/perfil")
    public String actualizarPerfil(@AuthenticationPrincipal Object principal, @Valid @ModelAttribute("perfilDTO") EditarPerfilAdminDTO perfilDTO, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes)
    {
        Long usuarioId = null;
        String nombreAdmin = "Administrador";

        if (principal instanceof UserDetailsImpl userDetails)
        {
            usuarioId = userDetails.getId();
            nombreAdmin = userDetails.getNombreCompleto();
        }
        else if (principal instanceof UserDetails user)
        {
            EditarPerfilAdminDTO existente = this.usuarioService.obtenerPerfilAdminPorUsername(user.getUsername());
            usuarioId = existente != null ? existente.getId() : null;
            nombreAdmin = user.getUsername();
        }

        model.addAttribute("nombreAdmin", nombreAdmin);

        if (bindingResult.hasErrors())
        {
            repoblarCamposLectura(usuarioId, perfilDTO);
            return "admin/perfil";
        }

        try
        {
            if (usuarioId != null)
            {
                this.usuarioService.actualizarPerfilAdmin(usuarioId, perfilDTO);
            }
            redirectAttributes.addFlashAttribute("mensajeExito", "Tus datos personales se han actualizado correctamente.");
            return "redirect:/admin/perfil";
        }
        catch (ReglaNegocioException ex)
        {
            repoblarCamposLectura(usuarioId, perfilDTO);
            model.addAttribute("error", ex.getMessage());
            return "admin/perfil";
        }
    }

    private void repoblarCamposLectura(Long usuarioId, EditarPerfilAdminDTO perfilDTO)
    {
        if (usuarioId != null)
        {
            EditarPerfilAdminDTO original = this.usuarioService.obtenerPerfilAdmin(usuarioId);
            perfilDTO.setNombreUsuario(original.getNombreUsuario());
            perfilDTO.setCorreo(original.getCorreo());
            perfilDTO.setRol(original.getRol());
        }
    }
}
