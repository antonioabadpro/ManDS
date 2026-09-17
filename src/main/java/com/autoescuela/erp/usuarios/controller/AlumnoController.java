package com.autoescuela.erp.usuarios.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.autoescuela.erp.core.security.UserDetailsImpl;

@Controller
@RequestMapping("/alumno")
public class AlumnoController
{
    /**
     * Redirige al alumno autenticado a su panel de control (dashboard) y añade su nombre completo al modelo para mostrarlo en la vista.
     * @param userDetails - Información del usuario autenticado inyectada por Spring Security.
     * @param model - Modelo de datos para pasar atributos a la vista Thymeleaf.
     * @return String - Nombre de la plantilla Thymeleaf a renderizar (alumno/dashboard).
     */
    @GetMapping("/dashboard")
    public String redirigirDashboard(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model)
    {
        String nomAlumno = userDetails.getNombreCompleto();
        model.addAttribute("nombreAlumno", nomAlumno);

        return "alumno/dashboard";
    }
}
