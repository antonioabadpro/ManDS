/**
 * profesor-examenes.js
 * JavaScript no intrusivo para la vista de Convocatorias y Calificaciones DGT del Profesor.
 * Gestiona la apertura reactiva del modal de calificación y el flujo de confirmación en dos pasos
 * diferenciando de forma pedagógica las consecuencias de calificar APTO vs NO APTO.
 */

document.addEventListener('DOMContentLoaded', () => {
    'use strict';
    inicializarGestionExamenesProfesor();
});

function inicializarGestionExamenesProfesor() {
    registrarEscuchadoresAperturaCalificar();
    registrarEscuchadoresTransicionConfirmacion();
    registrarEscuchadoresVolverModales();
    registrarEscuchadoresCierreModales();
}

/**
 * Estado en memoria del examen seleccionado actualmente para calificar.
 */
const estadoExamenSeleccionado = {
    id: null,
    alumnoNombre: '',
    alumnoDni: '',
    fechaHora: '',
    tipoExamen: ''
};

/**
 * Registra los escuchadores en los botones "Calificar" de cada fila de la tabla.
 */
function registrarEscuchadoresAperturaCalificar() {
    document.addEventListener('click', (evento) => {
        const btnCalificar = evento.target.closest('[data-calificar-examen]');
        if (!btnCalificar) return;

        estadoExamenSeleccionado.id = btnCalificar.dataset.calificarExamen;
        estadoExamenSeleccionado.alumnoNombre = btnCalificar.dataset.alumnoNombre || 'Alumno';
        estadoExamenSeleccionado.alumnoDni = btnCalificar.dataset.alumnoDni || '';
        estadoExamenSeleccionado.fechaHora = btnCalificar.dataset.fechaHora || '';
        estadoExamenSeleccionado.tipoExamen = btnCalificar.dataset.tipoExamen || 'PRACTICO';

        abrirModalSeleccionCalificar();
    });
}

/**
 * Abre el Modal 1 y rellena los datos informativos del alumno y examen.
 */
function abrirModalSeleccionCalificar() {
    const modal = document.getElementById('modal-calificar-examen');
    if (!modal) return;

    const elAlumno = document.getElementById('calificar-examen-alumno');
    const elDetalles = document.getElementById('calificar-examen-detalles');
    const errorMsg = document.getElementById('error-seleccion-calificacion');

    if (elAlumno) {
        elAlumno.textContent = estadoExamenSeleccionado.alumnoNombre;
    }

    if (elDetalles) {
        const dniTexto = estadoExamenSeleccionado.alumnoDni ? `DNI: ${estadoExamenSeleccionado.alumnoDni} • ` : '';
        const tipoTexto = estadoExamenSeleccionado.tipoExamen === 'TEORICO' ? 'Prueba Teórica' : 'Prueba Práctica';
        elDetalles.textContent = `${dniTexto}${estadoExamenSeleccionado.fechaHora} (${tipoTexto})`;
    }

    // Desmarcar opciones previas
    const radios = modal.querySelectorAll('input[name="modalEsApto"]');
    radios.forEach(r => { r.checked = false; });

    if (errorMsg) {
        errorMsg.classList.add('hidden');
    }

    modal.classList.remove('hidden');
    modal.classList.add('flex');
    document.body.classList.add('overflow-hidden');
}

/**
 * Gestiona el clic en "Continuar con la Calificación" para validar y abrir el modal 2 correspondiente.
 */
function registrarEscuchadoresTransicionConfirmacion() {
    const btnContinuar = document.getElementById('btn-continuar-calificacion');
    if (!btnContinuar) return;

    btnContinuar.addEventListener('click', () => {
        const modal1 = document.getElementById('modal-calificar-examen');
        const opcionSeleccionada = modal1 ? modal1.querySelector('input[name="modalEsApto"]:checked') : null;
        const errorMsg = document.getElementById('error-seleccion-calificacion');

        if (!opcionSeleccionada) {
            if (errorMsg) {
                errorMsg.textContent = 'Debes seleccionar obligatoriamente si el resultado oficial es APTO o NO APTO.';
                errorMsg.classList.remove('hidden');
            }
            return;
        }

        if (errorMsg) {
            errorMsg.classList.add('hidden');
        }

        const esApto = opcionSeleccionada.value === 'true';

        // Ocultar modal 1
        modal1.classList.add('hidden');
        modal1.classList.remove('flex');

        if (esApto) {
            abrirModalConfirmarApto();
        } else {
            abrirModalConfirmarNoApto();
        }
    });
}

/**
 * Abre el modal de confirmación de APTO y vuelca los datos del formulario.
 */
function abrirModalConfirmarApto() {
    const modalApto = document.getElementById('modal-confirmar-apto');
    if (!modalApto) return;

    const inputId = document.getElementById('confirmar-apto-examen-id');
    const elAlumno = document.getElementById('confirmar-apto-alumno');
    const elDetalles = document.getElementById('confirmar-apto-detalles');

    if (inputId) inputId.value = estadoExamenSeleccionado.id;
    if (elAlumno) elAlumno.textContent = estadoExamenSeleccionado.alumnoNombre;
    if (elDetalles) {
        const dniTexto = estadoExamenSeleccionado.alumnoDni ? `DNI: ${estadoExamenSeleccionado.alumnoDni} • ` : '';
        elDetalles.textContent = `${dniTexto}${estadoExamenSeleccionado.fechaHora}`;
    }

    modalApto.classList.remove('hidden');
    modalApto.classList.add('flex');
}

/**
 * Abre el modal de confirmación de NO APTO y vuelca los datos del formulario.
 */
function abrirModalConfirmarNoApto() {
    const modalNoApto = document.getElementById('modal-confirmar-no-apto');
    if (!modalNoApto) return;

    const inputId = document.getElementById('confirmar-no-apto-examen-id');
    const elAlumno = document.getElementById('confirmar-no-apto-alumno');
    const elDetalles = document.getElementById('confirmar-no-apto-detalles');

    if (inputId) inputId.value = estadoExamenSeleccionado.id;
    if (elAlumno) elAlumno.textContent = estadoExamenSeleccionado.alumnoNombre;
    if (elDetalles) {
        const dniTexto = estadoExamenSeleccionado.alumnoDni ? `DNI: ${estadoExamenSeleccionado.alumnoDni} • ` : '';
        elDetalles.textContent = `${dniTexto}${estadoExamenSeleccionado.fechaHora}`;
    }

    modalNoApto.classList.remove('hidden');
    modalNoApto.classList.add('flex');
}

/**
 * Registra los botones "Volver" para retroceder de la confirmación al modal de selección.
 */
function registrarEscuchadoresVolverModales() {
    document.addEventListener('click', (evento) => {
        const btnVolver = evento.target.closest('[data-modal-volver]');
        if (!btnVolver) return;

        const modalOrigenId = btnVolver.dataset.modalVolver;
        const modalOrigen = document.getElementById(modalOrigenId);
        const modalSeleccion = document.getElementById('modal-calificar-examen');

        if (modalOrigen) {
            modalOrigen.classList.add('hidden');
            modalOrigen.classList.remove('flex');
        }

        if (modalSeleccion) {
            modalSeleccion.classList.remove('hidden');
            modalSeleccion.classList.add('flex');
        }
    });
}

/**
 * Cierre general de modales con tecla Escape y comprobación de backdrop.
 */
function registrarEscuchadoresCierreModales() {
    document.addEventListener('keydown', (evento) => {
        if (evento.key === 'Escape') {
            cerrarTodosLosModalesCalificar();
        }
    });

    document.addEventListener('click', (evento) => {
        const btnCerrar = evento.target.closest('[data-modal-close]');
        if (btnCerrar) {
            const modalId = btnCerrar.dataset.modalClose;
            const modal = document.getElementById(modalId);
            if (modal) {
                modal.classList.add('hidden');
                modal.classList.remove('flex');
                document.body.classList.remove('overflow-hidden');
            }
        }
    });
}

function cerrarTodosLosModalesCalificar() {
    ['modal-calificar-examen', 'modal-confirmar-apto', 'modal-confirmar-no-apto'].forEach(id => {
        const m = document.getElementById(id);
        if (m) {
            m.classList.add('hidden');
            m.classList.remove('flex');
        }
    });
    document.body.classList.remove('overflow-hidden');
}
