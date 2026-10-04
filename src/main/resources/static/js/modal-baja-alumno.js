/**
 * modal-baja-alumno.js
 * Lógica de interacción y transición orientada a eventos para el modal de baja lógica de alumnos.
 * Implementa un flujo en dos pasos: revisión de impacto y confirmación previa obligatoria.
 */

document.addEventListener('DOMContentLoaded', () => {
    // Inicialización si fuese requerida
});

/**
 * Escuchador delegado para el envío del formulario de Paso 1 (Transición a Confirmación).
 */
document.addEventListener('submit', (e) => {
    const form = e.target.closest('#form-baja-alumno');
    if (!form) return;

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
 * Escuchador delegado para volver del Modal 2 (Confirmación) al Modal 1 (Revisión).
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
