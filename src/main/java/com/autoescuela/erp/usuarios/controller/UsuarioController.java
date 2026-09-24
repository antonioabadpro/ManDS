package com.autoescuela.erp.usuarios.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.autoescuela.erp.usuarios.service.UsuarioService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/usuario")
public class UsuarioController
{
    private final UsuarioService usuarioService;

    /**
     * Endpoint HTMX para validar de forma temprana la disponibilidad y formato del nombre de usuario.
     * Devuelve un fragmento Thymeleaf con el mensaje de error o un fragmento vacío si es válido.
     */
    @PostMapping("/validar-username")
    public String validarNombreUsuario(@RequestParam(name = "nombreUsuario", required = false) String nombreUsuario, Model model)
    {
        if (nombreUsuario == null || nombreUsuario.isBlank())
        {
            model.addAttribute("mensaje", "El nombre de usuario es obligatorio.");
            return "auth/registro :: mensaje-error";
        }

        String limpio = nombreUsuario.trim();
        if (limpio.length() < 3 || limpio.length() > 50)
        {
            model.addAttribute("mensaje", "El nombre de usuario debe tener entre 3 y 50 caracteres.");
            return "auth/registro :: mensaje-error";
        }

        if (!limpio.matches("^[a-zA-Z0-9_.-]+$"))
        {
            model.addAttribute("mensaje", "El nombre de usuario solo puede contener letras, números, puntos y guiones.");
            return "auth/registro :: mensaje-error";
        }

        if (this.usuarioService.existeNombreUsuario(limpio))
        {
            model.addAttribute("mensaje", "El nombre de usuario ya está registrado en el sistema.");
            return "auth/registro :: mensaje-error";
        }

        return "auth/registro :: fragmento-vacio";
    }

    /**
     * Endpoint HTMX para validar de forma temprana el formato y unicidad del correo electrónico.
     */
    @PostMapping("/validar-correo")
    public String validarCorreo(@RequestParam(name = "correo", required = false) String correo, Model model)
    {
        if (correo == null || correo.isBlank())
        {
            model.addAttribute("mensaje", "El correo electrónico es obligatorio.");
            return "auth/registro :: mensaje-error";
        }

        String limpio = correo.trim().toLowerCase();
        if (!limpio.matches("^[A-Za-z0-9+_.-]+@(.+)$") || limpio.length() > 50)
        {
            model.addAttribute("mensaje", "El formato del correo electrónico no es válido.");
            return "auth/registro :: mensaje-error";
        }

        if (this.usuarioService.existeCorreo(limpio))
        {
            model.addAttribute("mensaje", "El correo electrónico ya está registrado en el sistema.");
            return "auth/registro :: mensaje-error";
        }

        return "auth/registro :: fragmento-vacio";
    }

    /**
     * Endpoint HTMX para validar de forma temprana el formato y unicidad del DNI/NIE.
     */
    @PostMapping("/validar-dni")
    public String validarDni(@RequestParam(name = "dni", required = false) String dni, Model model)
    {
        if (dni == null || dni.isBlank())
        {
            model.addAttribute("mensaje", "El DNI / NIE es obligatorio.");
            return "auth/registro :: mensaje-error";
        }

        String limpio = dni.trim().toUpperCase();
        if (!limpio.matches("^[0-9]{8}[A-Za-z]$|^[XYZxyz][0-9]{7}[A-Za-z]$"))
        {
            model.addAttribute("mensaje", "El formato del DNI/NIE no es válido (ej. 12345678Z o X1234567Z).");
            return "auth/registro :: mensaje-error";
        }

        if (this.usuarioService.existeDni(limpio))
        {
            model.addAttribute("mensaje", "El DNI/NIE ya está registrado en el sistema.");
            return "auth/registro :: mensaje-error";
        }

        return "auth/registro :: fragmento-vacio";
    }

    @PostMapping("/validar-telefono")
    public String validarTelefono(@RequestParam(name = "telefono", required = false) String telefono, Model model)
    {
        if (telefono == null || telefono.isBlank())
        {
            model.addAttribute("mensaje", "El teléfono es obligatorio.");
            return "auth/registro :: mensaje-error";
        }

        String limpio = telefono.trim();
        if (!limpio.matches("^[6789][0-9]{8}$"))
        {
            model.addAttribute("mensaje", "El formato del teléfono no es válido (ej. 600123456).");
            return "auth/registro :: mensaje-error";
        }

        if (this.usuarioService.existeTelefono(limpio))
        {
            model.addAttribute("mensaje", "El teléfono ya está registrado en el sistema.");
            return "auth/registro :: mensaje-error";
        }

        return "auth/registro :: fragmento-vacio";
    }
}
