package com.autoescuela.erp.practicas.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import com.autoescuela.erp.BaseIntegrationTest;
import com.autoescuela.erp.core.enums.EstadoVehiculo;
import com.autoescuela.erp.flota.model.Vehiculo;
import com.autoescuela.erp.flota.repository.VehiculoRepository;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.repository.AlumnoRepository;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Pruebas de integración para validaciones de reserva, cancelación, ficha técnica y control de acceso en ClasePracticaController.
 */
class ClasePracticaControllerTest extends BaseIntegrationTest
{
    @Autowired
    private AlumnoRepository alumnoRepository;

    @Autowired
    private VehiculoRepository vehiculoRepository;

    @Test
    @WithMockUser(username = "alumno1", roles = "ALUMNO")
    @DisplayName("Modales de alumno cubren preselección de fecha, 404 y 403 sobre clases ajenas")
    void testModalesAlumnoPreseleccionYAcceso() throws Exception
    {
        this.mockMvc.perform(get("/alumno/clases/reservar-modal").param("fecha", "2027-01-01"))
                .andExpect(status().isOk())
                .andExpect(view().name("alumno/fragments/modal-reservar-clase :: modalReservarClase"))
                .andExpect(model().attribute("fechaPreseleccionada", "2027-01-01T10:00"))
                .andExpect(model().attribute("esNoLaborable", true));

        this.mockMvc.perform(get("/alumno/clases/reservar-modal").param("fecha", "2027-02-10T09:30:00"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("fechaPreseleccionada", "2027-02-10T09:30"));

        this.mockMvc.perform(get("/alumno/clases/99999/modal"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"));

        // Clase 49 pertenece a alumno2 (id=201) -> alumno1 debe recibir error/403
        this.mockMvc.perform(get("/alumno/clases/49/modal"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/403"));

        this.mockMvc.perform(get("/alumno/clases/49/cancelar-modal"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/403"));

        // Listado historial de clases estándar y HTMX
        this.mockMvc.perform(get("/alumno/clases"))
                .andExpect(status().isOk())
                .andExpect(view().name("alumno/clases"));

        this.mockMvc.perform(get("/alumno/clases").header("HX-Request", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("alumno/clases :: tablaHistorialClases"));
    }

    @Test
    @WithMockUser(username = "alumno1", roles = "ALUMNO")
    @DisplayName("POST /alumno/clases/reservar valida campos, festivos, turno MATINAL, mantenimiento y éxito estándar")
    void testReservarClaseValidacionesAlumnoMatinal() throws Exception
    {
        LocalDate habilFuturo = LocalDate.now().plusWeeks(3).with(DayOfWeek.MONDAY);
        while (habilFuturo.getMonthValue() == 8 || (habilFuturo.getMonthValue() == 12 && habilFuturo.getDayOfMonth() >= 23))
        {
            habilFuturo = habilFuturo.plusWeeks(3);
        }

        // BindingResult con error (sin puntoRecogida) en HTMX y no-HTMX
        this.mockMvc.perform(post("/alumno/clases/reservar")
                        .with(csrf())
                        .header("HX-Request", "true")
                        .param("fechaHora", LocalDateTime.of(habilFuturo.getYear(), habilFuturo.getMonth(), habilFuturo.getDayOfMonth(), 10, 15).toString())
                        .param("duracion", "45")
                        .param("puntoRecogida", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("alumno/fragments/alerta-feedback :: feedbackExito"))
                .andExpect(model().attributeExists("error"));

        this.mockMvc.perform(post("/alumno/clases/reservar")
                        .with(csrf())
                        .param("fechaHora", LocalDateTime.of(habilFuturo.getYear(), habilFuturo.getMonth(), habilFuturo.getDayOfMonth(), 10, 15).toString())
                        .param("duracion", "45")
                        .param("puntoRecogida", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/alumno/calendario"))
                .andExpect(flash().attributeExists("error"));

        // Día no laborable (1 de enero)
        this.mockMvc.perform(post("/alumno/clases/reservar")
                        .with(csrf())
                        .header("HX-Request", "true")
                        .param("fechaHora", (LocalDate.now().getYear() + 1) + "-01-01T10:15")
                        .param("duracion", "45")
                        .param("puntoRecogida", "Calle Toledo 10"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"));

        // Fuera de turno MATINAL (16:00)
        this.mockMvc.perform(post("/alumno/clases/reservar")
                        .with(csrf())
                        .header("HX-Request", "true")
                        .param("fechaHora", habilFuturo + "T16:30")
                        .param("duracion", "45")
                        .param("puntoRecogida", "Calle Toledo 10"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"));

        // ReglaNegocioException del servicio (fecha pasada) -> HX-Trigger: actualizarCalendario
        this.mockMvc.perform(post("/alumno/clases/reservar")
                        .with(csrf())
                        .header("HX-Request", "true")
                        .param("fechaHora", LocalDate.now().minusDays(5).with(DayOfWeek.TUESDAY) + "T10:15")
                        .param("duracion", "45")
                        .param("puntoRecogida", "Calle Toledo 10"))
                .andExpect(status().isOk())
                .andExpect(header().string("HX-Trigger", "actualizarCalendario"))
                .andExpect(model().attributeExists("error"));

        // Vehículo del profesor en MANTENIMIENTO
        Alumno alumno = this.alumnoRepository.findByNombreUsuario("alumno1").orElseThrow();
        Vehiculo vehiculo = alumno.getProfesor().getVehiculo();
        vehiculo.setEstado(EstadoVehiculo.MANTENIMIENTO);
        this.vehiculoRepository.saveAndFlush(vehiculo);

        this.mockMvc.perform(post("/alumno/clases/reservar")
                        .with(csrf())
                        .header("HX-Request", "true")
                        .param("fechaHora", habilFuturo + "T10:15")
                        .param("duracion", "45")
                        .param("puntoRecogida", "Calle Toledo 10"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"));

        // Restaurar estado operativo y completar reserva estándar sin HTMX
        vehiculo.setEstado(EstadoVehiculo.OCUPADO);
        this.vehiculoRepository.saveAndFlush(vehiculo);

        this.mockMvc.perform(post("/alumno/clases/reservar")
                        .with(csrf())
                        .param("fechaHora", habilFuturo + "T10:15")
                        .param("duracion", "45")
                        .param("puntoRecogida", "Calle Toledo 10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/alumno/calendario"))
                .andExpect(flash().attributeExists("mensajeExito"));
    }

    @Test
    @WithMockUser(username = "alumno10", roles = "ALUMNO")
    @DisplayName("POST /alumno/clases/reservar valida restricción de turno TARDE para profesor de tarde")
    void testReservarFueraDeTurnoTarde() throws Exception
    {
        LocalDate habilFuturo = LocalDate.now().plusWeeks(3).with(DayOfWeek.THURSDAY);
        this.mockMvc.perform(post("/alumno/clases/reservar")
                        .with(csrf())
                        .header("HX-Request", "true")
                        .param("fechaHora", habilFuturo + "T10:15")
                        .param("duracion", "45")
                        .param("puntoRecogida", "Calle Serrano 102"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"));
    }

    @Test
    @WithMockUser(username = "alumno22", roles = "ALUMNO")
    @DisplayName("POST /alumno/clases/reservar rechaza alumno sin profesor asignado")
    void testReservarSinProfesorAsignado() throws Exception
    {
        LocalDate habilFuturo = LocalDate.now().plusWeeks(3).with(DayOfWeek.THURSDAY);
        this.mockMvc.perform(post("/alumno/clases/reservar")
                        .with(csrf())
                        .header("HX-Request", "true")
                        .param("fechaHora", habilFuturo + "T10:15")
                        .param("duracion", "45")
                        .param("puntoRecogida", "Calle Zurbano 50"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"));
    }

    @Test
    @WithMockUser(username = "alumno23", roles = "ALUMNO")
    @DisplayName("POST /alumno/clases/reservar rechaza alumno sin capacidad de reserva")
    void testReservarSinCapacidad() throws Exception
    {
        LocalDate habilFuturo = LocalDate.now().plusWeeks(3).with(DayOfWeek.THURSDAY);
        this.mockMvc.perform(post("/alumno/clases/reservar")
                        .with(csrf())
                        .header("HX-Request", "true")
                        .param("fechaHora", habilFuturo + "T10:15")
                        .param("duracion", "45")
                        .param("puntoRecogida", "Calle Caracas 8"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"));
    }

    @Test
    @WithMockUser(username = "alumno1", roles = "ALUMNO")
    @DisplayName("POST /alumno/clases/cancelar cubre 404, 403 ajeno, clase RECIBIDA y cancelación estándar")
    void testCancelarClaseAlumnoFlujosCompletos() throws Exception
    {
        // Clase inexistente
        this.mockMvc.perform(post("/alumno/clases/cancelar")
                        .with(csrf())
                        .header("HX-Request", "true")
                        .param("claseId", "99999"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"));

        // Clase de otro alumno (id=49 de alumno2) -> error/403
        this.mockMvc.perform(post("/alumno/clases/cancelar")
                        .with(csrf())
                        .param("claseId", "49"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/403"));

        // Clase ya RECIBIDA (id=35 de alumno1) -> ReglaNegocioException
        this.mockMvc.perform(post("/alumno/clases/cancelar")
                        .with(csrf())
                        .header("HX-Request", "true")
                        .param("claseId", "35"))
                .andExpect(status().isOk())
                .andExpect(header().string("HX-Trigger", "actualizarCalendario"))
                .andExpect(model().attributeExists("error"));

        // Cancelación válida sin HTMX (clase pendiente 37)
        this.mockMvc.perform(post("/alumno/clases/cancelar")
                        .with(csrf())
                        .param("claseId", "37"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/alumno/calendario"))
                .andExpect(flash().attributeExists("mensajeExito"));
    }

    @Test
    @WithMockUser(username = "profesor1", roles = "PROFESOR")
    @DisplayName("Endpoints de profesor para modal, completar ficha técnica y cancelar clase cubren errores y éxitos")
    void testEndpointsProfesorCompletarYCancelar() throws Exception
    {
        this.mockMvc.perform(get("/profesor/clases/99999/modal"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"));

        this.mockMvc.perform(get("/profesor/clases/37/modal"))
                .andExpect(status().isOk())
                .andExpect(view().name("profesor/fragments/modal-ficha-clase :: modalFichaClase"))
                .andExpect(model().attributeExists("kmActualVehiculo", "clase"));

        // Completar clase inexistente (HTMX y no-HTMX)
        this.mockMvc.perform(post("/profesor/clases/completar")
                        .with(csrf())
                        .header("HX-Request", "true")
                        .param("claseId", "99999")
                        .param("kmInicio", "42207")
                        .param("kmFin", "42230"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"));

        this.mockMvc.perform(post("/profesor/clases/completar")
                        .with(csrf())
                        .param("claseId", "99999")
                        .param("kmInicio", "42207")
                        .param("kmFin", "42230"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profesor/calendario"))
                .andExpect(flash().attributeExists("error"));

        // BindingResult con kmFin <= kmInicio en HTMX y no-HTMX
        this.mockMvc.perform(post("/profesor/clases/completar")
                        .with(csrf())
                        .header("HX-Request", "true")
                        .param("claseId", "37")
                        .param("kmInicio", "42207")
                        .param("kmFin", "42100"))
                .andExpect(status().isOk())
                .andExpect(view().name("profesor/fragments/modal-ficha-clase :: modalFichaClase"))
                .andExpect(model().attributeExists("error"));

        this.mockMvc.perform(post("/profesor/clases/completar")
                        .with(csrf())
                        .param("claseId", "37")
                        .param("kmInicio", "42207")
                        .param("kmFin", "42100"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profesor/calendario"))
                .andExpect(flash().attributeExists("error"));

        // ReglaNegocioException del servicio (clase 35 ya RECIBIDA)
        this.mockMvc.perform(post("/profesor/clases/completar")
                        .with(csrf())
                        .header("HX-Request", "true")
                        .param("claseId", "35")
                        .param("kmInicio", "42152")
                        .param("kmFin", "42185"))
                .andExpect(status().isOk())
                .andExpect(view().name("profesor/fragments/modal-ficha-clase :: modalFichaClase"))
                .andExpect(model().attributeExists("error"));

        // Completar válido en flujo estándar (clase 37)
        this.mockMvc.perform(post("/profesor/clases/completar")
                        .with(csrf())
                        .param("claseId", "37")
                        .param("kmInicio", "42207")
                        .param("kmFin", "42235")
                        .param("observaciones", "Buena gestión en incorporaciones."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profesor/calendario"))
                .andExpect(flash().attributeExists("mensajeExito"));

        // Cancelar clase por profesor: error (id inexistente) y éxito estándar (clase 38)
        this.mockMvc.perform(post("/profesor/clases/cancelar")
                        .with(csrf())
                        .header("HX-Request", "true")
                        .param("claseId", "99999")
                        .param("motivo", "Incidencia"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"));

        this.mockMvc.perform(post("/profesor/clases/cancelar")
                        .with(csrf())
                        .param("claseId", "38")
                        .param("motivo", "Reprogramación operativa de la agenda."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profesor/calendario"))
                .andExpect(flash().attributeExists("mensajeExito"));
    }
}
