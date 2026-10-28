package com.autoescuela.erp.core.security;

import java.io.IOException;

import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.support.SessionFlashMapManager;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Estrategia de control de concurrencia de sesiones concurrentes.
 * Notifica la caducidad por inicio en otro equipo mediante Flash Attributes hacia /login,
 * evitando exponer ?expirada=true en la URL del navegador.
 */
@Component
public class FlashSessionInformationExpiredStrategy implements SessionInformationExpiredStrategy
{
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onExpiredSessionDetected(SessionInformationExpiredEvent event) throws IOException, ServletException
    {
        HttpServletRequest request = event.getRequest();
        HttpServletResponse response = event.getResponse();

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
