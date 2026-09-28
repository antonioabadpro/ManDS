// Elementos DOM
const formPago = document.getElementById('form-pago-checkout');
const inputHolder = document.getElementById('card-holder');
const inputNumber = document.getElementById('card-number');
const inputExpiry = document.getElementById('card-expiry');
const inputCvv = document.getElementById('card-cvv');

const previewHolder = document.getElementById('card-preview-name');
const previewNumber = document.getElementById('card-preview-number');
const previewExpiry = document.getElementById('card-preview-expiry');
const brandDisplay = document.getElementById('card-brand-display');

/**
 * Muestra el error visual en el input con recuadro rojo y el mensaje debajo en su feedback container.
 */
function mostrarErrorCampo(inputId, feedbackId, mensaje) {
    const input = document.getElementById(inputId);
    const feedback = document.getElementById(feedbackId);

    if (input) {
        input.classList.add('border-red-500', 'focus:ring-red-500', 'dark:border-red-500');
        input.classList.remove('border-slate-300', 'dark:border-slate-700', 'focus:ring-blue-600');
        input.setCustomValidity(mensaje);
    }

    if (feedback) {
        feedback.innerHTML = `
            <div class="mensaje-error-campo flex items-center space-x-1.5 text-xs text-red-600 dark:text-red-400 mt-1 font-medium fade-in">
                <svg class="w-3.5 h-3.5 shrink-0 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                </svg>
                <span>${mensaje}</span>
            </div>
        `;
    }
}

/**
 * Restablece el estilo estándar del input eliminando el recuadro rojo y vacía el contenedor de feedback.
 */
function limpiarErrorCampo(inputId, feedbackId) {
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

/**
 * Comprueba si el campo tiene un error activo en el DOM.
 */
function tieneErrorActivo(inputId) {
    const input = document.getElementById(inputId);
    return input ? input.classList.contains('border-red-500') : false;
}

/**
 * Valida el nombre del titular de la tarjeta.
 */
function validarTitular() {
    if (!inputHolder) return true;
    const valor = inputHolder.value.trim();
    if (!valor) {
        mostrarErrorCampo('card-holder', 'feedback-card-holder', 'El nombre del titular es obligatorio.');
        return false;
    }
    const regexTitular = /^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\s'-]{3,50}$/;
    if (!regexTitular.test(valor)) {
        mostrarErrorCampo('card-holder', 'feedback-card-holder', 'Introduce un nombre válido (solo letras, mínimo 3 caracteres).');
        return false;
    }
    limpiarErrorCampo('card-holder', 'feedback-card-holder');
    return true;
}

/**
 * Valida que el número de tarjeta contenga exactamente 16 dígitos numéricos.
 */
function validarNumeroTarjeta() {
    if (!inputNumber) return true;
    const rawNumber = inputNumber.value.replace(/\s/g, '');
    if (!rawNumber) {
        mostrarErrorCampo('card-number', 'feedback-card-number', 'El número de tarjeta es obligatorio.');
        return false;
    }
    if (!/^\d{16}$/.test(rawNumber)) {
        mostrarErrorCampo('card-number', 'feedback-card-number', 'Introduce un número de tarjeta válido (16 dígitos).');
        return false;
    }
    limpiarErrorCampo('card-number', 'feedback-card-number');
    return true;
}

/**
 * Comprueba que el formato de caducidad sea MM/AA y no esté en el pasado.
 */
function esFechaValidaMMYY(fecha) {
    const regex = /^(0[1-9]|1[0-2])\/\d{2}$/;
    if (!regex.test(fecha)) return false;

    const [mes, anio] = fecha.split('/').map(Number);
    const ahora = new Date();
    const anioActual = ahora.getFullYear() % 100;
    const mesActual = ahora.getMonth() + 1;

    return (anio > anioActual) || (anio === anioActual && mes >= mesActual);
}

/**
 * Valida la fecha de caducidad de la tarjeta.
 */
function validarCaducidad() {
    if (!inputExpiry) return true;
    const valor = inputExpiry.value.trim();
    if (!valor) {
        mostrarErrorCampo('card-expiry', 'feedback-card-expiry', 'La fecha de caducidad es obligatoria.');
        return false;
    }
    if (!esFechaValidaMMYY(valor)) {
        mostrarErrorCampo('card-expiry', 'feedback-card-expiry', 'Fecha inválida. Usa formato MM/AA (no caducada).');
        return false;
    }
    limpiarErrorCampo('card-expiry', 'feedback-card-expiry');
    return true;
}

/**
 * Valida el código de seguridad CVV / CVC (3 dígitos numéricos).
 */
function validarCvv() {
    if (!inputCvv) return true;
    const valor = inputCvv.value.trim();
    if (!valor) {
        mostrarErrorCampo('card-cvv', 'feedback-card-cvv', 'El código CVV es obligatorio.');
        return false;
    }
    if (!/^\d{3}$/.test(valor)) {
        mostrarErrorCampo('card-cvv', 'feedback-card-cvv', 'Introduce un código CVV válido (3 dígitos).');
        return false;
    }
    limpiarErrorCampo('card-cvv', 'feedback-card-cvv');
    return true;
}

/**
 * Valida de forma integral todos los campos del formulario de pago.
 */
function validarFormularioPago() {
    const esValidoTitular = validarTitular();
    const esValidoNumero = validarNumeroTarjeta();
    const esValidaCaducidad = validarCaducidad();
    const esValidoCvv = validarCvv();

    const todoValido = esValidoTitular && esValidoNumero && esValidaCaducidad && esValidoCvv;

    if (!todoValido) {
        if (!esValidoTitular) {
            inputHolder.focus();
        } else if (!esValidoNumero) {
            inputNumber.focus();
        } else if (!esValidaCaducidad) {
            inputExpiry.focus();
        } else if (!esValidoCvv) {
            inputCvv.focus();
        }
        return false;
    }
    return true;
}

// Formateo y reflejo en tiempo real del Número de Tarjeta (grupos de 4 dígitos)
if (inputNumber) {
    inputNumber.addEventListener('input', (e) => {
        let value = e.target.value.replace(/\D/g, '').substring(0, 16);
        let formatted = value.match(/.{1,4}/g)?.join(' ') || '';
        e.target.value = formatted;

        // Reflejo en la tarjeta de previsualización
        if (formatted.length > 0) {
            previewNumber.textContent = formatted;
        } else {
            previewNumber.textContent = '•••• •••• •••• ••••';
        }

        // Detección simple de marca (AMEX = 3, Visa = 4, Mastercard = 5)
        if (value.startsWith('3')) {
            brandDisplay.textContent = 'AMEX';
        } else if (value.startsWith('4')) {
            brandDisplay.textContent = 'VISA';
        } else if (value.startsWith('5')) {
            brandDisplay.textContent = 'MASTERCARD';
        } else {
            brandDisplay.textContent = 'TARJETA';
        }

        // Si ya tenía error visible, revalidar en caliente para limpiarlo
        if (tieneErrorActivo('card-number')) {
            validarNumeroTarjeta();
        }
    });

    inputNumber.addEventListener('blur', () => {
        if (inputNumber.value.trim().length > 0) {
            validarNumeroTarjeta();
        }
    });
}

// Reflejo en tiempo real del Nombre del Titular
if (inputHolder) {
    inputHolder.addEventListener('input', (e) => {
        const val = e.target.value.toUpperCase();
        e.target.value = val;
        previewHolder.textContent = val.trim().length > 0 ? val : 'NOMBRE Y APELLIDOS';

        if (tieneErrorActivo('card-holder')) {
            validarTitular();
        }
    });

    inputHolder.addEventListener('blur', () => {
        if (inputHolder.value.trim().length > 0) {
            validarTitular();
        }
    });
}

// Formateo de Caducidad MM/AA
if (inputExpiry) {
    inputExpiry.addEventListener('input', (e) => {
        let val = e.target.value.replace(/\D/g, '').substring(0, 4);
        if (val.length >= 2) {
            val = val.substring(0, 2) + '/' + val.substring(2);
        }
        e.target.value = val;
        previewExpiry.textContent = val.length > 0 ? val : 'MM/AA';

        if (tieneErrorActivo('card-expiry')) {
            validarCaducidad();
        }
    });

    inputExpiry.addEventListener('blur', () => {
        if (inputExpiry.value.trim().length > 0) {
            validarCaducidad();
        }
    });
}

// Formateo de CVV
if (inputCvv) {
    inputCvv.addEventListener('input', (e) => {
        e.target.value = e.target.value.replace(/\D/g, '').substring(0, 3);

        if (tieneErrorActivo('card-cvv')) {
            validarCvv();
        }
    });

    inputCvv.addEventListener('blur', () => {
        if (inputCvv.value.trim().length > 0) {
            validarCvv();
        }
    });
}

// Función para realizar el procesamiento del pago simulado
function procesarPagoSimulado() {
    const btn = document.getElementById('btn-procesar-pago');
    const spinner = document.getElementById('btn-spinner');
    const lockIcon = document.getElementById('btn-lock-icon');
    const label = document.getElementById('btn-pago-label');

    // Estado de carga
    btn.disabled = true;
    spinner.classList.remove('hidden');
    lockIcon.classList.add('hidden');
    label.textContent = 'Procesando pago seguro...';

    // Simulación de respuesta bancaria (1.2 segundos)
    setTimeout(() => {
        // Preparar datos del recibo
        const rawNumber = inputNumber.value.replace(/\s/g, '');
        const ultimos4 = rawNumber.length >= 4 ? rawNumber.substring(rawNumber.length - 4) : '4242';
        const titular = inputHolder.value || 'ALUMNO';
        const randomId = 'TXN-2026-' + Math.floor(100000 + Math.random() * 900000);
        const ahora = new Date();
        const fechaFormateada = ahora.toLocaleDateString('es-ES') + ' ' + ahora.toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' });

        document.getElementById('receipt-txn-id').textContent = randomId;
        document.getElementById('receipt-holder-name').textContent = titular;
        document.getElementById('receipt-card-masked').textContent = '•••• ' + ultimos4;
        document.getElementById('receipt-timestamp').textContent = fechaFormateada;

        // Transición a la vista de confirmación visual
        document.getElementById('checkout-view').classList.add('hidden');
        document.getElementById('confirmation-view').classList.remove('hidden');
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }, 1200);
}

// Función para cerrar la ventana segura con fallback
function cerrarVentanaSegura() {
    window.close();
    // Fallback si el navegador bloquea scripts cerrando ventanas no abiertas por window.open
    alert('Puedes cerrar esta pestaña en tu navegador.');
}

document.addEventListener('DOMContentLoaded', () => {
    // Interceptar envío del formulario para validación completa
    if (formPago) {
        formPago.addEventListener('submit', (e) => {
            e.preventDefault();
            if (validarFormularioPago()) {
                procesarPagoSimulado();
            }
        });
    }

    const btnImprimir = document.getElementById('btn-imprimir-comprobante');
    if (btnImprimir) {
        btnImprimir.addEventListener('click', () => {
            window.print();
        });
    }

    const btnCerrar = document.getElementById('btn-cerrar-ventana');
    if (btnCerrar) {
        btnCerrar.addEventListener('click', cerrarVentanaSegura);
    }
});