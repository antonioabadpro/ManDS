/**
 * admin-incidencias.js - Gestión de Averías e Incidencias Mecánicas de Flota
 *
 * =====================================================================================
 * 1. CICLO DE VIDA DE INCIDENCIAS:
 *    - Cuando un profesor reporta una incidencia mecánica, el vehículo puede pasar a
 *      estado 'MANTENIMIENTO' (lo cual cancela automáticamente las clases futuras, bloquea
 *      nuevas reservas y notifica a los alumnos afectados por email).
 *    - Cuando el Administrador gestiona la avería y la marca como 'RESUELTA', el vehículo
 *      regresa automáticamente a su estado operativo ('DISPONIBLE' u 'OCUPADO').
 *
 * 2. MODALES DINÁMICOS HTMX Y PREVENCIÓN DE DOBLE ENVÍO:
 *    - Los modales para gestionar y consultar el detalle de averías se cargan asíncronamente
 *      mediante HTMX en los contenedores '#contenedor-modal-gestionar-incidencia' y
 *      '#contenedor-modal-detalle-incidencia'.
 *    - El bloqueo de envío y activación del spinner recaen declarativamente en 'hx-disabled-elt'.
 * =====================================================================================
 */
(function () {
    'use strict';

    // =========================================================================
    // MANEJADORES DE EVENTOS CON NOMBRE
    // =========================================================================

    /**
     * Intercepta la carga asíncrona de fragmentos HTMX en la gestión de incidencias.
     * Si se acaba de insertar un modal, bloquea el scroll del body.
     * @param {CustomEvent} evento - Evento 'htmx:afterSwap'.
     */
    function manejarHtmxAfterSwapIncidencias(evento) {
        const targetId = evento.detail && evento.detail.target ? evento.detail.target.id : '';

        if (targetId === 'contenedor-modal-gestionar-incidencia' || targetId === 'contenedor-modal-detalle-incidencia') {
            const modalCargado = evento.detail.target.querySelector('[role="dialog"]');
            if (modalCargado && !modalCargado.classList.contains('hidden')) {
                document.body.classList.add('overflow-hidden');
            }
        }
    }

    // =========================================================================
    // PUNTO DE ENTRADA DE INICIALIZACIÓN
    // =========================================================================
    function inicializarGestionIncidencias() {
        const tablaIncidencias = document.getElementById('tabla-incidencias');
        if (!tablaIncidencias) return; // Guard clause: solo ejecutar en la vista de incidencias

        // El cierre genérico (Escape, [data-modal-close] y .modal-backdrop) lo gestiona layout.js
        document.body.addEventListener('htmx:afterSwap', manejarHtmxAfterSwapIncidencias);
    }

    document.addEventListener('DOMContentLoaded', inicializarGestionIncidencias);
})();
