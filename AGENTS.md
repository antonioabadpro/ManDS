# AGENTS.md - Sistema ERP para Autoescuelas (TFG)
> Guía maestra de arquitectura, directrices de interacción, reglas de dominio y memoria normativa para los agentes de desarrollo.

---

# 1. Reglas Supremas y Comportamiento del Agente

## 1.1. Modo de Interacción y Rol
- **Idioma Obligatorio:** Español de España (`es-ES`) SIEMPRE en explicaciones, preguntas, respuestas, comentarios y documentación.
- **Rol:** Desarrollador Sénior y Mentor. Antes de entregar código, explica el porqué técnico y de arquitectura en 2-3 frases para facilitar el aprendizaje del desarrollador.
- **Corrección Proactiva:** Señala inmediatamente cualquier error propuesto (bugs, seguridad, concurrencia o negocio) y ofrece la alternativa correcta.
- **Formato de Salida (Modo Diff/Quirúrgico):** Prohibido reescribir archivos completos excepto en creación inicial o petición expresa. Devuelve únicamente métodos, fragmentos modificados o diffs indicando ubicación exacta.
- **Planificación Previa y Walkthroughs en Artefactos:** Para cualquier tarea de más de 2 pasos, presenta previamente un Plan de Implementación conceptual sin código y espera confirmación del desarrollador antes de generar nada. Tanto planes como walkthroughs e informes detallados deben entregarse obligatoriamente como artefactos Markdown en el panel lateral.
- **Uso de Skills:** Usa siempre las skills disponibles en `.skills/`. Si detectas fallos en una skill, corrígela. Tras modificar este archivo, invoca `find-skills`.
- **Código del Desarrollador como Fuente de Verdad (Anti-Test-Lock-In):** Los cambios de negocio o arquitectura introducidos por el desarrollador representan la nueva intención oficial. Queda **TERMINANTEMENTE PROHIBIDO** revertir o alterar su lógica para "poner los tests en verde".
  - *Protocolo ante Fallo de Tests:* 1) Analizar si se debe al nuevo diseño del desarrollador; 2) En caso afirmativo, adaptar y actualizar las expectativas del test (`assertEquals`, mocks, datos); 3) Si es un bug colateral imprevisto (ej. NPE), corregir el detalle técnico sin alterar la intención original.

## 1.2. Protocolo de Memoria Viva y Registro de Decisiones ("Recuerda...")
- **Separación de Responsabilidades:** Este archivo contiene **exclusivamente directrices normativas y reglas de negocio activas**. El historial cronológico de decisiones de arquitectura e hitos completados reside en [`docs/REGISTRO_DECISIONES.md`](file:///c:/Users/anton/Desktop/Programaci%C3%B3n/ManDS/docs/REGISTRO_DECISIONES.md). Prohibido acumular tablas históricas en `AGENTS.md`.
- **Protocolo de Actuación ante Triggers (*"recuerda"*, *"anota"*, *"apunta esto"*, *"guarda esto en memoria"*):**
  - **Regla de negocio, técnica, arquitectura o UI/UX:** Incorporar o modificar directamente la regla en la sección correspondiente de `AGENTS.md`.
  - **Decisión de arquitectura o hito completado (ADR / Changelog):** Anexar automáticamente una nueva fila al final de [`docs/REGISTRO_DECISIONES.md`](file:///c:/Users/anton/Desktop/Programaci%C3%B3n/ManDS/docs/REGISTRO_DECISIONES.md) con fecha (DD/MM/YYYY), resumen y justificación técnica (incluso si no se pide expresamente).

## 1.3. Toma de Decisiones, Dudas y Seguridad
- **Dudas de Negocio o Técnicas:** NUNCA asumir una solución unilateral. Plantear preguntas abiertas exponiendo alternativas con pros y contras antes de escribir código.
- **Seguridad en BD y Despliegue:** NUNCA ejecutar scripts destructivos (`DROP TABLE`, `TRUNCATE`, modificaciones irreversibles en Supabase) ni dar por terminado un despliegue a Render sin confirmación explícita previa con resumen de impacto.

## 1.4. Directrices UI/UX Responsive (Mobile-First) y JS No Intrusivo
- **Responsividad Total:** Vistas, modales y fragmentos (Tailwind CSS / Flowbite / DaisyUI) 100% responsivos sin desbordamiento horizontal (`overflow-x`) en móvil (`< 640px`), tablet (`768px`) y escritorio (`>= 1024px`). Tablas con `overflow-x-auto` o tarjetas apiladas en móvil; barras de navegación colapsables en menú hamburguesa o drawer.
- **Cohesión Visual:** Prohibido alterar arbitrariamente paleta de colores, radios de borde (`rounded-*`), tipografías o espaciados entre vistas.
- **Separación Estricta de JavaScript (Unobtrusive JS):** Prohibido JS inline en plantillas Thymeleaf, sintaxis `/*[[${...}]]*/` o eventos inline (`onclick`, `onsubmit`, `onchange`). Todo JS debe residir en `src/main/resources/static/js/`, cargarse con `<script th:src="@{...}" defer></script>` y vincularse con `addEventListener` (cumplimiento CSP).
- **Paso de Datos por HTML5 Dataset:** Transferencia de datos de Thymeleaf a JS exclusivamente mediante atributos `data-*` (`element.dataset.*`).
- **Desacoplamiento JS y Named Event Handlers:** Prohibidos scripts monolíticos por rol (ej. JS global de Admin). Mínimo un fichero JS independiente por cada endpoint/vista. Debe implementarse bajo el patrón *Named Event Handlers* con una función punto de entrada (`inicializar...`) para registrar los eventos.

---

# 2. Documentación de Referencia y Memoria del Proyecto
- **Especificación Técnica y Arquitectura:** [`docs/ARQUITECTURA.md`](file:///c:/Users/anton/Desktop/Programaci%C3%B3n/ManDS/docs/ARQUITECTURA.md) (Stack tecnológico, estructura *Package by Features*, entidades del dominio y casos de uso).
- **Registro Histórico de Decisiones (ADR):** [`docs/REGISTRO_DECISIONES.md`](file:///c:/Users/anton/Desktop/Programaci%C3%B3n/ManDS/docs/REGISTRO_DECISIONES.md) (Histórico cronológico inmutable de cambios y decisiones técnicas).
- **Diagramas Oficiales PlantUML:** [`Diagramas/Diagrama_ER.puml`](file:///c:/Users/anton/Desktop/Programaci%C3%B3n/ManDS/Diagramas/Diagrama_ER.puml) y [`Diagramas/Diagrama_CU.puml`](file:///c:/Users/anton/Desktop/Programaci%C3%B3n/ManDS/Diagramas/Diagrama_CU.puml).

---

# 3. Reglas de Negocio Estrictas

## 3.1. Autenticación, Registro y Estados de Usuario
- **Roles Estrictos:** `ADMIN`, `PROFESOR`, `ALUMNO`.
- **Inicio de Sesión:** Admite indistintamente nombre de usuario (`username`) o correo (`email`) con contraseña.
- **Alta de Profesores:** Exclusiva del Administrador; genera `TokenVerificacion` efímero (60 min) enviado por email transaccional para que el profesor cree sus credenciales.
- **Alta de Alumnos:** Registro público en BD como `INACTIVO`. Se activa (`ACTIVO`) y formaliza su matrícula tras confirmación de pago en Stripe Checkout. En cancelación o abandono se purgan registros huérfanos.
- **Trazabilidad Lógica:** Borrado lógico (`INACTIVO`) en profesores y alumnos preservando histórico de clases, exámenes y pagos.

## 3.2. Matrícula, Tarifas y Convocatorias de Examen
- **Límite de Carnet:** 1 carnet activo simultáneamente por alumno.
- **Tarifario:** Matrícula (450€ pesados / 250€ resto), clases sueltas (60€ pesados / 30€ resto), bonos (Bono 10 a 540€/270€, Bono 15 a 800€/400€, Bono 20 a 1000€/500€), tasas de renovación.
- **Tipos y Modalidades:** *Teórico + Prácticas*, *Sólo Prácticas*, *Individual* (mejora futura). Modalidades: *Nueva Matriculación* y *Renovación de Matrícula*.
- **Vinculación Estricta Alumno-Profesor:** Un alumno **únicamente puede tener profesor asignado si dispone de una matrícula activa**. Si no tiene matrícula activa (carnet obtenido, sin matricular o baja lógica), su campo `profesor` es obligatoriamente `null`. El profesor asignado debe estar habilitado para el carnet de la matrícula del alumno.
- **Gestión de Convocatorias y Suspensos:** Cada matrícula concede **2 convocatorias iniciales**. Convocatorias consumidas ($2 - \text{convocatoriasRestantes}$) equivalen a exámenes oficiales (`NO APTO`). Al suspender ambas, se bloquea la adquisición de saldo y reserva hasta abonar la Renovación de Matrícula vía Stripe.
- **Secuencia Temporal en Modalidad TEORICO_PRACTICA:** Un alumno en modalidad `TEORICO_PRACTICA` **NO puede realizar clases prácticas ni solicitar examen práctico hasta haber aprobado previamente el examen teórico** (`esApto = true`). Toda clase práctica o citación a prueba práctica exige examen teórico aprobado en fecha anterior.
- **Cierre por Examen Aprobado:** Al calificar el examen práctico como `APTO`, la matrícula pasa a inactiva (`estaActiva = false`), se desvincula al profesor (`alumno.setProfesor(null)`), se cancelan clases futuras pendientes y se habilita matriculación en un nuevo permiso.

## 3.3. Flota de Vehículos y Profesores
- **Propiedad:** Los vehículos pertenecen siempre a la autoescuela, nunca al profesor.
- **Compatibilidad Carnet-Vehículo:** Un profesor solo puede asignarse a un vehículo si cuenta con el permiso en su `listaTiposCarnet`. Asignación exclusiva 1-a-1.
- **Baja y Reasignación del Profesor:**
  - Al dar de baja a un profesor: borrado lógico (`INACTIVO`), vehículo liberado a `DISPONIBLE`, cancelación de clases futuras y aviso por email a alumnos desvinculados/reasignados.
  - Reasignación en baja: selector y backend solo admiten profesores activos habilitados para el carnet del vehículo del profesor saliente (o todos si no tenía vehículo), ordenados por menor carga (`ORDER BY SIZE(p.listaAlumnos) ASC`).
  - Reasignación desde Gestión de Alumnos: selector y backend solo admiten profesores activos habilitados para el carnet de la matrícula del alumno, ordenados por menor carga (`ORDER BY SIZE(p.listaAlumnos) ASC`).
- **Bloqueo por Examen Práctico:** Al calendarizar un examen oficial en un vehículo, se cancelan automáticamente todas las clases prácticas de ese día en dicho vehículo con correo explicativo a los alumnos.
- **Turnos de Profesor:** Cada profesor opera bajo un `TipoTurno` fijo (`MATINAL` o `TARDE`), delimitando sus tramos hábiles para reservas en la cuadrícula semanal nativa.
- **Inmutabilidad de Vehículo con Clases Pendientes:** PROHIBIDO modificar (reasignar o desvincular) el vehículo asignado a un profesor si este cuenta con clases prácticas pendientes (`EstadoClase.PENDIENTE`). Para cambiarlo, dichas clases deben completarse (`RECIBIDA`) o cancelarse (`CANCELADA`) previamente.
- **Borrado Lógico e Irreversibilidad de Vehículos:** Un vehículo `INACTIVO` no puede eliminarse físicamente de la BD (borrado lógico) y tiene prohibida su reactivación tras la baja (al igual que el alumno).
- **Condición de Borrado Lógico:** Un Vehículo solo puede eliminarse si está `DISPONIBLE`.
- **Gestión de Incidencias y Estado MANTENIMIENTO:**
  - *Transición a MANTENIMIENTO:* Si el Administrador pasa una incidencia a `EN_PROCESO`, el vehículo entra en `MANTENIMIENTO` automáticamente, cancela todas sus clases y notifica por email al profesor (aviso único) y a sus alumnos (suspensión hasta nuevo aviso).
  - *Bloqueo en Gestión de Flota:* El Administrador NO puede cambiar el estado de un vehículo en `MANTENIMIENTO` desde Flota; exige resolver previamente la incidencia desde el panel de Incidencias de Flota.
  - *Resolución (`RESUELTA`):* Al resolver la incidencia, el vehículo recupera automáticamente su estado previo (`DISPONIBLE` u `OCUPADO`) y notifica por email al profesor y a sus alumnos la reapertura de reservas.
  - *Bloqueo de Reservas:* Alumno no puede reservar si su vehículo está en `MANTENIMIENTO` o no tiene profesor (aviso reactivo al pulsar en la cuadrícula del calendario).

## 3.4. Algoritmo y Control de Reserva de Clases
- **Fórmula de Capacidad de Reserva:**
  $$\text{CapacidadReserva} = \text{saldoClases} - \text{clasesReservadasPendientes}$$
- **Condición de Compra:** Un alumno solo puede adquirir clases sueltas o bonos cuando su saldo restante sea cero ($\text{saldoClases} = 0$). Comprar suma clases al saldo (`saldoClases += cantidadComprada`).
- **Condición de Reserva:** El alumno solo puede reservar si $\text{CapacidadReserva} > 0$ y su vehículo no está en `MANTENIMIENTO` y ha seleccionado una hora que corresponde con la del horario de su profesor. En caso de NO cumplir alguna de estas condiciones, la cuadrícula del calendario bloquea la reserva con aviso.
- **Deducción Atómica de Clases:** Tras la clase, el profesor cumplimenta ficha técnica (`kmInicio`, `kmFin >= kmInicio`, observaciones pedagógicas). Al registrarla, pasa a `RECIBIDA` y atómicamente se descuenta 1 unidad de saldo (`saldoClases -= 1`) y 1 unidad de pendientes (`clasesReservadasPendientes -= 1`).
- **Concurrencia en Reservas (First-Come, First-Served):** Restricción única en BD `UNIQUE(profesor_id, fecha_hora)` para clases activas. Ejecución bajo `@Transactional` con bloqueo pesimista o verificación atómica. En colisión, devolver HTTP 409 o feedback reactivo HTMX.
- **Cancelación de Clases:** Al cancelar una clase `PENDIENTE` (por alumno o profesor), pasa a `CANCELADA` y se decrementa atómicamente `clasesReservadasPendientes -= 1` sin alterar `saldoClases` (restituye de inmediato capacidad de reserva).
- **Ventana Temporal y Precedencia de Clases Prácticas:**
  - La fecha de toda clase práctica debe estar estrictamente entre `fechaMatriculacion` y la fecha del examen práctico oficial (`fechaHora`).
  - La fecha de celebración de cualquier examen práctico oficial debe ser **estrictamente posterior a la fecha de la última clase práctica recibida**.

## 3.5. Circuito y Gestión de Solicitudes de Examen (DGT)
- **Control Centralizado por el Administrador:** Solo el Administrador puede fijar fechas, aceptar o rechazar solicitudes DGT. Los profesores solo consultan solicitudes y alumnos citados en su vehículo/turno sin poder fijar fechas ni resolver solicitudes.
- **Estados y Orden:** Estados `PENDIENTE`, `RECHAZADA`, `ACEPTADA`. Solicitudes pendientes ordenadas estrictamente por antigüedad FIFO (`ORDER BY fecha_solicitud ASC`).
- **2 Fechas Oficiales al Mes:** El Administrador configura manualmente las dos fechas mensuales oficiales de la DGT (1ª y 2ª quincena). Prohibido el cálculo algorítmico o aleatorio.
- **Cupo DGT:** El Administrador establece un límite máximo por tipo de carnet en cada jornada de examen práctico.
- **Resolución:**
  - Rechazo: Pasa a `RECHAZADA` con justificación obligatoria enviada por email al alumno.
  - Aceptación: Pasa a `ACEPTADA`, valida el cupo fijado para ese carnet, dispara citación por email y bloquea reservas del vehículo ese día.
- **Incompatibilidad de Clases Pendientes con Examen Práctico Citado / Presentado:** Un alumno **NO puede tener clases prácticas pendientes (`EstadoClase.PENDIENTE`) si ya se ha presentado a examen práctico oficial o se encuentra citado formalmente para dicha prueba**. Toda su formación previa debe constar en estado `RECIBIDA` (o `CANCELADA`).
- **Obligatoriedad de Clases Previas en Examen Práctico:** Un alumno **NUNCA puede ser citado, examinarse ni aprobar un examen práctico oficial DGT sin haber realizado clases prácticas previamente**. Todo examen práctico exige clases en estado `RECIBIDA` previas en fecha.

## 3.6. Cuadrícula Semanal Nativa de Calendario (SSR + Thymeleaf + HTMX)
- **Prohibición de FullCalendar y Enfoque Tecnológico:** Queda terminantemente prohibido el uso de FullCalendar v6 o cualquier otra librería externa de calendario en el cliente. Todos los calendarios (alumno y profesor) deben implementarse mediante renderizado del lado del servidor (SSR) con Thymeleaf, fragmentos HTMX para la navegación semanal asíncrona, y maquetación nativa CSS Grid/Flexbox con Tailwind CSS.
- **Estructura Semanal Laboral:** Calendario estrictamente semanal de lunes a viernes (excluyendo sábados y domingos), comenzando obligatoriamente en lunes.
- **Cabecera y Navegación Unificada:**
  - Bloque superior con título principal ("Calendario de Prácticas"), subtítulo ("Pulsa sobre cualquier clase para abrir su Ficha Técnica o gestionar su estado") y leyenda de estados mediante píldoras:
    - *Clase Pendiente:* indicador naranja/ámbar.
    - *Clase Recibida:* indicador verde esmeralda.
    - *Clase Cancelada:* indicador gris pizarra (`slate`).
    - *Examen Oficial DGT:* indicador morado (`violet`/`purple`).
  - Barra de navegación semanal con botones `<` (semana anterior), `>` (semana siguiente), `Hoy` y rango de fechas visible en el centro (ej. "5 – 9 oct 2026").
  - Prohibido incluir controles de cambio de vista (sin botones Mes, Semana, Día, Lista), la vista es única y semanal.
- **Franjas Horarias según Turno del Profesor (Intervalos de 45 min):** La cuadrícula limita sus franjas horarias al turno oficial asignado al profesor:
  - `MATINAL`: intervalos de 45 minutos de 08:00 a 14:45 (08:00, 08:45, 09:30, 10:15, 11:00, 11:45, 12:30, 13:15, 14:00).
  - `TARDE`: intervalos de 45 minutos de 15:00 a 21:45 (15:00, 15:45, 16:30, 17:15, 18:00, 18:45, 19:30, 20:15, 21:00).
- **Bloqueo por Jornada de Examen Oficial DGT (Día Completo):** Cuando un profesor tenga asignada una convocatoria o jornada de examen oficial DGT, esta debe indicarse en morado ocupando la jornada completa. Dicho día queda terminantemente bloqueada la reserva de clases prácticas a cualquier hora para ese profesor.
- **Responsividad y Cohesión UI/UX:** Maquetación 100% responsiva (móvil, tablet, escritorio) con soporte simultáneo de modo claro y oscuro (`dark:`), respetando la estética, tipografía y paleta corporativa de la aplicación.
- **Celdas y Tarjetas Contenidas:** En cada franja horaria ocupada, la clase se renderiza como una tarjeta redondeada (`rounded-xl` o `rounded-lg`) contenida holgadamente dentro de los límites de la celda (con margen y padding interior, sin desbordar ni ocupar el 100% del recuadro disponible).
- **Privacidad y Permisos de Interacción por Rol:**
  - *Calendario del Profesor:* En cada tarjeta de clase ocupada se muestra el nombre y apellidos del alumno. Al pulsar sobre la tarjeta, se abre el modal HTMX con la ficha técnica de la clase práctica con todos sus detalles.
  - *Calendario del Alumno:* En las tarjetas de clases ocupadas se muestra el texto literal `"Horario Ocupado"` (sin nombre ni identificador de otros alumnos).
    - Si la clase pertenece al alumno autenticado: la tarjeta es interactiva y al hacer clic se abre el modal HTMX con la ficha técnica / detalles de su clase práctica con posibilidad de cancelar dicha clase.
    - Si la clase pertenece a otro alumno: la tarjeta es inerte/bloqueada; el alumno NO puede hacer clic sobre ella ni acceder a su información.
    - Si la celda está libre y el alumno cumple las condiciones de reserva (y no es día de examen DGT): al hacer clic en la celda se abre el modal HTMX para reservar clase práctica.

---

# 4. Restricciones Técnicas y Anti-Patrones

- **NO Single Page Applications (SPA):** Prohibido React, Angular o Vue. Dinamismo mediante Thymeleaf + HTMX + JS ES6+ nativo.
- **NO Usar FullCalendar ni Librerías Pesadas de Calendario:** Prohibido el uso de FullCalendar v6 o componentes cliente pesados. Toda la gestión de agendas se realiza mediante cuadrícula nativa SSR con Thymeleaf, fragmentos HTMX y Tailwind CSS Grid/Flexbox, optimizando el rendimiento (Core Web Vitals) y la responsividad total en cualquier dispositivo.
- **NO Borrado en Cascada Destructivo:** Aplicar siempre borrado lógico (*soft delete*) o transición a `INACTIVO` preservando la integridad referencial histórica.
- **NO IDs Secuenciales en URLs Críticas:** Usar UUIDs o tokens criptográficos para pagos, activaciones e invitaciones.
- **Transaccionalidad Estricta (@Transactional):** Obligatoria en compras, deducción de clases y reservas concurrentes.
- **Prioridad de Legibilidad sobre Concisión (Anti-One-Liners):** Prohibido compactar lógica compleja en streams ilegibles o lambdas anidadas. Priorizar bucles imperativos claros y métodos auxiliares descriptivos.
- **Uso Obligatorio de `this.` para Atributos de Instancia:** En todo el código Java del backend, es obligatorio anteponer `this.` a cualquier lectura o asignación de campos de clase (`this.campo = valor;`, `return this.campo;`) para prevenir *shadowing*.
- **Prohibición Léxica Expresa:** Queda terminantemente prohibido utilizar los términos *"profesor tutor"* o *"docente"* en cualquier texto, vista, variable o comentario de la aplicación.
- **Estructuración Semilla de Datos (`data.sql`):** Los IDs de `persona` siguen rangos reservados por rol: Administrador (ID 1), Rango de reserva libre (IDs 2..99), Profesores (IDs 100..199) y Alumnos (IDs 200..299). Los vehículos se agrupan correlativamente por tipo de carnet (B, A2, C, D, B_E, AM).
- **Pirámide de Testing (Foco Unitario):** Prohibido `@SpringBootTest` en servicios, mappers o utilidades; validar su lógica exclusivamente con tests unitarios rápidos en JUnit 5 + Mockito (`@ExtendWith(MockitoExtension.class)`).
- **Aserciones Web sin Acoplamiento HTML:** Prohibido validar texto o etiquetas HTML estáticas en Thymeleaf (`content().string(containsString(...))`). Los tests de controladores se limitan estrictamente a: 1) Estado HTTP (`status().isOk()`, `status().is3xxRedirection()`), 2) Vista/redirección (`view().name(...)`, `redirectedUrl(...)`), 3) Atributos del modelo (`model().attributeExists(...)`).
- **Centralización de Seguridad RBAC:** Las comprobaciones de autorización (rutas anónimas como `/login` o accesos `403 Forbidden` por rol) pertenecen exclusivamente a `SeguridadIntegrationTest`. Prohibido replicar estas baterías en clases `Vista*Test`.
- **Reutilización del Contexto Spring (`BaseIntegrationTest`):** Todo `@SpringBootTest` debe extender de `com.autoescuela.erp.BaseIntegrationTest`. Prohibido declarar `@TestPropertySource` o `@MockitoBean` en clases hijas para evitar invalidar el *Spring Test Context Caching*.
- **Ejecución Selectiva en Desarrollo Activo:** Ejecutar únicamente el test de la clase afectada (`mvn test -Dtest=NombreTest`) durante la implementación, reservando la suite completa para la fase pre-commit.

---

# 5. Catálogo de Skills del Proyecto (`.skills/`)
1. `feature-scaffolder`: Andamiaje de paquetes *Package by Feature* (Entidad, DTOs, Repositorio, Servicio, Controlador, Vistas).
2. `htmx-view-builder`: Fragmentos interactivos responsivos con Thymeleaf, Tailwind y atributos HTMX (`hx-*`).
3. `stripe-flow-validator`: Gestión de webhooks de Stripe, verificación de firmas criptográficas y flujos de pago.
4. `business-rules-tester`: Pruebas JUnit 5 + Mockito enfocadas en reglas de negocio estrictas.
5. `git-commits`: Estandarización de commits bajo Conventional Commits (`feat:`, `fix:`, `refactor:`).