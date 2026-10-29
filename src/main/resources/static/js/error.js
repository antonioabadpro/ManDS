/**
 * error.js - Gestión no intrusiva de interacciones en vistas de error HTTP
 *
 * =====================================================================================
 * 1. PRINCIPIO DE JAVASCRIPT NO INTRUSIVO (Unobtrusive JS):
 *    En lugar de usar 'onclick="history.back()"' directamente en el HTML de las vistas
 *    403, 404, 500 o error-negocio, este archivo escucha los eventos de clic desde un
 *    script externo. Esto cumple con la directiva CSP (Content Security Policy).
 *
 * 2. GESTIÓN INTELIGENTE DEL HISTORIAL DE NAVEGACIÓN:
 *    - Si el usuario llegó a la página de error desde otra página de la aplicación,
 *      'window.history.length > 1' es verdadero y el botón "Volver" lo regresa a donde estaba.
 *    - Si el usuario accedió directamente escribiendo una URL incorrecta en la barra del
 *      navegador, 'window.history.back()' no tendría a dónde regresar. En ese caso, el script
 *      lo redirige de forma segura a la página principal ('/').
 * =====================================================================================
 */

document.addEventListener('DOMContentLoaded', () => {
    'use strict';
    inicializarBotonesError();
});

/**
 * Registra los escuchadores de eventos para los botones interactivos de las páginas de error.
 */
function inicializarBotonesError() {
    // 1. Botón "Volver a la página anterior"
    const btnVolver = document.getElementById('btn-error-volver');
    if (btnVolver) {
        btnVolver.addEventListener('click', () => {
            // Comprobamos si el historial del navegador contiene páginas anteriores
            if (window.history.length > 1) {
                window.history.back(); // Regresa a la página anterior en el historial
            } else {
                window.location.href = '/'; // Redirección de seguridad a la raíz
            }
        });
    }

    // 2. Botón "Reintentar operación"
    const btnReintentar = document.getElementById('btn-error-reintentar');
    if (btnReintentar) {
        btnReintentar.addEventListener('click', () => {
            // Recarga la página actual para reintentar la petición al servidor
            window.location.reload();
        });
    }
}
