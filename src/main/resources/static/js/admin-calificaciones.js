/**
 * admin-calificaciones.js - Acta de Exámenes Teóricos Oficiales DGT y Flujo de Calificación en 2 Pasos (Administrador)
 *
 * =====================================================================================
 * 1. INMUTABILIDAD Y TRASCENDENCIA DE LA CALIFICACIÓN TEÓRICA:
 *    - La calificación de un examen teórico DGT es estrictamente irreversible e inmutable en base de datos.
 *    - Si el resultado es APTO en examen teórico: el alumno queda habilitado para iniciar prácticas
 *      de circulación y reservar en el calendario. Se envía correo electrónico oficial de felicitación.
 *    - Si el resultado es NO APTO: se computa y descuenta una convocatoria DGT ('convocatoriasGastadas++').
 *      Si se agotan las 2 convocatorias oficiales, se bloquea la cuenta del alumno hasta abonar la Renovación.
 *
 * 2. PATRÓN DE MODAL EN 2 PASOS (SELECCIÓN Y CONFIRMACIÓN DIFERENCIADA):
 *    - Paso 1 ('modal-calificar-examen'): El Administrador selecciona mediante radio button si el alumno
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
    inicializarGestionCalificacionesAdmin();
});

function inicializarGestionCalificacionesAdmin() {
    registrarEscuchadoresAperturaCalificarAdmin();
    registrarEscuchadoresTransicionConfirmacionAdmin();
    registrarEscuchadoresVolverModalesAdmin();
    registrarEscuchadoresCierreModalesAdmin();
    registrarEnvioFormulariosCalificarAdmin();
}

/**
 * Almacenamiento temporal en memoria de los metadatos del examen teórico que se está calificando.
 */
const estadoExamenTeoricoSeleccionado = {
    id: null,
    alumnoNombre: '',
    alumnoDni: '',
    fechaHora: '',
    tipoExamen: 'TEORICO'
};

/**
 * Escucha los clics en los botones "Calificar" de la tabla de actas de exámenes teóricos.
 */
function registrarEscuchadoresAperturaCalificarAdmin() {
    document.addEventListener('click', (evento) => {
        const btnCalificar = evento.target.closest('[data-calificar-examen]');
        if (!btnCalificar) return;

        // Extracción segura de datos desde los atributos 'data-*' del botón HTML
        estadoExamenTeoricoSeleccionado.id = btnCalificar.dataset.calificarExamen;
        estadoExamenTeoricoSeleccionado.alumnoNombre = btnCalificar.dataset.alumnoNombre || 'Alumno';
        estadoExamenTeoricoSeleccionado.alumnoDni = btnCalificar.dataset.alumnoDni || '';
        estadoExamenTeoricoSeleccionado.fechaHora = btnCalificar.dataset.fechaHora || '';
        estadoExamenTeoricoSeleccionado.tipoExamen = btnCalificar.dataset.tipoExamen || 'TEORICO';

        abrirModalSeleccionCalificarAdmin();
    });
}

/**
 * Abre el Modal 1 y vuelca los datos informativos del alumno y examen teórico.
 */
function abrirModalSeleccionCalificarAdmin() {
    const modal = document.getElementById('modal-calificar-examen');
    if (!modal) return;

    const elAlumno = document.getElementById('calificar-examen-alumno');
    const elDetalles = document.getElementById('calificar-examen-detalles');
    const errorMsg = document.getElementById('error-seleccion-calificacion');

    if (elAlumno) {
        elAlumno.textContent = estadoExamenTeoricoSeleccionado.alumnoNombre;
    }

    if (elDetalles) {
        const dniTexto = estadoExamenTeoricoSeleccionado.alumnoDni ? `DNI: ${estadoExamenTeoricoSeleccionado.alumnoDni} • ` : '';
        elDetalles.textContent = `${dniTexto}${estadoExamenTeoricoSeleccionado.fechaHora} (Prueba Teórica Oficial)`;
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
function registrarEscuchadoresTransicionConfirmacionAdmin() {
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

        // Abrir el submodal de confirmación correspondiente
        if (esApto) {
            abrirModalConfirmarAptoAdmin();
        } else {
            abrirModalConfirmarNoAptoAdmin();
        }
    });
}

/**
 * Abre el modal de confirmación de APTO y vuelca los datos del examen y sus consecuencias normativas.
 */
function abrirModalConfirmarAptoAdmin() {
    const modalApto = document.getElementById('modal-confirmar-apto');
    if (!modalApto) return;

    const inputId = document.getElementById('confirmar-apto-examen-id');
    const elAlumno = document.getElementById('confirmar-apto-alumno');
    const elDetalles = document.getElementById('confirmar-apto-detalles');

    if (inputId) inputId.value = estadoExamenTeoricoSeleccionado.id;
    if (elAlumno) elAlumno.textContent = estadoExamenTeoricoSeleccionado.alumnoNombre;
    if (elDetalles) {
        const dniTexto = estadoExamenTeoricoSeleccionado.alumnoDni ? `DNI: ${estadoExamenTeoricoSeleccionado.alumnoDni} • ` : '';
        elDetalles.textContent = `${dniTexto}${estadoExamenTeoricoSeleccionado.fechaHora} • Prueba Teórica Oficial`;
    }

    modalApto.classList.remove('hidden');
    modalApto.classList.add('flex');
}

/**
 * Abre el modal de confirmación de NO APTO y detalla el impacto en las convocatorias restantes.
 */
function abrirModalConfirmarNoAptoAdmin() {
    const modalNoApto = document.getElementById('modal-confirmar-no-apto');
    if (!modalNoApto) return;

    const inputId = document.getElementById('confirmar-no-apto-examen-id');
    const elAlumno = document.getElementById('confirmar-no-apto-alumno');
    const elDetalles = document.getElementById('confirmar-no-apto-detalles');

    if (inputId) inputId.value = estadoExamenTeoricoSeleccionado.id;
    if (elAlumno) elAlumno.textContent = estadoExamenTeoricoSeleccionado.alumnoNombre;
    if (elDetalles) {
        const dniTexto = estadoExamenTeoricoSeleccionado.alumnoDni ? `DNI: ${estadoExamenTeoricoSeleccionado.alumnoDni} • ` : '';
        elDetalles.textContent = `${dniTexto}${estadoExamenTeoricoSeleccionado.fechaHora} • Prueba Teórica Oficial`;
    }

    modalNoApto.classList.remove('hidden');
    modalNoApto.classList.add('flex');
}

/**
 * Registra los botones "Volver" para regresar del submodal de confirmación al modal de selección.
 */
function registrarEscuchadoresVolverModalesAdmin() {
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
function registrarEscuchadoresCierreModalesAdmin() {
    document.addEventListener('keydown', (evento) => {
        if (evento.key === 'Escape') {
            cerrarTodosLosModalesCalificarAdmin();
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

function cerrarTodosLosModalesCalificarAdmin() {
    ['modal-calificar-examen', 'modal-confirmar-apto', 'modal-confirmar-no-apto'].forEach(id => {
        const m = document.getElementById(id);
        if (m) {
            m.classList.add('hidden');
            m.classList.remove('flex');
        }
    });
    document.body.classList.remove('overflow-hidden');
}

/**
 * Gestiona el envío seguro de los formularios de calificación asentando el resultado y mostrando el spinner animado.
 */
function registrarEnvioFormulariosCalificarAdmin() {
    ['modal-confirmar-apto', 'modal-confirmar-no-apto'].forEach(modalId => {
        const modal = document.getElementById(modalId);
        if (!modal) return;
        const form = modal.querySelector('form');
        if (!form) return;

        form.addEventListener('submit', (evento) => {
            const btn = form.querySelector('button[type="submit"]');
            if (btn) {
                if (btn.disabled) {
                    evento.preventDefault();
                    return;
                }
                btn.disabled = true;
                const btnIcon = btn.querySelector('svg:not(.btn-spinner)');
                const btnText = btn.querySelector('.btn-text');
                const spinner = btn.querySelector('.btn-spinner');
                const spinnerText = btn.querySelector('.btn-spinner-text');
                if (btnIcon) btnIcon.classList.add('hidden');
                if (btnText) btnText.classList.add('hidden');
                if (spinner) spinner.classList.remove('hidden');
                if (spinnerText) spinnerText.classList.remove('hidden');
            }
        });
    });
}
