/**
 * Módulo JavaScript no intrusivo para la gestión de Convocatorias, Cupos y Solicitudes DGT (Rol Admin).
 * Implementa el patrón Named Event Handlers y modales de doble confirmación con pantallas limpias sin superposiciones.
 */
document.addEventListener('DOMContentLoaded', () => {
    inicializarExamenesAdmin();
    limitarFechasAlMesSeleccionado();
});

document.addEventListener('htmx:afterSwap', (event) => {
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
    // 1. Modales globales (Abrir y Cerrar)
    document.addEventListener('click', (event) => {
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

        // 2. Modales en 2 pasos para Fijar Fechas DGT
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

        if (event.target.closest('#btn-cancelar-submodal-fechas')) {
            const paso1 = document.getElementById('contenedor-paso-1-fechas');
            const submodal = document.getElementById('submodal-confirmar-fijar-fechas');
            if (submodal) submodal.classList.add('hidden');
            if (paso1) paso1.classList.remove('hidden');
            return;
        }

        if (event.target.closest('#btn-ejecutar-fijar-fechas')) {
            const form = document.getElementById('form-fijar-fechas-dgt');
            if (form) form.submit();
            return;
        }

        // 3. Modales en 2 pasos para Fijar Cupos DGT
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

        // 4. Modal de Edición de Solicitud (HTMX Dinámico)
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

        // Paso 2: Aceptar Solicitud (Oculta paso 1 completamente y muestra confirmación limpia)
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

        // Paso 2: Rechazar Solicitud (Oculta paso 1 completamente y muestra confirmación limpia)
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

    // Atajo de teclado: Enter en el buscador de solicitudes para forzar la búsqueda en servidor
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

    // Cerrar modales con tecla Escape
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

    const valorMes = inputMes.value;
    const partes = valorMes.split('-');
    const anio = parseInt(partes[0], 10);
    const mes = parseInt(partes[1], 10);

    const ultimoDia = new Date(anio, mes, 0).getDate();
    const mesStr = String(mes).padStart(2, '0');
    const ultimoDiaStr = String(ultimoDia).padStart(2, '0');

    const minIso = `${anio}-${mesStr}-01T08:00`;
    const maxIso = `${anio}-${mesStr}-${ultimoDiaStr}T21:45`;

    const fecha1Input = document.getElementById('fechaHora1');
    const fecha2Input = document.getElementById('fechaHora2');

    [fecha1Input, fecha2Input].forEach((input, index) => {
        if (!input) return;

        input.min = minIso;
        input.max = maxIso;

        if (!input.value || input.value < minIso || input.value > maxIso) {
            const diaSugerido = index === 0 ? '10' : '24';
            input.value = `${anio}-${mesStr}-${diaSugerido}T08:30`;
        }

        input.onchange = () => {
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
        };
    });
}
