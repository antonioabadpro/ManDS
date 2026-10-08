/**
 * profesor-perfil.js
 * JavaScript no intrusivo y modular para la vista "Mi Perfil" del Profesor en ManDS ERP.
 * Implementa el Patrón de Manejadores de Eventos con Nombre y Punto de Entrada de Inicialización.
 *
 * Responsabilidades:
 * 1. Restricciones dinámicas de fecha de nacimiento (18 a 100 años respecto a la fecha actual).
 * 2. Validación en cliente en tiempo real de nombre, apellidos, teléfono nacional y dirección.
 * 3. Prevención de envío ante errores y sincronización con validaciones asíncronas de HTMX.
 */
(function () {
    'use strict';

    function formatearFechaISO(fecha) {
        if (!(fecha instanceof Date) || isNaN(fecha.getTime())) return '';
        const a = fecha.getFullYear();
        const m = String(fecha.getMonth() + 1).padStart(2, '0');
        const d = String(fecha.getDate()).padStart(2, '0');
        return `${a}-${m}-${d}`;
    }

    function mostrarError(inputId, feedbackId, mensaje) {
        const input = document.getElementById(inputId);
        const feedback = document.getElementById(feedbackId);

        if (input) {
            input.classList.add('border-red-500', 'focus:ring-red-500', 'dark:border-red-500');
            input.classList.remove('border-slate-300', 'dark:border-slate-700', 'focus:ring-blue-600');
            input.setCustomValidity(mensaje);
        }

        if (feedback) {
            feedback.innerHTML = `
                <div class="mensaje-error-campo flex items-center space-x-1.5 text-xs text-red-600 dark:text-red-400 mt-1.5 font-medium fade-in">
                    <svg class="w-3.5 h-3.5 shrink-0 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                    </svg>
                    <span>${mensaje}</span>
                </div>
            `;
        }
    }

    function limpiarError(inputId, feedbackId) {
        const input = document.getElementById(inputId);
        const feedback = document.getElementById(feedbackId);

        if (input) {
            input.classList.remove('border-red-500', 'focus:ring-red-500', 'dark:border-red-500');
            input.classList.add('border-slate-300', 'dark:border-slate-700', 'focus:ring-blue-600');
            input.setCustomValidity('');
        }

        if (feedback) {
            feedback.innerHTML = '';
        }
    }

    // =========================================================================
    // VALIDACIONES DE CAMPOS INDIVIDUALES
    // =========================================================================
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
            mostrarError('fechaNacimiento', 'feedback-fechaNacimiento', 'El profesor debe ser mayor de edad (al menos 18 años).');
            return false;
        }

        limpiarError('fechaNacimiento', 'feedback-fechaNacimiento');
        return true;
    }

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

    function validarTelefonoPerfil() {
        const input = document.getElementById('telefono');
        if (!input) return true;

        const valor = input.value ? input.value.trim() : '';
        if (!valor) {
            mostrarError('telefono', 'feedback-telefono', 'El teléfono es obligatorio.');
            return false;
        }

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

    function manejarSubmitPerfil(evento) {
        const vFecha = validarFechaNacimientoPerfil();
        const vNombre = validarNombrePerfil();
        const vApellidos = validarApellidosPerfil();
        const vTel = validarTelefonoPerfil();
        const vDir = validarDireccionPerfil();

        const formPerfil = document.getElementById('form-perfil-profesor');
        const feedbackError = formPerfil ? formPerfil.querySelector('.mensaje-error-campo') : null;

        if (!vFecha || !vNombre || !vApellidos || !vTel || !vDir || feedbackError) {
            evento.preventDefault();
            const primerInvalido = formPerfil ? formPerfil.querySelector('.border-red-500') : null;
            if (primerInvalido) {
                primerInvalido.focus();
            }
        }
    }

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
    function inicializarPerfilProfesor() {
        const formPerfil = document.getElementById('form-perfil-profesor');
        if (!formPerfil) return; // Solo ejecutar en la vista de perfil del profesor

        const inputFechaNac = document.getElementById('fechaNacimiento');
        if (inputFechaNac) {
            const hoy = new Date();
            const hace18Anios = new Date(hoy.getFullYear() - 18, hoy.getMonth(), hoy.getDate());
            const hace100Anios = new Date(hoy.getFullYear() - 100, hoy.getMonth(), hoy.getDate());
            inputFechaNac.setAttribute('max', formatearFechaISO(hace18Anios));
            inputFechaNac.setAttribute('min', formatearFechaISO(hace100Anios));
            inputFechaNac.addEventListener('blur', manejarBlurFechaNacimiento);
            inputFechaNac.addEventListener('change', manejarChangeFechaNacimiento);
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

    document.addEventListener('DOMContentLoaded', inicializarPerfilProfesor);
})();
