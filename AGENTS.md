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
- **Uso de Skills:** Utiliza siempre las skills del proyecto en `.agents/skills/` bajo el mecanismo de carga progresiva (*progressive disclosure*). Si detectas oportunidades de mejora en una skill, corrígela o amplíala.
- **Código del Desarrollador como Fuente de Verdad (Anti-Test-Lock-In):** Los cambios de negocio o arquitectura introducidos por el desarrollador representan la nueva intención oficial. Queda **TERMINANTEMENTE PROHIBIDO** revertir o alterar su lógica para "poner los tests en verde".
  - *Protocolo ante Fallo de Tests:* 1) Analizar si se debe al nuevo diseño del desarrollador; 2) En caso afirmativo, adaptar y actualizar las expectativas del test (`assertEquals`, mocks, datos); 3) Si es un bug colateral imprevisto (ej. NPE), corregir el detalle técnico sin alterar la intención original.

## 1.2. Protocolo de Memoria Viva y Registro de Decisiones ("Recuerda...")
- **Separación de Responsabilidades:** Este archivo contiene **exclusivamente directrices normativas y reglas de negocio activas**. El historial cronológico de decisiones de arquitectura e hitos completados reside en [`docs/REGISTRO_DECISIONES.md`](file:///c:/Users/anton/Desktop/Programaci%C3%B3n/ManDS/docs/REGISTRO_DECISIONES.md). Prohibido acumular tablas históricas en `AGENTS.md`.
- **Protocolo de Actuación ante Triggers (*"recuerda"*, *"anota"*, *"apunta esto"*, *"guarda esto en memoria"*):**
  - **Regla de negocio, técnica, arquitectura o UI/UX:** Incorporar o modificar directamente la regla en la sección correspondiente de `AGENTS.md`. Toda regla debe tener un tope estricto de 2-3 líneas, redactada de forma atómica y concisa.
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
- **Colocación y Modularización de Fragments por Rol:** Todo fragment específico de un rol debe residir obligatoriamente en `templates/{admin|alumno|profesor}/fragments/`. La carpeta raíz `templates/fragments/` se reserva en exclusiva para componentes compartidos por varios roles.

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
- **Alta y Verificación:** Alta de profesores exclusiva del Administrador con `TokenVerificacion` efímero de 60 min. Tokens de recuperación de contraseña expiran a los 15 min (`TokenVerificacionService`).
- **Alta de Alumnos:** Registro público en BD como `INACTIVO`. Se activa (`ACTIVO`) y formaliza su matrícula tras confirmación de pago en Stripe Checkout; purga automática de huérfanos en cancelación/abandono.
- **Trazabilidad Lógica:** Borrado lógico (`INACTIVO`) en profesores y alumnos preservando histórico inmutable de clases, exámenes y pagos.

## 3.2. Matrícula, Tarifas y Convocatorias de Examen
- **Límite de Carnet:** 1 carnet activo simultáneamente por alumno.
- **Tarifario Oficial:**
  | Concepto | Pesados (C, D) | Resto (B, A2, etc.) |
  | :--- | :--- | :--- |
  | Matrícula (incluye 2 convocatorias) | 450 € | 250 € |
  | Clase práctica suelta | 60 € | 30 € |
  | Bono 10 / Bono 15 / Bono 20 | 540 € / 800 € / 1.000 € | 270 € / 400 € / 500 € |
  | Renovación de Matrícula | Tarifa de renovación estipulada tras agotar 2 convocatorias |
- **Modalidades de Matrícula:** *Teórico + Prácticas*, *Sólo Prácticas*. Tipos: *Nueva Matriculación* y *Renovación de Matrícula*.
- **Vinculación Alumno-Profesor:** Un alumno solo tiene profesor asignado si dispone de matrícula activa habilitada para dicho carnet. Sin matrícula activa, el campo `profesor` es obligatoriamente `null`.
- **Convocatorias y Suspensos:** 2 convocatorias iniciales por matrícula. Al agotar convocatorias (`convocatorias <= 0`) o acumular 2 suspensos, se bloquea la adquisición de saldo, el acceso a los Test Teóricos y el acceso al calendario de reservas hasta abonar la Renovación vía Stripe.
- **Secuencia en TEORICO_PRACTICA:** Exige examen teórico aprobado (`esApto = true`) en fecha previa para acceder al calendario, reservar clases prácticas o solicitar examen práctico oficial.
- **Cierre por Examen Aprobado:** Tras calificar práctico como `APTO`, la matrícula pasa a inactiva (`estaActiva = false`), se desvincula al profesor (`alumno.setProfesor(null)`), se cancelan reservas futuras y se habilita nuevo permiso.
- **Inmutabilidad y Doble Confirmación:** El examen calificado (`esApto != null`) es inmutable e impide re-calificaciones. La calificación del profesor exige modal en 2 pasos con consecuencias normativas irreversibles según modalidad y resultado.
- **Notificaciones tras Calificar Examen:** Toda calificación notifica por email al alumno: felicitación en `APTO` (desbloqueo de prácticas en teórico; cierre de matrícula en práctico) o aviso de convocatoria deducida y restantes en `NO APTO` (bloqueo al 2º suspenso).

## 3.3. Flota de Vehículos y Profesores
- **Propiedad y Asignación:** Vehículos propiedad de la autoescuela. Asignación exclusiva 1-a-1 a profesores habilitados en su `listaTiposCarnet`.
- **Baja y Reasignación de Profesor:** Borrado lógico a `INACTIVO`, vehículo liberado a `DISPONIBLE`, clases futuras canceladas con aviso por email. Reasignación por menor carga (`ORDER BY SIZE(p.listaAlumnos) ASC`) entre profesores habilitados.
- **Bloqueo por Examen Práctico:** Asignar examen práctico oficial a un vehículo cancela automáticamente todas sus clases prácticas de ese día con aviso por email a los alumnos.
- **Inmutabilidad con Clases Pendientes:** Prohibido reasignar o desvincular un vehículo con clases en `EstadoClase.PENDIENTE`; exige completarlas (`RECIBIDA`) o cancelarlas previamente.
- **Baja y Borrado Lógico de Vehículos:** Solo permitida si el vehículo está `DISPONIBLE`. Pasa a `INACTIVO` sin borrado físico en BD, con prohibición estricta de reactivación tras la baja.
- **Incidencias y Estado MANTENIMIENTO:** Incidencia a `EN_PROCESO` transiciona el vehículo a `MANTENIMIENTO` (cancela clases, bloquea reservas y envía email a profesor y alumnos). No se puede modificar el estado de un vehículo `MANTENIMIENTO`. Solo se desbloquea al pasar la incidencia a `RESUELTA` desde el panel de Incidencias (recupera su estado previo `DISPONIBLE` u `OCUPADO` y envía email al profesor y alumnos).
    Un alumno no puede reservar si su vehículo está en `MANTENIMIENTO`.

## 3.4. Algoritmo y Control de Reserva de Clases
- **Capacidad de Reserva:** $\text{CapacidadReserva} = \text{saldoClases} - \text{clasesReservadasPendientes}$.
- **Condición de Compra:** Alumno solo puede comprar clases/bonos con saldo agotado ($\text{saldoClases} = 0$). La compra incrementa `saldoClases += cantidadComprada`.
- **Condición de Reserva:** Exige $\text{CapacidadReserva} > 0$, convocatorias vigentes (`convocatorias > 0`), examen teórico aprobado (en `TEORICO_PRACTICA`), vehículo operativo (no en `MANTENIMIENTO`) y franja dentro del turno del profesor asignado.
- **Deducción y Cancelación Atómica:** Al registrar ficha técnica pasa a `RECIBIDA` y descuenta `saldoClases -= 1` y `clasesReservadasPendientes -= 1`. Al cancelar (`PENDIENTE` a `CANCELADA`), solo decrementa `clasesReservadasPendientes -= 1`.
- **Límite Horario de Cancelación:** El alumno no puede cancelar una clase una vez iniciada (`fechaHora <= now`), protegiendo demoras del profesor en cumplimentar la ficha.
- **Concurrencia Pesimista:** Restricción única en BD `UNIQUE(profesor_id, fecha_hora)` y transacción `@Transactional` con verificación atómica (HTTP 409 / feedback HTMX en colisión).
- **Ventana Temporal y Precedencia:** Fechas de clase estrictamente entre `fechaMatriculacion` y examen práctico oficial. Todo examen práctico debe celebrarse con fecha posterior a la última clase recibida.
- **Bloqueo en Festivos y Vacaciones:** Prohibido reservar en festivos nacionales oficiales de España (fijos y Semana Santa móvil Meeus/Butcher) y vacaciones de la autoescuela (1-14 agosto y 23 dic-7 ene). Doble barrera: celda bloqueada en SSR y `ReglaNegocioException` en backend.
- **Cancelación Asimétrica y Bloqueo de Franja:** Cancelación por profesor (`canceladaPorProfesor = true`) bloquea ese tramo de 45 min en ambos calendarios impidiendo reservas. Cancelación por alumno libera el tramo a `LIBRE`.

## 3.5. Circuito y Gestión de Solicitudes de Examen (DGT)
- **Control Centralizado y Calificación:** Exclusivo del Administrador (fijar fechas, cupos, aceptar o rechazar solicitudes). Profesores consultan los exámenes de sus alumnos y califican las actas oficiales.
- **Orden FIFO y 2 Fechas Oficiales al Mes:** Solicitudes pendientes gestionadas estrictamente por antigüedad (`ORDER BY fecha_hora ASC`). El Administrador fija manualmente 2 fechas oficiales por mes (fecha 2 posterior o igual a fecha 1, días hábiles sin festivos ni vacaciones). La fijación de fechas y cupos es inmutable con doble confirmación.
- **Cupos y Resolución:** Rechazo exige justificación obligatoria y envío de email. Aceptación valida cupo máximo por carnet y tipo de examen (cupos independientes para teórica y práctica asignados a ambas fechas), dispara citación por email y bloquea la jornada completa del vehículo.
- **Incompatibilidad y Clases Previas Obligatorias:** Un alumno citado o examinado no puede tener clases `PENDIENTE` posteriores a la fecha de examen (sí se admiten clases formativas previas a dicha fecha). Es obligatorio haber completado clases en estado `RECIBIDA` antes de examinarse o ser citado a prueba práctica DGT.
- **Tasas de Examen Incluidas en Matrícula:** Las tasas oficiales DGT (2 convocatorias) van incluidas en el abono inicial de matrícula y en su renovación. La solicitud de fecha es un trámite administrativo sin cobro en pasarela.
- **Validaciones de Solicitud de Examen:** Exige matrícula activa (`estaActiva = true`). Prohibido solicitar teórico en modalidad `PRACTICA` o si ya fue aprobado en `TEORICO_PRACTICA`. Expedientes con ambos exámenes aprobados y matrícula inactiva no admiten solicitudes.
- **Concurrencia de Solicitudes y Examen Futuro:** Prohibido solicitar examen si existe una petición `PENDIENTE` o una `ACEPTADA` con fecha no celebrada. Solicitudes `RECHAZADAS` habilitan nueva petición inmediata.
- **Atributos y Tipado de Solicitud:** La solicitud registra `fechaHora` (`LocalDateTime`) y `tipo` (`TipoExamen`). En solicitudes teóricas (`TEORICO`) el campo `profesor` es obligatoriamente `null` (sin profesor asignado); `comentarioJustificacion` es exclusivo de Administración para motivar rechazos.

## 3.6. Cuadrícula Semanal Nativa de Calendario (SSR + Thymeleaf + HTMX)
- **Estructura y Turnos Oficiales:** Calendario laboral semanal (lunes a viernes). Las reservas se ciñen a los turnos oficiales y sus franjas hábiles de 45 min:
  | Turno | Horario Oficial | Franjas Hábiles de Clase (45 min) |
  | :--- | :--- | :--- |
  | `MATINAL` | 08:00 – 15:00 | 08:00, 08:45, 09:30, 10:15, 11:00, 11:45, 12:30, 13:15, 14:00 (hasta 14:45) |
  | `TARDE` | 15:00 – 22:00 | 15:00, 15:45, 16:30, 17:15, 18:00, 18:45, 19:30, 20:15, 21:00 (hasta 21:45) |
- **Navegación Unificada:** Barra con botones `<` (anterior), `>` (siguiente), `Hoy` y rango visible (ej. "5 – 9 oct 2026"). Sin cambios de vista (exclusivamente semanal).
- **Bloqueo por Examen DGT:** Jornada de examen oficial en color morado cubriendo el día completo; bloquea terminantemente cualquier reserva en ese vehículo/profesor.
- **UI/UX y Tarjetas Contenidas:** Maquetación CSS Grid/Flexbox responsiva con soporte claro/oscuro (`dark:`). Clases contenidas en tarjetas redondeadas (`rounded-lg`/`rounded-xl`) con margen interior sin desbordar la celda.
- **Privacidad y Permisos:** En profesor muestra nombre de alumno (clic abre ficha técnica HTMX). En alumno muestra `"Horario Ocupado"`; solo sus propias clases son clicables para ver detalles/cancelar. Celdas libres válidas abren modal HTMX de reserva.

---

# 4. Restricciones Técnicas y Anti-Patrones

- **NO Single Page Applications (SPA):** Prohibido React, Angular o Vue. Dinamismo mediante Thymeleaf + HTMX + JS ES6+ nativo.
- **NO Librerías Externas de Calendario (FullCalendar):** Prohibido el uso de FullCalendar v6 o librerías cliente pesadas; las agendas se implementan exclusivamente con SSR nativo en Thymeleaf + HTMX + Tailwind CSS (ver Sección 3.6).
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

# 5. Catálogo de Skills del Proyecto (`.agents/skills/`)
1. `feature-scaffolder`: Andamiaje de paquetes *Package by Feature* (Entidad, DTOs, Repositorio, Servicio, Controlador, Vistas).
2. `htmx-view-builder`: Fragmentos interactivos responsivos con Thymeleaf, Tailwind y atributos HTMX (`hx-*`).
3. `stripe-flow-validator`: Gestión de webhooks de Stripe, verificación de firmas criptográficas y flujos de pago.
4. `business-rules-tester`: Pruebas JUnit 5 + Mockito enfocadas en reglas de negocio estrictas.
5. `git-commits`: Estandarización de commits bajo Conventional Commits (`feat:`, `fix:`, `refactor:`).
6. `supabase`: Directrices generales de configuración, cliente y migraciones en Supabase.
7. `supabase-postgres-best-practices`: Optimización de esquemas relacionales, transacciones, RLS e índices en PostgreSQL.