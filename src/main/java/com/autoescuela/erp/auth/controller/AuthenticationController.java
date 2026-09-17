package com.autoescuela.erp.auth.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.autoescuela.erp.auth.service.AuthenticationService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Expone las rutas de autenticación y autorización de la aplicación.
 * Devuelve las vistas de Thymeleaf para el login (/auth/login), el registro de usuarios (/auth/registro) y recuperación de contraseña (/auth/recuperar-password).
 * Parsea los formularios de login y registro de usuarios y llama a los servicios correspondientes para autenticar o registrar al usuario.
 */
@Controller
@RequiredArgsConstructor
public class AuthenticationController
{
    private final AuthenticationService autenticacionService;

    /**
     * Muestra la vista de inicio de sesión.
     * Si un usuario con una sesión activa intenta acceder a /login, se cierra su sesión
     * automáticamente y se le redirige a la vista limpia de acceso.
     */
    @GetMapping("/login")
    public String iniciarSesion(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
    {
        if (autenticacionService.estaAutenticado(authentication))
        {
            autenticacionService.cerrarSesion(request, response, authentication);
            return "redirect:/login";
        }
        return "auth/login";
    }

    @GetMapping("/registro")
    public String registro()
    {
        return "auth/registro";
    }

    @GetMapping("/recuperar-password")
    public String recuperarPassword(@RequestParam(name = "token", required = false) String token, Model model)
    {
        if (token != null && !token.isBlank())
        {
            model.addAttribute("token", token);
        }
        return "auth/recuperar-password";
    }
}
