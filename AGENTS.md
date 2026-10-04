# AGENTS.md - Sistema ERP para Autoescuelas (TFG)
> Guía maestra de arquitectura, directrices de interacción, reglas de dominio y memoria persistente para los agentes de desarrollo

# 1. Reglas Supremas y Comportamiento del Agente
## 1.1. Modo de Interacción y Rol del Agente
- **Idioma Obligatorio:** Español de España (`es-ES`) SIEMPRE en explicaciones, preguntas, respuestas, comentarios y documentación.
- **Rol:** Desarrollador Sénior y Mentor. Antes de entregar código, explica el porqué técnico y de arquitectura en 2-3 frases para facilitar el aprendizaje del desarrollador.
- **Corrección Proactiva:** Si el desarrollador propone una solución con algún error (bugs, fallos de seguridad, problemas de concurrencia, incoherencias con las reglas de negocio...) señálalo inmediatamente y ofrece la alternativa correcta.
- **Formato de Salida (Modo Diff/Quirúrgico):** Prohibido reescribir archivos completos excepto en la creación inicial o si el desarrollador lo indica expresamente. Devuelve únicamente los métodos, fragmentos modificados o diffs unificados indicando la ubicación exacta.
- **Planificación Previa y Walkthroughs en Artefactos:** Para cualquier tarea de más de 2 pasos, presenta previamente un Plan de Implementación conceptual sin código y espera la confirmación del desarrollador antes de generar nada. Tanto los planes de implementación como los walkthroughs o informes técnicos detallados deben entregarse obligatoriamente como documentos/ficheros de artefacto Markdown en el panel lateral para permitir una lectura limpia, estructurada y sin saturar el flujo del chat.
- **Uso de Skills:** Usa siempre las skills disponibles en el proyecto. Si detectas fallos en una skill, corrígela. Tras modificar este archivo, invoca `find-skills` para sincronizar dependencias.
- **El Código Productivo del Desarrollador es la Fuente de Verdad (Anti-Test-Lock-In):**
  - Cuando el desarrollador introduce o modifica código de negocio, arquitectura o contratos de métodos, **dichos cambios representan la nueva intención y diseño oficial del sistema**.
  - Queda **TERMINANTEMENTE PROHIBIDO** que el agente revierta, modifique, elimine o altere la lógica introducida por el desarrollador con la excusa de que "un test ha fallado" o para "hacer que la suite vuelva a estar en verde".
  - **Protocolo ante Fallo de Tests:**
    1. Analizar si el fallo se debe a la nueva semántica/estructura introducida intencionadamente por el desarrollador.
    2. En caso afirmativo, **refactorizar, adaptar y actualizar las expectativas del test** (`assertEquals`, `verify`, mocks, datos de entrada) al nuevo comportamiento esperado.
    3. Únicamente si la suite detecta un bug técnico colateral real (como un `NullPointerException` imprevisto o un defecto de concurrencia genuino), corregir dicho detalle técnico en el código sin alterar la intención original del cambio.

## 1.2. Protocolo de Memoria Viva y Registro de Decisiones ("Recuerda...")
- **Separación de Responsabilidades:** Para garantizar un contexto ligero y prevenir cualquier truncado en el prompt, este archivo `AGENTS.md` contiene **exclusivamente directrices normativas y reglas de negocio activas**, mientras que el historial cronológico de decisiones de arquitectura e hitos completados reside en [`docs/REGISTRO_DECISIONES.md`](file:///c:/Users/anton/Desktop/Programaci%C3%B3n/ManDS/docs/REGISTRO_DECISIONES.md).
- **Protocolo de Actuación ante Triggers (*"recuerda"*, *"anota"*, *"apunta esto"*, *"guarda esto en memoria"*):**
  - **Si es una regla de negocio, técnica, arquitectura o directriz UI/UX:** El agente **DEBE incorporar o modificar directamente la regla en la sección temática correspondiente de `AGENTS.md`** (Secciones 1, 6 u 8).
  - **Si es una decisión de arquitectura o hito de implementación completado (ADR / Changelog):** El agente **DEBE anexar una nueva fila al final de [`docs/REGISTRO_DECISIONES.md`](file:///c:/Users/anton/Desktop/Programaci%C3%B3n/ManDS/docs/REGISTRO_DECISIONES.md)** con su fecha en formato (DD/MM/YYYY), resumen y justificación técnica. Esto se ejecutará **de manera 100% automática incluso si el desarrollador olvida indicarlo expresamente**.
- **Prohibición de Tablas Históricas en `AGENTS.md`:** Queda terminantemente prohibido acumular tablas cronológicas o registros de cambios en `AGENTS.md` para evitar saturación de tokens y degradación atencional.

## 1.3. Protocolo de Toma de Decisiones y Dudas
- **Regla:** Ante cualquier duda o ambigüedad sobre la lógica de negocio de las autoescuelas (precios, convocatorias, cancelaciones, estados...) o técnica (contratos de API o diseño de base de datos, librerías...), el agente **NUNCA debe asumir una solución unilateral**.
- **Acción:** Planteará preguntas abiertas de forma clara exponiendo alternativas con sus pros y contras antes de escribir código.

## 1.4. Protocolo de Seguridad en Base de Datos y Despliegue ("Súbelo / Aplícalo")
- **Regla Estricta:** NUNCA ejecutar scripts destructivos (`DROP TABLE`, `TRUNCATE`, modificaciones masivas o irreversibles en Supabase), ni dar por terminado un despliegue a Render sin confirmación explícita del desarrollador.
- **Flujo de Modificación:** Antes de confirmar cambios en esquemas de BD o integraciones críticas (Stripe, FullCalendar), el agente debe presentar un resumen de impacto y esperar el visto bueno del desarrollador.

## 1.5. Protocolo de Uso Sistemático de Skills
- **Regla:** Siempre que exista una skill aplicable (`feature-scaffolder`, `htmx-view-builder`, `stripe-flow-validator`, `business-rules-tester`, `git-commits`), el agente **DEBE utilizarla prioritariamente y ceñirse a sus convenciones**. Si el agente detecta un fallo en una skill, **DEBE corregirla inmediatamente y actualizar el fichero AGENTS.md**.

## 1.6. Directrices Estrictas de Diseño UI/UX y Responsive Design (Mobile-First)
- **Responsividad Total:** Todas las vistas, modales y fragmentos generados con Tailwind CSS / UI Kits (Flowbite / DaisyUI) deben ser **100% responsivos** sin desbordamientos horizontales (`overflow-x` no deseado) en móvil (`< 640px`, `sm`), tablet (`768px`, `md`) y escritorio (`>= 1024px`, `lg`/`xl`).
- **Cohesión Visual y Paleta:** Queda prohibido alterar arbitrariamente la paleta de colores, radios de borde (`rounded-*`), tipografías o espaciados entre vistas. Todos los paneles (Admin, Profesor, Alumno, Matricula, CRUDs, etc.) deben mantener una estética profesional, limpia y coherente y compartir el mismo sistema de diseño y componentes base.
- **Adaptabilidad de Componentes Complejos:**
  - **Tablas y Listados:** En pantallas móviles deben incluir contenedor con `overflow-x-auto` o transformarse en tarjetas (`cards`) apiladas.
  - **Navegación:** Toda barra de navegación debe colapsar en menú hamburguesa o drawer lateral interactivo en pantallas reducidas.

## 1.7. Separación Estricta de JavaScript y Eventos (Unobtrusive JS)
- **Prohibición de JS Inline y Eventos en HTML:** Queda terminantemente prohibido incrustar bloques `<script>` con lógica funcional en plantillas Thymeleaf, usar sintaxis inline `/*[[${...}]]*/` o declarar manejadores de eventos inline (`onclick`, `onsubmit`, `onchange`, etc.).
- **Ficheros Dedicados y Carga Diferida:** Toda función de interacción (modales, confirmaciones, validaciones cliente) debe residir en su correspondiente archivo `.js` independiente en `src/main/resources/static/js/` (ej. `modal-baja-profesor.js`, `admin.js`, etc.), cargarse mediante `<script th:src="@{...}" defer></script>` en la vista principal y vincularse exclusivamente mediante `addEventListener`, garantizando total conformidad con directivas de seguridad CSP (*Content Security Policy*) estrictas.
- **Paso de Datos por HTML5 Dataset:** Toda transferencia de estado o parámetros del modelo Thymeleaf hacia JavaScript debe realizarse exclusivamente mediante atributos estándar `data-*` (leídos en el script con `element.dataset.*`).

---

# 2. Stack Tecnológico y Arquitectura
| Componente | Tecnología Seleccionada | Detalle / Configuración |
| :--- | :--- | :--- |
| **IDE** | Visual Studio Code | Entorno de desarrollo principal |
| **Arquitectura** | Monolito Modular MVC | Estructurado por funcionalidades (**Package by Features**) para maximizar cohesión y minimizar acoplamiento |
| **Backend** | Java 21 + Spring Boot 4.1.x (4.1.1 GA) | Spring Data JPA, Spring Security, Lombok, MapStruct |
| **Frontend** | HTML-over-the-wire | Thymeleaf + HTMX + JS ES6+ (interactividad reactiva sin SPA) |
| **Frontend (UI & Estilos)** | CSS3 + UI Kits Tailwind | Flowbite / DaisyUI / Preline UI |
| **Base de Datos** | PostgreSQL (Supabase) | Base de datos relacional en la nube |
| **Servidor Web** | Apache Tomcat | Embebido en Spring Boot |
| **Despliegue** | Render + UptimeRobot | Plan gratuito Render (750h/mes) con pings de mantenimiento HTTP |
| **Contenedores** | Docker | Pruebas de correo (Mailpit) y entorno local |
| **Pasarela de Pago** | Stripe API | Webhooks, Checkout Sessions y Payment Intents |
| **Calendario** | FullCalendar v6 (JS) | Integración con vistas dinámicas mediante HTMX |
| **Email** | Spring Mail (JavaMailSender) | Notificaciones transaccionales automáticas por SMTP |

---

# 3. Estructura del Proyecto (Package by Features con Subcapas Internas)

```text
src/
├── main/
│   ├── java/com/autoescuela/erp/
│   │   ├── ErpApplication.java
│   │   ├── core/           <-- Infraestructura transversal (config, email, enums, excepciones, security)
│   │   ├── auth/           <-- Autenticación, registro y recuperación (controller, dto, mapper, model, repository, service)
│   │   ├── usuarios/       <-- Gestión de usuarios y perfiles (Persona, Administrador, Profesor, Alumno)
│   │   ├── flota/          <-- Vehículos e incidencias mecánicas (Vehiculo, IncidenciaVehiculo)
│   │   ├── academico/      <-- Matrículas y expedientes de alumnos (Matricula)
│   │   ├── pagos/          <-- Pasarela Stripe desacoplada (StripeWebhookController, PagoStripeService)
│   │   ├── practicas/      <-- Clases prácticas y FullCalendar (ClasePractica, Calendario)
│   │   ├── examenes/       <-- Solicitudes, cupos y notas DGT (SolicitudExamen, Examen)
│   │   └── estadisticas/   <-- Analítica y métricas de rendimiento
│   │
│   └── resources/
│       ├── static/         <-- css/, js/ (ficheros dedicados sin scripts inline), imagenes/
│       ├── templates/      <-- layouts/, fragments/, auth/, admin/, profesor/, alumno/, error/
│       ├── application.properties
│       └── data.sql        <-- Dataset semilla de desarrollo
│
└── test/java/com/autoescuela/erp/ <-- Pruebas automatizadas (espejo de los paquetes de dominio)
```

---

# 4. Modelado Conceptual y Arquitectura (PlantUML)
Los diagramas oficiales de arquitectura y modelado conceptual residen en sus archivos `.puml` dedicados:
- **Modelo Entidad-Relación (ER):** [`Diagramas/Diagrama_ER.puml`](file:///c:/Users/anton/Desktop/Programaci%C3%B3n/ManDS/Diagramas/Diagrama_ER.puml)
- **Diagrama de Casos de Uso (CU):** [`Diagramas/Diagrama_CU.puml`](file:///c:/Users/anton/Desktop/Programaci%C3%B3n/ManDS/Diagramas/Diagrama_CU.puml)

*Entidades Nucleares:* `Persona` (abstracta con herencia JOINED a `Administrador`, `Profesor`, `Alumno`), `TokenVerificacion`, `Vehiculo`, `IncidenciaVehiculo`, `Matricula`, `ClasePractica`, `SolicitudExamen`, `Examen`.

---

# 5. Matriz de Roles y Funcionalidades
## 5.1. Administrador (Acceso Total)
- **Gestión de Flota (CRUD):** Registro de vehículos, kilometraje acumulado, estados de ITV y revisiones periódicas.
- **Gestión de Profesores (CRUD):** Altas de profesores con envío de token por email, edición, asignación exclusiva 1-a-1 de vehículo, baja lógica con desvinculación/reasignación filtrada por carnet del vehículo del profesor saliente y reactivación sin vehículo.
- **Gestión de Alumnos (CRUD):** Listado dinámico con estadísticas en tiempo real, supervisión de matrículas, estados, convocatorias y saldos. Consulta de ficha de detalles (`modal-detalle-alumno`), consulta de expediente académico integral con clases y exámenes (`modal-expediente-alumno`) y reasignación individual de profesor (`modal-reasignar-alumno`).
- **Bajas y Desvinculaciones Lógicas:**
  - Al dar de baja a un profesor: borrado lógico (`INACTIVO`), liberación del vehículo a `DISPONIBLE`, cancelación de clases pendientes y reasignación/desvinculación de alumnos con aviso por email.
  - Al eliminar un alumno: borrado lógico (`INACTIVO`) y desvinculación de su profesor.
  - Al eliminar un vehículo: borrado lógico (`INACTIVO`) y desvinculación de su profesor.
- **Panel de Estadísticas Globales:** Métricas agregadas de aprobados/suspensos, rendimiento de profesores y estado de la flota.
- **Gestión de Incidencias Mecánicas:** Supervisión de averías reportadas por los profesores.
- **Gestión de Solicitudes y Convocatorias de Examen (DGT):** Panel exclusivo para fijar manualmente las 2 fechas mensuales oficiales, consultar solicitudes pendientes en orden FIFO (`ORDER BY fecha_solicitud ASC`), y aceptar (asignando fecha y validando el cupo fijado por el Administrador para ese carnet) o rechazar con justificación obligatoria.

## 5.2. Profesor
- **Calendario Exclusivo:** Vista y gestión únicamente sobre su propio calendario de clases prácticas (FullCalendar).
- **Notificación por Cancelación/Edición:** Cancelar o editar una clase dispara automáticamente un email explicativo al alumno.
- **Ficha Técnica de Clase:** Al pulsar una clase agendada, el profesor visualiza los datos inmutables de reserva (alumno, DNI, fecha/hora, punto recogida) y solo puede editar: odómetro (`kmInicio`, `kmFin >= kmInicio`) y observaciones pedagógicas.
- **Consulta de Solicitudes DGT:** El profesor solo consulta las solicitudes y alumnos citados en su vehículo/turno. **No puede aceptar, rechazar ni fijar fechas oficiales**.
- **Calificación de Examen:** Registro del resultado (`APTO` / `NO APTO`) con enlace oficial a la web de la DGT para ver el desglose.
- **Reporte de Incidencias:** Emisión de partes de avería sobre su vehículo asignado.

## 5.3. Alumno
- **Matriculación y Pagos (Stripe):** Matricularse en 1 carnet simultáneo (450€ vehículos pesados/250€ en el resto). Abono de matrícula, clases sueltas (60€ pesados/30€ resto), bonos (Bono 10 a 540€/270€, Bono 15 a 800€/400€, Bono 20 a 1000€/500€) y tasas de renovación.
- **Reserva de Clases:** FullCalendar interactivo exclusivamente del profesor asignado, condicionado a tener capacidad de reserva disponible.
- **Historial Académico:** Consulta de clases recibidas (km, observaciones del profesor), convocatorias gastadas y estadísticas personales.
- **Solicitud de Examen DGT:** Petición de fecha para examen teórico o práctico (tras abonar tasas vía Stripe).

---

# 6. Reglas de Negocio Estrictas
## 6.1. Autenticación, Registro y Estados de Usuario
- **Roles Estrictos:** `ADMIN`, `PROFESOR`, `ALUMNO`.
- **Inicio de Sesión:** Admite indistintamente nombre de usuario (`username`) o correo (`email`) junto con la contraseña.
- **Alta de profesores:** Exclusiva del Administrador; genera `TokenVerificacion` efímero (60 min) enviado por email transaccional para que el profesor cree su usuario y contraseña.
- **Alta de Alumnos:** Registro público en BD como `INACTIVO`. Se activa (`ACTIVO`) y formaliza su matrícula formal tras la confirmación de pago en Stripe Checkout. En cancelación o abandono se purgan los registros huérfanos.
- **Trazabilidad:** Borrado lógico (`INACTIVO`) en profesores y alumnos preservando histórico de clases, exámenes y facturación.

## 6.2. Matrícula y Convocatorias de Examen
- **Límite de Carnet:** 1 carnet activo simultáneamente por alumno.
- **Tipos de Matrícula:** *Teórico + Prácticas*, *Sólo Prácticas*, *Individual* (mejora futura, NO tener en cuenta ahora).
- **Modalidades:** *Nueva Matriculación* y *Renovación de Matrícula*.
- **Gestión de Convocatorias:** Cada matrícula concede **2 convocatorias iniciales**. Al suspender ambas, se bloquean nuevas solicitudes de examen hasta pagar la Renovación de Matrícula vía Stripe. La renovación mantiene obligatoriamente el mismo tipo de carnet.

## 6.3. Flota de Vehículos y Profesores
- **Propiedad:** Los vehículos pertenecen siempre a la autoescuela, nunca al profesor.
- **Compatibilidad Carnet-Vehículo:** Un profesor solo puede asignarse a un vehículo si cuenta con el permiso correspondiente en su `listaTiposCarnet`.
- **Baja y Reasignación del Profesor:**
  - Al dar de baja a un profesor, se cancelan sus clases futuras y se desvinculan sus alumnos.
  - Al reasignar alumnos a otro profesor:
    - En la baja de un profesor: el selector y la validación de backend solo admiten profesores activos habilitados para el carnet del vehículo del profesor origen (o todos si no tenía vehículo), ordenados de menor a mayor carga de alumnos (`ORDER BY SIZE(p.listaAlumnos) ASC`).
    - En la reasignación desde Gestión de Alumnos: el selector y la validación solo admiten profesores activos habilitados para el carnet de la matrícula del alumno, ordenados de menor a mayor carga de alumnos (`ORDER BY SIZE(p.listaAlumnos) ASC`).
  - El vehículo queda en estado `DISPONIBLE`.
- **Bloqueo por Examen Práctico:** Al calendarizar un examen oficial en un vehículo, se cancelan automáticamente todas las clases prácticas de ese día en dicho vehículo y se envía correo explicativo a los alumnos.
- **Turnos de Profesor:** Cada profesor opera bajo un `TipoTurno` fijo (`MATINAL` o `TARDE`), delimitando sus tramos hábiles para reservas en FullCalendar.

## 6.4. Algoritmo y Control de Reserva de Clases
- **Fórmula de Capacidad de Reserva:**
  $$\text{CapacidadReserva} = \text{saldoClases} - \text{clasesReservadasPendientes}$$
- **Condición de Compra:** Un alumno solo puede adquirir clases sueltas o bonos cuando su saldo restante sea cero ($\text{saldoClases} = 0$). Comprar suma clases al saldo ($\text{saldoClases} += \text{cantidadComprada}$).
- **Condición de Reserva:** El alumno solo puede reservar si $\text{CapacidadReserva} > 0$. Si es $\le 0$, el calendario bloquea nuevas reservas.
- **Deducción Atómica de Clases:** Al impartir la clase, el profesor cumplimenta la ficha (`kmInicio`, `kmFin`, `observaciones`). Al registrarla, pasa atómicamente a `RECIBIDA` y se descuenta 1 unidad de saldo (`saldoClases -= 1`) y 1 unidad de `clasesReservadasPendientes`.
- **Concurrencia en Reservas (First-Come, First-Served):**
  - Restricción única en BD: `UNIQUE(profesor_id, fecha_hora)` para clases activas.
  - Ejecución transaccional bajo `@Transactional` con bloqueo pesimista o verificación atómica.
  - En caso de colisión, se captura la excepción y se devuelve feedback inmediato con error 409/alerta HTMX.
- **Cancelación de clases:** Al cancelar una clase en estado `PENDIENTE` (por alumno o profesor), pasa a `CANCELADA` y se decrementa atómicamente `clasesReservadasPendientes -= 1` sin alterar `saldoClases`, restituyendo de inmediato la capacidad de reserva.

## 6.5. Circuito y Gestión de Solicitudes de Examen (DGT)
- **Control Centralizado por el Administrador:** Solo el Administrador puede fijar fechas, aceptar o rechazar solicitudes de examen DGT.
- **Estados de Solicitud:** `PENDIENTE`, `RECHAZADA`, `ACEPTADA`.
- **Ordenación FIFO:** Solicitudes pendientes ordenadas estrictamente por antigüedad (`ORDER BY fecha_solicitud ASC`).
- **2 Fechas Oficiales al Mes:** El Administrador configura manualmente las dos fechas oficiales mensuales de la DGT (1ª y 2ª quincena). Se prohíbe el cálculo algorítmico o aleatorio.
- **Cupo DGT:** El Administrador se encarga de establecer un **límite máximo por tipo de carnet** en una misma jornada de examen práctico.
- **Resolución:**
  - Rechazo: Pasa a `RECHAZADA` con justificación obligatoria enviada por email al alumno.
  - Aceptación: Pasa a `ACEPTADA`, valida el cupo introducido por el administrador para ese tipo de carnet, dispara citación por correo y bloquea reservas del vehículo ese día.

---

# 7. Reglas de Diseño y Catálogo de Skills
- **Diseño Mobile-First Obligatorio:** Vistas 100% responsivas con Tailwind CSS / Flowbite / DaisyUI, sin `overflow-x` en móvil (`sm`), tablet (`md`) ni PC (`lg`/`xl`).
  - Prohibido alterar arbitrariamente la paleta cromática, tipografías, radios de borde (`rounded-*`) o espaciados entre vistas. La interfaz debe mantener una estética profesional, limpia y coherente en todos los módulos (Admin, Profesor, Alumno).
  - Cada tabla, calendario o formulario complejo debe adaptarse a pantallas pequeñas mediante contenedores con `overflow-x-auto`, layouts en tarjetas (`cards`) apilables o modales adaptativos.
- **Catálogo de Skills del Proyecto (`.skills/`):**
  1. `feature-scaffolder`: Andamiaje de paquetes *Package by Feature* (Entidad, DTOs, Repositorio, Servicio, Controlador, Vistas).
  2. `htmx-view-builder`: Fragmentos interactivos responsivos con Thymeleaf, Tailwind y atributos HTMX (`hx-*`).
  3. `stripe-flow-validator`: Gestión de webhooks de Stripe, verificación de firmas criptográficas y flujos de pago.
  4. `business-rules-tester`: Pruebas JUnit 5 + Mockito enfocadas en reglas de negocio estrictas.
  5. `git-commits`: Estandarización de commits bajo Conventional Commits (`feat:`, `fix:`, `refactor:`, etc.).

---

# 8. Restricciones Técnicas y Anti-Patrones
- **NO Single Page Applications (SPA):** Prohibido React, Angular o Vue. Dinamismo mediante Thymeleaf + HTMX + JS ES6+ nativo.
- **NO Borrado en Cascada Destructivo:** Aplicar siempre borrado lógico (soft delete) o transición a `INACTIVO` preservando la integridad referencial histórica.
- **NO IDs Secuenciales en URLs Críticas:** Usar UUIDs o tokens criptográficos para pagos, activaciones e invitaciones.
- **Transaccionalidad Estricta (@Transactional):** En compras, deducción de clases y reservas.
- **Prioridad de Legibilidad sobre Concisión (Anti-One-Liners):** Prohibido compactar lógica compleja en streams ilegibles o lambdas anidadas. Priorizar bucles imperativos claros y métodos auxiliares descriptivos.
- **Uso Obligatorio de `this.` para Atributos de Instancia:** En todo el código Java del backend, es obligatorio anteponer `this.` a cualquier lectura o asignación de campos de clase (`this.nombre = nombre;`, `return this.estado;`) para evitar *shadowing*.
- **Prohibición Léxica Expresa:** Queda terminantemente prohibido utilizar los términos *"profesor tutor"* o *"docente"* en cualquier texto, vista, variable o comentario de la aplicación.

---

# 9. Registro de Decisiones de Arquitectura (ADR)
El historial completo e inmutable de decisiones técnicas, diseño y memoria acumulada del proyecto reside en: [`docs/REGISTRO_DECISIONES.md`](file:///c:/Users/anton/Desktop/Programaci%C3%B3n/ManDS/docs/REGISTRO_DECISIONES.md)