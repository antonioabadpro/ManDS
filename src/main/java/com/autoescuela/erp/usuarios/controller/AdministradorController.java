package com.autoescuela.erp.usuarios.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.core.security.UserDetailsImpl;
import com.autoescuela.erp.usuarios.dto.EditarPerfilAdminDTO;
import com.autoescuela.erp.usuarios.service.UsuarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdministradorController
{
    private final UsuarioService usuarioService;

    /**
     * Muestra el panel de control (dashboard) del Administrador.
     *
     * @param userDetails Información del usuario autenticado inyectada por Spring Security.
     * @param model Modelo de datos para la plantilla Thymeleaf.
     * @return Nombre de la vista admin/dashboard.
     */
    @GetMapping("/dashboard")
    public String redirectToDashboard(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model)
    {
        if (userDetails != null)
        {
            model.addAttribute("nombreAdmin", userDetails.getNombreCompleto());
        }
        return "admin/dashboard";
    }

    /**
     * Muestra la vista de gestión de profesores.
     */
    @GetMapping("/profesores")
    public String mostrarProfesores(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model)
    {
        if (userDetails != null)
        {
            model.addAttribute("nombreAdmin", userDetails.getNombreCompleto());
        }
        return "admin/profesores";
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
        else if (principal instanceof org.springframework.security.core.userdetails.UserDetails user)
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
    public String actualizarPerfil(@AuthenticationPrincipal Object principal,
                                   @Valid @ModelAttribute("perfilDTO") EditarPerfilAdminDTO perfilDTO,
                                   BindingResult bindingResult,
                                   Model model,
                                   RedirectAttributes redirectAttributes)
    {
        Long usuarioId = null;
        String nombreAdmin = "Administrador";

        if (principal instanceof UserDetailsImpl userDetails)
        {
            usuarioId = userDetails.getId();
            nombreAdmin = userDetails.getNombreCompleto();
        }
        else if (principal instanceof org.springframework.security.core.userdetails.UserDetails user)
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
            perfilDTO.setDni(original.getDni());
            perfilDTO.setNombreUsuario(original.getNombreUsuario());
            perfilDTO.setCorreo(original.getCorreo());
            perfilDTO.setRol(original.getRol());
        }
    }
}
