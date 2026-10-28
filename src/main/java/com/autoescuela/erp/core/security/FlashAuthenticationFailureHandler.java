package com.autoescuela.erp.core.security;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.support.SessionFlashMapManager;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Manejador personalizado de fallos de autenticación.
 * Transmite el estado de error mediante Flash Attributes hacia /login,
 * evitando exponer parámetros de consulta en la URL (?error=true).
 */
@Component
public class FlashAuthenticationFailureHandler implements AuthenticationFailureHandler
{
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException
    {
        FlashMap flashMap = new FlashMap();
        flashMap.put("errorLogin", true);
        FlashMapManager flashMapManager = new SessionFlashMapManager();
        flashMapManager.saveOutputFlashMap(flashMap, request, response);

        this.redirectStrategy.sendRedirect(request, response, "/login");
    }
}
