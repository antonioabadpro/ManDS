package com.autoescuela.erp.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Expone las rutas de autenticación y autorización de la aplicación.
 * Devuelve las vistas de Thymeleaf para el login (/auth/login) y el registro de usuarios (/auth/registro) y recuperación de contraseña (/auth/recuperar).
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
}
