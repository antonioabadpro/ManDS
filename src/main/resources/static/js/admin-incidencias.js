/**
 * admin-incidencias.js
 * JavaScript no intrusivo y modular para la Gestión de Incidencias en ManDS ERP.
 * Implementa el Patrón de Manejadores de Eventos con Nombre y Punto de Entrada de Inicialización.
 *
 * Responsabilidades:
 * 1. Orquestación y accesibilidad de los modales HTMX de gestión y detalle de incidencia.
 * 2. Cierre accesible con tecla Escape y clic en backdrop / botones de cierre.
 * 3. Prevención de doble envío en la actualización de estados de reparación con feedback de carga.
 * 4. Sincronización de eventos de swap en contenedores dinámicos HTMX.
 */
(function () {
    'use strict';

    // =========================================================================
    // MANEJADORES DE EVENTOS CON NOMBRE
    // =========================================================================

    /**
     * Gestiona el cierre accesible de modales al presionar la tecla Escape.
     */
    function manejarKeydownEscapeIncidencia(evento) {
        if (evento.key === 'Escape') {
            const modalesAbiertos = document.querySelectorAll('[role="dialog"]:not(.hidden)');
            modalesAbiertos.forEach(modal => {
                modal.classList.add('hidden');
                modal.classList.remove('flex');
            });
            document.body.classList.remove('overflow-hidden');
        }
    }

    /**
     * Gestiona el cierre al hacer clic en botones data-modal-close o backdrop.
     */
    function manejarClickCerrarModalIncidencia(evento) {
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
     * Intercepta y sincroniza la carga de fragmentos HTMX en la gestión de incidencias.
     */
    function manejarHtmxAfterSwapIncidencias(evento) {
        const targetId = evento.detail && evento.detail.target ? evento.detail.target.id : '';

        // Si se cargó el modal de gestión o de detalle, asegurar scroll bloqueado en body
        if (targetId === 'contenedor-modal-gestionar-incidencia' || targetId === 'contenedor-modal-detalle-incidencia') {
            const modalCargado = evento.detail.target.querySelector('[role="dialog"]');
            if (modalCargado && !modalCargado.classList.contains('hidden')) {
                document.body.classList.add('overflow-hidden');
            }
        }
    }

    /**
     * Controla el envío del formulario de actualización de incidencia para prevenir envíos duplicados.
     */
    function manejarSubmitIncidencia(evento) {
        const form = evento.target.closest('#modal-gestionar-incidencia form');
        if (!form) return;

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
        if (!tablaIncidencias) return; // Solo ejecutar en la vista de incidencias

        document.addEventListener('keydown', manejarKeydownEscapeIncidencia);
        document.addEventListener('click', manejarClickCerrarModalIncidencia);
        document.body.addEventListener('htmx:afterSwap', manejarHtmxAfterSwapIncidencias);
        document.addEventListener('submit', manejarSubmitIncidencia);
    }

    document.addEventListener('DOMContentLoaded', inicializarGestionIncidencias);
})();
