/**
 * admin-flota.js
 * JavaScript no intrusivo y modular para la Gestión de Flota de Vehículos en ManDS ERP.
 * Implementa el Patrón de Manejadores de Eventos con Nombre y Punto de Entrada de Inicialización.
 *
 * Responsabilidades:
 * 1. Restricciones dinámicas y cruzadas de fechas de revisión (ITV) y año de matriculación.
 * 2. Validación en tiempo real de formato de matrícula DGT, potencias, kilometrajes no negativos y textos.
 * 3. Prevención de doble alta/modificación y sincronización completa con el ciclo de vida HTMX.
 */
(function () {
    'use strict';

    // Helpers locales con fallback seguro a AdminUI
    function formatFecha(fecha) {
        if (window.AdminUI && window.AdminUI.formatearFechaISO) {
            return window.AdminUI.formatearFechaISO(fecha);
        }
        const a = fecha.getFullYear();
        const m = String(fecha.getMonth() + 1).padStart(2, '0');
        const d = String(fecha.getDate()).padStart(2, '0');
        return `${a}-${m}-${d}`;
    }

    function mostrarError(inputId, feedbackId, mensaje) {
        if (window.AdminUI && window.AdminUI.mostrarErrorCampo) {
            window.AdminUI.mostrarErrorCampo(inputId, feedbackId, mensaje);
        }
    }

    function limpiarError(inputId, feedbackId) {
        if (window.AdminUI && window.AdminUI.limpiarErrorCampo) {
            window.AdminUI.limpiarErrorCampo(inputId, feedbackId);
        }
    }

    // =========================================================================
    // CONFIGURACIÓN DE RESTRICCIONES DE FECHAS EN CALENDARIOS
    // =========================================================================
    function configurarRestriccionesFechasVehiculo(sufijo = '') {
        const inputAnio = document.getElementById(`vehiculo-anio${sufijo}`);
        const inputUltima = document.getElementById(`vehiculo-fecha-ultima${sufijo}`);
        const inputProxima = document.getElementById(`vehiculo-fecha-proxima${sufijo}`);
        const hoy = new Date();

        if (inputAnio) {
            inputAnio.setAttribute('min', '1990');
            inputAnio.setAttribute('max', hoy.getFullYear().toString());
        }

        const hace4Anios = new Date(hoy.getFullYear() - 4, hoy.getMonth(), hoy.getDate());
        const en10Anios = new Date(hoy.getFullYear() + 10, hoy.getMonth(), hoy.getDate());

        if (inputUltima) {
            inputUltima.setAttribute('min', formatFecha(hace4Anios));
            let maxUltima = new Date(hoy.getFullYear(), hoy.getMonth(), hoy.getDate());
            if (inputProxima && inputProxima.value.trim()) {
                const partesProxima = inputProxima.value.trim().split('-');
                if (partesProxima.length === 3) {
                    const fProx = new Date(parseInt(partesProxima[0], 10), parseInt(partesProxima[1], 10) - 1, parseInt(partesProxima[2], 10));
                    if (!isNaN(fProx.getTime())) {
                        const diaAntes = new Date(fProx.getFullYear(), fProx.getMonth(), fProx.getDate() - 1);
                        if (diaAntes < maxUltima) {
                            maxUltima = diaAntes;
                        }
                    }
                }
            }
            inputUltima.setAttribute('max', formatFecha(maxUltima));
        }

        if (inputProxima) {
            inputProxima.setAttribute('max', formatFecha(en10Anios));
            let minProxima = new Date(hoy.getFullYear(), hoy.getMonth(), hoy.getDate());
            if (inputUltima && inputUltima.value.trim()) {
                const partesUltima = inputUltima.value.trim().split('-');
                if (partesUltima.length === 3) {
                    const fUlt = new Date(parseInt(partesUltima[0], 10), parseInt(partesUltima[1], 10) - 1, parseInt(partesUltima[2], 10));
                    if (!isNaN(fUlt.getTime())) {
                        const diaDespues = new Date(fUlt.getFullYear(), fUlt.getMonth(), fUlt.getDate() + 1);
                        minProxima = diaDespues;
                    }
                }
            }
            inputProxima.setAttribute('min', formatFecha(minProxima));
        }
    }

    // =========================================================================
    // VALIDACIONES DE CAMPOS INDIVIDUALES
    // =========================================================================
    function validarMatriculaVehiculo(sufijo = '') {
        const inputId = `vehiculo-matricula${sufijo}`;
        const feedbackId = `feedback-matricula${sufijo}`;
        const input = document.getElementById(inputId);
        if (!input || input.disabled || input.readOnly) return true;

        const valor = input.value ? input.value.trim().toUpperCase() : '';
        if (!valor) {
            mostrarError(inputId, feedbackId, 'La matrícula es obligatoria.');
            return false;
        }

        const regexMatricula = /^[0-9]{4}[ -]?[A-Za-z]{3}$/;
        if (!regexMatricula.test(valor)) {
            mostrarError(inputId, feedbackId, 'Formato de matrícula inválido (ej. 1234-LMN o 1234LMN).');
            return false;
        }

        const feedback = document.getElementById(feedbackId);
        if (!feedback || !feedback.querySelector('.mensaje-error-campo')) {
            limpiarError(inputId, feedbackId);
        }
        return true;
    }

    function validarMarcaVehiculo(sufijo = '') {
        const inputId = `vehiculo-marca${sufijo}`;
        const feedbackId = `feedback-marca${sufijo}`;
        const input = document.getElementById(inputId);
        if (!input) return true;

        const valor = input.value ? input.value.trim() : '';
        if (!valor) {
            mostrarError(inputId, feedbackId, 'La marca es obligatoria.');
            return false;
        }
        if (valor.length > 50) {
            mostrarError(inputId, feedbackId, 'La marca no puede superar los 50 caracteres.');
            return false;
        }

        limpiarError(inputId, feedbackId);
        return true;
    }

    function validarModeloVehiculo(sufijo = '') {
        const inputId = `vehiculo-modelo${sufijo}`;
        const feedbackId = `feedback-modelo${sufijo}`;
        const input = document.getElementById(inputId);
        if (!input) return true;

        const valor = input.value ? input.value.trim() : '';
        if (!valor) {
            mostrarError(inputId, feedbackId, 'El modelo es obligatorio.');
            return false;
        }
        if (valor.length > 50) {
            mostrarError(inputId, feedbackId, 'El modelo no puede superar los 50 caracteres.');
            return false;
        }

        limpiarError(inputId, feedbackId);
        return true;
    }

    function validarColorVehiculo(sufijo = '') {
        const inputId = `vehiculo-color${sufijo}`;
        const feedbackId = `feedback-color${sufijo}`;
        const input = document.getElementById(inputId);
        if (!input) return true;

        const valor = input.value ? input.value.trim() : '';
        if (!valor) {
            mostrarError(inputId, feedbackId, 'El color es obligatorio.');
            return false;
        }
        if (valor.length > 30) {
            mostrarError(inputId, feedbackId, 'El color no puede superar los 30 caracteres.');
            return false;
        }

        limpiarError(inputId, feedbackId);
        return true;
    }

    function validarAnioVehiculo(sufijo = '') {
        const inputId = `vehiculo-anio${sufijo}`;
        const feedbackId = `feedback-anio${sufijo}`;
        const input = document.getElementById(inputId);
        if (!input) return true;

        const valorStr = input.value ? input.value.trim() : '';
        if (!valorStr) {
            mostrarError(inputId, feedbackId, 'El año de matriculación es obligatorio.');
            return false;
        }

        const anio = parseInt(valorStr, 10);
        const anioActual = new Date().getFullYear();
        if (isNaN(anio) || anio < 1990 || anio > anioActual) {
            mostrarError(inputId, feedbackId, `El año de matriculación debe estar comprendido entre 1990 y ${anioActual}.`);
            return false;
        }

        limpiarError(inputId, feedbackId);
        return true;
    }

    function validarCvVehiculo(sufijo = '') {
        const inputId = `vehiculo-cv${sufijo}`;
        const feedbackId = `feedback-cv${sufijo}`;
        const input = document.getElementById(inputId);
        if (!input) return true;

        const valorStr = input.value ? input.value.trim() : '';
        if (!valorStr) {
            mostrarError(inputId, feedbackId, 'La potencia del motor es obligatoria.');
            return false;
        }

        const cv = parseInt(valorStr, 10);
        if (isNaN(cv) || cv < 0 || cv > 1000) {
            mostrarError(inputId, feedbackId, 'La potencia debe ser un valor válido entre 0 y 1000 CV.');
            return false;
        }

        limpiarError(inputId, feedbackId);
        return true;
    }

    function validarKmVehiculo(sufijo = '') {
        const inputId = `vehiculo-km${sufijo}`;
        const feedbackId = `feedback-km${sufijo}`;
        const input = document.getElementById(inputId);
        if (!input) return true;

        const valorStr = input.value ? input.value.trim() : '';
        if (!valorStr) {
            mostrarError(inputId, feedbackId, 'El kilometraje es obligatorio.');
            return false;
        }

        const km = parseInt(valorStr, 10);
        if (isNaN(km) || km < 0) {
            mostrarError(inputId, feedbackId, 'El kilometraje no puede ser negativo.');
            return false;
        }

        limpiarError(inputId, feedbackId);
        return true;
    }

    function validarFechasRevisionVehiculo(sufijo = '') {
        const inputUltima = document.getElementById(`vehiculo-fecha-ultima${sufijo}`);
        const inputProxima = document.getElementById(`vehiculo-fecha-proxima${sufijo}`);
        const feedbackProximaId = `feedback-fecha-proxima${sufijo}`;
        const feedbackUltimaId = `feedback-fecha-ultima${sufijo}`;
        if (!inputProxima) return true;

        configurarRestriccionesFechasVehiculo(sufijo);

        const hoy = new Date();
        const hoySinHora = new Date(hoy.getFullYear(), hoy.getMonth(), hoy.getDate());
        const en10Anios = new Date(hoy.getFullYear() + 10, hoy.getMonth(), hoy.getDate());
        const hace4Anios = new Date(hoy.getFullYear() - 4, hoy.getMonth(), hoy.getDate());

        let esUltimaValida = true;
        let fechaUltima = null;

        if (inputUltima && inputUltima.value.trim()) {
            const valUltima = inputUltima.value.trim();
            const partesUltima = valUltima.split('-');
            if (partesUltima.length !== 3) {
                mostrarError(`vehiculo-fecha-ultima${sufijo}`, feedbackUltimaId, 'Formato de fecha inválido.');
                esUltimaValida = false;
            } else {
                fechaUltima = new Date(parseInt(partesUltima[0], 10), parseInt(partesUltima[1], 10) - 1, parseInt(partesUltima[2], 10));
                if (isNaN(fechaUltima.getTime())) {
                    mostrarError(`vehiculo-fecha-ultima${sufijo}`, feedbackUltimaId, 'Introduce una fecha de última revisión válida.');
                    esUltimaValida = false;
                } else if (fechaUltima > hoySinHora) {
                    mostrarError(`vehiculo-fecha-ultima${sufijo}`, feedbackUltimaId, 'La fecha de la última revisión no puede ser posterior a hoy.');
                    esUltimaValida = false;
                } else if (fechaUltima < hace4Anios) {
                    mostrarError(`vehiculo-fecha-ultima${sufijo}`, feedbackUltimaId, 'La fecha de la última revisión no puede ser anterior a hace 4 años.');
                    esUltimaValida = false;
                } else {
                    limpiarError(`vehiculo-fecha-ultima${sufijo}`, feedbackUltimaId);
                }
            }
        } else if (inputUltima) {
            limpiarError(`vehiculo-fecha-ultima${sufijo}`, feedbackUltimaId);
        }

        let esProximaValida = true;
        let fechaProxima = null;
        const valProxima = inputProxima.value.trim();

        if (!valProxima) {
            mostrarError(`vehiculo-fecha-proxima${sufijo}`, feedbackProximaId, 'La fecha de próxima revisión o ITV es obligatoria.');
            esProximaValida = false;
        } else {
            const partesProxima = valProxima.split('-');
            if (partesProxima.length !== 3) {
                mostrarError(`vehiculo-fecha-proxima${sufijo}`, feedbackProximaId, 'Formato de fecha inválido.');
                esProximaValida = false;
            } else {
                fechaProxima = new Date(parseInt(partesProxima[0], 10), parseInt(partesProxima[1], 10) - 1, parseInt(partesProxima[2], 10));
                if (isNaN(fechaProxima.getTime())) {
                    mostrarError(`vehiculo-fecha-proxima${sufijo}`, feedbackProximaId, 'Introduce una fecha de próxima revisión válida.');
                    esProximaValida = false;
                } else if (fechaProxima > en10Anios) {
                    mostrarError(`vehiculo-fecha-proxima${sufijo}`, feedbackProximaId, 'La fecha de la próxima revisión no puede superar los 10 años en el futuro.');
                    esProximaValida = false;
                } else if (fechaProxima < hoySinHora) {
                    mostrarError(`vehiculo-fecha-proxima${sufijo}`, feedbackProximaId, 'La fecha de la próxima revisión no puede ser anterior a la fecha actual.');
                    esProximaValida = false;
                } else {
                    limpiarError(`vehiculo-fecha-proxima${sufijo}`, feedbackProximaId);
                }
            }
        }

        if (esUltimaValida && esProximaValida && fechaUltima && fechaProxima && fechaProxima <= fechaUltima) {
            mostrarError(`vehiculo-fecha-proxima${sufijo}`, feedbackProximaId, 'La fecha de la próxima revisión debe ser estrictamente posterior a la fecha de la última revisión.');
            esProximaValida = false;
        }

        return esUltimaValida && esProximaValida;
    }

    function validarFormularioVehiculo(formId, sufijo = '') {
        const form = document.getElementById(formId);
        if (!form) return true;

        const vMatricula = validarMatriculaVehiculo(sufijo);
        const vMarca = validarMarcaVehiculo(sufijo);
        const vModelo = validarModeloVehiculo(sufijo);
        const vColor = validarColorVehiculo(sufijo);
        const vAnio = validarAnioVehiculo(sufijo);
        const vCv = validarCvVehiculo(sufijo);
        const vKm = validarKmVehiculo(sufijo);
        const vFechas = validarFechasRevisionVehiculo(sufijo);

        const errorActivo = form.querySelector('.mensaje-error-campo');

        if (!vMatricula || !vMarca || !vModelo || !vColor || !vAnio || !vCv || !vKm || !vFechas || errorActivo) {
            const primerInvalido = form.querySelector('.border-red-500') || form.querySelector(':invalid');
            if (primerInvalido) {
                primerInvalido.focus();
            }
            return false;
        }

        return true;
    }

    // =========================================================================
    // MANEJADORES DE EVENTOS CON NOMBRE
    // =========================================================================
    function manejarBlurMatriculaAlta() { validarMatriculaVehiculo(''); }
    function manejarInputMatriculaAlta() {
        const el = document.getElementById('vehiculo-matricula');
        if (el && (el.classList.contains('border-red-500') || el.value.trim())) validarMatriculaVehiculo('');
    }
    function manejarBlurMatriculaEdicion() { validarMatriculaVehiculo('-editar'); }
    function manejarInputMatriculaEdicion() {
        const el = document.getElementById('vehiculo-matricula-editar');
        if (el && (el.classList.contains('border-red-500') || el.value.trim())) validarMatriculaVehiculo('-editar');
    }

    function manejarBlurMarcaAlta() { validarMarcaVehiculo(''); }
    function manejarInputMarcaAlta() {
        const el = document.getElementById('vehiculo-marca');
        if (el && (el.classList.contains('border-red-500') || el.value.trim())) validarMarcaVehiculo('');
    }
    function manejarBlurMarcaEdicion() { validarMarcaVehiculo('-editar'); }
    function manejarInputMarcaEdicion() {
        const el = document.getElementById('vehiculo-marca-editar');
        if (el && (el.classList.contains('border-red-500') || el.value.trim())) validarMarcaVehiculo('-editar');
    }

    function manejarBlurModeloAlta() { validarModeloVehiculo(''); }
    function manejarInputModeloAlta() {
        const el = document.getElementById('vehiculo-modelo');
        if (el && (el.classList.contains('border-red-500') || el.value.trim())) validarModeloVehiculo('');
    }
    function manejarBlurModeloEdicion() { validarModeloVehiculo('-editar'); }
    function manejarInputModeloEdicion() {
        const el = document.getElementById('vehiculo-modelo-editar');
        if (el && (el.classList.contains('border-red-500') || el.value.trim())) validarModeloVehiculo('-editar');
    }

    function manejarBlurColorAlta() { validarColorVehiculo(''); }
    function manejarInputColorAlta() {
        const el = document.getElementById('vehiculo-color');
        if (el && (el.classList.contains('border-red-500') || el.value.trim())) validarColorVehiculo('');
    }
    function manejarBlurColorEdicion() { validarColorVehiculo('-editar'); }
    function manejarInputColorEdicion() {
        const el = document.getElementById('vehiculo-color-editar');
        if (el && (el.classList.contains('border-red-500') || el.value.trim())) validarColorVehiculo('-editar');
    }

    function manejarBlurAnioAlta() { validarAnioVehiculo(''); }
    function manejarInputAnioAlta() {
        const el = document.getElementById('vehiculo-anio');
        if (el && (el.classList.contains('border-red-500') || el.value.trim())) validarAnioVehiculo('');
    }
    function manejarBlurAnioEdicion() { validarAnioVehiculo('-editar'); }
    function manejarInputAnioEdicion() {
        const el = document.getElementById('vehiculo-anio-editar');
        if (el && (el.classList.contains('border-red-500') || el.value.trim())) validarAnioVehiculo('-editar');
    }

    function manejarBlurCvAlta() { validarCvVehiculo(''); }
    function manejarInputCvAlta() {
        const el = document.getElementById('vehiculo-cv');
        if (el && (el.classList.contains('border-red-500') || el.value.trim())) validarCvVehiculo('');
    }
    function manejarBlurCvEdicion() { validarCvVehiculo('-editar'); }
    function manejarInputCvEdicion() {
        const el = document.getElementById('vehiculo-cv-editar');
        if (el && (el.classList.contains('border-red-500') || el.value.trim())) validarCvVehiculo('-editar');
    }

    function manejarBlurKmAlta() { validarKmVehiculo(''); }
    function manejarInputKmAlta() {
        const el = document.getElementById('vehiculo-km');
        if (el && (el.classList.contains('border-red-500') || el.value.trim())) validarKmVehiculo('');
    }
    function manejarBlurKmEdicion() { validarKmVehiculo('-editar'); }
    function manejarInputKmEdicion() {
        const el = document.getElementById('vehiculo-km-editar');
        if (el && (el.classList.contains('border-red-500') || el.value.trim())) validarKmVehiculo('-editar');
    }

    function manejarChangeFechasAlta() { validarFechasRevisionVehiculo(''); }
    function manejarChangeFechasEdicion() { validarFechasRevisionVehiculo('-editar'); }

    function manejarSubmitFormAltaVehiculo(evento) {
        if (!validarFormularioVehiculo('form-alta-vehiculo', '')) {
            evento.preventDefault();
            evento.stopImmediatePropagation();
        } else {
            const btnSubmit = document.getElementById('btn-submit-alta-vehiculo');
            if (btnSubmit) btnSubmit.disabled = true;
        }
    }

    function manejarSubmitFormEditarVehiculo(evento) {
        if (!validarFormularioVehiculo('form-editar-vehiculo', '-editar')) {
            evento.preventDefault();
            evento.stopImmediatePropagation();
        } else {
            const btnSubmit = document.getElementById('btn-submit-editar-vehiculo');
            if (btnSubmit) btnSubmit.disabled = true;
        }
    }

    /**
     * Intercepta evento de confirmación HTMX para abortar peticiones no válidas.
     */
    function manejarHtmxConfirmVehiculo(evento) {
        const formId = evento.target ? evento.target.id : '';
        if (formId === 'form-alta-vehiculo') {
            if (!validarFormularioVehiculo('form-alta-vehiculo', '')) {
                evento.preventDefault();
            } else {
                const btnSubmit = document.getElementById('btn-submit-alta-vehiculo');
                if (btnSubmit) btnSubmit.disabled = true;
            }
        } else if (formId === 'form-editar-vehiculo') {
            if (!validarFormularioVehiculo('form-editar-vehiculo', '-editar')) {
                evento.preventDefault();
            } else {
                const btnSubmit = document.getElementById('btn-submit-editar-vehiculo');
                if (btnSubmit) btnSubmit.disabled = true;
            }
        }
    }

    function manejarHtmxErrorVehiculo(evento) {
        const formId = evento.target ? evento.target.id : '';
        if (formId === 'form-alta-vehiculo') {
            const btn = document.getElementById('btn-submit-alta-vehiculo');
            if (btn) btn.disabled = false;
        } else if (formId === 'form-editar-vehiculo') {
            const btn = document.getElementById('btn-submit-editar-vehiculo');
            if (btn) btn.disabled = false;
        }
    }

    /**
     * Sincroniza clases visuales y reinicializa modales re-renderizados tras intercambios HTMX.
     */
    function manejarHtmxAfterSwapVehiculo(evento) {
        const targetId = evento.detail && evento.detail.target ? evento.detail.target.id : '';

        // Sincronizar feedback de campos individuales
        if (targetId && targetId.startsWith('feedback-')) {
            const campoNombre = targetId.replace('feedback-', '');
            const input = document.getElementById(`vehiculo-${campoNombre}`);
            const tieneError = evento.detail.target.querySelector('.mensaje-error-campo') !== null;
            if (input) {
                if (tieneError) {
                    input.classList.add('border-red-500', 'focus:ring-red-500', 'dark:border-red-500');
                    input.classList.remove('border-slate-300', 'dark:border-slate-700', 'focus:ring-blue-600');
                } else {
                    input.classList.remove('border-red-500', 'focus:ring-red-500', 'dark:border-red-500');
                    input.classList.add('border-slate-300', 'dark:border-slate-700', 'focus:ring-blue-600');
                }
            }
        }

        // Reinicializar modales cargados dinámicamente
        if (targetId === 'contenedor-modal-alta-vehiculo' || targetId === 'modal-alta-vehiculo') {
            vincularEventosModalAltaVehiculo();
        }
        if (targetId === 'contenedor-modal-editar-vehiculo' || targetId === 'modal-editar-vehiculo') {
            vincularEventosModalEditarVehiculo();
        }
    }

    // =========================================================================
    // VINCULACIÓN DE EVENTOS POR MODAL
    // =========================================================================
    function vincularEventosModalAltaVehiculo() {
        const form = document.getElementById('form-alta-vehiculo');
        if (!form || form._eventosVehiculoInicializados) return;
        form._eventosVehiculoInicializados = true;

        configurarRestriccionesFechasVehiculo('');

        const inputMatricula = document.getElementById('vehiculo-matricula');
        if (inputMatricula) {
            inputMatricula.addEventListener('blur', manejarBlurMatriculaAlta);
            inputMatricula.addEventListener('input', manejarInputMatriculaAlta);
        }

        const inputMarca = document.getElementById('vehiculo-marca');
        if (inputMarca) {
            inputMarca.addEventListener('blur', manejarBlurMarcaAlta);
            inputMarca.addEventListener('input', manejarInputMarcaAlta);
        }

        const inputModelo = document.getElementById('vehiculo-modelo');
        if (inputModelo) {
            inputModelo.addEventListener('blur', manejarBlurModeloAlta);
            inputModelo.addEventListener('input', manejarInputModeloAlta);
        }

        const inputColor = document.getElementById('vehiculo-color');
        if (inputColor) {
            inputColor.addEventListener('blur', manejarBlurColorAlta);
            inputColor.addEventListener('input', manejarInputColorAlta);
        }

        const inputAnio = document.getElementById('vehiculo-anio');
        if (inputAnio) {
            inputAnio.addEventListener('blur', manejarBlurAnioAlta);
            inputAnio.addEventListener('input', manejarInputAnioAlta);
        }

        const inputCv = document.getElementById('vehiculo-cv');
        if (inputCv) {
            inputCv.addEventListener('blur', manejarBlurCvAlta);
            inputCv.addEventListener('input', manejarInputCvAlta);
        }

        const inputKm = document.getElementById('vehiculo-km');
        if (inputKm) {
            inputKm.addEventListener('blur', manejarBlurKmAlta);
            inputKm.addEventListener('input', manejarInputKmAlta);
        }

        const inputUltima = document.getElementById('vehiculo-fecha-ultima');
        if (inputUltima) {
            inputUltima.addEventListener('change', manejarChangeFechasAlta);
            inputUltima.addEventListener('blur', manejarChangeFechasAlta);
        }

        const inputProxima = document.getElementById('vehiculo-fecha-proxima');
        if (inputProxima) {
            inputProxima.addEventListener('change', manejarChangeFechasAlta);
            inputProxima.addEventListener('blur', manejarChangeFechasAlta);
        }

        form.addEventListener('submit', manejarSubmitFormAltaVehiculo);
    }

    function vincularEventosModalEditarVehiculo() {
        const form = document.getElementById('form-editar-vehiculo');
        if (!form || form._eventosVehiculoInicializados) return;
        form._eventosVehiculoInicializados = true;

        configurarRestriccionesFechasVehiculo('-editar');

        const inputMatricula = document.getElementById('vehiculo-matricula-editar');
        if (inputMatricula && !inputMatricula.disabled && !inputMatricula.readOnly) {
            inputMatricula.addEventListener('blur', manejarBlurMatriculaEdicion);
            inputMatricula.addEventListener('input', manejarInputMatriculaEdicion);
        }

        const inputMarca = document.getElementById('vehiculo-marca-editar');
        if (inputMarca) {
            inputMarca.addEventListener('blur', manejarBlurMarcaEdicion);
            inputMarca.addEventListener('input', manejarInputMarcaEdicion);
        }

        const inputModelo = document.getElementById('vehiculo-modelo-editar');
        if (inputModelo) {
            inputModelo.addEventListener('blur', manejarBlurModeloEdicion);
            inputModelo.addEventListener('input', manejarInputModeloEdicion);
        }

        const inputColor = document.getElementById('vehiculo-color-editar');
        if (inputColor) {
            inputColor.addEventListener('blur', manejarBlurColorEdicion);
            inputColor.addEventListener('input', manejarInputColorEdicion);
        }

        const inputAnio = document.getElementById('vehiculo-anio-editar');
        if (inputAnio) {
            inputAnio.addEventListener('blur', manejarBlurAnioEdicion);
            inputAnio.addEventListener('input', manejarInputAnioEdicion);
        }

        const inputCv = document.getElementById('vehiculo-cv-editar');
        if (inputCv) {
            inputCv.addEventListener('blur', manejarBlurCvEdicion);
            inputCv.addEventListener('input', manejarInputCvEdicion);
        }

        const inputKm = document.getElementById('vehiculo-km-editar');
        if (inputKm) {
            inputKm.addEventListener('blur', manejarBlurKmEdicion);
            inputKm.addEventListener('input', manejarInputKmEdicion);
        }

        const inputUltima = document.getElementById('vehiculo-fecha-ultima-editar');
        if (inputUltima) {
            inputUltima.addEventListener('change', manejarChangeFechasEdicion);
            inputUltima.addEventListener('blur', manejarChangeFechasEdicion);
        }

        const inputProxima = document.getElementById('vehiculo-fecha-proxima-editar');
        if (inputProxima) {
            inputProxima.addEventListener('change', manejarChangeFechasEdicion);
            inputProxima.addEventListener('blur', manejarChangeFechasEdicion);
        }

        form.addEventListener('submit', manejarSubmitFormEditarVehiculo);
    }

    // =========================================================================
    // PUNTO DE ENTRADA DE INICIALIZACIÓN
    // =========================================================================
    function inicializarGestionFlota() {
        // Inicializar modales estáticos en el DOM
        vincularEventosModalAltaVehiculo();
        vincularEventosModalEditarVehiculo();

        // Escuchadores globales HTMX para flota
        document.body.addEventListener('htmx:confirm', manejarHtmxConfirmVehiculo);
        document.body.addEventListener('htmx:responseError', manejarHtmxErrorVehiculo);
        document.body.addEventListener('htmx:sendError', manejarHtmxErrorVehiculo);
        document.body.addEventListener('htmx:afterSwap', manejarHtmxAfterSwapVehiculo);
    }

    document.addEventListener('DOMContentLoaded', inicializarGestionFlota);
})();
