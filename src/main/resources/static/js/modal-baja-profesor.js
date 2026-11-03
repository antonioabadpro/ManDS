/**
 * modal-baja-profesor.js - Flujo de Doble Confirmación para Baja Lógica de Profesores
 *
 * =====================================================================================
 * 1. REGLAS DE NEGOCIO AL DAR DE BAJA A UN PROFESOR:
 *    - El profesor pasa a 'INACTIVO' (soft delete).
 *    - Su vehículo asignado queda liberado automáticamente a estado 'DISPONIBLE'.
 *    - Todas sus clases futuras se cancelan y se notifica a los alumnos por email.
 *    - Gestión de alumnos asignados: El administrador puede optar por:
 *        a) Reasignarlos a otro profesor activo disponible habilitado en su permiso.
 *        b) Dejarlos temporalmente 'SIN_PROFESOR' para asignarlos más adelante.
 *
 * 2. PATRÓN DE MODAL EN 2 PASOS (SELECCIÓN Y CONFIRMACIÓN):
 *    - Paso 1 ('modal-baja-profesor'): Selección del destino de los alumnos (radio buttons y select).
 *    - Paso 2 ('modal-confirmar-baja-profesor'): Vuelca los datos seleccionados en campos ocultos
 *      y muestra un resumen limpio del impacto antes de ejecutar la baja definitiva en Spring Boot.
 * =====================================================================================
 */

document.addEventListener('DOMContentLoaded', () => {
    'use strict';
});

/**
 * Escuchador delegado para el envío del formulario del Paso 1 (Transición a Confirmación).
 * Valida la opción elegida, sincroniza el texto de resumen y traslada los datos al Modal 2.
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
        // Opción REASIGNAR: exige seleccionar un profesor activo en el desplegable
        if (!selectProfesor || !selectProfesor.value) {
            const alerta = document.getElementById('alerta-error-baja-profesor');
            const texto = document.getElementById('texto-error-baja-profesor');
            if (alerta && texto) {
                texto.textContent = 'Debes seleccionar un profesor activo disponible para reasignar a los alumnos.';
                alerta.classList.remove('hidden');
            }
            if (selectProfesor) {
                selectProfesor.focus();
                selectProfesor.classList.add('border-rose-500', 'focus:ring-rose-500');
            }
            return;
        }
        const alerta = document.getElementById('alerta-error-baja-profesor');
        if (alerta) {
            alerta.classList.add('hidden');
        }
        if (selectProfesor) {
            selectProfesor.classList.remove('border-rose-500', 'focus:ring-rose-500');
        }
        if (resumenDestino && selectProfesor.selectedOptions.length > 0) {
            resumenDestino.textContent = selectProfesor.selectedOptions[0].text;
        }
        if (inputProfesor) inputProfesor.value = selectProfesor.value;
        if (inputOpcion) inputOpcion.value = 'REASIGNAR';
    }

    // Transición visual: oculta Modal 1 y muestra Modal 2
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
 * Escuchador reactivo al cambiar de radio button:
 * Habilita el menú desplegable de profesores solo si se selecciona la opción 'REASIGNAR'.
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
