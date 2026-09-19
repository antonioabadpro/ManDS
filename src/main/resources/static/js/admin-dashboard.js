/**
 * admin-dashboard.js
 * Lógica cliente desacoplada y no intrusiva para el Dashboard del Administrador de ManDS.
 * Gestiona el menú lateral desplegable (drawer móvil con backdrop blur y colapso en PC),
 * el modal accesible para dar de alta a nuevos profesores y la interacción por teclado (Escape).
 */
document.addEventListener('DOMContentLoaded', () => {
    'use strict';

    // =========================================================================
    // 1. SELECTORES DE ELEMENTOS DEL DOM
    // =========================================================================
    // Menú lateral y backdrop
    const sidebar = document.getElementById('admin-sidebar');
    const sidebarBackdrop = document.getElementById('sidebar-backdrop');
    const btnAbrirSidebarMovil = document.getElementById('btn-abrir-sidebar-movil');
    const btnCerrarSidebarMovil = document.getElementById('btn-cerrar-sidebar-movil');
    const btnToggleSidebarDesktop = document.getElementById('btn-toggle-sidebar-desktop');

    // Modal Alta de Profesor
    const modalAltaProfesor = document.getElementById('modal-alta-profesor');
    const modalProfesorBackdrop = document.getElementById('modal-profesor-backdrop');
    const btnAbrirModalProfesor = document.getElementById('btn-abrir-modal-profesor');
    const btnAbrirModalProfesorMenu = document.getElementById('btn-abrir-modal-profesor-menu');
    const btnCerrarModalProfesor = document.getElementById('btn-cerrar-modal-profesor');
    const btnCancelarModalProfesor = document.getElementById('btn-cancelar-modal-profesor');
    const formAltaProfesor = document.getElementById('form-alta-profesor');
    const primerInputModal = document.getElementById('profesor-nombre');

    // =========================================================================
    // 2. CONTROL DEL MENÚ LATERAL EN MÓVIL (Off-Canvas con Backdrop Blur)
    // =========================================================================
    function abrirSidebarMovil() {
        if (!sidebar || !sidebarBackdrop) return;

        // Mostrar backdrop con efecto blur y transición de opacidad
        sidebarBackdrop.classList.remove('hidden');
        requestAnimationFrame(() => {
            sidebarBackdrop.classList.remove('opacity-0', 'pointer-events-none');
            sidebarBackdrop.classList.add('opacity-100', 'pointer-events-auto');
        });

        // Deslizar sidebar desde la izquierda
        sidebar.classList.remove('-translate-x-full');
        sidebar.classList.add('translate-x-0');

        if (btnAbrirSidebarMovil) {
            btnAbrirSidebarMovil.setAttribute('aria-expanded', 'true');
        }

        // Bloquear scroll de fondo en móvil para evitar saltos
        document.body.classList.add('overflow-hidden');
    }

    function cerrarSidebarMovil() {
        if (!sidebar || !sidebarBackdrop) return;

        // Ocultar backdrop
        sidebarBackdrop.classList.remove('opacity-100', 'pointer-events-auto');
        sidebarBackdrop.classList.add('opacity-0', 'pointer-events-none');

        // Deslizar sidebar fuera de la vista
        sidebar.classList.remove('translate-x-0');
        sidebar.classList.add('-translate-x-full');

        if (btnAbrirSidebarMovil) {
            btnAbrirSidebarMovil.setAttribute('aria-expanded', 'false');
        }

        // Restaurar scroll
        document.body.classList.remove('overflow-hidden');

        // Ocultar tras la animación
        setTimeout(() => {
            if (sidebarBackdrop.classList.contains('opacity-0')) {
                sidebarBackdrop.classList.add('hidden');
            }
        }, 300);
    }

    if (btnAbrirSidebarMovil) {
        btnAbrirSidebarMovil.addEventListener('click', abrirSidebarMovil);
    }

    if (btnCerrarSidebarMovil) {
        btnCerrarSidebarMovil.addEventListener('click', cerrarSidebarMovil);
    }

    if (sidebarBackdrop) {
        sidebarBackdrop.addEventListener('click', cerrarSidebarMovil);
    }

    // =========================================================================
    // 3. CONTROL DEL MENÚ LATERAL EN PC (Colapsar / Expandir)
    // =========================================================================
    if (btnToggleSidebarDesktop && sidebar) {
        btnToggleSidebarDesktop.addEventListener('click', () => {
            const estaColapsado = sidebar.classList.contains('lg:-ml-72');
            if (estaColapsado) {
                // Expandir sidebar
                sidebar.classList.remove('lg:-ml-72');
                btnToggleSidebarDesktop.setAttribute('aria-expanded', 'true');
            } else {
                // Colapsar sidebar
                sidebar.classList.add('lg:-ml-72');
                btnToggleSidebarDesktop.setAttribute('aria-expanded', 'false');
            }
        });
    }

    // =========================================================================
    // 4. CONTROL DEL MODAL: ALTA DE PROFESOR
    // =========================================================================
    function abrirModalProfesor() {
        if (!modalAltaProfesor) return;

        // Cerrar menú móvil si estuviera abierto para limpiar la pantalla
        cerrarSidebarMovil();

        modalAltaProfesor.classList.remove('hidden');
        modalAltaProfesor.classList.add('flex');
        document.body.classList.add('overflow-hidden');

        // Enfocar primer campo para accesibilidad
        if (primerInputModal) {
            setTimeout(() => primerInputModal.focus(), 100);
        }
    }

    function cerrarModalProfesor() {
        if (!modalAltaProfesor) return;

        modalAltaProfesor.classList.add('hidden');
        modalAltaProfesor.classList.remove('flex');
        document.body.classList.remove('overflow-hidden');

        if (formAltaProfesor) {
            formAltaProfesor.reset();
        }
    }

    if (btnAbrirModalProfesor) {
        btnAbrirModalProfesor.addEventListener('click', abrirModalProfesor);
    }

    if (btnAbrirModalProfesorMenu) {
        btnAbrirModalProfesorMenu.addEventListener('click', (e) => {
            e.preventDefault();
            abrirModalProfesor();
        });
    }

    if (btnCerrarModalProfesor) {
        btnCerrarModalProfesor.addEventListener('click', cerrarModalProfesor);
    }

    if (btnCancelarModalProfesor) {
        btnCancelarModalProfesor.addEventListener('click', cerrarModalProfesor);
    }

    if (modalProfesorBackdrop) {
        modalProfesorBackdrop.addEventListener('click', cerrarModalProfesor);
    }

    // Manejador temporal de envío visual del formulario
    if (formAltaProfesor) {
        formAltaProfesor.addEventListener('submit', (e) => {
            // Si no tiene action backend configurado, prevenimos el envío por defecto para pruebas visuales
            const tieneActionReal = formAltaProfesor.getAttribute('action') && formAltaProfesor.getAttribute('action') !== '#';
            if (!tieneActionReal) {
                e.preventDefault();
                alert('¡Profesor registrado para pruebas visuales! El sistema enviará la invitación por correo electrónico al docente conforme a la Regla 7.1.');
                cerrarModalProfesor();
            }
        });
    }

    // =========================================================================
    // 5. ACCESIBILIDAD POR TECLADO (Tecla Escape para cerrar Drawer o Modal)
    // =========================================================================
    document.addEventListener('keydown', (event) => {
        if (event.key === 'Escape') {
            // Si el modal está abierto, cerrarlo primero
            if (modalAltaProfesor && !modalAltaProfesor.classList.contains('hidden')) {
                cerrarModalProfesor();
                return;
            }
            // Si el drawer móvil está abierto, cerrarlo
            if (sidebar && sidebar.classList.contains('translate-x-0')) {
                cerrarSidebarMovil();
            }
        }
    });

    // Cerrar el drawer en móvil si el usuario redimensiona la pantalla a tamaño escritorio
    window.addEventListener('resize', () => {
        if (window.innerWidth >= 1024 && sidebar && sidebar.classList.contains('translate-x-0')) {
            cerrarSidebarMovil();
        }
    });
});

