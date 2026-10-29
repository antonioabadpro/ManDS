/**
 * profesor-examenes.js - Acta de Exámenes Oficiales DGT y Flujo de Calificación en 2 Pasos
 *
 * =====================================================================================
 * 1. INMUTABILIDAD Y TRASCENDENCIA DE LA CALIFICACIÓN:
 *    - La calificación de un examen DGT es estrictamente irreversible e inmutable en base de datos.
 *    - Si el resultado es APTO en examen práctico: se cierra la matrícula ('estaActiva = false'),
 *      se desvincula al profesor y se envía email oficial de felicitación.
 *    - Si el resultado es NO APTO: se descuenta una convocatoria DGT ('convocatorias -= 1'). Si las
 *      convocatorias llegan a 0, se bloquea la cuenta del alumno hasta que abone una Renovación.
 *
 * 2. PATRÓN DE MODAL EN 2 PASOS (SELECCIÓN Y CONFIRMACIÓN DIFERENCIADA):
 *    - Paso 1 ('modal-calificar-examen'): El profesor selecciona mediante radio button si el alumno
 *      ha resultado APTO o NO APTO.
 *    - Paso 2 ('modal-confirmar-apto' o 'modal-confirmar-no-apto'): Se oculta el Paso 1 y se abre
 *      un modal específico que detalla de forma pedagógica las consecuencias exactas antes de consolidar.
 *
 * 3. PASO DE DATOS MEDIANTE DATASET (SIN JS INLINE):
 *    Los datos del examen (ID, alumno, DNI, fecha/hora, tipo de prueba) se leen directamente
 *    de los atributos 'data-*' de cada botón de la tabla.
 * =====================================================================================
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
 * Almacenamiento temporal en memoria de los metadatos del examen que se está calificando.
 */
const estadoExamenSeleccionado = {
    id: null,
    alumnoNombre: '',
    alumnoDni: '',
    fechaHora: '',
    tipoExamen: ''
};

/**
 * Escucha los clics en los botones "Calificar" de la tabla de actas de examen.
 */
function registrarEscuchadoresAperturaCalificar() {
    document.addEventListener('click', (evento) => {
        const btnCalificar = evento.target.closest('[data-calificar-examen]');
        if (!btnCalificar) return;

        // Extracción segura de datos desde los atributos 'data-*' del botón HTML
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

    // Desmarcar selecciones previas de radio buttons
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
 * Valida la elección (APTO vs NO APTO) y realiza la transición hacia el Modal 2 de confirmación.
 */
function registrarEscuchadoresTransicionConfirmacion() {
    const btnContinuar = document.getElementById('btn-continuar-calificacion');
    if (!btnContinuar) return;

    btnContinuar.addEventListener('click', () => {
        const modal1 = document.getElementById('modal-calificar-examen');
        const opcionSeleccionada = modal1 ? modal1.querySelector('input[name="modalEsApto"]:checked') : null;
        const errorMsg = document.getElementById('error-seleccion-calificacion');

        // Obligatoriedad de seleccionar una opción
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

        // Ocultar Modal 1
        modal1.classList.add('hidden');
        modal1.classList.remove('flex');

        // Abrir el submodal específico correspondiente
        if (esApto) {
            abrirModalConfirmarApto();
        } else {
            abrirModalConfirmarNoApto();
        }
    });
}

/**
 * Abre el modal de confirmación de APTO y vuelca los datos del examen y sus consecuencias.
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
        const tipoTexto = estadoExamenSeleccionado.tipoExamen === 'TEORICO' ? ' • Prueba Teórica' : ' • Prueba Práctica';
        elDetalles.textContent = `${dniTexto}${estadoExamenSeleccionado.fechaHora}${tipoTexto}`;
    }

    // Diferenciar consecuencias según si era examen teórico o práctico
    const listaTeorico = document.getElementById('consecuencias-apto-teorico');
    const listaPractico = document.getElementById('consecuencias-apto-practico');

    if (listaTeorico && listaPractico) {
        if (estadoExamenSeleccionado.tipoExamen === 'TEORICO') {
            listaTeorico.classList.remove('hidden');
            listaPractico.classList.add('hidden');
        } else {
            listaPractico.classList.remove('hidden');
            listaTeorico.classList.add('hidden');
        }
    }

    modalApto.classList.remove('hidden');
    modalApto.classList.add('flex');
}

/**
 * Abre el modal de confirmación de NO APTO y detalla el impacto en las convocatorias restantes.
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
        const tipoTexto = estadoExamenSeleccionado.tipoExamen === 'TEORICO' ? ' • Prueba Teórica' : ' • Prueba Práctica';
        elDetalles.textContent = `${dniTexto}${estadoExamenSeleccionado.fechaHora}${tipoTexto}`;
    }

    const listaTeorico = document.getElementById('consecuencias-no-apto-teorico');
    const listaPractico = document.getElementById('consecuencias-no-apto-practico');

    if (listaTeorico && listaPractico) {
        if (estadoExamenSeleccionado.tipoExamen === 'TEORICO') {
            listaTeorico.classList.remove('hidden');
            listaPractico.classList.add('hidden');
        } else {
            listaPractico.classList.remove('hidden');
            listaTeorico.classList.add('hidden');
        }
    }

    modalNoApto.classList.remove('hidden');
    modalNoApto.classList.add('flex');
}

/**
 * Registra los botones "Volver" para regresar del submodal de confirmación al modal de selección.
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
 * Cierre general de modales con tecla Escape y botones con 'data-modal-close'.
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
