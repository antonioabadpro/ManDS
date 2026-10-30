/**
 * modal-reasignar-alumno.js - Flujo de Doble Confirmación para Reasignación de Alumnos
 *
 * =====================================================================================
 * 1. VINCULACIÓN ACADÉMICA:
 *    - Un alumno solo puede tener profesor asignado si posee una matrícula activa.
 *    - La reasignación permite transferir el expediente de un alumno a otro profesor activo
 *      de la autoescuela que esté habilitado en su tipo de carnet ('listaTiposCarnet').
 *    - Alternativamente, se permite desvincular temporalmente al profesor dejándolo 'SIN_PROFESOR'.
 *
 * 2. PATRÓN DE MODAL EN 2 PASOS (SELECCIÓN Y CONFIRMACIÓN):
 *    - Paso 1 ('modal-reasignar-alumno'): Selección del nuevo profesor mediante radio y select.
 *    - Paso 2 ('modal-confirmar-reasignacion-alumno'): Traslada el nombre y ID del profesor elegido
 *      al formulario final de confirmación, asegurando que el administrador no cometa errores.
 * =====================================================================================
 */

document.addEventListener('DOMContentLoaded', () => {
    'use strict';
});

/**
 * Escuchador delegado para el envío del formulario del Paso 1 (Transición a Confirmación).
 * Valida la selección del nuevo profesor y prepara el formulario del Paso 2.
 */
document.addEventListener('submit', (e) => {
    const form = e.target.closest('#form-reasignar-alumno');
    if (!form) return;

    // Detiene el envío real para mostrar primero el modal de confirmación
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
        // Validación obligatoria de selección de profesor en el desplegable
        if (!selectProfesor || !selectProfesor.value) {
            const alerta = document.getElementById('alerta-error-reasignar-alumno');
            const texto = document.getElementById('texto-error-reasignar-alumno');
            if (alerta && texto) {
                texto.textContent = 'Debes seleccionar un profesor activo disponible para reasignar al alumno.';
                alerta.classList.remove('hidden');
            }
            if (selectProfesor) {
                selectProfesor.focus();
                selectProfesor.classList.add('border-rose-500', 'focus:ring-rose-500');
            }
            return;
        }
        const alerta = document.getElementById('alerta-error-reasignar-alumno');
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

    // Ocultar Modal 1 y desplegar Modal 2
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
 * Escuchador reactivo al cambiar de radio button:
 * Habilita el desplegable solo si la opción marcada es 'REASIGNAR'.
 */
document.addEventListener('change', (e) => {
    if (e.target && e.target.name === 'opcion') {
        const select = document.getElementById('select-nuevo-profesor');
        if (select) {
            select.disabled = (e.target.value !== 'REASIGNAR');
        }
        const alerta = document.getElementById('alerta-error-reasignar-alumno');
        if (alerta) alerta.classList.add('hidden');
        if (select) select.classList.remove('border-rose-500', 'focus:ring-rose-500');
    } else if (e.target && e.target.id === 'select-nuevo-profesor') {
        const alerta = document.getElementById('alerta-error-reasignar-alumno');
        if (alerta) alerta.classList.add('hidden');
        e.target.classList.remove('border-rose-500', 'focus:ring-rose-500');
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
