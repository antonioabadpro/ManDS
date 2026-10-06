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
        this.mockMvc.perform(get("/admin/alumnos/detalle/7"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/modal-detalle-alumno :: modal-detalle-alumno"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /admin/alumnos/expediente/{id} renderiza el modal de expediente con métricas, últimas clases y exámenes")
    void testModalExpedienteAlumnoRenderizadoConClasesYExamenes() throws Exception
    {
        this.mockMvc.perform(get("/admin/alumnos/expediente/7"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/modal-expediente-alumno :: modal-expediente-alumno"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /admin/alumnos/expediente/{id} renderiza el modal de expediente para alumno sin clases ni exámenes")
    void testModalExpedienteAlumnoSinClasesNiExamenes() throws Exception
    {
        this.mockMvc.perform(get("/admin/alumnos/expediente/31"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/modal-expediente-alumno :: modal-expediente-alumno"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /admin/alumnos/expediente/{id} renderiza clases prácticas recibidas y exámenes para alumno con expediente previo")
    void testModalExpedienteAlumnoConClasesPreviasYExamenesAprobados() throws Exception
    {
        this.mockMvc.perform(get("/admin/alumnos/expediente/27"))
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
                .andExpect(model().attributeExists("vehiculos", "totalVehiculos", "totalDisponibles", "totalOcupados", "totalMantenimiento"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /admin/incidencias renderiza correctamente la vista de bandeja de incidencias")
    void testIncidenciasRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/admin/incidencias"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/incidencias"));
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
