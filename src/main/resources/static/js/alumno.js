/**
 * alumno.js - Lógica Cliente para el Portal del Alumno (Calendario, Clases y Tarifas)
 *
 * =====================================================================================
 * 1. NAVEGACIÓN Y EXPERIENCIA DEL ALUMNO:
 *    - Menú lateral en modo compacto para pantallas grandes (>= 1024px) con persistencia.
 *    - Búsqueda instantánea en tablas de histórico de pagos y clases.
 *
 * 2. CÁLCULO DINÁMICO DE TARIFAS DE CLASES SUELTAS:
 *    - Calcula en tiempo real el precio total al variar la cantidad de clases individuales
 *      deseadas, leyendo el precio unitario del atributo 'data-precio-clase' (30 € en carnets
 *      estándar o 60 € en pesados C/D).
 *
 * 3. DESPLAZAMIENTO SUAVE (SMOOTH SCROLL) A RENOVACIÓN DE MATRÍCULA:
 *    - Cuando el alumno agota sus convocatorias (2 suspensos o convocatorias <= 0),
 *      se le presenta un acceso directo que desplaza suavemente la vista hasta la sección de
 *      renovación mediante 'scrollIntoView({ behavior: "smooth" })'.
 *
 * 4. LEYENDA DEL CALENDARIO COLAPSABLE:
 *    - Permite alternar la guía de colores de la cuadrícula semanal con persistencia en 'localStorage'.
 * =====================================================================================
 */

document.addEventListener('DOMContentLoaded', () => {
    'use strict';

    // =========================================================================
    // 1. CONTROL DE SIDEBAR EN MODO COMPACTO (ESCRITORIO >= 1024px)
    // =========================================================================
    const sidebar = document.getElementById('alumno-sidebar');
    const btnToggleSidebar = document.getElementById('btn-toggle-sidebar');
    const TEMA_ALMACENADO_SIDEBAR = 'mands_alumno_sidebar_compact';

    /**
     * Alterna las clases Tailwind para colapsar o expandir el menú lateral del alumno.
     * @param {boolean} esCompacto - True para colapsar a solo iconos (w-20).
     */
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
        if (!tableId) return;

        input.addEventListener('input', () => {
            const table = document.getElementById(tableId);
            if (!table) return;

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
    // Escucha el evento disparado por el servidor tras reservar o cancelar una clase
    document.body.addEventListener('actualizarCalendario', () => {
        document.body.classList.remove('overflow-hidden');
        const modal = document.getElementById('contenedor-modal');
        if (modal) {
            modal.innerHTML = '';
        }
    });

    // =========================================================================
    // 4. CONTROLADOR DE PRECIO DINÁMICO PARA CLASES INDIVIDUALES
    // =========================================================================
    const inputCantidadIndividual = document.getElementById('cantidad-clases-individual');
    const displayTotalIndividual = document.getElementById('total-clases-individual');
    if (inputCantidadIndividual && displayTotalIndividual) {
        /**
         * Recalcula el precio total según la cantidad seleccionada y el precio unitario del permiso.
         */
        function actualizarTotalIndividual() {
            let cant = parseInt(inputCantidadIndividual.value, 10);
            if (isNaN(cant) || cant < 1) {
                cant = 1;
                inputCantidadIndividual.value = 1;
            }
            // Lee el precio unitario del dataset (30 € estándar o 60 € pesados)
            const precioClase = parseFloat(inputCantidadIndividual.dataset.precioClase) || 30;
            const total = cant * precioClase;
            displayTotalIndividual.textContent = total.toFixed(2) + ' €';
        }
        inputCantidadIndividual.addEventListener('input', actualizarTotalIndividual);
        inputCantidadIndividual.addEventListener('change', actualizarTotalIndividual);
    }

    // =========================================================================
    // 5. DESPLAZAMIENTO SUAVE A RENOVACIÓN DE MATRÍCULA
    // =========================================================================
    function inicializarDesplazamientoRenovacion() {
        const btnIrARenovacion = document.getElementById('btn-ir-a-renovacion');
        if (!btnIrARenovacion) return;

        btnIrARenovacion.addEventListener('click', (evento) => {
            evento.preventDefault();
            const seccionRenovacion = document.getElementById('seccion-renovacion');
            if (seccionRenovacion) {
                // Desplazamiento animado suave de la ventana
                seccionRenovacion.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
                // Actualización limpia de la URL con el hash sin recargar
                if (window.history && window.history.pushState) {
                    window.history.pushState(null, '', '#seccion-renovacion');
                }
            }
        });
    }

    inicializarDesplazamientoRenovacion();

    // =========================================================================
    // 6. TOGGLE DE LEYENDA DEL CALENDARIO (COLAPSABLE CON PERSISTENCIA)
    // =========================================================================
    function inicializarToggleLeyenda() {
        const btnToggleLeyenda = document.getElementById('btn-toggle-leyenda');
        const filaLeyenda = document.getElementById('fila-leyenda-desplegable');
        const chevronLeyenda = document.getElementById('chevron-leyenda');
        const CLAVE_STORAGE = 'mands_alumno_leyenda_visible';

        if (!btnToggleLeyenda || !filaLeyenda || !chevronLeyenda) return;

        /**
         * Actualiza las clases visuales de la leyenda según su estado de visibilidad.
         */
        function actualizarEstadoLeyenda(visible) {
            if (visible) {
                filaLeyenda.classList.remove('hidden');
                chevronLeyenda.classList.add('rotate-180');
                btnToggleLeyenda.setAttribute('aria-expanded', 'true');
                btnToggleLeyenda.classList.add('bg-indigo-50', 'dark:bg-indigo-950/40', 'text-indigo-700', 'dark:text-indigo-300', 'border-indigo-200', 'dark:border-indigo-800');
                btnToggleLeyenda.classList.remove('bg-slate-100', 'dark:bg-slate-800/80', 'text-slate-600', 'dark:text-slate-300', 'border-slate-200/80', 'dark:border-slate-700/80');
            } else {
                filaLeyenda.classList.add('hidden');
                chevronLeyenda.classList.remove('rotate-180');
                btnToggleLeyenda.setAttribute('aria-expanded', 'false');
                btnToggleLeyenda.classList.remove('bg-indigo-50', 'dark:bg-indigo-950/40', 'text-indigo-700', 'dark:text-indigo-300', 'border-indigo-200', 'dark:border-indigo-800');
                btnToggleLeyenda.classList.add('bg-slate-100', 'dark:bg-slate-800/80', 'text-slate-600', 'dark:text-slate-300', 'border-slate-200/80', 'dark:border-slate-700/80');
            }
        }

        // Cargar estado inicial desde localStorage
        try {
            const preferencia = localStorage.getItem(CLAVE_STORAGE);
            if (preferencia !== null) {
                actualizarEstadoLeyenda(preferencia === 'true');
            } else {
                actualizarEstadoLeyenda(false);
            }
        } catch (e) {
            actualizarEstadoLeyenda(false);
        }

        btnToggleLeyenda.addEventListener('click', () => {
            const estaVisible = !filaLeyenda.classList.contains('hidden');
            const nuevoEstado = !estaVisible;
            actualizarEstadoLeyenda(nuevoEstado);
            try {
                localStorage.setItem(CLAVE_STORAGE, nuevoEstado ? 'true' : 'false');
            } catch (e) {
                // Fallback silencioso
            }
        });
    }

    inicializarToggleLeyenda();
});
