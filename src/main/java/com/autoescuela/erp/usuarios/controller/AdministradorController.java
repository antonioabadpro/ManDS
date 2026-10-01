package com.autoescuela.erp.usuarios.controller;

import java.time.LocalDate;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.enums.TipoTurno;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.core.security.UserDetailsImpl;
import com.autoescuela.erp.estadisticas.service.EstadisticaService;
import com.autoescuela.erp.examenes.service.ExamenService;
import com.autoescuela.erp.flota.service.FlotaService;
import com.autoescuela.erp.usuarios.dto.AltaProfesorDTO;
import com.autoescuela.erp.usuarios.dto.EditarPerfilAdminDTO;
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
    private final FlotaService flotaService;
    private final ExamenService examenService;
    private final EstadisticaService estadisticaService;

    /**
     * Proporciona el nombre corto/de pila del Administrador a todas las vistas para evitar
     * desbordamientos horizontales en el pie de la barra lateral.
     */
    @ModelAttribute("nomUsuario")
    public String mostrarNombreUsuario(@AuthenticationPrincipal Object principal)
    {
        if (principal instanceof UserDetailsImpl userDetails)
        {
            return userDetails.getNombreUsuario();
        }
        else if (principal instanceof UserDetails user)
        {
            return user.getUsername();
        }
        return "nomUsuario";
    }

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
        if (userDetails != null)
        {
            model.addAttribute("nombreAdmin", userDetails.getNombreCompleto());
        }

        cargarDatosDashboard(model);
        cargarCatalogosAltaProfesor(model);

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

        if (principal instanceof UserDetailsImpl userDetails)
        {
            model.addAttribute("nombreAdmin", userDetails.getNombreCompleto());
        }

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
                return "fragments/modal-alta-profesor :: #modal-alta-profesor";
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
                return "fragments/modal-alta-profesor :: #modal-alta-profesor";
            }
            return esDesdeProfesores ? "admin/profesores" : "admin/dashboard";
        }
    }

    /**
     * Muestra la vista de gestión de profesores con carga de flota disponible, catálogo de carnets y turnos.
     */
    @GetMapping("/profesores")
    public String mostrarProfesores(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model)
    {
        if (userDetails != null)
        {
            model.addAttribute("nombreAdmin", userDetails.getNombreCompleto());
        }

        cargarCatalogosAltaProfesor(model);

        return "admin/profesores";
    }

    /**
     * Carga en el modelo las métricas globales y los listados resumidos del dashboard.
     */
    private void cargarDatosDashboard(Model model)
    {
        model.addAttribute("estadisticas", this.estadisticaService.obtenerEstadisticasDashboard());
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
        model.addAttribute("tiposCarnet", TipoCarnet.values());
        model.addAttribute("turnos", TipoTurno.values());
    }

    /**
     * Muestra la vista de gestión y expedientes de alumnos.
     */
    @GetMapping("/alumnos")
    public String mostrarAlumnos(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model)
    {
        if (userDetails != null)
        {
            model.addAttribute("nombreAdmin", userDetails.getNombreCompleto());
        }
        return "admin/alumnos";
    }

    /**
     * Muestra la vista de gestión de flota de vehículos.
     */
    @GetMapping("/flota")
    public String mostrarFlota(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model)
    {
        if (userDetails != null)
        {
            model.addAttribute("nombreAdmin", userDetails.getNombreCompleto());
        }
        return "admin/flota";
    }

    /**
     * Muestra la bandeja de incidencias y averías mecánicas reportadas.
     */
    @GetMapping("/incidencias")
    public String mostrarIncidencias(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model)
    {
        if (userDetails != null)
        {
            model.addAttribute("nombreAdmin", userDetails.getNombreCompleto());
        }
        return "admin/incidencias";
    }

    /**
     * Muestra la gestión de convocatorias y solicitudes DGT (cola FIFO y cupos).
     */
    @GetMapping("/examenes")
    public String mostrarExamenes(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model)
    {
        if (userDetails != null)
        {
            model.addAttribute("nombreAdmin", userDetails.getNombreCompleto());
        }
        return "admin/examenes";
    }

    @PostMapping("/examenes/fijar-fechas-examen")
    public String guardarConvocatoriasDgt(@RequestParam("mesAnio") String mesAnio, @RequestParam("fechaConvocatoria1") LocalDate fecha1, @RequestParam("horaConvocatoria1") String hora1, @RequestParam("fechaConvocatoria2") LocalDate fecha2, @RequestParam("horaConvocatoria2") String hora2, RedirectAttributes redirectAttributes)
    {
        // Lógica de servicio: fijar fechas y bloquear agendas de vehículos
        redirectAttributes.addFlashAttribute("mensajeExito", "Convocatorias DGT fijadas correctamente.");
        return "redirect:/admin/examenes";
    }

    /**
     * Muestra la agenda global de prácticas de profesores y vehículos.
     */
    @GetMapping("/practicas")
    public String mostrarPracticas(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model)
    {
        if (userDetails != null)
        {
            model.addAttribute("nombreAdmin", userDetails.getNombreCompleto());
        }
        return "admin/practicas";
    }

    /**
     * Muestra el panel de estadísticas globales y métricas de la autoescuela.
     */
    @GetMapping("/estadisticas")
    public String mostrarEstadisticas(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model)
    {
        if (userDetails != null)
        {
            model.addAttribute("nombreAdmin", userDetails.getNombreCompleto());
        }
        return "admin/estadisticas";
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
