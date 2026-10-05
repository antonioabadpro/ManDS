# Arquitectura y Especificación Técnica del Sistema ERP para Autoescuelas (TFG)

> Documento de referencia arquitectónica, estructura de paquetes y modelo conceptual del sistema.

---

# 1. Stack Tecnológico y Dependencias

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

# 2. Estructura del Proyecto (Package by Features)

La aplicación sigue el patrón **Package by Features** con subcapas internas (`controller`, `dto`, `mapper`, `model`, `repository`, `service`):

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

# 3. Modelado Conceptual y Diagramas PlantUML

Los diagramas oficiales de arquitectura y modelado conceptual residen en sus archivos `.puml` dedicados:
- **Modelo Entidad-Relación (ER):** [`Diagramas/Diagrama_ER.puml`](file:///c:/Users/anton/Desktop/Programaci%C3%B3n/ManDS/Diagramas/Diagrama_ER.puml)
- **Diagrama de Casos de Uso (CU):** [`Diagramas/Diagrama_CU.puml`](file:///c:/Users/anton/Desktop/Programaci%C3%B3n/ManDS/Diagramas/Diagrama_CU.puml)

### Entidades Nucleares del Dominio:
- **`Persona`**: Entidad abstracta con herencia `JOINED` hacia `Administrador`, `Profesor` y `Alumno`.
- **`TokenVerificacion`**: Tokens de invitación y recuperación de contraseñas.
- **`Vehiculo`** e **`IncidenciaVehiculo`**: Gestión del parque móvil y partes mecánicos.
- **`Matricula`**: Vínculo del alumno con un carnet activo, convocatorias y estado.
- **`ClasePractica`**: Sesión formativa con odómetro y observaciones pedagógicas.
- **`SolicitudExamen`** y **`Examen`**: Flujo de presentación a pruebas DGT, citaciones y actas.

---

# 4. Panorama Funcional de Roles del Sistema

### 4.1. Administrador (Control Global del ERP)
- **Flota de Vehículos:** Alta, edición, seguimiento de ITV, kilometraje acumulado y revisiones mecánicas.
- **Equipo de Profesores:** Alta con invitaciones seguras por correo, asignación vehicular exclusiva 1-a-1, gestión de bajas lógicas y reasignaciones de alumnado.
- **Expedientes de Alumnos:** Monitorización de matrículas, estados académicos, convocatorias restantes, asignación individual de profesor y expedientes completos.
- **Convocatorias y Cupos DGT:** Configuración manual de las 2 fechas mensuales oficiales, gestión FIFO de solicitudes de examen, y resolución (aceptación con control de cupo por carnet o rechazo con motivo).
- **Métricas e Incidencias:** Analítica agregada de rentabilidad, aprobados/suspensos y supervisión de averías mecánicas.

### 4.2. Profesor (Gestión Docente y Operativa)
- **Agenda de Clases:** FullCalendar exclusivo con sus tramos de trabajo según su turno asignado (`MATINAL` o `TARDE`).
- **Ficha Pedagógica:** Registro inmutable de recogida del alumno y edición de odómetro (`kmInicio`, `kmFin`) y notas formativas tras cada clase.
- **Control de Exámenes:** Consulta de solicitudes y alumnos citados a su vehículo; registro de actas de examen (`APTO` / `NO APTO`).
- **Averías:** Notificación de incidencias mecánicas sobre su vehículo asignado.

### 4.3. Alumno (Área Personal y Formación)
- **Matrícula y Finanzas:** Matriculación en 1 carnet activo, adquisición de clases sueltas y bonos (10, 15, 20) mediante Stripe Checkout.
- **Reserva de Prácticas:** FullCalendar interactivo para agendar clases con su profesor asignado según su capacidad de reserva disponible.
- **Expediente Académico:** Histórico de clases realizadas (kilometraje, observaciones), convocatorias consumidas y estado de expediente.
- **Pruebas DGT:** Solicitud formal de convocatoria de examen teórico o práctico.
