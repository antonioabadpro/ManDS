/**
 * profesor.js
 * JavaScript no intrusivo para el portal del Profesor en ManDS ERP.
 * Las funciones generales de interfaz (tema oscuro, reloj, modales, menú de usuario) residen en layout.js.
 *
 * Funcionalidades específicas de Profesor:
 * 1. Alternancia del menú lateral (Sidebar) en modo compacto para escritorio con persistencia.
 * 2. Filtrado rápido de tablas en cliente mediante data-table-search.
 * 3. Integración con FullCalendar v6 (código de colores acordado, visualización de eventos y modal de ficha de clase).
 * 4. Validación técnica de kilometraje (kmFin >= kmInicio) antes del envío del reporte de clase.
 */

document.addEventListener('DOMContentLoaded', () => {
    'use strict';

    // =========================================================================
    // 1. CONTROL DE SIDEBAR EN MODO COMPACTO (ESCRITORIO >= 1024px)
    // =========================================================================
    const sidebar = document.getElementById('profesor-sidebar');
    const btnToggleSidebar = document.getElementById('btn-toggle-sidebar');
    const TEMA_ALMACENADO_SIDEBAR = 'mands_profesor_sidebar_compact';

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
    // 3. INTEGRACIÓN CON FULLCALENDAR v6 (AGENDA DOCENTE)
    // =========================================================================
    const calendarEl = document.getElementById('calendario-profesor');
    if (calendarEl && typeof FullCalendar !== 'undefined') {
        const calendar = new FullCalendar.Calendar(calendarEl, {
            initialView: window.innerWidth < 768 ? 'listDay' : 'timeGridWeek',
            locale: 'es',
            firstDay: 1, // Lunes
            slotMinTime: '07:00:00',
            slotMaxTime: '22:00:00',
            allDaySlot: true,
            allDayText: 'Jornada',
            slotDuration: '00:30:00',
            slotLabelInterval: '01:00:00',
            slotLabelFormat: {
                hour: '2-digit',
                minute: '2-digit',
                hour12: false
            },
            headerToolbar: {
                left: 'prev,next today',
                center: 'title',
                right: 'dayGridMonth,timeGridWeek,timeGridDay,listDay'
            },
            buttonText: {
                today: 'Hoy',
                month: 'Mes',
                week: 'Semana',
                day: 'Día',
                list: 'Lista'
            },
            navLinks: true,
            nowIndicator: true,
            editable: false,
            events: '/profesor/calendario/eventos',
            eventClick: function (info) {
                info.jsEvent.preventDefault();
                const props = info.event.extendedProps || {};

                // 1. Si es un día de examen DGT bloqueado
                if (props.tipo === 'EXAMEN_DGT') {
                    if (window.htmx && props.examenId) {
                        window.htmx.ajax('GET', '/profesor/examenes/' + props.examenId + '/modal', {
                            target: '#contenedor-modal',
                            swap: 'innerHTML'
                        });
                        document.body.classList.add('overflow-hidden');
                    }
                    return;
                }

                // 2. Si es una clase práctica: cargar fragmento Thymeleaf con HTMX
                if (window.htmx && info.event.id) {
                    window.htmx.ajax('GET', '/profesor/clases/' + info.event.id + '/modal', {
                        target: '#contenedor-modal',
                        swap: 'innerHTML'
                    });
                    document.body.classList.add('overflow-hidden');
                }
            }
        });

        calendar.render();

        // Escuchar evento emitido desde el servidor por HTMX (HX-Trigger: actualizarCalendario)
        document.body.addEventListener('actualizarCalendario', () => {
            document.body.classList.remove('overflow-hidden');
            if (calendar) {
                calendar.refetchEvents();
            }
        });

        // Reajustar vista según resize
        window.addEventListener('resize', () => {
            if (window.innerWidth < 768 && calendar.view.type !== 'listDay') {
                calendar.changeView('listDay');
            } else if (window.innerWidth >= 768 && calendar.view.type === 'listDay') {
                calendar.changeView('timeGridWeek');
            }
        });
    }

    // =========================================================================
    // 4. VALIDACIÓN DE REPORTES DOCENTES DE CLASE (kmFin >= kmInicio)
    // =========================================================================
    document.addEventListener('submit', (e) => {
        const formCompletarClase = e.target.closest('#form-completar-clase');
        if (!formCompletarClase) return;

        const inputKmInicio = formCompletarClase.querySelector('#modal-clase-km-inicio');
        const inputKmFin = formCompletarClase.querySelector('#modal-clase-km-fin');
        const errorKm = formCompletarClase.querySelector('#error-validacion-km');

        if (inputKmInicio && inputKmFin) {
            const kmInicio = parseInt(inputKmInicio.value, 10);
            const kmFin = parseInt(inputKmFin.value, 10);

            if (isNaN(kmInicio) || isNaN(kmFin)) {
                e.preventDefault();
                e.stopImmediatePropagation();
                if (errorKm) {
                    errorKm.textContent = 'Debes introducir valores numéricos válidos de kilometraje.';
                    errorKm.classList.remove('hidden');
                }
                return;
            }

            if (kmFin < kmInicio) {
                e.preventDefault();
                e.stopImmediatePropagation();
                if (errorKm) {
                    errorKm.textContent = 'El kilometraje final (' + kmFin + ' km) no puede ser menor que el inicial (' + kmInicio + ' km).';
                    errorKm.classList.remove('hidden');
                }
                return;
            }
        }
    });

    // =========================================================================
    // 5. APERTURA DINÁMICA DE MODAL CALIFICAR EXAMEN
    // =========================================================================
    document.querySelectorAll('[data-calificar-examen]').forEach(btn => {
        btn.addEventListener('click', () => {
            const examenId = btn.getAttribute('data-calificar-examen');
            const alumnoNombre = btn.getAttribute('data-alumno-nombre');
            const fechaHora = btn.getAttribute('data-fecha-hora');

            const modal = document.getElementById('modal-calificar-examen');
            if (!modal) return;

            const inputId = document.getElementById('calificar-examen-id');
            const elAlumno = document.getElementById('calificar-examen-alumno');
            const elFecha = document.getElementById('calificar-examen-fecha');

            if (inputId) inputId.value = examenId;
            if (elAlumno) elAlumno.textContent = alumnoNombre || 'Alumno';
            if (elFecha) elFecha.textContent = fechaHora || '';

            modal.classList.remove('hidden');
            modal.classList.add('flex');
            document.body.classList.add('overflow-hidden');
        });
    });
});
