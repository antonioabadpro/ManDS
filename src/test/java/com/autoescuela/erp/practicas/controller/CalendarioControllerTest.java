package com.autoescuela.erp.practicas.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import com.autoescuela.erp.BaseIntegrationTest;
import com.autoescuela.erp.academico.model.Matricula;
import com.autoescuela.erp.academico.repository.MatriculaRepository;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.repository.AlumnoRepository;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Pruebas de integración para rutas límite, validaciones de jornadas y fragmentos HTMX en CalendarioController.
 */
class CalendarioControllerTest extends BaseIntegrationTest
{
    @Autowired
    private AlumnoRepository alumnoRepository;

    @Autowired
    private MatriculaRepository matriculaRepository;

    @Test
    @WithMockUser(username = "usuario_inexistente", roles = "ALUMNO")
    @DisplayName("Endpoints de calendario del alumno redirigen a /login si el usuario no existe en BD")
    void testCalendarioAlumnoSinEntidad() throws Exception
    {
        this.mockMvc.perform(get("/alumno/calendario"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        this.mockMvc.perform(get("/alumno/calendario/cuadricula"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        this.mockMvc.perform(get("/alumno/calendario/eventos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @WithMockUser(username = "alumno1", roles = "ALUMNO")
    @DisplayName("GET /alumno/calendario y /cuadricula aceptan fechaRef válida o inválida y /cuadricula bloquea sin matrícula")
    void testCalendarioAlumnoFechaRefYBloqueoCuadricula() throws Exception
    {
        this.mockMvc.perform(get("/alumno/calendario").param("fechaRef", "2026-10-12"))
                .andExpect(status().isOk())
                .andExpect(view().name("alumno/calendario"))
                .andExpect(model().attributeExists("calendarioSemanal"));

        this.mockMvc.perform(get("/alumno/calendario/cuadricula").param("fechaRef", "formato-invalido"))
                .andExpect(status().isOk())
                .andExpect(view().name("alumno/fragments/cuadricula-calendario :: cuadriculaSemanal"))
                .andExpect(model().attributeExists("calendarioSemanal"));

        // Desactivar temporalmente matrícula dentro de la transacción de prueba para cubrir bloqueo desde /cuadricula
        Alumno alumno = this.alumnoRepository.findByNombreUsuario("alumno1").orElseThrow();
        Matricula matricula = this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno).orElseThrow();
        matricula.setEstaActiva(false);
        this.matriculaRepository.saveAndFlush(matricula);

        this.mockMvc.perform(get("/alumno/calendario/cuadricula"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/alumno/dashboard"));
    }

    @Test
    @WithMockUser(username = "usuario_inexistente", roles = "PROFESOR")
    @DisplayName("Endpoints de calendario del profesor redirigen a /login si el usuario no existe en BD")
    void testCalendarioProfesorSinEntidad() throws Exception
    {
        this.mockMvc.perform(get("/profesor/calendario"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        this.mockMvc.perform(get("/profesor/calendario/cuadricula"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        this.mockMvc.perform(get("/profesor/calendario/eventos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        this.mockMvc.perform(get("/profesor/calendario/cancelar-dia/modal"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        this.mockMvc.perform(get("/profesor/calendario/cancelar-dia/calcular-clases").param("fecha", LocalDate.now().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        this.mockMvc.perform(post("/profesor/calendario/cancelar-dia")
                        .with(csrf())
                        .param("fecha", LocalDate.now().plusDays(1).toString())
                        .param("motivo", "Prueba"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(username = "profesor1", roles = "PROFESOR")
    @DisplayName("Modal y cálculo previo de cancelación diaria cubren fechas pasadas, fin de semana y festivos")
    void testModalYCalcularCancelarDiaValidaciones() throws Exception
    {
        LocalDate pasado = LocalDate.now().minusDays(2);
        LocalDate sabado = LocalDate.now().plusWeeks(1).with(DayOfWeek.SATURDAY);
        LocalDate festivoNavidad = LocalDate.of(LocalDate.now().getYear() + 1, 12, 25);

        this.mockMvc.perform(get("/profesor/calendario/cancelar-dia/modal")
                        .param("fechaRef", pasado.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("profesor/fragments/modal-cancelar-dia :: modalCancelarDia"))
                .andExpect(model().attributeExists("calendarioSemanal"));

        this.mockMvc.perform(get("/profesor/calendario/cancelar-dia/modal")
                        .param("fecha", pasado.toString()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("noCancelable", true));

        this.mockMvc.perform(get("/profesor/calendario/cancelar-dia/calcular-clases")
                        .param("fecha", sabado.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("profesor/fragments/modal-cancelar-dia :: vistaPreviaClases"))
                .andExpect(model().attribute("noCancelable", true));

        this.mockMvc.perform(get("/profesor/calendario/cancelar-dia/calcular-clases")
                        .param("fecha", festivoNavidad.toString()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("noCancelable", true));
    }

    @Test
    @WithMockUser(username = "profesor1", roles = "PROFESOR")
    @DisplayName("POST /profesor/calendario/cancelar-dia rechaza fechas inválidas y permite flujo tradicional no-HTMX")
    void testCancelarDiaErroresYFlujoEstandar() throws Exception
    {
        LocalDate pasado = LocalDate.now().minusDays(1);
        LocalDate sabado = LocalDate.now().plusWeeks(2).with(DayOfWeek.SATURDAY);
        LocalDate festivo = LocalDate.of(LocalDate.now().getYear() + 1, 1, 1);
        LocalDate habilFuturo = LocalDate.now().plusWeeks(4).with(DayOfWeek.WEDNESDAY);

        // Fecha pasada en flujo no-HTMX
        this.mockMvc.perform(post("/profesor/calendario/cancelar-dia")
                        .with(csrf())
                        .param("fecha", pasado.toString())
                        .param("motivo", "Ausencia"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profesor/calendario"))
                .andExpect(flash().attributeExists("error"));

        // Fin de semana con HTMX
        this.mockMvc.perform(post("/profesor/calendario/cancelar-dia")
                        .with(csrf())
                        .header("HX-Request", "true")
                        .param("fecha", sabado.toString())
                        .param("motivo", "Ausencia"))
                .andExpect(status().isOk())
                .andExpect(view().name("profesor/fragments/alerta-feedback :: feedbackExito"))
                .andExpect(model().attributeExists("error"));

        // Festivo / vacaciones con HTMX
        this.mockMvc.perform(post("/profesor/calendario/cancelar-dia")
                        .with(csrf())
                        .header("HX-Request", "true")
                        .param("fecha", festivo.toString())
                        .param("motivo", "Ausencia"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"));

        // Motivo en blanco con HTMX
        this.mockMvc.perform(post("/profesor/calendario/cancelar-dia")
                        .with(csrf())
                        .header("HX-Request", "true")
                        .param("fecha", habilFuturo.toString())
                        .param("motivo", "   "))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"));

        // Éxito sin cabecera HX-Request -> redirect con flash
        this.mockMvc.perform(post("/profesor/calendario/cancelar-dia")
                        .with(csrf())
                        .param("fecha", habilFuturo.toString())
                        .param("motivo", "Asistencia a curso de actualización vial."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profesor/calendario"))
                .andExpect(flash().attributeExists("mensajeExito"));
    }
}
