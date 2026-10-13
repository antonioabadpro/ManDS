/**
 * Configuración global de HTMX para Spring Security y CSRF.
 * Inyecta automáticamente los tokens CSRF presentes en los meta tags en cada petición AJAX/HTMX.
 */
document.addEventListener('DOMContentLoaded', () => {
    // Inyección de estilos globales para indicadores de carga HTMX
    if (!document.getElementById('htmx-indicator-styles')) {
        const style = document.createElement('style');
        style.id = 'htmx-indicator-styles';
        style.textContent = `
            .htmx-indicator { display: none !important; }
            .htmx-request .htmx-indicator,
            .htmx-request.htmx-indicator { display: inline-flex !important; }
            .htmx-request .htmx-indicator-none,
            .htmx-request.htmx-indicator-none { display: none !important; }
        `;
        document.head.appendChild(style);
    }

    document.body.addEventListener('htmx:configRequest', (event) => {
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');

        if (csrfHeader && csrfToken) {
            event.detail.headers[csrfHeader] = csrfToken;
        }
    });

    // Limpieza determinista de clases htmx-request para evitar bloqueos por colisión de requestCount
    document.body.addEventListener('htmx:afterRequest', (event) => {
        const elt = event.detail.elt || event.target;
        if (elt) {
            elt.classList.remove('htmx-request');
            if (elt['htmx-internal-data']) {
                elt['htmx-internal-data'].requestCount = 0;
            }
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

    // Manejo de errores HTTP (404, 500, 403) y redirecciones enviadas por el servidor mediante HX-Redirect
    document.body.addEventListener('htmx:responseError', (event) => {
        const xhr = event.detail.xhr;
        const hxRedirect = xhr.getResponseHeader('HX-Redirect');
        if (hxRedirect) {
            window.location.href = hxRedirect;
            return;
        }

        // Si el servidor devuelve una vista completa de error (404, 403, 500), reemplazar el documento
        if (xhr.status === 404 || xhr.status === 403 || xhr.status === 500) {
            if (xhr.responseText && xhr.responseText.includes('<!DOCTYPE html>')) {
                document.open();
                document.write(xhr.responseText);
                document.close();
            }
        }
    });
});

