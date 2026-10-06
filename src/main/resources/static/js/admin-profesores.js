/**
 * admin-profesores.js
 * JavaScript no intrusivo y modular para la Gestión de Profesores en ManDS ERP.
 * Implementa el Patrón de Manejadores de Eventos con Nombre y Punto de Entrada de Inicialización.
 *
 * Responsabilidades:
 * 1. Restricciones dinámicas de fechas en calendarios nativos (nacimiento 18-100 años, contratación +-1 mes).
 * 2. Validación de campos en tiempo real (nombre, apellidos, DNI/NIE, teléfono, correo, dirección).
 * 3. Validación de permisos obligatorios y compatibilidad carnet-vehículo (Regla 7.3).
 * 4. Interceptación y sincronización con el ciclo de vida de peticiones HTMX.
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
    // CONFIGURACIÓN DE RESTRICCIONES DE FECHAS
    // =========================================================================
    function configurarRestriccionesFechasProfesor(sufijo = '') {
        const inputNacimiento = document.getElementById(`profesor-nacimiento${sufijo}`);
        const inputContratacion = document.getElementById(`profesor-fecha${sufijo}`);
        const hoy = new Date();

        if (inputNacimiento) {
            const hace18Anios = new Date(hoy.getFullYear() - 18, hoy.getMonth(), hoy.getDate());
            const hace100Anios = new Date(hoy.getFullYear() - 100, hoy.getMonth(), hoy.getDate());
            inputNacimiento.setAttribute('max', formatFecha(hace18Anios));
            inputNacimiento.setAttribute('min', formatFecha(hace100Anios));
        }

        if (inputContratacion && sufijo !== '-editar') {
            const hace1Mes = new Date(hoy.getFullYear(), hoy.getMonth() - 1, hoy.getDate());
            const en1Mes = new Date(hoy.getFullYear(), hoy.getMonth() + 1, hoy.getDate());
            inputContratacion.setAttribute('min', formatFecha(hace1Mes));
            inputContratacion.setAttribute('max', formatFecha(en1Mes));
        }
    }

    // =========================================================================
    // VALIDACIONES DE CAMPOS INDIVIDUALES
    // =========================================================================
    function validarNombreProfesor(sufijo = '') {
        const inputId = `profesor-nombre${sufijo}`;
        const feedbackId = `feedback-nombre${sufijo}`;
        const input = document.getElementById(inputId);
        if (!input) return true;

        const valor = input.value ? input.value.trim() : '';
        if (!valor) {
            mostrarError(inputId, feedbackId, 'El nombre es obligatorio.');
            return false;
        }
        if (valor.length > 50) {
            mostrarError(inputId, feedbackId, 'El nombre no puede superar los 50 caracteres.');
            return false;
        }
        limpiarError(inputId, feedbackId);
        return true;
    }

    function validarApellidosProfesor(sufijo = '') {
        const inputId = `profesor-apellidos${sufijo}`;
        const feedbackId = `feedback-apellidos${sufijo}`;
        const input = document.getElementById(inputId);
        if (!input) return true;

        const valor = input.value ? input.value.trim() : '';
        if (!valor) {
            mostrarError(inputId, feedbackId, 'Los apellidos son obligatorios.');
            return false;
        }
        if (valor.length > 100) {
            mostrarError(inputId, feedbackId, 'Los apellidos no pueden superar los 100 caracteres.');
            return false;
        }
        limpiarError(inputId, feedbackId);
        return true;
    }

    function validarDniProfesor(sufijo = '') {
        const inputId = `profesor-dni${sufijo}`;
        const feedbackId = `feedback-dni${sufijo}`;
        const input = document.getElementById(inputId);
        if (!input) return true;

        const valor = input.value ? input.value.trim().toUpperCase() : '';
        if (!valor) {
            mostrarError(inputId, feedbackId, 'El DNI / NIE es obligatorio.');
            return false;
        }
        const regexDni = /^[0-9]{8}[A-Za-z]$|^[XYZxyz][0-9]{7}[A-Za-z]$/;
        if (!regexDni.test(valor)) {
            mostrarError(inputId, feedbackId, 'El formato del DNI/NIE no es válido (ej. 12345678Z o X1234567Z).');
            return false;
        }
        const feedback = document.getElementById(feedbackId);
        if (!feedback || !feedback.querySelector('.mensaje-error-campo')) {
            limpiarError(inputId, feedbackId);
        }
        return true;
    }

    function validarTelefonoProfesor(sufijo = '') {
        const inputId = `profesor-telefono${sufijo}`;
        const feedbackId = `feedback-telefono${sufijo}`;
        const input = document.getElementById(inputId);
        if (!input) return true;

        const valor = input.value ? input.value.trim() : '';
        if (!valor) {
            mostrarError(inputId, feedbackId, 'El teléfono móvil es obligatorio.');
            return false;
        }
        const regexTel = /^(\+34|0034)?[6789]\d{8}$/;
        if (!regexTel.test(valor)) {
            mostrarError(inputId, feedbackId, 'El formato del teléfono no es válido (ej. 600123456).');
            return false;
        }
        const feedback = document.getElementById(feedbackId);
        if (!feedback || !feedback.querySelector('.mensaje-error-campo')) {
            limpiarError(inputId, feedbackId);
        }
        return true;
    }

    function validarCorreoProfesor(sufijo = '') {
        const inputId = `profesor-correo${sufijo}`;
        const feedbackId = `feedback-correo${sufijo}`;
        const input = document.getElementById(inputId);
        if (!input) return true;

        const valor = input.value ? input.value.trim() : '';
        if (!valor) {
            mostrarError(inputId, feedbackId, 'El correo electrónico es obligatorio.');
            return false;
        }
        const regexCorreo = /^[A-Za-z0-9+_.-]+@(.+)$/;
        if (!regexCorreo.test(valor)) {
            mostrarError(inputId, feedbackId, 'El formato del correo electrónico no es válido.');
            return false;
        }
        if (valor.length > 50) {
            mostrarError(inputId, feedbackId, 'El correo no puede superar los 50 caracteres.');
            return false;
        }
        const feedback = document.getElementById(feedbackId);
        if (!feedback || !feedback.querySelector('.mensaje-error-campo')) {
            limpiarError(inputId, feedbackId);
        }
        return true;
    }

    function validarDireccionProfesor(sufijo = '') {
        const inputId = `profesor-direccion${sufijo}`;
        const feedbackId = `feedback-direccion${sufijo}`;
        const input = document.getElementById(inputId);
        if (!input) return true;

        const valor = input.value ? input.value.trim() : '';
        if (!valor) {
            mostrarError(inputId, feedbackId, 'La dirección de residencia es obligatoria.');
            return false;
        }
        if (valor.length > 150) {
            mostrarError(inputId, feedbackId, 'La dirección no puede superar los 150 caracteres.');
            return false;
        }
        limpiarError(inputId, feedbackId);
        return true;
    }

    function validarFechaNacimientoProfesor(sufijo = '') {
        const inputId = `profesor-nacimiento${sufijo}`;
        const feedbackId = `feedback-nacimiento${sufijo}`;
        const input = document.getElementById(inputId);
        if (!input) return true;

        const valor = input.value ? input.value.trim() : '';
        if (!valor) {
            mostrarError(inputId, feedbackId, 'La fecha de nacimiento es obligatoria.');
            return false;
        }

        const partes = valor.split('-');
        if (partes.length !== 3) {
            mostrarError(inputId, feedbackId, 'Formato de fecha inválido.');
            return false;
        }

        const anio = parseInt(partes[0], 10);
        const mes = parseInt(partes[1], 10) - 1;
        const dia = parseInt(partes[2], 10);
        const fechaNac = new Date(anio, mes, dia);

        if (isNaN(fechaNac.getTime())) {
            mostrarError(inputId, feedbackId, 'Introduce una fecha de nacimiento válida.');
            return false;
        }

        const hoy = new Date();
        const hoySinHora = new Date(hoy.getFullYear(), hoy.getMonth(), hoy.getDate());

        if (fechaNac > hoySinHora) {
            mostrarError(inputId, feedbackId, 'La fecha de nacimiento debe ser una fecha pasada.');
            return false;
        }

        const hace100Anios = new Date(hoy.getFullYear() - 100, hoy.getMonth(), hoy.getDate());
        if (fechaNac < hace100Anios) {
            mostrarError(inputId, feedbackId, 'La fecha de nacimiento no puede ser anterior a hace 100 años.');
            return false;
        }

        const fechaMinima18 = new Date(anio + 18, mes, dia);
        if (hoySinHora < fechaMinima18) {
            mostrarError(inputId, feedbackId, 'El profesor debe ser mayor de edad (al menos 18 años).');
            return false;
        }

        limpiarError(inputId, feedbackId);
        return true;
    }

    function validarFechaContratacionProfesor(sufijo = '') {
        const inputId = `profesor-fecha${sufijo}`;
        const feedbackId = `feedback-fecha-contratacion${sufijo}`;
        const input = document.getElementById(inputId);
        if (!input) return true;

        const valor = input.value ? input.value.trim() : '';
        if (!valor) {
            mostrarError(inputId, feedbackId, 'La fecha de contratación es obligatoria.');
            return false;
        }

        const partes = valor.split('-');
        if (partes.length !== 3) {
            mostrarError(inputId, feedbackId, 'Formato de fecha inválido.');
            return false;
        }

        const fechaContrato = new Date(parseInt(partes[0], 10), parseInt(partes[1], 10) - 1, parseInt(partes[2], 10));
        if (isNaN(fechaContrato.getTime())) {
            mostrarError(inputId, feedbackId, 'Introduce una fecha de contratación válida.');
            return false;
        }

        const hoy = new Date();
        const hace1Mes = new Date(hoy.getFullYear(), hoy.getMonth() - 1, hoy.getDate());
        const en1Mes = new Date(hoy.getFullYear(), hoy.getMonth() + 1, hoy.getDate());

        if (fechaContrato < hace1Mes || fechaContrato > en1Mes) {
            mostrarError(inputId, feedbackId, 'La fecha de contratación debe estar comprendida entre 1 mes antes y 1 mes después de la fecha actual.');
            return false;
        }

        limpiarError(inputId, feedbackId);
        return true;
    }

    function validarPermisosProfesor(modalId = 'modal-alta-profesor', sufijo = '') {
        const modal = document.getElementById(modalId);
        if (!modal) return true;

        const checkboxes = modal.querySelectorAll('input[name="permisos"]:checked');
        const feedbackId = `feedback-permisos${sufijo}`;
        const feedback = document.getElementById(feedbackId);

        if (checkboxes.length === 0) {
            if (feedback) {
                feedback.innerHTML = `
                    <div class="mensaje-error-campo flex items-center space-x-1.5 text-xs text-red-600 dark:text-red-400 mt-1 font-medium fade-in">
                        <svg class="w-3.5 h-3.5 shrink-0 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                        </svg>
                        <span>Debe seleccionar al menos un permiso de conducción autorizado.</span>
                    </div>
                `;
            }
            return false;
        }

        if (feedback) {
            feedback.innerHTML = '';
        }
        return true;
    }

    function validarCompatibilidadVehiculo(modalId = 'modal-alta-profesor', sufijo = '') {
        const selectId = `profesor-vehiculo${sufijo}`;
        const feedbackId = `feedback-vehiculo-compatibilidad${sufijo}`;
        const selectVehiculo = document.getElementById(selectId);
        const feedback = document.getElementById(feedbackId);
        if (!selectVehiculo || selectVehiculo.disabled) {
            if (feedback) feedback.innerHTML = '';
            return true;
        }

        const selectedOption = selectVehiculo.options[selectVehiculo.selectedIndex];
        const tipoVehiculo = selectedOption ? selectedOption.getAttribute('data-tipo') : null;
        const nombreTipo = selectedOption ? (selectedOption.getAttribute('data-tipo-nombre') || tipoVehiculo) : '';

        // Si no hay vehículo seleccionado ("-- Sin Vehículo Asignado --"), es válido
        if (!tipoVehiculo || selectVehiculo.value === '') {
            if (feedback) feedback.innerHTML = '';
            selectVehiculo.classList.remove('border-red-500', 'focus:ring-red-500', 'dark:border-red-500');
            selectVehiculo.classList.add('border-slate-300', 'dark:border-slate-700', 'focus:ring-blue-600');
            selectVehiculo.setCustomValidity('');
            return true;
        }

        // Comprobar si entre los permisos marcados está el requerido
        const modal = document.getElementById(modalId);
        const checkboxCarnet = modal ? modal.querySelector(`input[name="permisos"][value="${tipoVehiculo}"]`) : null;
        const estaMarcado = checkboxCarnet && checkboxCarnet.checked;

        if (!estaMarcado) {
            const mensaje = `El vehículo seleccionado requiere ${nombreTipo}, pero este permiso no está seleccionado en los permisos autorizados.`;
            if (feedback) {
                feedback.innerHTML = `
                    <div class="mensaje-error-campo flex items-start space-x-1.5 text-xs text-red-600 dark:text-red-400 mt-1.5 font-medium fade-in">
                        <svg class="w-3.5 h-3.5 shrink-0 text-red-500 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                        </svg>
                        <span>${mensaje}</span>
                    </div>
                `;
            }
            selectVehiculo.classList.add('border-red-500', 'focus:ring-red-500', 'dark:border-red-500');
            selectVehiculo.classList.remove('border-slate-300', 'dark:border-slate-700', 'focus:ring-blue-600');
            selectVehiculo.setCustomValidity(mensaje);
            return false;
        }

        if (feedback) {
            feedback.innerHTML = '';
        }
        selectVehiculo.classList.remove('border-red-500', 'focus:ring-red-500', 'dark:border-red-500');
        selectVehiculo.classList.add('border-slate-300', 'dark:border-slate-700', 'focus:ring-blue-600');
        selectVehiculo.setCustomValidity('');
        return true;
    }

    function validarFormularioProfesor(formId, modalId, sufijo = '') {
        const form = document.getElementById(formId);
        if (!form) return true;

        const vNombre = validarNombreProfesor(sufijo);
        const vApellidos = validarApellidosProfesor(sufijo);
        const vDni = validarDniProfesor(sufijo);
        const vTelefono = validarTelefonoProfesor(sufijo);
        const vCorreo = validarCorreoProfesor(sufijo);
        const vNacimiento = validarFechaNacimientoProfesor(sufijo);
        const vDireccion = validarDireccionProfesor(sufijo);
        const vContratacion = sufijo === '-editar' ? true : validarFechaContratacionProfesor(sufijo);
        const vPermisos = validarPermisosProfesor(modalId, sufijo);
        const vVehiculo = validarCompatibilidadVehiculo(modalId, sufijo);

        const errorActivo = form.querySelector('.mensaje-error-campo');

        if (!vNombre || !vApellidos || !vDni || !vTelefono || !vCorreo || !vNacimiento || !vDireccion || !vContratacion || !vPermisos || !vVehiculo || errorActivo) {
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

    function manejarBlurNombreAlta() { validarNombreProfesor(''); }
    function manejarInputNombreAlta() {
        const el = document.getElementById('profesor-nombre');
        if (el && (el.classList.contains('border-red-500') || el.value.trim())) validarNombreProfesor('');
    }
    function manejarBlurNombreEdicion() { validarNombreProfesor('-editar'); }
    function manejarInputNombreEdicion() {
        const el = document.getElementById('profesor-nombre-editar');
        if (el && (el.classList.contains('border-red-500') || el.value.trim())) validarNombreProfesor('-editar');
    }

    function manejarBlurApellidosAlta() { validarApellidosProfesor(''); }
    function manejarInputApellidosAlta() {
        const el = document.getElementById('profesor-apellidos');
        if (el && (el.classList.contains('border-red-500') || el.value.trim())) validarApellidosProfesor('');
    }
    function manejarBlurApellidosEdicion() { validarApellidosProfesor('-editar'); }
    function manejarInputApellidosEdicion() {
        const el = document.getElementById('profesor-apellidos-editar');
        if (el && (el.classList.contains('border-red-500') || el.value.trim())) validarApellidosProfesor('-editar');
    }

    function manejarBlurDniAlta() { validarDniProfesor(''); }
    function manejarInputDniAlta() {
        const el = document.getElementById('profesor-dni');
        if (el && (el.classList.contains('border-red-500') || el.value.trim())) validarDniProfesor('');
    }
    function manejarBlurDniEdicion() { validarDniProfesor('-editar'); }
    function manejarInputDniEdicion() {
        const el = document.getElementById('profesor-dni-editar');
        if (el && (el.classList.contains('border-red-500') || el.value.trim())) validarDniProfesor('-editar');
    }

    function manejarBlurTelefonoAlta() { validarTelefonoProfesor(''); }
    function manejarInputTelefonoAlta() {
        const el = document.getElementById('profesor-telefono');
        if (el && (el.classList.contains('border-red-500') || el.value.trim())) validarTelefonoProfesor('');
    }
    function manejarBlurTelefonoEdicion() { validarTelefonoProfesor('-editar'); }
    function manejarInputTelefonoEdicion() {
        const el = document.getElementById('profesor-telefono-editar');
        if (el && (el.classList.contains('border-red-500') || el.value.trim())) validarTelefonoProfesor('-editar');
    }

    function manejarBlurCorreoAlta() { validarCorreoProfesor(''); }
    function manejarInputCorreoAlta() {
        const el = document.getElementById('profesor-correo');
        if (el && (el.classList.contains('border-red-500') || el.value.trim())) validarCorreoProfesor('');
    }
    function manejarBlurCorreoEdicion() { validarCorreoProfesor('-editar'); }
    function manejarInputCorreoEdicion() {
        const el = document.getElementById('profesor-correo-editar');
        if (el && (el.classList.contains('border-red-500') || el.value.trim())) validarCorreoProfesor('-editar');
    }

    function manejarBlurDireccionAlta() { validarDireccionProfesor(''); }
    function manejarInputDireccionAlta() {
        const el = document.getElementById('profesor-direccion');
        if (el && (el.classList.contains('border-red-500') || el.value.trim())) validarDireccionProfesor('');
    }
    function manejarBlurDireccionEdicion() { validarDireccionProfesor('-editar'); }
    function manejarInputDireccionEdicion() {
        const el = document.getElementById('profesor-direccion-editar');
        if (el && (el.classList.contains('border-red-500') || el.value.trim())) validarDireccionProfesor('-editar');
    }

    function manejarChangeFechaNacimientoAlta() { validarFechaNacimientoProfesor(''); }
    function manejarBlurFechaNacimientoAlta() { validarFechaNacimientoProfesor(''); }
    function manejarChangeFechaNacimientoEdicion() { validarFechaNacimientoProfesor('-editar'); }
    function manejarBlurFechaNacimientoEdicion() { validarFechaNacimientoProfesor('-editar'); }

    function manejarChangeFechaContratacionAlta() { validarFechaContratacionProfesor(''); }
    function manejarBlurFechaContratacionAlta() { validarFechaContratacionProfesor(''); }

    function manejarChangeVehiculoAlta() { validarCompatibilidadVehiculo('modal-alta-profesor', ''); }
    function manejarChangeVehiculoEdicion() { validarCompatibilidadVehiculo('modal-editar-profesor', '-editar'); }

    function manejarChangePermisosModalAlta(evento) {
        if (evento.target && evento.target.name === 'permisos') {
            validarPermisosProfesor('modal-alta-profesor', '');
            validarCompatibilidadVehiculo('modal-alta-profesor', '');
        }
    }

    function manejarChangePermisosModalEdicion(evento) {
        if (evento.target && evento.target.name === 'permisos') {
            validarPermisosProfesor('modal-editar-profesor', '-editar');
            validarCompatibilidadVehiculo('modal-editar-profesor', '-editar');
        }
    }

    function manejarSubmitFormAltaProfesor(evento) {
        if (!validarFormularioProfesor('form-alta-profesor', 'modal-alta-profesor', '')) {
            evento.preventDefault();
            evento.stopImmediatePropagation();
        }
    }

    function manejarSubmitFormEditarProfesor(evento) {
        if (!validarFormularioProfesor('form-editar-profesor', 'modal-editar-profesor', '-editar')) {
            evento.preventDefault();
            evento.stopImmediatePropagation();
        }
    }

    /**
     * Intercepta evento de confirmación HTMX para abortar peticiones no válidas.
     */
    function manejarHtmxConfirmProfesor(evento) {
        const formId = evento.target ? evento.target.id : '';
        if (formId === 'form-alta-profesor') {
            if (!validarFormularioProfesor('form-alta-profesor', 'modal-alta-profesor', '')) {
                evento.preventDefault();
            }
        } else if (formId === 'form-editar-profesor') {
            if (!validarFormularioProfesor('form-editar-profesor', 'modal-editar-profesor', '-editar')) {
                evento.preventDefault();
            }
        }
    }

    /**
     * Sincroniza clases visuales y reinicializa modales re-renderizados tras intercambios HTMX.
     */
    function manejarHtmxAfterSwapProfesor(evento) {
        const targetId = evento.detail && evento.detail.target ? evento.detail.target.id : '';

        // Sincronizar feedback de campos individuales
        if (targetId && targetId.startsWith('feedback-')) {
            const campoNombre = targetId.replace('feedback-', '');
            const input = document.getElementById(`profesor-${campoNombre}`);
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
        if (targetId === 'contenedor-modal-alta-profesor' || targetId === 'modal-alta-profesor') {
            vincularEventosModalAltaProfesor();
        }
        if (targetId === 'contenedor-modal-editar-profesor' || targetId === 'modal-editar-profesor') {
            vincularEventosModalEditarProfesor();
        }
    }

    // =========================================================================
    // VINCULACIÓN DE EVENTOS POR MODAL
    // =========================================================================
    function vincularEventosModalAltaProfesor() {
        const form = document.getElementById('form-alta-profesor');
        const modal = document.getElementById('modal-alta-profesor');
        if (!form && !modal) return;

        configurarRestriccionesFechasProfesor('');

        if (form && !form._eventosProfesorInicializados) {
            form._eventosProfesorInicializados = true;

            const inputNombre = document.getElementById('profesor-nombre');
            if (inputNombre) {
                inputNombre.addEventListener('blur', manejarBlurNombreAlta);
                inputNombre.addEventListener('input', manejarInputNombreAlta);
            }

            const inputApellidos = document.getElementById('profesor-apellidos');
            if (inputApellidos) {
                inputApellidos.addEventListener('blur', manejarBlurApellidosAlta);
                inputApellidos.addEventListener('input', manejarInputApellidosAlta);
            }

            const inputDni = document.getElementById('profesor-dni');
            if (inputDni) {
                inputDni.addEventListener('blur', manejarBlurDniAlta);
                inputDni.addEventListener('input', manejarInputDniAlta);
            }

            const inputTelefono = document.getElementById('profesor-telefono');
            if (inputTelefono) {
                inputTelefono.addEventListener('blur', manejarBlurTelefonoAlta);
                inputTelefono.addEventListener('input', manejarInputTelefonoAlta);
            }

            const inputCorreo = document.getElementById('profesor-correo');
            if (inputCorreo) {
                inputCorreo.addEventListener('blur', manejarBlurCorreoAlta);
                inputCorreo.addEventListener('input', manejarInputCorreoAlta);
            }

            const inputDireccion = document.getElementById('profesor-direccion');
            if (inputDireccion) {
                inputDireccion.addEventListener('blur', manejarBlurDireccionAlta);
                inputDireccion.addEventListener('input', manejarInputDireccionAlta);
            }

            const inputNacimiento = document.getElementById('profesor-nacimiento');
            if (inputNacimiento) {
                inputNacimiento.addEventListener('change', manejarChangeFechaNacimientoAlta);
                inputNacimiento.addEventListener('blur', manejarBlurFechaNacimientoAlta);
            }

            const inputContratacion = document.getElementById('profesor-fecha');
            if (inputContratacion) {
                inputContratacion.addEventListener('change', manejarChangeFechaContratacionAlta);
                inputContratacion.addEventListener('blur', manejarBlurFechaContratacionAlta);
            }

            const selectVehiculo = document.getElementById('profesor-vehiculo');
            if (selectVehiculo) {
                selectVehiculo.addEventListener('change', manejarChangeVehiculoAlta);
            }

            form.addEventListener('submit', manejarSubmitFormAltaProfesor);
        }

        if (modal && !modal._eventosPermisosInicializados) {
            modal._eventosPermisosInicializados = true;
            modal.addEventListener('change', manejarChangePermisosModalAlta);
        }

        validarCompatibilidadVehiculo('modal-alta-profesor', '');
    }

    function vincularEventosModalEditarProfesor() {
        const form = document.getElementById('form-editar-profesor');
        const modal = document.getElementById('modal-editar-profesor');
        if (!form && !modal) return;

        configurarRestriccionesFechasProfesor('-editar');

        if (form && !form._eventosProfesorInicializados) {
            form._eventosProfesorInicializados = true;

            const inputNombre = document.getElementById('profesor-nombre-editar');
            if (inputNombre) {
                inputNombre.addEventListener('blur', manejarBlurNombreEdicion);
                inputNombre.addEventListener('input', manejarInputNombreEdicion);
            }

            const inputApellidos = document.getElementById('profesor-apellidos-editar');
            if (inputApellidos) {
                inputApellidos.addEventListener('blur', manejarBlurApellidosEdicion);
                inputApellidos.addEventListener('input', manejarInputApellidosEdicion);
            }

            const inputDni = document.getElementById('profesor-dni-editar');
            if (inputDni) {
                inputDni.addEventListener('blur', manejarBlurDniEdicion);
                inputDni.addEventListener('input', manejarInputDniEdicion);
            }

            const inputTelefono = document.getElementById('profesor-telefono-editar');
            if (inputTelefono) {
                inputTelefono.addEventListener('blur', manejarBlurTelefonoEdicion);
                inputTelefono.addEventListener('input', manejarInputTelefonoEdicion);
            }

            const inputCorreo = document.getElementById('profesor-correo-editar');
            if (inputCorreo) {
                inputCorreo.addEventListener('blur', manejarBlurCorreoEdicion);
                inputCorreo.addEventListener('input', manejarInputCorreoEdicion);
            }

            const inputDireccion = document.getElementById('profesor-direccion-editar');
            if (inputDireccion) {
                inputDireccion.addEventListener('blur', manejarBlurDireccionEdicion);
                inputDireccion.addEventListener('input', manejarInputDireccionEdicion);
            }

            const inputNacimiento = document.getElementById('profesor-nacimiento-editar');
            if (inputNacimiento) {
                inputNacimiento.addEventListener('change', manejarChangeFechaNacimientoEdicion);
                inputNacimiento.addEventListener('blur', manejarBlurFechaNacimientoEdicion);
            }

            const selectVehiculo = document.getElementById('profesor-vehiculo-editar');
            if (selectVehiculo) {
                selectVehiculo.addEventListener('change', manejarChangeVehiculoEdicion);
            }

            form.addEventListener('submit', manejarSubmitFormEditarProfesor);
        }

        if (modal && !modal._eventosPermisosInicializados) {
            modal._eventosPermisosInicializados = true;
            modal.addEventListener('change', manejarChangePermisosModalEdicion);
        }

        validarCompatibilidadVehiculo('modal-editar-profesor', '-editar');
    }

    // =========================================================================
    // PUNTO DE ENTRADA DE INICIALIZACIÓN
    // =========================================================================
    function inicializarGestionProfesores() {
        // Inicializar modales presentes en el DOM estático
        vincularEventosModalAltaProfesor();
        vincularEventosModalEditarProfesor();

        // Escuchadores globales HTMX para profesores
        document.body.addEventListener('htmx:confirm', manejarHtmxConfirmProfesor);
        document.body.addEventListener('htmx:afterSwap', manejarHtmxAfterSwapProfesor);
    }

    document.addEventListener('DOMContentLoaded', inicializarGestionProfesores);
})();

