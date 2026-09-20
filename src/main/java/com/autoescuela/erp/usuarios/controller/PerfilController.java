package com.autoescuela.erp.usuarios.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador de redirección transversal para el acceso al perfil de usuario autenticado.
 */
@Controller
public class PerfilController
{
    /**
     * Redirige al perfil o panel correspondiente según el rol del usuario autenticado.
     */
    @GetMapping("/perfil")
    public String redirigirPerfil(Authentication authentication)
    {
        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        boolean esProfesor = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PROFESOR"));

        boolean esAlumno = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ALUMNO"));

        if (authentication == null || !authentication.isAuthenticated())
        {
            return "redirect:/login";
        }

        if (esAdmin)
        {
            return "redirect:/admin/perfil";
        }

        if (esProfesor)
        {
            return "redirect:/profesor/perfil";
        }

        if (esAlumno)
        {
            return "redirect:/alumno/perfil";
        }

        return "redirect:/login";
    }
}
