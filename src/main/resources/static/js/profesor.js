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
    // 3. ACTUALIZACIÓN REACTIVA DE LA CUADRÍCULA SEMANAL (HTMX)
    // =========================================================================
    document.body.addEventListener('actualizarCalendario', () => {
        document.body.classList.remove('overflow-hidden');
        const modal = document.getElementById('contenedor-modal');
        if (modal) {
            modal.innerHTML = '';
        }
    });

    // =========================================================================
    // 4. VALIDACIÓN TÉCNICA (kmFin > kmInicio) Y DOBLE CONFIRMACIÓN DE CLASE
    // =========================================================================
    function validarFormularioKilometraje(form) {
        const inputKmInicio = form.querySelector('#modal-clase-km-inicio');
        const inputKmFin = form.querySelector('#modal-clase-km-fin');
        const errorKm = form.querySelector('#error-validacion-km');

        if (!inputKmInicio || !inputKmFin) return true;

        const kmInicioStr = inputKmInicio.value.trim();
        const kmFinStr = inputKmFin.value.trim();

        if (kmInicioStr === '' || kmFinStr === '') {
            if (errorKm) {
                errorKm.textContent = 'Debes rellenar tanto el odómetro inicial como el odómetro final.';
                errorKm.classList.remove('hidden');
            }
            return false;
        }

        const kmInicio = parseInt(kmInicioStr, 10);
        const kmFin = parseInt(kmFinStr, 10);

        if (isNaN(kmInicio) || isNaN(kmFin)) {
            if (errorKm) {
                errorKm.textContent = 'Debes introducir valores numéricos válidos de kilometraje.';
                errorKm.classList.remove('hidden');
            }
            return false;
        }

        if (kmFin <= kmInicio) {
            if (errorKm) {
                errorKm.textContent = 'El kilometraje final (' + kmFin + ' km) debe ser estrictamente superior al inicial (' + kmInicio + ' km).';
                errorKm.classList.remove('hidden');
            }
            return false;
        }

        if (errorKm) {
            errorKm.classList.add('hidden');
            errorKm.textContent = '';
        }
        return true;
    }

    // Delegación de clics para la doble verificación interactiva
    document.addEventListener('click', (e) => {
        // 4.1. Abrir confirmación para consolidar clase
        const btnPreCompletar = e.target.closest('#btn-pre-completar-clase');
        if (btnPreCompletar) {
            const form = btnPreCompletar.closest('#form-completar-clase');
            if (!form) return;

            const esValido = validarFormularioKilometraje(form);
            if (!esValido) return;

            const contenedorPre = document.getElementById('contenedor-btn-pre-completar');
            const panelConfirmacion = document.getElementById('panel-confirmacion-completar');
            if (contenedorPre && panelConfirmacion) {
                contenedorPre.classList.add('hidden');
                panelConfirmacion.classList.remove('hidden');
            }
            return;
        }

        // 4.2. Cancelar panel de confirmación de consolidación
        const btnVolverCompletar = e.target.closest('#btn-cancelar-confirmacion-completar');
        if (btnVolverCompletar) {
            const contenedorPre = document.getElementById('contenedor-btn-pre-completar');
            const panelConfirmacion = document.getElementById('panel-confirmacion-completar');
            if (contenedorPre && panelConfirmacion) {
                panelConfirmacion.classList.add('hidden');
                contenedorPre.classList.remove('hidden');
            }
            return;
        }

        // 4.3. Abrir confirmación para cancelar clase
        const btnPreCancelar = e.target.closest('#btn-pre-cancelar-clase');
        if (btnPreCancelar) {
            const inputMotivo = document.getElementById('input-motivo-cancelacion-profesor');
            if (inputMotivo) {
                if (!inputMotivo.value.trim()) {
                    inputMotivo.focus();
                    if (inputMotivo.reportValidity) {
                        inputMotivo.reportValidity();
                    }
                    return;
                }
            }
            const panelCancelar = document.getElementById('panel-confirmacion-cancelar');
            if (panelCancelar) {
                btnPreCancelar.classList.add('hidden');
                panelCancelar.classList.remove('hidden');
            }
            return;
        }

        // 4.4. Cancelar panel de confirmación de cancelación
        const btnVolverCancelar = e.target.closest('#btn-cancelar-confirmacion-cancelar');
        if (btnVolverCancelar) {
            const btnPreCancelar = document.getElementById('btn-pre-cancelar-clase');
            const panelCancelar = document.getElementById('panel-confirmacion-cancelar');
            if (btnPreCancelar && panelCancelar) {
                panelCancelar.classList.add('hidden');
                btnPreCancelar.classList.remove('hidden');
            }
            return;
        }
    });

    // Validación preventiva en el evento submit de consolidar clase
    document.addEventListener('submit', (e) => {
        const formCompletarClase = e.target.closest('#form-completar-clase');
        if (!formCompletarClase) return;

        const esValido = validarFormularioKilometraje(formCompletarClase);
        if (!esValido) {
            e.preventDefault();
            e.stopImmediatePropagation();
            const contenedorPre = document.getElementById('contenedor-btn-pre-completar');
            const panelConfirmacion = document.getElementById('panel-confirmacion-completar');
            if (contenedorPre && panelConfirmacion) {
                panelConfirmacion.classList.add('hidden');
                contenedorPre.classList.remove('hidden');
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
