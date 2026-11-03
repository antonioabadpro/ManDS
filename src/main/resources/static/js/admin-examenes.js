/**
 * admin-examenes.js - Gestión de Fechas Oficiales DGT, Cupos y Solicitudes de Alumnos
 *
 * =====================================================================================
 * 1. REGLA DE NEGOCIO DGT:
 *    - El Administrador fija exactamente 2 fechas oficiales de examen por mes (fecha 2 >= fecha 1).
 *    - Fija cupos independientes de plazas por tipo de permiso (B, A2, C, D) y tipo (Teórico/Práctico).
 *    - Como fijar estas fechas es una acción crítica e inmutable, se implementa un patrón
 *      de MODAL EN 2 PASOS (Doble Confirmación): el Paso 1 se oculta completamente y se muestra
 *      un submodal limpio de advertencia antes de persistir en base de datos.
 *
 * 2. RESTRICCIÓN ESTRICTA DE CALENDARIO AL MES SELECCIONADO:
 *    La función 'limitarFechasAlMesSeleccionado' calcula dinámicamente el último día del mes
 *    elegido (manejando años bisiestos con 'new Date(anio, mes, 0).getDate()') y fija los
 *    atributos 'min' y 'max' en los selectores HTML5 'datetime-local' para impedir errores humanos.
 *
 * 3. RESOLUCIÓN DE PETICIONES (COLA FIFO):
 *    Permite aceptar solicitudes o rechazarlas (exigiendo en este último caso una justificación
 *    obligatoria que se remitirá por email al alumno).
 * =====================================================================================
 */

document.addEventListener('DOMContentLoaded', () => {
    'use strict';
    inicializarExamenesAdmin();
    limitarFechasAlMesSeleccionado();
});

// Sincronización con eventos de HTMX:
// Al refrescar el contenedor de jornadas o la tabla de solicitudes, se re-aplica la restricción de fechas
// y se re-aplica el filtro de búsqueda si el administrador ya tenía texto escrito en el buscador
document.addEventListener('htmx:afterSwap', (event) => {
    'use strict';
    if (event.detail && event.detail.target && event.detail.target.id === 'contenedor-jornadas-dgt') {
        limitarFechasAlMesSeleccionado();
    }
    if (event.detail && event.detail.target && event.detail.target.id === 'contenedor-tabla-solicitudes') {
        const inputBusqueda = document.querySelector('input[data-table-search="tabla-solicitudes"]');
        if (inputBusqueda && inputBusqueda.value.trim() !== '') {
            inputBusqueda.dispatchEvent(new Event('input'));
        }
    }
});

function inicializarExamenesAdmin() {
    'use strict';

    // Manejador centralizado de clics (Event Delegation)
    document.addEventListener('click', (event) => {
        // 1. Apertura genérica de modales
        const targetBtn = event.target.closest('[data-modal-target]');
        if (targetBtn) {
            const modalId = targetBtn.getAttribute('data-modal-target');
            const modalEl = document.getElementById(modalId);
            if (modalEl) {
                modalEl.classList.remove('hidden');
                modalEl.classList.add('flex');
                if (modalId === 'modal-fijar-fechas-dgt') {
                    limitarFechasAlMesSeleccionado();
                }
            }
            return;
        }

        // Cierre genérico de modales
        const closeBtn = event.target.closest('[data-modal-close]');
        if (closeBtn) {
            const modalId = closeBtn.getAttribute('data-modal-close');
            const modalEl = document.getElementById(modalId);
            if (modalEl) {
                modalEl.classList.add('hidden');
                modalEl.classList.remove('flex');
            }
            return;
        }

        // 2. MODAL EN 2 PASOS: FIJAR FECHAS DGT
        // Paso 1 -> Paso 2: Valida el formulario y muestra la pantalla de confirmación
        if (event.target.closest('#btn-pre-fijar-fechas')) {
            const form = document.getElementById('form-fijar-fechas-dgt');
            if (form && form.checkValidity()) {
                const paso1 = document.getElementById('contenedor-paso-1-fechas');
                const submodal = document.getElementById('submodal-confirmar-fijar-fechas');
                if (paso1) paso1.classList.add('hidden');
                if (submodal) submodal.classList.remove('hidden');
            } else if (form) {
                form.reportValidity();
            }
            return;
        }

        // Cancelar Paso 2 y regresar al Paso 1
        if (event.target.closest('#btn-cancelar-submodal-fechas')) {
            const paso1 = document.getElementById('contenedor-paso-1-fechas');
            const submodal = document.getElementById('submodal-confirmar-fijar-fechas');
            if (submodal) submodal.classList.add('hidden');
            if (paso1) paso1.classList.remove('hidden');
            return;
        }

        // Confirmar y enviar el formulario de fechas al backend
        if (event.target.closest('#btn-ejecutar-fijar-fechas')) {
            const form = document.getElementById('form-fijar-fechas-dgt');
            if (form) form.submit();
            return;
        }

        // 3. MODAL EN 2 PASOS: FIJAR CUPOS DGT
        if (event.target.closest('#btn-pre-fijar-cupos')) {
            const form = document.getElementById('form-fijar-cupos-dgt');
            if (form && form.checkValidity()) {
                const paso1 = document.getElementById('contenedor-paso-1-cupos');
                const submodal = document.getElementById('submodal-confirmar-fijar-cupos');
                if (paso1) paso1.classList.add('hidden');
                if (submodal) submodal.classList.remove('hidden');
            } else if (form) {
                form.reportValidity();
            }
            return;
        }

        if (event.target.closest('#btn-cancelar-submodal-cupos')) {
            const paso1 = document.getElementById('contenedor-paso-1-cupos');
            const submodal = document.getElementById('submodal-confirmar-fijar-cupos');
            if (submodal) submodal.classList.add('hidden');
            if (paso1) paso1.classList.remove('hidden');
            return;
        }

        if (event.target.closest('#btn-ejecutar-fijar-cupos')) {
            const form = document.getElementById('form-fijar-cupos-dgt');
            if (form) form.submit();
            return;
        }

        // 4. GESTIÓN DE SOLICITUDES DE EXAMEN (ACEPTAR / RECHAZAR)
        // Desplegar campo de motivo de rechazo
        if (event.target.closest('#btn-desplegar-rechazar')) {
            const seccionRechazar = document.getElementById('seccion-rechazar-solicitud');
            if (seccionRechazar) seccionRechazar.classList.remove('hidden');
            return;
        }

        if (event.target.closest('#btn-cancelar-despliegue-rechazar')) {
            const seccionRechazar = document.getElementById('seccion-rechazar-solicitud');
            if (seccionRechazar) seccionRechazar.classList.add('hidden');
            return;
        }

        // Doble confirmación para Aceptar Solicitud
        if (event.target.closest('#btn-pre-aceptar')) {
            const paso1 = document.getElementById('contenedor-paso-1-solicitud');
            const submodal = document.getElementById('submodal-confirmar-aceptar');
            if (paso1) paso1.classList.add('hidden');
            if (submodal) submodal.classList.remove('hidden');
            return;
        }

        if (event.target.closest('#btn-cancelar-submodal-aceptar')) {
            const paso1 = document.getElementById('contenedor-paso-1-solicitud');
            const submodal = document.getElementById('submodal-confirmar-aceptar');
            if (submodal) submodal.classList.add('hidden');
            if (paso1) paso1.classList.remove('hidden');
            return;
        }

        if (event.target.closest('#btn-ejecutar-aceptar')) {
            const form = document.getElementById('form-aceptar-solicitud');
            if (form) form.submit();
            return;
        }

        // Doble confirmación para Rechazar Solicitud (requiere motivo no vacío)
        if (event.target.closest('#btn-pre-rechazar')) {
            const motivoText = document.getElementById('motivoRechazo');
            if (motivoText && motivoText.value.trim() === '') {
                motivoText.focus();
                motivoText.reportValidity();
                return;
            }
            const paso1 = document.getElementById('contenedor-paso-1-solicitud');
            const submodal = document.getElementById('submodal-confirmar-rechazar');
            if (paso1) paso1.classList.add('hidden');
            if (submodal) submodal.classList.remove('hidden');
            return;
        }

        if (event.target.closest('#btn-cancelar-submodal-rechazar')) {
            const paso1 = document.getElementById('contenedor-paso-1-solicitud');
            const submodal = document.getElementById('submodal-confirmar-rechazar');
            if (submodal) submodal.classList.add('hidden');
            if (paso1) paso1.classList.remove('hidden');
            return;
        }

        if (event.target.closest('#btn-ejecutar-rechazar')) {
            const form = document.getElementById('form-rechazar-solicitud');
            if (form) form.submit();
            return;
        }
    });

    // Atajo de teclado: pulsar Enter en el filtro de búsqueda dispara la consulta
    const inputBusqueda = document.getElementById('filtro-busqueda');
    if (inputBusqueda) {
        inputBusqueda.addEventListener('keydown', (event) => {
            if (event.key === 'Enter') {
                event.preventDefault();
                const btnBuscar = document.getElementById('btn-buscar-solicitudes');
                if (btnBuscar) {
                    btnBuscar.click();
                }
            }
        });
    }

    // Cierre accesible con tecla Escape
    document.addEventListener('keydown', (event) => {
        if (event.key === 'Escape') {
            document.querySelectorAll('[role="dialog"]:not(.hidden)').forEach(modal => {
                modal.classList.add('hidden');
                modal.classList.remove('flex');
            });
        }
    });
}

/**
 * Limita los selectores nativos de fecha y hora estrictamente al mes seleccionado.
 * Impide seleccionar, navegar o escribir fechas pertenecientes a meses o años distintos.
 */
function limitarFechasAlMesSeleccionado() {
    const form = document.getElementById('form-fijar-fechas-dgt');
    if (!form) return;

    const inputMes = form.querySelector('input[name="mes"]');
    if (!inputMes || !inputMes.value) return;

    // Descompone el mes seleccionado (formato "YYYY-MM", ej. "2026-10")
    const valorMes = inputMes.value;
    const partes = valorMes.split('-');
    const anio = parseInt(partes[0], 10);
    const mes = parseInt(partes[1], 10);

    // Truco de Date en JS: el día 0 del mes siguiente devuelve el último día del mes actual
    const ultimoDia = new Date(anio, mes, 0).getDate();
    const mesStr = String(mes).padStart(2, '0');
    const ultimoDiaStr = String(ultimoDia).padStart(2, '0');

    // Rangos ISO válidos para inputs datetime-local (dentro de la jornada laboral)
    const minIso = `${anio}-${mesStr}-01T08:00`;
    const maxIso = `${anio}-${mesStr}-${ultimoDiaStr}T21:45`;

    const fecha1Input = document.getElementById('fechaHora1');
    const fecha2Input = document.getElementById('fechaHora2');

    [fecha1Input, fecha2Input].forEach((input, index) => {
        if (!input) return;

        // Asignación de límites HTML5
        input.min = minIso;
        input.max = maxIso;

        // Si no tiene valor o está fuera de rango, sugerir días hábiles por defecto (día 10 y 24)
        if (!input.value || input.value < minIso || input.value > maxIso) {
            const diaSugerido = index === 0 ? '10' : '24';
            input.value = `${anio}-${mesStr}-${diaSugerido}T08:30`;
        }

        // Validación reactiva mediante addEventListener al modificar la fecha
        input.addEventListener('change', () => {
            if (input.value) {
                if (input.value < minIso || input.value > maxIso) {
                    input.setCustomValidity(`Solo puedes seleccionar fechas de ${mesStr}/${anio}.`);
                    input.reportValidity();
                    const diaSugerido = index === 0 ? '10' : '24';
                    input.value = `${anio}-${mesStr}-${diaSugerido}T08:30`;
                } else {
                    input.setCustomValidity('');
                }
            }
        });
    });
}
