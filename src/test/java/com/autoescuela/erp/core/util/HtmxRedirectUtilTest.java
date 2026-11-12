package com.autoescuela.erp.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import org.springframework.web.servlet.support.SessionFlashMapManager;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Pruebas unitarias para {@link HtmxRedirectUtil}.
 * Verifican la propagación de atributos flash y cabeceras HTMX en peticiones estándar y reactivas.
 */
class HtmxRedirectUtilTest
{
    @Test
    @DisplayName("redirigirConFlash en petición convencional añade flash attribute y devuelve prefijo redirect:")
    void redirigirConFlash_peticionNormal_devuelveRedirect()
    {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String resultado = HtmxRedirectUtil.redirigirConFlash(
                "/admin/profesores",
                "mensajeExito",
                "Operación completada",
                false,
                request,
                response,
                redirectAttributes);

        assertEquals("redirect:/admin/profesores", resultado);
        assertEquals("Operación completada", redirectAttributes.getFlashAttributes().get("mensajeExito"));
        assertNull(response.getHeader("HX-Redirect"));
    }

    @Test
    @DisplayName("redirigirConFlash en petición HTMX guarda FlashMap y emite cabecera HX-Redirect")
    void redirigirConFlash_peticionHtmx_emiteHxRedirectYGuardaFlashMap()
    {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/erp");
        FlashMap outputFlashMap = new FlashMap();
        SessionFlashMapManager flashMapManager = new SessionFlashMapManager();
        request.setAttribute(DispatcherServlet.OUTPUT_FLASH_MAP_ATTRIBUTE, outputFlashMap);
        request.setAttribute(DispatcherServlet.FLASH_MAP_MANAGER_ATTRIBUTE, flashMapManager);

        MockHttpServletResponse response = new MockHttpServletResponse();
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String resultado = HtmxRedirectUtil.redirigirConFlash(
                "/admin/flota",
                "mensajeExito",
                "Vehículo guardado",
                true,
                request,
                response,
                redirectAttributes);

        assertNull(resultado);
        assertEquals("/erp/admin/flota", response.getHeader("HX-Redirect"));
        assertEquals("Vehículo guardado", outputFlashMap.get("mensajeExito"));
    }

    @Test
    @DisplayName("redirigirSesionExpirada en petición HTMX responde 401 con HX-Redirect y flash sesionExpirada")
    void redirigirSesionExpirada_peticionHtmx_responde401() throws IOException
    {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/mands");
        request.addHeader("HX-Request", "true");
        MockHttpServletResponse response = new MockHttpServletResponse();
        RedirectStrategy redirectStrategy = mock(RedirectStrategy.class);

        HtmxRedirectUtil.redirigirSesionExpirada(request, response, redirectStrategy);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals("/mands/login", response.getHeader("HX-Redirect"));
        @SuppressWarnings("unchecked")
        List<FlashMap> flashMaps = (List<FlashMap>) request.getSession().getAttribute(SessionFlashMapManager.class.getName() + ".FLASH_MAPS");
        assertTrue(flashMaps != null && !flashMaps.isEmpty());
        assertEquals(true, flashMaps.getFirst().get("sesionExpirada"));
    }

    @Test
    @DisplayName("redirigirSesionExpirada en petición normal delega en RedirectStrategy hacia /login")
    void redirigirSesionExpirada_peticionNormal_delegaEnRedirectStrategy() throws IOException
    {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        RedirectStrategy redirectStrategy = mock(RedirectStrategy.class);

        HtmxRedirectUtil.redirigirSesionExpirada(request, response, redirectStrategy);

        verify(redirectStrategy).sendRedirect(request, response, "/login");
        assertNull(response.getHeader("HX-Redirect"));
    }
}
