/**
 * alumno-examenes.js
 * Gestión de interacciones para la bandeja de convocatorias y solicitudes DGT del Alumno (CU-038).
 * Arquitectura: JavaScript No Intrusivo con Named Event Handlers y ciclo de vida HTMX.
 */

/**
 * Cierra la ventana modal de solicitud de examen y restablece el scroll del documento.
 */
function cerrarModalSolicitarExamen() {
    const contenedorModal = document.getElementById('contenedor-modal');
    if (contenedorModal) {
        contenedorModal.innerHTML = '';
    }
    document.body.classList.remove('overflow-hidden');
}

/**
 * Maneja los clics delegados para el cierre del modal de solicitud de examen.
 * @param {MouseEvent} evento
 */
function manejarClickCierreModal(evento) {
    const disparadorCierre = evento.target.closest('[data-modal-close="modal-solicitar-examen"]');
    if (disparadorCierre) {
        evento.preventDefault();
        cerrarModalSolicitarExamen();
    }
}

/**
 * Maneja el cierre del modal al pulsar la tecla Escape.
 * @param {KeyboardEvent} evento
 */
function manejarTecladoEscape(evento) {
    if (evento.key === 'Escape') {
        const modal = document.getElementById('modal-solicitar-examen');
        if (modal) {
            cerrarModalSolicitarExamen();
        }
    }
}

/**
 * Bloquea el scroll del body cuando HTMX carga el modal en el DOM.
 * @param {CustomEvent} evento
 */
function manejarAperturaModalHtmx(evento) {
    if (evento.detail && evento.detail.target && evento.detail.target.id === 'contenedor-modal') {
        const modal = document.getElementById('modal-solicitar-examen');
        if (modal) {
            document.body.classList.add('overflow-hidden');
        }
    }
}

/**
 * Registra los escuchadores de eventos para la vista de exámenes del alumno.
 */
function inicializarAlumnoExamenes() {
    document.addEventListener('click', manejarClickCierreModal);
    document.addEventListener('keydown', manejarTecladoEscape);
    document.addEventListener('htmx:afterSwap', manejarAperturaModalHtmx);
}

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', inicializarAlumnoExamenes);
} else {
    inicializarAlumnoExamenes();
}
