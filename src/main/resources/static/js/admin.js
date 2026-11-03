/**
 * admin.js - Utilidades Comunes y Navegación para el Panel del Administrador
 *
 * =====================================================================================
 * 1. ESPACIO DE NOMBRES COMPARTIDO ('window.AdminUI'):
 *    Define utilidades comunes (formateo ISO de fechas para inputs de tipo 'date',
 *    y funciones de feedback visual para mostrar o limpiar errores en rojo) que son
 *    reutilizadas por el resto de scripts del módulo de administración (profesores, flota,
 *    alumnos, exámenes).
 *
 * 2. MODO COMPACTO DEL MENÚ LATERAL (DESKTOP SIDEBAR COLLAPSE):
 *    En pantallas de escritorio (>= 1024px), permite alternar el menú lateral entre modo
 *    expandido (ancho 'w-72' = 288px) y modo compacto (ancho 'w-20' = 80px, solo iconos).
 *    La preferencia se recuerda en el navegador con 'localStorage'.
 *
 * 3. BÚSQUEDA INSTANTÁNEA EN TABLAS HTML (CLIENT-SIDE FILTERING):
 *    Mediante el atributo declarativo 'data-table-search="idDeTabla"', cualquier campo de
 *    búsqueda puede filtrar filas ('<tr>') en tiempo real evaluando el texto de cada celda,
 *    proporcionando feedback instantáneo sin necesidad de consultar al servidor.
 * =====================================================================================
 */
(function () {
    'use strict';

    // Clave para recordar si el administrador prefiere el menú lateral colapsado o expandido
    const TEMA_ALMACENADO_SIDEBAR = 'mands_admin_sidebar_compact';

    // =========================================================================
    // UTILIDADES GLOBALES COMPARTIDAS (AdminUI)
    // =========================================================================
    window.AdminUI = {
        /**
         * Da formato estándar YYYY-MM-DD a un objeto Date de JavaScript.
         * Es necesario para fijar atributos 'min' y 'max' en elementos <input type="date">.
         * @param {Date} fecha - Instancia de Date.
         * @returns {string} Fecha en formato "YYYY-MM-DD" o cadena vacía si es inválida.
         */
        formatearFechaISO(fecha) {
            if (!(fecha instanceof Date) || isNaN(fecha.getTime())) return '';
            const anio = fecha.getFullYear();
            const mes = String(fecha.getMonth() + 1).padStart(2, '0'); // padStart asegura 2 dígitos: "05", "11"
            const dia = String(fecha.getDate()).padStart(2, '0');
            return `${anio}-${mes}-${dia}`;
        },

        /**
         * Muestra un mensaje de error accesible debajo de un campo y aplica un borde rojo al input.
         * @param {string} inputId - ID del input erróneo.
         * @param {string} feedbackId - ID del contenedor <div> donde renderizar el mensaje.
         * @param {string} mensaje - Texto explicativo del fallo.
         */
        mostrarErrorCampo(inputId, feedbackId, mensaje) {
            const input = document.getElementById(inputId);
            const feedback = document.getElementById(feedbackId);

            if (input) {
                // Modificación de clases Tailwind para activar borde rojo de alerta
                input.classList.add('border-red-500', 'focus:ring-red-500', 'dark:border-red-500');
                input.classList.remove('border-slate-300', 'dark:border-slate-700', 'focus:ring-blue-600');
                // Constraint Validation API nativa para soporte de accesibilidad
                input.setCustomValidity(mensaje);
            }

            if (feedback) {
                // Inyección del badge visual con icono SVG de peligro
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
         * @param {string} inputId - ID del input a limpiar.
         * @param {string} feedbackId - ID del contenedor de feedback.
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
    /**
     * Aplica las clases de ancho y visibilidad al menú lateral según si está en modo compacto o extendido.
     * @param {boolean} esCompacto - True para colapsar el menú a solo iconos (80px).
     */
    function aplicarModoCompactoEscritorio(esCompacto) {
        const sidebar = document.getElementById('admin-sidebar');
        if (!sidebar) return;

        const textElements = sidebar.querySelectorAll('.sidebar-text-element');
        const navLinks = sidebar.querySelectorAll('.nav-link');
        const sidebarFooter = document.getElementById('sidebar-footer');
        const sidebarFooterContent = document.getElementById('sidebar-footer-content');

        if (esCompacto) {
            // Contraer a 80px (clase w-20)
            sidebar.classList.remove('w-72');
            sidebar.classList.add('w-20');

            // Ocultar etiquetas de texto y contadores/badges de las secciones
            textElements.forEach(el => {
                el.classList.add('hidden');
            });

            // Centrar iconos en los enlaces de navegación
            navLinks.forEach(link => {
                link.classList.add('justify-center', 'px-2');
            });

            // Adaptar el pie del menú lateral en modo compacto
            if (sidebarFooter && sidebarFooterContent) {
                sidebarFooter.classList.remove('p-4');
                sidebarFooter.classList.add('p-2', 'py-3');
                sidebarFooterContent.classList.remove('flex', 'items-center', 'justify-between');
                sidebarFooterContent.classList.add('flex', 'flex-col', 'items-center', 'justify-center', 'gap-2');
            }
        } else {
            // Expandir a 288px (clase w-72)
            sidebar.classList.remove('w-20');
            sidebar.classList.add('w-72');

            // Volver a mostrar las etiquetas de texto
            textElements.forEach(el => {
                el.classList.remove('hidden');
            });

            // Restaurar espaciado y alineación estándar
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
     * Alterna la vista compacta o expandida del menú lateral en pantalla de escritorio al pulsar el botón.
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
     * En móvil (< 1024px) asegura que el cajón mantenga el ancho completo para no romper la vista táctil.
     */
    function manejarResizeVentana() {
        const sidebar = document.getElementById('admin-sidebar');
        if (!sidebar) return;

        if (window.innerWidth >= 1024) {
            const estaCompacto = localStorage.getItem(TEMA_ALMACENADO_SIDEBAR) === 'true';
            aplicarModoCompactoEscritorio(estaCompacto);
        } else {
            aplicarModoCompactoEscritorio(false);
        }
    }

    /**
     * Filtra filas de una tabla HTML en tiempo real según el término escrito por el usuario.
     * @param {Event} evento - Evento de tipo 'input'.
     */
    function manejarInputBusquedaTabla(evento) {
        const input = evento.currentTarget;
        const tableId = input.getAttribute('data-table-search');
        const table = document.getElementById(tableId);
        if (!table) return;

        const termino = input.value.trim().toLowerCase();
        const filas = table.querySelectorAll('tbody tr');

        filas.forEach(fila => {
            // Se concatena el texto visible de la fila con posibles atributos ocultos como 'data-permisos'
            const permisosExtra = fila.getAttribute('data-permisos') || '';
            const textoCompleto = (fila.textContent + ' ' + permisosExtra).toLowerCase();

            // Si contiene el término, se muestra la fila; si no, se oculta mediante 'display: none'
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
        // 1. Restaurar preferencia guardada en escritorio al cargar la página
        if (window.innerWidth >= 1024) {
            const estaCompacto = localStorage.getItem(TEMA_ALMACENADO_SIDEBAR) === 'true';
            aplicarModoCompactoEscritorio(estaCompacto);
        }

        // 2. Vincular botón de alternancia del menú
        const btnToggleSidebar = document.getElementById('btn-toggle-sidebar');
        if (btnToggleSidebar) {
            btnToggleSidebar.addEventListener('click', manejarToggleSidebar);
        }

        // 3. Vincular evento resize de ventana
        window.addEventListener('resize', manejarResizeVentana);

        // 4. Vincular todos los inputs que contengan el atributo declarativo 'data-table-search'
        const searchInputs = document.querySelectorAll('[data-table-search]');
        searchInputs.forEach(input => {
            input.addEventListener('input', manejarInputBusquedaTabla);
        });
    }

    // Registro al cargarse el árbol DOM
    document.addEventListener('DOMContentLoaded', inicializarAdmin);
})();
