package com.autoescuela.erp.usuarios;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import com.autoescuela.erp.BaseIntegrationTest;
import com.autoescuela.erp.academico.model.Matricula;
import com.autoescuela.erp.academico.repository.MatriculaRepository;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.repository.AlumnoRepository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Pruebas de integración visual y controladores para el Rol Profesor.
 * Valida la resolución de plantillas, fragmentos HTMX y flujos de reporte heredando de BaseIntegrationTest.
 */
class VistaProfesorTest extends BaseIntegrationTest
{
    @Autowired
    private AlumnoRepository alumnoRepository;

    @Autowired
    private MatriculaRepository matriculaRepository;

    @Test
    @WithMockUser(username = "profesor1", roles = "PROFESOR")
    @DisplayName("GET /profesor/dashboard renderiza correctamente el panel del profesor")
    void testDashboardRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/profesor/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("profesor/dashboard"));
    }

    @Test
    @WithMockUser(username = "profesor1", roles = "PROFESOR")
    @DisplayName("GET /profesor/calendario renderiza la vista interactiva de calendario")
    void testCalendarioRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/profesor/calendario"))
                .andExpect(status().isOk())
                .andExpect(view().name("profesor/calendario"));
    }

    @Test
    @WithMockUser(username = "profesor1", roles = "PROFESOR")
    @DisplayName("GET /profesor/calendario/eventos devuelve JSON con eventos para FullCalendar")
    void testEventosCalendarioApi() throws Exception
    {
        this.mockMvc.perform(get("/profesor/calendario/eventos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "profesor1", roles = "PROFESOR")
    @DisplayName("GET /profesor/alumnos renderiza correctamente el listado de alumnos asignados")
    void testAlumnosRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/profesor/alumnos"))
                .andExpect(status().isOk())
                .andExpect(view().name("profesor/alumnos"));
    }

    @Test
    @WithMockUser(username = "profesor1", roles = "PROFESOR")
    @DisplayName("GET /profesor/examenes renderiza la vista de convocatorias DGT")
    void testExamenesRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/profesor/examenes"))
                .andExpect(status().isOk())
                .andExpect(view().name("profesor/examenes"));
    }

    @Test
    @WithMockUser(username = "profesor1", roles = "PROFESOR")
    @DisplayName("GET /profesor/vehiculo renderiza la ficha técnica del vehículo asignado e incidencias")
    void testVehiculoRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/profesor/vehiculo"))
                .andExpect(status().isOk())
                .andExpect(view().name("profesor/vehiculo"));
    }

    @Test
    @WithMockUser(username = "profesor1", roles = "PROFESOR")
    @DisplayName("GET /profesor/estadisticas renderiza métricas del profesor")
    void testEstadisticasRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/profesor/estadisticas"))
                .andExpect(status().isOk())
                .andExpect(view().name("profesor/estadisticas"));
    }

    @Test
    @WithMockUser(username = "profesor1", roles = "PROFESOR")
    @DisplayName("GET /profesor/perfil renderiza los datos de perfil del profesor")
    void testPerfilRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/profesor/perfil"))
                .andExpect(status().isOk())
                .andExpect(view().name("profesor/perfil"));
    }

    @Test
    @WithMockUser(username = "profesor1", roles = "PROFESOR")
    @DisplayName("POST /profesor/clases/completar registra la ficha técnica y redirige al calendario")
    void testCompletarClasePracticaExito() throws Exception
    {
        this.mockMvc.perform(post("/profesor/clases/completar")
                .with(csrf())
                .param("claseId", "2")
                .param("kmInicio", "45000")
                .param("kmFin", "45040")
                .param("observaciones", "Práctica de estacionamiento en línea realizada con éxito."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profesor/calendario"))
                .andExpect(flash().attributeExists("mensajeExito"));
    }

    @Test
    @WithMockUser(username = "profesor1", roles = "PROFESOR")
    @DisplayName("POST /profesor/vehiculo/incidencia registra una avería mecánica correctamente")
    void testReportarIncidenciaExito() throws Exception
    {
        this.mockMvc.perform(post("/profesor/vehiculo/incidencia")
                .with(csrf())
                .param("vehiculoId", "1")
                .param("descripcion", "Ruido al accionar el pedal del embrague en frío."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profesor/vehiculo"))
                .andExpect(flash().attributeExists("mensajeExito"));
    }

    @Test
    @WithMockUser(username = "profesor1", roles = "PROFESOR")
    @DisplayName("GET /profesor/clases/{id}/modal devuelve el fragmento Thymeleaf con datos de la clase para HTMX")
    void testModalClasePracticaHtmx() throws Exception
    {
        this.mockMvc.perform(get("/profesor/clases/1/modal")
                .header("HX-Request", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("profesor/fragments/modal-ficha-clase :: modalFichaClase"));
    }

    @Test
    @WithMockUser(username = "profesor1", roles = "PROFESOR")
    @DisplayName("GET /profesor/examenes/{id}/modal devuelve el fragmento Thymeleaf con detalles de la jornada DGT para HTMX")
    void testModalExamenDgtHtmx() throws Exception
    {
        this.mockMvc.perform(get("/profesor/examenes/1/modal")
                .header("HX-Request", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("profesor/fragments/modal-detalle-examen :: modalDetalleExamen"));
    }

    @Test
    @WithMockUser(username = "profesor1", roles = "PROFESOR")
    @DisplayName("POST /profesor/clases/completar con HTMX devuelve 200, cabecera HX-Trigger y fragmento de feedback")
    void testCompletarClasePracticaHtmxExito() throws Exception
    {
        this.mockMvc.perform(post("/profesor/clases/completar")
                .with(csrf())
                .header("HX-Request", "true")
                .param("claseId", "2")
                .param("kmInicio", "45000")
                .param("kmFin", "45040")
                .param("observaciones", "Práctica completada con éxito vía HTMX."))
                .andExpect(status().isOk())
                .andExpect(header().string("HX-Trigger", "actualizarCalendario"))
                .andExpect(view().name("profesor/fragments/alerta-feedback :: feedbackExito"));
    }

    @Test
    @WithMockUser(username = "profesor1", roles = "PROFESOR")
    @DisplayName("POST /profesor/clases/cancelar con HTMX devuelve 200, cabecera HX-Trigger y fragmento de feedback")
    void testCancelarClasePracticaHtmxExito() throws Exception
    {
        this.mockMvc.perform(post("/profesor/clases/cancelar")
                .with(csrf())
                .header("HX-Request", "true")
                .param("claseId", "3")
                .param("motivo", "Indisposición temporal del profesor"))
                .andExpect(status().isOk())
                .andExpect(header().string("HX-Trigger", "actualizarCalendario"))
                .andExpect(view().name("profesor/fragments/alerta-feedback :: feedbackExito"));
    }

    @Test
    @WithMockUser(username = "profesor1", roles = "PROFESOR")
    @DisplayName("POST /profesor/examenes/calificar con APTO en examen práctico cierra la matrícula y desvincula al profesor")
    void testCalificarExamenPracticoAptoCierraMatriculaYDesvinculaProfesor() throws Exception
    {
        this.mockMvc.perform(post("/profesor/examenes/calificar")
                .with(csrf())
                .param("examenId", "3")
                .param("esApto", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profesor/examenes"))
                .andExpect(flash().attributeExists("mensajeExito"));

        Alumno alumno = this.alumnoRepository.findById(7L).orElseThrow();
        assertNull(alumno.getProfesor(), "El profesor debe quedar desvinculado tras aprobar el examen práctico.");

        Matricula matricula = this.matriculaRepository.findById(1L).orElseThrow();
        assertFalse(matricula.getEstaActiva(), "La matrícula debe finalizar (estaActiva = false) tras aprobar el práctico.");
    }
}
