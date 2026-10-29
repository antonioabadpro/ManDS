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
 * 3. CONTROL DE CARGA RESILIENTE DE IMÁGENES:
 *    Escucha los eventos 'load' y 'error' de la ilustración corporativa en el hero lateral.
 *    Si la imagen carga con éxito, se muestra; si falla la conexión, se conmuta a un
 *    marcador de posición vectorial (placeholder SVG) garantizando que la UI nunca se rompa.
 *
 * 4. VALIDACIÓN PREVIA EN CLIENTE (PRE-SUBMIT):
 *    Evita peticiones innecesarias al servidor si los campos de usuario o contraseña están
 *    vacíos, enfocando el campo erróneo y mostrando el tooltip nativo del navegador.
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
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l18 18" />
            `;
        } else {
            // Icono: Ojo abierto (indica acción para mostrar)
            icon.innerHTML = `
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
            `;
        }
    }
}

/**
 * Registra todos los escuchadores de eventos para la página de login.
 */
function inicializarEventosLogin() {
    // 1. Control no intrusivo de carga de la imagen corporativa en el hero lateral
    const heroImg = document.getElementById('login-hero-img');
    const heroPlaceholder = document.getElementById('hero-vector-placeholder');

    if (heroImg && heroPlaceholder) {
        // Si la imagen carga correctamente, la hacemos visible y ocultamos el placeholder
        heroImg.addEventListener('load', () => {
            heroImg.classList.remove('hidden');
            heroPlaceholder.classList.add('hidden');
            heroPlaceholder.classList.remove('flex');
        });

        // Si la imagen falla (red caída o URL no disponible), mostramos el placeholder SVG
        heroImg.addEventListener('error', () => {
            heroImg.classList.add('hidden');
            heroPlaceholder.classList.remove('hidden');
            heroPlaceholder.classList.add('flex');
        });
    }

    // 2. Botón para ver/ocultar contraseña
    const btnOcultarLogin = document.getElementById('btn-ocultar-password-login');
    if (btnOcultarLogin) {
        btnOcultarLogin.addEventListener('click', () => {
            togglePasswordVisibility('login-password', 'eye-icon-login');
        });
    }

    // 3. Validación rápida antes de enviar para no golpear innecesariamente el backend
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
        });
    }
}

// Inicialización automática cuando el árbol DOM está completamente cargado
document.addEventListener('DOMContentLoaded', () => {
    inicializarEventosLogin();
});