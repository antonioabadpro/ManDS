/**
 * admin-incidencias.js
 * JavaScript no intrusivo y modular para la Gestión de Incidencias en ManDS ERP.
 * Implementa el Patrón de Manejadores de Eventos con Nombre y Punto de Entrada de Inicialización.
 *
 * Responsabilidades:
 * 1. Control accesible del modal para actualizar el estado operativo de incidencias mecánicas.
 * 2. Cierre accesible con tecla Escape y clic en backdrop.
 * 3. Prevención de doble envío en la actualización de estados de reparación.
 */
(function () {
    'use strict';

    // =========================================================================
    // MANEJADORES DE EVENTOS CON NOMBRE
    // =========================================================================

    /**
     * Abre el modal de gestión de incidencia correspondiente.
     */
    function manejarAbrirModalIncidencia(evento) {
        const btnAbrir = evento.target.closest('[data-modal-target="modal-gestionar-incidencia"]');
        if (!btnAbrir) return;

        const modal = document.getElementById('modal-gestionar-incidencia');
        if (modal) {
            modal.classList.remove('hidden');
            modal.classList.add('flex');
            document.body.classList.add('overflow-hidden');
        }
    }

    /**
     * Cierra el modal de incidencia al pulsar el botón de cerrar o el backdrop.
     */
    function manejarCerrarModalIncidencia(evento) {
        const btnCerrar = evento.target.closest('[data-modal-close="modal-gestionar-incidencia"]');
        const esBackdrop = evento.target.classList.contains('modal-backdrop');

        if (btnCerrar || esBackdrop) {
            const modal = document.getElementById('modal-gestionar-incidencia');
            if (modal) {
                modal.classList.add('hidden');
                modal.classList.remove('flex');
                document.body.classList.remove('overflow-hidden');
            }
        }
    }

    /**
     * Cierra el modal de gestión de incidencia al presionar la tecla Escape.
     */
    function manejarKeydownEscapeIncidencia(evento) {
        if (evento.key === 'Escape') {
            const modal = document.getElementById('modal-gestionar-incidencia');
            if (modal && !modal.classList.contains('hidden')) {
                modal.classList.add('hidden');
                modal.classList.remove('flex');
                document.body.classList.remove('overflow-hidden');
            }
        }
    }

    /**
     * Controla el envío del formulario de actualización de incidencia.
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
        const modalIncidencia = document.getElementById('modal-gestionar-incidencia');
        if (!modalIncidencia) return; // Solo ejecutar en la vista de incidencias

        document.addEventListener('click', manejarAbrirModalIncidencia);
        document.addEventListener('click', manejarCerrarModalIncidencia);
        document.addEventListener('keydown', manejarKeydownEscapeIncidencia);
        document.addEventListener('submit', manejarSubmitIncidencia);
    }

    document.addEventListener('DOMContentLoaded', inicializarGestionIncidencias);
})();

