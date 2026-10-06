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
                const permisosExtra = fila.getAttribute('data-permisos') || '';
                const textoCompleto = (fila.textContent + ' ' + permisosExtra).toLowerCase();
                if (textoCompleto.includes(termino)) {
                    fila.style.display = '';
                } else {
                    fila.style.display = 'none';
                }
            });
        });
    });

    // =========================================================================
    // VALIDACIÓN Y CONTROL DE CALENDARIOS: MODAL ALTA DE PROFESOR
    // =========================================================================

    /**
     * Da formato YYYY-MM-DD a un objeto Date para atributos min y max de inputs tipo date.
     */
    function formatearFechaISO(fecha) {
        const anio = fecha.getFullYear();
        const mes = String(fecha.getMonth() + 1).padStart(2, '0');
        const dia = String(fecha.getDate()).padStart(2, '0');
        return `${anio}-${mes}-${dia}`;
    }

    /**
     * Configura dinámicamente las restricciones temporales en los calendarios nativos:
     * - Fecha de Nacimiento: entre 18 y 100 años respecto a hoy.
     * - Fecha de Contratación: +- 1 mes respecto a hoy tanto en alta como en edición.
     */
    function configurarRestriccionesFechasModalProfesor(sufijo = '') {
        const inputNacimiento = document.getElementById(`profesor-nacimiento${sufijo}`);
        const inputContratacion = document.getElementById(`profesor-fecha${sufijo}`);
        const hoy = new Date();

        if (inputNacimiento) {
            const hace18Anios = new Date(hoy.getFullYear() - 18, hoy.getMonth(), hoy.getDate());
            const hace100Anios = new Date(hoy.getFullYear() - 100, hoy.getMonth(), hoy.getDate());
            inputNacimiento.setAttribute('max', formatearFechaISO(hace18Anios));
            inputNacimiento.setAttribute('min', formatearFechaISO(hace100Anios));
        }

        if (inputContratacion && sufijo !== '-editar') {
            const hace1Mes = new Date(hoy.getFullYear(), hoy.getMonth() - 1, hoy.getDate());
            const en1Mes = new Date(hoy.getFullYear(), hoy.getMonth() + 1, hoy.getDate());
            inputContratacion.setAttribute('min', formatearFechaISO(hace1Mes));
            inputContratacion.setAttribute('max', formatearFechaISO(en1Mes));
        }
    }

    function mostrarErrorCampo(inputId, feedbackId, mensaje) {
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
    }

    function limpiarErrorCampo(inputId, feedbackId) {
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

    function validarNombreProfesor(sufijo = '') {
        const inputId = `profesor-nombre${sufijo}`;
        const feedbackId = `feedback-nombre${sufijo}`;
        const input = document.getElementById(inputId);
        if (!input) return true;

        const valor = input.value?.trim();
        if (!valor) {
            mostrarErrorCampo(inputId, feedbackId, 'El nombre es obligatorio.');
            return false;
        }
        if (valor.length > 50) {
            mostrarErrorCampo(inputId, feedbackId, 'El nombre no puede superar los 50 caracteres.');
            return false;
        }
        limpiarErrorCampo(inputId, feedbackId);
        return true;
    }

    function validarApellidosProfesor(sufijo = '') {
        const inputId = `profesor-apellidos${sufijo}`;
        const feedbackId = `feedback-apellidos${sufijo}`;
        const input = document.getElementById(inputId);
        if (!input) return true;

        const valor = input.value?.trim();
        if (!valor) {
            mostrarErrorCampo(inputId, feedbackId, 'Los apellidos son obligatorios.');
            return false;
        }
        if (valor.length > 100) {
            mostrarErrorCampo(inputId, feedbackId, 'Los apellidos no pueden superar los 100 caracteres.');
            return false;
        }
        limpiarErrorCampo(inputId, feedbackId);
        return true;
    }

    function validarDniProfesor(sufijo = '') {
        const inputId = `profesor-dni${sufijo}`;
        const feedbackId = `feedback-dni${sufijo}`;
        const input = document.getElementById(inputId);
        if (!input) return true;

        const valor = input.value?.trim().toUpperCase();
        if (!valor) {
            mostrarErrorCampo(inputId, feedbackId, 'El DNI / NIE es obligatorio.');
            return false;
        }
        const regexDni = /^[0-9]{8}[A-Za-z]$|^[XYZxyz][0-9]{7}[A-Za-z]$/;
        if (!regexDni.test(valor)) {
            mostrarErrorCampo(inputId, feedbackId, 'El formato del DNI/NIE no es válido (ej. 12345678Z o X1234567Z).');
            return false;
        }
        const feedback = document.getElementById(feedbackId);
        if (!feedback || !feedback.querySelector('.mensaje-error-campo')) {
            limpiarErrorCampo(inputId, feedbackId);
        }
        return true;
    }

    function validarTelefonoProfesor(sufijo = '') {
        const inputId = `profesor-telefono${sufijo}`;
        const feedbackId = `feedback-telefono${sufijo}`;
        const input = document.getElementById(inputId);
        if (!input) return true;

        const valor = input.value?.trim();
        if (!valor) {
            mostrarErrorCampo(inputId, feedbackId, 'El teléfono móvil es obligatorio.');
            return false;
        }
        const regexTel = /^(\+34|0034)?[6789]\d{8}$/;
        if (!regexTel.test(valor)) {
            mostrarErrorCampo(inputId, feedbackId, 'El formato del teléfono no es válido (ej. 600123456).');
            return false;
        }
        const feedback = document.getElementById(feedbackId);
        if (!feedback || !feedback.querySelector('.mensaje-error-campo')) {
            limpiarErrorCampo(inputId, feedbackId);
        }
        return true;
    }

    function validarCorreoProfesor(sufijo = '') {
        const inputId = `profesor-correo${sufijo}`;
        const feedbackId = `feedback-correo${sufijo}`;
        const input = document.getElementById(inputId);
        if (!input) return true;

        const valor = input.value?.trim();
        if (!valor) {
            mostrarErrorCampo(inputId, feedbackId, 'El correo electrónico es obligatorio.');
            return false;
        }
        const regexCorreo = /^[A-Za-z0-9+_.-]+@(.+)$/;
        if (!regexCorreo.test(valor)) {
            mostrarErrorCampo(inputId, feedbackId, 'El formato del correo electrónico no es válido.');
            return false;
        }
        if (valor.length > 50) {
            mostrarErrorCampo(inputId, feedbackId, 'El correo no puede superar los 50 caracteres.');
            return false;
        }
        const feedback = document.getElementById(feedbackId);
        if (!feedback || !feedback.querySelector('.mensaje-error-campo')) {
            limpiarErrorCampo(inputId, feedbackId);
        }
        return true;
    }

    function validarDireccionProfesor(sufijo = '') {
        const inputId = `profesor-direccion${sufijo}`;
        const feedbackId = `feedback-direccion${sufijo}`;
        const input = document.getElementById(inputId);
        if (!input) return true;

        const valor = input.value?.trim();
        if (!valor) {
            mostrarErrorCampo(inputId, feedbackId, 'La dirección de residencia es obligatoria.');
            return false;
        }
        if (valor.length > 150) {
            mostrarErrorCampo(inputId, feedbackId, 'La dirección no puede superar los 150 caracteres.');
            return false;
        }
        limpiarErrorCampo(inputId, feedbackId);
        return true;
    }

    function validarFechaNacimientoProfesor(sufijo = '') {
        const inputId = `profesor-nacimiento${sufijo}`;
        const feedbackId = `feedback-nacimiento${sufijo}`;
        const input = document.getElementById(inputId);
        if (!input) return true;

        const valor = input.value?.trim();
        if (!valor) {
            mostrarErrorCampo(inputId, feedbackId, 'La fecha de nacimiento es obligatoria.');
            return false;
        }

        const partes = valor.split('-');
        if (partes.length !== 3) {
            mostrarErrorCampo(inputId, feedbackId, 'Formato de fecha inválido.');
            return false;
        }

        const anio = parseInt(partes[0], 10);
        const mes = parseInt(partes[1], 10) - 1;
        const dia = parseInt(partes[2], 10);
        const fechaNac = new Date(anio, mes, dia);

        if (isNaN(fechaNac.getTime())) {
            mostrarErrorCampo(inputId, feedbackId, 'Introduce una fecha de nacimiento válida.');
            return false;
        }

        const hoy = new Date();
        const hoySinHora = new Date(hoy.getFullYear(), hoy.getMonth(), hoy.getDate());

        if (fechaNac > hoySinHora) {
            mostrarErrorCampo(inputId, feedbackId, 'La fecha de nacimiento debe ser una fecha pasada.');
            return false;
        }

        const hace100Anios = new Date(hoy.getFullYear() - 100, hoy.getMonth(), hoy.getDate());
        if (fechaNac < hace100Anios) {
            mostrarErrorCampo(inputId, feedbackId, 'La fecha de nacimiento no puede ser anterior a hace 100 años.');
            return false;
        }

        const fechaMinima18 = new Date(anio + 18, mes, dia);
        if (hoySinHora < fechaMinima18) {
            mostrarErrorCampo(inputId, feedbackId, 'El profesor debe ser mayor de edad (al menos 18 años).');
            return false;
        }

        limpiarErrorCampo(inputId, feedbackId);
        return true;
    }

    function validarFechaContratacionProfesor(sufijo = '') {
        const inputId = `profesor-fecha${sufijo}`;
        const feedbackId = `feedback-fecha-contratacion${sufijo}`;
        const input = document.getElementById(inputId);
        if (!input) return true;

        const valor = input.value?.trim();
        if (!valor) {
            mostrarErrorCampo(inputId, feedbackId, 'La fecha de contratación es obligatoria.');
            return false;
        }

        const partes = valor.split('-');
        if (partes.length !== 3) {
            mostrarErrorCampo(inputId, feedbackId, 'Formato de fecha inválido.');
            return false;
        }

        const fechaContrato = new Date(parseInt(partes[0], 10), parseInt(partes[1], 10) - 1, parseInt(partes[2], 10));
        if (isNaN(fechaContrato.getTime())) {
            mostrarErrorCampo(inputId, feedbackId, 'Introduce una fecha de contratación válida.');
            return false;
        }

        const hoy = new Date();
        const hace1Mes = new Date(hoy.getFullYear(), hoy.getMonth() - 1, hoy.getDate());
        const en1Mes = new Date(hoy.getFullYear(), hoy.getMonth() + 1, hoy.getDate());

        // Restricción a +- 1 mes respecto a la fecha actual tanto en alta como en edición
        if (fechaContrato < hace1Mes || fechaContrato > en1Mes) {
            mostrarErrorCampo(inputId, feedbackId, 'La fecha de contratación debe estar comprendida entre 1 mes antes y 1 mes después de la fecha actual.');
            return false;
        }

        limpiarErrorCampo(inputId, feedbackId);
        return true;
    }

    function validarPermisosProfesor(modalId = 'modal-alta-profesor', sufijo = '') {
        const modal = document.getElementById(modalId);
        if (!modal) return true;

        const checkboxes = modal.querySelectorAll('input[name="permisos"]:checked');
        const feedbackId = `feedback-permisos${sufijo}`;
        const feedback = document.getElementById(feedbackId);

        if (checkboxes.length === 0) {
            if (feedback) {
                feedback.innerHTML = `
                    <div class="mensaje-error-campo flex items-center space-x-1.5 text-xs text-red-600 dark:text-red-400 mt-1 font-medium fade-in">
                        <svg class="w-3.5 h-3.5 shrink-0 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                        </svg>
                        <span>Debe seleccionar al menos un permiso de conducción autorizado.</span>
                    </div>
                `;
            }
            return false;
        }

        if (feedback) {
            feedback.innerHTML = '';
        }
        return true;
    }

    function validarCompatibilidadVehiculo(modalId = 'modal-alta-profesor', sufijo = '') {
        const selectId = `profesor-vehiculo${sufijo}`;
        const feedbackId = `feedback-vehiculo-compatibilidad${sufijo}`;
        const selectVehiculo = document.getElementById(selectId);
        const feedback = document.getElementById(feedbackId);
        if (!selectVehiculo || selectVehiculo.disabled) {
            if (feedback) feedback.innerHTML = '';
            return true;
        }

        const selectedOption = selectVehiculo.options[selectVehiculo.selectedIndex];
        const tipoVehiculo = selectedOption ? selectedOption.getAttribute('data-tipo') : null;
        const nombreTipo = selectedOption ? (selectedOption.getAttribute('data-tipo-nombre') || tipoVehiculo) : '';

        // Si no hay vehículo seleccionado ("-- Sin Vehículo Asignado --"), es válido
        if (!tipoVehiculo || selectVehiculo.value === '') {
            if (feedback) feedback.innerHTML = '';
            selectVehiculo.classList.remove('border-red-500', 'focus:ring-red-500', 'dark:border-red-500');
            selectVehiculo.classList.add('border-slate-300', 'dark:border-slate-700', 'focus:ring-blue-600');
            selectVehiculo.setCustomValidity('');
            return true;
        }

        // Comprobar si entre los checkboxes de permisos marcados está el carnet del vehículo
        const modal = document.getElementById(modalId);
        const checkboxCarnet = modal ? modal.querySelector(`input[name="permisos"][value="${tipoVehiculo}"]`) : null;
        const estaMarcado = checkboxCarnet && checkboxCarnet.checked;

        if (!estaMarcado) {
            const mensaje = `El vehículo seleccionado requiere ${nombreTipo}, pero este permiso no está seleccionado en los permisos autorizados.`;
            if (feedback) {
                feedback.innerHTML = `
                    <div class="mensaje-error-campo flex items-start space-x-1.5 text-xs text-red-600 dark:text-red-400 mt-1.5 font-medium fade-in">
                        <svg class="w-3.5 h-3.5 shrink-0 text-red-500 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                        </svg>
                        <span>${mensaje}</span>
                    </div>
                `;
            }
            selectVehiculo.classList.add('border-red-500', 'focus:ring-red-500', 'dark:border-red-500');
            selectVehiculo.classList.remove('border-slate-300', 'dark:border-slate-700', 'focus:ring-blue-600');
            selectVehiculo.setCustomValidity(mensaje);
            return false;
        }

        if (feedback) {
            feedback.innerHTML = '';
        }
        selectVehiculo.classList.remove('border-red-500', 'focus:ring-red-500', 'dark:border-red-500');
        selectVehiculo.classList.add('border-slate-300', 'dark:border-slate-700', 'focus:ring-blue-600');
        selectVehiculo.setCustomValidity('');
        return true;
    }

    function validarFormularioProfesor(formId, modalId, sufijo = '') {
        const form = document.getElementById(formId);
        if (!form) return true;

        const vNombre = validarNombreProfesor(sufijo);
        const vApellidos = validarApellidosProfesor(sufijo);
        const vDni = validarDniProfesor(sufijo);
        const vTelefono = validarTelefonoProfesor(sufijo);
        const vCorreo = validarCorreoProfesor(sufijo);
        const vNacimiento = validarFechaNacimientoProfesor(sufijo);
        const vDireccion = validarDireccionProfesor(sufijo);
        const vContratacion = sufijo === '-editar' ? true : validarFechaContratacionProfesor(sufijo);
        const vPermisos = validarPermisosProfesor(modalId, sufijo);
        const vVehiculo = validarCompatibilidadVehiculo(modalId, sufijo);

        const errorActivo = form.querySelector('.mensaje-error-campo');

        if (!vNombre || !vApellidos || !vDni || !vTelefono || !vCorreo || !vNacimiento || !vDireccion || !vContratacion || !vPermisos || !vVehiculo || errorActivo) {
            const primerInvalido = form.querySelector('.border-red-500') || form.querySelector(':invalid');
            if (primerInvalido) {
                primerInvalido.focus();
            }
            return false;
        }

        return true;
    }

    function inicializarEventosModalProfesorGenerico(modalId, formId, sufijo = '') {
        const modal = document.getElementById(modalId);
        const form = document.getElementById(formId);
        if (!modal && !form) return;

        configurarRestriccionesFechasModalProfesor(sufijo);

        if (form) {
            if (form._eventosProfesorInicializados) return;
            form._eventosProfesorInicializados = true;
        }

        const inputNombre = document.getElementById(`profesor-nombre${sufijo}`);
        if (inputNombre) {
            inputNombre.addEventListener('blur', () => validarNombreProfesor(sufijo));
            inputNombre.addEventListener('input', () => {
                if (inputNombre.classList.contains('border-red-500') || inputNombre.value.trim()) {
                    validarNombreProfesor(sufijo);
                }
            });
        }

        const inputApellidos = document.getElementById(`profesor-apellidos${sufijo}`);
        if (inputApellidos) {
            inputApellidos.addEventListener('blur', () => validarApellidosProfesor(sufijo));
            inputApellidos.addEventListener('input', () => {
                if (inputApellidos.classList.contains('border-red-500') || inputApellidos.value.trim()) {
                    validarApellidosProfesor(sufijo);
                }
            });
        }

        const inputDni = document.getElementById(`profesor-dni${sufijo}`);
        if (inputDni) {
            inputDni.addEventListener('blur', () => validarDniProfesor(sufijo));
            inputDni.addEventListener('input', () => {
                if (inputDni.classList.contains('border-red-500') || inputDni.value.trim()) {
                    validarDniProfesor(sufijo);
                }
            });
        }

        const inputTelefono = document.getElementById(`profesor-telefono${sufijo}`);
        if (inputTelefono) {
            inputTelefono.addEventListener('blur', () => validarTelefonoProfesor(sufijo));
            inputTelefono.addEventListener('input', () => {
                if (inputTelefono.classList.contains('border-red-500') || inputTelefono.value.trim()) {
                    validarTelefonoProfesor(sufijo);
                }
            });
        }

        const inputCorreo = document.getElementById(`profesor-correo${sufijo}`);
        if (inputCorreo) {
            inputCorreo.addEventListener('blur', () => validarCorreoProfesor(sufijo));
            inputCorreo.addEventListener('input', () => {
                if (inputCorreo.classList.contains('border-red-500') || inputCorreo.value.trim()) {
                    validarCorreoProfesor(sufijo);
                }
            });
        }

        const inputNacimiento = document.getElementById(`profesor-nacimiento${sufijo}`);
        if (inputNacimiento) {
            inputNacimiento.addEventListener('change', () => validarFechaNacimientoProfesor(sufijo));
            inputNacimiento.addEventListener('blur', () => validarFechaNacimientoProfesor(sufijo));
            inputNacimiento.addEventListener('input', () => {
                if (inputNacimiento.classList.contains('border-red-500')) {
                    validarFechaNacimientoProfesor(sufijo);
                }
            });
        }

        const inputDireccion = document.getElementById(`profesor-direccion${sufijo}`);
        if (inputDireccion) {
            inputDireccion.addEventListener('blur', () => validarDireccionProfesor(sufijo));
            inputDireccion.addEventListener('input', () => {
                if (inputDireccion.classList.contains('border-red-500') || inputDireccion.value.trim()) {
                    validarDireccionProfesor(sufijo);
                }
            });
        }

        const inputContratacion = document.getElementById(`profesor-fecha${sufijo}`);
        if (inputContratacion && sufijo !== '-editar') {
            inputContratacion.addEventListener('change', () => validarFechaContratacionProfesor(sufijo));
            inputContratacion.addEventListener('blur', () => validarFechaContratacionProfesor(sufijo));
            inputContratacion.addEventListener('input', () => {
                if (inputContratacion.classList.contains('border-red-500')) {
                    validarFechaContratacionProfesor(sufijo);
                }
            });
        }

        const selectVehiculo = document.getElementById(`profesor-vehiculo${sufijo}`);
        if (selectVehiculo) {
            selectVehiculo.addEventListener('change', () => validarCompatibilidadVehiculo(modalId, sufijo));
        }

        if (modal) {
            modal.addEventListener('change', (e) => {
                if (e.target && e.target.name === 'permisos') {
                    validarPermisosProfesor(modalId, sufijo);
                    validarCompatibilidadVehiculo(modalId, sufijo);
                }
            });
        }

        validarCompatibilidadVehiculo(modalId, sufijo);

        if (form) {
            form.addEventListener('submit', (e) => {
                if (!validarFormularioProfesor(formId, modalId, sufijo)) {
                    e.preventDefault();
                    e.stopImmediatePropagation();
                }
            });
        }
    }

    function inicializarEventosModalesProfesor() {
        inicializarEventosModalProfesorGenerico('modal-alta-profesor', 'form-alta-profesor', '');
        inicializarEventosModalProfesorGenerico('modal-editar-profesor', 'form-editar-profesor', '-editar');
    }

    // =========================================================================
    // VALIDACIÓN Y CONTROL DE CAMPOS: MODAL ALTA DE VEHÍCULO
    // =========================================================================

    function validarMatriculaVehiculo() {
        const input = document.getElementById('vehiculo-matricula');
        const feedbackId = 'feedback-matricula';
        if (!input) return true;

        const valor = input.value.trim().toUpperCase();
        if (!valor) {
            mostrarErrorCampo('vehiculo-matricula', feedbackId, 'La matrícula es obligatoria.');
            return false;
        }

        const regexMatricula = /^[0-9]{4}[ -]?[A-Za-z]{3}$/;
        if (!regexMatricula.test(valor)) {
            mostrarErrorCampo('vehiculo-matricula', feedbackId, 'Formato de matrícula inválido (ej. 1234-LMN o 1234LMN).');
            return false;
        }

        const feedback = document.getElementById(feedbackId);
        if (!feedback || !feedback.querySelector('.mensaje-error-campo')) {
            limpiarErrorCampo('vehiculo-matricula', feedbackId);
        }
        return true;
    }

    function validarMarcaVehiculo() {
        const input = document.getElementById('vehiculo-marca');
        const feedbackId = 'feedback-marca';
        if (!input) return true;

        const valor = input.value.trim();
        if (!valor) {
            mostrarErrorCampo('vehiculo-marca', feedbackId, 'La marca es obligatoria.');
            return false;
        }
        if (valor.length > 50) {
            mostrarErrorCampo('vehiculo-marca', feedbackId, 'La marca no puede superar los 50 caracteres.');
            return false;
        }

        limpiarErrorCampo('vehiculo-marca', feedbackId);
        return true;
    }

    function validarModeloVehiculo() {
        const input = document.getElementById('vehiculo-modelo');
        const feedbackId = 'feedback-modelo';
        if (!input) return true;

        const valor = input.value.trim();
        if (!valor) {
            mostrarErrorCampo('vehiculo-modelo', feedbackId, 'El modelo es obligatorio.');
            return false;
        }
        if (valor.length > 50) {
            mostrarErrorCampo('vehiculo-modelo', feedbackId, 'El modelo no puede superar los 50 caracteres.');
            return false;
        }

        limpiarErrorCampo('vehiculo-modelo', feedbackId);
        return true;
    }

    function validarColorVehiculo() {
        const input = document.getElementById('vehiculo-color');
        const feedbackId = 'feedback-color';
        if (!input) return true;

        const valor = input.value.trim();
        if (!valor) {
            mostrarErrorCampo('vehiculo-color', feedbackId, 'El color es obligatorio.');
            return false;
        }
        if (valor.length > 30) {
            mostrarErrorCampo('vehiculo-color', feedbackId, 'El color no puede superar los 30 caracteres.');
            return false;
        }

        limpiarErrorCampo('vehiculo-color', feedbackId);
        return true;
    }

    function validarAnioVehiculo() {
        const input = document.getElementById('vehiculo-anio');
        const feedbackId = 'feedback-anio';
        if (!input) return true;

        const valorStr = input.value.trim();
        if (!valorStr) {
            mostrarErrorCampo('vehiculo-anio', feedbackId, 'El año de matriculación es obligatorio.');
            return false;
        }

        const anio = parseInt(valorStr, 10);
        const anioActual = new Date().getFullYear();
        if (isNaN(anio) || anio < 1990 || anio > anioActual) {
            mostrarErrorCampo('vehiculo-anio', feedbackId, `El año de matriculación debe estar comprendido entre 1990 y ${anioActual}.`);
            return false;
        }

        limpiarErrorCampo('vehiculo-anio', feedbackId);
        return true;
    }

    function validarCvVehiculo() {
        const input = document.getElementById('vehiculo-cv');
        const feedbackId = 'feedback-cv';
        if (!input) return true;

        const valorStr = input.value.trim();
        if (!valorStr) {
            mostrarErrorCampo('vehiculo-cv', feedbackId, 'La potencia del motor es obligatoria.');
            return false;
        }

        const cv = parseInt(valorStr, 10);
        if (isNaN(cv) || cv < 0 || cv > 1000) {
            mostrarErrorCampo('vehiculo-cv', feedbackId, 'La potencia debe ser un valor válido entre 0 y 1000 CV.');
            return false;
        }

        limpiarErrorCampo('vehiculo-cv', feedbackId);
        return true;
    }

    function validarKmVehiculo() {
        const input = document.getElementById('vehiculo-km');
        const feedbackId = 'feedback-km';
        if (!input) return true;

        const valorStr = input.value.trim();
        if (!valorStr) {
            mostrarErrorCampo('vehiculo-km', feedbackId, 'El kilometraje inicial es obligatorio.');
            return false;
        }

        const km = parseInt(valorStr, 10);
        if (isNaN(km) || km < 0) {
            mostrarErrorCampo('vehiculo-km', feedbackId, 'El kilometraje inicial no puede ser negativo.');
            return false;
        }

        limpiarErrorCampo('vehiculo-km', feedbackId);
        return true;
    }

    /**
     * Configura y sincroniza dinámicamente las restricciones temporales (min y max) en los inputs del modal de vehículo:
     * - Año de matriculación: entre 1990 y el año actual.
     * - Última revisión: entre hace 4 años y hoy (y estrictamente anterior a la próxima revisión si ya está elegida).
     * - Próxima revisión / ITV: entre hoy (o día posterior a última revisión) y un máximo de 10 años en el futuro.
     */
    function configurarRestriccionesFechasModalVehiculo() {
        const inputAnio = document.getElementById('vehiculo-anio');
        const inputUltima = document.getElementById('vehiculo-fecha-ultima');
        const inputProxima = document.getElementById('vehiculo-fecha-proxima');
        const hoy = new Date();

        if (inputAnio) {
            inputAnio.setAttribute('min', '1990');
            inputAnio.setAttribute('max', hoy.getFullYear().toString());
        }

        const hace4Anios = new Date(hoy.getFullYear() - 4, hoy.getMonth(), hoy.getDate());
        const en10Anios = new Date(hoy.getFullYear() + 10, hoy.getMonth(), hoy.getDate());

        if (inputUltima) {
            inputUltima.setAttribute('min', formatearFechaISO(hace4Anios));
            let maxUltima = new Date(hoy.getFullYear(), hoy.getMonth(), hoy.getDate());
            if (inputProxima && inputProxima.value.trim()) {
                const partesProxima = inputProxima.value.trim().split('-');
                if (partesProxima.length === 3) {
                    const fProx = new Date(parseInt(partesProxima[0], 10), parseInt(partesProxima[1], 10) - 1, parseInt(partesProxima[2], 10));
                    if (!isNaN(fProx.getTime())) {
                        const diaAntes = new Date(fProx.getFullYear(), fProx.getMonth(), fProx.getDate() - 1);
                        if (diaAntes < maxUltima) {
                            maxUltima = diaAntes;
                        }
                    }
                }
            }
            inputUltima.setAttribute('max', formatearFechaISO(maxUltima));
        }

        if (inputProxima) {
            inputProxima.setAttribute('max', formatearFechaISO(en10Anios));
            let minProxima = new Date(hoy.getFullYear(), hoy.getMonth(), hoy.getDate());
            if (inputUltima && inputUltima.value.trim()) {
                const partesUltima = inputUltima.value.trim().split('-');
                if (partesUltima.length === 3) {
                    const fUlt = new Date(parseInt(partesUltima[0], 10), parseInt(partesUltima[1], 10) - 1, parseInt(partesUltima[2], 10));
                    if (!isNaN(fUlt.getTime())) {
                        const diaDespues = new Date(fUlt.getFullYear(), fUlt.getMonth(), fUlt.getDate() + 1);
                        minProxima = diaDespues;
                    }
                }
            }
            inputProxima.setAttribute('min', formatearFechaISO(minProxima));
        }
    }

    function validarFechasRevisionVehiculo() {
        const inputUltima = document.getElementById('vehiculo-fecha-ultima');
        const inputProxima = document.getElementById('vehiculo-fecha-proxima');
        const feedbackProximaId = 'feedback-fecha-proxima';
        const feedbackUltimaId = 'feedback-fecha-ultima';
        if (!inputProxima) return true;

        configurarRestriccionesFechasModalVehiculo();

        const hoy = new Date();
        const hoySinHora = new Date(hoy.getFullYear(), hoy.getMonth(), hoy.getDate());
        const en10Anios = new Date(hoy.getFullYear() + 10, hoy.getMonth(), hoy.getDate());
        const hace4Anios = new Date(hoy.getFullYear() - 4, hoy.getMonth(), hoy.getDate());

        let esUltimaValida = true;
        let fechaUltima = null;

        if (inputUltima && inputUltima.value.trim()) {
            const valUltima = inputUltima.value.trim();
            const partesUltima = valUltima.split('-');
            if (partesUltima.length !== 3) {
                mostrarErrorCampo('vehiculo-fecha-ultima', feedbackUltimaId, 'Formato de fecha inválido.');
                esUltimaValida = false;
            } else {
                fechaUltima = new Date(parseInt(partesUltima[0], 10), parseInt(partesUltima[1], 10) - 1, parseInt(partesUltima[2], 10));
                if (isNaN(fechaUltima.getTime())) {
                    mostrarErrorCampo('vehiculo-fecha-ultima', feedbackUltimaId, 'Introduce una fecha de última revisión válida.');
                    esUltimaValida = false;
                } else if (fechaUltima > hoySinHora) {
                    mostrarErrorCampo('vehiculo-fecha-ultima', feedbackUltimaId, 'La fecha de la última revisión no puede ser posterior a hoy.');
                    esUltimaValida = false;
                } else if (fechaUltima < hace4Anios) {
                    mostrarErrorCampo('vehiculo-fecha-ultima', feedbackUltimaId, 'La fecha de la última revisión no puede ser anterior a hace 4 años.');
                    esUltimaValida = false;
                } else {
                    limpiarErrorCampo('vehiculo-fecha-ultima', feedbackUltimaId);
                }
            }
        } else if (inputUltima) {
            limpiarErrorCampo('vehiculo-fecha-ultima', feedbackUltimaId);
        }

        let esProximaValida = true;
        let fechaProxima = null;
        const valProxima = inputProxima.value.trim();

        if (!valProxima) {
            mostrarErrorCampo('vehiculo-fecha-proxima', feedbackProximaId, 'La fecha de próxima revisión o ITV es obligatoria.');
            esProximaValida = false;
        } else {
            const partesProxima = valProxima.split('-');
            if (partesProxima.length !== 3) {
                mostrarErrorCampo('vehiculo-fecha-proxima', feedbackProximaId, 'Formato de fecha inválido.');
                esProximaValida = false;
            } else {
                fechaProxima = new Date(parseInt(partesProxima[0], 10), parseInt(partesProxima[1], 10) - 1, parseInt(partesProxima[2], 10));
                if (isNaN(fechaProxima.getTime())) {
                    mostrarErrorCampo('vehiculo-fecha-proxima', feedbackProximaId, 'Introduce una fecha de próxima revisión válida.');
                    esProximaValida = false;
                } else if (fechaProxima > en10Anios) {
                    mostrarErrorCampo('vehiculo-fecha-proxima', feedbackProximaId, 'La fecha de la próxima revisión no puede superar los 10 años en el futuro.');
                    esProximaValida = false;
                } else if (fechaProxima < hoySinHora) {
                    mostrarErrorCampo('vehiculo-fecha-proxima', feedbackProximaId, 'La fecha de la próxima revisión no puede ser anterior a la fecha actual.');
                    esProximaValida = false;
                } else {
                    limpiarErrorCampo('vehiculo-fecha-proxima', feedbackProximaId);
                }
            }
        }

        if (esUltimaValida && esProximaValida && fechaUltima && fechaProxima && fechaProxima <= fechaUltima) {
            mostrarErrorCampo('vehiculo-fecha-proxima', feedbackProximaId, 'La fecha de la próxima revisión debe ser estrictamente posterior a la fecha de la última revisión.');
            esProximaValida = false;
        }

        return esUltimaValida && esProximaValida;
    }

    function validarFormularioAltaVehiculo() {
        const form = document.getElementById('form-alta-vehiculo');
        if (!form) return true;

        const vMatricula = validarMatriculaVehiculo();
        const vMarca = validarMarcaVehiculo();
        const vModelo = validarModeloVehiculo();
        const vColor = validarColorVehiculo();
        const vAnio = validarAnioVehiculo();
        const vCv = validarCvVehiculo();
        const vKm = validarKmVehiculo();
        const vFechas = validarFechasRevisionVehiculo();

        const errorActivo = form.querySelector('.mensaje-error-campo');

        if (!vMatricula || !vMarca || !vModelo || !vColor || !vAnio || !vCv || !vKm || !vFechas || errorActivo) {
            const primerInvalido = form.querySelector('.border-red-500') || form.querySelector(':invalid');
            if (primerInvalido) {
                primerInvalido.focus();
            }
            return false;
        }

        return true;
    }

    function inicializarEventosModalVehiculo() {
        const form = document.getElementById('form-alta-vehiculo');
        if (!form) return;

        if (form._eventosVehiculoInicializados) return;
        form._eventosVehiculoInicializados = true;

        // Configurar restricciones dinámicas de fechas y límites cruzados (min y max)
        configurarRestriccionesFechasModalVehiculo();

        // Listeners interactivos
        const inputMatricula = document.getElementById('vehiculo-matricula');
        if (inputMatricula) {
            inputMatricula.addEventListener('blur', validarMatriculaVehiculo);
            inputMatricula.addEventListener('input', () => {
                if (inputMatricula.classList.contains('border-red-500') || inputMatricula.value.trim()) {
                    validarMatriculaVehiculo();
                }
            });
        }

        const inputMarca = document.getElementById('vehiculo-marca');
        if (inputMarca) {
            inputMarca.addEventListener('blur', validarMarcaVehiculo);
            inputMarca.addEventListener('input', () => {
                if (inputMarca.classList.contains('border-red-500') || inputMarca.value.trim()) {
                    validarMarcaVehiculo();
                }
            });
        }

        const inputModelo = document.getElementById('vehiculo-modelo');
        if (inputModelo) {
            inputModelo.addEventListener('blur', validarModeloVehiculo);
            inputModelo.addEventListener('input', () => {
                if (inputModelo.classList.contains('border-red-500') || inputModelo.value.trim()) {
                    validarModeloVehiculo();
                }
            });
        }

        const inputColor = document.getElementById('vehiculo-color');
        if (inputColor) {
            inputColor.addEventListener('blur', validarColorVehiculo);
            inputColor.addEventListener('input', () => {
                if (inputColor.classList.contains('border-red-500') || inputColor.value.trim()) {
                    validarColorVehiculo();
                }
            });
        }

        const inputAnio = document.getElementById('vehiculo-anio');
        if (inputAnio) {
            inputAnio.addEventListener('blur', validarAnioVehiculo);
            inputAnio.addEventListener('input', () => {
                if (inputAnio.classList.contains('border-red-500') || inputAnio.value.trim()) {
                    validarAnioVehiculo();
                }
            });
        }

        const inputCv = document.getElementById('vehiculo-cv');
        if (inputCv) {
            inputCv.addEventListener('blur', validarCvVehiculo);
            inputCv.addEventListener('input', () => {
                if (inputCv.classList.contains('border-red-500') || inputCv.value.trim()) {
                    validarCvVehiculo();
                }
            });
        }

        const inputKm = document.getElementById('vehiculo-km');
        if (inputKm) {
            inputKm.addEventListener('blur', validarKmVehiculo);
            inputKm.addEventListener('input', () => {
                if (inputKm.classList.contains('border-red-500') || inputKm.value.trim()) {
                    validarKmVehiculo();
                }
            });
        }

        const inputProxima = document.getElementById('vehiculo-fecha-proxima');
        if (inputProxima) {
            inputProxima.addEventListener('change', validarFechasRevisionVehiculo);
            inputProxima.addEventListener('input', validarFechasRevisionVehiculo);
            inputProxima.addEventListener('blur', validarFechasRevisionVehiculo);
        }

        const inputUltima = document.getElementById('vehiculo-fecha-ultima');
        if (inputUltima) {
            inputUltima.addEventListener('change', validarFechasRevisionVehiculo);
            inputUltima.addEventListener('input', validarFechasRevisionVehiculo);
            inputUltima.addEventListener('blur', validarFechasRevisionVehiculo);
        }

        form.addEventListener('submit', (e) => {
            if (!validarFormularioAltaVehiculo()) {
                e.preventDefault();
                e.stopImmediatePropagation();
            } else {
                // Deshabilitar botón para prevenir doble envío accidental
                const btnSubmit = document.getElementById('btn-submit-alta-vehiculo');
                if (btnSubmit) {
                    btnSubmit.disabled = true;
                }
            }
        });
    }

    // Interceptar evento HTMX para abortar la petición si no pasa las validaciones de cliente
    document.body.addEventListener('htmx:confirm', (e) => {
        if (e.target && e.target.id === 'form-alta-profesor') {
            if (!validarFormularioProfesor('form-alta-profesor', 'modal-alta-profesor', '')) {
                e.preventDefault();
            }
        } else if (e.target && e.target.id === 'form-editar-profesor') {
            if (!validarFormularioProfesor('form-editar-profesor', 'modal-editar-profesor', '-editar')) {
                e.preventDefault();
            }
        } else if (e.target && e.target.id === 'form-alta-vehiculo') {
            if (!validarFormularioAltaVehiculo()) {
                e.preventDefault();
            } else {
                // Deshabilitar botón para prevenir doble alta durante la petición HTMX
                const btnSubmit = document.getElementById('btn-submit-alta-vehiculo');
                if (btnSubmit) {
                    btnSubmit.disabled = true;
                }
            }
        }
    });

    // Re-habilitar botón de envío de vehículo en caso de fallo de red en HTMX
    document.body.addEventListener('htmx:responseError', (e) => {
        if (e.target && e.target.id === 'form-alta-vehiculo') {
            const btnSubmit = document.getElementById('btn-submit-alta-vehiculo');
            if (btnSubmit) btnSubmit.disabled = false;
        }
    });
    document.body.addEventListener('htmx:sendError', (e) => {
        if (e.target && e.target.id === 'form-alta-vehiculo') {
            const btnSubmit = document.getElementById('btn-submit-alta-vehiculo');
            if (btnSubmit) btnSubmit.disabled = false;
        }
    });

    // Sincronizar clases visuales de inputs tras peticiones HTMX (/usuario/validar-*) y al reabrir los modales
    document.body.addEventListener('htmx:afterSwap', (event) => {
        const targetId = event.detail.target?.id;

        // Feedback de campos individuales (soporta modales alta/edición profesor y perfil admin)
        if (targetId && targetId.startsWith('feedback-')) {
            const campoNombre = targetId.replace('feedback-', '');
            const input = document.getElementById(`profesor-${campoNombre}`) || document.getElementById(`vehiculo-${campoNombre}`) || document.getElementById(campoNombre);
            const tieneError = event.detail.target.querySelector('.mensaje-error-campo') !== null;

            if (input) {
                if (tieneError) {
                    input.classList.add('border-red-500', 'focus:ring-red-500', 'dark:border-red-500');
                    input.classList.remove('border-slate-300', 'dark:border-slate-700', 'focus:ring-blue-600');
                } else {
                    input.classList.remove('border-red-500', 'focus:ring-red-500', 'dark:border-red-500');
                    input.classList.add('border-slate-300', 'dark:border-slate-700', 'focus:ring-blue-600');
                }
            }
        }

        // Si HTMX re-renderizó el modal de alta o edición
        if (targetId === 'contenedor-modal-alta-profesor' || targetId === 'modal-alta-profesor' || event.detail.target?.querySelector('#form-alta-profesor')) {
            inicializarEventosModalProfesorGenerico('modal-alta-profesor', 'form-alta-profesor', '');
        }

        if (targetId === 'contenedor-modal-editar-profesor' || targetId === 'modal-editar-profesor' || event.detail.target?.querySelector('#form-editar-profesor')) {
            inicializarEventosModalProfesorGenerico('modal-editar-profesor', 'form-editar-profesor', '-editar');
        }

        if (targetId === 'contenedor-modal-alta-vehiculo' || targetId === 'modal-alta-vehiculo' || event.detail.target?.querySelector('#form-alta-vehiculo')) {
            inicializarEventosModalVehiculo();
        }
    });

    // =========================================================================
    // VALIDACIONES FRONT-END: PERFIL DEL ADMINISTRADOR
    // =========================================================================
    function inicializarValidacionesPerfilAdmin() {
        const formPerfil = document.getElementById('form-perfil-admin');
        if (!formPerfil) return;

        const inputDni = document.getElementById('dni');
        const inputFechaNac = document.getElementById('fechaNacimiento');
        const inputNombre = document.getElementById('nombre');
        const inputApellidos = document.getElementById('apellidos');
        const inputTelefono = document.getElementById('telefono');
        const inputDireccion = document.getElementById('direccion');

        // Configurar rango 18 a 100 años para fecha de nacimiento
        if (inputFechaNac) {
            const hoy = new Date();
            const hace18Anios = new Date(hoy.getFullYear() - 18, hoy.getMonth(), hoy.getDate());
            const hace100Anios = new Date(hoy.getFullYear() - 100, hoy.getMonth(), hoy.getDate());
            inputFechaNac.setAttribute('max', formatearFechaISO(hace18Anios));
            inputFechaNac.setAttribute('min', formatearFechaISO(hace100Anios));
        }

        function validarDni() {
            if (!inputDni) return true;
            const valor = inputDni.value.trim().toUpperCase();
            if (!valor) {
                mostrarErrorCampo('dni', 'feedback-dni', 'El DNI / NIE es obligatorio.');
                return false;
            }
            const regexDni = /^[0-9]{8}[A-Za-z]$|^[XYZxyz][0-9]{7}[A-Za-z]$/;
            if (!regexDni.test(valor)) {
                mostrarErrorCampo('dni', 'feedback-dni', 'El formato del DNI/NIE no es válido (ej. 12345678Z o X1234567Z).');
                return false;
            }
            const feedback = document.getElementById('feedback-dni');
            if (!feedback || !feedback.querySelector('.mensaje-error-campo')) {
                limpiarErrorCampo('dni', 'feedback-dni');
            }
            return true;
        }

        function validarFechaNacimiento() {
            if (!inputFechaNac) return true;
            const valor = inputFechaNac.value.trim();
            if (!valor) {
                mostrarErrorCampo('fechaNacimiento', 'feedback-fechaNacimiento', 'La fecha de nacimiento es obligatoria.');
                return false;
            }
            const partes = valor.split('-');
            if (partes.length !== 3) {
                mostrarErrorCampo('fechaNacimiento', 'feedback-fechaNacimiento', 'Formato de fecha inválido.');
                return false;
            }
            const anio = parseInt(partes[0], 10);
            const mes = parseInt(partes[1], 10) - 1;
            const dia = parseInt(partes[2], 10);
            const fechaNac = new Date(anio, mes, dia);

            if (isNaN(fechaNac.getTime())) {
                mostrarErrorCampo('fechaNacimiento', 'feedback-fechaNacimiento', 'Introduce una fecha de nacimiento válida.');
                return false;
            }

            const hoy = new Date();
            const hoySinHora = new Date(hoy.getFullYear(), hoy.getMonth(), hoy.getDate());
            if (fechaNac > hoySinHora) {
                mostrarErrorCampo('fechaNacimiento', 'feedback-fechaNacimiento', 'La fecha de nacimiento debe ser una fecha pasada.');
                return false;
            }

            const hace100Anios = new Date(hoy.getFullYear() - 100, hoy.getMonth(), hoy.getDate());
            if (fechaNac < hace100Anios) {
                mostrarErrorCampo('fechaNacimiento', 'feedback-fechaNacimiento', 'La fecha de nacimiento no puede ser anterior a hace 100 años.');
                return false;
            }

            const fechaMinima18 = new Date(anio + 18, mes, dia);
            if (hoySinHora < fechaMinima18) {
                mostrarErrorCampo('fechaNacimiento', 'feedback-fechaNacimiento', 'El administrador debe ser mayor de edad (al menos 18 años).');
                return false;
            }

            limpiarErrorCampo('fechaNacimiento', 'feedback-fechaNacimiento');
            return true;
        }

        function validarNombre() {
            if (!inputNombre) return true;
            const valor = inputNombre.value.trim();
            if (!valor) {
                mostrarErrorCampo('nombre', 'feedback-nombre', 'El nombre es obligatorio.');
                return false;
            }
            if (valor.length > 50) {
                mostrarErrorCampo('nombre', 'feedback-nombre', 'El nombre no puede superar los 50 caracteres.');
                return false;
            }
            limpiarErrorCampo('nombre', 'feedback-nombre');
            return true;
        }

        function validarApellidos() {
            if (!inputApellidos) return true;
            const valor = inputApellidos.value.trim();
            if (!valor) {
                mostrarErrorCampo('apellidos', 'feedback-apellidos', 'Los apellidos son obligatorios.');
                return false;
            }
            if (valor.length > 100) {
                mostrarErrorCampo('apellidos', 'feedback-apellidos', 'Los apellidos no pueden superar los 100 caracteres.');
                return false;
            }
            limpiarErrorCampo('apellidos', 'feedback-apellidos');
            return true;
        }

        function validarTelefono() {
            if (!inputTelefono) return true;
            const valor = inputTelefono.value.trim();
            if (!valor) {
                mostrarErrorCampo('telefono', 'feedback-telefono', 'El teléfono es obligatorio.');
                return false;
            }
            const regexTel = /^(\+34|0034)?[6789]\d{8}$/;
            if (!regexTel.test(valor)) {
                mostrarErrorCampo('telefono', 'feedback-telefono', 'El formato del teléfono no es válido (ej. 600111222).');
                return false;
            }
            const feedback = document.getElementById('feedback-telefono');
            if (!feedback || !feedback.querySelector('.mensaje-error-campo')) {
                limpiarErrorCampo('telefono', 'feedback-telefono');
            }
            return true;
        }

        function validarDireccion() {
            if (!inputDireccion) return true;
            const valor = inputDireccion.value.trim();
            if (!valor) {
                mostrarErrorCampo('direccion', 'feedback-direccion', 'La dirección es obligatoria.');
                return false;
            }
            if (valor.length > 200) {
                mostrarErrorCampo('direccion', 'feedback-direccion', 'La dirección no puede superar los 200 caracteres.');
                return false;
            }
            limpiarErrorCampo('direccion', 'feedback-direccion');
            return true;
        }

        // Listeners individuales blur e input
        if (inputDni) {
            inputDni.addEventListener('blur', validarDni);
            inputDni.addEventListener('input', () => {
                if (inputDni.value.trim()) validarDni();
            });
        }

        if (inputFechaNac) {
            inputFechaNac.addEventListener('blur', validarFechaNacimiento);
            inputFechaNac.addEventListener('change', validarFechaNacimiento);
        }

        if (inputNombre) {
            inputNombre.addEventListener('blur', validarNombre);
            inputNombre.addEventListener('input', () => {
                if (inputNombre.value.trim()) validarNombre();
            });
        }

        if (inputApellidos) {
            inputApellidos.addEventListener('blur', validarApellidos);
            inputApellidos.addEventListener('input', () => {
                if (inputApellidos.value.trim()) validarApellidos();
            });
        }

        if (inputTelefono) {
            inputTelefono.addEventListener('blur', validarTelefono);
            inputTelefono.addEventListener('input', () => {
                if (inputTelefono.value.trim()) validarTelefono();
            });
        }

        if (inputDireccion) {
            inputDireccion.addEventListener('blur', validarDireccion);
            inputDireccion.addEventListener('input', () => {
                if (inputDireccion.value.trim()) validarDireccion();
            });
        }

        // Validación global al enviar el formulario
        formPerfil.addEventListener('submit', (e) => {
            const vDni = validarDni();
            const vFecha = validarFechaNacimiento();
            const vNombre = validarNombre();
            const vApellidos = validarApellidos();
            const vTel = validarTelefono();
            const vDir = validarDireccion();

            const feedbackError = formPerfil.querySelector('.mensaje-error-campo');
            if (!vDni || !vFecha || !vNombre || !vApellidos || !vTel || !vDir || feedbackError) {
                e.preventDefault();
                const primerInvalido = formPerfil.querySelector('.border-red-500');
                if (primerInvalido) {
                    primerInvalido.focus();
                }
            }
        });
    }

    inicializarEventosModalesProfesor();
    inicializarEventosModalVehiculo();
    inicializarValidacionesPerfilAdmin();
});
