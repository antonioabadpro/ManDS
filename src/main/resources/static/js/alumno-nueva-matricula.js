/**
 * alumno-nueva-matricula.js - Desglose Dinámico de Tarifas para Nuevas Matrículas
 *
 * =====================================================================================
 * 1. TARIFARIO OFICIAL DE MATRICULACIÓN:
 *    - La autoescuela tiene dos tarifas principales según la tipología del vehículo:
 *        * Permisos Estándar (B, A2, etc.): Matrícula total de 250,00 €
 *          (Cuota de autoescuela: 155,95 € + Tasas DGT oficiales: 94,05 €).
 *        * Permisos Pesados (C, D - Camiones y Autobuses): Matrícula total de 450,00 €
 *          (Cuota de autoescuela: 355,95 € + Tasas DGT oficiales: 94,05 €).
 *
 * 2. ACTUALIZACIÓN REACTIVA EN CLIENTE:
 *    A medida que el alumno cambia la opción seleccionada en el menú desplegable,
 *    la función 'actualizarPrecios' detecta si el permiso comienza por 'PERMISO_C' o
 *    'PERMISO_D' y actualiza instantáneamente los importes en el desglose visual.
 * =====================================================================================
 */

document.addEventListener('DOMContentLoaded', () => {
    'use strict';

    const selectorCarnet = document.getElementById('selector-tipo-carnet');
    const cuotaAutoescuelaTexto = document.getElementById('cuota-autoescuela-texto');
    const precioTotalTexto = document.getElementById('precio-total-texto');

    // Guard clause: si los elementos no están presentes en la vista actual, finalizar
    if (!selectorCarnet || !cuotaAutoescuelaTexto || !precioTotalTexto) {
        return;
    }

    /**
     * Evalúa el valor del desplegable y actualiza los textos de precios en la interfaz.
     */
    const actualizarPrecios = () => {
        const valorSeleccionado = selectorCarnet.value;
        // Comprobación de vehículos pesados (C o D)
        const esPesado = valorSeleccionado.startsWith('PERMISO_C') || valorSeleccionado.startsWith('PERMISO_D');

        if (esPesado) {
            cuotaAutoescuelaTexto.textContent = '355,95 €';
            precioTotalTexto.textContent = '450,00 €';
        } else {
            cuotaAutoescuelaTexto.textContent = '155,95 €';
            precioTotalTexto.textContent = '250,00 €';
        }
    };

    // Escuchar el evento 'change' del selector de permisos
    selectorCarnet.addEventListener('change', actualizarPrecios);
});
