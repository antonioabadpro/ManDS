package com.autoescuela.erp.auth.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.autoescuela.erp.auth.dto.RegistroAlumnoDTO;
import com.autoescuela.erp.auth.service.AuthenticationService;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;


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

    /**
     * Muestra el formulario público de registro de alumnos en 3 pasos (CU-002).
     * Si el usuario ya está autenticado, se le redirige a la página principal.
     */
    @GetMapping("/registro")
    public String registro(Model model, Authentication authentication)
    {
        if (autenticacionService.estaAutenticado(authentication))
        {
            return "redirect:/";
        }
        if (!model.containsAttribute("registroDTO"))
        {
            model.addAttribute("registroDTO", new RegistroAlumnoDTO());
        }
        return "auth/registro";
    }

    /**
     * Procesa la solicitud de registro público de un nuevo alumno.
     * Valida los datos mediante Bean Validation, ejecuta las comprobaciones de negocio
     * y persiste al nuevo alumno en la base de datos redirigiendo a la pantalla de login.
     *
     * @param registroDTO Datos enviados por el usuario desde el formulario.
     * @param bindingResult Resultado de las validaciones declarativas.
     * @param model Modelo para la vista en caso de error.
     * @param redirectAttributes Atributos de redirección para mensajes flash.
     * @return Redirección a login tras éxito o retorno a la vista de registro en caso de error.
     */
    @PostMapping("/registro")
    public String procesarRegistro(@Valid @ModelAttribute("registroDTO") RegistroAlumnoDTO registroDTO, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes)
    {
        if (bindingResult.hasErrors())
        {
            String mensajeError = bindingResult.getAllErrors().isEmpty()
                    ? "Por favor, revise los datos introducidos en el formulario."
                    : bindingResult.getAllErrors().getFirst().getDefaultMessage();
            model.addAttribute("error", mensajeError);
            return "auth/registro";
        }

        try
        {
            autenticacionService.registrarAlumno(registroDTO);
            redirectAttributes.addFlashAttribute("mensajeExito", "Registro completado con éxito.");
            return "redirect:/login?registrado=true";
        }
        catch (ReglaNegocioException ex)
        {
            model.addAttribute("error", ex.getMessage());
            return "auth/registro";
        }
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

    /**
     * Endpoint HTMX para validar de forma temprana la disponibilidad y formato del nombre de usuario.
     * Devuelve un fragmento Thymeleaf con el mensaje de error o un fragmento vacío si es válido.
     */
    @PostMapping("/registro/validar-usuario")
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

        if (autenticacionService.existeNombreUsuario(limpio))
        {
            model.addAttribute("mensaje", "El nombre de usuario ya está registrado en el sistema.");
            return "auth/registro :: mensaje-error";
        }

        return "auth/registro :: fragmento-vacio";
    }

    /**
     * Endpoint HTMX para validar de forma temprana el formato y unicidad del correo electrónico.
     */
    @PostMapping("/registro/validar-correo")
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

        if (autenticacionService.existeCorreo(limpio))
        {
            model.addAttribute("mensaje", "El correo electrónico ya está registrado en el sistema.");
            return "auth/registro :: mensaje-error";
        }

        return "auth/registro :: fragmento-vacio";
    }

    /**
     * Endpoint HTMX para validar de forma temprana el formato y unicidad del DNI/NIE.
     */
    @PostMapping("/registro/validar-dni")
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

        if (autenticacionService.existeDni(limpio))
        {
            model.addAttribute("mensaje", "El DNI/NIE ya está registrado en el sistema.");
            return "auth/registro :: mensaje-error";
        }

        return "auth/registro :: fragmento-vacio";
    }

    @PostMapping("/registro/validar-telefono")
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

        if (autenticacionService.existeTelefono(limpio))
        {
            model.addAttribute("mensaje", "El teléfono ya está registrado en el sistema.");
            return "auth/registro :: mensaje-error";
        }

        return "auth/registro :: fragmento-vacio";
    }

}
