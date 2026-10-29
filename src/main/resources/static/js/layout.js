/**
 * layout.js - JavaScript transversal para la interfaz general de ManDS ERP
 *
 * =====================================================================================
 * Este archivo centraliza toda la interactividad común de la aplicación, compartida
 * por los paneles de Administrador, Profesor y Alumno. Al estar desacoplado de las vistas
 * HTML, garantiza el cumplimiento de Content Security Policy (CSP) sin eventos inline.
 *
 * Módulos y funcionalidades transversales:
 * 1. MODO OSCURO: Persistencia en 'localStorage', sincronización con el sistema operativo
 *    y compatibilidad con reemplazos dinámicos del DOM realizados por HTMX.
 * 2. RELOJ EN TIEMPO REAL: Formateo nativo en español con 'Intl.DateTimeFormat' y
 *    actualización periódica en la barra superior (topbar).
 * 3. MENÚ DE USUARIO ACCESIBLE: Control de apertura/cierre, transiciones CSS fluidas con
 *    'requestAnimationFrame', atributos WAI-ARIA ('aria-expanded') y cierre por clic fuera.
 * 4. CAJÓN LATERAL MÓVIL (DRAWER): Menú responsive para smartphones y tablets con
 *    desenfoque de fondo (backdrop) y bloqueo de scroll en el body.
 * 5. GESTOR UNIVERSAL DE MODALES: Control declarativo mediante atributos HTML ('data-modal-target',
 *    'data-modal-close'), cierre al pulsar el fondo (light-dismiss), foco automático accesible
 *    y limpieza de formularios.
 * 6. ACCESIBILIDAD POR TECLADO (TECLA ESCAPE): Cierre jerárquico accesible respetando
 *    las directrices internacionales WAI-ARIA.
 * =====================================================================================
 */

document.addEventListener('DOMContentLoaded', () => {
    'use strict';

    // =========================================================================
    // 1. MODO OSCURO (PERSISTENCIA Y CONTROL)
    // =========================================================================
    // Clave unificada en el navegador para almacenar 'dark' o 'light'
    const TEMA_ALMACENADO = 'mands_theme';

    /**
     * Actualiza la visibilidad de los iconos de Sol y Luna y las etiquetas de texto
     * en función de si el modo oscuro está activo o no.
     * @param {boolean} esOscuro - True si el modo oscuro está activo.
     */
    function sincronizarIconosTema(esOscuro) {
        const themeIconSun = document.getElementById('theme-icon-sun');
        const themeIconMoon = document.getElementById('theme-icon-moon');
        const themeToggleLabel = document.getElementById('theme-toggle-label');
        const themeToggleStatus = document.getElementById('theme-toggle-status');

        // Si los iconos existen en la cabecera actual:
        if (themeIconSun && themeIconMoon) {
            if (esOscuro) {
                // En modo oscuro: se muestra el icono del Sol (para invitar a cambiar a claro)
                themeIconSun.classList.remove('hidden');
                themeIconMoon.classList.add('hidden');
            } else {
                // En modo claro: se muestra el icono de la Luna (para invitar a cambiar a oscuro)
                themeIconSun.classList.add('hidden');
                themeIconMoon.classList.remove('hidden');
            }
        }

        // Actualización de textos de apoyo visual y accesibilidad
        if (themeToggleLabel) {
            themeToggleLabel.textContent = esOscuro ? 'Modo Claro' : 'Modo Oscuro';
        }
        if (themeToggleStatus) {
            themeToggleStatus.textContent = esOscuro ? 'Oscuro' : 'Claro';
        }
    }

    /**
     * Aplica el tema visual indicado, actualizando la clase 'dark' en <html>,
     * persistiendo la elección en localStorage y actualizando los iconos.
     * @param {'dark' | 'light'} tema - Tema a aplicar.
     */
    function aplicarTema(tema) {
        const esOscuro = tema === 'dark';

        // Modificación de la clase en el elemento raíz <html> para activar variantes 'dark:*' de Tailwind
        if (esOscuro) {
            document.documentElement.classList.add('dark');
        } else {
            document.documentElement.classList.remove('dark');
        }

        // Guardado seguro en localStorage (capturando excepciones si el usuario navega en incógnito estricto)
        try {
            localStorage.setItem(TEMA_ALMACENADO, tema);
        } catch (e) {
            console.warn('No se pudo guardar la preferencia de tema en localStorage:', e);
        }

        sincronizarIconosTema(esOscuro);
    }

    /**
     * Determina el tema inicial al cargar la página evaluando primero localStorage
     * y, si no existe valor previo, la preferencia del sistema operativo del usuario.
     */
    function sincronizarTemaInicial() {
        let temaGuardado = null;
        try {
            temaGuardado = localStorage.getItem(TEMA_ALMACENADO);
        } catch (e) {
            // Ignorar error si el almacenamiento local está restringido
        }

        // Evalúa si el sistema operativo prefiere tema oscuro
        const prefiereOscuro = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
        const esOscuro = temaGuardado === 'dark' || (!temaGuardado && prefiereOscuro);

        // Asegurar que tanto el DOM como los iconos estén en perfecta sincronía
        aplicarTema(esOscuro ? 'dark' : 'light');

        // Escucha cambios en tiempo real en las preferencias del Sistema Operativo
        // Solo reacciona si el usuario NO ha forzado manualmente una elección en localStorage
        if (window.matchMedia) {
            window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
                try {
                    if (!localStorage.getItem(TEMA_ALMACENADO)) {
                        aplicarTema(e.matches ? 'dark' : 'light');
                    }
                } catch (err) {
                    aplicarTema(e.matches ? 'dark' : 'light');
                }
            });
        }
    }
    sincronizarTemaInicial();

    /**
     * PATRÓN EVENT DELEGATION PARA ALTERNAR TEMA:
     * Al registrar el listener en 'document' y buscar con '.closest()', garantizamos que el
     * botón siga funcionando incluso si HTMX intercambia la barra superior dinámicamente.
     */
    document.addEventListener('click', (e) => {
        const btnThemeToggle = e.target.closest('#btn-theme-toggle');
        if (btnThemeToggle) {
            const actualmenteOscuro = document.documentElement.classList.contains('dark');
            aplicarTema(actualmenteOscuro ? 'light' : 'dark');
        }
    });

    /**
     * INTEGRACIÓN CON HTMX:
     * Tras cualquier swap de contenido (evento 'htmx:afterSwap'), volvemos a sincronizar los
     * iconos y nos aseguramos de mantener bloqueado el scroll si hay un modal abierto.
     */
    document.addEventListener('htmx:afterSwap', (event) => {
        const esOscuro = document.documentElement.classList.contains('dark');
        sincronizarIconosTema(esOscuro);

        // Bloquear scroll si el contenido intercambiado contiene o es un modal activo
        const modalActivo = document.querySelector('[role="dialog"].flex:not(.hidden)');
        if (modalActivo) {
            document.body.classList.add('overflow-hidden');
        }
    });

    // =========================================================================
    // 2. RELOJ DEL SISTEMA EN TIEMPO REAL (TOPBAR)
    // =========================================================================
    function iniciarRelojSistema() {
        const relojBadge = document.getElementById('reloj-sistema-badge');
        const relojTexto = document.getElementById('reloj-sistema-texto');
        const relojFecha = document.getElementById('reloj-sistema-fecha');
        const relojHora = document.getElementById('reloj-sistema-hora');

        // Si la página actual no dispone de reloj en la barra superior, salimos sin hacer nada
        if (!relojBadge && !relojTexto && !relojFecha && !relojHora) return;

        /**
         * Obtiene la hora actual del cliente y formatea la fecha y la hora
         * utilizando la API nativa de internacionalización 'Intl.DateTimeFormat'.
         */
        function actualizarReloj() {
            const ahora = new Date();
            const elFecha = document.getElementById('reloj-sistema-fecha');
            const elHora = document.getElementById('reloj-sistema-hora');
            const elTexto = document.getElementById('reloj-sistema-texto');

            if (elFecha && elHora) {
                // Formato Fecha: ej. "jue., 29 oct."
                const fechaFormateada = new Intl.DateTimeFormat('es-ES', {
                    weekday: 'short',
                    day: 'numeric',
                    month: 'short'
                }).format(ahora);

                // Formato Hora en formato 24 horas: ej. "20:05"
                const horaFormateada = new Intl.DateTimeFormat('es-ES', {
                    hour: '2-digit',
                    minute: '2-digit'
                }).format(ahora);

                // Capitalizar primera letra de la fecha (ej. "jue., 29 oct." -> "Jue, 29 oct.")
                elFecha.textContent = fechaFormateada.charAt(0).toUpperCase() + fechaFormateada.slice(1);
                elHora.textContent = horaFormateada;
            }

            // Retrocompatibilidad con contenedores de texto único
            if (elTexto) {
                const formateador = new Intl.DateTimeFormat('es-ES', {
                    weekday: 'short',
                    day: 'numeric',
                    month: 'short',
                    hour: '2-digit',
                    minute: '2-digit'
                });
                elTexto.textContent = formateador.format(ahora);
            }
        }

        actualizarReloj();
        // Actualiza el reloj cada 30 segundos (suficiente para minutos sin sobrecargar la CPU)
        setInterval(actualizarReloj, 30000);

        // Re-sincronizar reloj si HTMX intercambia fragmentos de la cabecera
        document.addEventListener('htmx:afterSwap', actualizarReloj);
    }
    iniciarRelojSistema();

    // =========================================================================
    // 3. MENÚ DESPLEGABLE DE USUARIO (TOP BAR DROPDOWN ACCESIBLE)
    // =========================================================================
    /**
     * Despliega el menú del usuario con animación suave de escala y opacidad,
     * actualizando los atributos WAI-ARIA para lectores de pantalla.
     */
    function abrirMenuUsuario() {
        const userMenuDropdown = document.getElementById('user-menu-dropdown');
        const btnUserMenu = document.getElementById('btn-user-menu');
        const userMenuChevron = document.getElementById('user-menu-chevron');
        if (!userMenuDropdown) return;

        userMenuDropdown.classList.remove('hidden');

        // 'requestAnimationFrame' sincroniza la animación con el siguiente repintado del navegador
        requestAnimationFrame(() => {
            userMenuDropdown.classList.remove('opacity-0', 'scale-95');
            userMenuDropdown.classList.add('opacity-100', 'scale-100');
        });

        // Accesibilidad WAI-ARIA: informa a tecnologías de asistencia de que el menú está desplegado
        if (btnUserMenu) {
            btnUserMenu.setAttribute('aria-expanded', 'true');
        }
        // Rota 180 grados la flecha del botón
        if (userMenuChevron) {
            userMenuChevron.classList.add('rotate-180');
        }
    }

    /**
     * Oculta el menú del usuario de forma animada y restablece sus estados de accesibilidad.
     */
    function cerrarMenuUsuario() {
        const userMenuDropdown = document.getElementById('user-menu-dropdown');
        const btnUserMenu = document.getElementById('btn-user-menu');
        const userMenuChevron = document.getElementById('user-menu-chevron');
        if (!userMenuDropdown || userMenuDropdown.classList.contains('hidden')) return;

        // Inicia la transición de desvanecimiento y reducción
        userMenuDropdown.classList.remove('opacity-100', 'scale-100');
        userMenuDropdown.classList.add('opacity-0', 'scale-95');

        if (btnUserMenu) {
            btnUserMenu.setAttribute('aria-expanded', 'false');
        }
        if (userMenuChevron) {
            userMenuChevron.classList.remove('rotate-180');
        }

        // Espera a que termine la transición CSS (150ms) antes de aplicar 'hidden'
        setTimeout(() => {
            if (userMenuDropdown.classList.contains('opacity-0')) {
                userMenuDropdown.classList.add('hidden');
            }
        }, 150);
    }

    /**
     * Alterna el estado del menú desplegable de usuario (abrir si está cerrado, cerrar si está abierto).
     */
    function alternarMenuUsuario() {
        const userMenuDropdown = document.getElementById('user-menu-dropdown');
        if (!userMenuDropdown) return;
        const estaAbierto = !userMenuDropdown.classList.contains('hidden');
        if (estaAbierto) {
            cerrarMenuUsuario();
        } else {
            abrirMenuUsuario();
        }
    }

    // Escucha global de clics para controlar el menú y el cierre al hacer clic fuera (Light-Dismiss)
    document.addEventListener('click', (e) => {
        // Clic en el botón disparador del menú
        const btnUserMenu = e.target.closest('#btn-user-menu');
        if (btnUserMenu) {
            e.stopPropagation(); // Evita que el evento se propague al documento
            alternarMenuUsuario();
            return;
        }

        // Clic fuera del contenedor del menú: si el menú estaba abierto, lo cerramos
        const userMenuContainer = document.getElementById('user-menu-container');
        if (userMenuContainer && !userMenuContainer.contains(e.target)) {
            cerrarMenuUsuario();
        }
    });

    // =========================================================================
    // 4. CAJÓN LATERAL MÓVIL (MOBILE DRAWER)
    // =========================================================================
    /**
     * Obtiene el elemento del menú lateral (soporta tanto 'aside[aria-label]' como '#admin-sidebar').
     */
    function getSidebar() {
        return document.querySelector('aside[aria-label]') || document.getElementById('admin-sidebar');
    }

    /**
     * Obtiene el fondo oscuro semitransparente (backdrop) del menú móvil.
     */
    function getSidebarBackdrop() {
        return document.getElementById('sidebar-backdrop');
    }

    /**
     * Abre el menú lateral en dispositivos móviles deslizando el panel desde la izquierda.
     */
    function abrirSidebarMovil() {
        const sidebar = getSidebar();
        const sidebarBackdrop = getSidebarBackdrop();
        if (!sidebar || !sidebarBackdrop) return;

        // Mostrar backdrop y aplicar transición de opacidad
        sidebarBackdrop.classList.remove('hidden');
        requestAnimationFrame(() => {
            sidebarBackdrop.classList.remove('opacity-0', 'pointer-events-none');
            sidebarBackdrop.classList.add('opacity-100', 'pointer-events-auto');
        });

        // Deslizar menú lateral hacia adentro de la pantalla mediante transformaciones CSS
        sidebar.classList.remove('-translate-x-full');
        sidebar.classList.add('translate-x-0');

        // Bloquear el scroll de la página de fondo mientras el menú esté visible
        document.body.classList.add('overflow-hidden');
    }

    /**
     * Cierra el menú lateral móvil y restaura el scroll de la página.
     */
    function cerrarSidebarMovil() {
        const sidebar = getSidebar();
        const sidebarBackdrop = getSidebarBackdrop();
        if (!sidebar || !sidebarBackdrop) return;

        // Desvanecer el fondo
        sidebarBackdrop.classList.remove('opacity-100', 'pointer-events-auto');
        sidebarBackdrop.classList.add('opacity-0', 'pointer-events-none');

        // Deslizar el menú hacia fuera de la pantalla
        sidebar.classList.remove('translate-x-0');
        sidebar.classList.add('-translate-x-full');

        // Restaurar el scroll del body
        document.body.classList.remove('overflow-hidden');

        // Ocultar completamente el backdrop tras terminar la transición CSS (300ms)
        setTimeout(() => {
            if (sidebarBackdrop.classList.contains('opacity-0')) {
                sidebarBackdrop.classList.add('hidden');
            }
        }, 300);
    }

    /**
     * Alterna la visibilidad del menú lateral móvil según su estado actual.
     */
    function alternarSidebarMovil() {
        const sidebar = getSidebar();
        if (!sidebar) return;
        const estaAbierto = sidebar.classList.contains('translate-x-0');
        if (estaAbierto) {
            cerrarSidebarMovil();
        } else {
            abrirSidebarMovil();
        }
    }

    // Delegación de eventos de clic para botones del menú móvil y el backdrop
    document.addEventListener('click', (e) => {
        // Botón hamburguesa (solo en pantallas móviles < 1024px)
        const btnToggleSidebar = e.target.closest('#btn-toggle-sidebar');
        if (btnToggleSidebar && window.innerWidth < 1024) {
            alternarSidebarMovil();
            return;
        }

        // Botón de aspa "X" para cerrar el menú móvil
        const btnCerrarSidebarMovil = e.target.closest('#btn-cerrar-sidebar-movil');
        if (btnCerrarSidebarMovil) {
            cerrarSidebarMovil();
            return;
        }

        // Clic en el fondo oscuro translúcido (cierra el menú al tocar fuera)
        const backdrop = e.target.closest('#sidebar-backdrop');
        if (backdrop) {
            cerrarSidebarMovil();
        }
    });

    // =========================================================================
    // 5. GESTOR ACCESIBLE DE MODALES (DECLARATIVO POR ATRIBUTOS DE DATOS)
    // =========================================================================
    /**
     * Muestra un modal por su ID, bloquea el scroll del fondo y posiciona el foco
     * en el primer elemento interactivo para cumplir directrices de accesibilidad.
     * @param {string} modalId - ID del elemento dialog/modal en el DOM.
     */
    function abrirModal(modalId) {
        const modal = document.getElementById(modalId);
        if (!modal) return;

        // Si el menú móvil estaba abierto, lo cerramos previamente para evitar colisión de capas
        cerrarSidebarMovil();

        // Muestra el modal con display 'flex' centrado
        modal.classList.remove('hidden');
        modal.classList.add('flex');

        // Bloquea el scroll de la página inferior
        document.body.classList.add('overflow-hidden');

        // Accesibilidad WAI-ARIA: traslada el foco del teclado al primer input, textarea o botón
        const primerElemento = modal.querySelector('input:not([type="hidden"]), select, textarea, button:not([data-modal-close])');
        if (primerElemento) {
            setTimeout(() => primerElemento.focus(), 80);
        }
    }

    /**
     * Oculta un modal por su ID, restaura el scroll del body, resetea el formulario
     * interno y limpia el contenedor si se trataba de un modal inyectado por HTMX.
     * @param {string} modalId - ID del modal a cerrar.
     */
    function cerrarModal(modalId) {
        const modal = document.getElementById(modalId);
        if (!modal) return;

        // Ocultar modal
        modal.classList.add('hidden');
        modal.classList.remove('flex');

        // Restaurar el scroll del documento
        document.body.classList.remove('overflow-hidden');

        // Resetear el formulario interior a menos que el modal especifique 'data-preserve-form'
        const form = modal.querySelector('form');
        if (form && !modal.dataset.preserveForm) {
            form.reset();
        }

        // Limpiar clases visuales de error de validación previa
        const inputsConError = modal.querySelectorAll('.border-red-500');
        inputsConError.forEach(input => {
            input.classList.remove('border-red-500', 'focus:ring-red-500', 'dark:border-red-500');
            input.classList.add('border-slate-300', 'dark:border-slate-700', 'focus:ring-blue-600');
            input.setCustomValidity('');
        });
        const mensajesError = modal.querySelectorAll('.mensaje-error-campo');
        mensajesError.forEach(msg => msg.remove());

        // Si el modal pertenece a un contenedor dinámico cargado por HTMX, limpiar su contenido HTML
        // para que no queden elementos duplicados u obsoletos en el DOM
        const contenedorDinamico = modal.closest('[id^="contenedor-modal-"]');
        if (contenedorDinamico && contenedorDinamico.id !== 'contenedor-modal-alta-profesor' && contenedorDinamico.id !== 'contenedor-modal-alta-vehiculo' && contenedorDinamico.id !== 'contenedor-modal-reportar-incidencia') {
            setTimeout(() => {
                if (modal.classList.contains('hidden')) {
                    contenedorDinamico.innerHTML = '';
                }
            }, 100);
        } else {
            const contenedorLegacy = modal.closest('#contenedor-modal');
            if (contenedorLegacy) {
                contenedorLegacy.innerHTML = '';
            }
        }
    }

    /**
     * GESTIÓN DECLARATIVA DE MODALES MEDIANTE EVENT DELEGATION:
     * En lugar de enlazar listeners a cada botón, escuchamos en 'document' los atributos:
     * - 'data-modal-target="idModal"': Abre el modal indicado.
     * - 'data-modal-close="idModal"': Cierra el modal indicado.
     * - Clic en clase '.modal-backdrop': Cierra el modal al pulsar el fondo (Light-Dismiss).
     */
    document.addEventListener('click', (event) => {
        // Disparador de apertura
        const triggerAbrir = event.target.closest('[data-modal-target]');
        if (triggerAbrir) {
            event.preventDefault();
            const modalId = triggerAbrir.getAttribute('data-modal-target');
            abrirModal(modalId);
            return;
        }

        // Disparador de cierre
        const triggerCerrar = event.target.closest('[data-modal-close]');
        if (triggerCerrar) {
            const modalId = triggerCerrar.getAttribute('data-modal-close');
            cerrarModal(modalId);

            // Solo prevenir default si no es un disparador de petición HTMX (para no interrumpir 'hx-post' o 'hx-get')
            if (!triggerCerrar.hasAttribute('hx-get') && !triggerCerrar.hasAttribute('hx-post')) {
                event.preventDefault();
            }
            return;
        }

        // Cierre por clic en el fondo semitransparente del modal (Light-Dismiss)
        if (event.target.classList.contains('modal-backdrop')) {
            const modalPadre = event.target.closest('[role="dialog"]');
            if (modalPadre && modalPadre.id) {
                cerrarModal(modalPadre.id);
            }
        }
    });

    // Bloquear scroll si un modal ya viene abierto directamente desde el servidor (ej. renderizado tras fallo de validación)
    if (document.querySelector('[role="dialog"].flex')) {
        document.body.classList.add('overflow-hidden');
    }

    // =========================================================================
    // 6. ACCESIBILIDAD POR TECLADO (TECLA ESCAPE)
    // =========================================================================
    // Cumple con el estándar WAI-ARIA permitiendo cerrar capas superpuestas con Escape
    document.addEventListener('keydown', (event) => {
        if (event.key === 'Escape') {
            // 1. Si hay un modal visible, cerrar el último abierto
            const modalesAbiertos = document.querySelectorAll('[role="dialog"]:not(.hidden)');
            if (modalesAbiertos.length > 0) {
                const ultimoModal = modalesAbiertos[modalesAbiertos.length - 1];
                cerrarModal(ultimoModal.id);
                return;
            }

            // 2. Si no hay modales pero el menú de usuario está desplegado, cerrarlo
            cerrarMenuUsuario();

            // 3. Si el cajón lateral móvil está abierto, cerrarlo
            const sidebar = getSidebar();
            if (sidebar && sidebar.classList.contains('translate-x-0')) {
                cerrarSidebarMovil();
            }
        }
    });

    // Sincronización al redimensionar la ventana:
    // Si el usuario amplía la pantalla a tamaño escritorio (>= 1024px), cerrar el cajón móvil
    window.addEventListener('resize', () => {
        const sidebar = getSidebar();
        if (window.innerWidth >= 1024 && sidebar && sidebar.classList.contains('translate-x-0')) {
            cerrarSidebarMovil();
        }
    });
});
