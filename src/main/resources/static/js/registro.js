/**
 * registro.js - Asistente de Registro Multi-Paso de Alumnos (Wizard) para ManDS ERP
 *
 * =====================================================================================
 * 1. EXPERIENCIA DE USUARIO GUIADA (WIZARD MULTI-STEP):
 *    El alta de un nuevo alumno requiere recopilar datos de acceso, datos personales y
 *    selección de permiso. Dividir el formulario en 4 pasos secuenciales reduce la carga
 *    cognitiva del usuario y mejora la tasa de conversión respecto a un formulario kilométrico.
 *
 * 2. ESTRUCTURA DE LOS 4 PASOS:
 *    - Paso 1: Credenciales (Usuario, Email, Contraseña y Confirmación).
 *    - Paso 2: Datos Personales (DNI/NIE, Nombre, Apellidos, Teléfono, Fecha de Nacimiento).
 *    - Paso 3: Configuración Académica (Tipo de Carnet y Modalidad Teórico-Práctica o Práctica).
 *    - Paso 4: Resumen de Matrícula y Pasarela a Pago Seguro con Stripe.
 *
 * 3. DOBLE BARRERA DE VALIDACIÓN (CLIENTE + HTMX EN TIEMPO REAL):
 *    - Validación nativa en cliente: Constraint Validation API ('checkValidity', 'setCustomValidity'),
 *      cálculo de mayoría de edad (mínimo 18 años cumplidos) y coincidencia de contraseñas.
 *    - Validación asíncrona mediante HTMX: comprueba en el backend si el DNI, email o username
 *      ya están registrados antes de que el usuario avance de paso.
 *
 * 4. NAVEGACIÓN INTELIGENTE CON TECLADO:
 *    Intercepta la pulsación de la tecla 'Enter' en los campos para enfocar el siguiente input
 *    en lugar de disparar el envío prematuro del formulario.
 * =====================================================================================
 */

// Estado global del asistente: paso actual y número total de pasos
let pasoActual = 1;
const TOTAL_PASOS = 4;

/**
 * Actualiza dinámicamente la información de la tasa de matrícula en el Paso 4 (Resumen)
 * según el tipo de carnet seleccionado en el desplegable del Paso 3:
 * - Permisos C y D (vehículos pesados/industriales): 450,00 €.
 * - Resto de permisos (B, A2, etc.): 250,00 €.
 */
function actualizarInformacionTasa() {
    const selectCarnet = document.getElementById('reg-tipoCarnet');
    const inputDni = document.getElementById('reg-dni');
    const valorCarnet = selectCarnet?.value || 'PERMISO_B';
    const valorDni = inputDni?.value?.trim() || '';

    // Extraemos la descripción legible de la opción seleccionada (ej. "Permiso B — Turismos")
    const textoOption = selectCarnet && selectCarnet.selectedIndex >= 0
        ? selectCarnet.options[selectCarnet.selectedIndex].text
        : 'Permiso B';

    const nombreCarnet = textoOption.split('—')[0].trim();

    // Tarifas oficiales: Permisos C y D son pesados (450 €) vs estándar (250 €)
    const esVehiculoPesado = valorCarnet.startsWith('PERMISO_C') || valorCarnet.startsWith('PERMISO_D');
    const importeTexto = esVehiculoPesado ? '450,00 €' : '250,00 €';

    // Elementos del DOM donde reflejaremos la tarifa recalculada
    const elemTitulo = document.getElementById('resumen-carnet-titulo');
    const elemPrecio = document.getElementById('resumen-tasa-precio');
    const elemBotonTexto = document.getElementById('btn-pago-texto');

    if (elemTitulo) elemTitulo.textContent = nombreCarnet;
    if (elemPrecio) elemPrecio.textContent = importeTexto;
    if (elemBotonTexto) elemBotonTexto.textContent = `Pagar Matrícula (${importeTexto})`;

    // Ajuste de características incluidas según la modalidad elegida
    const selectModalidad = document.getElementById('reg-modalidad');
    const valorModalidad = selectModalidad?.value || 'TEORICO_PRACTICA';
    const esTeoricoPractica = valorModalidad === 'TEORICO_PRACTICA';
    const puntoTests = document.getElementById('punto-incluido-tests');

    // Los tests teóricos solo se muestran incluidos si la modalidad es Teórico-Práctica
    if (puntoTests) {
        puntoTests.style.display = esTeoricoPractica ? 'flex' : 'none';
    }
}

/**
 * Valida los campos obligatorios del paso actual antes de permitir avanzar.
 * @param {number} paso - Número del paso a validar (1, 2 o 3).
 * @returns {boolean} - true si todos los campos del paso son válidos.
 */
function validarPaso(paso) {
    const contenedorPaso = document.getElementById(`panel-step-${paso}`);
    if (!contenedorPaso) return true;

    // Validación específica del Paso 1: Coincidencia idéntica de contraseñas
    if (paso === 1) {
        if (!validarCoincidenciaPasswords()) {
            return false;
        }
    }

    // Obtener todos los inputs obligatorios del paso y validar con la API nativa HTML5
    const inputs = contenedorPaso.querySelectorAll('input, select, textarea');
    for (const input of inputs) {
        if (!input.checkValidity()) {
            input.reportValidity(); // Muestra el globo de validación nativo del navegador
            return false;
        }
    }

    // Validación específica del Paso 2: Fecha de nacimiento (mayoría de edad)
    if (paso === 2) {
        if (!validarFechaNacimiento()) {
            const inputFecha = document.getElementById('reg-fechaNacimiento');
            if (inputFecha) inputFecha.focus();
            return false;
        }
    }

    // Comprobar si existen mensajes de error asíncronos activos devueltos por HTMX (ej. "DNI ya registrado")
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
 * Valida que la confirmación de contraseña coincida exactamente con la contraseña principal.
 * Utiliza la Constraint Validation API nativa de HTML5 ('setCustomValidity').
 * @returns {boolean} - true si ambas claves coinciden.
 */
function validarCoincidenciaPasswords() {
    const passInput = document.getElementById('reg-password');
    const confirmInput = document.getElementById('reg-confirmPassword');

    if (!passInput || !confirmInput) return true;

    // Si se ha escrito algo en la confirmación y no coincide con la original:
    if (confirmInput.value && passInput.value !== confirmInput.value) {
        confirmInput.setCustomValidity('Las contraseñas no coinciden.');
        confirmInput.reportValidity();
        return false;
    } else {
        // Cadena vacía restablece el campo a estado válido
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
    // Restamos 18 años a la fecha actual para la fecha límite superior
    const hace18Anios = new Date(hoy.getFullYear() - 18, hoy.getMonth(), hoy.getDate());
    // Restamos 100 años a la fecha actual para el límite inferior razonable
    const hace100Anios = new Date(hoy.getFullYear() - 100, hoy.getMonth(), hoy.getDate());

    // Formateador auxiliar YYYY-MM-DD
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
 * - Debe ser una fecha pasada válida.
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
    const mes = parseInt(partes[1], 10) - 1; // En JavaScript los meses van de 0 (enero) a 11 (diciembre)
    const dia = parseInt(partes[2], 10);
    const fechaNac = new Date(anio, mes, dia);

    if (isNaN(fechaNac.getTime())) {
        mostrarErrorFechaNacimiento('Introduce una fecha de nacimiento válida.');
        input.setCustomValidity('Introduce una fecha de nacimiento válida.');
        return false;
    }

    const hoy = new Date();
    const hoySinHora = new Date(hoy.getFullYear(), hoy.getMonth(), hoy.getDate());

    // La fecha no puede ser futura
    if (fechaNac > hoySinHora) {
        mostrarErrorFechaNacimiento('La fecha de nacimiento debe ser una fecha pasada.');
        input.setCustomValidity('La fecha de nacimiento debe ser una fecha pasada.');
        return false;
    }

    // Límite de 100 años
    const hace100Anios = new Date(hoy.getFullYear() - 100, hoy.getMonth(), hoy.getDate());
    if (fechaNac < hace100Anios) {
        mostrarErrorFechaNacimiento('Introduce una fecha de nacimiento válida.');
        input.setCustomValidity('Introduce una fecha de nacimiento válida.');
        return false;
    }

    // Comprobación de mayoría de edad (18 años cumplidos)
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
 * Muestra el panel del paso indicado y actualiza visualmente los indicadores del Stepper superior.
 * @param {number} nuevoPaso - Paso al que se desea navegar (1, 2, 3 o 4).
 */
function irAPaso(nuevoPaso) {
    if (nuevoPaso < 1 || nuevoPaso > TOTAL_PASOS) return;

    // 1. Alternar visibilidad de los paneles de formulario con transición fade-in
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

    // 2. Actualizar estilos visuales e indicadores del Stepper en la cabecera
    for (let i = 1; i <= TOTAL_PASOS; i++) {
        const stepCircle = document.getElementById(`step-indicator-${i}`);
        const stepLabel = document.getElementById(`step-label-${i}`);
        const stepLine = document.getElementById(`step-line-${i}`);

        if (stepCircle && stepLabel) {
            if (i < nuevoPaso) {
                // Paso completado: Fondo azul corporativo con icono de check SVG
                stepCircle.className = 'w-8 h-8 rounded-full flex items-center justify-center font-bold text-xs bg-blue-600 text-white shadow-sm ring-2 ring-blue-600/30';
                stepCircle.innerHTML = `
                    <svg class="w-4 h-4 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="3">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7" />
                    </svg>
                `;
                stepLabel.className = 'hidden sm:inline text-xs font-semibold text-blue-600 dark:text-blue-400';
            } else if (i === nuevoPaso) {
                // Paso activo actual: Fondo azul con número y anillo iluminado
                stepCircle.className = 'w-8 h-8 rounded-full flex items-center justify-center font-bold text-xs bg-blue-600 text-white shadow-md shadow-blue-500/30 ring-4 ring-blue-500/20';
                stepCircle.textContent = i;
                stepLabel.className = 'hidden sm:inline text-xs font-bold text-slate-900 dark:text-white';
            } else {
                // Paso futuro pendiente: Fondo gris neutro deshabilitado
                stepCircle.className = 'w-8 h-8 rounded-full flex items-center justify-center font-medium text-xs bg-slate-100 dark:bg-slate-800 text-slate-400 dark:text-slate-500 border border-slate-200 dark:border-slate-700';
                stepCircle.textContent = i;
                stepLabel.className = 'hidden sm:inline text-xs font-medium text-slate-400 dark:text-slate-500';
            }
        }

        // Conector de línea entre los círculos del Stepper
        if (stepLine) {
            if (i < nuevoPaso) {
                stepLine.className = 'flex-1 h-0.5 bg-blue-600 transition-colors duration-300';
            } else {
                stepLine.className = 'flex-1 h-0.5 bg-slate-200 dark:bg-slate-700 transition-colors duration-300';
            }
        }
    }

    pasoActual = nuevoPaso;

    // Si entramos al Paso 4, refrescamos el desglose de tarifas
    if (nuevoPaso === 4) {
        actualizarInformacionTasa();
    }

    // Foco automático en el primer input accesible del nuevo paso para mejorar usabilidad
    const primerInput = document.querySelector(`#panel-step-${nuevoPaso} input:not([type="hidden"])`);
    if (primerInput) {
        primerInput.focus();
    }
}

/**
 * Alterna la visibilidad de un campo de contraseña entre 'password' y 'text'.
 * @param {string} inputId - ID del input de contraseña en el DOM.
 * @param {string} iconId - ID del SVG del icono para actualizar su forma geométrica.
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
 * Registra todos los escuchadores de eventos para los botones e inputs del asistente.
 */
function inicializarEventosRegistro() {
    // 1. Botones de avance ("Siguiente paso")
    const btnNext1 = document.getElementById('btn-next-step-1');
    const btnNext2 = document.getElementById('btn-next-step-2');
    const btnNext3 = document.getElementById('btn-next-step-3');

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

    if (btnNext3) {
        btnNext3.addEventListener('click', () => {
            if (validarPaso(3)) {
                actualizarInformacionTasa();
                irAPaso(4);
            }
        });
    }

    // 2. Botones de retroceso ("Paso anterior")
    const btnPrev2 = document.getElementById('btn-prev-step-2');
    const btnPrev3 = document.getElementById('btn-prev-step-3');
    const btnPrev4 = document.getElementById('btn-prev-step-4');

    if (btnPrev2) {
        btnPrev2.addEventListener('click', () => irAPaso(1));
    }

    if (btnPrev3) {
        btnPrev3.addEventListener('click', () => irAPaso(2));
    }

    if (btnPrev4) {
        btnPrev4.addEventListener('click', () => irAPaso(3));
    }

    // 3. Recálculo reactivo de tarifas al cambiar de carnet o modalidad
    const selectCarnet = document.getElementById('reg-tipoCarnet');
    if (selectCarnet) {
        selectCarnet.addEventListener('change', actualizarInformacionTasa);
    }

    const selectModalidad = document.getElementById('reg-modalidad');
    if (selectModalidad) {
        selectModalidad.addEventListener('change', actualizarInformacionTasa);
    }

    const inputDni = document.getElementById('reg-dni');
    if (inputDni) {
        inputDni.addEventListener('input', actualizarInformacionTasa);
        inputDni.addEventListener('change', actualizarInformacionTasa);
    }

    // 4. Botones para alternar visibilidad de las contraseñas
    const btnOcultarReg = document.getElementById('btn-ocultar-password-registro');
    const btnOcultarConfirm = document.getElementById('btn-ocultar-confirm-password');

    if (btnOcultarReg) {
        btnOcultarReg.addEventListener('click', () => togglePasswordVisibility('reg-password', 'eye-icon-reg'));
    }

    if (btnOcultarConfirm) {
        btnOcultarConfirm.addEventListener('click', () => togglePasswordVisibility('reg-confirmPassword', 'eye-icon-confirm'));
    }

    // 5. Validación en tiempo real de coincidencia de contraseñas
    const passInput = document.getElementById('reg-password');
    const confirmInput = document.getElementById('reg-confirmPassword');

    if (passInput) {
        passInput.addEventListener('input', () => {
            // Si el usuario ya había escrito en la confirmación, revalidamos automáticamente
            if (confirmInput && confirmInput.value) {
                validarCoincidenciaPasswords();
            }
        });
    }

    if (confirmInput) {
        confirmInput.addEventListener('input', validarCoincidenciaPasswords);
    }

    // 6. Validación interactiva de la fecha de nacimiento
    const fechaNacInput = document.getElementById('reg-fechaNacimiento');
    if (fechaNacInput) {
        // Al enfocar, limpia errores previos para permitir escribir con comodidad
        fechaNacInput.addEventListener('focus', () => {
            limpiarErrorFechaNacimiento();
        });

        fechaNacInput.addEventListener('change', () => {
            if (fechaNacInput.value) {
                validarFechaNacimiento();
            } else {
                limpiarErrorFechaNacimiento();
            }
        });

        fechaNacInput.addEventListener('input', () => {
            if (fechaNacInput.value) {
                validarFechaNacimiento();
            } else {
                limpiarErrorFechaNacimiento();
            }
        });

        fechaNacInput.addEventListener('blur', () => {
            if (fechaNacInput.value) {
                validarFechaNacimiento();
            }
        });
    }

    // 7. Botón final para proceder al pago con Stripe Checkout
    const btnIrPago = document.getElementById('btn-ir-pago');
    if (btnIrPago) {
        btnIrPago.addEventListener('click', (e) => {
            // Validación estricta acumulada de los pasos 1, 2 y 3
            for (let i = 1; i <= 3; i++) {
                if (!validarPaso(i)) {
                    e.preventDefault();
                    irAPaso(i); // Regresa al primer paso que contenga errores
                    return;
                }
            }
            actualizarInformacionTasa();
        });
    }

    // 8. Control del formulario y navegación suave con tecla Enter
    const formRegistro = document.getElementById('form-registro');
    if (formRegistro) {
        // Al pulsar Enter en un campo de texto, enfoca el siguiente campo en vez de enviar el formulario
        formRegistro.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' && e.target.tagName === 'INPUT' && e.target.type !== 'submit') {
                e.preventDefault();
                const inputsPaso = Array.from(document.querySelectorAll(`#panel-step-${pasoActual} input:not([type="hidden"]), #panel-step-${pasoActual} select`));
                const currentIndex = inputsPaso.indexOf(e.target);
                if (currentIndex >= 0 && currentIndex < inputsPaso.length - 1) {
                    inputsPaso[currentIndex + 1].focus();
                } else if (currentIndex === inputsPaso.length - 1) {
                    // Si es el último input del paso actual, simula clic en "Siguiente"
                    const btnSiguiente = document.getElementById(`btn-next-step-${pasoActual}`);
                    if (btnSiguiente) {
                        btnSiguiente.click();
                    }
                }
            }
        });

        // Intercepta el evento submit general del formulario
        formRegistro.addEventListener('submit', (e) => {
            if (pasoActual < TOTAL_PASOS) {
                e.preventDefault();
                const btnSiguiente = document.getElementById(`btn-next-step-${pasoActual}`);
                if (btnSiguiente) {
                    btnSiguiente.click();
                }
            } else {
                for (let i = 1; i <= 3; i++) {
                    if (!validarPaso(i)) {
                        e.preventDefault();
                        irAPaso(i);
                        return;
                    }
                }
            }
        });
    }

    // 9. Sincronización con respuestas HTMX:
    // Cuando el backend valida de forma asíncrona (ej. DNI duplicado), añade estilos rojos o verdes según corresponda
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

// Inicialización del asistente al completarse el DOM
document.addEventListener('DOMContentLoaded', () => {
    configurarRestriccionFechaNacimiento();
    inicializarEventosRegistro();
    irAPaso(1);
});
