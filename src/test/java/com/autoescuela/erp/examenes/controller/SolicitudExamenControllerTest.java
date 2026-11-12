package com.autoescuela.erp.examenes.controller;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import com.autoescuela.erp.BaseIntegrationTest;
import com.autoescuela.erp.academico.model.Matricula;
import com.autoescuela.erp.academico.repository.MatriculaRepository;
import com.autoescuela.erp.examenes.model.Examen;
import com.autoescuela.erp.examenes.repository.ExamenRepository;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.repository.AlumnoRepository;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Pruebas de integración para todas las barreras y flujos de SolicitudExamenController.
 */
class SolicitudExamenControllerTest extends BaseIntegrationTest
{
    @Autowired
    private AlumnoRepository alumnoRepository;

    @Autowired
    private MatriculaRepository matriculaRepository;

    @Autowired
    private ExamenRepository examenRepository;

    @Test
    @WithMockUser(username = "alumno1", roles = "ALUMNO")
    @DisplayName("GET /alumno/examenes/solicitar-modal carga flags de diagnóstico según estado del expediente")
    void testModalSolicitarExamenVariosEstados() throws Exception
    {
        this.mockMvc.perform(get("/alumno/examenes/solicitar-modal"))
                .andExpect(status().isOk())
                .andExpect(view().name("alumno/fragments/modal-solicitar-examen :: modalSolicitarExamen"))
                .andExpect(model().attributeExists("esMatriculaActiva", "teoricoAprobado", "puedeSolicitarPractico"));
    }

    @Test
    @WithMockUser(username = "alumno9", roles = "ALUMNO")
    @DisplayName("GET /solicitar-modal detecta expediente cerrado tras práctico aprobado y POST rechaza nuevas solicitudes")
    void testExpedienteCerradoConPracticoAprobado() throws Exception
    {
        this.mockMvc.perform(get("/alumno/examenes/solicitar-modal"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("expedienteCerrado", true))
                .andExpect(model().attribute("practicoAprobado", true));

        // Reactivar temporalmente matrícula para verificar específicamente la barrera de práctico aprobado en POST /solicitar
        Alumno alumno = this.alumnoRepository.findByNombreUsuario("alumno9").orElseThrow();
        Matricula ultima = this.matriculaRepository.findByAlumnoOrderByFechaMatriculacionDesc(alumno).getFirst();
        ultima.setEstaActiva(true);
        this.matriculaRepository.saveAndFlush(ultima);

        this.mockMvc.perform(post("/alumno/examenes/solicitar")
                        .with(csrf())
                        .param("tipoExamen", "PRACTICO"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/alumno/examenes"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @WithMockUser(username = "alumno1", roles = "ALUMNO")
    @DisplayName("POST /alumno/examenes/solicitar valida binding, sin matrícula, examen futuro, duplicado teórico y registro práctico válido")
    void testSolicitarExamenBarrerasYRegistroExitoso() throws Exception
    {
        // Error de validación (sin tipoExamen)
        this.mockMvc.perform(post("/alumno/examenes/solicitar")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/alumno/examenes"))
                .andExpect(flash().attributeExists("error"));

        // Poner fecha futura al examen práctico 3 del alumno1 para cubrir solicitud aceptada pendiente
        Examen examen3 = this.examenRepository.findById(3L).orElseThrow();
        examen3.setFechaHora(LocalDateTime.now().plusDays(10));
        this.examenRepository.saveAndFlush(examen3);

        this.mockMvc.perform(post("/alumno/examenes/solicitar")
                        .with(csrf())
                        .param("tipoExamen", "PRACTICO"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/alumno/examenes"))
                .andExpect(flash().attributeExists("error"));

        // Marcar examen 3 como calificado (NO APTO) para liberar convocatoria futura pendiente
        examen3.setFechaHora(LocalDateTime.now().minusDays(3));
        examen3.setEsApto(false);
        this.examenRepository.saveAndFlush(examen3);

        // Solicitar TEORICO estando ya aprobado -> error
        this.mockMvc.perform(post("/alumno/examenes/solicitar")
                        .with(csrf())
                        .param("tipoExamen", "TEORICO"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/alumno/examenes"))
                .andExpect(flash().attributeExists("error"));

        // Solicitud PRACTICO válida -> mensajeExito
        this.mockMvc.perform(post("/alumno/examenes/solicitar")
                        .with(csrf())
                        .param("tipoExamen", "PRACTICO"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/alumno/examenes"))
                .andExpect(flash().attributeExists("mensajeExito"));

        // Reintentar con solicitud PENDIENTE ya creada -> error
        this.mockMvc.perform(post("/alumno/examenes/solicitar")
                        .with(csrf())
                        .param("tipoExamen", "PRACTICO"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/alumno/examenes"))
                .andExpect(flash().attributeExists("error"));

        // Sin matrícula activa -> error
        Alumno alumno = this.alumnoRepository.findByNombreUsuario("alumno1").orElseThrow();
        Matricula matricula = this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno).orElseThrow();
        matricula.setEstaActiva(false);
        this.matriculaRepository.saveAndFlush(matricula);

        this.mockMvc.perform(post("/alumno/examenes/solicitar")
                        .with(csrf())
                        .param("tipoExamen", "PRACTICO"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/alumno/examenes"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @WithMockUser(username = "alumno7", roles = "ALUMNO")
    @DisplayName("POST /alumno/examenes/solicitar bloquea matrícula con convocatorias agotadas")
    void testSolicitarConConvocatoriasAgotadas() throws Exception
    {
        this.mockMvc.perform(post("/alumno/examenes/solicitar")
                        .with(csrf())
                        .param("tipoExamen", "PRACTICO"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/alumno/examenes"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @WithMockUser(username = "alumno3", roles = "ALUMNO")
    @DisplayName("POST /alumno/examenes/solicitar rechaza TEORICO en modalidad sólo PRACTICA")
    void testSolicitarTeoricoEnModalidadPractica() throws Exception
    {
        this.mockMvc.perform(post("/alumno/examenes/solicitar")
                        .with(csrf())
                        .param("tipoExamen", "TEORICO"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/alumno/examenes"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @WithMockUser(username = "alumno18", roles = "ALUMNO")
    @DisplayName("POST /alumno/examenes/solicitar rechaza PRACTICO sin teórico previo en modalidad TEORICO_PRACTICA")
    void testSolicitarPracticoSinTeoricoAprobado() throws Exception
    {
        this.mockMvc.perform(post("/alumno/examenes/solicitar")
                        .with(csrf())
                        .param("tipoExamen", "PRACTICO"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/alumno/examenes"))
                .andExpect(flash().attributeExists("error"));
    }
}
