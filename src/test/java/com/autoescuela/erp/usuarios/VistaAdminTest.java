package com.autoescuela.erp.usuarios;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import com.autoescuela.erp.BaseIntegrationTest;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Pruebas de integración visual y controladores para el Rol Administrador.
 * Valida la resolución de plantillas, fragmentos modales y flujos de edición heredando de BaseIntegrationTest.
 */
class VistaAdminTest extends BaseIntegrationTest
{
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /admin/dashboard renderiza correctamente la vista del panel de administración")
    void testDashboardRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /admin/profesores renderiza correctamente la vista de gestión de profesores")
    void testProfesoresRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/admin/profesores"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/profesores"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /admin/alumnos renderiza correctamente la vista de gestión de alumnos")
    void testAlumnosRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/admin/alumnos"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/alumnos"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /admin/alumnos/detalle/{id} renderiza correctamente el fragmento modal con los datos del alumno")
    void testModalDetalleAlumnoRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/admin/alumnos/detalle/200"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/modal-detalle-alumno :: modal-detalle-alumno"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /admin/alumnos/expediente/{id} renderiza el modal de expediente con métricas, últimas clases y exámenes")
    void testModalExpedienteAlumnoRenderizadoConClasesYExamenes() throws Exception
    {
        this.mockMvc.perform(get("/admin/alumnos/expediente/200"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/modal-expediente-alumno :: modal-expediente-alumno"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /admin/alumnos/expediente/{id} renderiza el modal de expediente para alumno sin clases ni exámenes")
    void testModalExpedienteAlumnoSinClasesNiExamenes() throws Exception
    {
        this.mockMvc.perform(get("/admin/alumnos/expediente/224"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/modal-expediente-alumno :: modal-expediente-alumno"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /admin/alumnos/expediente/{id} renderiza clases prácticas recibidas y exámenes para alumno con expediente previo")
    void testModalExpedienteAlumnoConClasesPreviasYExamenesAprobados() throws Exception
    {
        this.mockMvc.perform(get("/admin/alumnos/expediente/220"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/modal-expediente-alumno :: modal-expediente-alumno"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /admin/flota renderiza correctamente la vista de gestión de flota")
    void testFlotaRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/admin/flota"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/flota"))
                .andExpect(model().attributeExists("vehiculos", "totalVehiculos", "totalDisponibles", "totalOcupados", "totalMantenimiento", "altaVehiculoDTO", "tiposCarnet", "tiposCombustible", "tiposCambio"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("POST /admin/flota/alta da de alta un vehículo válido y redirige con mensaje flash")
    void testDarAltaVehiculoExitoso() throws Exception
    {
        this.mockMvc.perform(post("/admin/flota/alta")
                        .with(csrf())
                        .param("matricula", "9988-ZZZ")
                        .param("marca", "Hyundai")
                        .param("modelo", "i20 N Line")
                        .param("color", "Rojo Dragón")
                        .param("km", "150")
                        .param("cv", "120")
                        .param("anio", "2024")
                        .param("tipoCombustible", "GASOLINA")
                        .param("cajaCambios", "MANUAL")
                        .param("tipoPermiso", "PERMISO_B")
                        .param("fechaProximaRevision", "2027-10-01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/flota"))
                .andExpect(flash().attributeExists("mensajeExito"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("POST /admin/flota/alta con errores de validación mantiene la vista y abre el modal con errores")
    void testDarAltaVehiculoErroresValidacion() throws Exception
    {
        this.mockMvc.perform(post("/admin/flota/alta")
                        .with(csrf())
                        .param("matricula", "")
                        .param("marca", "")
                        .param("modelo", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/flota"))
                .andExpect(model().attributeExists("abrirModalAltaVehiculo"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /admin/flota/detalle/{id} renderiza correctamente el fragmento modal con los datos del vehículo")
    void testModalDetalleVehiculoRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/admin/flota/detalle/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/modal-detalle-vehiculo :: modal-detalle-vehiculo"))
                .andExpect(model().attributeExists("vehiculo", "abrirModalDetalleVehiculo"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /admin/flota/baja/{id} renderiza el modal de baja con opción de tramitación para vehículo disponible")
    void testModalBajaVehiculoDisponibleRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/admin/flota/baja/5"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/modal-baja-vehiculo :: modal-baja-vehiculo"))
                .andExpect(model().attributeExists("vehiculo", "abrirModalBajaVehiculo", "tieneProfesorAsignado", "puedeDarBaja"))
                .andExpect(model().attribute("tieneProfesorAsignado", false))
                .andExpect(model().attribute("puedeDarBaja", true));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /admin/flota/baja/{id} renderiza el modal con bloqueo si el vehículo tiene profesor asignado")
    void testModalBajaVehiculoConProfesorBloqueado() throws Exception
    {
        this.mockMvc.perform(get("/admin/flota/baja/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/modal-baja-vehiculo :: modal-baja-vehiculo"))
                .andExpect(model().attributeExists("vehiculo", "abrirModalBajaVehiculo", "tieneProfesorAsignado", "puedeDarBaja"))
                .andExpect(model().attribute("tieneProfesorAsignado", true))
                .andExpect(model().attribute("puedeDarBaja", false));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("POST /admin/flota/baja/{id} tramita la baja lógica de vehículo disponible y redirige con mensaje flash")
    void testDarBajaVehiculoDisponibleExitoso() throws Exception
    {
        this.mockMvc.perform(post("/admin/flota/baja/6")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/flota"))
                .andExpect(flash().attributeExists("mensajeExito"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("POST /admin/flota/baja/{id} rechaza la baja de un vehículo con profesor asignado y muestra error en modal")
    void testDarBajaVehiculoConProfesorAsignadoRechazado() throws Exception
    {
        this.mockMvc.perform(post("/admin/flota/baja/1")
                        .header("HX-Request", "true")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/modal-baja-vehiculo :: modal-baja-vehiculo"))
                .andExpect(model().attributeExists("errorBajaVehiculo", "abrirModalBajaVehiculo"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("POST /admin/flota/validar-matricula devuelve fragmento de error cuando la matrícula ya existe en la BD")
    void testValidarMatriculaExistenteDevuelveMensajeError() throws Exception
    {
        this.mockMvc.perform(post("/admin/flota/validar-matricula")
                        .with(csrf())
                        .param("matricula", "1234-LMN"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/registro :: mensaje-error"))
                .andExpect(model().attributeExists("mensaje"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("POST /admin/flota/validar-matricula devuelve fragmento vacío cuando la matrícula está disponible y es válida")
    void testValidarMatriculaNuevaDisponibleDevuelveFragmentoVacio() throws Exception
    {
        this.mockMvc.perform(post("/admin/flota/validar-matricula")
                        .with(csrf())
                        .param("matricula", "0001-AAA"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/registro :: fragmento-vacio"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("POST /admin/flota/validar-matricula devuelve fragmento de error cuando el formato es inválido o está vacía")
    void testValidarMatriculaFormatoInvalidoDevuelveMensajeError() throws Exception
    {
        this.mockMvc.perform(post("/admin/flota/validar-matricula")
                        .with(csrf())
                        .param("matricula", "123-A"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/registro :: mensaje-error"))
                .andExpect(model().attributeExists("mensaje"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /admin/incidencias renderiza correctamente la vista de bandeja de incidencias con métricas")
    void testIncidenciasRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/admin/incidencias"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/incidencias"))
                .andExpect(model().attributeExists("incidencias"))
                .andExpect(model().attributeExists("totalPendientes"))
                .andExpect(model().attributeExists("totalEnProceso"))
                .andExpect(model().attributeExists("totalResueltas"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /admin/incidencias/gestionar/{id} renderiza el modal de gestión para una incidencia pendiente")
    void testModalGestionarIncidenciaRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/admin/incidencias/gestionar/2"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/modal-gestionar-incidencia :: modal-gestionar-incidencia"))
                .andExpect(model().attributeExists("incidencia"))
                .andExpect(model().attribute("abrirModalGestionarIncidencia", true));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /admin/incidencias/detalle/{id} renderiza el modal de detalle informativo de la incidencia")
    void testModalDetalleIncidenciaRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/admin/incidencias/detalle/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/modal-detalle-incidencia :: modal-detalle-incidencia"))
                .andExpect(model().attributeExists("incidencia"))
                .andExpect(model().attribute("abrirModalDetalleIncidencia", true));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("POST /admin/incidencias/gestionar actualiza el estado a EN_PROCESO y redirige a la bandeja")
    void testGestionarIncidenciaTransicionEnProcesoExito() throws Exception
    {
        this.mockMvc.perform(post("/admin/incidencias/gestionar")
                .with(csrf())
                .param("id", "2")
                .param("estado", "EN_PROCESO"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/incidencias"))
                .andExpect(flash().attributeExists("mensajeExito"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /admin/examenes renderiza correctamente la vista de convocatorias y cola FIFO DGT")
    void testExamenesRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/admin/examenes"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/examenes"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /admin/practicas renderiza correctamente la agenda global de prácticas")
    void testPracticasRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/admin/practicas"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/practicas"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /admin/estadisticas renderiza correctamente el panel de métricas globales")
    void testEstadisticasRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/admin/estadisticas"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/estadisticas"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /admin/dashboard responde 200 OK para el administrador")
    void testTopBarComponentesRenderizados() throws Exception
    {
        this.mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /admin/perfil renderiza correctamente el formulario de Mi Perfil")
    void testPerfilAdminRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/admin/perfil"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/perfil"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /perfil redirige a /admin/perfil para un usuario autenticado con Rol ADMIN")
    void testRedireccionPerfilAdmin() throws Exception
    {
        this.mockMvc.perform(get("/perfil"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/perfil"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("POST /admin/perfil actualiza los datos personales y redirige con mensaje flash de éxito")
    void testActualizarPerfilAdminExito() throws Exception
    {
        this.mockMvc.perform(post("/admin/perfil")
                .with(csrf())
                .param("dni", "00000000T")
                .param("nombre", "Carlos Modificado")
                .param("apellidos", "García Moreno")
                .param("telefono", "611223344")
                .param("direccion", "Calle Gran Vía 100, Madrid")
                .param("fechaNacimiento", "1985-04-12"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/perfil"))
                .andExpect(flash().attributeExists("mensajeExito"));
    }
}
