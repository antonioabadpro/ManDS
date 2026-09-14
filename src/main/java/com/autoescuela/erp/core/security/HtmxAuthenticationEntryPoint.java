package com.autoescuela.erp.core.security;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Punto de entrada de autenticación personalizado con soporte para HTMX.
 * Si una petición HTMX no está autenticada o la sesión ha expirado, responde con
 * la cabecera 'HX-Redirect' para que el navegador ejecute una redirección completa a /login,
 * evitando incrustar la página de login completa dentro de un fragmento HTML.
 */
@Component
public class HtmxAuthenticationEntryPoint implements AuthenticationEntryPoint
{
    /**
     * Delegado que maneja la redirección a la página de login para peticiones no HTMX.
     * Se utiliza para mantener la compatibilidad con peticiones tradicionales.
     * La clase 'LoginUrlAuthenticationEntryPoint' de Spring Security se encarga de redirigir a la URL de login cuando la petición no es HTMX.
     */
    private final LoginUrlAuthenticationEntryPoint delegate = new LoginUrlAuthenticationEntryPoint("/login");

    /**
     * Se invoca automáticamente si ocurre un fallo de autenticación.
     * Se encarga de construir y enviar la respuesta HTTP al cliente.
     * @param request La solicitud HTTP.
     * @param response La respuesta HTTP.
     * @param authException La excepción de autenticación que provocó el fallo.
     * @throws IOException Si ocurre un error de entrada/salida.
     * @throws ServletException Si ocurre un error en el servlet.
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException
    {
        String hxRequest = request.getHeader("HX-Request");
        if (hxRequest != null && hxRequest.equalsIgnoreCase("true"))
        {
            response.setHeader("HX-Redirect", request.getContextPath() + "/login?sesionExpirada=true");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        delegate.commence(request, response, authException);
    }
}
