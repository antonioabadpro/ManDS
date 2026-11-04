/**
 * recuperar-password.js - Recuperación y Restablecimiento Seguro de Contraseña para ManDS
 *
 * =====================================================================================
 * 1. CIRCUITO DE RECUPERACIÓN:
 *    - Paso A: El usuario introduce su correo electrónico y el servidor genera un token
 *      criptográfico efímero con validez estricta de 15 minutos ('TokenVerificacionService').
 *    - Paso B: El usuario pulsa el enlace del correo y accede a la vista de restablecimiento,
 *      donde define su nueva contraseña.
 *
 * 2. REQUISITOS DE ROBUSTEZ Y ASISTENCIA VISUAL:
 *    - Exige una longitud mínima de 6 caracteres (para cuentas restablecidas).
 *    - Comprueba la inclusión de al menos un número y una letra mayúscula.
 *    - Los iconos y colores de la lista de requisitos cambian en vivo mientras el usuario teclea.
 *
 * 3. VALIDACIÓN PREVENTIVA SIN RECARGA:
 *    Evita enviar el formulario al backend si el correo no tiene formato válido o si
 *    las contraseñas no son idénticas, ahorrando peticiones HTTP innecesarias.
 * =====================================================================================
 */

/**
 * Alterna la visibilidad de un campo de contraseña entre 'password' y 'text'.
 * @param {string} inputId - ID del input de contraseña en el DOM.
 * @param {string} iconId - ID del icono SVG del botón para actualizar su geometría.
 * @param {string} buttonId - ID del botón que ejecuta la alternancia para atributos ARIA.
 */
function togglePasswordVisibility(inputId, iconId, buttonId) {
    const input = document.getElementById(inputId);
    const icon = document.getElementById(iconId);
    const button = document.getElementById(buttonId);

    if (!input) return;

    const esPassword = input.type === 'password';
    input.type = esPassword ? 'text' : 'password';

    // Soporte accesible WAI-ARIA
    if (button) {
        button.setAttribute('aria-label', esPassword ? 'Ocultar contraseña' : 'Mostrar contraseña');
        button.setAttribute('aria-pressed', String(esPassword));
    }

    if (icon) {
        if (esPassword) {
            // Icono de ojo tachado (indica que la clave se muestra en texto plano)
            icon.innerHTML = `
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3.98 8.223A10.477 10.477 0 001.934 12C3.226 16.338 7.244 19.5 12 19.5c.993 0 1.953-.138 2.863-.395M6.228 6.228A10.45 10.45 0 0112 4.5c4.756 0 8.773 3.162 10.065 7.498a10.523 10.523 0 01-4.293 5.774M6.228 6.228L3 3m3.228 3.228l3.65 3.65m7.894 7.894L21 21m-3.228-3.228l-3.65-3.65m0 0a3 3 0 10-4.243-4.243m4.242 4.242L9.88 9.88" />
            `;
        } else {
            // Icono de ojo normal (indica que la clave está oculta con puntos)
            icon.innerHTML = `
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z" />
            `;
        }
    }
}

/**
 * Valida en tiempo real los requisitos de seguridad de la nueva contraseña.
 */
function inicializarValidadorSeguridad() {
    const passInput = document.getElementById('nueva-password');
    const confirmInput = document.getElementById('confirmar-password');
    const reqLength = document.getElementById('req-length');
    const reqNumber = document.getElementById('req-number');
    const reqUpper = document.getElementById('req-upper');

    if (!passInput) return;

    // Escucha cada pulsación del usuario
    passInput.addEventListener('input', () => {
        const val = passInput.value;

        // Requisito: Mínimo 6 caracteres para restablecimiento de seguridad
        if (reqLength) {
            actualizarIndicadorRequisito(reqLength, val.length >= 6);
        }

        // Requisito: Al menos un número (\d)
        if (reqNumber) {
            actualizarIndicadorRequisito(reqNumber, /\d/.test(val));
        }

        // Requisito: Al menos una letra mayúscula ([A-Z])
        if (reqUpper) {
            actualizarIndicadorRequisito(reqUpper, /[A-Z]/.test(val));
        }

        // Comprobación de coincidencia si el usuario ya escribió en el campo de confirmación
        if (confirmInput && confirmInput.value) {
            validarCoincidenciaPasswords();
        }
    });

    if (confirmInput) {
        confirmInput.addEventListener('input', validarCoincidenciaPasswords);
    }
}

/**
 * Actualiza el icono y color de un requisito de contraseña en el DOM.
 * @param {HTMLElement} elemento - Elemento contenedor del requisito.
 * @param {boolean} cumplido - Indica si la condición se cumple con el valor actual.
 */
function actualizarIndicadorRequisito(elemento, cumplido) {
    const icono = elemento.querySelector('svg');

    if (cumplido) {
        // Estado satisfecho: verde esmeralda con check
        elemento.classList.remove('text-slate-400', 'dark:text-slate-500');
        elemento.classList.add('text-emerald-600', 'dark:text-emerald-400');
        if (icono) {
            icono.innerHTML = `<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M5 13l4 4L19 7" />`;
        }
    } else {
        // Estado pendiente: gris neutro con punto central
        elemento.classList.remove('text-emerald-600', 'dark:text-emerald-400');
        elemento.classList.add('text-slate-400', 'dark:text-slate-500');
        if (icono) {
            icono.innerHTML = `<circle cx="12" cy="12" r="9" stroke-width="2" fill="none" /><circle cx="12" cy="12" r="2" fill="currentColor" />`;
        }
    }
}

/**
 * Valida que la confirmación coincida exactamente con la nueva contraseña introducida.
 * @returns {boolean} - true si ambas claves coinciden.
 */
function validarCoincidenciaPasswords() {
    const passInput = document.getElementById('nueva-password');
    const confirmInput = document.getElementById('confirmar-password');

    if (!passInput || !confirmInput) return true;

    if (confirmInput.value && passInput.value !== confirmInput.value) {
        confirmInput.setCustomValidity('Las contraseñas no coinciden.');
        confirmInput.reportValidity();
        return false;
    } else {
        confirmInput.setCustomValidity('');
        return true;
    }
}

/**
 * Inicializa todos los eventos y escuchadores del DOM para la recuperación de contraseñas.
 */
function inicializarEventosRecuperacion() {
    // 1. Botones para conmutar visibilidad de las contraseñas
    const btnPass = document.getElementById('btn-toggle-nueva-pass');
    if (btnPass) {
        btnPass.addEventListener('click', () => {
            togglePasswordVisibility('nueva-password', 'eye-icon-nueva', 'btn-toggle-nueva-pass');
        });
    }

    const btnConfirm = document.getElementById('btn-toggle-confirmar-pass');
    if (btnConfirm) {
        btnConfirm.addEventListener('click', () => {
            togglePasswordVisibility('confirmar-password', 'eye-icon-confirmar', 'btn-toggle-confirmar-pass');
        });
    }

    // 2. Validación de seguridad en vivo
    inicializarValidadorSeguridad();

    // 3. Validación al enviar el formulario de restablecimiento
    const formRestablecer = document.getElementById('form-restablecer');
    if (formRestablecer) {
        formRestablecer.addEventListener('submit', (e) => {
            if (!validarCoincidenciaPasswords()) {
                e.preventDefault();
                return;
            }

            const passInput = document.getElementById('nueva-password');
            if (passInput && passInput.value.length < 6) {
                e.preventDefault();
                passInput.focus();
                passInput.reportValidity();
                return;
            }

            // Inhabilitar botón e iniciar animación de spinner para prevenir doble sumisión
            const btnSubmit = document.getElementById('btn-restablecer-password');
            if (btnSubmit && !btnSubmit.disabled) {
                btnSubmit.disabled = true;
                const btnText = btnSubmit.querySelector('.btn-text');
                const spinner = btnSubmit.querySelector('.btn-spinner');
                const spinnerText = btnSubmit.querySelector('.btn-spinner-text');
                if (btnText) btnText.classList.add('hidden');
                if (spinner) spinner.classList.remove('hidden');
                if (spinnerText) spinnerText.classList.remove('hidden');
            }
        });
    }

    // 4. Validación al enviar el formulario de solicitud inicial de recuperación
    const formSolicitud = document.getElementById('form-solicitud-recuperacion');
    if (formSolicitud) {
        formSolicitud.addEventListener('submit', (e) => {
            const correoInput = document.getElementById('recuperar-correo');
            if (correoInput && !correoInput.checkValidity()) {
                e.preventDefault();
                correoInput.focus();
                correoInput.reportValidity();
            }
        });
    }
}

// Inicialización automática tras cargar el DOM
document.addEventListener('DOMContentLoaded', () => {
    inicializarEventosRecuperacion();
});
