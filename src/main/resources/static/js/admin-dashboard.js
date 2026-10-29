/**
 * admin-dashboard.js - Interactividad del Panel Principal (Dashboard) del Administrador
 *
 * =====================================================================================
 * 1. EXPERIENCIA RESPONSIVE Y NAVEGACIÓN OFF-CANVAS:
 *    - En dispositivos móviles (< 1024px): control de apertura y cierre del menú lateral
 *      deslizante (off-canvas drawer) con desenfoque de fondo ('backdrop-blur-sm') y
 *      bloqueo de scroll para evitar saltos de pantalla.
 *    - En ordenadores de escritorio (>= 1024px): control de colapso rápido mediante
 *      desplazamiento de margen negativo ('lg:-ml-72').
 *
 * 2. MODAL ACCESIBLE DE ALTA DE PROFESOR:
 *    Abre el modal de alta de profesor asegurando el cumplimiento de las pautas WAI-ARIA
 *    (traslado del foco del teclado al primer input, reseteo del formulario al cerrar y
 *    cierre por pulsación en el fondo exterior).
 *
 * 3. CONTROL JERÁRQUICO DE TECLADO (ESCAPE):
 *    Escucha globalmente la tecla 'Escape': si hay un modal abierto, lo cierra; si no,
 *    cierra el menú móvil en caso de que esté desplegado.
 * =====================================================================================
 */
document.addEventListener('DOMContentLoaded', () => {
    'use strict';

    // =========================================================================
    // 1. SELECTORES DE ELEMENTOS DEL DOM
    // =========================================================================
    // Menú lateral y backdrop translúcido
    const sidebar = document.getElementById('admin-sidebar');
    const sidebarBackdrop = document.getElementById('sidebar-backdrop');
    const btnAbrirSidebarMovil = document.getElementById('btn-abrir-sidebar-movil');
    const btnCerrarSidebarMovil = document.getElementById('btn-cerrar-sidebar-movil');
    const btnToggleSidebarDesktop = document.getElementById('btn-toggle-sidebar-desktop');

    // Modal para Alta de Profesor
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
    /**
     * Despliega el menú lateral en dispositivos móviles.
     */
    function abrirSidebarMovil() {
        if (!sidebar || !sidebarBackdrop) return;

        // Mostrar backdrop con efecto blur y transición suave de opacidad
        sidebarBackdrop.classList.remove('hidden');
        requestAnimationFrame(() => {
            sidebarBackdrop.classList.remove('opacity-0', 'pointer-events-none');
            sidebarBackdrop.classList.add('opacity-100', 'pointer-events-auto');
        });

        // Deslizar sidebar desde la izquierda mediante traslación CSS
        sidebar.classList.remove('-translate-x-full');
        sidebar.classList.add('translate-x-0');

        // Actualizar accesibilidad
        if (btnAbrirSidebarMovil) {
            btnAbrirSidebarMovil.setAttribute('aria-expanded', 'true');
        }

        // Bloquear scroll de fondo en móvil para evitar navegación involuntaria de fondo
        document.body.classList.add('overflow-hidden');
    }

    /**
     * Oculta el menú lateral en dispositivos móviles y restaura el scroll.
     */
    function cerrarSidebarMovil() {
        if (!sidebar || !sidebarBackdrop) return;

        // Desvanecer el fondo oscuro
        sidebarBackdrop.classList.remove('opacity-100', 'pointer-events-auto');
        sidebarBackdrop.classList.add('opacity-0', 'pointer-events-none');

        // Deslizar sidebar fuera de la pantalla
        sidebar.classList.remove('translate-x-0');
        sidebar.classList.add('-translate-x-full');

        if (btnAbrirSidebarMovil) {
            btnAbrirSidebarMovil.setAttribute('aria-expanded', 'false');
        }

        // Restaurar scroll de la página principal
        document.body.classList.remove('overflow-hidden');

        // Ocultar del árbol de accesibilidad tras terminar la animación (300ms)
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
                // Expandir sidebar quitando el margen negativo
                sidebar.classList.remove('lg:-ml-72');
                btnToggleSidebarDesktop.setAttribute('aria-expanded', 'true');
            } else {
                // Colapsar sidebar aplicando margen negativo de 288px
                sidebar.classList.add('lg:-ml-72');
                btnToggleSidebarDesktop.setAttribute('aria-expanded', 'false');
            }
        });
    }

    // =========================================================================
    // 4. CONTROL DEL MODAL: ALTA DE PROFESOR
    // =========================================================================
    /**
     * Abre el modal para registrar un nuevo profesor y ubica el foco en su primer campo.
     */
    function abrirModalProfesor() {
        if (!modalAltaProfesor) return;

        // Si el menú móvil estaba abierto, cerrarlo primero para limpiar la pantalla
        cerrarSidebarMovil();

        modalAltaProfesor.classList.remove('hidden');
        modalAltaProfesor.classList.add('flex');
        document.body.classList.add('overflow-hidden');

        // Traslado accesible del foco al primer input interactivo
        if (primerInputModal) {
            setTimeout(() => primerInputModal.focus(), 100);
        }
    }

    /**
     * Oculta el modal de alta de profesor y resetea el formulario.
     */
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

    // Control de envío del formulario
    if (formAltaProfesor) {
        formAltaProfesor.addEventListener('submit', (e) => {
            const tieneActionReal = formAltaProfesor.getAttribute('action') && formAltaProfesor.getAttribute('action') !== '#';
            if (!tieneActionReal) {
                e.preventDefault();
                alert('¡Profesor registrado para pruebas visuales! El sistema enviará la invitación por correo electrónico al profesor');
                cerrarModalProfesor();
            }
        });
    }

    // =========================================================================
    // 5. ACCESIBILIDAD POR TECLADO (Tecla Escape para cerrar Drawer o Modal)
    // =========================================================================
    document.addEventListener('keydown', (event) => {
        if (event.key === 'Escape') {
            // 1. Si el modal está visible, cerrarlo
            if (modalAltaProfesor && !modalAltaProfesor.classList.contains('hidden')) {
                cerrarModalProfesor();
                return;
            }
            // 2. Si el cajón lateral móvil está visible, cerrarlo
            if (sidebar && sidebar.classList.contains('translate-x-0')) {
                cerrarSidebarMovil();
            }
        }
    });

    // Cerrar el cajón móvil automáticamente si el usuario amplía la ventana al tamaño de escritorio
    window.addEventListener('resize', () => {
        if (window.innerWidth >= 1024 && sidebar && sidebar.classList.contains('translate-x-0')) {
            cerrarSidebarMovil();
        }
    });
});
