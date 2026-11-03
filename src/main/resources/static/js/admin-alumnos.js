/**
 * admin-alumnos.js - Gestión de Alumnos y Expedientes Académicos para el Administrador
 *
 * =====================================================================================
 * 1. ORQUESTACIÓN DE FRAGMENTOS HTMX:
 *    En lugar de cargar pesados modales en el HTML inicial, los modales de "Detalle de Alumno"
 *    y "Expediente Académico Completo" se descargan por demanda (Lazy Loading) mediante HTMX
 *    cuando el administrador pulsa sobre un alumno en la tabla.
 *
 * 2. SINCRONIZACIÓN DE SWAP ('htmx:afterSwap'):
 *    Cuando HTMX inyecta el modal en '#contenedor-modal-detalle-alumno' o
 *    '#contenedor-modal-expediente-alumno', este script detecta el evento y bloquea de inmediato
 *    el scroll del body ('overflow-hidden') para evitar desplazamientos accidentales de fondo.
 *
 * 3. CONTROL DE CIERRE Y ACCESIBILIDAD:
 *    Permite cerrar los modales inyectados dinámicamente mediante:
 *    - Pulsación de la tecla 'Escape'.
 *    - Clic en botones con 'data-modal-close'.
 *    - Clic en el fondo oscuro translúcido (.modal-backdrop).
 * =====================================================================================
 */
(function () {
    'use strict';

    // =========================================================================
    // MANEJADORES DE EVENTOS CON NOMBRE
    // =========================================================================

    /**
     * Cierra cualquier modal abierto en la vista al presionar la tecla Escape.
     * @param {KeyboardEvent} evento - Evento de teclado.
     */
    function manejarKeydownEscape(evento) {
        if (evento.key === 'Escape') {
            const modalesAbiertos = document.querySelectorAll('[role="dialog"]:not(.hidden)');
            modalesAbiertos.forEach(modal => {
                modal.classList.add('hidden');
                modal.classList.remove('flex');
            });
            // Restablece el scroll de la página
            document.body.classList.remove('overflow-hidden');
        }
    }

    /**
     * Cierra el modal cuando el usuario hace clic en un botón de cierre o en el fondo exterior (backdrop).
     * @param {MouseEvent} evento - Evento de clic.
     */
    function manejarClickCerrarModal(evento) {
        // Caso 1: Clic en botón con atributo 'data-modal-close'
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

        // Caso 2: Clic en el fondo translúcido (Light-Dismiss)
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
     * Detecta la inyección de fragmentos HTMX en el DOM.
     * Si se acaba de insertar el modal de detalle o expediente, activa el bloqueo de scroll.
     * @param {CustomEvent} evento - Evento 'htmx:afterSwap'.
     */
    function manejarHtmxAfterSwapAlumnos(evento) {
        const targetId = evento.detail && evento.detail.target ? evento.detail.target.id : '';

        // Comprueba si el fragmento recién insertado corresponde a uno de los contenedores de alumno
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
        // Comprobación de seguridad: solo ejecutar si estamos en la vista de alumnos (tabla presente)
        const tablaAlumnos = document.getElementById('tabla-alumnos');
        if (!tablaAlumnos) return;

        // Registro de escuchadores de eventos
        document.addEventListener('keydown', manejarKeydownEscape);
        document.addEventListener('click', manejarClickCerrarModal);
        document.body.addEventListener('htmx:afterSwap', manejarHtmxAfterSwapAlumnos);
    }

    document.addEventListener('DOMContentLoaded', inicializarGestionAlumnos);
})();
