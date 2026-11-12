package com.autoescuela.erp.core.util;

import java.io.IOException;

import org.springframework.security.web.RedirectStrategy;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.support.SessionFlashMapManager;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Utilidad transversal para gestionar redirecciones con Flash Attributes,
 * garantizando compatibilidad uniforme entre peticiones HTTP estándar y peticiones HTMX ({@code HX-Redirect}).
 */
public final class HtmxRedirectUtil
{
    private HtmxRedirectUtil()
    {
        // Clase de utilidad no instanciable
    }

    /**
     * Resuelve la redirección tras una operación de controlador preservando el atributo flash indicado.
     * En peticiones HTMX persiste el {@link FlashMap} de salida, establece la cabecera {@code HX-Redirect}
     * con el {@code contextPath} y devuelve {@code null}; en peticiones convencionales delega en {@code redirect:}.
     *
     * @param rutaDestino Ruta relativa de destino (ej. {@code /admin/profesores}).
     * @param claveFlash Nombre del atributo flash a propagar.
     * @param valorFlash Valor del atributo flash a propagar.
     * @param esPeticionHtmx {@code true} si la petición proviene de HTMX.
     * @param request Petición HTTP entrante.
     * @param response Respuesta HTTP saliente.
     * @param redirectAttributes Atributos de redirección de Spring MVC.
     * @return Cadena {@code redirect:<rutaDestino>} en peticiones normales o {@code null} en peticiones HTMX.
     */
    public static String redirigirConFlash(String rutaDestino, String claveFlash, Object valorFlash, boolean esPeticionHtmx, HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes)
    {
        redirectAttributes.addFlashAttribute(claveFlash, valorFlash);

        if (esPeticionHtmx)
        {
            FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
            if (flashMap != null)
            {
                flashMap.put(claveFlash, valorFlash);
                FlashMapManager flashMapManager = RequestContextUtils.getFlashMapManager(request);
                if (flashMapManager != null)
                {
                    flashMapManager.saveOutputFlashMap(flashMap, request, response);
                }
            }
            String contextPath = request.getContextPath() != null ? request.getContextPath() : "";
            response.setHeader("HX-Redirect", contextPath + rutaDestino);
            return null;
        }

        return "redirect:" + rutaDestino;
    }

    /**
     * Persiste un atributo flash en sesión mediante {@link SessionFlashMapManager} para redirecciones
     * emitidas desde filtros o estrategias de Spring Security fuera del ciclo normal de un controlador.
     *
     * @param request Petición HTTP entrante.
     * @param response Respuesta HTTP saliente.
     * @param claveFlash Clave del atributo flash.
     * @param valorFlash Valor del atributo flash.
     * @param targetRequestPath Ruta destino opcional para acotar la validez del {@link FlashMap}.
     */
    public static void guardarFlashSesion(HttpServletRequest request, HttpServletResponse response, String claveFlash, Object valorFlash, String targetRequestPath)
    {
        FlashMap flashMap = new FlashMap();
        if (targetRequestPath != null)
        {
            flashMap.setTargetRequestPath(targetRequestPath);
        }
        flashMap.put(claveFlash, valorFlash);
        FlashMapManager flashMapManager = new SessionFlashMapManager();
        flashMapManager.saveOutputFlashMap(flashMap, request, response);
    }

    /**
     * Gestiona la redirección por sesión inválida o expirada hacia {@code /login}, propagando el atributo
     * {@code sesionExpirada} y respondiendo con {@code 401} + {@code HX-Redirect} si la petición es HTMX.
     *
     * @param request Petición HTTP entrante.
     * @param response Respuesta HTTP saliente.
     * @param redirectStrategy Estrategia de redirección para peticiones no HTMX.
     * @throws IOException Si falla la redirección HTTP.
     */
    public static void redirigirSesionExpirada(HttpServletRequest request, HttpServletResponse response, RedirectStrategy redirectStrategy) throws IOException
    {
        guardarFlashSesion(request, response, "sesionExpirada", true, null);

        String hxRequest = request.getHeader("HX-Request");
        if (hxRequest != null && "true".equalsIgnoreCase(hxRequest))
        {
            String contextPath = request.getContextPath() != null ? request.getContextPath() : "";
            response.setHeader("HX-Redirect", contextPath + "/login");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        redirectStrategy.sendRedirect(request, response, "/login");
    }
}
