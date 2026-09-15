/**
 * Script de soporte para el Asistente de Registro Multi-Paso de Alumnos (ManDS).
 * Gestiona el avance y retroceso entre los 3 pasos, la validación de campos
 * y la visibilidad interactiva de contraseñas.
 */

let pasoActual = 1;
const TOTAL_PASOS = 3;

/**
 * Valida los campos obligatorios del paso actual antes de permitir avanzar.
 * @param {number} paso - Número del paso a validar (1, 2 o 3).
 * @returns {boolean} - true si todos los campos del paso son válidos.
 */
function validarPaso(paso) {
    const contenedorPaso = document.getElementById(`panel-step-${paso}`);
    if (!contenedorPaso) return true;

    // Obtener todos los inputs obligatorios o con reglas del paso actual
    const inputs = contenedorPaso.querySelectorAll('input, select, textarea');
    for (const input of inputs) {
        if (!input.checkValidity()) {
            input.reportValidity();
            return false;
        }
    }

    // Validación específica del Paso 1: Coincidencia de contraseñas
    if (paso === 1) {
        const pass = document.getElementById('reg-password');
        const confirmPass = document.getElementById('reg-confirmPassword');

        if (pass && confirmPass && pass.value !== confirmPass.value) {
            confirmPass.setCustomValidity('Las contraseñas introducidas no coinciden.');
            confirmPass.reportValidity();
            return false;
        } else if (confirmPass) {
            confirmPass.setCustomValidity('');
        }
    }

    return true;
}

/**
 * Muestra el panel del paso indicado y actualiza la apariencia del Stepper.
 * @param {number} nuevoPaso - Paso al que se desea navegar (1, 2 o 3).
 */
function irAPaso(nuevoPaso) {
    if (nuevoPaso < 1 || nuevoPaso > TOTAL_PASOS) return;

    // 1. Alternar visibilidad de los paneles de formulario
    for (let i = 1; i <= TOTAL_PASOS; i++) {
        const panel = document.getElementById(`panel-step-${i}`);
        if (panel) {
            if (i === nuevoPaso) {
                panel.classList.remove('hidden');
                panel.classList.add('fade-in');
            } else {
                panel.classList.add('hidden');
                panel.classList.remove('fade-in');
            }
        }
    }

    // 2. Actualizar estilos visuales e indicadores del Stepper
    for (let i = 1; i <= TOTAL_PASOS; i++) {
        const stepCircle = document.getElementById(`step-indicator-${i}`);
        const stepLabel = document.getElementById(`step-label-${i}`);
        const stepLine = document.getElementById(`step-line-${i}`);

        if (stepCircle && stepLabel) {
            if (i < nuevoPaso) {
                // Paso completado: Fondo azul con check SVG
                stepCircle.className = 'w-8 h-8 rounded-full flex items-center justify-center font-bold text-xs bg-blue-600 text-white shadow-sm ring-2 ring-blue-600/30';
                stepCircle.innerHTML = `
                    <svg class="w-4 h-4 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="3">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7" />
                    </svg>
                `;
                stepLabel.className = 'hidden sm:inline text-xs font-semibold text-blue-600 dark:text-blue-400';
            } else if (i === nuevoPaso) {
                // Paso activo actual: Fondo azul con número y anillo pulsante
                stepCircle.className = 'w-8 h-8 rounded-full flex items-center justify-center font-bold text-xs bg-blue-600 text-white shadow-md shadow-blue-500/30 ring-4 ring-blue-500/20';
                stepCircle.textContent = i;
                stepLabel.className = 'hidden sm:inline text-xs font-bold text-slate-900 dark:text-white';
            } else {
                // Paso futuro pendiente: Fondo gris neutro
                stepCircle.className = 'w-8 h-8 rounded-full flex items-center justify-center font-medium text-xs bg-slate-100 dark:bg-slate-800 text-slate-400 dark:text-slate-500 border border-slate-200 dark:border-slate-700';
                stepCircle.textContent = i;
                stepLabel.className = 'hidden sm:inline text-xs font-medium text-slate-400 dark:text-slate-500';
            }
        }

        // Conector de línea entre pasos
        if (stepLine) {
            if (i < nuevoPaso) {
                stepLine.className = 'flex-1 h-0.5 bg-blue-600 transition-colors duration-300';
            } else {
                stepLine.className = 'flex-1 h-0.5 bg-slate-200 dark:bg-slate-700 transition-colors duration-300';
            }
        }
    }

    pasoActual = nuevoPaso;

    // Foco automático en el primer input accesible del nuevo paso
    const primerInput = document.querySelector(`#panel-step-${nuevoPaso} input:not([type="hidden"])`);
    if (primerInput) {
        primerInput.focus();
    }
}

/**
 * Alterna la visibilidad de un campo de contraseña entre 'password' y 'text'.
 * @param {string} inputId - ID del input de contraseña.
 * @param {string} iconId - ID del icono SVG del botón.
 */
function togglePasswordVisibility(inputId, iconId) {
    const input = document.getElementById(inputId);
    const icon = document.getElementById(iconId);

    if (!input || !icon) return;

    if (input.type === 'password') {
        input.type = 'text';
        icon.innerHTML = `
            <path stroke-linecap="round" stroke-linejoin="round" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l18 18" />
        `;
    } else {
        input.type = 'password';
        icon.innerHTML = `
            <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
            <path stroke-linecap="round" stroke-linejoin="round" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/>
        `;
    }
}

/**
 * Registra los escuchadores de eventos para el asistente de registro.
 */
function inicializarEventosRegistro() {
    // Botones de avance ("Siguiente")
    const btnNext1 = document.getElementById('btn-next-step-1');
    const btnNext2 = document.getElementById('btn-next-step-2');

    if (btnNext1) {
        btnNext1.addEventListener('click', () => {
            if (validarPaso(1)) {
                irAPaso(2);
            }
        });
    }

    if (btnNext2) {
        btnNext2.addEventListener('click', () => {
            if (validarPaso(2)) {
                irAPaso(3);
            }
        });
    }

    // Botones de retroceso ("Atrás")
    const btnPrev2 = document.getElementById('btn-prev-step-2');
    const btnPrev3 = document.getElementById('btn-prev-step-3');

    if (btnPrev2) {
        btnPrev2.addEventListener('click', () => irAPaso(1));
    }

    if (btnPrev3) {
        btnPrev3.addEventListener('click', () => irAPaso(2));
    }

    // Botones de visibilidad de contraseña
    const btnOcultarReg = document.getElementById('btn-ocultar-password-registro');
    const btnOcultarConfirm = document.getElementById('btn-ocultar-confirm-password');

    if (btnOcultarReg) {
        btnOcultarReg.addEventListener('click', () => togglePasswordVisibility('reg-password', 'eye-icon-reg'));
    }

    if (btnOcultarConfirm) {
        btnOcultarConfirm.addEventListener('click', () => togglePasswordVisibility('reg-confirmPassword', 'eye-icon-confirm'));
    }

    // Limpiar mensaje de no coincidencia en confirmPassword al teclear
    const confirmPass = document.getElementById('reg-confirmPassword');
    if (confirmPass) {
        confirmPass.addEventListener('input', () => {
            confirmPass.setCustomValidity('');
        });
    }
}

// Inicializar el asistente al cargar la vista
document.addEventListener('DOMContentLoaded', () => {
    inicializarEventosRegistro();
    irAPaso(1);
});

