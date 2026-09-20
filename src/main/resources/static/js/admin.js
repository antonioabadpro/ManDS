/**
 * admin.js
 * JavaScript no intrusivo y específico para el panel de administración de ManDS ERP.
 * Las funciones generales de interfaz (tema oscuro, reloj, modales, menú de usuario)
 * residen en layout.js.
 *
 * Funcionalidades específicas de Administración:
 * 1. Alternancia del menú lateral (Sidebar) en modo compacto para escritorio (>= 1024px)
 *    con persistencia en localStorage.
 * 2. Filtrado rápido de tablas en cliente mediante el atributo data-table-search.
 */

document.addEventListener('DOMContentLoaded', () => {
    'use strict';

    // =========================================================================
    // SELECTORES DE NAVEGACIÓN Y SIDEBAR
    // =========================================================================
    const sidebar = document.getElementById('admin-sidebar');
    const btnToggleSidebar = document.getElementById('btn-toggle-sidebar');
    const TEMA_ALMACENADO_SIDEBAR = 'mands_admin_sidebar_compact';

    function aplicarModoCompactoEscritorio(esCompacto) {
        if (!sidebar) return;

        const textElements = sidebar.querySelectorAll('.sidebar-text-element');
        const navLinks = sidebar.querySelectorAll('.nav-link');
        const sidebarFooter = document.getElementById('sidebar-footer');
        const sidebarFooterContent = document.getElementById('sidebar-footer-content');

        if (esCompacto) {
            // Contraer a 80px (w-20)
            sidebar.classList.remove('w-72');
            sidebar.classList.add('w-20');

            // Ocultar etiquetas de texto y badges
            textElements.forEach(el => {
                el.classList.add('hidden');
            });

            // Centrar iconos
            navLinks.forEach(link => {
                link.classList.add('justify-center', 'px-2');
            });

            // Adaptar el pie en modo compacto (apilado centrado con logout accesible)
            if (sidebarFooter && sidebarFooterContent) {
                sidebarFooter.classList.remove('p-4');
                sidebarFooter.classList.add('p-2', 'py-3');

                sidebarFooterContent.classList.remove('flex', 'items-center', 'justify-between');
                sidebarFooterContent.classList.add('flex', 'flex-col', 'items-center', 'justify-center', 'gap-2');
            }
        } else {
            // Expandir a 288px (w-72)
            sidebar.classList.remove('w-20');
            sidebar.classList.add('w-72');

            // Mostrar etiquetas de texto y badges
            textElements.forEach(el => {
                el.classList.remove('hidden');
            });

            // Restaurar padding original
            navLinks.forEach(link => {
                link.classList.remove('justify-center', 'px-2');
            });

            // Restaurar el pie de la barra lateral al expandir
            if (sidebarFooter && sidebarFooterContent) {
                sidebarFooter.classList.remove('p-2', 'py-3');
                sidebarFooter.classList.add('p-4');

                sidebarFooterContent.classList.remove('flex-col', 'justify-center', 'gap-2');
                sidebarFooterContent.classList.add('flex', 'items-center', 'justify-between');
            }
        }
    }

    // Restaurar estado guardado en PC al cargar
    if (window.innerWidth >= 1024) {
        const estaCompacto = localStorage.getItem(TEMA_ALMACENADO_SIDEBAR) === 'true';
        aplicarModoCompactoEscritorio(estaCompacto);
    }

    // Alternar modo compacto en PC al hacer clic en el toggle
    if (btnToggleSidebar) {
        btnToggleSidebar.addEventListener('click', () => {
            if (window.innerWidth >= 1024 && sidebar) {
                const estaCompacto = sidebar.classList.contains('w-20');
                const nuevoEstado = !estaCompacto;
                aplicarModoCompactoEscritorio(nuevoEstado);
                localStorage.setItem(TEMA_ALMACENADO_SIDEBAR, nuevoEstado.toString());
            }
        });
    }

    // Sincronizar modo compacto o expandido al redimensionar ventana
    window.addEventListener('resize', () => {
        if (!sidebar) return;
        if (window.innerWidth >= 1024) {
            const estaCompacto = localStorage.getItem(TEMA_ALMACENADO_SIDEBAR) === 'true';
            aplicarModoCompactoEscritorio(estaCompacto);
        } else {
            // En móvil, mantener el ancho completo del cajón y sus elementos visibles
            aplicarModoCompactoEscritorio(false);
        }
    });

    // =========================================================================
    // FILTRADO RÁPIDO DE TABLAS EN CLIENTE
    // =========================================================================
    const searchInputs = document.querySelectorAll('[data-table-search]');
    searchInputs.forEach(input => {
        const tableId = input.getAttribute('data-table-search');
        const table = document.getElementById(tableId);
        if (!table) return;

        input.addEventListener('input', () => {
            const termino = input.value.trim().toLowerCase();
            const filas = table.querySelectorAll('tbody tr');

            filas.forEach(fila => {
                const texto = fila.textContent.toLowerCase();
                if (texto.includes(termino)) {
                    fila.style.display = '';
                } else {
                    fila.style.display = 'none';
                }
            });
        });
    });
});
