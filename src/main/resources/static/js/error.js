/**
 * error.js - Gestión no intrusiva de interacciones en vistas de error (403, 404, 500, error-negocio).
 * Cumple con CSP evitando eventos inline (onclick) en plantillas Thymeleaf.
 */
document.addEventListener('DOMContentLoaded', () => {
    inicializarBotonesError();
});

/**
 * Registra los escuchadores de eventos para los botones de acción en las páginas de error.
 */
function inicializarBotonesError() {
    const btnVolver = document.getElementById('btn-error-volver');
    if (btnVolver) {
        btnVolver.addEventListener('click', () => {
            if (window.history.length > 1) {
                window.history.back();
            } else {
                window.location.href = '/';
            }
        });
    }

    const btnReintentar = document.getElementById('btn-error-reintentar');
    if (btnReintentar) {
        btnReintentar.addEventListener('click', () => {
            window.location.reload();
        });
    }
}
