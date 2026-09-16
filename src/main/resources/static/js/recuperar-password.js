/**
 * Script de soporte para la vista de Recuperación y Restablecimiento de Contraseña (ManDS).
 * Gestiona la visualización interactiva de contraseñas, validaciones previas al envío
 * y comprobación de coincidencia de claves.
 */

/**
 * Alterna la visibilidad de un campo de contraseña entre 'password' y 'text'.
 * @param {string} inputId - ID del input de contraseña.
 * @param {string} iconId - ID del icono SVG del botón.
 * @param {string} buttonId - ID del botón que ejecuta la alternancia.
 */
function togglePasswordVisibility(inputId, iconId, buttonId) {
    const input = document.getElementById(inputId);
    const icon = document.getElementById(iconId);
    const button = document.getElementById(buttonId);

    if (!input) return;

    const esPassword = input.type === 'password';
    input.type = esPassword ? 'text' : 'password';

    if (button) {
        button.setAttribute('aria-label', esPassword ? 'Ocultar contraseña' : 'Mostrar contraseña');
        button.setAttribute('aria-pressed', String(esPassword));
    }

    if (icon) {
        if (esPassword) {
            // Icono de ojo tachado (ocultar)
            icon.innerHTML = `
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l18 18" />
            `;
        } else {
            // Icono de ojo normal (mostrar)
            icon.innerHTML = `
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
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

    passInput.addEventListener('input', () => {
        const val = passInput.value;

        // Regla 1: Mínimo 8 caracteres
        if (reqLength) {
            actualizarIndicadorRequisito(reqLength, val.length >= 8);
        }

        // Regla 2: Al menos un número
        if (reqNumber) {
            actualizarIndicadorRequisito(reqNumber, /\d/.test(val));
        }

        // Regla 3: Al menos una letra mayúscula
        if (reqUpper) {
            actualizarIndicadorRequisito(reqUpper, /[A-Z]/.test(val));
        }

        // Comprobación de coincidencia si ya se escribió en confirmar
        if (confirmInput && confirmInput.value) {
            validarCoincidenciaPasswords();
        }
    });

    if (confirmInput) {
        confirmInput.addEventListener('input', validarCoincidenciaPasswords);
    }
}

/**
 * Actualiza el icono y color de un requisito de contraseña.
 * @param {HTMLElement} elemento - Elemento contenedor del requisito.
 * @param {boolean} cumplido - Indica si la condición se cumple.
 */
function actualizarIndicadorRequisito(elemento, cumplido) {
    const icono = elemento.querySelector('svg');
    const texto = elemento.querySelector('span');

    if (cumplido) {
        elemento.classList.remove('text-slate-400', 'dark:text-slate-500');
        elemento.classList.add('text-emerald-600', 'dark:text-emerald-400');
        if (icono) {
            icono.innerHTML = `<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M5 13l4 4L19 7" />`;
        }
    } else {
        elemento.classList.remove('text-emerald-600', 'dark:text-emerald-400');
        elemento.classList.add('text-slate-400', 'dark:text-slate-500');
        if (icono) {
            icono.innerHTML = `<circle cx="12" cy="12" r="9" stroke-width="2" fill="none" /><circle cx="12" cy="12" r="2" fill="currentColor" />`;
        }
    }
}

/**
 * Valida que la confirmación de contraseña coincida exactamente con la nueva contraseña.
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
 * Inicializa todos los eventos y escuchadores del DOM.
 */
function inicializarEventosRecuperacion() {
    // 1. Toggles de contraseñas
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
            if (passInput && passInput.value.length < 8) {
                e.preventDefault();
                passInput.focus();
                passInput.reportValidity();
            }
        });
    }

    // 4. Validación al enviar el formulario de solicitud
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

document.addEventListener('DOMContentLoaded', () => {
    inicializarEventosRecuperacion();
});

