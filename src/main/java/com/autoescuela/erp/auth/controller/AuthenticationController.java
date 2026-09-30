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

import com.autoescuela.erp.auth.dto.ActivarCuentaProfesorDTO;
import com.autoescuela.erp.auth.dto.RegistroAlumnoDTO;
import com.autoescuela.erp.auth.dto.RestablecerPasswordDTO;
import com.autoescuela.erp.auth.service.AuthenticationService;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.pagos.dto.SesionPagoDTO;
import com.autoescuela.erp.pagos.service.PagoStripeService;

import com.autoescuela.erp.usuarios.model.Alumno;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
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
    private final PagoStripeService pagoStripeService;

    /**
     * Muestra la vista de inicio de sesión.
     * Si un usuario con una sesión activa intenta acceder a /login, se cierra su sesión
     * automáticamente y se le redirige a la vista limpia de acceso.
     */
    @GetMapping("/login")
    public String iniciarSesion(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
    {
        if (this.autenticacionService.estaAutenticado(authentication))
        {
            this.autenticacionService.cerrarSesion(request, response, authentication);
            return "redirect:/login";
        }
        return "auth/login";
    }

    /**
     * Muestra el formulario público de registro de alumnos en 3 pasos.
     * Si el usuario ya está autenticado, se le redirige a la página principal.
     */
    @GetMapping("/registro")
    public String registro(Model model, Authentication authentication)
    {
        if (this.autenticacionService.estaAutenticado(authentication))
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
    public String procesarRegistro(@Valid @ModelAttribute("registroDTO") RegistroAlumnoDTO registroDTO, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes, HttpSession sesion)
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
            // Registramos al alumno en la BD con estado INACTIVO (pendiente de confirmación de pago en Stripe)
            Alumno alumno = this.autenticacionService.registrarAlumno(registroDTO);

            // Almacenamos únicamente identificadores en sesión para enriquecer la experiencia tras el retorno
            sesion.setAttribute("dniAlumno", alumno.getDni());
            sesion.setAttribute("tipoCarnet", registroDTO.tipoCarnet().name());

            // Creamos la sesión de la pasarela de pago en Stripe
            SesionPagoDTO sesionPago = this.pagoStripeService.crearSesionPagoMatricula(registroDTO.tipoCarnet(), alumno.getDni());

            // Almacenamos el importe total (en euros) en sesión para recuperarlo en la pantalla de éxito
            float importeTotal = sesionPago.importeTotal() / 100.0f;
            sesion.setAttribute("importeTotal", importeTotal);

            // Redirigimos al usuario a la pasarela de pago de Stripe Checkout
            return "redirect:" + sesionPago.urlStripe();
        }
        catch (ReglaNegocioException ex)
        {
            model.addAttribute("error", ex.getMessage());
            return "auth/registro";
        }
    }

    /**
     * Muestra la vista de recuperación o restablecimiento de contraseña.
     * Si se recibe un parámetro "token", se verifica su validez y vigencia.
     * Si el token es inválido o ha expirado, se redirige inmediatamente con la alerta correspondiente.
     */
    @GetMapping("/recuperar-password")
    public String recuperarPassword(@RequestParam(name = "token", required = false) String token, Model model)
    {
        if (token != null && !token.isBlank())
        {
            model.addAttribute("token", token.trim());
            if (this.autenticacionService.validarTokenRecuperacion(token.trim()) == false)
            {
                return "redirect:/recuperar-password?tokenInvalido=true";
            }
            model.addAttribute("token", token.trim());
        }
        return "auth/recuperar-password";
    }

    /**
     * Procesa la solicitud pública de recuperación de contraseña enviando el correo con el enlace y token efímero.
     * Por motivos de seguridad y privacidad (Anti-User Enumeration), siempre redirige a ?enviado=true.
     * Soporta peticiones reactivas HTMX emitiendo el encabezado HX-Redirect.
     */
    @PostMapping("/recuperar-password")
    public String procesarSolicitudRecuperacion(@RequestParam(name = "correo", required = false) String correo, HttpServletRequest request, HttpServletResponse response)
    {
        boolean esPeticionHtmx = "true".equals(request.getHeader("HX-Request"));
        String baseUrl = request.getRequestURL().toString().replace(request.getRequestURI(), request.getContextPath());
        this.autenticacionService.solicitarRecuperacionPassword(correo, baseUrl);

        if (esPeticionHtmx)
        {
            String contextPath = request.getContextPath() != null ? request.getContextPath() : "";
            response.setHeader("HX-Redirect", contextPath + "/recuperar-password?enviado=true");
            return null;
        }

        return "redirect:/recuperar-password?enviado=true";
    }

    /**
     * Procesa el cambio efectivo de contraseña a partir del token criptográfico temporal.
     * Si la operación tiene éxito, redirige a la vista con el estado de éxito (?exito=true).
     */
    @PostMapping("/recuperar-password/restablecer")
    public String procesarRestablecimientoPassword(@Valid @ModelAttribute("restablecerDTO") RestablecerPasswordDTO dto, BindingResult bindingResult, Model model)
    {
        if (bindingResult.hasErrors())
        {
            String mensajeError = bindingResult.getAllErrors().getFirst().getDefaultMessage();
            model.addAttribute("token", dto.token());
            model.addAttribute("mensajeError", mensajeError);
            return "auth/recuperar-password";
        }

        try
        {
            this.autenticacionService.restablecerPassword(dto);
            return "redirect:/recuperar-password?exito=true";
        }
        catch (ReglaNegocioException ex)
        {
            if (ex.getMessage().contains("caducado") || ex.getMessage().contains("inválido"))
            {
                return "redirect:/recuperar-password?tokenInvalido=true";
            }
            model.addAttribute("token", dto.token());
            model.addAttribute("mensajeError", ex.getMessage());
            return "auth/recuperar-password";
        }
    }

    /**
     * Muestra la vista de activación de cuenta para el nuevo profesor (Regla 7.1).
     */
    @GetMapping("/activar-cuenta")
    public String mostrarActivacionCuenta(@RequestParam(name = "token", required = false) String token, Model model)
    {
        if (token == null || token.isBlank() || !this.autenticacionService.validarTokenRecuperacion(token.trim()))
        {
            return "redirect:/login?tokenInvalido=true";
        }
        model.addAttribute("token", token.trim());
        model.addAttribute("activarDTO", new ActivarCuentaProfesorDTO(token.trim(), null, null, null));
        return "auth/activar-cuenta";
    }

    /**
     * Procesa la activación de cuenta del profesor, configurando su nombre de usuario y contraseña definitivos.
     */
    @PostMapping("/activar-cuenta")
    public String procesarActivacionCuenta(@Valid @ModelAttribute("activarDTO") ActivarCuentaProfesorDTO dto, BindingResult bindingResult, Model model)
    {
        if (bindingResult.hasErrors())
        {
            String mensajeError = bindingResult.getAllErrors().getFirst().getDefaultMessage();
            model.addAttribute("token", dto.token());
            model.addAttribute("mensajeError", mensajeError);
            return "auth/activar-cuenta";
        }

        try
        {
            this.autenticacionService.activarCuentaProfesor(dto);
            return "redirect:/login?activado=true";
        }
        catch (ReglaNegocioException ex)
        {
            model.addAttribute("token", dto.token());
            model.addAttribute("mensajeError", ex.getMessage());
            return "auth/activar-cuenta";
        }
    }

}
