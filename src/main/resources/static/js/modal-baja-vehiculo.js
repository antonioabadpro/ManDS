/**
 * modal-baja-vehiculo.js - Flujo de Doble Confirmación para Baja Lógica de Vehículos
 *
 * =====================================================================================
 * 1. REGLA DE NEGOCIO DE FLOTA:
 *    - La baja de un vehículo lo transfiere a estado 'INACTIVO' (soft delete), con
 *      prohibición estricta de reactivación.
 *    - Solo puede darse de baja si está en estado 'DISPONIBLE' y no tiene clases 'PENDIENTE'
 *      programadas (todas deben haber sido previamente completadas como 'RECIBIDA' o 'CANCELADA').
 *
 * 2. PATRÓN DE MODAL EN 2 PASOS (IMPACTO Y CONFIRMACIÓN):
 *    - Paso 1 ('modal-baja-vehiculo'): Muestra la ficha técnica del vehículo, matrícula,
 *      kilometraje y advertencias legales.
 *    - Al pulsar "Continuar", este script intercepta el 'submit' con 'e.preventDefault()',
 *      oculta el Paso 1 y abre el Paso 2 ('modal-confirmar-baja-vehiculo') con la confirmación
 *      definitiva en rojo.
 *    - El botón "Volver" permite regresar al Paso 1.
 * =====================================================================================
 */

document.addEventListener('DOMContentLoaded', () => {
    'use strict';
});

/**
 * Escuchador delegado para el envío del formulario del Paso 1 (Transición a Confirmación).
 */
document.addEventListener('submit', (e) => {
    const form = e.target.closest('#form-baja-vehiculo');
    if (!form) return;

    // Evita el envío prematuro al backend
    e.preventDefault();

    const modal1 = document.getElementById('modal-baja-vehiculo');
    const modal2 = document.getElementById('modal-confirmar-baja-vehiculo');
    if (modal1 && modal2) {
        modal1.classList.add('hidden');
        modal1.classList.remove('flex');
        modal2.classList.remove('hidden');
        modal2.classList.add('flex');
    }
});

/**
 * Escuchador delegado para volver del Modal 2 (Confirmación) al Modal 1 (Revisión de ficha).
 */
document.addEventListener('click', (e) => {
    const btnVolver = e.target.closest('#btn-volver-modal-baja-vehiculo');
    if (!btnVolver) return;

    const modal1 = document.getElementById('modal-baja-vehiculo');
    const modal2 = document.getElementById('modal-confirmar-baja-vehiculo');
    if (modal1 && modal2) {
        modal2.classList.add('hidden');
        modal2.classList.remove('flex');
        modal1.classList.remove('hidden');
        modal1.classList.add('flex');
    }
});
