/**
 * Configuración global de HTMX para Spring Security y CSRF.
 * Inyecta automáticamente los tokens CSRF presentes en los meta tags en cada petición AJAX/HTMX.
 */
document.addEventListener('DOMContentLoaded', () => {
    document.body.addEventListener('htmx:configRequest', (event) => {
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');

        if (csrfHeader && csrfToken) {
            event.detail.headers[csrfHeader] = csrfToken;
        }
    });

    // Manejo de redirecciones enviadas por el servidor mediante la cabecera HX-Redirect (ej. sesiones expiradas)
    document.body.addEventListener('htmx:responseError', (event) => {
        const hxRedirect = event.detail.xhr.getResponseHeader('HX-Redirect');
        if (hxRedirect) {
            window.location.href = hxRedirect;
        }
    });
});

