package com.autoescuela.erp.core.security;

import java.io.IOException;

import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.session.InvalidSessionStrategy;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.support.SessionFlashMapManager;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Estrategia de detección de sesiones inválidas o caducadas por inactividad (timeout).
 * Transmite el estado de caducidad mediante Flash Attributes hacia /login,
 * manteniendo una URL limpia y ofreciendo compatibilidad completa con HTMX.
 */
@Component
public class FlashInvalidSessionStrategy implements InvalidSessionStrategy
{
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onInvalidSessionDetected(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
        String uri = request.getRequestURI();
        if (uri != null && (uri.startsWith("/css/") || uri.startsWith("/js/") || uri.startsWith("/imagenes/") || uri.startsWith("/favicon.ico") || uri.startsWith("/webjars/")))
        {
            return;
        }

        FlashMap flashMap = new FlashMap();
        flashMap.put("sesionExpirada", true);
        FlashMapManager flashMapManager = new SessionFlashMapManager();
        flashMapManager.saveOutputFlashMap(flashMap, request, response);

        String hxRequest = request.getHeader("HX-Request");
        if (hxRequest != null && "true".equalsIgnoreCase(hxRequest))
        {
            String contextPath = request.getContextPath() != null ? request.getContextPath() : "";
            response.setHeader("HX-Redirect", contextPath + "/login");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        this.redirectStrategy.sendRedirect(request, response, "/login");
    }
}
