package com.autoescuela.erp.core.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.support.SessionFlashMapManager;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Manejador personalizado de cierre de sesión exitoso.
 * Transmite el estado de logout mediante Flash Attributes hacia /login,
 * manteniendo una URL limpia sin parámetros (?logout=true).
 */
@Component
public class FlashLogoutSuccessHandler implements LogoutSuccessHandler
{
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException
    {
        FlashMap flashMap = new FlashMap();
        flashMap.put("logoutExitoso", true);
        FlashMapManager flashMapManager = new SessionFlashMapManager();
        flashMapManager.saveOutputFlashMap(flashMap, request, response);

        this.redirectStrategy.sendRedirect(request, response, "/login");
    }
}
