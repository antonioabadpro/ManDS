# AGENTS.md - Sistema ERP para Autoescuelas (TFG)
> Guía maestra de arquitectura, directrices de interacción, reglas de dominio y memoria normativa para los agentes de desarrollo.

---

# 1. Reglas Supremas y Comportamiento del Agente

## 1.1. Modo de Interacción y Rol
- **Idioma Obligatorio:** Español de España (`es-ES`) SIEMPRE en explicaciones, preguntas, respuestas, comentarios y documentación.
- **Rol de Mentor:** Desarrollador Sénior y Mentor. Explica siempre el porqué técnico y de arquitectura en 2-3 frases antes de entregar código.
- **Corrección Proactiva:** Señala inmediatamente errores de diseño, seguridad, concurrencia o negocio, aportando la solución óptima.
- **Modo Quirúrgico (Diff):** Prohibido reescribir archivos completos salvo creación o petición expresa; devolver solo diffs o fragmentos modificados con ubicación exacta.
- **Planificación en Artefactos:** En tareas de más de 2 pasos, presenta obligatoriamente un Plan de Implementación conceptual en un artefacto Markdown y espera confirmación antes de codificar.
- **Skills del Proyecto:** Usar skills de `.agents/skills/` bajo carga progresiva (*progressive disclosure*), mejorándolas si procede.
- **Anti-Test-Lock-In:** El código del desarrollador es la fuente de verdad oficial; prohibido revertir su lógica para pasar tests. Ante fallos: 1) Si responde al nuevo diseño, adaptar aserciones y mocks del test; 2) Si es un bug técnico colateral (ej. NPE), corregirlo sin alterar la intención original.

## 1.2. Memoria Viva y Registro de Decisiones
- **Separación de Responsabilidades:** `AGENTS.md` contiene **exclusivamente reglas activas**. El historial cronológico inmutable reside en [`docs/REGISTRO_DECISIONES.md`](file:///c:/Users/anton/Desktop/Programaci%C3%B3n/ManDS/docs/REGISTRO_DECISIONES.md); prohibido acumular tablas históricas aquí.
- **Triggers de Memoria (*"recuerda"*, *"anota"*, *"apunta"*):** Si es regla activa de negocio/técnica/UI, añadirla a `AGENTS.md` (máx. 2-3 líneas atómicas). Si es decisión o hito completado (ADR), anexar fila a `docs/REGISTRO_DECISIONES.md` (fecha DD/MM/YYYY, resumen y justificación).

## 1.3. Dudas y Seguridad
- **Cero Suposiciones Unilaterales:** Ante dudas de negocio o técnicas, plantear alternativas con pros y contras antes de codificar.
- **Seguridad en BD y Despliegue:** Prohibido ejecutar scripts destructivos (`DROP`, `TRUNCATE`, cambios irreversibles en Supabase) o finalizar despliegues a Render sin confirmación explícita con análisis de impacto.

## 1.4. Directrices UI/UX Responsive (Mobile-First) y JS No Intrusivo
- **Responsividad Total:** Vistas y modales (Tailwind / Flowbite / DaisyUI) 100% responsivos sin `overflow-x` en móvil (<640px), tablet (768px) y escritorio (>=1024px). Tablas con `overflow-x-auto` o tarjetas apiladas; navegación colapsable en drawer/hamburguesa.
- **Cohesión Visual y Tipografía:** Prohibido alterar paleta corporativa, radios (`rounded-*`) o espaciados. Tipografía única `Inter` (`font-sans`), prohibiendo terminantemente `font-mono`.
- **Formateo de Fechas:** Toda fecha y hora mostrada debe incluir obligatoriamente las clases `tabular-nums` y `whitespace-nowrap`.
- **JavaScript No Intrusivo (CSP):** Prohibido JS inline en Thymeleaf, sintaxis `/*[[${...}]]*/` o eventos inline (`onclick`, etc.). Todo script reside en `src/main/resources/static/js/`, cargado con `defer` y vinculado con `addEventListener`.
- **Comunicación y Modularización JS:** Paso de datos exclusivo mediante `data-*` (`dataset`). Prohibidos scripts monolíticos: 1 archivo JS por vista bajo patrón *Named Event Handlers* (`inicializar...`).
- **Fragments por Rol:** Todo fragment de rol reside en `templates/{admin|alumno|profesor}/fragments/`. La raíz `templates/fragments/` se reserva en exclusiva para componentes transversales compartidos.

---

# 2. Documentación de Referencia y Memoria del Proyecto
- **Especificación Técnica y Arquitectura:** [`docs/ARQUITECTURA.md`](file:///c:/Users/anton/Desktop/Programaci%C3%B3n/ManDS/docs/ARQUITECTURA.md) (Stack tecnológico, estructura *Package by Features*, entidades del dominio y casos de uso).
- **Registro Histórico de Decisiones (ADR):** [`docs/REGISTRO_DECISIONES.md`](file:///c:/Users/anton/Desktop/Programaci%C3%B3n/ManDS/docs/REGISTRO_DECISIONES.md) (Histórico cronológico inmutable de cambios y decisiones técnicas).
- **Diagramas Oficiales PlantUML:** [`Diagramas/Diagrama_ER.puml`](file:///c:/Users/anton/Desktop/Programaci%C3%B3n/ManDS/Diagramas/Diagrama_ER.puml) y [`Diagramas/Diagrama_CU.puml`](file:///c:/Users/anton/Desktop/Programaci%C3%B3n/ManDS/Diagramas/Diagrama_CU.puml).

---

# 3. Reglas de Negocio Estrictas

## 3.1. Autenticación, Registro y Estados de Usuario
- **Roles y Acceso:** Roles estrictos `ADMIN`, `PROFESOR`, `ALUMNO`. Login indistinto por nombre de usuario (`username`) o correo (`email`) con contraseña.
- **Tokens Efímeros:** Alta de profesor exclusiva de Admin con `TokenVerificacion` de 60 min. Tokens de recuperación de contraseña expiran a los 15 min (`TokenVerificacionService`).
- **Ciclo de Alumno:** Registro público en BD como `INACTIVO`. Pasa a `ACTIVO` y formaliza matrícula tras confirmar pago en Stripe Checkout; purga automática de huérfanos en cancelación/abandono.
- **Trazabilidad Lógica:** Borrado lógico (*soft delete* a `INACTIVO`) en profesores y alumnos, preservando histórico de clases, exámenes y pagos.
- **URLs Limpias y Flash Attributes:** Prohibidos query params en login/auth. Notificaciones exclusivas vía Flash Attributes normalizados (`errorLogin`, `logoutExitoso`, `sesionExpirada`, `registroExitoso`, `cuentaActivada`, `tokenInvalido`, `mensajeEnviado`, `mensajeExito`). Acceso autenticado a `/` o `/login` cierra sesión automáticamente.

## 3.2. Matrícula, Tarifas y Convocatorias de Examen
- **Límite y Modalidades:** 1 carnet activo por alumno. Modalidades: `TEORICO_PRACTICA` y `PRACTICA`. Tipos: *Nueva Matriculación* y *Renovación de Matrícula*.
- **Tarifario Oficial:** Matrícula (incluye 2 convocatorias DGT): 250 € (estándar: B, A2, etc.) / 450 € (pesados: C, D). Clase suelta: 30 € / 60 €. Bonos (10, 15, 20 clases): 270 € / 400 € / 500 € (estándar) y 540 € / 800 € / 1.000 € (pesados). Renovación tras agotar convocatorias.
- **Vinculación Alumno-Profesor:** Un alumno solo tiene profesor asignado si tiene matrícula activa; sin ella, `profesor` es obligatoriamente `null`.
- **Convocatorias y Suspensos:** 2 convocatorias por matrícula. Con `convocatorias <= 0` o 2 suspensos, se bloquea compra de saldo, acceso a Tests Teóricos y reservas de clases hasta abonar Renovación en Stripe.
- **Secuencia en TEORICO_PRACTICA:** Exige examen teórico aprobado (`esApto = true`) con fecha previa para comprar clases, reservar en calendario o solicitar examen práctico DGT.
- **Cierre por Práctico APTO:** Inactiva matrícula (`estaActiva = false`), desvincula profesor (`alumno.setProfesor(null)`), cancela reservas futuras y habilita nuevo permiso.
- **Inmutabilidad y Calificación:** Examen calificado (`esApto != null`) es inmutable. Calificación exige modal en 2 pasos y envía email al alumno (felicitación en `APTO` o deducción de convocatoria y restantes en `NO APTO`).

## 3.3. Flota de Vehículos y Profesores
- **Propiedad y Asignación:** Flota propiedad de la autoescuela. Asignación exclusiva 1-a-1 a profesores habilitados en `listaTiposCarnet`.
- **Baja y Reasignación de Profesor:** Soft delete a `INACTIVO`, vehículo liberado a `DISPONIBLE`, clases futuras canceladas con aviso por email. Reasignación por menor carga (`ORDER BY SIZE(p.listaAlumnos) ASC`) entre habilitados.
- **Inmutabilidad y Bloqueos de Flota:** Prohibido reasignar o desvincular vehículos con clases `PENDIENTE` (exige `RECIBIDA` o `CANCELADA`). Examen práctico oficial en vehículo cancela automáticamente todas sus clases de ese día con aviso por email.
- **Baja de Vehículos:** Solo si está `DISPONIBLE`. Pasa a `INACTIVO` (soft delete) con prohibición estricta de reactivación.
- **Incidencias y MANTENIMIENTO:** Incidencia a `EN_PROCESO` pasa vehículo a `MANTENIMIENTO` (cancela clases, bloquea reservas y notifica por email; alumnos no pueden reservar). Estado bloqueado hasta resolver incidencia a `RESUELTA` (restaura estado previo `DISPONIBLE`/`OCUPADO` y notifica por email).

## 3.4. Algoritmo y Control de Reserva de Clases
- **Fórmula de Capacidad:** $\text{CapacidadReserva} = \text{saldoClases} - \text{clasesReservadasPendientes}$.
- **Condición de Compra:** Matrícula activa, convocatorias vigentes y teórico aprobado (en `TEORICO_PRACTICA`). Incrementa `saldoClases += cantidad` sin exigir saldo previo agotado.
- **Condición de Reserva:** $\text{CapacidadReserva} > 0$, `convocatorias > 0`, teórico aprobado (en `TEORICO_PRACTICA`), vehículo operativo (no `MANTENIMIENTO`) y franja dentro del turno del profesor.
- **Ciclo y Deducción Atómica:** Registrar ficha pasa clase a `RECIBIDA` y descuenta `saldoClases -= 1` y `clasesReservadasPendientes -= 1`. Cancelar (`PENDIENTE` a `CANCELADA`) decrementa solo `clasesReservadasPendientes -= 1`.
- **Límite de Cancelación y Concurrencia:** Alumno no puede cancelar clase ya iniciada (`fechaHora <= now`). Concurrencia protegida con `UNIQUE(profesor_id, fecha_hora)` y `@Transactional` atómica (HTTP 409 / feedback HTMX).
- **Ventana Temporal y Precedencia:** Clases estrictamente entre `fechaMatriculacion` y examen práctico. Todo examen práctico debe celebrarse tras la última clase recibida.
- **Festivos y Vacaciones:** Bloqueo en festivos nacionales de España (fijos y Semana Santa Meeus/Butcher) y vacaciones (1–14 agosto y 23 dic–7 ene). Doble barrera: celda bloqueada en SSR y `ReglaNegocioException` en backend.
- **Cancelación Asimétrica:** Cancelación por profesor (`canceladaPorProfesor = true`) bloquea el tramo de 45 min en ambos calendarios. Cancelación por alumno lo libera a `LIBRE`.

## 3.5. Circuito y Gestión de Solicitudes de Examen (DGT)
- **Roles y Atributos:** Control exclusivo de Admin (fijar fechas, cupos, aceptar/rechazar); profesores solo consultan y califican actas. Registra `fechaHora` (`LocalDateTime`) y `tipo` (`TipoExamen`). En solicitudes `TEORICO`, `profesor` es obligatoriamente `null`; `comentarioJustificacion` es exclusivo de Admin para motivar rechazos.
- **FIFO y 2 Fechas Mensuales:** Cola gestionada estrictamente por antigüedad (`ORDER BY fecha_hora ASC`). Admin fija 2 fechas oficiales/mes (fecha 2 >= fecha 1, días hábiles no festivos/vacacionales) y cupos independientes por carnet y tipo; fijación inmutable con doble confirmación.
- **Resolución y Cupos:** Rechazo exige justificación obligatoria y email. Aceptación valida cupo máximo por carnet/tipo, despacha citación por email y bloquea la jornada completa del vehículo.
- **Incompatibilidad y Clases Previas:** Prohibido tener clases `PENDIENTE` posteriores a la fecha de examen (se permiten previas). Obligatorio tener clases `RECIBIDA` antes de ser citado o examinarse de prueba práctica DGT.
- **Tasas y Concurrencia de Peticiones:** Tasas DGT incluidas en matrícula y renovación (solicitud sin coste). Prohibido solicitar examen si existe petición `PENDIENTE` o `ACEPTADA` con fecha futura; `RECHAZADA` permite nueva solicitud.
- **Validaciones de Expediente:** Matrícula activa obligatoria. Prohibido solicitar teórico en `PRACTICA` o si ya fue aprobado en `TEORICO_PRACTICA`. Expedientes con ambos exámenes aprobados y matrícula inactiva no admiten solicitudes.

## 3.6. Cuadrícula Semanal Nativa de Calendario (SSR + Thymeleaf + HTMX)
- **Estructura y Turnos:** Calendario laboral semanal (L-V). Franjas de clase de 45 min dentro de los turnos oficiales:
  - `MATINAL` (08:00–15:00): 9 franjas (08:00, 08:45, 09:30, 10:15, 11:00, 11:45, 12:30, 13:15, 14:00 hasta 14:45).
  - `TARDE` (15:00–22:00): 9 franjas (15:00, 15:45, 16:30, 17:15, 18:00, 18:45, 19:30, 20:15, 21:00 hasta 21:45).
- **Navegación y Bloqueo DGT:** Barra con botones `<`, `>`, `Hoy` y rango semanal visible (ej. "5 – 9 oct 2026"; solo vista semanal). Jornada de examen oficial en color morado cubriendo el día completo, bloqueando cualquier reserva en el vehículo/profesor.
- **UI/UX y Privacidad:** CSS Grid/Flexbox claro/oscuro (`dark:`), tarjetas contenidas (`rounded-lg`/`xl`) sin desbordar celda. En profesor muestra nombre de alumno (abre ficha HTMX). En alumno muestra `"Horario Ocupado"` (solo sus clases son clicables); celdas libres válidas abren modal de reserva.

---

# 4. Restricciones Técnicas y Anti-Patrones

- **Stack Web:** Prohibido SPA (React, Vue, Angular) y librerías cliente pesadas de calendario (FullCalendar). Dinamismo exclusivo con Thymeleaf + HTMX + Tailwind CSS + JS ES6+ nativo.
- **Integridad y Seguridad:** Prohibido borrado físico en cascada (usar siempre soft delete a `INACTIVO`). Prohibidos IDs secuenciales en URLs críticas (usar UUIDs o tokens criptográficos). Transaccionalidad `@Transactional` obligatoria en compras, deducciones y reservas concurrentes.
- **Buenas Prácticas Java Backend:** Priorizar legibilidad sobre lambdas/streams ilegibles. Uso obligatorio de `this.` para toda lectura o asignación de campos de instancia (`this.campo = valor;`, `return this.campo;`) para prevenir shadowing.
- **Prohibición Léxica Expresa:** Terminantemente prohibido usar los términos *"profesor tutor"* o *"docente"* en textos, vistas, variables o comentarios del sistema. Prohibido mostrar al usuario o incluir en comentarios o código numeraciones o identificadores de reglas (utilizar siempre explicaciones semánticas del requisito o validación).
- **Rangos de IDs en `data.sql`:** `persona`: Admin (ID 1), libres (2..99), Profesores (100..199), Alumnos (200..299). Vehículos agrupados por carnet (ver enum `TipoCarnet.java`).
- **Pirámide de Testing:** Servicios, mappers y utilidades se validan exclusivamente con tests unitarios en JUnit 5 + Mockito (`@ExtendWith(MockitoExtension.class)`); prohibido `@SpringBootTest` en ellos.
- **Aserciones Web Desacopladas de HTML:** Prohibido validar cadenas HTML estáticas en Thymeleaf (`content().string(...)`). Tests de controladores verifican solo estado HTTP (`status()`), vista/redirección (`view()`, `redirectedUrl()`) y modelo (`model().attributeExists(...)`).
- **Seguridad RBAC y Context Caching:** Autorización y accesos 403 pertenecen en exclusiva a `SeguridadIntegrationTest`. Todo test `@SpringBootTest` debe extender de `BaseIntegrationTest` sin declarar `@TestPropertySource` ni `@MockitoBean` en hijas para preservar el *Spring Test Context Caching*.
- **Ejecución Selectiva:** Ejecutar únicamente el test afectado (`mvn test -Dtest=NombreTest`) durante el desarrollo activo.

---

# 5. Catálogo de Skills del Proyecto (`.agents/skills/`)
1. `feature-scaffolder`: Andamiaje de paquetes *Package by Feature* (Entidad, DTOs, Repositorio, Servicio, Controlador, Vistas).
2. `htmx-view-builder`: Fragmentos interactivos responsivos con Thymeleaf, Tailwind y atributos HTMX (`hx-*`).
3. `stripe-flow-validator`: Gestión de webhooks de Stripe, verificación de firmas criptográficas y flujos de pago.
4. `business-rules-tester`: Pruebas JUnit 5 + Mockito enfocadas en reglas de negocio estrictas.
5. `git-commits`: Estandarización de commits bajo Conventional Commits (`feat:`, `fix:`, `refactor:`).
6. `supabase`: Directrices generales de configuración, cliente y migraciones en Supabase.
7. `supabase-postgres-best-practices`: Optimización de esquemas relacionales, transacciones, RLS e índices en PostgreSQL.