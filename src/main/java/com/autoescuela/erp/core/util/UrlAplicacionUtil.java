package com.autoescuela.erp.core.util;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Utilidad transversal para la resolución y normalización segura de URLs absolutas de la aplicación.
 * Garantiza esquemas correctos detrás de proxies inversos (HTTPS), elimina barras finales duplicadas
 * y provee respaldo consistente hacia la propiedad {@code app_base_url} en contextos sin petición HTTP.
 */
public final class UrlAplicacionUtil
{
    private static final String BASE_URL_DEFECTO = "http://localhost:8080";

    private UrlAplicacionUtil()
    {
        // Clase de utilidad no instanciable
    }

    /**
     * Limpia espacios en blanco y elimina cualquier barra diagonal final repetida de una URL base.
     *
     * @param url URL base a normalizar.
     * @return URL normalizada sin barras finales o cadena vacía si es nula/blanca.
     */
    public static String normalizarBaseUrl(String url)
    {
        if (url == null || url.isBlank())
        {
            return "";
        }

        String limpia = url.trim();
        while (limpia.endsWith("/"))
        {
            limpia = limpia.substring(0, limpia.length() - 1);
        }
        return limpia;
    }

    /**
     * Resuelve la URL base efectiva de la aplicación priorizando:
     * 1) URL explícita pasada por parámetro (si no está vacía),
     * 2) contexto HTTP actual mediante {@link ServletUriComponentsBuilder},
     * 3) URL configurada de respaldo o {@code http://localhost:8080}.
     *
     * @param baseUrlExplicita URL base explícita (opcional).
     * @param appBaseUrlRespaldo URL de configuración de respaldo (opcional).
     * @return URL base canónica sin barra final.
     */
    public static String resolverBaseUrlEfectiva(String baseUrlExplicita, String appBaseUrlRespaldo)
    {
        String explicitaNormalizada = normalizarBaseUrl(baseUrlExplicita);
        if (!explicitaNormalizada.isBlank())
        {
            return explicitaNormalizada;
        }

        try
        {
            String urlContexto = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
            String contextoNormalizada = normalizarBaseUrl(urlContexto);
            if (!contextoNormalizada.isBlank())
            {
                return contextoNormalizada;
            }
        }
        catch (Exception ignored)
        {
            // Contexto sin petición HTTP activa (ej. pruebas unitarias o tareas asíncronas)
        }

        String respaldoNormalizado = normalizarBaseUrl(appBaseUrlRespaldo);
        return !respaldoNormalizado.isBlank() ? respaldoNormalizado : BASE_URL_DEFECTO;
    }

    /**
     * Resuelve la URL base desde una petición HTTP concreta respetando cabeceras de proxy inverso,
     * con respaldo hacia la URL configurada de la aplicación.
     *
     * @param request Petición HTTP entrante.
     * @param appBaseUrlRespaldo URL de configuración de respaldo (opcional).
     * @return URL base canónica sin barra final.
     */
    public static String resolverBaseUrlDesdeRequest(HttpServletRequest request, String appBaseUrlRespaldo)
    {
        if (request != null)
        {
            try
            {
                String urlContexto = ServletUriComponentsBuilder.fromContextPath(request).build().toUriString();
                String contextoNormalizada = normalizarBaseUrl(urlContexto);
                if (!contextoNormalizada.isBlank())
                {
                    return contextoNormalizada;
                }
            }
            catch (Exception ignored)
            {
                // Respaldo seguro si la petición simulada no contiene esquema o host válido
            }
        }

        return resolverBaseUrlEfectiva(null, appBaseUrlRespaldo);
    }

    /**
     * Combina una URL base y una ruta relativa asegurando exactamente una barra separadora.
     *
     * @param baseUrl URL base (se normalizará automáticamente).
     * @param ruta Ruta destino (ej. "/login" o "activar-cuenta?token=...").
     * @return URL completa sin barras duplicadas.
     */
    public static String construirUrl(String baseUrl, String ruta)
    {
        String baseLimpia = normalizarBaseUrl(baseUrl);
        if (baseLimpia.isBlank())
        {
            baseLimpia = BASE_URL_DEFECTO;
        }

        if (ruta == null || ruta.isBlank())
        {
            return baseLimpia;
        }

        String rutaLimpia = ruta.trim();
        while (rutaLimpia.startsWith("/"))
        {
            rutaLimpia = rutaLimpia.substring(1);
        }

        return baseLimpia + "/" + rutaLimpia;
    }
}
