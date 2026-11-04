/**
 * login.js - Lógica interactiva no intrusiva para la pantalla de inicio de sesión
 *
 * =====================================================================================
 * 1. CUMPLIMIENTO ESTRICTO DE CSP (Content Security Policy):
 *    No se utiliza ningún evento inline en el HTML ('onclick' ni 'onsubmit'). Todo se
 *    vincula dinámicamente mediante 'addEventListener' al dispararse 'DOMContentLoaded'.
 *
 * 2. ALTERNANCIA VISUAL DE CONTRASEÑA (SHOW/HIDE PASSWORD):
 *    Permite al usuario conmutar el campo entre tipo 'password' (oculto con puntos) y
 *    tipo 'text' (visible en claro). Se acompaña de actualización accesible WAI-ARIA
 *    ('aria-label' y 'aria-pressed') y reemplazo dinámico del icono SVG (ojo abierto / tachado).
 *
 * 3. VALIDACIÓN PREVIA EN CLIENTE (PRE-SUBMIT):
 *    Evita peticiones innecesarias al servidor si los campos de usuario o contraseña están
 *    vacíos, enfocando el campo erróneo y mostrando el tooltip nativo del navegador.
 *
 * 4. DESPLAZAMIENTO SUAVE HACIA EL FORMULARIO EN MÓVIL:
 *    Permite al botón del panel lateral en pantallas móviles/tablets deslizar la vista
 *    suavemente ('smooth scroll') hasta el inicio del formulario de autenticación.
 * =====================================================================================
 */

/**
 * Alterna la visibilidad de un campo de contraseña entre 'password' y 'text'.
 * @param {string} inputId - ID del elemento <input> de contraseña en el DOM.
 * @param {string} iconId - ID del contenedor SVG del botón para actualizar el gráfico.
 */
function togglePasswordVisibility(inputId, iconId) {
    const input = document.getElementById(inputId);
    const icon = document.getElementById(iconId);
    const button = document.getElementById('btn-ocultar-password-login');

    // Comprobación de seguridad: si el campo no existe en la página actual, salimos
    if (!input) return;

    // Evaluamos el tipo actual del input: si es 'password', lo pasamos a 'text'; si no, a 'password'
    const esPassword = input.type === 'password';
    input.type = esPassword ? 'text' : 'password';

    // Actualización de estado de accesibilidad WAI-ARIA para lectores de pantalla
    if (button) {
        button.setAttribute('aria-label', esPassword ? 'Ocultar contraseña' : 'Mostrar contraseña');
        button.setAttribute('aria-pressed', String(esPassword));
    }

    // Actualización de la geometría SVG:
    // Si ahora es texto visible, mostramos el ojo tachado; si está oculta, mostramos el ojo abierto
    if (icon) {
        if (esPassword) {
            // Icono: Ojo tachado (indica acción para volver a ocultar)
            icon.innerHTML = `
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3.98 8.223A10.477 10.477 0 001.934 12C3.226 16.338 7.244 19.5 12 19.5c.993 0 1.953-.138 2.863-.395M6.228 6.228A10.45 10.45 0 0112 4.5c4.756 0 8.773 3.162 10.065 7.498a10.523 10.523 0 01-4.293 5.774M6.228 6.228L3 3m3.228 3.228l3.65 3.65m7.894 7.894L21 21m-3.228-3.228l-3.65-3.65m0 0a3 3 0 10-4.243-4.243m4.242 4.242L9.88 9.88" />
            `;
        } else {
            // Icono: Ojo abierto (indica acción para mostrar)
            icon.innerHTML = `
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z" />
            `;
        }
    }
}

/**
 * Registra todos los escuchadores de eventos para la página de login.
 */
function inicializarEventosLogin() {
    // 1. Botón para ver/ocultar contraseña
    const btnOcultarLogin = document.getElementById('btn-ocultar-password-login');
    if (btnOcultarLogin) {
        btnOcultarLogin.addEventListener('click', () => {
            togglePasswordVisibility('login-password', 'eye-icon-login');
        });
    }

    // 2. Validación rápida antes de enviar para no golpear innecesariamente el backend
    // Selecciona el formulario cuyo atributo action termina en '/login'
    const formLogin = document.querySelector('form[action$="/login"]');
    if (formLogin) {
        formLogin.addEventListener('submit', (evento) => {
            const usernameInput = document.getElementById('login-username');
            const passwordInput = document.getElementById('login-password');

            // Validación de campo usuario: si está vacío o solo contiene espacios
            if (usernameInput && !usernameInput.value.trim()) {
                evento.preventDefault(); // Detiene el envío del formulario
                usernameInput.focus();   // Pone el cursor en el campo
                usernameInput.reportValidity(); // Muestra el mensaje nativo de validación HTML5
                return;
            }

            // Validación de campo contraseña
            if (passwordInput && !passwordInput.value.trim()) {
                evento.preventDefault();
                passwordInput.focus();
                passwordInput.reportValidity();
                return;
            }

            // Inhabilitar botón e iniciar spinner para prevenir doble sumisión en autenticación
            const btnSubmit = document.getElementById('btn-submit-login');
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

    // 3. Desplazamiento suave hacia el formulario de login en pantallas móviles/tablets
    const btnScrollLogin = document.getElementById('btn-scroll-login');
    if (btnScrollLogin) {
        btnScrollLogin.addEventListener('click', (evento) => {
            evento.preventDefault();
            const seccionLogin = document.getElementById('seccion-login');
            if (seccionLogin) {
                seccionLogin.scrollIntoView({ behavior: 'smooth' });
            }
        });
    }
}

// Inicialización automática cuando el árbol DOM está completamente cargado
document.addEventListener('DOMContentLoaded', () => {
    inicializarEventosLogin();
});