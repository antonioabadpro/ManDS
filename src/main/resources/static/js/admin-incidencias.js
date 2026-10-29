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
 *    - 'manejarSubmitIncidencia' desactiva el botón de guardar ('btnSubmit.disabled = true')
 *      al enviar la resolución para evitar dobles registros o transacciones duplicadas.
 * =====================================================================================
 */
(function () {
    'use strict';

    // =========================================================================
    // MANEJADORES DE EVENTOS CON NOMBRE
    // =========================================================================

    /**
     * Gestiona el cierre accesible de modales al presionar la tecla Escape.
     * @param {KeyboardEvent} evento - Evento de teclado.
     */
    function manejarKeydownEscapeIncidencia(evento) {
        if (evento.key === 'Escape') {
            const modalesAbiertos = document.querySelectorAll('[role="dialog"]:not(.hidden)');
            modalesAbiertos.forEach(modal => {
                modal.classList.add('hidden');
                modal.classList.remove('flex');
            });
            // Restablecer el scroll del fondo
            document.body.classList.remove('overflow-hidden');
        }
    }

    /**
     * Cierra el modal cuando el usuario hace clic en botones de cierre o en el fondo oscuro translúcido.
     * @param {MouseEvent} evento - Evento de clic.
     */
    function manejarClickCerrarModalIncidencia(evento) {
        // Clic en botón de cierre explícito
        const btnCerrar = evento.target.closest('[data-modal-close]');
        if (btnCerrar) {
            const modalId = btnCerrar.getAttribute('data-modal-close');
            const modal = document.getElementById(modalId) || btnCerrar.closest('[role="dialog"]');
            if (modal) {
                modal.classList.add('hidden');
                modal.classList.remove('flex');
                document.body.classList.remove('overflow-hidden');
            }
            return;
        }

        // Clic en el fondo oscuro exterior (Light-Dismiss)
        const backdrop = evento.target.closest('.modal-backdrop');
        if (backdrop) {
            const modal = backdrop.closest('[role="dialog"]');
            if (modal) {
                modal.classList.add('hidden');
                modal.classList.remove('flex');
                document.body.classList.remove('overflow-hidden');
            }
        }
    }

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

    /**
     * Controla el envío del formulario de actualización de incidencia para prevenir dobles envíos.
     * @param {SubmitEvent} evento - Evento de envío de formulario.
     */
    function manejarSubmitIncidencia(evento) {
        const form = evento.target.closest('#modal-gestionar-incidencia form');
        if (!form) return;

        // Deshabilita el botón de acción para proteger la transacción en base de datos
        const btnSubmit = form.querySelector('button[type="submit"]');
        if (btnSubmit) {
            btnSubmit.disabled = true;
        }
    }

    // =========================================================================
    // PUNTO DE ENTRADA DE INICIALIZACIÓN
    // =========================================================================
    function inicializarGestionIncidencias() {
        const tablaIncidencias = document.getElementById('tabla-incidencias');
        if (!tablaIncidencias) return; // Guard clause: solo ejecutar en la vista de incidencias

        // Registro de escuchadores
        document.addEventListener('keydown', manejarKeydownEscapeIncidencia);
        document.addEventListener('click', manejarClickCerrarModalIncidencia);
        document.body.addEventListener('htmx:afterSwap', manejarHtmxAfterSwapIncidencias);
        document.addEventListener('submit', manejarSubmitIncidencia);
    }

    document.addEventListener('DOMContentLoaded', inicializarGestionIncidencias);
})();
