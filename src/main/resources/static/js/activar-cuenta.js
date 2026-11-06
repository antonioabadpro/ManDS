/**
 * activar-cuenta.js - Asistente de Activación de Cuenta de Profesor para ManDS
 *
 * =====================================================================================
 * 1. FLUJO DE SEGURIDAD PARA PROFESORES:
 *    Los profesores no se registran públicamente. Son dados de alta por un Administrador,
 *    quien les envía un enlace con un 'TokenVerificacion' efímero (válido durante 60 minutos).
 *    Al acceder al enlace, el profesor debe establecer su nombre de usuario y su contraseña.
 *
 * 2. VALIDACIÓN VISUAL DE ROBUSTEZ EN TIEMPO REAL:
 *    A medida que el profesor escribe su contraseña (evento 'input'), el script evalúa:
 *    - Longitud mínima de 6 caracteres (requisito de seguridad).
 *    - Presencia de al menos un número mediante expresión regular '/\d/'.
 *    - Presencia de al menos una letra mayúscula mediante expresión regular '/[A-Z]/'.
 *    Los indicadores cambian dinámicamente de color (gris a verde esmeralda) y sustituyen
 *    su icono por un check SVG.
 *
 * 3. VALIDACIÓN DE COINCIDENCIA Y RESTRICCIÓN DE CARACTERES:
 *    - Comprueba que la confirmación sea idéntica a la contraseña original.
 *    - Valida que el nombre de usuario contenga exclusivamente caracteres seguros: letras,
 *      números, puntos y guiones ('/^[a-zA-Z0-9._-]+$/').
 * =====================================================================================
 */

/**
 * Alterna la visibilidad de un campo de contraseña entre 'password' y 'text'.
 * @param {string} inputId - ID del input de contraseña en el DOM.
 * @param {string} iconId - ID del icono SVG del botón para actualizar su trazado.
 * @param {string} buttonId - ID del botón para actualizar sus atributos WAI-ARIA.
 */
function togglePasswordVisibility(inputId, iconId, buttonId) {
    const input = document.getElementById(inputId);
    const icon = document.getElementById(iconId);
    const button = document.getElementById(buttonId);

    if (!input) return;

    const esPassword = input.type === 'password';
    input.type = esPassword ? 'text' : 'password';

    // Soporte accesible WAI-ARIA para tecnologías de asistencia
    if (button) {
        button.setAttribute('aria-label', esPassword ? 'Ocultar contraseña' : 'Mostrar contraseña');
        button.setAttribute('aria-pressed', String(esPassword));
    }

    if (icon) {
        if (esPassword) {
            // Icono de ojo tachado (indica que la clave ahora es visible y se puede volver a ocultar)
            icon.innerHTML = `
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3.98 8.223A10.477 10.477 0 001.934 12C3.226 16.338 7.244 19.5 12 19.5c.993 0 1.953-.138 2.863-.395M6.228 6.228A10.45 10.45 0 0112 4.5c4.756 0 8.773 3.162 10.065 7.498a10.523 10.523 0 01-4.293 5.774M6.228 6.228L3 3m3.228 3.228l3.65 3.65m7.894 7.894L21 21m-3.228-3.228l-3.65-3.65m0 0a3 3 0 10-4.243-4.243m4.242 4.242L9.88 9.88" />
            `;
        } else {
            // Icono de ojo abierto (indica que la clave está oculta y se puede mostrar)
            icon.innerHTML = `
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z" />
            `;
        }
    }
}

/**
 * Configura los escuchadores de eventos para validar en tiempo real la robustez de la contraseña.
 */
function inicializarValidadorSeguridad() {
    const passInput = document.getElementById('password');
    const confirmInput = document.getElementById('confirmPassword');
    const reqLength = document.getElementById('req-length');
    const reqNumber = document.getElementById('req-number');
    const reqUpper = document.getElementById('req-upper');

    if (!passInput) return;

    // Escuchamos el evento 'input' (se dispara cada vez que el usuario presiona una tecla o pega texto)
    passInput.addEventListener('input', () => {
        const val = passInput.value;

        // Requisito: Mínimo 6 caracteres
        if (reqLength) {
            actualizarIndicadorRequisito(reqLength, val.length >= 6);
        }

        // Requisito: Al menos un número (\d coincide con cualquier dígito del 0 al 9)
        if (reqNumber) {
            actualizarIndicadorRequisito(reqNumber, /\d/.test(val));
        }

        // Requisito: Al menos una letra mayúscula ([A-Z])
        if (reqUpper) {
            actualizarIndicadorRequisito(reqUpper, /[A-Z]/.test(val));
        }

        // Si el usuario ya había introducido algo en el campo de confirmación, revalidamos coincidencia
        if (confirmInput && confirmInput.value) {
            validarCoincidenciaPasswords();
        }
    });

    if (confirmInput) {
        confirmInput.addEventListener('input', validarCoincidenciaPasswords);
    }
}

/**
 * Actualiza el aspecto visual (color e icono SVG) de un elemento de requisito de contraseña.
 * @param {HTMLElement} elemento - Elemento contenedor del requisito en el DOM.
 * @param {boolean} cumplido - True si el requisito se satisface con el texto actual.
 */
function actualizarIndicadorRequisito(elemento, cumplido) {
    const icono = elemento.querySelector('svg');

    if (cumplido) {
        // Estado cumplido: verde esmeralda con check
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
 * Valida que la confirmación de contraseña coincida exactamente con la contraseña principal.
 * @returns {boolean} - true si ambas claves coinciden o no se han introducido aún.
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
 * Valida el formato y longitud del nombre de usuario antes de enviarlo.
 * @returns {boolean} - true si el nombre de usuario cumple con la expresión regular y longitud.
 */
function validarNombreUsuario() {
    const userInput = document.getElementById('nombreUsuario');
    if (!userInput) return true;

    const val = userInput.value.trim();
    // Expresión regular: solo caracteres alfanuméricos en inglés, punto, guion bajo o guion medio
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
 * Registra todos los escuchadores de eventos para la vista de activación.
 */
function inicializarEventosActivacion() {
    // 1. Botones para conmutar visibilidad de las contraseñas
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

    // 2. Activación del validador de seguridad en tiempo real
    inicializarValidadorSeguridad();

    // 3. Validación de nombre de usuario en tiempo real
    const userInput = document.getElementById('nombreUsuario');
    if (userInput) {
        userInput.addEventListener('input', validarNombreUsuario);
        userInput.addEventListener('blur', () => {
            if (!validarNombreUsuario()) {
                userInput.reportValidity();
            }
        });
    }

    // 4. Validación final al pulsar el botón de envío del formulario
    const formActivacion = document.getElementById('form-activacion');
    if (formActivacion) {
        formActivacion.addEventListener('submit', (e) => {
            // Validar nombre de usuario
            if (!validarNombreUsuario()) {
                e.preventDefault();
                if (userInput) {
                    userInput.focus();
                    userInput.reportValidity();
                }
                return;
            }

            // Validar longitud mínima de la clave
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

            // Validar coincidencia de claves
            if (!validarCoincidenciaPasswords()) {
                e.preventDefault();
                const confirmInput = document.getElementById('confirmPassword');
                if (confirmInput) {
                    confirmInput.focus();
                    confirmInput.reportValidity();
                }
                return;
            }

            // Inhabilitar botón e iniciar animación de spinner para prevenir doble sumisión
            const btnSubmit = document.getElementById('btn-submit-activar');
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

    // Cierre accesible de alertas y notificaciones con animación suave
    document.addEventListener('click', (evento) => {
        const triggerAlertaCerrar = evento.target.closest('[data-alerta-cerrar]');
        if (triggerAlertaCerrar) {
            evento.preventDefault();
            const alerta = triggerAlertaCerrar.closest('[role="alert"]') || triggerAlertaCerrar.closest('.p-4') || triggerAlertaCerrar.parentElement;
            if (alerta) {
                alerta.classList.add('transition-all', 'duration-300', 'opacity-0', '-translate-y-2');
                setTimeout(() => {
                    alerta.remove();
                }, 300);
            }
        }
    });
}

// Inicialización tras la carga del DOM
document.addEventListener('DOMContentLoaded', () => {
    inicializarEventosActivacion();
});
