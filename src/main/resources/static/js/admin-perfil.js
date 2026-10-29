/**
 * admin-perfil.js - Gestión del Perfil Personal y Datos del Administrador
 *
 * =====================================================================================
 * 1. PATRÓN DE MANEJADORES CON NOMBRE (NAMED EVENT HANDLERS):
 *    En lugar de funciones anónimas o código inline en Thymeleaf, cada interacción
 *    (blur, input, change, submit) está asignada a una función con nombre explicativo
 *    dentro de un ámbito IIFE seguro.
 *
 * 2. REGLAS DE VALIDACIÓN EN CLIENTE PARA EL PERFIL:
 *    - DNI/NIE español: regex que valida 8 números + letra o NIE con X/Y/Z inicial.
 *    - Teléfono nacional: regex que admite prefijo internacional (+34 / 0034) y números
 *      iniciados por 6, 7, 8 o 9 con 9 dígitos en total.
 *    - Fecha de nacimiento: cálculo de mayoría de edad (mínimo 18 años, máximo 100).
 *    - Límites de longitud: nombre (50 caracteres), apellidos (100) y dirección (200).
 *
 * 3. RETROALIMENTACIÓN SINCRONIZADA CON HTMX:
 *    Si el backend detecta algún conflicto asíncrono (ej. DNI o email ya en uso),
 *    el evento 'htmx:afterSwap' actualiza los bordes visuales y muestra el mensaje.
 * =====================================================================================
 */
(function () {
    'use strict';

    // Helpers locales con fallback seguro a AdminUI (definido en admin.js)
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
    // VALIDACIONES DE CAMPOS INDIVIDUALES
    // =========================================================================

    /**
     * Valida el formato del DNI o NIE español del administrador.
     * @returns {boolean} - true si el documento es válido.
     */
    function validarDniPerfil() {
        const input = document.getElementById('dni');
        if (!input) return true;

        const valor = input.value ? input.value.trim().toUpperCase() : '';
        if (!valor) {
            mostrarError('dni', 'feedback-dni', 'El DNI / NIE es obligatorio.');
            return false;
        }

        // Formato estándar DNI: 8 dígitos y letra | NIE: X, Y o Z seguido de 7 dígitos y letra
        const regexDni = /^[0-9]{8}[A-Za-z]$|^[XYZxyz][0-9]{7}[A-Za-z]$/;
        if (!regexDni.test(valor)) {
            mostrarError('dni', 'feedback-dni', 'El formato del DNI/NIE no es válido (ej. 12345678Z o X1234567Z).');
            return false;
        }

        const feedback = document.getElementById('feedback-dni');
        if (!feedback || !feedback.querySelector('.mensaje-error-campo')) {
            limpiarError('dni', 'feedback-dni');
        }
        return true;
    }

    /**
     * Valida que la fecha de nacimiento certifique la mayoría de edad (al menos 18 años).
     * @returns {boolean} - true si la fecha es válida.
     */
    function validarFechaNacimientoPerfil() {
        const input = document.getElementById('fechaNacimiento');
        if (!input) return true;

        const valor = input.value ? input.value.trim() : '';
        if (!valor) {
            mostrarError('fechaNacimiento', 'feedback-fechaNacimiento', 'La fecha de nacimiento es obligatoria.');
            return false;
        }

        const partes = valor.split('-');
        if (partes.length !== 3) {
            mostrarError('fechaNacimiento', 'feedback-fechaNacimiento', 'Formato de fecha inválido.');
            return false;
        }

        const anio = parseInt(partes[0], 10);
        const mes = parseInt(partes[1], 10) - 1;
        const dia = parseInt(partes[2], 10);
        const fechaNac = new Date(anio, mes, dia);

        if (isNaN(fechaNac.getTime())) {
            mostrarError('fechaNacimiento', 'feedback-fechaNacimiento', 'Introduce una fecha de nacimiento válida.');
            return false;
        }

        const hoy = new Date();
        const hoySinHora = new Date(hoy.getFullYear(), hoy.getMonth(), hoy.getDate());

        if (fechaNac > hoySinHora) {
            mostrarError('fechaNacimiento', 'feedback-fechaNacimiento', 'La fecha de nacimiento debe ser una fecha pasada.');
            return false;
        }

        const hace100Anios = new Date(hoy.getFullYear() - 100, hoy.getMonth(), hoy.getDate());
        if (fechaNac < hace100Anios) {
            mostrarError('fechaNacimiento', 'feedback-fechaNacimiento', 'La fecha de nacimiento no puede ser anterior a hace 100 años.');
            return false;
        }

        const fechaMinima18 = new Date(anio + 18, mes, dia);
        if (hoySinHora < fechaMinima18) {
            mostrarError('fechaNacimiento', 'feedback-fechaNacimiento', 'El administrador debe ser mayor de edad (al menos 18 años).');
            return false;
        }

        limpiarError('fechaNacimiento', 'feedback-fechaNacimiento');
        return true;
    }

    /**
     * Valida el nombre del administrador (no vacío y máximo 50 caracteres).
     */
    function validarNombrePerfil() {
        const input = document.getElementById('nombre');
        if (!input) return true;

        const valor = input.value ? input.value.trim() : '';
        if (!valor) {
            mostrarError('nombre', 'feedback-nombre', 'El nombre es obligatorio.');
            return false;
        }
        if (valor.length > 50) {
            mostrarError('nombre', 'feedback-nombre', 'El nombre no puede superar los 50 caracteres.');
            return false;
        }

        limpiarError('nombre', 'feedback-nombre');
        return true;
    }

    /**
     * Valida los apellidos del administrador (no vacío y máximo 100 caracteres).
     */
    function validarApellidosPerfil() {
        const input = document.getElementById('apellidos');
        if (!input) return true;

        const valor = input.value ? input.value.trim() : '';
        if (!valor) {
            mostrarError('apellidos', 'feedback-apellidos', 'Los apellidos son obligatorios.');
            return false;
        }
        if (valor.length > 100) {
            mostrarError('apellidos', 'feedback-apellidos', 'Los apellidos no pueden superar los 100 caracteres.');
            return false;
        }

        limpiarError('apellidos', 'feedback-apellidos');
        return true;
    }

    /**
     * Valida el número de teléfono con formato nacional español.
     */
    function validarTelefonoPerfil() {
        const input = document.getElementById('telefono');
        if (!input) return true;

        const valor = input.value ? input.value.trim() : '';
        if (!valor) {
            mostrarError('telefono', 'feedback-telefono', 'El teléfono es obligatorio.');
            return false;
        }

        // Formato: opcional +34 o 0034, seguido de 9 dígitos empezando por 6, 7, 8 o 9
        const regexTel = /^(\+34|0034)?[6789]\d{8}$/;
        if (!regexTel.test(valor)) {
            mostrarError('telefono', 'feedback-telefono', 'El formato del teléfono no es válido (ej. 600111222).');
            return false;
        }

        const feedback = document.getElementById('feedback-telefono');
        if (!feedback || !feedback.querySelector('.mensaje-error-campo')) {
            limpiarError('telefono', 'feedback-telefono');
        }
        return true;
    }

    /**
     * Valida la dirección postal (máximo 200 caracteres).
     */
    function validarDireccionPerfil() {
        const input = document.getElementById('direccion');
        if (!input) return true;

        const valor = input.value ? input.value.trim() : '';
        if (!valor) {
            mostrarError('direccion', 'feedback-direccion', 'La dirección es obligatoria.');
            return false;
        }
        if (valor.length > 200) {
            mostrarError('direccion', 'feedback-direccion', 'La dirección no puede superar los 200 caracteres.');
            return false;
        }

        limpiarError('direccion', 'feedback-direccion');
        return true;
    }

    // =========================================================================
    // MANEJADORES DE EVENTOS CON NOMBRE
    // =========================================================================
    function manejarBlurDni() { validarDniPerfil(); }
    function manejarInputDni() {
        const input = document.getElementById('dni');
        if (input && input.value.trim()) validarDniPerfil();
    }

    function manejarBlurFechaNacimiento() { validarFechaNacimientoPerfil(); }
    function manejarChangeFechaNacimiento() { validarFechaNacimientoPerfil(); }

    function manejarBlurNombre() { validarNombrePerfil(); }
    function manejarInputNombre() {
        const input = document.getElementById('nombre');
        if (input && input.value.trim()) validarNombrePerfil();
    }

    function manejarBlurApellidos() { validarApellidosPerfil(); }
    function manejarInputApellidos() {
        const input = document.getElementById('apellidos');
        if (input && input.value.trim()) validarApellidosPerfil();
    }

    function manejarBlurTelefono() { validarTelefonoPerfil(); }
    function manejarInputTelefono() {
        const input = document.getElementById('telefono');
        if (input && input.value.trim()) validarTelefonoPerfil();
    }

    function manejarBlurDireccion() { validarDireccionPerfil(); }
    function manejarInputDireccion() {
        const input = document.getElementById('direccion');
        if (input && input.value.trim()) validarDireccionPerfil();
    }

    /**
     * Valida todos los campos antes de permitir el envío del formulario.
     * @param {SubmitEvent} evento - Evento submit.
     */
    function manejarSubmitPerfil(evento) {
        const vDni = validarDniPerfil();
        const vFecha = validarFechaNacimientoPerfil();
        const vNombre = validarNombrePerfil();
        const vApellidos = validarApellidosPerfil();
        const vTel = validarTelefonoPerfil();
        const vDir = validarDireccionPerfil();

        const formPerfil = document.getElementById('form-perfil-admin');
        const feedbackError = formPerfil ? formPerfil.querySelector('.mensaje-error-campo') : null;

        // Si algún campo no pasa la validación, cancelamos el envío y ponemos el foco en el primero con error
        if (!vDni || !vFecha || !vNombre || !vApellidos || !vTel || !vDir || feedbackError) {
            evento.preventDefault();
            const primerInvalido = formPerfil ? formPerfil.querySelector('.border-red-500') : null;
            if (primerInvalido) {
                primerInvalido.focus();
            }
        }
    }

    /**
     * Sincroniza clases visuales de error cuando HTMX devuelve respuestas de validación.
     */
    function manejarHtmxAfterSwapPerfil(evento) {
        const targetId = evento.detail && evento.detail.target ? evento.detail.target.id : '';
        if (targetId && targetId.startsWith('feedback-')) {
            const campoNombre = targetId.replace('feedback-', '');
            const input = document.getElementById(campoNombre);
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
    }

    // =========================================================================
    // PUNTO DE ENTRADA DE INICIALIZACIÓN
    // =========================================================================
    function inicializarPerfilAdmin() {
        const formPerfil = document.getElementById('form-perfil-admin');
        if (!formPerfil) return; // Guard clause

        // Configuración de restricciones de fecha de nacimiento
        const inputFechaNac = document.getElementById('fechaNacimiento');
        if (inputFechaNac) {
            const hoy = new Date();
            const hace18Anios = new Date(hoy.getFullYear() - 18, hoy.getMonth(), hoy.getDate());
            const hace100Anios = new Date(hoy.getFullYear() - 100, hoy.getMonth(), hoy.getDate());
            inputFechaNac.setAttribute('max', formatFecha(hace18Anios));
            inputFechaNac.setAttribute('min', formatFecha(hace100Anios));
            inputFechaNac.addEventListener('blur', manejarBlurFechaNacimiento);
            inputFechaNac.addEventListener('change', manejarChangeFechaNacimiento);
        }

        // Registro de escuchadores para cada campo interactivo
        const inputDni = document.getElementById('dni');
        if (inputDni) {
            inputDni.addEventListener('blur', manejarBlurDni);
            inputDni.addEventListener('input', manejarInputDni);
        }

        const inputNombre = document.getElementById('nombre');
        if (inputNombre) {
            inputNombre.addEventListener('blur', manejarBlurNombre);
            inputNombre.addEventListener('input', manejarInputNombre);
        }

        const inputApellidos = document.getElementById('apellidos');
        if (inputApellidos) {
            inputApellidos.addEventListener('blur', manejarBlurApellidos);
            inputApellidos.addEventListener('input', manejarInputApellidos);
        }

        const inputTelefono = document.getElementById('telefono');
        if (inputTelefono) {
            inputTelefono.addEventListener('blur', manejarBlurTelefono);
            inputTelefono.addEventListener('input', manejarInputTelefono);
        }

        const inputDireccion = document.getElementById('direccion');
        if (inputDireccion) {
            inputDireccion.addEventListener('blur', manejarBlurDireccion);
            inputDireccion.addEventListener('input', manejarInputDireccion);
        }

        formPerfil.addEventListener('submit', manejarSubmitPerfil);
        document.body.addEventListener('htmx:afterSwap', manejarHtmxAfterSwapPerfil);
    }

    document.addEventListener('DOMContentLoaded', inicializarPerfilAdmin);
})();
