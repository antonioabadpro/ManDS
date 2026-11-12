# ManDS - Manager Driving School
> **Programa de gestión web para la administración formativa, operativa y de cobros en autoescuelas.**
> Trabajo de Fin de Grado (TFG) presentado en la Escuela Técnica Superior de Ingeniería (ETSI) de la **Universidad de Huelva (UHU)** para la obtención del Grado en Ingeniería Informática.

---

<p align="center">
  <a href="https://mands-mu4p.onrender.com" target="_blank">
    <img src="https://img.shields.io/badge/App_en_Vivo-Render-46E3B7?style=for-the-badge&logo=render&logoColor=0f172a" alt="Ver aplicación desplegada en Render" />
  </a>
  <img src="https://img.shields.io/badge/Tests-536%20Passed%20%E2%9C%94-brightgreen?style=for-the-badge&logo=junit5&logoColor=white" alt="Batería de Pruebas: 536 tests superados" />
  <a href="https://sonarcloud.io/project/overview?id=antonioabadpro_ManDS" target="_blank">
    <img src="https://img.shields.io/badge/SonarQube_Cloud-Quality_Gate-4E9BCD?style=for-the-badge&logo=sonarcloud&logoColor=white" alt="Análisis en SonarQube Cloud" />
  </a>
  <a href="https://hub.docker.com/r/antonioabadpro/mands" target="_blank">
    <img src="https://img.shields.io/badge/Docker_Hub-mands:latest-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="Imagen Docker en Docker Hub" />
  </a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21" />
  <img src="https://img.shields.io/badge/Spring_Boot-4.1.2-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="Spring Boot 4.1.2" />
  <img src="https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL" />
  <img src="https://img.shields.io/badge/Tailwind_CSS-38B2AC?style=for-the-badge&logo=tailwind-css&logoColor=white" alt="Tailwind CSS" />
  <img src="https://img.shields.io/badge/Thymeleaf-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white" alt="Thymeleaf" />
  <img src="https://img.shields.io/badge/HTMX-3366CC?style=for-the-badge&logo=htmx&logoColor=white" alt="HTMX" />
  <img src="https://img.shields.io/badge/Brevo_REST_API-0B996F?style=for-the-badge&logo=brevo&logoColor=white" alt="Brevo REST API v3" />
</p>

> 🌐 **Aplicación desplegada en producción:** [https://mands-mu4p.onrender.com](https://mands-mu4p.onrender.com) *(haz clic en el enlace o en el badge **App en Vivo** para probarla directamente)*.

---

## 1. Descripción del Proyecto y Planteamiento del Problema

### El Problema
Las autoescuelas tradicionales suelen gestionar su trabajo diario de forma manual y desorganizada:
- **Gestión dispersa:** Los expedientes de los alumnos, las fichas de los profesores y los datos de los vehículos se suelen apuntar en papel o en hojas de cálculo separadas. Esto provoca errores al anotar datos y dificulta saber el estado real de cada alumno.
- **Reservas de clases por teléfono o mensajería:** Concertar las prácticas por teléfono o mensajes de WhatsApp genera pérdidas de tiempo, dobles reservas a la misma hora y continuas interrupciones al profesor durante su jornada.
- **Cobros a mano y falta de control:** Cobrar matrículas o bonos de clases en metálico o por transferencia obliga a revisar cuentas bancarias a mano antes de permitir al alumno empezar sus prácticas.
- **Averías comunicadas tarde:** Cuando un vehículo sufre una avería mecánica, el aviso a secretaría suele retrasarse, lo que termina provocando cancelaciones a última hora cuando el alumno ya se ha desplazado.
- **Gestión compleja de los exámenes de la DGT:** La Dirección General de Tráfico fija fechas concretas cada mes y un número máximo de plazas por permiso. Organizar estas listas a mano suele crear quejas y falta de transparencia entre los alumnos.

### La Solución: ManDS
**ManDS** (*Manager Driving School*) es un programa de gestión web diseñado para centralizar y automatizar el trabajo diario de la autoescuela en una única plataforma:
1. **Matrículas y pagos online:** Los alumnos se registran y pagan su matrícula, clases sueltas o bonos directamente con tarjeta a través de **Stripe**. Al confirmarse el pago mediante webhooks, el saldo de clases se actualiza al instante en su cuenta.
2. **Calendario semanal interactivo:** Los alumnos ven los tramos libres de su profesor en franjas de 45 minutos (en turno de mañana o tarde) y reservan su práctica con un clic gracias a **HTMX**, evitando solapamientos y protegiendo la privacidad entre compañeros.
3. **Control de vehículos y avisos por avería:** Cada profesor tiene un vehículo asignado. Si se notifica una avería, el coche pasa a estado de mantenimiento, el sistema cancela las clases afectadas de ese día y avisa automáticamente por correo electrónico a los alumnos.
4. **Gestión ordenada de exámenes DGT:** La autoescuela fija las fechas oficiales de cada mes y el número de plazas disponibles. El sistema gestiona las solicitudes de los alumnos por estricto orden de llegada, emite las citaciones y reparte las calificaciones: el administrador califica las pruebas teóricas y los profesores las prácticas.

---

## 2. Arquitectura y Stack Tecnológico

La plataforma se fundamenta en una arquitectura monolítica modular orientada a funcionalidades (**Package-by-Feature**), priorizando el principio de alta cohesión y bajo acoplamiento, la seguridad perimetral y la sencillez operativa de despliegue.

```
       ┌────────────────────────────────────────────────────────┐
       │               Cliente Web (Navegador)                  │
       │    Thymeleaf SSR + HTMX (HTML-over-the-wire) + Tailwind│
       └───────────────────────────┬────────────────────────────┘
                                   │ HTTPS / HTML Fragments
                                   ▼
       ┌────────────────────────────────────────────────────────┐
       │             Capa Web / Controladores Spring            │
       │     Spring MVC · Spring Security (RBAC) · Validation   │
       └───────────────────────────┬────────────────────────────┘
                                   │ Inyección de Dependencias
                                   ▼
       ┌────────────────────────────────────────────────────────┐
       │              Capa de Servicios y Dominio               │
       │   Package-by-Feature: academico · auth · practicas ·   │
       │   flota · examenes · pagos · usuarios · estadisticas · │
       │   core                                                 │
       └──────────────┬──────────────────────────┬──────────────┘
                      │                          │
        Transacciones │ JPA / Hibernate          │ API / Webhooks
                      ▼                          ▼
       ┌──────────────────────────┐   ┌─────────────────────────┐
       │  PostgreSQL (Supabase)   │   │     Pasarelas y Correo  │
       │  H2 Database (Pruebas)   │   │ Stripe API · Brevo API  │
       └──────────────────────────┘   └─────────────────────────┘
```

- **Backend (Java 21 + Spring Boot 4.1.2):**
  - **Spring MVC:** Enrutamiento semántico de controladores y renderizado seguro desde el servidor.
  - **Spring Security:** Autenticación unificada (username/correo electrónico), protección contra CSRF, headers de seguridad HTTP y control de acceso basado en roles (`ADMIN`, `PROFESOR`, `ALUMNO`).
  - **Spring Data JPA & Hibernate:** Mapeo objeto-relacional optimizado, bloqueos de concurrencia e índices únicos a nivel de persistencia.
  - **Bean Validation & MapStruct:** Validación declarativa de DTOs tipados con Records y transformación eficiente entre entidades y vistas.
- **Frontend (HTML-over-the-wire con Thymeleaf + HTMX + Tailwind CSS):**
  - **Enfoque HTML-over-the-wire:** Se descartan los frameworks JavaScript pesados del lado cliente (SPA) para erradicar la duplicación de modelos y la sobrecarga de dependencias. **HTMX** (auto-hospedado en local) proporciona una experiencia de usuario ultra-fluida mediante peticiones asíncronas parciales (`hx-get`, `hx-post`, `hx-target`, `hx-swap`), logrando reactividad en tiempo real sin abandonar el renderizado seguro en servidor.
  - **Tailwind CSS:** Compilado estáticamente con CLI a `tailwind.min.css` junto con la tipografía corporativa **Inter** en local (sin CDNs externos). Diseño totalmente adaptativo (*mobile-first*), garantizando coherencia visual en smartphones (<640px), tablets (768px) y estaciones de escritorio (≥1024px) con soporte nativo de modo oscuro (`dark:`).
  - **JavaScript No Intrusivo:** Cumplimiento estricto de directivas CSP (sin JS inline). Scripts modulares en `src/main/resources/static/js/` asociados mediante eventos del DOM y atributos semánticos `data-*`.
- **Persistencia y Datos:**
  - **PostgreSQL:** Base de datos relacional principal alojada en Supabase (producción) o en contenedor Docker local, configurada con índices de concurrencia y restricciones relacionales.
  - **H2 Database:** Base de datos en memoria para la ejecución ultra-rápida, determinista y aislada de la batería de pruebas automatizadas.
- **Integraciones Clave:**
  - **Stripe:** Pasarela de pagos con Stripe Checkout para la compra segura de bonos, clases y pagos de matrícula. Incluye conciliación asíncrona mediante webhooks con verificación criptográfica de firmas para prevenir ataques de repetición.
  - **Mensajería Transaccional Segregada (Brevo REST API v3 / Mailpit SMTP):** Despacho asíncrono gestionado por `EmailAsyncDispatcher`:
    - **Producción (`deploy` en Render):** Envío exclusivo mediante la **REST API v3 de Brevo** sobre HTTPS (puerto 443) usando `HttpClient` nativo, evitando los bloqueos del tráfico SMTP saliente en proveedores cloud.
    - **Desarrollo Local y Docker (`docker`):** Envío mediante `JavaMailSender` sobre protocolo SMTP hacia **Mailpit** (puerto `1025`) para interceptar y validar visualmente todos los correos en su interfaz web (puerto `8025`) sin consumir cuota.
- **Arquitectura Package-by-Feature:** Organización modular del código en paquetes según el dominio de negocio:
  - `com.autoescuela.erp.academico`: Ciclo de vida de matrículas, modalidades y saldo formativo.
  - `com.autoescuela.erp.auth`: Autenticación, tokens efímeros, activación de cuentas y recuperación de accesos.
  - `com.autoescuela.erp.core`: Configuración transversal, seguridad perimetral, mensajería, excepciones de negocio y utilidades de calendario.
  - `com.autoescuela.erp.estadisticas`: Cuadros de mando, métricas de rendimiento, tasas de aprobados y desglose financiero por rol.
  - `com.autoescuela.erp.examenes`: Solicitudes, cupos y convocatorias oficiales de la DGT.
  - `com.autoescuela.erp.flota`: Gestión integral del parque móvil, mantenimientos e incidencias de averías.
  - `com.autoescuela.erp.pagos`: Servicios de pago Stripe, checkout y escucha de webhooks.
  - `com.autoescuela.erp.practicas`: Calendarios nativos, algoritmo de capacidad y clases de conducción.
  - `com.autoescuela.erp.usuarios`: Modelado de perfiles, habilitación de permisos y expedientes de alumnos.

---

## 3. Matriz de Roles y Funcionalidades Principales

El sistema implementa un estricto modelo de Control de Acceso Basado en Roles (RBAC). A continuación se resume la matriz de capacidades según el perfil autenticado:

| Funcionalidad / Dominio | Administrador | Profesor | Alumno |
| :--- | :---: | :---: | :---: |
| Panel de Mando y Analítica Global del Centro | ✔️ | ❌ | ❌ |
| Gestión y Alta de Profesores (Tokens de 60 min) | ✔️ | ❌ | ❌ |
| Asignación de Turnos Laborales y Permisos Habilitados | ✔️ | ❌ | ❌ |
| Control y Soft Delete de Expedientes de Alumnos | ✔️ | ❌ | ❌ |
| Alta, Baja y Asignación 1-a-1 de Vehículos de Flota | ✔️ | ❌ | ❌ |
| Auditoría y Resolución Técnica de Incidencias de Flota | ✔️ | ❌ | ❌ |
| Fijación de Convocatorias Oficiales DGT (2 al mes) y Cupos | ✔️ | ❌ | ❌ |
| Tramitación FIFO de Solicitudes de Examen DGT | ✔️ | ❌ | ❌ |
| Calificación de Exámenes Teóricos DGT | ✔️ | ❌ | ❌ |
| Reporte de Incidencias Mecánicas en Vehículo Asignado | ❌ | ✔️ | ❌ |
| Calificación de Exámenes Prácticos DGT | ❌ | ✔️ | ❌ |
| Cuadrícula de Calendario y Firma de Fichas de Prácticas | ❌ | ✔️ | ❌ |
| Cancelación Preventiva de Jornadas de Clases | ❌ | ✔️ | ❌ |
| Reserva de Clases Prácticas en Franjas de 45 minutos | ❌ | ❌ | ✔️ |
| Cancelación de Clases Prácticas Propias No Iniciadas | ❌ | ❌ | ✔️ |
| Compra de Clases, Bonos y Renovaciones con Stripe | ❌ | ❌ | ✔️ |
| Tramitación de Solicitud de Examen Teórico/Práctico | ❌ | ❌ | ✔️ |
| Acceso a Cuestionarios y Tests Teóricos | ❌ | ❌ | ✔️ |

### Desglose de Funcionalidades por Barra Lateral (Side-bar)

#### 🛡️ Rol: Administrador
1. **Dashboard General:** Monitoriza en tiempo real los indicadores clave del negocio, volumen de matriculaciones, ingresos acumulados y resumen de clases y exámenes programados. Ofrece una vista panorámica del rendimiento diario de la autoescuela con accesos directos a las operaciones más críticas.
2. **Gestión de Profesores:** Permite dar de alta a nuevos profesores generando invitaciones con tokens efímeros, asignar turnos y habilitaciones de permisos, y realizar bajas lógicas (soft delete). Centraliza el registro de profesores preservando en todo momento su histórico de clases previo.
3. **Gestión de Alumnos:** Administra los expedientes académicos, vinculación de profesores asignados, estado de matrícula y control de convocatorias oficiales DGT restantes. Permite consultar el detalle formativo completo de cada aspirante y ejecutar bajas lógicas o reasignaciones según la carga de trabajo.
4. **Gestión de Flota:** Supervisa el parque de vehículos de la autoescuela clasificándolos por tipo de permiso, combustible, tipo de transmisión y estado operativo. Facilita la asignación exclusiva 1-a-1 a profesores habilitados y el control de revisiones periódicas e inspecciones técnicas.
5. **Incidencias de Flota:** Recibe y audita las averías mecánicas reportadas por los profesores, gestionando la transición de estados desde su apertura hasta su resolución técnica en taller. Al pasar una avería a gestión activa, bloquea preventivamente el vehículo en mantenimiento cancelando las clases afectadas y notificando a los alumnos.
6. **Solicitudes de Examen DGT:** Tramita las peticiones de examen de los alumnos mediante un estricto orden de prelación temporal (FIFO) y comprobación de cupos máximos por permiso. Permite aceptar o rechazar solicitudes con justificación motivada obligatoria, despachando citaciones y bloqueando la jornada oficial del vehículo.
7. **Exámenes y Calificaciones:** Publica las dos fechas oficiales mensuales fijadas para las pruebas teóricas y prácticas respetando el calendario laboral y los cupos disponibles. Permite al administrador calificar exclusivamente las actas de examen teórico (Apto / No Apto) actualizando de inmediato el expediente del alumno.
8. **Historial de Prácticas:** Proporciona un registro centralizado y auditable de todas las clases prácticas impartidas, pendientes y canceladas por profesores y alumnos en el sistema. Supervisa la progresión de kilometrajes de los vehículos, motivos de cancelación y cumplimiento de las franjas horarias reglamentarias.
9. **Estadísticas Globales:** Presenta cuadros de mando avanzados con métricas de tasa de aprobados a la primera, distribución de alumnos por permiso y rentabilidad económica de la autoescuela. Analiza el volumen de actividad de las clases y la progresión de los alumnos para la toma de decisiones.

#### 🚗 Rol: Profesor
1. **Dashboard Principal:** Ofrece una visión sintetizada del día a día del profesor, destacando las clases prácticas fijadas para la jornada, el vehículo asignado y las notificaciones recientes. Brinda accesos directos al calendario lectivo y a la relación de alumnos a su cargo.
2. **Mi Calendario:** Despliega la cuadrícula semanal interactiva estructurada en 9 franjas de 45 minutos adaptadas a su turno de trabajo asignado (Matinal o Tarde). Permite consultar las reservas de sus alumnos, registrar las fichas de clases impartidas y ejecutar la cancelación preventiva de jornadas completas con notificación por email.
3. **Mis Alumnos:** Muestra la lista de aspirantes asignados a su tutela, permitiendo inspeccionar su historial de clases prácticas recibidas y nivel de destreza al volante. Facilita el seguimiento personalizado de cada estudiante para determinar si cumple los requisitos para presentarse a examen.
4. **Exámenes y Calificaciones:** Permite al profesor consultar las actas de citación oficiales para las pruebas prácticas de circulación en vía pública fijadas por la DGT. Otorga la potestad reglamentaria de evaluar y calificar como Apto o No Apto a los alumnos examinados en su vehículo, desencadenando el cierre del expediente o la deducción de convocatorias.
5. **Mi Vehículo e Incidencias:** Proporciona los datos técnicos del coche asignado (matrícula, odómetro actual, combustible y última revisión) y habilita el formulario de notificación rápida de averías mecánicas. Ofrece un historial directo de las incidencias reportadas y el estado de reparación de las mismas.
6. **Mis Estadísticas:** Detalla indicadores personales sobre sus clases impartidas, porcentaje de alumnos aprobados y clases canceladas en el periodo. Permite evaluar su productividad laboral y planificar su disponibilidad formativa.

#### 🎓 Rol: Alumno
1. **Mi Panel:** Resume el estado actual de su formación vial, indicando su modalidad de matrícula, convocatorias oficiales disponibles, saldo de clases prácticas y profesor asignado. Incluye accesos directos a las acciones inmediatas que puede realizar según su avance curricular.
2. **Test Teóricos:** Proporciona acceso interactivo al portal de cuestionarios y baterías de preguntas para preparar la prueba de conocimientos viales de la DGT. Esta sección permanece disponible de manera exclusiva para alumnos matriculados en modalidad teórico-práctica que aún no hayan obtenido el apto teórico.
3. **Reservar Clases:** Cuadrícula semanal que expone en tiempo real los tramos libres de su profesor asignado, permitiendo reservar clases de 45 minutos con deducción de su saldo disponible. Los huecos ocupados por otros compañeros se anonimizan para garantizar la privacidad y se impiden reservas concurrentes o en días no lectivos.
4. **Historial de Clases:** Registro cronológico de todas las prácticas de circulación recibidas, pendientes de realizar o canceladas por el alumno o por el centro. Permite cancelar clases pendientes con antelación suficiente y comprobar las observaciones pedagógicas introducidas por el profesor tras cada sesión.
5. **Solicitud y Convocatorias:** Tramita la inscripción oficial a las convocatorias de examen teórico o práctico publicadas por la autoescuela cuando el alumno cumple los requisitos previos pertinentes. Permite consultar el estado de tramitación de la solicitud (Pendiente, Aceptada o Rechazada) y la fecha/hora exacta de citación oficial.
6. **Mis Calificaciones:** Consulta las actas de resultados de las pruebas oficiales DGT (Apto o No Apto) y las convocatorias consumidas a lo largo de su expediente. En caso de superar la prueba práctica definitiva, certifica la culminación exitosa del permiso de conducción.
7. **Comprar Saldo / Matrícula:** Tienda virtual conectada con Stripe Checkout para la adquisición de clases sueltas, bonos con descuento (10, 15 o 20 clases) o el abono de la tasa de renovación de matrícula tras agotar convocatorias. Actualiza de forma atómica e instantánea el saldo disponible en el expediente una vez confirmado el pago seguro.

---

### Seguridad, Registro y Ciclo de Vida de Identidades

- **Alta de Profesores Mediante Tokens Efímeros:** El alta de un profesor está estrictamente restringida al Administrador. El sistema genera un registro provisional y despacha por correo un enlace seguro provisto de un `TokenVerificacion` criptográfico de un solo uso con caducidad estricta de **60 minutos**. El profesor accede al enlace, define su contraseña cifrada mediante BCrypt y activa su perfil de manera autónoma.
- **Recuperación Segura de Contraseñas:** El flujo de restablecimiento de credenciales emite un token criptográfico efímero con una validez estricta de **15 minutos** gestionado por `TokenVerificacionService`. Transcurrido ese plazo, el token queda invalidado automáticamente para prevenir ataques de secuestro de sesión.
- **Ciclo de Vida del Alumno y Purga de Registros:** El alumno realiza un prerregistro público en la plataforma quedando en estado `INACTIVO`. Su cuenta pasa automáticamente a `ACTIVO` y formaliza su matrícula en base de datos únicamente tras la confirmación exitosa del pago en **Stripe Checkout** a través del webhook correspondiente. Asimismo, el sistema implementa una purga automática de prerregistros huérfanos que hayan sido cancelados o abandonados sin formalización económica.

---

## 4. Versiones Utilizadas

| Componente / Herramienta | Versión Oficial | Propósito en el Proyecto |
| :--- | :--- | :--- |
| **Java JDK** | `21 (LTS)` | Entorno de ejecución estándar y lenguaje base (Records, Pattern Matching) |
| **Apache Maven** | `v4.0.0` (Wrapper 3.9+) | Gestor de ciclo de vida del proyecto, dependencias y empaquetado |
| **Spring Boot** | `v4.1.2` | Framework empresarial base (Spring MVC, Security, Data JPA) |
| **Stripe Java SDK** | `v33.4.2` | Integración de sesiones Checkout y verificación criptográfica de Webhooks |
| **MapStruct** | `v1.6.3` | Mapeo tipado en tiempo de compilación entre entidades y DTOs |
| **Lombok MapStruct Binding**| `v0.2.0` | Generación de código boilerplate y compatibilidad con MapStruct |
| **Tailwind CSS / HTMX** | `v3.4.17` / `v1.9.12` | Compilación estática de estilos y actualización parcial HTML-over-the-wire |
| **JaCoCo** | `v0.8.16` | Cobertura de código integrada en Maven (`verify`) y SonarQube Cloud |
| **Docker Desktop** | `v4.97.0` | Orquestación de contenedores locales (PostgreSQL 16 + Mailpit + ManDS App) |

---

## 5. Guía de Ejecución en Docker (Entorno Local)

No es necesario clonar el repositorio ni instalar Java o Maven para probar la aplicación completa en tu equipo. El archivo [`docker-compose.yaml`](docker-compose.yaml) orquesta automáticamente los **3 contenedores** del sistema (`postgres_tfg`, `mailpit_tfg` y `mands_app`) descargando la imagen oficial precompilada `antonioabadpro/mands:latest` desde Docker Hub.

### Opción A: Ejecución rápida solo con Docker (Sin clonar el repositorio)

1. **Descargar únicamente el fichero `docker-compose.yaml`** en una carpeta vacía:
   ```bash
   curl -O https://raw.githubusercontent.com/antonioabadpro/ManDS/main/docker-compose.yaml
   ```
   *(También puedes descargarlo directamente desde GitHub mediante "Download raw file").*

2. **Levantar los 3 contenedores en segundo plano:**
   ```bash
   docker compose up -d
   ```
   > **Nota sobre variables de entorno:** El archivo `.env` es opcional. Si no está presente, la aplicación arranca automáticamente con la configuración predeterminada del perfil `docker` (PostgreSQL + Mailpit) y opera las pasarelas de Stripe en modo simulación. Si deseas probar pagos reales en modo test de Stripe, crea un `.env` junto al `docker-compose.yaml` con `STRIPE_PUBLIC_KEY`, `STRIPE_SECRET_KEY` y `STRIPE_WEBHOOK_SECRET`.

### Opción B: Para desarrollo (Clonando el repositorio)

Si deseas modificar el código fuente y ejecutarlo o reconstruir la imagen localmente:
```bash
git clone https://github.com/antonioabadpro/ManDS.git
cd ManDS
docker compose up -d --build
```
O bien, levantando únicamente la infraestructura auxiliar (`postgres` y `mailpit`) y ejecutando Spring Boot con el Maven Wrapper:
```bash
docker compose up -d postgres mailpit
./mvnw spring-boot:run -Dspring-boot.run.profiles=docker
```

#### Puntos de Acceso a los Servicios Locales:
- 🌐 **Aplicación Web (ManDS):** [http://localhost:8080](http://localhost:8080)
- 📬 **Buzón de Correo Virtual (Mailpit - SMTP en puerto 1025):** [http://localhost:8025](http://localhost:8025)
- 🐘 **Base de Datos PostgreSQL (Docker):** `localhost:5432` (BD: `tfg_db`)
- 🗃️ **Consola H2 en memoria *(solo si ejecutas con perfil `h2`)*:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)

---

## 6. Datos para Realizar las Pruebas

Para conocer en detalle el censo precargado de usuarios (administrador, profesores y alumnos), vehículos clasificados por permiso de conducción, contraseñas de desarrollo e invariantes de negocio contempladas en la base de datos, consulta el documento técnico de auditoría:

📄 **Consultar Especificación de Datos y Auditoría Semilla:** [`docs/Reglas_script.md`](docs/Reglas_script.md)

En dicho documento encontrarás:
- Censo tabulado de los 30 profesores iniciales (IDs `100` a `129`) con turnos de trabajo y permisos acreditados.
- Censo tabulado de los 68 alumnos de prueba (IDs `200` a `267`) con sus respectivas modalidades y estados.
- Credenciales predeterminadas para iniciar sesión con cualquier rol.
- Mapeo y cobertura de las normativas de la DGT implementadas en el sistema.

---

## 7. Tests Unitarios y de Integración

El proyecto incorpora una exhaustiva batería de pruebas automatizadas que validan cada servicio, controlador, flujo transaccional y restricción de dominio bajo **JUnit 5**, **Mockito** y **MockMvc**.

### Comando de Ejecución:
```bash
./mvnw clean test
```

### Resultados de la Batería de Pruebas:
> 🟢 **536 tests ejecutados con un 100% de éxito (0 fallos, 0 errores, 0 omitidos)**

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.autoescuela.erp.academico.AcademicoServiceTest
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
...
[INFO] Running com.autoescuela.erp.usuarios.VistaProfesorTest
[INFO] Tests run: 30, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 536, Failures: 0, Errors: 0, Skipped: 0
[INFO] -------------------------------------------------------
[INFO] BUILD SUCCESS
```

Las pruebas cubren de forma minuciosa:
- Deducción atómica y cálculo de capacidad de reserva de clases.
- Algoritmo de validación de letra de DNI/NIE mediante Módulo 23.
- Control de concurrencia y prevención de solapamientos horarios.
- Conciliación y procesamiento idempotente de eventos de pago de Stripe.
- Restricciones de días festivos y periodos vacacionales en el calendario laboral.
- Control de acceso RBAC y protección de endpoints por rol.

---

## 8. Despliegue en Vivo y Pipeline CI/CD

### 🌐 Entorno de Producción en Vivo
La aplicación se encuentra desplegada y accesible públicamente en **Render**:
- **URL de Acceso:** [https://mands-mu4p.onrender.com](https://mands-mu4p.onrender.com)
- **Topología Cloud:** Contenedor Docker (JRE 21 Alpine optimizado para 512 MB de RAM con perfil `deploy`), base de datos **PostgreSQL** gestionada en **Supabase** (Connection Pooler IPv4 con SSL), pasarela de pagos **Stripe Checkout** y correo transaccional mediante **Brevo REST API v3** sobre HTTPS (puerto 443).
- *(Nota: Al ejecutarse en el plan gratuito de Render, el servicio entra en reposo tras un periodo de inactividad. Para evitar suspensiones por inactividad, utilizamos UptimeRobot para hacer peticiones a /ping cada 5min).*

### ⚙️ Automatización CI/CD (GitHub Actions)
El repositorio cuenta con un pipeline unificado en [`.github/workflows/ci_cd.yaml`](.github/workflows/ci_cd.yaml) dividido en dos fases encadenadas:
1. **Integración Continua y Calidad (`build`):**
   - En cada `push` a `dev` ejecuta `mvn -B clean verify`, validando compilación, la batería completa de **536 tests** y el reporte de cobertura con **JaCoCo** (`0.8.16`).
   - En `push` a `main` y en *Pull Requests* hacia `main` ejecuta además el análisis estático de código, cobertura y seguridad en **SonarQube Cloud** (`antonioabadpro_ManDS`).
2. **Entrega Continua (`docker-publish`):**
   - Condicionado al éxito previo de `build` (`needs: build`) y restringido exclusivamente a `push` en `main`.
   - Construye la imagen **multi-stage** definida en [`Dockerfile`](Dockerfile) (compilación estática de **Tailwind CSS** en Node 22 Alpine → empaquetado Maven → imagen final ligera `eclipse-temurin:21-jre-alpine` con usuario no privilegiado) para `linux/amd64` y la publica automáticamente en Docker Hub como `antonioabadpro/mands:latest`.

---

## 9. Estructura del Repositorio

```
├── .github/workflows/        # Pipeline CI/CD (Maven, JaCoCo, SonarQube Cloud y Docker Hub)
├── Diagramas/                # Diagramas PlantUML (ER, CU, Componentes, Despliegue, Estados y Secuencia)
├── docs/                     # Especificación de arquitectura, ADRs y auditoría de datos semilla
├── src/
│   ├── main/
│   │   ├── java/com/autoescuela/erp/
│   │   │   ├── academico/    # Módulo de matrículas y compras
│   │   │   ├── auth/         # Autenticación, tokens y registro
│   │   │   ├── core/         # Configuración global, seguridad, mensajería y calendario
│   │   │   ├── estadisticas/ # Cuadros de mando y analítica agregada por rol
│   │   │   ├── examenes/     # Solicitudes y cupos DGT
│   │   │   ├── flota/        # Gestión de vehículos e incidencias
│   │   │   ├── pagos/        # Integración con Stripe (checkout y webhooks)
│   │   │   ├── practicas/    # Calendario y reservas de clases
│   │   │   └── usuarios/     # Entidades y flujos de Alumno, Profesor y Admin
│   │   └── resources/
│   │       ├── static/       # CSS compilado (Tailwind), fuentes Inter, imágenes y JS por vista
│   │       └── templates/    # Vistas y fragmentos Thymeleaf / HTMX por rol
│   └── test/                 # Batería de pruebas unitarias y de integración (536 tests)
├── Dockerfile                # Build multi-stage (Node 22 + Maven/JDK 21 + JRE 21 Alpine)
├── docker-compose.yaml       # Orquestación local (PostgreSQL 16, Mailpit y ManDS App)
├── package.json              # Scripts de compilación Tailwind CSS y activos frontend locales
├── tailwind.config.js        # Configuración corporativa de estilos Tailwind
└── pom.xml                   # Configuración Maven, dependencias, JaCoCo y SonarQube Cloud
```

---

## 10. 📑 Documentación y Memoria Técnica del Proyecto

El desarrollo del proyecto está acompañado por un compendio de documentación arquitectónica y de diseño que fundamenta cada decisión técnica adoptada:

- 📐 **Especificación de Arquitectura:** [`docs/ARQUITECTURA.md`](docs/ARQUITECTURA.md) — Detalle del stack, entidades del modelo relacional y principios de diseño modular.
- 📜 **Histórico de Decisiones de Arquitectura (ADR):** [`docs/REGISTRO_DECISIONES.md`](docs/REGISTRO_DECISIONES.md) — Registro cronológico inmutable de hitos, elecciones tecnológicas y refactorizaciones.
- 📋 **Auditoría de Reglas y Semilla:** [`docs/Reglas_script.md`](docs/Reglas_script.md) — Mapeo forense de datos iniciales y reglas de negocio.
- 📊 **Diagramas Oficiales del Sistema:**
  - [Diagrama Entidad-Relación (PlantUML)](Diagramas/Diagrama_ER.puml)
  - [Diagrama de Casos de Uso (PlantUML)](Diagramas/Diagrama_CU.puml)
  - [Diagrama de Componentes (PlantUML)](Diagramas/Diagrama_Componentes.puml)
  - [Diagrama de Despliegue (PlantUML)](Diagramas/Diagrama_Despliegue.puml)
  - [Diagrama de Estados del Dominio (PlantUML)](Diagramas/Diagrama_Estados_Dominio.puml)

[![Memoria Técnica](https://img.shields.io/badge/PDF-Ver_Memoria_Técnica-EC1C24?style=for-the-badge&logo=adobeacrobatreader&logoColor=white)](https://github.com/antonioabadpro/ManDS/docs/Memoria_ManDS.pdf)

> **Nota:** Haz clic en el botón de arriba para visualizar o descargar la memoria técnica completa en PDF.

---

## 11. 👤 Autor

**Antonio Abad Hernández Gálvez** <br>
*Proyecto realizado para la Universidad de Huelva (UHU)*
<div align="left">

[![Sitio Web](https://img.shields.io/badge/SITIO_WEB-EN_VIVO-8820C7?style=for-the-badge&logo=render&logoColor=white)](https://antonioabadpro.netlify.app/)
[![GitHub](https://img.shields.io/badge/GITHUB-ANTONIOABADPRO-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/antonioabadpro)

</div>

---

## 12. ©️ Licencia y Derechos de Autor

© 2026. Antonio Abad Hernández Gálvez.
Todos los derechos reservados.

Este proyecto es propiedad intelectual de su autor.
El código se proporciona únicamente con fines de consulta y demostración de portfolio.

El código fuente de este proyecto es propiedad exclusiva de su autor. Se permite su visualización con fines educativos y de evaluación académica.

⛔ **Prohibido su uso:** Queda estrictamente prohibida su reproducción total o parcial, modificación, distribución o uso para fines comerciales o académicos por parte de terceros sin la autorización expresa y por escrito del autor.

Este proyecto es el resultado de una evaluación académica para la Universidad de Huelva.
El plagio o uso indebido de este código en otros proyectos académicos será reportado.
