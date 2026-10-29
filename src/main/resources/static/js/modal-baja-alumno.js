/**
 * modal-baja-alumno.js - Flujo de Doble Confirmación para Baja Lógica de Alumnos
 *
 * =====================================================================================
 * 1. TRAZABILIDAD Y SOFT DELETE:
 *    - En un ERP educativo está terminantemente prohibido el borrado físico ('DELETE')
 *      para preservar el histórico inmutable de clases, pagos y exámenes.
 *    - La baja lógica pasa al alumno a estado 'INACTIVO', desvincula su profesor y cancela
 *      todas sus reservas de clases futuras pendientes.
 *
 * 2. PATRÓN DE MODAL EN 2 PASOS (IMPACTO Y CONFIRMACIÓN):
 *    - Paso 1 ('modal-baja-alumno'): Muestra el expediente del alumno y el número de clases
 *      futuras que se van a cancelar.
 *    - Al pulsar "Continuar", este script intercepta el 'submit' con 'e.preventDefault()',
 *      oculta el Paso 1 y abre el Paso 2 ('modal-confirmar-baja-alumno') con un diseño limpio
 *      y advertencias en rojo.
 *    - El botón "Volver" permite regresar al Paso 1 sin perder el contexto.
 * =====================================================================================
 */

document.addEventListener('DOMContentLoaded', () => {
    'use strict';
});

/**
 * Escuchador delegado para el envío del formulario del Paso 1 (Transición a Confirmación).
 * Oculta el Paso 1 y despliega el Paso 2 de confirmación sin recargar la página.
 */
document.addEventListener('submit', (e) => {
    const form = e.target.closest('#form-baja-alumno');
    if (!form) return;

    // Detiene el envío real al servidor para mostrar primero la doble confirmación
    e.preventDefault();

    const modal1 = document.getElementById('modal-baja-alumno');
    const modal2 = document.getElementById('modal-confirmar-baja-alumno');
    if (modal1 && modal2) {
        modal1.classList.add('hidden');
        modal1.classList.remove('flex');
        modal2.classList.remove('hidden');
        modal2.classList.add('flex');
    }
});

/**
 * Escuchador delegado para volver del Modal 2 (Confirmación) al Modal 1 (Revisión de impacto).
 */
document.addEventListener('click', (e) => {
    const btnVolver = e.target.closest('#btn-volver-modal-baja-alumno');
    if (!btnVolver) return;

    const modal1 = document.getElementById('modal-baja-alumno');
    const modal2 = document.getElementById('modal-confirmar-baja-alumno');
    if (modal1 && modal2) {
        modal2.classList.add('hidden');
        modal2.classList.remove('flex');
        modal1.classList.remove('hidden');
        modal1.classList.add('flex');
    }
});
