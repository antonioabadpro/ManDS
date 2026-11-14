/**
 * admin-practicas.js - Búsqueda en tiempo real y sincronización HTMX en Agenda Global de Prácticas
 *
 * =====================================================================================
 * 1. SINCRONIZACIÓN DE FILTRO EN TIEMPO REAL CON HTMX:
 *    - El campo '#filtro-busqueda-practicas' utiliza el atributo declarativo
 *      'data-table-search="tabla-practicas"' gestionado por 'admin.js'.
 *    - Cuando el administrador pulsa "Buscar Clases", HTMX reemplaza '#contenedor-tabla-practicas'.
 *      Este script detecta 'htmx:afterSwap' y re-aplica el término escrito en el buscador
 *      sobre las filas recién cargadas sin perder el filtro del usuario.
 *
 * 2. ESTADO VACÍO EN FILTRADO CLIENTE:
 *    - Si hay clases cargadas en la tabla pero ninguna coincide con el texto buscado,
 *      muestra la fila informativa '#fila-sin-coincidencias-practicas'.
 * =====================================================================================
 */
(function () {
    'use strict';

    /**
     * Evalúa si existen filas de clases visibles tras aplicar el filtro de búsqueda en cliente
     * y muestra u oculta el mensaje de "sin coincidencias".
     */
    function actualizarEstadoVacioBusquedaPracticas() {
        const tabla = document.getElementById('tabla-practicas');
        const filaSinCoincidencias = document.getElementById('fila-sin-coincidencias-practicas');
        if (!tabla || !filaSinCoincidencias) return;

        const filasDatos = tabla.querySelectorAll('tbody tr[data-practica-row="true"]');
        if (filasDatos.length === 0) {
            filaSinCoincidencias.classList.add('hidden');
            return;
        }

        const hayAlgunaVisible = Array.from(filasDatos).some(fila => fila.style.display !== 'none');
        if (hayAlgunaVisible) {
            filaSinCoincidencias.classList.add('hidden');
        } else {
            filaSinCoincidencias.classList.remove('hidden');
        }
    }

    /**
     * Manejador del evento 'input' sobre el buscador de prácticas para sincronizar
     * la fila informativa de ausencia de coincidencias.
     */
    function manejarInputBusquedaPracticas() {
        actualizarEstadoVacioBusquedaPracticas();
    }

    /**
     * Re-aplica automáticamente el término de búsqueda cuando HTMX recarga el fragmento
     * de la tabla de prácticas tras pulsar "Buscar Clases".
     * @param {CustomEvent} evento - Evento 'htmx:afterSwap'.
     */
    function manejarHtmxAfterSwapPracticas(evento) {
        const target = evento.detail ? evento.detail.target : null;
        if (!target || target.id !== 'contenedor-tabla-practicas') return;

        const inputBusqueda = document.getElementById('filtro-busqueda-practicas');
        if (inputBusqueda && inputBusqueda.value.trim() !== '') {
            inputBusqueda.dispatchEvent(new Event('input'));
        } else {
            actualizarEstadoVacioBusquedaPracticas();
        }
    }

    /**
     * Punto de entrada de inicialización para la vista de Agenda Global de Prácticas.
     */
    function inicializarGestionPracticasAdmin() {
        const inputBusqueda = document.getElementById('filtro-busqueda-practicas');
        if (!inputBusqueda) return;

        inputBusqueda.addEventListener('input', manejarInputBusquedaPracticas);
        document.body.addEventListener('htmx:afterSwap', manejarHtmxAfterSwapPracticas);
    }

    document.addEventListener('DOMContentLoaded', inicializarGestionPracticasAdmin);
})();
