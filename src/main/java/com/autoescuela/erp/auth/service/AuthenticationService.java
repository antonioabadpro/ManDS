package com.autoescuela.erp.auth.service;

import java.util.Optional;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autoescuela.erp.core.security.UserDetailsImpl;
import com.autoescuela.erp.usuarios.model.Persona;
import com.autoescuela.erp.usuarios.repository.PersonaRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Servicio centralizado para la gestión del contexto de seguridad y autenticación.
 * Proporciona métodos de consulta para verificar el estado de la sesión activa,
 * obtener los datos del usuario autenticado y gestionar la invalidación de credenciales.
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService
{
    private final PersonaRepository personaRepository;

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
}
