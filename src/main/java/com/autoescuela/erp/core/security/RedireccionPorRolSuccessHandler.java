package com.autoescuela.erp.core.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Manejador de éxito en la autenticación que redirige dinámicamente al usuario
 * a su panel de control correspondiente según su rol asignado.
 */
@Component
public class RedireccionPorRolSuccessHandler implements AuthenticationSuccessHandler
{
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException
    {
        String urlDestino = determinarUrlDestino(authentication);

        // Si la respuesta ya ha sido comprometida NO se puede realizar una redirección.
        if (response.isCommitted())
        {
            return;
        }

        this.redirectStrategy.sendRedirect(request, response, urlDestino);
    }

    private String determinarUrlDestino(Authentication authentication)
    {
        // Recorremos los roles del usuario autenticado y determinamos la URL de destino según el rol.
        for (GrantedAuthority authority : authentication.getAuthorities())
        {
            String rol = authority.getAuthority();
            if (rol.equals("ROLE_ADMIN"))
            {
                return "/admin/dashboard";
            }
            if (rol.equals("ROLE_PROFESOR"))
            {
                return "/profesor/dashboard";
            }
            if (rol.equals("ROLE_ALUMNO"))
            {
                return "/alumno/dashboard";
            }
        }
        return "/";
    }
}
