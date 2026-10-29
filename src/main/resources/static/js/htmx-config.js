/**
 * htmx-config.js - Configuración global de HTMX, Seguridad CSRF y Manejo de Errores
 *
 * =====================================================================================
 * 1. INTEGRACIÓN CON SPRING SECURITY (PROTECCIÓN CSRF):
 *    Por defecto, Spring Security bloquea cualquier petición AJAX que modifique datos
 *    (POST, PUT, DELETE) devolviendo HTTP 403 Forbidden si no incluye el token CSRF.
 *    Este script escucha el evento 'htmx:configRequest' y extrae automáticamente los tokens
 *    CSRF presentes en los meta tags del <head> de Thymeleaf para adjuntarlos como cabecera HTTP.
 *
 * 2. INDICADORES VISUALES DE CARGA (SPINNERS):
 *    Inyecta dinámicamente reglas CSS para mostrar u ocultar elementos con la clase
 *    '.htmx-indicator' mientras una petición asíncrona está en curso.
 *
 * 3. LIMPIEZA DE ESTADOS Y CONTROL DE ERRORES:
 *    - Limpia la clase 'htmx-request' al finalizar cada petición para evitar que los spinners
 *      se queden congelados en pantalla.
 *    - Maneja cabeceras de redirección del servidor ('HX-Redirect') y fallos críticos (403, 404, 500),
 *      reemplazando la vista completa si el servidor devolvió una página de error HTML.
 * =====================================================================================
 */

document.addEventListener('DOMContentLoaded', () => {
    'use strict';

    // =========================================================================
    // 1. ESTILOS GLOBALES PARA INDICADORES DE CARGA (SPINNERS HTMX)
    // =========================================================================
    // Si los estilos aún no existen en el <head>, se crea una etiqueta <style> dinámica.
    // Esto asegura que cualquier componente con 'hx-indicator' se oculte por defecto
    // y se vuelva visible únicamente mientras HTMX añade la clase '.htmx-request'.
    if (!document.getElementById('htmx-indicator-styles')) {
        const style = document.createElement('style');
        style.id = 'htmx-indicator-styles';
        style.textContent = `
            /* Oculto en reposo */
            .htmx-indicator { display: none !important; }

            /* Visible en estado activo de petición */
            .htmx-request .htmx-indicator,
            .htmx-request.htmx-indicator { display: inline-flex !important; }

            /* Ocultar elementos alternativos durante la carga (ej. el texto del botón) */
            .htmx-request .htmx-indicator-none,
            .htmx-request.htmx-indicator-none { display: none !important; }
        `;
        document.head.appendChild(style);
    }

    // =========================================================================
    // 2. INYECCIÓN AUTOMÁTICA DE TOKENS CSRF EN PETICIONES HTMX
    // =========================================================================
    // 'htmx:configRequest' se dispara justo antes de que HTMX envíe cualquier solicitud HTTP.
    // 'event.detail.headers' permite añadir cabeceras personalizadas a la petición.
    document.body.addEventListener('htmx:configRequest', (event) => {
        // Obtenemos el nombre de la cabecera (típicamente 'X-CSRF-TOKEN') y el valor del token desde el HTML
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');

        // Si ambos existen, los inyectamos en la cabecera de la petición AJAX saliente
        if (csrfHeader && csrfToken) {
            event.detail.headers[csrfHeader] = csrfToken;
        }
    });

    // =========================================================================
    // 3. LIMPIEZA DE ESTADOS TRAS LA PETICIÓN (PREVENCIÓN DE BLOQUEOS)
    // =========================================================================
    // 'htmx:afterRequest' se dispara cuando la petición HTTP termina (con éxito o con error).
    // Garantizamos que los elementos que mostraban spinner vuelvan a su estado normal.
    document.body.addEventListener('htmx:afterRequest', (event) => {
        // Elemento que originó la petición (o el objetivo)
        const elt = event.detail.elt || event.target;
        if (elt) {
            elt.classList.remove('htmx-request');
            if (elt['htmx-internal-data']) {
                elt['htmx-internal-data'].requestCount = 0; // Reinicia el contador interno de HTMX
            }

            // Si el elemento definía un indicador específico mediante 'hx-indicator="#id"'
            const indicatorSelector = elt.getAttribute('hx-indicator');
            if (indicatorSelector) {
                document.querySelectorAll(indicatorSelector).forEach((ind) => {
                    ind.classList.remove('htmx-request');
                    if (ind['htmx-internal-data']) {
                        ind['htmx-internal-data'].requestCount = 0;
                    }
                });
            }
        }
    });

    // =========================================================================
    // 4. MANEJO DE ERRORES HTTP Y REDIRECCIONES DEL SERVIDOR
    // =========================================================================
    // 'htmx:responseError' se dispara si el servidor responde con códigos de error HTTP (4xx o 5xx)
    document.body.addEventListener('htmx:responseError', (event) => {
        const xhr = event.detail.xhr;

        // 4.1. Redirecciones controladas por Spring Boot:
        // Si el backend envía la cabecera 'HX-Redirect: /login', forzamos la navegación completa del navegador
        const hxRedirect = xhr.getResponseHeader('HX-Redirect');
        if (hxRedirect) {
            window.location.href = hxRedirect;
            return;
        }

        // 4.2. Respuestas de error completas (404 No encontrado, 403 Prohibido, 500 Error Interno):
        // Si una llamada AJAX falló y Spring Boot devolvió una página HTML completa de error,
        // reemplazamos el documento actual para mostrar la pantalla de error al usuario
        if (xhr.status === 404 || xhr.status === 403 || xhr.status === 500) {
            if (xhr.responseText && xhr.responseText.includes('<!DOCTYPE html>')) {
                document.open();
                document.write(xhr.responseText);
                document.close();
            }
        }
    });
});
