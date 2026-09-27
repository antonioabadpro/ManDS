package com.autoescuela.erp.usuarios;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@TestPropertySource(properties =
{
        "spring.sql.init.mode=always",
        "spring.sql.init.data-locations=classpath:data.sql",
        "spring.jpa.defer-datasource-initialization=true"
})
/**
 * Pruebas de integración visual y autorización para el conjunto de vistas del Rol Profesor.
 */
class VistaProfesorTest
{
    @Autowired
    private WebApplicationContext contexto;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp()
    {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(this.contexto)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("Usuario anónimo que intenta acceder a /profesor/dashboard es redirigido al login")
    void testAccesoAnonimoRedirigeALogin() throws Exception
    {
        this.mockMvc.perform(get("/profesor/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(username = "alumno1", roles = "ALUMNO")
    @DisplayName("Usuario con Rol ALUMNO que intenta acceder a /profesor/dashboard recibe 403 Forbidden")
    void testAccesoAlumnoEsDenegado() throws Exception
    {
        this.mockMvc.perform(get("/profesor/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "profesor1", roles = "PROFESOR")
    @DisplayName("GET /profesor/dashboard renderiza correctamente con métricas y resumen de jornada")
    void testDashboardRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/profesor/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("profesor/dashboard"))
                .andExpect(content().string(containsString("Panel de Control del Profesor")))
                .andExpect(content().string(containsString("Alumnos Asignados")))
                .andExpect(content().string(containsString("Calendario de Clases")));
    }

    @Test
    @WithMockUser(username = "profesor1", roles = "PROFESOR")
    @DisplayName("GET /profesor/calendario renderiza la vista interactiva con contenedor de FullCalendar")
    void testCalendarioRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/profesor/calendario"))
                .andExpect(status().isOk())
                .andExpect(view().name("profesor/calendario"))
                .andExpect(content().string(containsString("Mi Calendario de Prácticas")))
                .andExpect(content().string(containsString("id=\"calendario-profesor\"")))
                .andExpect(content().string(containsString("Ficha Técnica de Clase Práctica")));
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
    @DisplayName("GET /profesor/alumnos renderiza correctamente el listado de alumnos tutelados")
    void testAlumnosRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/profesor/alumnos"))
                .andExpect(status().isOk())
                .andExpect(view().name("profesor/alumnos"))
                .andExpect(content().string(containsString("Mis Alumnos Tutelados")))
                .andExpect(content().string(containsString("id=\"tabla-mis-alumnos\"")));
    }

    @Test
    @WithMockUser(username = "profesor1", roles = "PROFESOR")
    @DisplayName("GET /profesor/examenes renderiza la vista de convocatorias con recordatorio de la Regla 7.5")
    void testExamenesRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/profesor/examenes"))
                .andExpect(status().isOk())
                .andExpect(view().name("profesor/examenes"))
                .andExpect(content().string(containsString("Convocatorias y Calificaciones DGT")))
                .andExpect(content().string(containsString("Regla de Negocio 7.5")));
    }

    @Test
    @WithMockUser(username = "profesor1", roles = "PROFESOR")
    @DisplayName("GET /profesor/vehiculo renderiza la ficha técnica del vehículo asignado e incidencias")
    void testVehiculoRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/profesor/vehiculo"))
                .andExpect(status().isOk())
                .andExpect(view().name("profesor/vehiculo"))
                .andExpect(content().string(containsString("Mi Vehículo e Incidencias")))
                .andExpect(content().string(containsString("SEAT Ibiza 1.0 TSI")))
                .andExpect(content().string(containsString("1234-LMN")));
    }

    @Test
    @WithMockUser(username = "profesor1", roles = "PROFESOR")
    @DisplayName("GET /profesor/estadisticas renderiza métricas docentes y tasa de aprobados")
    void testEstadisticasRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/profesor/estadisticas"))
                .andExpect(status().isOk())
                .andExpect(view().name("profesor/estadisticas"))
                .andExpect(content().string(containsString("Mis Estadísticas Docentes")))
                .andExpect(content().string(containsString("Tasa de Aprobados")));
    }

    @Test
    @WithMockUser(username = "profesor1", roles = "PROFESOR")
    @DisplayName("GET /profesor/perfil renderiza los datos profesionales y formularios de perfil")
    void testPerfilRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/profesor/perfil"))
                .andExpect(status().isOk())
                .andExpect(view().name("profesor/perfil"))
                .andExpect(content().string(containsString("Mi Perfil Docente")))
                .andExpect(content().string(containsString("Datos de Contacto y Personales")))
                .andExpect(content().string(containsString("Seguridad y Credenciales")));
    }

    @Test
    @WithMockUser(username = "profesor1", roles = "PROFESOR")
    @DisplayName("POST /profesor/clases/completar registra la ficha técnica y descuenta saldo al alumno")
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
                .andExpect(view().name("profesor/fragments/modal-ficha-clase :: modalFichaClase"))
                .andExpect(content().string(containsString("Ficha Técnica de la Clase Práctica")))
                .andExpect(content().string(containsString("form-completar-clase")));
    }

    @Test
    @WithMockUser(username = "profesor1", roles = "PROFESOR")
    @DisplayName("GET /profesor/examenes/{id}/modal devuelve el fragmento Thymeleaf con detalles de la jornada DGT para HTMX")
    void testModalExamenDgtHtmx() throws Exception
    {
        this.mockMvc.perform(get("/profesor/examenes/1/modal")
                .header("HX-Request", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("profesor/fragments/modal-detalle-examen :: modalDetalleExamen"))
                .andExpect(content().string(containsString("Jornada Oficial DGT")));
    }

    @Test
    @WithMockUser(username = "profesor1", roles = "PROFESOR")
    @DisplayName("POST /profesor/clases/completar con HTMX devuelve 200, cabecera HX-Trigger y alerta OOB")
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
                .andExpect(view().name("profesor/fragments/alerta-feedback :: feedbackExito"))
                .andExpect(content().string(containsString("Ficha técnica de la clase registrada correctamente")));
    }

    @Test
    @WithMockUser(username = "profesor1", roles = "PROFESOR")
    @DisplayName("POST /profesor/clases/cancelar con HTMX devuelve 200, cabecera HX-Trigger y alerta OOB")
    void testCancelarClasePracticaHtmxExito() throws Exception
    {
        this.mockMvc.perform(post("/profesor/clases/cancelar")
                .with(csrf())
                .header("HX-Request", "true")
                .param("claseId", "3")
                .param("motivo", "Indisposición temporal del profesor"))
                .andExpect(status().isOk())
                .andExpect(header().string("HX-Trigger", "actualizarCalendario"))
                .andExpect(view().name("profesor/fragments/alerta-feedback :: feedbackExito"))
                .andExpect(content().string(containsString("Clase práctica cancelada correctamente")));
    }
}
