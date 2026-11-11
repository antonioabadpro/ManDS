package com.autoescuela.erp.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * Pruebas unitarias para {@link UrlAplicacionUtil}.
 * Verifican la normalización de barras finales, construcción de rutas y resolución segura de URLs base.
 */
class UrlAplicacionUtilTest
{
    @Test
    @DisplayName("normalizarBaseUrl elimina espacios en blanco y múltiples barras diagonales finales")
    void normalizarBaseUrl_debeLimpiarBarrasDuplicadas()
    {
        assertEquals("", UrlAplicacionUtil.normalizarBaseUrl(null));
        assertEquals("", UrlAplicacionUtil.normalizarBaseUrl("   "));
        assertEquals("https://mands.onrender.com", UrlAplicacionUtil.normalizarBaseUrl("  https://mands.onrender.com///  "));
        assertEquals("http://localhost:8080", UrlAplicacionUtil.normalizarBaseUrl("http://localhost:8080/"));
    }

    @Test
    @DisplayName("construirUrl combina base y ruta con exactamente una barra separadora sin duplicados")
    void construirUrl_debeCombinarConUnicaBarra()
    {
        assertEquals("https://mands.onrender.com/login", UrlAplicacionUtil.construirUrl("https://mands.onrender.com///", "///login"));
        assertEquals("https://mands.onrender.com/activar-cuenta?token=abc", UrlAplicacionUtil.construirUrl("https://mands.onrender.com", "activar-cuenta?token=abc"));
        assertEquals("http://localhost:8080/admin/incidencias", UrlAplicacionUtil.construirUrl(null, "/admin/incidencias"));
    }

    @Test
    @DisplayName("resolverBaseUrlDesdeRequest extrae esquema, host y puerto efectivos o aplica respaldo cuando request es nulo")
    void resolverBaseUrlDesdeRequest_debeResolverDesdePeticionORespaldo()
    {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("https");
        request.setServerName("mands.onrender.com");
        request.setServerPort(443);
        request.setContextPath("");

        assertEquals("https://mands.onrender.com", UrlAplicacionUtil.resolverBaseUrlDesdeRequest(request, "http://localhost:8080"));
        assertEquals("https://respaldo.mands.es", UrlAplicacionUtil.resolverBaseUrlDesdeRequest(null, "https://respaldo.mands.es///"));
        assertEquals("http://localhost:8080", UrlAplicacionUtil.resolverBaseUrlEfectiva("  ", "   "));
    }
}
