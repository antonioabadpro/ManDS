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

    // Validación específica del Paso 2: Fecha de nacimiento (mayoría de edad)
    if (paso === 2) {
        if (!validarFechaNacimiento()) {
            const inputFecha = document.getElementById('reg-fechaNacimiento');
            if (inputFecha) inputFecha.focus();
            return false;
        }
    }

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
        if (!validarCoincidenciaPasswords()) {
            return false;
        }
    }

    // Comprobar si existen mensajes de error asíncronos activos devueltos por HTMX
    const errorHTMX = contenedorPaso.querySelector('.mensaje-error-campo');
    if (errorHTMX) {
        const feedbackDiv = errorHTMX.closest('[id^="feedback-"]');
        if (feedbackDiv) {
            const campoNombre = feedbackDiv.id.replace('feedback-', '');
            const inputAsociado = document.getElementById(`reg-${campoNombre}`);
            if (inputAsociado) {
                inputAsociado.focus();
            }
        }
        return false;
    }

    return true;
}

/**
 * Valida que la confirmación de contraseña coincida exactamente con la contraseña.
 * Utiliza la Constraint Validation API nativa de HTML5.
 * @returns {boolean} - true si ambas claves coinciden.
 */
function validarCoincidenciaPasswords() {
    const passInput = document.getElementById('reg-password');
    const confirmInput = document.getElementById('reg-confirmPassword');

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
 * Configura los límites de fecha (min y max) en el selector nativo de fecha de nacimiento.
 * Restringe la fecha máxima a hoy hace 18 años (mayoría de edad) y mínima a hace 100 años.
 */
function configurarRestriccionFechaNacimiento() {
    const inputFecha = document.getElementById('reg-fechaNacimiento');
    if (!inputFecha) return;

    const hoy = new Date();
    const hace18Anios = new Date(hoy.getFullYear() - 18, hoy.getMonth(), hoy.getDate());
    const hace100Anios = new Date(hoy.getFullYear() - 100, hoy.getMonth(), hoy.getDate());

    const formatearFecha = (d) => {
        const anio = d.getFullYear();
        const mes = String(d.getMonth() + 1).padStart(2, '0');
        const dia = String(d.getDate()).padStart(2, '0');
        return `${anio}-${mes}-${dia}`;
    };

    inputFecha.setAttribute('max', formatearFecha(hace18Anios));
    inputFecha.setAttribute('min', formatearFecha(hace100Anios));
}

/**
 * Muestra el mensaje de error visual para el campo de fecha de nacimiento,
 * respetando el formato SVG y las clases Tailwind usadas en los componentes HTMX.
 * @param {string} mensaje - Texto explicativo del error.
 */
function mostrarErrorFechaNacimiento(mensaje) {
    const input = document.getElementById('reg-fechaNacimiento');
    const feedback = document.getElementById('feedback-fechaNacimiento');

    if (input) {
        input.classList.add('border-red-500', 'focus:ring-red-500', 'dark:border-red-500');
        input.classList.remove('border-slate-300', 'dark:border-slate-700', 'focus:ring-blue-600');
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

/**
 * Limpia el mensaje de error y restablece el estilo estándar del campo de fecha de nacimiento.
 */
function limpiarErrorFechaNacimiento() {
    const input = document.getElementById('reg-fechaNacimiento');
    const feedback = document.getElementById('feedback-fechaNacimiento');

    if (input) {
        input.classList.remove('border-red-500', 'focus:ring-red-500', 'dark:border-red-500');
        input.classList.add('border-slate-300', 'dark:border-slate-700', 'focus:ring-blue-600');
    }

    if (feedback) {
        feedback.innerHTML = '';
    }
}

/**
 * Valida la fecha de nacimiento en el frontend:
 * - Debe ser obligatoria.
 * - Debe ser una fecha pasada.
 * - El alumno debe ser mayor de edad (al menos 18 años cumplidos).
 * - La edad no debe exceder un límite razonable (100 años).
 * @returns {boolean} true si la fecha es válida y cumple con la mayoría de edad.
 */
function validarFechaNacimiento() {
    const input = document.getElementById('reg-fechaNacimiento');
    if (!input) return true;

    const valor = input.value?.trim();
    if (!valor) {
        mostrarErrorFechaNacimiento('La fecha de nacimiento es obligatoria.');
        input.setCustomValidity('La fecha de nacimiento es obligatoria.');
        return false;
    }

    const partes = valor.split('-');
    if (partes.length !== 3) {
        mostrarErrorFechaNacimiento('El formato de la fecha no es válido.');
        input.setCustomValidity('El formato de la fecha no es válido.');
        return false;
    }

    const anio = parseInt(partes[0], 10);
    const mes = parseInt(partes[1], 10) - 1;
    const dia = parseInt(partes[2], 10);
    const fechaNac = new Date(anio, mes, dia);

    if (isNaN(fechaNac.getTime())) {
        mostrarErrorFechaNacimiento('Introduce una fecha de nacimiento válida.');
        input.setCustomValidity('Introduce una fecha de nacimiento válida.');
        return false;
    }

    const hoy = new Date();
    const hoySinHora = new Date(hoy.getFullYear(), hoy.getMonth(), hoy.getDate());

    if (fechaNac > hoySinHora) {
        mostrarErrorFechaNacimiento('La fecha de nacimiento debe ser una fecha pasada.');
        input.setCustomValidity('La fecha de nacimiento debe ser una fecha pasada.');
        return false;
    }

    const hace100Anios = new Date(hoy.getFullYear() - 100, hoy.getMonth(), hoy.getDate());
    if (fechaNac < hace100Anios) {
        mostrarErrorFechaNacimiento('Introduce una fecha de nacimiento válida.');
        input.setCustomValidity('Introduce una fecha de nacimiento válida.');
        return false;
    }

    const fechaMinima18 = new Date(anio + 18, mes, dia);
    if (hoySinHora < fechaMinima18) {
        mostrarErrorFechaNacimiento('El alumno debe ser mayor de edad (al menos 18 años).');
        input.setCustomValidity('El alumno debe ser mayor de edad (al menos 18 años).');
        return false;
    }

    limpiarErrorFechaNacimiento();
    input.setCustomValidity('');
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

    // Validación reactiva en tiempo real de coincidencia de contraseñas
    const passInput = document.getElementById('reg-password');
    const confirmInput = document.getElementById('reg-confirmPassword');

    if (passInput) {
        passInput.addEventListener('input', () => {
            // Si ya se ha escrito en la confirmación, revalidamos al modificar la clave principal
            if (confirmInput && confirmInput.value) {
                validarCoincidenciaPasswords();
            }
        });
    }

    if (confirmInput) {
        confirmInput.addEventListener('input', validarCoincidenciaPasswords);
    }

    // Validación reactiva de fecha de nacimiento (mayoría de edad)
    const fechaNacInput = document.getElementById('reg-fechaNacimiento');
    if (fechaNacInput) {
        fechaNacInput.addEventListener('change', validarFechaNacimiento);
        fechaNacInput.addEventListener('input', () => {
            if (fechaNacInput.value) {
                validarFechaNacimiento();
            }
        });
        fechaNacInput.addEventListener('blur', () => {
            if (fechaNacInput.value) {
                validarFechaNacimiento();
            }
        });
    }

    // Interceptar envío de formulario (evita submit accidental con Enter en pasos 1 y 2)
    const formRegistro = document.getElementById('form-registro');
    if (formRegistro) {
        formRegistro.addEventListener('submit', (e) => {
            if (pasoActual < TOTAL_PASOS) {
                // Prevenir el POST al servidor si aún estamos en pasos preliminares
                e.preventDefault();
                const btnSiguiente = document.getElementById(`btn-next-step-${pasoActual}`);
                if (btnSiguiente) {
                    btnSiguiente.click();
                }
            } else {
                // En el paso 3, validar los campos antes de permitir el envío nativo
                if (!validarPaso(TOTAL_PASOS)) {
                    e.preventDefault();
                }
            }
        });
    }
    // Escucha de respuestas HTMX para alternar clases visuales de error/éxito en los inputs
    document.body.addEventListener('htmx:afterSwap', (event) => {
        const targetId = event.detail.target?.id;
        if (targetId && targetId.startsWith('feedback-')) {
            const campoNombre = targetId.replace('feedback-', '');
            const input = document.getElementById(`reg-${campoNombre}`);
            const tieneError = event.detail.target.querySelector('.mensaje-error-campo') !== null;

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
    });
}

// Inicializar el asistente al cargar la vista
document.addEventListener('DOMContentLoaded', () => {
    configurarRestriccionFechaNacimiento();
    inicializarEventosRegistro();
    irAPaso(1);
});


