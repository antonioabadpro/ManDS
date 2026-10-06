/**
 * admin-alumnos.js
 * JavaScript no intrusivo y modular para la Gestión de Alumnos en ManDS ERP.
 * Implementa el Patrón de Manejadores de Eventos con Nombre y Punto de Entrada de Inicialización.
 *
 * Responsabilidades:
 * 1. Orquestación y accesibilidad de los modales HTMX de detalle y expediente del alumno.
 * 2. Cierre accesible con tecla Escape y clic en backdrop.
 * 3. Sincronización de eventos de swap en contenedores dinámicos.
 */
(function () {
    'use strict';

    // =========================================================================
    // MANEJADORES DE EVENTOS CON NOMBRE
    // =========================================================================

    /**
     * Gestiona el cierre accesible de modales al presionar la tecla Escape.
     */
    function manejarKeydownEscape(evento) {
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
    function manejarClickCerrarModal(evento) {
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
     * Intercepta y sincroniza la carga de fragmentos HTMX en la gestión de alumnos.
     */
    function manejarHtmxAfterSwapAlumnos(evento) {
        const targetId = evento.detail && evento.detail.target ? evento.detail.target.id : '';

        // Si se cargó el modal de detalle o de expediente, asegurar scroll bloqueado en body
        if (targetId === 'contenedor-modal-detalle-alumno' || targetId === 'contenedor-modal-expediente-alumno') {
            const modalCargado = evento.detail.target.querySelector('[role="dialog"]');
            if (modalCargado && !modalCargado.classList.contains('hidden')) {
                document.body.classList.add('overflow-hidden');
            }
        }
    }

    // =========================================================================
    // PUNTO DE ENTRADA DE INICIALIZACIÓN
    // =========================================================================
    function inicializarGestionAlumnos() {
        const tablaAlumnos = document.getElementById('tabla-alumnos');
        if (!tablaAlumnos) return; // Solo ejecutar en la vista de alumnos

        document.addEventListener('keydown', manejarKeydownEscape);
        document.addEventListener('click', manejarClickCerrarModal);
        document.body.addEventListener('htmx:afterSwap', manejarHtmxAfterSwapAlumnos);
    }

    document.addEventListener('DOMContentLoaded', inicializarGestionAlumnos);
})();
