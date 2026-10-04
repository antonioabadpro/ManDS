/**
 * modal-reasignar-alumno.js
 * Lógica de interacción y transición orientada a eventos para el modal de reasignación de profesor a alumno.
 * Implementa un flujo en dos pasos: selección y confirmación previa obligatoria.
 */

document.addEventListener('DOMContentLoaded', () => {
    // Inicialización si fuese necesaria
});

/**
 * Escuchador delegado para el envío del formulario de Paso 1 (Transición a Confirmación).
 */
document.addEventListener('submit', (e) => {
    const form = e.target.closest('#form-reasignar-alumno');
    if (!form) return;

    e.preventDefault();

    const opcion = form.querySelector('input[name="opcion"]:checked')?.value || 'REASIGNAR';
    const selectProfesor = document.getElementById('select-nuevo-profesor');
    const resumenDestino = document.getElementById('resumen-confirmacion-destino');
    const inputOpcion = document.getElementById('input-confirmacion-opcion');
    const inputProfesor = document.getElementById('input-confirmacion-nuevo-profesor');

    if (opcion === 'SIN_PROFESOR') {
        if (resumenDestino) resumenDestino.textContent = 'Sin profesor asignado (temporalmente)';
        if (inputOpcion) inputOpcion.value = 'SIN_PROFESOR';
        if (inputProfesor) inputProfesor.value = '';
    } else {
        if (!selectProfesor || !selectProfesor.value) {
            alert('Debes seleccionar un profesor activo disponible.');
            return;
        }
        if (resumenDestino && selectProfesor.selectedOptions.length > 0) {
            resumenDestino.textContent = selectProfesor.selectedOptions[0].text;
        }
        if (inputProfesor) inputProfesor.value = selectProfesor.value;
        if (inputOpcion) inputOpcion.value = 'REASIGNAR';
    }

    const modal1 = document.getElementById('modal-reasignar-alumno');
    const modal2 = document.getElementById('modal-confirmar-reasignacion-alumno');
    if (modal1 && modal2) {
        modal1.classList.add('hidden');
        modal1.classList.remove('flex');
        modal2.classList.remove('hidden');
        modal2.classList.add('flex');
    }
});

/**
 * Escuchador delegado de cambio de opción docente (radio button) para habilitar/deshabilitar el select.
 */
document.addEventListener('change', (e) => {
    if (e.target && e.target.name === 'opcion') {
        const select = document.getElementById('select-nuevo-profesor');
        if (select) {
            select.disabled = (e.target.value !== 'REASIGNAR');
        }
    }
});

/**
 * Escuchador delegado para volver del Modal 2 (Confirmación) al Modal 1 (Selección).
 */
document.addEventListener('click', (e) => {
    const btnVolver = e.target.closest('#btn-volver-modal-reasignar');
    if (!btnVolver) return;

    const modal1 = document.getElementById('modal-reasignar-alumno');
    const modal2 = document.getElementById('modal-confirmar-reasignacion-alumno');
    if (modal1 && modal2) {
        modal2.classList.add('hidden');
        modal2.classList.remove('flex');
        modal1.classList.remove('hidden');
        modal1.classList.add('flex');
    }
});
