/**
 * alumno.js
 * JavaScript no intrusivo para el portal del Alumno en ManDS ERP.
 * Las funciones generales de interfaz (tema oscuro, reloj, modales, menú de usuario) residen en layout.js.
 *
 * Funcionalidades específicas de Alumno:
 * 1. Alternancia del menú lateral (Sidebar) en modo compacto para escritorio con persistencia.
 * 2. Filtrado rápido de tablas en cliente mediante data-table-search.
 * 3. Integración con FullCalendar v6 y HTMX para reserva y consulta de clases prácticas.
 */

document.addEventListener('DOMContentLoaded', () => {
    'use strict';

    // =========================================================================
    // 1. CONTROL DE SIDEBAR EN MODO COMPACTO (ESCRITORIO >= 1024px)
    // =========================================================================
    const sidebar = document.getElementById('alumno-sidebar');
    const btnToggleSidebar = document.getElementById('btn-toggle-sidebar');
    const TEMA_ALMACENADO_SIDEBAR = 'mands_alumno_sidebar_compact';

    function aplicarModoCompactoEscritorio(esCompacto) {
        if (!sidebar) return;

        const textElements = sidebar.querySelectorAll('.sidebar-text-element');
        const navLinks = sidebar.querySelectorAll('.nav-link');
        const sidebarFooter = document.getElementById('sidebar-footer');
        const sidebarFooterContent = document.getElementById('sidebar-footer-content');

        if (esCompacto) {
            sidebar.classList.remove('w-72');
            sidebar.classList.add('w-20');

            textElements.forEach(el => el.classList.add('hidden'));
            navLinks.forEach(link => link.classList.add('justify-center', 'px-2'));

            if (sidebarFooter && sidebarFooterContent) {
                sidebarFooter.classList.remove('p-4');
                sidebarFooter.classList.add('p-2', 'py-3');
                sidebarFooterContent.classList.remove('flex', 'items-center', 'justify-between');
                sidebarFooterContent.classList.add('flex', 'flex-col', 'items-center', 'justify-center', 'gap-2');
            }
        } else {
            sidebar.classList.remove('w-20');
            sidebar.classList.add('w-72');

            textElements.forEach(el => el.classList.remove('hidden'));
            navLinks.forEach(link => link.classList.remove('justify-center', 'px-2'));

            if (sidebarFooter && sidebarFooterContent) {
                sidebarFooter.classList.remove('p-2', 'py-3');
                sidebarFooter.classList.add('p-4');
                sidebarFooterContent.classList.remove('flex-col', 'justify-center', 'gap-2');
                sidebarFooterContent.classList.add('flex', 'items-center', 'justify-between');
            }
        }
    }

    try {
        if (localStorage.getItem(TEMA_ALMACENADO_SIDEBAR) === 'true' && window.innerWidth >= 1024) {
            aplicarModoCompactoEscritorio(true);
        }
    } catch (e) {
        // Fallback silencioso
    }

    if (btnToggleSidebar) {
        btnToggleSidebar.addEventListener('click', () => {
            if (window.innerWidth >= 1024 && sidebar) {
                const actualmenteCompacto = sidebar.classList.contains('w-20');
                const nuevoEstado = !actualmenteCompacto;
                aplicarModoCompactoEscritorio(nuevoEstado);
                try {
                    localStorage.setItem(TEMA_ALMACENADO_SIDEBAR, nuevoEstado ? 'true' : 'false');
                } catch (e) {
                    // Fallback silencioso
                }
            }
        });
    }

    // =========================================================================
    // 2. FILTRADO RÁPIDO DE TABLAS EN CLIENTE (data-table-search)
    // =========================================================================
    document.querySelectorAll('input[data-table-search]').forEach(input => {
        const tableId = input.getAttribute('data-table-search');
        const table = document.getElementById(tableId);
        if (!table) return;

        input.addEventListener('input', () => {
            const termino = input.value.toLowerCase().trim();
            const filas = table.querySelectorAll('tbody tr');

            filas.forEach(fila => {
                const texto = fila.textContent.toLowerCase();
                fila.style.display = texto.includes(termino) ? '' : 'none';
            });
        });
    });

    // =========================================================================
    // 3. ACTUALIZACIÓN REACTIVA DE LA CUADRÍCULA SEMANAL (HTMX)
    // =========================================================================
    document.body.addEventListener('actualizarCalendario', () => {
        document.body.classList.remove('overflow-hidden');
        const modal = document.getElementById('contenedor-modal');
        if (modal) {
            modal.innerHTML = '';
        }
        const cuadricula = document.getElementById('contenedor-cuadricula-calendario');
        if (cuadricula && window.htmx) {
            window.htmx.ajax('GET', '/alumno/calendario/cuadricula', {
                target: '#contenedor-cuadricula-calendario',
                swap: 'outerHTML'
            });
        }
    });

    // =========================================================================
    // 4. CONTROLADOR DE PRECIO DINÁMICO PARA CLASES INDIVIDUALES
    // =========================================================================
    const inputCantidadIndividual = document.getElementById('cantidad-clases-individual');
    const displayTotalIndividual = document.getElementById('total-clases-individual');
    if (inputCantidadIndividual && displayTotalIndividual) {
        function actualizarTotalIndividual() {
            let cant = parseInt(inputCantidadIndividual.value, 10);
            if (isNaN(cant) || cant < 1) {
                cant = 1;
                inputCantidadIndividual.value = 1;
            }
            const precioClase = parseFloat(inputCantidadIndividual.dataset.precioClase) || 30;
            const total = cant * precioClase;
            displayTotalIndividual.textContent = total.toFixed(2) + ' €';
        }
        inputCantidadIndividual.addEventListener('input', actualizarTotalIndividual);
        inputCantidadIndividual.addEventListener('change', actualizarTotalIndividual);
    }
});
