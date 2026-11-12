package com.autoescuela.erp.core.security;

import java.io.IOException;

import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;
import org.springframework.stereotype.Component;

import com.autoescuela.erp.core.util.HtmxRedirectUtil;

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

        HtmxRedirectUtil.redirigirSesionExpirada(request, response, this.redirectStrategy);
    }
}
