---
name: htmx-view-builder
description: >-
  Diseño y construcción de vistas, modales y fragmentos interactivos responsivos con Thymeleaf, Tailwind CSS y HTMX.
  Usar cuando se implementen interfaces de usuario, componentes dinámicos, formularios con validación asíncrona,
  modales con doble confirmación o fragmentos renderizados desde el servidor sin JavaScript intrusivo.
metadata:
  author: ManDS
  version: "1.0.0"
---

# HTMX View Builder (SSR + Tailwind CSS + Thymeleaf)

Esta skill establece la guía operativa para construir interfaces de usuario reactivas, accesibles y responsivas en el ERP de la autoescuela utilizando Server-Side Rendering (SSR) con Thymeleaf, maquetación Tailwind CSS e interactividad asíncrona mediante HTMX y JavaScript no intrusivo (*Unobtrusive JS*).

---

## 1. Reglas Supremas de Frontend

1. **Separación Estricta de JavaScript (Zero Inline JS):**
   * Queda terminantemente prohibido el código JS en atributos HTML (`onclick`, `onsubmit`, `onchange`, `href="javascript:..."`).
   * Queda prohibida la sintaxis Thymeleaf para inyectar variables en bloques `<script>` (`/*[[${...}]]*/`).
   * Todo código JavaScript debe residir en ficheros externos dentro de `src/main/resources/static/js/`, cargados con `<script th:src="@{/js/<archivo>.js}" defer></script>` y vinculados exclusivamente con `addEventListener` para garantizar el estricto cumplimiento de la Política de Seguridad de Contenido (CSP).
2. **Paso de Datos por HTML5 Dataset:**
   * La transferencia de variables de Thymeleaf a JavaScript se realiza exclusivamente mediante atributos `data-*` en el marcado HTML (ej. `data-id="${vehiculo.id}"`, `data-estado="${clase.estado}"`).
   * En JavaScript, se leen mediante `elemento.dataset.<propiedad>`.
3. **Desacoplamiento JS y Named Event Handlers:**
   * Mínimo un fichero JS independiente por cada endpoint o vista funcional (ej. `admin-flota.js`, `alumno-perfil.js`).
   * Estructurar el código bajo funciones con nombre explícito (`manejar...`) y una función punto de entrada (`inicializar...`), facilitando la depuración en pila y evitando escuchadores duplicados.
4. **Diseño Mobile-First Responsivo:**
   * Todas las vistas, modales y fragmentos deben ser 100% responsivos sin desbordamiento horizontal (`overflow-x`) en móvil (`< 640px`), tablet (`768px`) y escritorio (`>= 1024px`).
   * Las tablas deben envolverse siempre en un contenedor `<div class="overflow-x-auto">` o transformarse en tarjetas apiladas en pantallas pequeñas.
   * Soporte simultáneo obligatorio de modo claro y oscuro (`dark:`).

---

## 2. Operativa HTMX en Plantillas Thymeleaf

### 2.1. Atributos Clave
* `hx-get="/ruta"` / `hx-post="/ruta"`: Dispara peticiones HTTP asíncronas desde cualquier elemento HTML.
* `hx-target="#contenedor"`: Especifica el selector CSS del elemento del DOM que recibirá el fragmento renderizado.
* `hx-swap="innerHTML"` / `outerHTML`: Define cómo se inserta la respuesta (`innerHTML` reemplaza el contenido interior; `outerHTML` sustituye al elemento completo).
* `hx-indicator="#spinner"`: Muestra el indicador de carga durante la ejecución de la petición.
* `hx-disabled-elt="this"`: Deshabilita el botón o control mientras la petición está en vuelo, previniendo dobles envíos accidentales (*double-click prevention*).
* `hx-trigger="click, keyup changed delay:300ms"`: Configura eventos disparadores con filtrado o *debouncing*.

### 2.2. Cabeceras HTTP de Control del Servidor
Desde los controladores Spring MVC, se utilizan cabeceras de respuesta para controlar el comportamiento del cliente:
* `HX-Redirect: /url`: Provoca una redirección de navegación completa en el navegador del cliente.
* `HX-Trigger: actualizarCalendario`: Dispara un evento personalizado en el DOM del cliente que otros elementos pueden escuchar con `hx-trigger="actualizarCalendario from:body"`.
* `HX-Refresh: true`: Fuerza una recarga completa de la página actual.

---

## 3. Patrón de Modales Reactivos y Doble Confirmación

### 3.1. Arquitectura de Modales
1. En la vista principal se reserva un contenedor vacío con identificador único:
   ```html
   <div id="contenedor-modal-detalle" class="relative z-50"></div>
   ```
2. El botón de apertura invoca asíncronamente el fragmento modal mediante HTMX:
   ```html
   <button type="button"
           hx-get="/admin/flota/detalle/12"
           hx-target="#contenedor-modal-detalle"
           hx-swap="innerHTML"
           class="p-2 text-slate-500 hover:text-slate-700 dark:hover:text-slate-300">
       <svg ...></svg>
   </button>
   ```
3. El fragmento modal devuelto incluye el backdrop y el diálogo, cerrándose declarativamente con `data-modal-close`:
   ```html
   <!-- templates/fragments/modal-detalle.html -->
   <div th:fragment="modalDetalle"
        id="modal-detalle-vehiculo"
        class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm"
        role="dialog"
        aria-modal="true">

       <div class="relative w-full max-w-2xl bg-white dark:bg-slate-800 rounded-2xl shadow-2xl border border-slate-200 dark:border-slate-700 overflow-hidden">
           <!-- Cabecera -->
           <div class="flex items-center justify-between px-6 py-4 border-b border-slate-200 dark:border-slate-700">
               <h3 class="text-lg font-bold text-slate-900 dark:text-white">Ficha Técnica</h3>
               <button type="button"
                       data-modal-close="modal-detalle-vehiculo"
                       class="p-1.5 text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 rounded-lg">
                   ✕
               </button>
           </div>

           <!-- Cuerpo -->
           <div class="p-6 space-y-4">
               ...
           </div>
       </div>
   </div>
   ```

### 3.2. Prevención de Doble Envío con Spinners
Todo botón de acción o submit en modales y formularios debe incluir el estado de carga animado:
```html
<button type="submit"
        id="btn-confirmar-accion"
        class="inline-flex items-center justify-center px-4 py-2 font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-xl disabled:opacity-50 transition-colors"
        hx-disabled-elt="this">
    <span class="htmx-indicator-none">Confirmar</span>
    <span class="htmx-indicator hidden items-center space-x-2">
        <svg class="w-4 h-4 animate-spin text-white" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z"></path>
        </svg>
        <span>Procesando...</span>
    </span>
</button>
```

---

## 4. Patrón de JavaScript No Intrusivo (Named Event Handlers)

El script asociado debe registrar escuchadores tras el evento `DOMContentLoaded` y manejar delegación de eventos para componentes que puedan llegar dinámicamente vía HTMX:

```javascript
/**
 * src/main/resources/static/js/admin-flota.js
 */
document.addEventListener('DOMContentLoaded', () => {
    inicializarGestionFlota();
});

function inicializarGestionFlota() {
    registrarEscuchadoresBuscador();
    registrarEscuchadoresCierreModales();
}

function registrarEscuchadoresBuscador() {
    const inputBuscar = document.getElementById('filtro-buscar-vehiculo');
    if (!inputBuscar) return;

    inputBuscar.addEventListener('input', manejarFiltroVehiculos);
}

function manejarFiltroVehiculos(evento) {
    const textoFiltro = evento.target.value.toLowerCase().trim();
    const filas = document.querySelectorAll('#tabla-vehiculos tbody tr');

    filas.forEach(fila => {
        const matricula = fila.dataset.matricula ? fila.dataset.matricula.toLowerCase() : '';
        const modelo = fila.dataset.modelo ? fila.dataset.modelo.toLowerCase() : '';
        const coincide = matricula.includes(textoFiltro) || modelo.includes(textoFiltro);
        fila.style.display = coincide ? '' : 'none';
    });
}

function registrarEscuchadoresCierreModales() {
    document.addEventListener('click', (evento) => {
        const botonCerrar = evento.target.closest('[data-modal-close]');
        if (botonCerrar) {
            const modalId = botonCerrar.dataset.modalClose;
            const modalElemento = document.getElementById(modalId);
            if (modalElemento) {
                modalElemento.remove();
            }
        }
    });
}
```

---

## 5. Actualizaciones Fuera de Banda (OOB Swaps)

Cuando una acción del usuario en un modal debe actualizar múltiples partes de la interfaz principal sin recargar la página:
1. El controlador emite la cabecera `HX-Trigger: actualizarTabla`:
   ```java
   response.setHeader("HX-Trigger", "actualizarTabla");
   ```
2. O bien retorna múltiples fragmentos utilizando el atributo `hx-swap-oob="true"`:
   ```html
   <!-- Fragmento principal que reemplaza el modal -->
   <div id="modal-container"></div>

   <!-- Fragmento secundario que actualiza el badge de capacidad en otra parte del DOM -->
   <div id="badge-capacidad" hx-swap-oob="true" class="badge-verde">
       Saldo: <span th:text="${saldoActual}">10</span>
   </div>
   ```

---

## 6. Checklist de Calidad UI/UX

Antes de dar por finalizada cualquier vista o fragmento:
1. [ ] **CSP Compliance:** Cero atributos `onclick`, `onchange` o `style` inline; sin scripts `<script>` embebidos con sintaxis Thymeleaf.
2. [ ] **Responsividad Total:** Comprobada en móvil (`375px`), tablet (`768px`) y escritorio (`1280px`). Sin scroll horizontal involuntario.
3. [ ] **Modo Oscuro:** Todas las clases de fondo, texto y bordes disponen de su homólogo `dark:` con contraste legible (WCAG AA/AAA).
4. [ ] **Prevención de Doble Clic:** Botones de submit protegidos con `hx-disabled-elt="this"` y spinner animado `animate-spin`.
5. [ ] **Gestión de Errores Visual:** Errores de validación devueltos con mensajes contextuales accesibles y bordes en color de peligro (`border-rose-500`).
