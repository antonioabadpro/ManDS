# Auditoría de Reglas del Script Semilla (`data.sql`) y Matriz Comparativa con las Reglas de Negocio

> **Documento de Auditoría y Especificación de Dominio**  
> **Fecha:** 01/11/2026 (Actualizado: 02/11/2026)  
> **Ámbito:** `src/main/resources/data.sql` vs. `AGENTS.md` y Requisitos Operativos DGT  
> **Objetivo:** Documentar exhaustivamente las reglas que modela actualmente el script de datos semilla, contrastarlas contra las reglas maestras de negocio y contra las observaciones y requerimientos señalados para el sistema.

---

## 1. Introducción y Propósito del Documento

El archivo de datos semilla `src/main/resources/data.sql` constituye la base de datos de desarrollo y pruebas locales (H2) del ERP de la autoescuela. Modela la estructura operacional de un centro de formación vial español sujeto a la normativa de la Dirección General de Tráfico (DGT).

El presente documento realiza un análisis forense de dicho script para:
1. Catalogar todas las reglas de negocio, invariantes y restricciones que el script ya contempla o modela mediante su conjunto de datos.
2. Comparar de forma exhaustiva el estado actual del script frente a las reglas oficiales recogidas en la memoria normativa (`AGENTS.md`) y las peticiones y correcciones planteadas por la dirección técnica.
3. Detectar discrepancias, inconsistencias matemáticas o desalineaciones en los registros actuales.
4. Proporcionar el censo oficial tabulado de todos los profesores y alumnos poblados actualmente en el sistema, detallando su estado operativo (`ACTIVO` o `INACTIVO`) en la columna final de cada registro.

---

## 2. Reglas Contempladas Actualmente en el Script (`data.sql`)

El script estructura su contenido en 9 bloques principales, reflejando las siguientes reglas de dominio e integridad:

### 2.1. Gestión de Flota de Vehículos (`vehiculo`)
- **Cobertura Completa de Permisos Oficiales:** Se definen 34 vehículos cubriendo la totalidad de los 12 permisos de conducción reconocidos por la DGT (`PERMISO_B`, `PERMISO_A2`, `PERMISO_C`, `PERMISO_D`, `PERMISO_B_E`, `PERMISO_AM`, `PERMISO_A1`, `PERMISO_A`, `PERMISO_B1`, `PERMISO_C_E`, `PERMISO_D_E` y `PERMISO_LVA`).
- **Mínimo de Vehículos por Permiso:** Cada permiso cuenta con un mínimo de 2 vehículos asociados para garantizar operatividad y redundancia formativa.
- **Ciclo de Vida y Estados de Vehículo:** Modela los estados `DISPONIBLE`, `OCUPADO` (asignado a profesor), `MANTENIMIENTO` (bloqueado por incidencia mecánica) e `INACTIVO` (baja lógica definitiva que no admite reactivación).
- **Consistencia Métrica y Odómetros:** Todos los vehículos registran kilometraje acumulado realista (`km`) y fechas coherentes de inspección periódica (`fecha_ultima_revision` y `fecha_proxima_revision`).

### 2.2. Usuarios Base y Jerarquía de Identidad (`persona`)
- **Particionamiento Estricto de Identificadores (IDs):**
  - Administrador de la autoescuela: `ID = 1`.
  - Rango reservado para profesores: `ID = 100 .. 199` (poblados 30 profesores: 100 a 129).
  - Rango reservado para alumnos: `ID = 200 .. 299` (poblados 68 alumnos: 200 a 267).
  - Rango libre para futuras operaciones: `ID = 2 .. 99`.
- **Estados de Usuario y Soft Delete:** Modela estados `ACTIVO` e `INACTIVO` (soft delete). En profesores se incluye 1 docente inactivo (`ID = 105`), y en alumnos se incluyen 2 alumnos inactivos por baja lógica (`ID = 207` y `ID = 215`), preservando íntegramente su histórico formativo.
- **Criptografía Segura:** Todas las contraseñas están precalculadas mediante hash BCrypt (`$2a$10$...`), garantizando autenticación real en el entorno Spring Security.
- **Unicidad de Credenciales:** Nombre de usuario (`nombre_usuario`), correo electrónico (`correo`) y documento de identidad (`dni`) son únicos y no se solapan.

### 2.3. Especialización de Roles y Asignación de Recursos (`profesor`, `profesor_permisos`, `alumno`)
- **Turnos Oficiales de Trabajo:** Distribución exclusiva entre `MATINAL` (jornada de 08:00 a 15:00) y `TARDE` (jornada de 15:00 a 22:00).
- **Asignación 1-a-1 de Vehículos:** Un vehículo solo puede estar asignado como máximo a un profesor en exclusividad (`vehiculo_id UNIQUE`).
- **Profesores de Reserva:** Profesores con `vehiculo_id = NULL` disponibles como dotación adicional sin carga de vehículo inicial.
- **Habilitación de Permisos Docentes:** Registro explícito en la tabla intermedia `profesor_permisos` de los permisos que cada profesor está legalmente facultado para impartir (81 habilitaciones en total).
- **Profesores Polivalentes:** Profesores con habilitación plena en los 12 permisos de la DGT (profesores 128 y 129).

### 2.4. Matrículas y Expedientes Académicos (`matricula`)
- **Unicidad de Matrícula Activa:** Cada alumno dispone de a lo sumo 1 matrícula en estado activo (`esta_activa = true`).
- **Modalidades Reglamentarias:** Se segregan matrículas en `TEORICO_PRACTICA` (para obtención completa) y `PRACTICA` (convalidación teórica por poseer permiso previo).
- **Tipos de Matrícula:** Diferenciación entre `NUEVA_MATRICULACION` y `RENOVACION` (tras agotamiento de convocatorias).
- **Sistema de Convocatorias Oficiales:** Cada expediente arranca con 2 convocatorias DGT. Se modela el agotamiento de convocatorias (`convocatorias = 0`, `convocatorias_gastadas = 2`) forzando la renovación económica.
- **Tarifario Canónico:** 250 € (estándar) y 450 € (pesados) para nueva matrícula; 120 € y 200 € en renovaciones.

### 2.5. Incidencias Mecánicas y Mantenimiento (`incidencia_vehiculo`)
- **Transición de Estados de Avería:** Modela incidencias en estados `ABIERTA`, `EN_PROCESO` y `RESUELTA`.
- **Bloqueo Preventivo:** Una incidencia en estado `EN_PROCESO` coloca automáticamente el vehículo asociado en estado `MANTENIMIENTO`, cancelando clases prácticas y bloqueando nuevas reservas.

### 2.6. Clases Prácticas de Conducción (`clase_practica`)
- **Duración Canónica Estricta:** Todas las 169 clases prácticas registradas tienen una duración exacta de 45 minutos (`duracion = 45`).
- **Respeto a las Franjas Horarias Oficiales:** Las horas de inicio se alinean con las franjas de 45 minutos dentro de cada turno:
  - Turno Matinal: 08:00, 08:45, 09:30, 10:15, 11:00, 11:45, 12:30, 13:15, 14:00 (finaliza 14:45).
  - Turno Tarde: 15:00, 15:45, 16:30, 17:15, 18:00, 18:45, 19:30, 20:15, 21:00 (finaliza 21:45).
- **Continuidad de Odómetro:** El kilometraje inicial (`km_inicio`) de cada clase coincide estrictamente con el kilometraje final (`km_fin`) de la clase anterior en dicho vehículo.
- **Trazabilidad de Cancelaciones:** Registro de cancelaciones por indisposición del profesor (`is_cancelada_por_profesor = TRUE`) o por citación de examen en el vehículo (`is_cancelacion_jornada = TRUE`).

### 2.7. Solicitudes y Exámenes DGT (`solicitud_examen`, `examen`, `fecha_examen`, `cupo_examen`)
- **Relación 1-a-1 Solicitud-Examen:** Todo examen celebrado o citado deriva de una solicitud aprobada previa (`solicitud_id` en la tabla `examen`).
- **Calificación Inmutable:** Se reflejan actas de examen calificadas con `es_apto = true` (Apto), `es_apto = false` (No Apto) y exámenes pendientes de calificar (`es_apto = NULL`).
- **Duración Oficial de Pruebas:** 30 minutos para el examen teórico y 35 minutos para el examen práctico de circulación en vía abierta.
- **Planificación Mensual y Cupos:** La tabla `fecha_examen` define 2 convocatorias por mes (días hábiles), mientras que `cupo_examen` asigna los cupos de aspirantes por carnet.

---

## 3. Comparativa Exhaustiva: Script Semilla vs. Reglas Maestras y Requerimientos

A continuación se contrasta cada una de las reglas solicitadas y señaladas por la dirección frente al estado real de `data.sql` y la lógica de la aplicación:

| Requerimiento / Regla Planteada | Estado en el Script Actual (`data.sql`) | Regla Maestra (`AGENTS.md`) | Diagnóstico y Acción Requerida |
| :--- | :--- | :--- | :--- |
| **1. Cancelación de clases en el pasado:** No permitir cancelar si `fechaHora <= now`, aunque el profesor no haya enviado la ficha. | Las clases pasadas están registradas como `RECIBIDA` o `CANCELADA`. No existen clases huérfanas pasadas en `PENDIENTE`. | El alumno no puede cancelar una clase ya iniciada (`fechaHora <= now`). Lanza excepción de negocio. | **Cumplido en seed; blindado en backend.** La restricción reside en la capa de servicio (`ClasePracticaService`) comprobando `clase.getFechaHora().isBefore(LocalDateTime.now())`. |
| **2. Duración de clases a 45 minutos:** Todas las clases deben durar 45 min. | Las 169 clases prácticas tienen `duracion = 45`. | Clases con ajuste canónico de 45 minutos. | **Cumplido al 100% en `data.sql`.** |
| **3. Franjas horarias de 45 minutos:** Inicios en 08:00, 08:45, 09:30... y 15:00, 15:45, 16:30... | Todas las clases siguen exactamente las franjas canónicas matinales y vespertinas. | Cuadrícula nativa de 9 tramos de 45 min por turno (08:00–14:45 y 15:00–21:45). | **Cumplido al 100% en `data.sql`.** |
| **4. Exámenes DGT en días hábiles:** No pueden coincidir con festivos nacionales ni vacaciones (1–14 ago, 23 dic–7 ene). | Las convocatorias se sitúan en días laborables (ej. 20 y 27 de agosto, tras las vacaciones de agosto). | Admin fija 2 fechas oficiales/mes en días hábiles no festivos ni vacacionales. | **Cumplido en convocatorias de `data.sql`.** Debe mantenerse la validación de festivos (Meeus/Butcher y fijos) en backend. |
| **5. Validación de DNIs por Módulo 23:** Algoritmo oficial del Ministerio del Interior y lista negra de DNIs no utilizables. | **Inconsistencia detectada en 26 DNIs.** Profesores 116..120, 122..129 y alumnos 253..257, 259..266 tienen letras secuenciales sintéticas erróneas (ej. `12098765A` en vez de `Y`). Ninguno está en la lista negra. | Validación estricta con algoritmo aritmético de módulo 23. Prohibición expresa de DNIs de prueba (`12345678Z`, `00000000T`, etc.). | **Requiere corrección en `data.sql`.** Se deben recalcular las letras oficiales para los 26 registros con inconsistencia aritmética. |
| **6. Incompatibilidad vehículo-alumno según permiso del profesor:** Si el profesor tiene varios permisos, el vehículo asignado debe coincidir con el permiso del alumno al dar clase. | En `data.sql` no se producen solapamientos de clases con vehículos incompatibles. Todos los alumnos vinculados a un profesor con vehículo activo comparten el tipo de carnet. | Flota asignada 1-a-1 a profesores habilitados. Una clase práctica exige que el vehículo asignado al profesor corresponda al permiso de la matrícula del alumno. | **Cumplido en `data.sql`.** La regla debe ser validada preventivamente en la lógica de asignación de vehículo/alumno en backend. |
| **7. Mínimo 10 clases prácticas para aprobar examen práctico:** El alumno debe haber completado al menos 10 clases antes de aprobar el práctico. | Todos los alumnos con examen práctico aprobado en el script tienen entre 10 y 14 clases recibidas. | Obligatorio tener clases recibidas antes de ser citado o examinarse de prueba práctica. | **Cumplido al 100% en `data.sql`.** |
| **8. Teórico aprobado antes de clases en Teórico-Práctica:** Un alumno de esta modalidad no puede reservar ni recibir clases sin haber aprobado el teórico. | Ningún alumno de `TEORICO_PRACTICA` sin teórico aprobado tiene clases prácticas registradas en `data.sql`. | Secuencia obligatoria: exige examen teórico aprobado (`esApto = true`) para asignar profesor, comprar clases o reservar en calendario. | **Cumplido al 100% en `data.sql`.** |
| **9. Matrícula activa obligatoria para solicitud pendiente:** No se admiten solicitudes pendientes sin expediente activo. | Todas las solicitudes pendientes corresponden a alumnos con `esta_activa = true`. | Matrícula activa obligatoria para tramitar o mantener solicitudes de examen. | **Cumplido al 100% en `data.sql`.** |
| **10. Añadir un profesor por cada carnet sin vehículo asignado:** 12 profesores sin vehículo, uno por cada permiso DGT. | Se incorporaron los profesores 116 a 127 con `vehiculo_id = NULL`, cubriendo los 12 permisos (AM, A1, A2, A, B, B_E, B1, C, C_E, D, D_E, LVA). | Soporte para profesores habilitados sin vehículo de dotación inicial. | **Cumplido al 100% en `data.sql`.** |
| **11. Añadir 2 profesores con todos los permisos:** Profesores polivalentes con los 12 permisos. | Profesores 128 (Julián Pascual) y 129 (Lorena Pastor) disponen de las 12 acreditaciones en `profesor_permisos`. | Cobertura de habilitaciones múltiples por profesor. | **Cumplido al 100% en `data.sql`.** |
| **12. Sin profesor si no ha aprobado el teórico (Teórico-Práctica):** `profesor_id` debe ser `NULL`. | Todos los alumnos en `TEORICO_PRACTICA` con teórico pendiente tienen `profesor_id = NULL`. | En modalidad `TEORICO_PRACTICA`, si no ha aprobado el teórico, `profesor` es obligatoriamente `null`. | **Cumplido al 100% en `data.sql`.** |
| **13. No marcar clase como `RECIBIDA` antes de su inicio:** El profesor no puede certificar la clase antes de su fecha/hora. | Todas las clases `RECIBIDA` tienen fechas y horas pasadas respecto a la fecha de simulación. | Validación en backend de momento de firma. | **Cumplido en `data.sql`; regla de negocio activa en backend.** |
| **14. Bloqueo de calificación de examen antes de la fecha:** Admin y profesores no pueden calificar antes del día del examen. | En `data.sql`, los exámenes futuros tienen `es_apto = NULL`. | Exámenes calificados son inmutables; calificación solo a partir del día del examen. | **Cumplido en datos; backend debe impedir `POST /calificaciones` si `fechaHora > now`**. |
| **15. Reafirmación: Alumno Teórico-Práctica sin profesor hasta aprobar teórico.** | Ídem punto 12. | Invariante de asignación docente en expediente. | **Cumplido al 100% en `data.sql`.** |
| **16. Añadir 5 alumnos Teórico-Práctica sin aprobar con solicitud teórica:** Alumnos sin profesor con solicitud teórica tramitada. | Alumnos 253, 254, 255, 256, 257 (5 alumnos) tienen matrícula activa, sin teórico, sin profesor y con solicitud teórica aceptada/pendiente. | Expediente en fase de tramitación de primera prueba oficial. | **Cumplido al 100% en `data.sql`.** |
| **17. Añadir 5 alumnos Teórico-Práctica sin aprobar y sin solicitud:** Alumnos que están estudiando el teórico sin haber solicitado examen aún. | **Discrepancia detectada:** Alumnos 260, 261 y 262 (3 alumnos) no tienen ninguna solicitud. Los alumnos 258 y 259 recibieron solicitudes para exámenes futuros. | Expediente en fase de formación teórica inicial sin citación oficial. | **Requiere ajuste en `data.sql`** para completar exactamente los 5 alumnos solicitados sin solicitud de examen. |
| **18. Añadir 5 alumnos Teórico-Práctica con teórico aprobado y sin profesor:** Aprobados pendientes de asignar profesor para iniciar prácticas. | Alumnos 263, 264, 265, 266, 267 (5 alumnos) tienen teórico aprobado (`es_apto = true`), matrícula activa y `profesor_id = NULL`. | Alumno listo para asignación de profesor por menor carga. | **Cumplido al 100% en `data.sql`.** |
| **19. Cada examen debe tener su solicitud asociada (Caso Alumno 21 / ID 220):** Si un alumno tiene aprobado teórico y práctico, debe tener ambas solicitudes registradas. | El Alumno 220 cuenta ahora con la Solicitud 18 (`TEORICO`, aceptada) y Solicitud 19 (`PRACTICO`, aceptada), asociadas respectivamente a los Exámenes 16 y 17. | Trazabilidad administrativa estricta: ningún examen puede existir sin solicitud previa. | **Cumplido al 100% en `data.sql`.** |
| **20. Exámenes con fecha futura bloqueados para calificación:** No pueden tener `es_apto` asignado. | Los exámenes con fecha futura (noviembre de 2026) tienen `es_apto = NULL`. | La calificación requiere celebración previa del examen. | **Cumplido al 100% en `data.sql`.** |
| **21. Automatización de fechas evitando días no lectivos:** No usar simples intervalos fijos (`CURRENT_DATE + INTERVAL`) que puedan caer en sábado/domingo. | En clases prácticas se utiliza `CURRENT_DATE + INTERVAL '1' DAY` que, según el día de ejecución, podría coincidir en fin de semana. En exámenes se usan fechas calendario fijas. | El calendario oficial de clases y exámenes opera estrictamente de lunes a viernes lectivos. | **Requiere ajuste:** En `CalendarioSeedInitializer` y scripts dinámicos se debe emplear cálculo de día hábil garantizado. |
| **22. Añadir 3 exámenes teóricos pasados pendientes de calificar:** Con fecha reciente pasada y `es_apto = NULL`. | Existen 5 exámenes teóricos con fecha 29/10/2026 y `es_apto = NULL` (Exámenes 15, 45, 46, 47, 48). | Casuística de actas pendientes de volcado por la administración. | **Cumplido con holgura en `data.sql`** (5 registros disponibles). |
| **23. Añadir 5 exámenes prácticos pasados pendientes de calificar:** Con fecha reciente pasada y `es_apto = NULL`. | Existen 7 exámenes prácticos con fecha 29/10/2026 y `es_apto = NULL` (Exámenes 3, 10, 11, 12, 13, 54, 55). | Casuística de actas prácticas pendientes de firma por el profesor. | **Cumplido con holgura en `data.sql`** (7 registros disponibles). |
| **24. Exámenes teóricos y prácticos con fecha futura no calificables:** Añadidos para pruebas de citación futura. | Exámenes teóricos 49, 50, 51 (12 y 26 de noviembre) y prácticos 52, 53 (12 y 26 de noviembre) con `es_apto = NULL`. | Convocatorias con citación despachada pendientes de celebración. | **Cumplido al 100% en `data.sql`.** |

---

## 4. Matriz de Hallazgos y Acciones Correctoras Recomendadas

De la auditoría forense se derivan las siguientes conclusiones de calidad de datos:

1. **Corrección Aritmética de DNIs en `data.sql` (Prioridad Alta):**
   - **Diagnóstico:** Se identifican 26 DNIs con letra final arbitraria (profesores 116..120, 122..129 y alumnos 253..257, 259..266).
   - **Solución:** Sustituir las letras erróneas por el carácter oficial derivado de `número % 23` según la tabla canónica del Ministerio del Interior (`TRWAGMYFPDXBNJZSQVHLCKE`).
2. **Alineación de Alumnos Teórico-Práctica sin Solicitud (Prioridad Media):**
   - **Diagnóstico:** Actualmente hay 3 alumnos sin solicitud alguna (IDs 260, 261, 262), mientras que se requerían 5. Los IDs 258 y 259 recibieron solicitudes para pruebas futuras.
   - **Solución:** Liberar las solicitudes de los alumnos 258 y 259 para completar el cupo exacto de 5 alumnos en preparación teórica sin solicitud formalizada.
3. **Robustez de Fechas Relativas en Semilla (Prioridad Media):**
   - **Diagnóstico:** Las clases relativas con `CURRENT_DATE + INTERVAL '1' DAY` o `'3' DAY` pueden coincidir en sábado o domingo si el script se carga en viernes o jueves.
   - **Solución:** Implementar la inicialización dinámica de clases en el componente Java `CalendarioSeedInitializer`, evaluando `DayOfWeek` para asegurar siempre días hábiles de lunes a viernes.
4. **Validaciones en Controladores y Servicios (Prioridad Alta en Backend):**
   - Asegurar que el endpoint de calificación de exámenes rechace cualquier intento de calificación si `examen.fechaHora.toLocalDate().isAfter(LocalDate.now())`.
   - Asegurar que la cancelación de clases por parte del alumno impida cancelar cuando `clase.fechaHora.isBefore(LocalDateTime.now())`.

---

## 5. Censo Oficial de Profesores Registrados en el Script

A continuación se detalla la nómina completa de los 30 profesores poblados en el script de datos semilla, indicando su estado operativo (`ACTIVO` o `INACTIVO`) en la última columna:

| Profesor | Nombre y Apellidos | Nombre de Usuario | ID | Vehículo Asignado | Tipo Permiso Vehículo | Nº Alumnos Asignados | IDs Alumnos Asignados | Estado |
| :--- | :--- | :--- | :---: | :--- | :---: | :---: | :--- | :---: |
| profesor 1 | Laura Sánchez Romero | `profesor1` | 100 | 1234-LMN (SEAT Ibiza 1.0 TSI) | `PERMISO_B` | 8 | `[200, 201, 202, 203, 204, 206, 214, 226]` | `ACTIVO` |
| profesor 2 | Manuel Navarro Torres | `profesor2` | 101 | 2233-MNX (MAN TGX 18.470) | `PERMISO_C` | 1 | `[216]` | `ACTIVO` |
| profesor 3 | Carlos Ruiz Martínez | `profesor3` | 102 | 3456-JTR (Toyota Yaris 1.5 Hybrid) | `PERMISO_B` | 4 | `[209, 212, 213, 227]` | `ACTIVO` |
| profesor 4 | Elena Moreno Díaz | `profesor4` | 103 | 9012-HND (Honda CB500F (35 kW)) | `PERMISO_A2` | 2 | `[205, 229]` | `ACTIVO` |
| profesor 5 | Javier Navarro Gil | `profesor5` | 104 | 4455-IRZ (Irizar i6 / Scania K410) | `PERMISO_D` | 2 | `[218, 221]` | `ACTIVO` |
| profesor 6 | Benjamín Soto Morales | `profesor6` | 105 | Ninguno | `Ninguno` | 0 | `[]` | `INACTIVO` |
| profesor 7 | Andrés Morales Romero | `profesor7` | 106 | 6677-FRD (Ford Ranger 2.0 EcoBlue + Remolque) | `PERMISO_B_E` | 1 | `[242]` | `ACTIVO` |
| profesor 8 | Marta Romero Castro | `profesor8` | 107 | 8899-PGZ (Piaggio Zip 50 4T) | `PERMISO_AM` | 1 | `[231]` | `ACTIVO` |
| profesor 9 | Raquel Ortiz Vega | `profesor9` | 108 | 1011-HBF (Honda CB125F) | `PERMISO_A1` | 1 | `[234]` | `ACTIVO` |
| profesor 10 | Joaquín Castillo Ramos | `profesor10` | 109 | 1013-BMW (BMW R 1250 GS) | `PERMISO_A` | 1 | `[237]` | `ACTIVO` |
| profesor 11 | Vicente Gil Peña | `profesor11` | 110 | 1015-CAM (Citroën Ami Electric) | `PERMISO_B1` | 1 | `[240]` | `ACTIVO` |
| profesor 12 | Beatriz Cruz Méndez | `profesor12` | 111 | 1017-VFH (Volvo FH16 750 + Semirremolque) | `PERMISO_C_E` | 1 | `[246]` | `ACTIVO` |
| profesor 13 | Tomás Vidal Marín | `profesor13` | 112 | 1019-STR (Setra S 515 HD + Remolque) | `PERMISO_D_E` | 1 | `[249]` | `ACTIVO` |
| profesor 14 | Alberto Cano Flores | `profesor14` | 113 | 1021-JDM (John Deere 6120M con Aperos) | `PERMISO_LVA` | 1 | `[252]` | `ACTIVO` |
| profesor 15 | Fernando Méndez Blanco | `profesor15` | 114 | 2345-LKP (Renault Clio 1.0 TCe) | `PERMISO_B` | 1 | `[210]` | `ACTIVO` |
| profesor 16 | Sonia Iglesias Pastor | `profesor16` | 115 | 4567-KLL (Peugeot 208 PureTech) | `PERMISO_B` | 1 | `[222]` | `ACTIVO` |
| profesor 17 | Guillermo Pardo Navarro | `profesor17` | 116 | Ninguno | `Ninguno` | 0 | `[]` | `ACTIVO` |
| profesor 18 | Patricia Herrera Campos | `profesor18` | 117 | Ninguno | `Ninguno` | 0 | `[]` | `ACTIVO` |
| profesor 19 | Mario Vázquez Lozano | `profesor19` | 118 | Ninguno | `Ninguno` | 0 | `[]` | `ACTIVO` |
| profesor 20 | Irene Soler Fuentes | `profesor20` | 119 | Ninguno | `Ninguno` | 0 | `[]` | `ACTIVO` |
| profesor 21 | Roberto Paredes Cano | `profesor21` | 120 | Ninguno | `Ninguno` | 0 | `[]` | `ACTIVO` |
| profesor 22 | Silvia Carrasco Rivas | `profesor22` | 121 | Ninguno | `Ninguno` | 0 | `[]` | `ACTIVO` |
| profesor 23 | Álvaro Garrido Peña | `profesor23` | 122 | Ninguno | `Ninguno` | 0 | `[]` | `ACTIVO` |
| profesor 24 | Lucía Esteban Marín | `profesor24` | 123 | Ninguno | `Ninguno` | 0 | `[]` | `ACTIVO` |
| profesor 25 | Daniel Mora Beltrán | `profesor25` | 124 | Ninguno | `Ninguno` | 0 | `[]` | `ACTIVO` |
| profesor 26 | Rosa Calvo Delgado | `profesor26` | 125 | Ninguno | `Ninguno` | 0 | `[]` | `ACTIVO` |
| profesor 27 | Hugo Ibáñez Crespo | `profesor27` | 126 | Ninguno | `Ninguno` | 0 | `[]` | `ACTIVO` |
| profesor 28 | Marta Benítez Luque | `profesor28` | 127 | Ninguno | `Ninguno` | 0 | `[]` | `ACTIVO` |
| profesor 29 | Julián Pascual Roldán | `profesor29` | 128 | Ninguno | `Ninguno` | 0 | `[]` | `ACTIVO` |
| profesor 30 | Lorena Pastor Cuesta | `profesor30` | 129 | Ninguno | `Ninguno` | 0 | `[]` | `ACTIVO` |


---

## 6. Censo Oficial de Alumnos Registrados en el Script

A continuación se detalla el expediente de los 68 alumnos poblados en el script de datos semilla, indicando su estado operativo (`ACTIVO` o `INACTIVO`) en la última columna:

| Alumno | Nombre y Apellidos | Nombre de Usuario | ID | Teórico Aprobado | Práctico en Curso | Clases Prácticas Pendientes | Convocatorias Restantes | Modalidad Matrícula | Permiso Carnet | Profesor Asignado | Estado |
| :--- | :--- | :--- | :---: | :---: | :---: | :--- | :---: | :---: | :---: | :--- | :---: |
| alumno 1 | Jose López Martín | `alumno1` | 200 | Sí | Sí | 2 clases prácticas pendientes | 2 | `TEORICO_PRACTICA` | `PERMISO_B` | Laura Sánchez Romero (ID: 100) | `ACTIVO` |
| alumno 2 | David Ruiz García | `alumno2` | 201 | Sí | Sí | 1 clase práctica pendiente | 1 | `TEORICO_PRACTICA` | `PERMISO_B` | Laura Sánchez Romero (ID: 100) | `ACTIVO` |
| alumno 3 | Carmen Vega Hernández | `alumno3` | 202 | Convalidado | Sí | 1 clase práctica pendiente | 2 | `PRACTICA` | `PERMISO_B` | Laura Sánchez Romero (ID: 100) | `ACTIVO` |
| alumno 4 | Alejandro Blanco Ramos | `alumno4` | 203 | Convalidado | Sí | 1 clase práctica pendiente | 2 | `PRACTICA` | `PERMISO_B` | Laura Sánchez Romero (ID: 100) | `ACTIVO` |
| alumno 5 | Lucía Fernández Santos | `alumno5` | 204 | Sí | Sí | 1 clase práctica pendiente | 1 | `TEORICO_PRACTICA` | `PERMISO_B` | Laura Sánchez Romero (ID: 100) | `ACTIVO` |
| alumno 6 | Hugo Medina Cruz | `alumno6` | 205 | Sí | Sí | 2 clases prácticas pendientes | 1 | `TEORICO_PRACTICA` | `PERMISO_A2` | Elena Moreno Díaz (ID: 103) | `ACTIVO` |
| alumno 7 | Daniel Marín Gil | `alumno7` | 206 | Sí | Sí | 1 clase práctica pendiente | 0 | `TEORICO_PRACTICA` | `PERMISO_B` | Laura Sánchez Romero (ID: 100) | `ACTIVO` |
| alumno 8 | María Castillo Ortiz | `alumno8` | 207 | Convalidado | Sí | 0 clases prácticas pendientes | 0 | `PRACTICA` | `PERMISO_B` | Ninguno | `INACTIVO` |
| alumno 9 | Marta Sánchez Gómez | `alumno9` | 208 | Sí | Aprobado | 0 clases prácticas pendientes | 2 | `TEORICO_PRACTICA` | `PERMISO_A2` | Ninguno | `ACTIVO` |
| alumno 10 | Sergio Morales Soto | `alumno10` | 209 | Convalidado | Sí | 1 clase práctica pendiente | 1 | `PRACTICA` | `PERMISO_B` | Carlos Ruiz Martínez (ID: 102) | `ACTIVO` |
| alumno 11 | Irene Ortiz Navarro | `alumno11` | 210 | Sí | Sí | 0 clases prácticas pendientes | 0 | `TEORICO_PRACTICA` | `PERMISO_B` | Fernando Méndez Blanco (ID: 114) | `ACTIVO` |
| alumno 12 | Álvaro Delgado Ruiz | `alumno12` | 211 | No | No (Teórico pendiente) | 0 clases prácticas pendientes | 1 | `TEORICO_PRACTICA` | `PERMISO_B` | Ninguno | `ACTIVO` |
| alumno 13 | Sofía Castro Moreno | `alumno13` | 212 | Convalidado | Sí | 1 clase práctica pendiente | 1 | `PRACTICA` | `PERMISO_B` | Carlos Ruiz Martínez (ID: 102) | `ACTIVO` |
| alumno 14 | Mario Rubio Torres | `alumno14` | 213 | Sí | Sí | 1 clase práctica pendiente | 0 | `TEORICO_PRACTICA` | `PERMISO_B` | Carlos Ruiz Martínez (ID: 102) | `ACTIVO` |
| alumno 15 | Alba Marín Romero | `alumno15` | 214 | Sí | Sí | 1 clase práctica pendiente | 1 | `TEORICO_PRACTICA` | `PERMISO_B` | Laura Sánchez Romero (ID: 100) | `ACTIVO` |
| alumno 16 | Javier Sanz Díaz | `alumno16` | 215 | Sí | No | 0 clases prácticas pendientes | 0 | `TEORICO_PRACTICA` | `PERMISO_B` | Ninguno | `INACTIVO` |
| alumno 17 | Paula Iglesias Vega | `alumno17` | 216 | Convalidado | Sí | 2 clases prácticas pendientes | 2 | `PRACTICA` | `PERMISO_C` | Manuel Navarro Torres (ID: 101) | `ACTIVO` |
| alumno 18 | Marcos Medina Serrano | `alumno18` | 217 | No | No (Teórico pendiente) | 0 clases prácticas pendientes | 1 | `TEORICO_PRACTICA` | `PERMISO_C` | Ninguno | `ACTIVO` |
| alumno 19 | Elena Cortés Ramos | `alumno19` | 218 | Convalidado | Sí | 2 clases prácticas pendientes | 2 | `PRACTICA` | `PERMISO_D` | Javier Navarro Gil (ID: 104) | `ACTIVO` |
| alumno 20 | Adrián Vega Gil | `alumno20` | 219 | Sí | Aprobado | 0 clases prácticas pendientes | 0 | `TEORICO_PRACTICA` | `PERMISO_B` | Ninguno | `ACTIVO` |
| alumno 21 | Jorge Romero Blanco | `alumno21` | 220 | Sí | Aprobado | 0 clases prácticas pendientes | 0 | `TEORICO_PRACTICA` | `PERMISO_B` | Ninguno | `ACTIVO` |
| alumno 22 | Clara Gil Santos | `alumno22` | 221 | Convalidado | Sí | 0 clases prácticas pendientes | 2 | `PRACTICA` | `PERMISO_D` | Javier Navarro Gil (ID: 104) | `ACTIVO` |
| alumno 23 | Gonzalo Serrano Cruz | `alumno23` | 222 | Sí | Sí | 0 clases prácticas pendientes | 1 | `TEORICO_PRACTICA` | `PERMISO_B` | Sonia Iglesias Pastor (ID: 115) | `ACTIVO` |
| alumno 24 | Valeria Blanco Marín | `alumno24` | 223 | No | No (Teórico pendiente) | 0 clases prácticas pendientes | 2 | `TEORICO_PRACTICA` | `PERMISO_D` | Ninguno | `ACTIVO` |
| alumno 25 | Raúl Molina Ortiz | `alumno25` | 224 | No | No (Teórico pendiente) | 0 clases prácticas pendientes | 2 | `TEORICO_PRACTICA` | `PERMISO_B` | Ninguno | `ACTIVO` |
| alumno 26 | Natalia Ramos Delgado | `alumno26` | 225 | No | No (Teórico pendiente) | 0 clases prácticas pendientes | 2 | `TEORICO_PRACTICA` | `PERMISO_AM` | Ninguno | `ACTIVO` |
| alumno 27 | Lucas Navarro Castro | `alumno27` | 226 | Sí | Sí | 1 clase práctica pendiente | 0 | `TEORICO_PRACTICA` | `PERMISO_B` | Laura Sánchez Romero (ID: 100) | `ACTIVO` |
| alumno 28 | Claudia Vega Rubio | `alumno28` | 227 | Convalidado | Sí | 1 clase práctica pendiente | 2 | `PRACTICA` | `PERMISO_B` | Carlos Ruiz Martínez (ID: 102) | `ACTIVO` |
| alumno 29 | Ismael Cano Gómez | `alumno29` | 228 | No | No (Teórico pendiente) | 0 clases prácticas pendientes | 2 | `TEORICO_PRACTICA` | `PERMISO_A2` | Ninguno | `ACTIVO` |
| alumno 30 | Lorena Gil Moreno | `alumno30` | 229 | Convalidado | Sí | 0 clases prácticas pendientes | 2 | `PRACTICA` | `PERMISO_A2` | Elena Moreno Díaz (ID: 103) | `ACTIVO` |
| alumno 31 | Guillermo Díaz Castro | `alumno31` | 230 | No | No (Teórico pendiente) | 0 clases prácticas pendientes | 2 | `TEORICO_PRACTICA` | `PERMISO_AM` | Ninguno | `ACTIVO` |
| alumno 32 | Inés Romero Santos | `alumno32` | 231 | Convalidado | Sí | 0 clases prácticas pendientes | 2 | `PRACTICA` | `PERMISO_AM` | Marta Romero Castro (ID: 107) | `ACTIVO` |
| alumno 33 | Rubén Serrano Ruiz | `alumno33` | 232 | No | No (Teórico pendiente) | 0 clases prácticas pendientes | 2 | `TEORICO_PRACTICA` | `PERMISO_A1` | Ninguno | `ACTIVO` |
| alumno 34 | Silvia Molina Torres | `alumno34` | 233 | No | No (Teórico pendiente) | 0 clases prácticas pendientes | 2 | `TEORICO_PRACTICA` | `PERMISO_A1` | Ninguno | `ACTIVO` |
| alumno 35 | Héctor Ruiz Ortiz | `alumno35` | 234 | Convalidado | Sí | 0 clases prácticas pendientes | 2 | `PRACTICA` | `PERMISO_A1` | Raquel Ortiz Vega (ID: 108) | `ACTIVO` |
| alumno 36 | Marcos Ortiz Vega | `alumno36` | 235 | No | No (Teórico pendiente) | 0 clases prácticas pendientes | 2 | `TEORICO_PRACTICA` | `PERMISO_A` | Ninguno | `ACTIVO` |
| alumno 37 | Andrea Vidal Blanco | `alumno37` | 236 | No | No (Teórico pendiente) | 0 clases prácticas pendientes | 2 | `TEORICO_PRACTICA` | `PERMISO_A` | Ninguno | `ACTIVO` |
| alumno 38 | Samuel Ramos Navarro | `alumno38` | 237 | Convalidado | Sí | 0 clases prácticas pendientes | 2 | `PRACTICA` | `PERMISO_A` | Joaquín Castillo Ramos (ID: 109) | `ACTIVO` |
| alumno 39 | Natalia Castro Delgado | `alumno39` | 238 | No | No (Teórico pendiente) | 0 clases prácticas pendientes | 2 | `TEORICO_PRACTICA` | `PERMISO_B1` | Ninguno | `ACTIVO` |
| alumno 40 | Víctor Navarro Cruz | `alumno40` | 239 | No | No (Teórico pendiente) | 0 clases prácticas pendientes | 2 | `TEORICO_PRACTICA` | `PERMISO_B1` | Ninguno | `ACTIVO` |
| alumno 41 | Carla Vega Rubio | `alumno41` | 240 | Convalidado | Sí | 0 clases prácticas pendientes | 2 | `PRACTICA` | `PERMISO_B1` | Vicente Gil Peña (ID: 110) | `ACTIVO` |
| alumno 42 | Jaime Morales Soto | `alumno42` | 241 | No | No (Teórico pendiente) | 0 clases prácticas pendientes | 2 | `TEORICO_PRACTICA` | `PERMISO_B_E` | Ninguno | `ACTIVO` |
| alumno 43 | Miriam Sanz Gil | `alumno43` | 242 | Convalidado | Sí | 0 clases prácticas pendientes | 2 | `PRACTICA` | `PERMISO_B_E` | Andrés Morales Romero (ID: 106) | `ACTIVO` |
| alumno 44 | Óscar Delgado Marín | `alumno44` | 243 | No | No (Teórico pendiente) | 0 clases prácticas pendientes | 2 | `TEORICO_PRACTICA` | `PERMISO_C` | Ninguno | `ACTIVO` |
| alumno 45 | Cristina Cruz Medina | `alumno45` | 244 | No | No (Teórico pendiente) | 0 clases prácticas pendientes | 2 | `TEORICO_PRACTICA` | `PERMISO_C_E` | Ninguno | `ACTIVO` |
| alumno 46 | Pablo Benítez Ramos | `alumno46` | 245 | No | No (Teórico pendiente) | 0 clases prácticas pendientes | 2 | `TEORICO_PRACTICA` | `PERMISO_C_E` | Ninguno | `ACTIVO` |
| alumno 47 | Nuria Rubio Iglesias | `alumno47` | 246 | Convalidado | Sí | 0 clases prácticas pendientes | 2 | `PRACTICA` | `PERMISO_C_E` | Beatriz Cruz Méndez (ID: 111) | `ACTIVO` |
| alumno 48 | Emilio Castillo Romero | `alumno48` | 247 | No | No (Teórico pendiente) | 0 clases prácticas pendientes | 2 | `TEORICO_PRACTICA` | `PERMISO_D_E` | Ninguno | `ACTIVO` |
| alumno 49 | Rocío Medina López | `alumno49` | 248 | No | No (Teórico pendiente) | 0 clases prácticas pendientes | 2 | `TEORICO_PRACTICA` | `PERMISO_D_E` | Ninguno | `ACTIVO` |
| alumno 50 | Ángel Flores Sánchez | `alumno50` | 249 | Convalidado | Sí | 0 clases prácticas pendientes | 2 | `PRACTICA` | `PERMISO_D_E` | Tomás Vidal Marín (ID: 112) | `ACTIVO` |
| alumno 51 | Celia Gallego Ruiz | `alumno51` | 250 | No | No (Teórico pendiente) | 0 clases prácticas pendientes | 2 | `TEORICO_PRACTICA` | `PERMISO_LVA` | Ninguno | `ACTIVO` |
| alumno 52 | Tomás Herrera Gil | `alumno52` | 251 | No | No (Teórico pendiente) | 0 clases prácticas pendientes | 2 | `TEORICO_PRACTICA` | `PERMISO_LVA` | Ninguno | `ACTIVO` |
| alumno 53 | Marina Lozano Santos | `alumno53` | 252 | Convalidado | Sí | 0 clases prácticas pendientes | 2 | `PRACTICA` | `PERMISO_LVA` | Alberto Cano Flores (ID: 113) | `ACTIVO` |
| alumno 54 | Marcos Prieto Ramos | `alumno54` | 253 | No | No (Teórico pendiente) | 0 clases prácticas pendientes | 2 | `TEORICO_PRACTICA` | `PERMISO_B` | Ninguno | `ACTIVO` |
| alumno 55 | Irene Garrido Peña | `alumno55` | 254 | No | No (Teórico pendiente) | 0 clases prácticas pendientes | 2 | `TEORICO_PRACTICA` | `PERMISO_B` | Ninguno | `ACTIVO` |
| alumno 56 | Adrián Cabrera Soler | `alumno56` | 255 | No | No (Teórico pendiente) | 0 clases prácticas pendientes | 2 | `TEORICO_PRACTICA` | `PERMISO_A2` | Ninguno | `ACTIVO` |
| alumno 57 | Paula Castillo Mora | `alumno57` | 256 | No | No (Teórico pendiente) | 0 clases prácticas pendientes | 2 | `TEORICO_PRACTICA` | `PERMISO_C` | Ninguno | `ACTIVO` |
| alumno 58 | Hugo Pardo Rivas | `alumno58` | 257 | No | No (Teórico pendiente) | 0 clases prácticas pendientes | 2 | `TEORICO_PRACTICA` | `PERMISO_B` | Ninguno | `ACTIVO` |
| alumno 59 | Elena Bermejo Cruz | `alumno59` | 258 | No | No (Teórico pendiente) | 0 clases prácticas pendientes | 2 | `TEORICO_PRACTICA` | `PERMISO_B` | Ninguno | `ACTIVO` |
| alumno 60 | Diego Santos León | `alumno60` | 259 | No | No (Teórico pendiente) | 0 clases prácticas pendientes | 2 | `TEORICO_PRACTICA` | `PERMISO_B` | Ninguno | `ACTIVO` |
| alumno 61 | Sara Marín Esteban | `alumno61` | 260 | No | No (Teórico pendiente) | 0 clases prácticas pendientes | 2 | `TEORICO_PRACTICA` | `PERMISO_A2` | Ninguno | `ACTIVO` |
| alumno 62 | Álvaro Fuentes Moya | `alumno62` | 261 | No | No (Teórico pendiente) | 0 clases prácticas pendientes | 2 | `TEORICO_PRACTICA` | `PERMISO_D` | Ninguno | `ACTIVO` |
| alumno 63 | Marta Herrero Solís | `alumno63` | 262 | No | No (Teórico pendiente) | 0 clases prácticas pendientes | 2 | `TEORICO_PRACTICA` | `PERMISO_B` | Ninguno | `ACTIVO` |
| alumno 64 | Javier Paredes Cano | `alumno64` | 263 | Sí | Sí | 0 clases prácticas pendientes | 1 | `TEORICO_PRACTICA` | `PERMISO_B` | Ninguno | `ACTIVO` |
| alumno 65 | Lucía Giménez Vidal | `alumno65` | 264 | Sí | Sí | 0 clases prácticas pendientes | 1 | `TEORICO_PRACTICA` | `PERMISO_B` | Ninguno | `ACTIVO` |
| alumno 66 | Manuel Reyes Cortés | `alumno66` | 265 | Sí | Sí | 0 clases prácticas pendientes | 1 | `TEORICO_PRACTICA` | `PERMISO_A2` | Ninguno | `ACTIVO` |
| alumno 67 | Alba Méndez Gallego | `alumno67` | 266 | Sí | Sí | 0 clases prácticas pendientes | 1 | `TEORICO_PRACTICA` | `PERMISO_C` | Ninguno | `ACTIVO` |
| alumno 68 | Daniel Lara Montero | `alumno68` | 267 | Sí | Sí | 0 clases prácticas pendientes | 1 | `TEORICO_PRACTICA` | `PERMISO_B` | Ninguno | `ACTIVO` |

