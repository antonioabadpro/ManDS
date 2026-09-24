package com.autoescuela.erp.auth.service;

import java.util.Optional;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.autoescuela.erp.auth.dto.ActivarCuentaProfesorDTO;
import com.autoescuela.erp.auth.dto.RegistroAlumnoDTO;
import com.autoescuela.erp.auth.dto.RestablecerPasswordDTO;
import com.autoescuela.erp.auth.mapper.AuthenticationMapper;
import com.autoescuela.erp.auth.model.TokenVerificacion;
import com.autoescuela.erp.core.email.EmailService;
import com.autoescuela.erp.core.enums.EstadoUsuario;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.core.security.UserDetailsImpl;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.model.Persona;
import com.autoescuela.erp.usuarios.repository.AlumnoRepository;
import com.autoescuela.erp.usuarios.repository.PersonaRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Servicio centralizado para la gestión del contexto de seguridad y autenticación.
 * Proporciona métodos de consulta para verificar el estado de la sesión activa,
 * obtener los datos del usuario autenticado, registrar nuevos alumnos y gestionar la invalidación de credenciales.
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService
{
    private final PersonaRepository personaRepository;
    private final AlumnoRepository alumnoRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationMapper authenticationMapper;
    private final TokenVerificacionService tokenVerificacionService;
    private final EmailService emailService;

    @Value("${app_base_url:http://localhost:8080}")
    private String appBaseUrlConfigurada;

    /**
     * Comprueba si una instancia de {@link Authentication} pertenece a un usuario autenticado legítimo.
     * En Spring Security, las peticiones sin autenticar pueden asociarse a un
     * {@link AnonymousAuthenticationToken}. Este método asegura que la autenticación existe,
     * está confirmada y no corresponde al rol anónimo del sistema.
     *
     * @param authentication Objeto Authentication de Spring Security.
     * @return true si el usuario está autenticado, false en caso contrario.
     */
    public boolean estaAutenticado(Authentication authentication)
    {
        if (authentication == null)
        {
            return false;
        }

        boolean usuarioAutenticado = authentication.isAuthenticated();
        boolean noEsAnonimo = !(authentication instanceof AnonymousAuthenticationToken);

        return usuarioAutenticado && noEsAnonimo;
    }

    /**
     * Comprueba si existe un usuario autenticado legítimo en el contexto de seguridad global.
     *
     * @return {@code true} si el usuario actual en el {@link SecurityContextHolder} está autenticado;
     *         {@code false} en caso contrario.
     */
    public boolean estaAutenticado()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return estaAutenticado(authentication);
    }

    /**
     * Recupera el adaptador {@link UserDetailsImpl} del usuario que tiene la sesión activa.
     *
     * @return Optional con UserDetailsImpl o vacío si no está autenticado o no es de este tipo.
     */
    public Optional<UserDetailsImpl> obtenerUserDetails()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!estaAutenticado(authentication))
        {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsImpl userDetails)
        {
            return Optional.of(userDetails);
        }

        return Optional.empty();
    }

    /**
     * Recupera desde la base de datos la entidad {@link Persona} gestionada por JPA
     * correspondiente al usuario que tiene la sesión activa.
     *
     * @return Optional con la Persona o vacío si no está autenticado o no existe en la base de datos.
     */
    @Transactional(readOnly = true)
    public Optional<Persona> obtenerPersonaAutenticada()
    {
        Optional<UserDetailsImpl> userDetailsOpt = obtenerUserDetails();

        if (userDetailsOpt.isEmpty())
        {
            return Optional.empty();
        }

        Long usuarioId = userDetailsOpt.get().getId();
        return this.personaRepository.findById(usuarioId);
    }

    /**
     * Cierra de forma segura la sesión activa del usuario.
     * Utiliza {@link SecurityContextLogoutHandler} para invalidar la sesión HTTP actual,
     * eliminar las cookies de sesión y limpiar el {@link SecurityContextHolder}.
     *
     * @param request Solicitud HTTP que contiene la sesión activa.
     * @param response Respuesta HTTP para propagar la invalidación de cookies.
     * @param authentication Objeto de autenticación del usuario que cierra sesión.
     */
    public void cerrarSesion(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
    {
        if (estaAutenticado(authentication))
        {
            SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
            logoutHandler.logout(request, response, authentication);
        }
    }

    /**
     * Registra a un nuevo alumno en el sistema a partir del formulario público.
     * Valida reglas de negocio de unicidad (nombre de usuario, correo y DNI),
     * comprueba la coincidencia de contraseñas, codifica la credencial mediante BCrypt
     * y persiste la entidad Alumno con estado ACTIVO.
     *
     * @param dto DTO que contiene los datos del formulario de registro.
     * @return Entidad Alumno persistida.
     * @throws ReglaNegocioException Si alguna validación de negocio falla.
     */
    @Transactional
    public Alumno registrarAlumno(RegistroAlumnoDTO dto)
    {
        if (dto == null)
        {
            throw new ReglaNegocioException("Los datos de registro no pueden ser nulos.");
        }

        // Validación de coincidencia de contraseñas
        if (dto.password() == null || !dto.password().equals(dto.confirmPassword()))
        {
            throw new ReglaNegocioException("Las contraseñas introducidas no coinciden.");
        }

        String nombreUsuarioLimpio = dto.nombreUsuario().trim();
        String correoLimpio = dto.correo().trim().toLowerCase();
        String dniLimpio = dto.dni().trim().toUpperCase();
        String telefonoLimpio = dto.telefono().trim();

        // Validaciones de unicidad en el repositorio global de personas
        if (this.personaRepository.existsByNombreUsuario(nombreUsuarioLimpio))
        {
            throw new ReglaNegocioException("El nombre de usuario ya está registrado en el sistema.");
        }

        if (this.personaRepository.existsByCorreo(correoLimpio))
        {
            throw new ReglaNegocioException("El correo electrónico ya está registrado en el sistema.");
        }

        if (this.personaRepository.existsByDni(dniLimpio))
        {
            throw new ReglaNegocioException("El DNI/NIE ya está registrado en el sistema.");
        }

        if (this.personaRepository.existsByTelefono(telefonoLimpio))
        {
            throw new ReglaNegocioException("El teléfono ya está registrado en el sistema.");
        }

        // Mapeo inicial con MapStruct
        Alumno alumno = this.authenticationMapper.toAlumno(dto);
        alumno.setNombreUsuario(nombreUsuarioLimpio);
        alumno.setCorreo(correoLimpio);
        alumno.setDni(dniLimpio);
        alumno.setNombre(dto.nombre().trim());
        alumno.setApellidos(dto.apellidos().trim());
        alumno.setTelefono(telefonoLimpio);
        alumno.setDireccion(dto.direccion().trim());
        alumno.setEstado(EstadoUsuario.ACTIVO);

        // Encriptación segura de contraseña con BCrypt
        alumno.actualizarPassword(this.passwordEncoder.encode(dto.password()));

        return this.alumnoRepository.save(alumno);
    }

    /**
     * Inicia el proceso de recuperación de contraseña.
     * Si el correo electrónico corresponde a una Persona con estado ACTIVO, se genera un
     * token temporal efímero (15 minutos) y se envía el correo transaccional con el enlace de recuperación.
     *
     * PRINCIPIO DE SEGURIDAD (Anti-User Enumeration): Si el correo no existe o el usuario está inactivo,
     * el método finaliza silenciosamente sin arrojar excepciones para impedir la enumeración de cuentas.
     *
     * @param correo Correo electrónico proporcionado en la solicitud.
     * @param baseUrl Base URL dinámica de la petición HTTP (o null para utilizar la configurada).
     */
    @Transactional
    public void solicitarRecuperacionPassword(String correo, String baseUrl)
    {
        if (correo == null || correo.isBlank())
        {
            return;
        }

        String correoLimpio = correo.trim().toLowerCase();
        Optional<Persona> personaOpt = this.personaRepository.findByCorreo(correoLimpio);

        if (personaOpt.isPresent())
        {
            Persona persona = personaOpt.get();
            if (persona.getEstado() == EstadoUsuario.ACTIVO)
            {
                TokenVerificacion token = this.tokenVerificacionService.generarTokenRecuperacion(persona);
                String baseUrlFinal = (baseUrl != null && !baseUrl.isBlank()) ? baseUrl : this.appBaseUrlConfigurada;
                if (baseUrlFinal.endsWith("/"))
                {
                    baseUrlFinal = baseUrlFinal.substring(0, baseUrlFinal.length() - 1);
                }
                String enlace = baseUrlFinal + "/recuperar-password?token=" + token.getToken();
                this.emailService.enviarCorreoRecuperacion(persona.getCorreo(), persona.getNombre(), enlace);
            }
        }
    }

    /**
     * Verifica la validez y vigencia de un token de recuperación.
     *
     * @param token Cadena UUID del token.
     * @return true si el token existe, no ha sido consumido y no ha expirado; false en caso contrario.
     */
    @Transactional(readOnly = true)
    public boolean validarTokenRecuperacion(String token)
    {
        return this.tokenVerificacionService.esTokenValido(token);
    }

    /**
     * Restablece la contraseña de un usuario a partir de un token de verificación válido.
     * Valida la coincidencia de claves, codifica la nueva contraseña mediante BCrypt,
     * actualiza la entidad Persona y marca el token como consumido para evitar reutilizaciones.
     *
     * @param dto DTO con el token, la nueva contraseña y su confirmación.
     * @throws ReglaNegocioException Si el token es inválido o expirado, o las contraseñas no coinciden.
     */
    @Transactional
    public void restablecerPassword(RestablecerPasswordDTO dto)
    {
        if (dto == null)
        {
            throw new ReglaNegocioException("Los datos de restablecimiento no pueden ser nulos.");
        }

        if (dto.password() == null || !dto.password().equals(dto.confirmPassword()))
        {
            throw new ReglaNegocioException("Las contraseñas introducidas no coinciden.");
        }

        if (dto.password().trim().length() < 6)
        {
            throw new ReglaNegocioException("La contraseña debe tener al menos 6 caracteres.");
        }

        // Comprobamos la validez del token de recuperación y obtenemos la persona asociada
        TokenVerificacion tokenVerificacion = this.tokenVerificacionService.obtenerTokenValido(dto.token())
                .orElseThrow(() -> new ReglaNegocioException("El enlace de recuperación es inválido o ha caducado."));

        Persona persona = tokenVerificacion.getPersona();

        // Codificamos la nueva contraseña y actualizamos la entidad Persona
        String passwordCodificada = this.passwordEncoder.encode(dto.password());
        persona.actualizarPassword(passwordCodificada);
        this.personaRepository.save(persona);

        this.tokenVerificacionService.marcarComoUsado(tokenVerificacion);
    }

    /**
     * Activa la cuenta de un nuevo profesor permitiéndole definir su nombre de usuario y contraseña iniciales.
     * Valida el token de activación, la coincidencia de contraseñas y la longitud mínima de la clave.
     * @dto DTO con el token de activación, nombre de usuario, contraseña y confirmación.
     * @throws ReglaNegocioException Si el token es inválido o expirado, si las contraseñas no coinciden, o si el nombre de usuario ya está en uso.
     */
    @Transactional
    public void activarCuentaProfesor(ActivarCuentaProfesorDTO dto)
    {
        if (dto == null)
        {
            throw new ReglaNegocioException("Los datos de activación no pueden ser nulos.");
        }

        if (dto.password() == null || !dto.password().equals(dto.confirmPassword()))
        {
            throw new ReglaNegocioException("Las contraseñas introducidas no coinciden.");
        }

        if (dto.password().trim().length() < 6)
        {
            throw new ReglaNegocioException("La contraseña debe tener al menos 6 caracteres.");
        }

        String usernameLimpio = dto.nombreUsuario().trim();
        TokenVerificacion tokenVerificacion = this.tokenVerificacionService.obtenerTokenValido(dto.token())
                .orElseThrow(() -> new ReglaNegocioException("El enlace de activación es inválido o ha caducado."));

        Persona persona = tokenVerificacion.getPersona();
        if (this.personaRepository.existsByNombreUsuario(usernameLimpio) && !usernameLimpio.equalsIgnoreCase(persona.getNombreUsuario()))
        {
            throw new ReglaNegocioException("El nombre de usuario '" + usernameLimpio + "' ya está en uso. Por favor, elige otro.");
        }

        persona.setNombreUsuario(usernameLimpio);
        String passwordCodificada = this.passwordEncoder.encode(dto.password());
        persona.actualizarPassword(passwordCodificada);
        this.personaRepository.save(persona);

        this.tokenVerificacionService.marcarComoUsado(tokenVerificacion);
    }
}
