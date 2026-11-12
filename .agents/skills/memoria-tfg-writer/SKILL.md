---
name: memoria-tfg-writer
description: >-
  Redacción técnica y académica de apartados de la memoria del Trabajo Fin de Grado ManDS
  (ETSI - Universidad de Huelva) siguiendo la voz editorial del autor, la estructura de la
  plantilla del tutor, la trazabilidad real con el código fuente y el presupuesto estricto de páginas A4.
  Usar al redactar, refactorizar o revisar capítulos (Implementación, Validación, Planificación,
  Estimación de costes, Conclusiones) o Anexos de la memoria del TFG.
metadata:
  author: ManDS
  version: "1.0.0"
---

# Memoria TFG Writer (Estilo Editorial y Precisión Técnica ManDS)

Esta skill codifica el estilo de redacción detectado en la memoria oficial de ManDS (ETSI, Universidad de Huelva), las restricciones de verificabilidad frente al código fuente y los criterios de maquetación profesional A4 para mantener uniformidad editorial en toda la obra.

---

## 1. Voz Editorial y Patrón Retórico del Autor

1. **Idioma y Registro:**
   * Redactar siempre en **español de España (`es-ES`)**, en tono técnico-académico formal, directo y orientado a decisiones de ingeniería (evitar retórica vacía o adjetivación superflua).
2. **Persona Gramatical:**
   * Emplear **primera persona del plural** para justificar elecciones de arquitectura, herramientas o descartes (*«hemos optado por...»*, *«estudiamos la posibilidad de...»*, *«finalmente nos decantamos por...»*, *«hemos descartado...»*, *«garantizando así...»*).
   * Alternar de forma natural con **tercera persona descriptiva** centrada en la plataforma o componentes cuando se explica su comportamiento (*«ManDS articula...»*, *«El sistema delega...»*, *«Esta capa centraliza...»*).
3. **Estructura Argumentativa en Tres Tiempos:**
   * **Apertura causal:** Iniciar cada apartado con 1–2 frases que establezcan el objetivo de ingeniería (seguridad, concurrencia, mantenibilidad, rendimiento o trazabilidad).
   * **Contraste comparativo:** Justificar las decisiones frente a su alternativa descartada mediante conectores característicos (*«Frente a... que conlleva...»*, *«En lugar de optar por...»*, *«A diferencia de...»*).
   * **Desglose estructurado:** Detallar los mecanismos técnicos implementados vinculando siempre la pieza de código con el problema de negocio o atributo de calidad que resuelve.

---

## 2. Convenciones Visuales, Tipográficas y de Maquetación

* **Marcadores jerárquicos coherentes con la memoria:**
  * Bloques temáticos principales con cabecera en estrella y negrita: **`★ Título del bloque:`** (o **`★ Título del bloque`**).
  * Ítems descriptivos de primer nivel con **`● Concepto clave:`** seguido de explicación causal, y segundo nivel con **`○ Subconcepto:`**.
* **Tablas de síntesis (`Tabla X.Y.`):** Condesar matrices comparativas, catálogos de patrones o listados técnicos extensos en tablas compactas numeradas secuencialmente dentro del capítulo (`Tabla 4.13`, `Tabla 4.14`, etc.) para reducir consumo de páginas sin perder rigor.
* **Convenciones tipográficas:**
  * Desplegar siglas en su primera aparición del apartado seguidas del término entre paréntesis o viceversa — p. ej. *RBAC (Role-Based Access Control)*, *CSRF (Cross-Site Request Forgery)*, *MCP (Model Context Protocol)*.
  * Emplear comillas latinas (`«...»`) para estados del dominio o literales funcionales (*«PENDIENTE»*, *«Horario Ocupado»*, *«In review»*) y formato código para rutas, propiedades, comandos y nombres exactos de clases o anotaciones (`@Transactional`, `@EntityGraph`, `BaseIntegrationTest`).
* **Presupuesto de páginas en formato A4 profesional:**
  * Estimar **~380–450 palabras por página** en prosa estructurada con viñetas (`★`/`●`) y **~0,4–0,7 páginas** por tabla compacta de 8–10 filas.
  * Prohibido insertar volcados extensos de código fuente Java en el cuerpo principal de la memoria; sintetizar contratos e interacciones y derivar scripts o guías operativas detalladas al *Anexo II*.

---

## 3. Reglas de Veracidad Técnica y Fronteras entre Capítulos

1. **Verificabilidad Estricta con el Repositorio:**
   * Cada clase, paquete, anotación, versión (`pom.xml`, `package.json`), perfil de configuración (`application*.properties`), archivo Docker (`Dockerfile`, `docker-compose.yaml`), flujo Git o servidor MCP mencionado debe haber sido comprobado directamente en el código del proyecto y en `docs/REGISTRO_DECISIONES.md`.
   * Nunca inventar pipelines de CI/CD inexistentes (por ejemplo, si no existe `.github/workflows`, documentar con honestidad la estrategia manual controlada por ramas `main`/`dev` y *Release Candidates*), ni inflar métricas o resultados de pruebas no ejecutados ni verificados.
2. **Prohibiciones Léxicas de Dominio (`AGENTS.md`):**
   * Queda **prohibido** utilizar los términos *«profesor tutor»* o *«docente»* (emplear siempre *«profesor»* o *«profesorado»*).
   * No exponer numeraciones internas de reglas de negocio fuera de las tablas del *Anexo IV*; describir semánticamente cada restricción o remitir al identificador oficial de caso de uso (`CU-XXX`) o requisito (`RF-XXX`, `RNF-XXX`) definido en el apartado `4.2`.
3. **Fronteras Editoriales para Evitar Redundancias:**
   * **Apartado 4.3 (Arquitectura):** Estilo Monolito Modular frente a microservicios, MPA frente a SPA, estructura *Package-by-Feature*, principio *HTML-over-the-wire* y topología Cloud/Docker.
   * **Apartado 4.4 (Diseño Detallado):** Modelo E/R, transformación relacional, 3FN, contratos DTO (`record` + MapStruct), diagramas de secuencia, máquinas de estado y ergonomía de calendario.
   * **Apartado 4.5 (Implementación y Aspectos Técnicos):** Materialización técnica concreta: stack y herramientas (`4.5.1`), patrones GoF/PoEAA aplicados en el código (`4.5.2`), seguridad y control de acceso en Spring Security (`4.5.3`), flujo de desarrollo asistido por IA con *skills* y MCP (`4.5.4`), optimizaciones de persistencia/caché/JVM (`4.5.5`) y ciclo de pruebas/construcción/despliegue/Git (`4.5.6`), **enlazando sin reexplicar** los fundamentos ya cubiertos en `4.3` y `4.4`.
   * **Capítulo 5 (Validación y Evaluación):** Baterías de pruebas ejecutadas, casos de prueba, cobertura, métricas de rendimiento y resultados empíricos. En `4.5` solo se describe el andamiaje técnico de pruebas como puente hacia el capítulo `5`.

---

## 4. Lista de Verificación Pre-Entrega

Antes de entregar cualquier bloque redactado para la memoria, comprobar:
1. ¿Mantiene la primera persona del plural (*«hemos optado»*, *«decantamos»*) y los marcadores `★` / `●`?
2. ¿Cada nombre de clase, archivo, propiedad, versión o herramienta coincide literalmente con el proyecto actual?
3. ¿Respeta la prohibición de *«docente»* / *«profesor tutor»* y evita repetir capítulos anteriores (`4.3`, `4.4`)?
4. ¿Cumple el límite máximo de páginas A4 asignado por la plantilla para ese capítulo o subapartado?
5. ¿Señala explícitamente cualquier incoherencia detectada en secciones previas del manuscrito para que el autor pueda unificarlas?
6. ¿Queda claro el contenido que se ha expresado en el texto incluso para algún ingeniero que no ha trabajado en el proyecto?
