/**
 * modal-baja-profesor.js
 * Lógica de interacción y transición orientada a eventos para el modal de baja lógica de profesores (Regla 7.3).
 * Implementa un flujo en dos pasos: selección y confirmación previa obligatoria.
 */

document.addEventListener('DOMContentLoaded', () => {
    // Inicialización si fuese necesaria
});

/**
 * Escuchador delegado para el envío del formulario de Paso 1 (Transición a Confirmación).
 */
document.addEventListener('submit', (e) => {
    const form = e.target.closest('#form-baja-profesor');
    if (!form) return;

    e.preventDefault();

    const totalAlumnos = parseInt(form.dataset.totalAlumnos || '0', 10);
    const opcionRadio = form.querySelector('input[name="opcionAlumnos"]:checked');
    const opcionHidden = form.querySelector('input[type="hidden"][name="opcionAlumnos"]');
    const opcion = opcionRadio ? opcionRadio.value : (opcionHidden ? opcionHidden.value : 'SIN_PROFESOR');

    const selectProfesor = document.getElementById('select-nuevo-profesor-baja');
    const resumenDestino = document.getElementById('resumen-confirmacion-destino-alumnos');
    const inputOpcion = document.getElementById('input-confirmacion-opcion-alumnos');
    const inputProfesor = document.getElementById('input-confirmacion-nuevo-profesor');

    if (totalAlumnos === 0) {
        if (resumenDestino) resumenDestino.textContent = 'Sin alumnos tutelados';
        if (inputOpcion) inputOpcion.value = 'SIN_PROFESOR';
        if (inputProfesor) inputProfesor.value = '';
    } else if (opcion === 'SIN_PROFESOR') {
        if (resumenDestino) resumenDestino.textContent = 'Sin profesor asignado (temporalmente)';
        if (inputOpcion) inputOpcion.value = 'SIN_PROFESOR';
        if (inputProfesor) inputProfesor.value = '';
    } else {
        if (!selectProfesor || !selectProfesor.value) {
            alert('Debes seleccionar un profesor activo disponible para reasignar a los alumnos.');
            return;
        }
        if (resumenDestino && selectProfesor.selectedOptions.length > 0) {
            resumenDestino.textContent = selectProfesor.selectedOptions[0].text;
        }
        if (inputProfesor) inputProfesor.value = selectProfesor.value;
        if (inputOpcion) inputOpcion.value = 'REASIGNAR';
    }

    const modal1 = document.getElementById('modal-baja-profesor');
    const modal2 = document.getElementById('modal-confirmar-baja-profesor');
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
    if (e.target && e.target.name === 'opcionAlumnos') {
        const select = document.getElementById('select-nuevo-profesor-baja');
        if (select) {
            select.disabled = (e.target.value !== 'REASIGNAR');
        }
    }
});

/**
 * Escuchador delegado para volver del Modal 2 (Confirmación) al Modal 1 (Selección).
 */
document.addEventListener('click', (e) => {
    const btnVolver = e.target.closest('#btn-volver-modal-baja');
    if (!btnVolver) return;

    const modal1 = document.getElementById('modal-baja-profesor');
    const modal2 = document.getElementById('modal-confirmar-baja-profesor');
    if (modal1 && modal2) {
        modal2.classList.add('hidden');
        modal2.classList.remove('flex');
        modal1.classList.remove('hidden');
        modal1.classList.add('flex');
    }
});
