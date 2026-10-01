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
     * - Fecha de Contratación: +- 1 mes respecto a hoy.
     */
    function configurarRestriccionesFechasModalProfesor() {
        const inputNacimiento = document.getElementById('profesor-nacimiento');
        const inputContratacion = document.getElementById('profesor-fecha');
        const hoy = new Date();

        if (inputNacimiento) {
            const hace18Anios = new Date(hoy.getFullYear() - 18, hoy.getMonth(), hoy.getDate());
            const hace100Anios = new Date(hoy.getFullYear() - 100, hoy.getMonth(), hoy.getDate());
            inputNacimiento.setAttribute('max', formatearFechaISO(hace18Anios));
            inputNacimiento.setAttribute('min', formatearFechaISO(hace100Anios));
        }

        if (inputContratacion) {
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

    function validarFechaNacimientoProfesor() {
        const input = document.getElementById('profesor-nacimiento');
        if (!input) return true;

        const valor = input.value?.trim();
        if (!valor) {
            mostrarErrorCampo('profesor-nacimiento', 'feedback-nacimiento', 'La fecha de nacimiento es obligatoria.');
            return false;
        }

        const partes = valor.split('-');
        if (partes.length !== 3) {
            mostrarErrorCampo('profesor-nacimiento', 'feedback-nacimiento', 'Formato de fecha inválido.');
            return false;
        }

        const anio = parseInt(partes[0], 10);
        const mes = parseInt(partes[1], 10) - 1;
        const dia = parseInt(partes[2], 10);
        const fechaNac = new Date(anio, mes, dia);

        if (isNaN(fechaNac.getTime())) {
            mostrarErrorCampo('profesor-nacimiento', 'feedback-nacimiento', 'Introduce una fecha de nacimiento válida.');
            return false;
        }

        const hoy = new Date();
        const hoySinHora = new Date(hoy.getFullYear(), hoy.getMonth(), hoy.getDate());

        if (fechaNac > hoySinHora) {
            mostrarErrorCampo('profesor-nacimiento', 'feedback-nacimiento', 'La fecha de nacimiento debe ser una fecha pasada.');
            return false;
        }

        const hace100Anios = new Date(hoy.getFullYear() - 100, hoy.getMonth(), hoy.getDate());
        if (fechaNac < hace100Anios) {
            mostrarErrorCampo('profesor-nacimiento', 'feedback-nacimiento', 'La fecha de nacimiento no puede ser anterior a hace 100 años.');
            return false;
        }

        const fechaMinima18 = new Date(anio + 18, mes, dia);
        if (hoySinHora < fechaMinima18) {
            mostrarErrorCampo('profesor-nacimiento', 'feedback-nacimiento', 'El profesor debe ser mayor de edad (al menos 18 años).');
            return false;
        }

        limpiarErrorCampo('profesor-nacimiento', 'feedback-nacimiento');
        return true;
    }

    function validarFechaContratacionProfesor() {
        const input = document.getElementById('profesor-fecha');
        if (!input) return true;

        const valor = input.value?.trim();
        if (!valor) {
            mostrarErrorCampo('profesor-fecha', 'feedback-fecha-contratacion', 'La fecha de contratación es obligatoria.');
            return false;
        }

        const partes = valor.split('-');
        if (partes.length !== 3) {
            mostrarErrorCampo('profesor-fecha', 'feedback-fecha-contratacion', 'Formato de fecha inválido.');
            return false;
        }

        const fechaContrato = new Date(parseInt(partes[0], 10), parseInt(partes[1], 10) - 1, parseInt(partes[2], 10));
        if (isNaN(fechaContrato.getTime())) {
            mostrarErrorCampo('profesor-fecha', 'feedback-fecha-contratacion', 'Introduce una fecha de contratación válida.');
            return false;
        }

        const hoy = new Date();
        const hace1Mes = new Date(hoy.getFullYear(), hoy.getMonth() - 1, hoy.getDate());
        const en1Mes = new Date(hoy.getFullYear(), hoy.getMonth() + 1, hoy.getDate());

        if (fechaContrato < hace1Mes || fechaContrato > en1Mes) {
            mostrarErrorCampo('profesor-fecha', 'feedback-fecha-contratacion', 'La fecha de contratación debe estar comprendida entre 1 mes antes y 1 mes después de la fecha actual.');
            return false;
        }

        limpiarErrorCampo('profesor-fecha', 'feedback-fecha-contratacion');
        return true;
    }

    function validarPermisosProfesor() {
        const checkboxes = document.querySelectorAll('#modal-alta-profesor input[name="permisos"]:checked');
        const feedback = document.getElementById('feedback-permisos');

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

    function validarCompatibilidadVehiculo() {
        const selectVehiculo = document.getElementById('profesor-vehiculo');
        const feedback = document.getElementById('feedback-vehiculo-compatibilidad');
        if (!selectVehiculo) return true;

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
        const checkboxCarnet = document.querySelector(`#modal-alta-profesor input[name="permisos"][value="${tipoVehiculo}"]`);
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

    function validarFormularioAltaProfesor() {
        const form = document.getElementById('form-alta-profesor');
        if (!form) return true;

        // 1. Validaciones HTML5 de campos obligatorios
        const inputs = form.querySelectorAll('input:not([type="checkbox"]):not([type="radio"]), select');
        for (const input of inputs) {
            if (!input.checkValidity()) {
                input.reportValidity();
                input.focus();
                return false;
            }
        }

        // 2. Validación de Fecha de Nacimiento
        if (!validarFechaNacimientoProfesor()) {
            const inputNac = document.getElementById('profesor-nacimiento');
            if (inputNac) inputNac.focus();
            return false;
        }

        // 3. Validación de Fecha de Contratación
        if (!validarFechaContratacionProfesor()) {
            const inputFecha = document.getElementById('profesor-fecha');
            if (inputFecha) inputFecha.focus();
            return false;
        }

        // 4. Validación de Permisos (al menos uno)
        if (!validarPermisosProfesor()) {
            return false;
        }

        // 5. Validación de Compatibilidad Vehículo - Permisos
        if (!validarCompatibilidadVehiculo()) {
            const selectVehiculo = document.getElementById('profesor-vehiculo');
            if (selectVehiculo) selectVehiculo.focus();
            return false;
        }

        // 6. Comprobar si existen mensajes de error asíncronos devueltos por HTMX
        const errorActivo = form.querySelector('.mensaje-error-campo');
        if (errorActivo) {
            const feedbackContainer = errorActivo.closest('[id^="feedback-"]');
            if (feedbackContainer) {
                const nombreCampo = feedbackContainer.id.replace('feedback-', '');
                const inputAsociado = document.getElementById(`profesor-${nombreCampo}`);
                if (inputAsociado) inputAsociado.focus();
            }
            return false;
        }

        return true;
    }

    function inicializarEventosModalProfesor() {
        configurarRestriccionesFechasModalProfesor();

        const inputNacimiento = document.getElementById('profesor-nacimiento');
        if (inputNacimiento) {
            inputNacimiento.addEventListener('change', validarFechaNacimientoProfesor);
            inputNacimiento.addEventListener('blur', validarFechaNacimientoProfesor);
        }

        const inputContratacion = document.getElementById('profesor-fecha');
        if (inputContratacion) {
            inputContratacion.addEventListener('change', validarFechaContratacionProfesor);
            inputContratacion.addEventListener('blur', validarFechaContratacionProfesor);
        }

        const selectVehiculo = document.getElementById('profesor-vehiculo');
        if (selectVehiculo) {
            selectVehiculo.addEventListener('change', validarCompatibilidadVehiculo);
        }

        // Escuchar cambios en los checkboxes de permisos para limpiar el error reactivamente
        // y sincronizar la compatibilidad con el vehículo asignado
        const modalProfesor = document.getElementById('modal-alta-profesor');
        if (modalProfesor) {
            modalProfesor.addEventListener('change', (e) => {
                if (e.target && e.target.name === 'permisos') {
                    validarPermisosProfesor();
                    validarCompatibilidadVehiculo();
                }
            });
        }

        // Ejecutar validación inicial de compatibilidad por si el formulario se inicializa con vehículo y permisos preseleccionados
        validarCompatibilidadVehiculo();

        // Interceptar el envío nativo del formulario
        const form = document.getElementById('form-alta-profesor');
        if (form) {
            form.addEventListener('submit', (e) => {
                if (!validarFormularioAltaProfesor()) {
                    e.preventDefault();
                    e.stopImmediatePropagation();
                }
            });
        }
    }

    // Interceptar evento HTMX para abortar la petición si no pasa las validaciones de cliente
    document.body.addEventListener('htmx:confirm', (e) => {
        if (e.target && e.target.id === 'form-alta-profesor') {
            if (!validarFormularioAltaProfesor()) {
                e.preventDefault();
            }
        }
    });

    // Sincronizar clases visuales de inputs tras peticiones HTMX (/usuario/validar-*) y al reabrir el modal
    document.body.addEventListener('htmx:afterSwap', (event) => {
        const targetId = event.detail.target?.id;

        // Feedback de campos individuales (soporta tanto modales profesor-... como perfil admin)
        if (targetId && targetId.startsWith('feedback-')) {
            const campoNombre = targetId.replace('feedback-', '');
            const input = document.getElementById(`profesor-${campoNombre}`) || document.getElementById(campoNombre);
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

        // Si HTMX re-renderizó el modal por completo tras validaciones del servidor
        if (targetId === 'modal-alta-profesor' || event.detail.target?.querySelector('#form-alta-profesor')) {
            inicializarEventosModalProfesor();
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

    inicializarEventosModalProfesor();
    inicializarValidacionesPerfilAdmin();
});
