/**
 * alumno-examenes.js - Bandeja de Convocatorias y Solicitudes de Examen DGT del Alumno
 *
 * =====================================================================================
 * 1. SOLICITUD DE EXAMEN:
 *    - Los alumnos solicitan examen oficial DGT (Teórico o Práctico) de forma gratuita, ya que
 *      las tasas están incluidas en la matrícula y en las renovaciones.
 *    - Para solicitar examen práctico en la modalidad 'TEORICO_PRACTICA', es obligatorio haber
 *      aprobado previamente el examen teórico y contar con al menos una clase práctica RECIBIDA.
 *
 * 2. MODAL ASÍNCRONO HTMX Y LIMPIEZA DEL CONTENEDOR:
 *    - El formulario de solicitud se carga por demanda dentro de '#contenedor-modal'.
 *    - Al cerrar el modal, 'cerrarModalSolicitarExamen' no solo oculta visualmente el elemento,
 *      sino que vacía el contenedor ('contenedorModal.innerHTML = ""') para que el DOM
 *      permanezca limpio y libre de elementos residuales.
 *
 * 3. COMPATIBILIDAD CON CARGA ASÍNCRONA ('document.readyState'):
 *    - Comprueba si el DOM aún se está analizando ('loading') o si ya está interactivo,
 *      garantizando que los escuchadores se registren sin importar el momento de inyección.
 * =====================================================================================
 */

/**
 * Cierra la ventana modal de solicitud de examen, vacía su contenedor dinámico y restaura el scroll.
 */
function cerrarModalSolicitarExamen() {
    const contenedorModal = document.getElementById('contenedor-modal');
    if (contenedorModal) {
        contenedorModal.innerHTML = ''; // Limpieza determinista del nodo inyectado por HTMX
    }
    document.body.classList.remove('overflow-hidden');
}

/**
 * Maneja los clics delegados para el cierre del modal de solicitud de examen.
 * @param {MouseEvent} evento - Evento de clic.
 */
function manejarClickCierreModal(evento) {
    const disparadorCierre = evento.target.closest('[data-modal-close="modal-solicitar-examen"]');
    if (disparadorCierre) {
        evento.preventDefault();
        cerrarModalSolicitarExamen();
    }
}

/**
 * Cierra el modal de examen si el usuario presiona la tecla Escape.
 * @param {KeyboardEvent} evento - Evento de teclado.
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
 * Bloquea el scroll del body cuando HTMX inyecta el modal en el DOM.
 * @param {CustomEvent} evento - Evento 'htmx:afterSwap'.
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
 * Registra todos los escuchadores de eventos para la vista de exámenes del alumno.
 */
function inicializarAlumnoExamenes() {
    document.addEventListener('click', manejarClickCierreModal);
    document.addEventListener('keydown', manejarTecladoEscape);
    document.body.addEventListener('htmx:afterSwap', manejarAperturaModalHtmx);
}

// Comprobación de estado de carga para ejecución segura tanto en renderizado directo como diferido
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', inicializarAlumnoExamenes);
} else {
    inicializarAlumnoExamenes();
}
