package com.autoescuela.erp.usuarios.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.autoescuela.erp.core.security.UserDetailsImpl;

@Controller
@RequestMapping("/profesor")
public class ProfesorController
{
    /**
     * Muestra el panel de control (dashboard) del Profesor.
     *
     * @param userDetails Información del usuario autenticado inyectada por Spring Security.
     * @param model Modelo de datos para la plantilla Thymeleaf.
     * @return Nombre de la vista profesor/dashboard.
     */
    @GetMapping("/dashboard")
    public String redirectToDashboard(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model)
    {
        if (userDetails != null)
        {
            model.addAttribute("nombreProfesor", userDetails.getNombreCompleto());
        }
        return "profesor/dashboard";
    }
}
