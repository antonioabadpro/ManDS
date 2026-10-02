/**
 * layout.js
 * JavaScript transversal para la interfaz general de ManDS ERP.
 * Compartido por todos los roles (Administrador, Profesor, Alumno).
 *
 * Funcionalidades transversales:
 * 1. Persistencia y control del Modo Oscuro (Tailwind 'class' mode).
 * 2. Reloj del sistema en tiempo real en la barra superior (topbar).
 * 3. Menú desplegable accesible de usuario (perfil y cierre de sesión).
 * 4. Gestor universal y accesible de modales (WAI-ARIA, light-dismiss, foco automático).
 * 5. Control de apertura/cierre del cajón móvil (drawer) y tecla Escape.
 */

document.addEventListener('DOMContentLoaded', () => {
    'use strict';

    // =========================================================================
    // 1. MODO OSCURO (PERSISTENCIA Y CONTROL)
    // =========================================================================
    const TEMA_ALMACENADO = 'mands_theme';

    function sincronizarIconosTema(esOscuro) {
        const themeIconSun = document.getElementById('theme-icon-sun');
        const themeIconMoon = document.getElementById('theme-icon-moon');
        const themeToggleLabel = document.getElementById('theme-toggle-label');
        const themeToggleStatus = document.getElementById('theme-toggle-status');

        if (themeIconSun && themeIconMoon) {
            if (esOscuro) {
                themeIconSun.classList.remove('hidden');
                themeIconMoon.classList.add('hidden');
            } else {
                themeIconSun.classList.add('hidden');
                themeIconMoon.classList.remove('hidden');
            }
        }
        if (themeToggleLabel) {
            themeToggleLabel.textContent = esOscuro ? 'Modo Claro' : 'Modo Oscuro';
        }
        if (themeToggleStatus) {
            themeToggleStatus.textContent = esOscuro ? 'Oscuro' : 'Claro';
        }
    }

    function aplicarTema(tema) {
        const esOscuro = tema === 'dark';
        if (esOscuro) {
            document.documentElement.classList.add('dark');
        } else {
            document.documentElement.classList.remove('dark');
        }
        try {
            localStorage.setItem(TEMA_ALMACENADO, tema);
        } catch (e) {
            console.warn('No se pudo guardar la preferencia de tema en localStorage:', e);
        }
        sincronizarIconosTema(esOscuro);
    }

    function sincronizarTemaInicial() {
        let temaGuardado = null;
        try {
            temaGuardado = localStorage.getItem(TEMA_ALMACENADO);
        } catch (e) {
            // Ignorar error de acceso a localStorage
        }
        const prefiereOscuro = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
        const esOscuro = temaGuardado === 'dark' || (!temaGuardado && prefiereOscuro);

        // Asegurar que tanto el DOM como los iconos estén en perfecta sincronía
        aplicarTema(esOscuro ? 'dark' : 'light');

        // Escuchar cambios en la preferencia del sistema operativo solo si el usuario no ha forzado un tema
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

    // Event delegation para alternar tema (soporta swaps dinámicos de HTMX)
    document.addEventListener('click', (e) => {
        const btnThemeToggle = e.target.closest('#btn-theme-toggle');
        if (btnThemeToggle) {
            const actualmenteOscuro = document.documentElement.classList.contains('dark');
            aplicarTema(actualmenteOscuro ? 'light' : 'dark');
        }
    });

    // Re-sincronizar iconos y scroll del body si HTMX intercambia fragmentos del DOM
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
    // 2. RELOJ DEL SISTEMA EN TIEMPO REAL (SOPORTE 2 LÍNEAS Y RETROCOMPATIBILIDAD)
    // =========================================================================
    function iniciarRelojSistema() {
        const relojBadge = document.getElementById('reloj-sistema-badge');
        const relojTexto = document.getElementById('reloj-sistema-texto');
        const relojFecha = document.getElementById('reloj-sistema-fecha');
        const relojHora = document.getElementById('reloj-sistema-hora');

        if (!relojBadge && !relojTexto && !relojFecha && !relojHora) return;

        function actualizarReloj() {
            const ahora = new Date();
            const elFecha = document.getElementById('reloj-sistema-fecha');
            const elHora = document.getElementById('reloj-sistema-hora');
            const elTexto = document.getElementById('reloj-sistema-texto');

            if (elFecha && elHora) {
                // Formato Fecha: ej. "Dom, 20 sept"
                const fechaFormateada = new Intl.DateTimeFormat('es-ES', {
                    weekday: 'short',
                    day: 'numeric',
                    month: 'short'
                }).format(ahora);

                // Formato Hora: ej. "19:22"
                const horaFormateada = new Intl.DateTimeFormat('es-ES', {
                    hour: '2-digit',
                    minute: '2-digit'
                }).format(ahora);

                // Capitalizar primera letra de la fecha (ej. "dom., 20 sept." -> "Dom, 20 sept.")
                elFecha.textContent = fechaFormateada.charAt(0).toUpperCase() + fechaFormateada.slice(1);
                elHora.textContent = horaFormateada;
            }
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
        setInterval(actualizarReloj, 30000);

        // Re-sincronizar reloj si HTMX intercambia fragmentos del DOM
        document.addEventListener('htmx:afterSwap', actualizarReloj);
    }
    iniciarRelojSistema();

    // =========================================================================
    // 3. MENÚ DESPLEGABLE DE USUARIO (TOP BAR DROPDOWN ACCESIBLE)
    // =========================================================================
    function abrirMenuUsuario() {
        const userMenuDropdown = document.getElementById('user-menu-dropdown');
        const btnUserMenu = document.getElementById('btn-user-menu');
        const userMenuChevron = document.getElementById('user-menu-chevron');
        if (!userMenuDropdown) return;

        userMenuDropdown.classList.remove('hidden');
        requestAnimationFrame(() => {
            userMenuDropdown.classList.remove('opacity-0', 'scale-95');
            userMenuDropdown.classList.add('opacity-100', 'scale-100');
        });
        if (btnUserMenu) {
            btnUserMenu.setAttribute('aria-expanded', 'true');
        }
        if (userMenuChevron) {
            userMenuChevron.classList.add('rotate-180');
        }
    }

    function cerrarMenuUsuario() {
        const userMenuDropdown = document.getElementById('user-menu-dropdown');
        const btnUserMenu = document.getElementById('btn-user-menu');
        const userMenuChevron = document.getElementById('user-menu-chevron');
        if (!userMenuDropdown || userMenuDropdown.classList.contains('hidden')) return;

        userMenuDropdown.classList.remove('opacity-100', 'scale-100');
        userMenuDropdown.classList.add('opacity-0', 'scale-95');
        if (btnUserMenu) {
            btnUserMenu.setAttribute('aria-expanded', 'false');
        }
        if (userMenuChevron) {
            userMenuChevron.classList.remove('rotate-180');
        }
        setTimeout(() => {
            if (userMenuDropdown.classList.contains('opacity-0')) {
                userMenuDropdown.classList.add('hidden');
            }
        }, 150);
    }

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

    document.addEventListener('click', (e) => {
        const btnUserMenu = e.target.closest('#btn-user-menu');
        if (btnUserMenu) {
            e.stopPropagation();
            alternarMenuUsuario();
            return;
        }

        const userMenuContainer = document.getElementById('user-menu-container');
        if (userMenuContainer && !userMenuContainer.contains(e.target)) {
            cerrarMenuUsuario();
        }
    });

    // =========================================================================
    // 4. CAJÓN LATERAL MÓVIL (MOBILE DRAWER)
    // =========================================================================
    function getSidebar() {
        return document.querySelector('aside[aria-label]') || document.getElementById('admin-sidebar');
    }

    function getSidebarBackdrop() {
        return document.getElementById('sidebar-backdrop');
    }

    function abrirSidebarMovil() {
        const sidebar = getSidebar();
        const sidebarBackdrop = getSidebarBackdrop();
        if (!sidebar || !sidebarBackdrop) return;

        sidebarBackdrop.classList.remove('hidden');
        requestAnimationFrame(() => {
            sidebarBackdrop.classList.remove('opacity-0', 'pointer-events-none');
            sidebarBackdrop.classList.add('opacity-100', 'pointer-events-auto');
        });

        sidebar.classList.remove('-translate-x-full');
        sidebar.classList.add('translate-x-0');
        document.body.classList.add('overflow-hidden');
    }

    function cerrarSidebarMovil() {
        const sidebar = getSidebar();
        const sidebarBackdrop = getSidebarBackdrop();
        if (!sidebar || !sidebarBackdrop) return;

        sidebarBackdrop.classList.remove('opacity-100', 'pointer-events-auto');
        sidebarBackdrop.classList.add('opacity-0', 'pointer-events-none');

        sidebar.classList.remove('translate-x-0');
        sidebar.classList.add('-translate-x-full');
        document.body.classList.remove('overflow-hidden');

        setTimeout(() => {
            if (sidebarBackdrop.classList.contains('opacity-0')) {
                sidebarBackdrop.classList.add('hidden');
            }
        }, 300);
    }

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

    document.addEventListener('click', (e) => {
        const btnToggleSidebar = e.target.closest('#btn-toggle-sidebar');
        if (btnToggleSidebar && window.innerWidth < 1024) {
            alternarSidebarMovil();
            return;
        }

        const btnCerrarSidebarMovil = e.target.closest('#btn-cerrar-sidebar-movil');
        if (btnCerrarSidebarMovil) {
            cerrarSidebarMovil();
            return;
        }

        const backdrop = e.target.closest('#sidebar-backdrop');
        if (backdrop) {
            cerrarSidebarMovil();
        }
    });

    // =========================================================================
    // 5. GESTOR ACCESIBLE DE MODALES (DECLARATIVO POR ATRIBUTOS DE DATOS)
    // =========================================================================
    function abrirModal(modalId) {
        const modal = document.getElementById(modalId);
        if (!modal) return;

        // Si el menú móvil estaba abierto, cerrarlo
        cerrarSidebarMovil();

        modal.classList.remove('hidden');
        modal.classList.add('flex');
        document.body.classList.add('overflow-hidden');

        // Enfocar el primer input o botón interactivo
        const primerElemento = modal.querySelector('input:not([type="hidden"]), select, textarea, button:not([data-modal-close])');
        if (primerElemento) {
            setTimeout(() => primerElemento.focus(), 80);
        }
    }

    function cerrarModal(modalId) {
        const modal = document.getElementById(modalId);
        if (!modal) return;

        modal.classList.add('hidden');
        modal.classList.remove('flex');
        document.body.classList.remove('overflow-hidden');

        // Resetear formulario si existe dentro
        const form = modal.querySelector('form');
        if (form && !modal.dataset.preserveForm) {
            form.reset();
        }

        // Limpiar errores visuales si los hubiera
        const inputsConError = modal.querySelectorAll('.border-red-500');
        inputsConError.forEach(input => {
            input.classList.remove('border-red-500', 'focus:ring-red-500', 'dark:border-red-500');
            input.classList.add('border-slate-300', 'dark:border-slate-700', 'focus:ring-blue-600');
            input.setCustomValidity('');
        });
        const mensajesError = modal.querySelectorAll('.mensaje-error-campo');
        mensajesError.forEach(msg => msg.remove());

        // Si el modal está contenido en un contenedor dinámico HTMX, limpiar el contenedor
        const contenedorDinamico = modal.closest('#contenedor-modal');
        if (contenedorDinamico) {
            contenedorDinamico.innerHTML = '';
        }
    }

    document.addEventListener('click', (event) => {
        const triggerAbrir = event.target.closest('[data-modal-target]');
        if (triggerAbrir) {
            event.preventDefault();
            const modalId = triggerAbrir.getAttribute('data-modal-target');
            abrirModal(modalId);
            return;
        }

        const triggerCerrar = event.target.closest('[data-modal-close]');
        if (triggerCerrar) {
            event.preventDefault();
            const modalId = triggerCerrar.getAttribute('data-modal-close');
            cerrarModal(modalId);
            return;
        }

        // Cierre por clic en el fondo del modal (Light-Dismiss)
        if (event.target.classList.contains('modal-backdrop')) {
            const modalPadre = event.target.closest('[role="dialog"]');
            if (modalPadre && modalPadre.id) {
                cerrarModal(modalPadre.id);
            }
        }
    });

    // Bloquear scroll si un modal viene abierto desde el servidor (errores de validación)
    if (document.querySelector('[role="dialog"].flex')) {
        document.body.classList.add('overflow-hidden');
    }

    // =========================================================================
    // 6. ACCESIBILIDAD POR TECLADO (TECLA ESCAPE)
    // =========================================================================
    document.addEventListener('keydown', (event) => {
        if (event.key === 'Escape') {
            // 1. Cerrar cualquier modal visible
            const modalesAbiertos = document.querySelectorAll('[role="dialog"]:not(.hidden)');
            if (modalesAbiertos.length > 0) {
                const ultimoModal = modalesAbiertos[modalesAbiertos.length - 1];
                cerrarModal(ultimoModal.id);
                return;
            }

            // 2. Cerrar el menú de usuario si está desplegado
            cerrarMenuUsuario();

            // 3. Cerrar el cajón en móvil si está abierto
            const sidebar = getSidebar();
            if (sidebar && sidebar.classList.contains('translate-x-0')) {
                cerrarSidebarMovil();
            }
        }
    });

    // Sincronización al redimensionar ventana
    window.addEventListener('resize', () => {
        const sidebar = getSidebar();
        if (window.innerWidth >= 1024 && sidebar && sidebar.classList.contains('translate-x-0')) {
            cerrarSidebarMovil();
        }
    });
});

