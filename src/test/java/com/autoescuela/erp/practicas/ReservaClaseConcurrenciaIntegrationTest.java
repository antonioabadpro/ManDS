package com.autoescuela.erp.practicas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.autoescuela.erp.BaseIntegrationTest;
import com.autoescuela.erp.core.enums.EstadoClase;
import com.autoescuela.erp.practicas.model.ClasePractica;
import com.autoescuela.erp.practicas.repository.ClasePracticaRepository;
import com.autoescuela.erp.usuarios.model.Profesor;
import com.autoescuela.erp.usuarios.repository.ProfesorRepository;

/**
 * Pruebas de integración para la concurrencia de reservas First-Come, First-Served (CU-034).
 * Valida la protección contra solapamiento de horarios entre alumnos del mismo profesor,
 * y la liberación de la franja horaria para nuevas reservas tras una cancelación.
 */
class ReservaClaseConcurrenciaIntegrationTest extends BaseIntegrationTest
{
    @Autowired
    private ClasePracticaRepository clasePracticaRepository;

    @Autowired
    private ProfesorRepository profesorRepository;

    @Test
    @DisplayName("Concurrencia FCFS: el segundo alumno que intenta reservar el mismo tramo recibe error y no solapa horario")
    void testColisionConcurrenteMismoProfesorYTramo() throws Exception
    {
        String fechaHoraSlot = "2026-11-20T10:15";

        // 1. Alumno1 (Jose López) reserva el tramo con éxito
        this.mockMvc.perform(post("/alumno/clases/reservar")
                .with(csrf())
                .with(user("alumno1").roles("ALUMNO"))
                .header("HX-Request", "true")
                .param("fechaHora", fechaHoraSlot)
                .param("duracion", "45")
                .param("puntoRecogida", "Sede Central")
                .param("observaciones", "Primera reserva"))
                .andExpect(status().isOk())
                .andExpect(view().name("alumno/fragments/alerta-feedback :: feedbackExito"))
                .andExpect(model().attributeExists("mensajeExito"))
                .andExpect(header().string("HX-Trigger", "actualizarCalendario"));

        // 2. Alumno2 (David Ruiz, mismo profesor id=100) intenta reservar el mismo tramo exacto
        this.mockMvc.perform(post("/alumno/clases/reservar")
                .with(csrf())
                .with(user("alumno2").roles("ALUMNO"))
                .header("HX-Request", "true")
                .param("fechaHora", fechaHoraSlot)
                .param("duracion", "45")
                .param("puntoRecogida", "Plaza Castilla")
                .param("observaciones", "Intento colisionante"))
                .andExpect(status().isOk())
                .andExpect(view().name("alumno/fragments/alerta-feedback :: feedbackExito"))
                .andExpect(model().attributeExists("error"))
                .andExpect(header().string("HX-Trigger", "actualizarCalendario"));

        // 3. Verificación de BD: solo existe 1 clase activa para ese profesor y fecha
        Profesor profesor = this.profesorRepository.findById(100L).orElseThrow();
        LocalDateTime fechaHoraParsed = LocalDateTime.parse(fechaHoraSlot);
        List<ClasePractica> clasesEnTramo = this.clasePracticaRepository.findByProfesorAndFechaHoraBetweenOrderByFechaHoraAsc(
                profesor, fechaHoraParsed.minusMinutes(1), fechaHoraParsed.plusMinutes(1)
        );

        assertThat(clasesEnTramo).hasSize(1);
        assertThat(clasesEnTramo.get(0).getAlumno().getNombreUsuario()).isEqualTo("alumno1");
        assertThat(clasesEnTramo.get(0).getEstadoClase()).isEqualTo(EstadoClase.PENDIENTE);
    }

    @Test
    @DisplayName("Liberación de tramo: al cancelar una clase previa, otro alumno puede reservar esa misma franja horaria")
    void testCancelacionLiberaTramoParaNuevaReserva() throws Exception
    {
        String fechaHoraSlot = "2026-11-21T09:30";

        // 1. Alumno1 reserva la franja
        this.mockMvc.perform(post("/alumno/clases/reservar")
                .with(csrf())
                .with(user("alumno1").roles("ALUMNO"))
                .header("HX-Request", "true")
                .param("fechaHora", fechaHoraSlot)
                .param("duracion", "45")
                .param("puntoRecogida", "Sede Central")
                .param("observaciones", "Reserva a cancelar"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("mensajeExito"));

        // Obtener la clase creada
        Profesor profesor = this.profesorRepository.findById(100L).orElseThrow();
        LocalDateTime fechaHoraParsed = LocalDateTime.parse(fechaHoraSlot);
        List<ClasePractica> clasesIniciales = this.clasePracticaRepository.findByProfesorAndFechaHoraBetweenOrderByFechaHoraAsc(
                profesor, fechaHoraParsed.minusMinutes(1), fechaHoraParsed.plusMinutes(1)
        );
        assertThat(clasesIniciales).hasSize(1);
        Long claseId = clasesIniciales.get(0).getId();

        // 2. Alumno1 cancela la clase
        this.mockMvc.perform(post("/alumno/clases/cancelar")
                .with(csrf())
                .with(user("alumno1").roles("ALUMNO"))
                .header("HX-Request", "true")
                .param("claseId", claseId.toString()))
                .andExpect(status().isOk())
                .andExpect(header().string("HX-Trigger", "actualizarCalendario"));

        // 3. Alumno2 reserva ahora la misma franja horaria liberada
        this.mockMvc.perform(post("/alumno/clases/reservar")
                .with(csrf())
                .with(user("alumno2").roles("ALUMNO"))
                .header("HX-Request", "true")
                .param("fechaHora", fechaHoraSlot)
                .param("duracion", "45")
                .param("puntoRecogida", "Plaza Castilla")
                .param("observaciones", "Reserva en franja liberada"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("mensajeExito"))
                .andExpect(header().string("HX-Trigger", "actualizarCalendario"));

        // 4. Verificación de BD: Coexisten la cancelada histórica y la nueva activa sin violar unicidad
        List<ClasePractica> clasesFinales = this.clasePracticaRepository.findByProfesorAndFechaHoraBetweenOrderByFechaHoraAsc(
                profesor, fechaHoraParsed.minusMinutes(1), fechaHoraParsed.plusMinutes(1)
        );
        assertThat(clasesFinales).hasSize(2);

        long clasesCanceladas = clasesFinales.stream()
                .filter(c -> c.getEstadoClase() == EstadoClase.CANCELADA)
                .count();
        long clasesActivas = clasesFinales.stream()
                .filter(c -> c.getEstadoClase() == EstadoClase.PENDIENTE)
                .count();

        assertThat(clasesCanceladas).isEqualTo(1);
        assertThat(clasesActivas).isEqualTo(1);
    }
}
