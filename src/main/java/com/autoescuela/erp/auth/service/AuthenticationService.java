package com.autoescuela.erp.auth.service;

import java.util.Optional;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.crypto.password.PasswordEncoder;
import com.autoescuela.erp.auth.dto.RegistroAlumnoDTO;
import com.autoescuela.erp.auth.mapper.AuthenticationMapper;
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
        return personaRepository.findById(usuarioId);
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
     * Registra a un nuevo alumno en el sistema a partir del formulario público (CU-002).
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
        if (personaRepository.existsByNombreUsuario(nombreUsuarioLimpio))
        {
            throw new ReglaNegocioException("El nombre de usuario ya está registrado en el sistema.");
        }

        if (personaRepository.existsByCorreo(correoLimpio))
        {
            throw new ReglaNegocioException("El correo electrónico ya está registrado en el sistema.");
        }

        if (personaRepository.existsByDni(dniLimpio))
        {
            throw new ReglaNegocioException("El DNI/NIE ya está registrado en el sistema.");
        }

        if (personaRepository.existsByTelefono(telefonoLimpio))
        {
            throw new ReglaNegocioException("El teléfono ya está registrado en el sistema.");
        }

        // Mapeo inicial con MapStruct
        Alumno alumno = authenticationMapper.toAlumno(dto);
        alumno.setNombreUsuario(nombreUsuarioLimpio);
        alumno.setCorreo(correoLimpio);
        alumno.setDni(dniLimpio);
        alumno.setNombre(dto.nombre().trim());
        alumno.setApellidos(dto.apellidos().trim());
        alumno.setTelefono(telefonoLimpio);
        alumno.setDireccion(dto.direccion().trim());
        alumno.setEstado(EstadoUsuario.ACTIVO);

        // Encriptación segura de contraseña con BCrypt
        alumno.actualizarPassword(passwordEncoder.encode(dto.password()));

        return alumnoRepository.save(alumno);
    }

    /**
     * Comprueba si un nombre de usuario ya está registrado en el sistema.
     *
     * @param nombreUsuario Nombre de usuario a comprobar.
     * @return true si ya existe, false en caso contrario.
     */
    @Transactional(readOnly = true)
    public boolean existeNombreUsuario(String nombreUsuario)
    {
        if (nombreUsuario == null || nombreUsuario.isBlank())
        {
            return false;
        }
        return personaRepository.existsByNombreUsuario(nombreUsuario.trim());
    }

    /**
     * Comprueba si una dirección de correo electrónico ya está registrada en el sistema.
     *
     * @param correo Correo electrónico a comprobar.
     * @return true si ya existe, false en caso contrario.
     */
    @Transactional(readOnly = true)
    public boolean existeCorreo(String correo)
    {
        if (correo == null || correo.isBlank())
        {
            return false;
        }
        return personaRepository.existsByCorreo(correo.trim().toLowerCase());
    }

    /**
     * Comprueba si un DNI/NIE ya está registrado en el sistema.
     *
     * @param dni DNI/NIE a comprobar.
     * @return true si ya existe, false en caso contrario.
     */
    @Transactional(readOnly = true)
    public boolean existeDni(String dni)
    {
        if (dni == null || dni.isBlank())
        {
            return false;
        }
        return personaRepository.existsByDni(dni.trim().toUpperCase());
    }

    /**
     * Comprueba si un número de teléfono ya está registrado en el sistema.
     *
     * @param telefono Número de teléfono a comprobar.
     * @return true si ya existe, false en caso contrario.
     */
    @Transactional(readOnly = true)
    public boolean existeTelefono(String telefono)
    {
        if (telefono == null || telefono.isBlank())
        {
            return false;
        }
        return personaRepository.existsByTelefono(telefono.trim());
    }
}
