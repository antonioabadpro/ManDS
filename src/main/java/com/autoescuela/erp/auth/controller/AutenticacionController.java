package com.autoescuela.erp.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Expone las rutas de autenticación y autorización de la aplicación.
 * Devuelve las vistas de Thymeleaf para el login (/auth/login), el registro de usuarios (/auth/registro) y recuperación de contraseña (/auth/recuperar-password).
 * Parsea los formularios de login y registro de usuarios y llama a los servicios correspondientes para autenticar o registrar al usuario.
 */
@Controller
public class AutenticacionController
{
    @GetMapping("/login")
    public String iniciarSesion()
    {
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
