/**
 * Script de soporte para la vista de Activación de Cuenta de Profesor (ManDS).
 * Gestiona la visibilidad interactiva de contraseñas, validación en tiempo real de requisitos
 * de seguridad (6+ caracteres, número, mayúscula), comprobación de coincidencia de claves
 * y validación del formato del nombre de usuario.
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
    const passInput = document.getElementById('password');
    const confirmInput = document.getElementById('confirmPassword');
    const reqLength = document.getElementById('req-length');
    const reqNumber = document.getElementById('req-number');
    const reqUpper = document.getElementById('req-upper');

    if (!passInput) return;

    passInput.addEventListener('input', () => {
        const val = passInput.value;

        // Regla 1: Mínimo 6 caracteres (estándar global de la app)
        if (reqLength) {
            actualizarIndicadorRequisito(reqLength, val.length >= 6);
        }

        // Regla 2: Al menos un número (recomendado)
        if (reqNumber) {
            actualizarIndicadorRequisito(reqNumber, /\d/.test(val));
        }

        // Regla 3: Al menos una letra mayúscula (recomendada)
        if (reqUpper) {
            actualizarIndicadorRequisito(reqUpper, /[A-Z]/.test(val));
        }

        // Comprobación de coincidencia si el usuario ya escribió en confirmar
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
 * Valida que la confirmación de contraseña coincida exactamente con la contraseña.
 * @returns {boolean} - true si ambas claves coinciden.
 */
function validarCoincidenciaPasswords() {
    const passInput = document.getElementById('password');
    const confirmInput = document.getElementById('confirmPassword');

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
 * Valida el formato y longitud del nombre de usuario.
 * @returns {boolean} - true si el nombre de usuario es válido.
 */
function validarNombreUsuario() {
    const userInput = document.getElementById('nombreUsuario');
    if (!userInput) return true;

    const val = userInput.value.trim();
    const patron = /^[a-zA-Z0-9._-]+$/;

    if (val.length > 0 && (val.length < 3 || val.length > 30)) {
        userInput.setCustomValidity('El nombre de usuario debe tener entre 3 y 30 caracteres.');
        return false;
    } else if (val.length > 0 && !patron.test(val)) {
        userInput.setCustomValidity('Solo se permiten letras, números, puntos y guiones.');
        return false;
    } else {
        userInput.setCustomValidity('');
        return true;
    }
}

/**
 * Inicializa todos los eventos y escuchadores del DOM.
 */
function inicializarEventosActivacion() {
    // 1. Toggles de contraseñas
    const btnPass = document.getElementById('btn-toggle-password');
    if (btnPass) {
        btnPass.addEventListener('click', () => {
            togglePasswordVisibility('password', 'eye-icon-password', 'btn-toggle-password');
        });
    }

    const btnConfirm = document.getElementById('btn-toggle-confirm');
    if (btnConfirm) {
        btnConfirm.addEventListener('click', () => {
            togglePasswordVisibility('confirmPassword', 'eye-icon-confirm', 'btn-toggle-confirm');
        });
    }

    // 2. Validación de seguridad en vivo
    inicializarValidadorSeguridad();

    // 3. Validación de nombre de usuario en vivo
    const userInput = document.getElementById('nombreUsuario');
    if (userInput) {
        userInput.addEventListener('input', validarNombreUsuario);
        userInput.addEventListener('blur', () => {
            if (!validarNombreUsuario()) {
                userInput.reportValidity();
            }
        });
    }

    // 4. Validación al enviar el formulario
    const formActivacion = document.getElementById('form-activacion');
    if (formActivacion) {
        formActivacion.addEventListener('submit', (e) => {
            if (!validarNombreUsuario()) {
                e.preventDefault();
                if (userInput) {
                    userInput.focus();
                    userInput.reportValidity();
                }
                return;
            }

            const passInput = document.getElementById('password');
            if (passInput && passInput.value.length < 6) {
                e.preventDefault();
                passInput.focus();
                passInput.setCustomValidity('La contraseña debe tener al menos 6 caracteres.');
                passInput.reportValidity();
                return;
            } else if (passInput) {
                passInput.setCustomValidity('');
            }

            if (!validarCoincidenciaPasswords()) {
                e.preventDefault();
                const confirmInput = document.getElementById('confirmPassword');
                if (confirmInput) {
                    confirmInput.focus();
                    confirmInput.reportValidity();
                }
            }
        });
    }
}

document.addEventListener('DOMContentLoaded', () => {
    inicializarEventosActivacion();
});
