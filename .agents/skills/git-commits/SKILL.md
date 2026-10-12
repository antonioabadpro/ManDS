---
name: git-commits
description: >-
  Estandarización de mensajes de confirmación de Git bajo la convención Conventional Commits en español.
  Usar al proponer o redactar mensajes de commit para cambios, correcciones de errores, refactorizaciones,
  pruebas automatizadas o documentación en el repositorio.
metadata:
  author: ManDS
  version: "1.0.0"
---

# Git Commits (Conventional Commits)

Esta skill define el estándar obligatorio para estructurar y redactar mensajes de confirmación de Git en el repositorio del ERP de autoescuelas, garantizando un historial trazable, legible y alineado con la especificación **Conventional Commits**.

---

## 1. Principios Fundamentales de Control de Versiones

1. **Idioma Obligatorio:** Español de España (`es-ES`) en todos los títulos, cuerpos y notas al pie de los commits.
2. **Commits Atómicos:** Cada confirmación debe representar **una única unidad lógica de cambio**. Queda desaconsejado mezclar refactorizaciones cosméticas con correcciones de negocio o nuevas funcionalidades en el mismo commit.
3. **Commits Siempre en Verde:** Queda terminantemente prohibido confirmar código que rompa la compilación o haga fallar la batería de pruebas de la funcionalidad afectada.
4. **Modo Imperativo:** El título debe redactarse como una orden o acción en infinitivo/imperativo (ej. `añadir soporte...`, `corregir cálculo...`, `refactorizar servicio...`), nunca en pasado (`añadido...`, `corregido...`).

---

## 2. Estructura Canónica del Mensaje

```text
<tipo>(<ámbito>): <descripción breve en imperativo y sin punto final>

[Cuerpo explicativo opcional pero recomendado: explica el QUÉ y el PORQUÉ técnico en 2-4 frases]

[Notas al pie opcionales: referencias a tareas, issues (#42) o BREAKING CHANGE]
```

### Reglas de Formato:
* **Línea de Asunto (Título):** Límite estricto de **72 caracteres**. Debe comenzar en minúscula tras los dos puntos y el espacio (`tipo(ámbito): texto`). No lleva punto final.
* **Línea en Blanco:** Separar obligatoriamente el título del cuerpo mediante una línea en blanco.
* **Cuerpo:** Explicar el contexto, la decisión técnica y la justificación arquitectónica del cambio.

---

## 3. Catálogo de Tipos Permitidos

| Tipo | Cuándo Utilizarlo | Ejemplo |
| :--- | :--- | :--- |
| `feat` | Nueva funcionalidad visible o servicio para el sistema. | `feat(flota): añadir modal de alta de vehículo con validación de fechas` |
| `fix` | Corrección de un fallo o bug funcional o técnico. | `fix(practicas): impedir cancelación de clases una vez iniciada la hora` |
| `refactor` | Modificación interna que no altera el comportamiento observable ni añade features. | `refactor(admin): desacoplar funciones monolíticas con Named Event Handlers` |
| `test` | Creación, adaptación o ampliación de suites de pruebas unitarias o integradas. | `test(calendario): añadir pruebas para bloqueo en festivos de Semana Santa` |
| `docs` | Modificación exclusiva de documentación (`AGENTS.md`, ADRs, Javadoc, README). | `docs(adr): registrar decisión de sustitución de FullCalendar por SSR nativo` |
| `chore` | Tareas rutinarias de configuración, dependencias (`pom.xml`), linters o build. | `chore(deps): actualizar versión de Spring Boot a 4.1.1` |
| `style` | Cambios de espaciado, formato o linting sin impacto en la lógica compilada. | `style(profesor): homogeneizar indentación y eliminación de imports no usados` |
| `perf` | Optimización de rendimiento (consultas JPQL, reducción de swaps, CWV). | `perf(vehiculo): optimizar consulta de flota evitando problema N+1 en incidencias` |

---

## 4. Taxonomía de Ámbitos del Dominio (Scope)

El ámbito (`scope`) sitúa el cambio en el módulo correspondiente del ERP:

* `auth`: Autenticación, registro de alumnos, login dual, tokens efímeros, Spring Security.
* `flota`: Gestión de vehículos, fichas mecánicas, revisiones ITV, altas y bajas lógicas.
* `incidencias`: Registro y resolución de averías, transición a `MANTENIMIENTO`.
* `profesores`: Panel docente, gestión de profesores, turnos, asignación 1-a-1 de vehículos.
* `alumnos`: Portal de estudiante, matrículas, expediente académico, balance de saldo.
* `practicas`: Clases prácticas, algoritmo de reserva, cancelaciones, fichas pedagógicas.
* `calendario`: Cuadrícula semanal SSR, franjas horarias de 45 min, control de festivos.
* `examenes`: Convocatorias DGT, solicitudes de examen, cupos y citaciones.
* `pagos`: Integración con Stripe SDK, Checkout Sessions, webhooks y verificación.
* `comun`: Componentes transversales, barra de navegación, layout base o excepciones globales.

---

## 5. Ejemplos Prácticos en el Proyecto

### 5.1. Ejemplo de Nueva Funcionalidad (`feat`)
```text
feat(calendario): incorporar bloqueo de reservas en festivos nacionales y vacaciones

Implementar FestivosCalendarioUtil con el cómputo de festivos oficiales fijos
de España y algoritmo Meeus/Butcher para festivos móviles de Semana Santa.
Las celdas de calendario se renderizan como no clicables en SSR y la capa de
servicio rechaza reservas lanzando ReglaNegocioException.
```

### 5.2. Ejemplo de Corrección de Defecto (`fix`)
```text
fix(alumnos): corregir desvinculación de profesor tutor tras calificar APTO

Al superar el examen práctico oficial, la matrícula pasa a inactiva y el
campo profesor del alumno se asigna a null para permitir la contratación
de un nuevo permiso de conducir sin dependencias residuales.
```

### 5.3. Ejemplo de Refactorización (`refactor`)
```text
refactor(auth): estandarizar el operador this en servicios y mappers

Anteponer sistemáticamente this. a los atributos de instancia en
AuthenticationService y TokenVerificacionService para prevenir shadowing
y unificar la legibilidad del código backend.
```

---

## 6. Antipatrones en Mensajes de Commit

* **Prohibido mensajes genéricos:** `fix: bug`, `cambios`, `actualización`, `wip`, `subiendo archivos`.
* **Prohibido omitir el ámbito cuando aplique:** Preferir `feat(flota): ...` sobre `feat: ...`.
* **Prohibido el inglés:** No usar `Add feature...`, `Fix bug in calendar...`.
* **Prohibido mezclar múltiples features:** Si un cambio toca la pasarela de Stripe y la cuadrícula del calendario en puntos independientes, realizar dos commits atómicos separados.
