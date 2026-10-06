/**
 * admin.js
 * JavaScript no intrusivo y transversal para el panel de administración de ManDS ERP.
 * Las funciones generales de interfaz (tema oscuro, reloj, modales base, menú de usuario)
 * residen en layout.js.
 *
 * Responsabilidades transversales:
 * 1. Menú lateral (Sidebar) en modo compacto para escritorio con persistencia en localStorage.
 * 2. Filtrado rápido de tablas en cliente mediante el atributo data-table-search.
 * 3. Utilidades compartidas de interfaz (AdminUI) para validación y feedback de formularios.
 */
(function () {
    'use strict';

    const TEMA_ALMACENADO_SIDEBAR = 'mands_admin_sidebar_compact';

    // =========================================================================
    // UTILIDADES GLOBALES COMPARTIDAS (AdminUI)
    // =========================================================================
    window.AdminUI = {
        /**
         * Da formato YYYY-MM-DD a un objeto Date para atributos min y max de inputs tipo date.
         */
        formatearFechaISO(fecha) {
            if (!(fecha instanceof Date) || isNaN(fecha.getTime())) return '';
            const anio = fecha.getFullYear();
            const mes = String(fecha.getMonth() + 1).padStart(2, '0');
            const dia = String(fecha.getDate()).padStart(2, '0');
            return `${anio}-${mes}-${dia}`;
        },

        /**
         * Muestra un mensaje de error visual accesible y resalta el borde en rojo.
         */
        mostrarErrorCampo(inputId, feedbackId, mensaje) {
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
        },

        /**
         * Limpia el mensaje de error visual y restaura el borde neutro del campo.
         */
        limpiarErrorCampo(inputId, feedbackId) {
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
    };

    // =========================================================================
    // LÓGICA DE NAVEGACIÓN Y SIDEBAR EN ESCRITORIO
    // =========================================================================
    function aplicarModoCompactoEscritorio(esCompacto) {
        const sidebar = document.getElementById('admin-sidebar');
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

            // Adaptar el pie en modo compacto
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

            // Restaurar espaciado original
            navLinks.forEach(link => {
                link.classList.remove('justify-center', 'px-2');
            });

            // Restaurar el pie de la barra lateral
            if (sidebarFooter && sidebarFooterContent) {
                sidebarFooter.classList.remove('p-2', 'py-3');
                sidebarFooter.classList.add('p-4');
                sidebarFooterContent.classList.remove('flex-col', 'justify-center', 'gap-2');
                sidebarFooterContent.classList.add('flex', 'items-center', 'justify-between');
            }
        }
    }

    // =========================================================================
    // MANEJADORES DE EVENTOS CON NOMBRE
    // =========================================================================

    /**
     * Alterna la vista compacta o expandida del menú lateral en pantalla de escritorio.
     */
    function manejarToggleSidebar() {
        if (window.innerWidth >= 1024) {
            const sidebar = document.getElementById('admin-sidebar');
            if (!sidebar) return;

            const estaCompacto = sidebar.classList.contains('w-20');
            const nuevoEstado = !estaCompacto;
            aplicarModoCompactoEscritorio(nuevoEstado);
            localStorage.setItem(TEMA_ALMACENADO_SIDEBAR, nuevoEstado.toString());
        }
    }

    /**
     * Sincroniza el menú lateral con el tamaño de ventana al redimensionar.
     */
    function manejarResizeVentana() {
        const sidebar = document.getElementById('admin-sidebar');
        if (!sidebar) return;

        if (window.innerWidth >= 1024) {
            const estaCompacto = localStorage.getItem(TEMA_ALMACENADO_SIDEBAR) === 'true';
            aplicarModoCompactoEscritorio(estaCompacto);
        } else {
            // En móvil, mantener el ancho completo del cajón y sus elementos visibles
            aplicarModoCompactoEscritorio(false);
        }
    }

    /**
     * Filtra filas de una tabla HTML en tiempo real según el texto introducido en el input.
     */
    function manejarInputBusquedaTabla(evento) {
        const input = evento.currentTarget;
        const tableId = input.getAttribute('data-table-search');
        const table = document.getElementById(tableId);
        if (!table) return;

        const termino = input.value.trim().toLowerCase();
        const filas = table.querySelectorAll('tbody tr');

        filas.forEach(fila => {
            const permisosExtra = fila.getAttribute('data-permisos') || '';
            const textoCompleto = (fila.textContent + ' ' + permisosExtra).toLowerCase();
            if (textoCompleto.includes(termino)) {
                fila.style.display = '';
            } else {
                fila.style.display = 'none';
            }
        });
    }

    // =========================================================================
    // PUNTO DE ENTRADA DE INICIALIZACIÓN
    // =========================================================================
    function inicializarAdmin() {
        // 1. Restaurar estado guardado en PC al cargar
        if (window.innerWidth >= 1024) {
            const estaCompacto = localStorage.getItem(TEMA_ALMACENADO_SIDEBAR) === 'true';
            aplicarModoCompactoEscritorio(estaCompacto);
        }

        // 2. Vincular botón de alternancia del sidebar
        const btnToggleSidebar = document.getElementById('btn-toggle-sidebar');
        if (btnToggleSidebar) {
            btnToggleSidebar.addEventListener('click', manejarToggleSidebar);
        }

        // 3. Vincular evento resize de ventana
        window.addEventListener('resize', manejarResizeVentana);

        // 4. Vincular inputs de búsqueda de tablas
        const searchInputs = document.querySelectorAll('[data-table-search]');
        searchInputs.forEach(input => {
            input.addEventListener('input', manejarInputBusquedaTabla);
        });
    }

    // Registro al cargar el DOM
    document.addEventListener('DOMContentLoaded', inicializarAdmin);
})();
