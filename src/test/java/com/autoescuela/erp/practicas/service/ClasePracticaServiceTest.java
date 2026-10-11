package com.autoescuela.erp.practicas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.autoescuela.erp.core.enums.EstadoClase;
import com.autoescuela.erp.core.enums.TipoTurno;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.flota.model.Vehiculo;
import com.autoescuela.erp.practicas.dto.ReservaClasePracticaDTO;
import com.autoescuela.erp.practicas.model.ClasePractica;
import com.autoescuela.erp.practicas.repository.ClasePracticaRepository;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.model.Profesor;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios de ClasePracticaService (Concurrencia FCFS)")
class ClasePracticaServiceTest
{
    @Mock
    private ClasePracticaRepository clasePracticaRepository;

    private ClasePracticaService clasePracticaService;

    @BeforeEach
    void setUp()
    {
        this.clasePracticaService = new ClasePracticaService(this.clasePracticaRepository);
    }

    @Test
    @DisplayName("reservarClase persiste la reserva con éxito cuando la franja horaria está libre")
    void testReservarClaseExito()
    {
        Alumno alumno = new Alumno();
        alumno.setNombre("Carlos");

        Profesor profesor = new Profesor();
        profesor.setNombre("Laura");
        profesor.setTurno(TipoTurno.MATINAL);
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setKm(45000L);
        profesor.setVehiculo(vehiculo);

        LocalDateTime fechaHora = LocalDateTime.of(2026, 10, 15, 9, 30);
        ReservaClasePracticaDTO dto = new ReservaClasePracticaDTO(
                fechaHora, 45, "Plaza Mayor", "Práctica de estacionamiento"
        );

        when(this.clasePracticaRepository.existsByProfesorAndFechaHoraAndEstadoClaseNot(
                profesor, fechaHora, EstadoClase.CANCELADA
        )).thenReturn(false);

        when(this.clasePracticaRepository.saveAndFlush(any(ClasePractica.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ClasePractica resultado = this.clasePracticaService.reservarClase(alumno, profesor, dto);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getAlumno()).isEqualTo(alumno);
        assertThat(resultado.getProfesor()).isEqualTo(profesor);
        assertThat(resultado.getFechaHora()).isEqualTo(fechaHora);
        assertThat(resultado.getEstadoClase()).isEqualTo(EstadoClase.PENDIENTE);
        assertThat(resultado.getPuntoRecogida()).isEqualTo("Plaza Mayor");
        assertThat(resultado.getKmInicio()).isEqualTo(45000);

        verify(this.clasePracticaRepository).saveAndFlush(any(ClasePractica.class));
    }

    @Test
    @DisplayName("reservarClase lanza ReglaNegocioException si la franja ya está ocupada por comprobación preventiva")
    void testReservarClaseFallaPorHorarioOcupadoPreventivo()
    {
        Alumno alumno = new Alumno();
        Profesor profesor = new Profesor();
        LocalDateTime fechaHora = LocalDateTime.of(2026, 10, 15, 9, 30);
        ReservaClasePracticaDTO dto = new ReservaClasePracticaDTO(
                fechaHora, 45, "Plaza Mayor", ""
        );

        when(this.clasePracticaRepository.existsByProfesorAndFechaHoraAndEstadoClaseNot(
                profesor, fechaHora, EstadoClase.CANCELADA
        )).thenReturn(true);

        assertThatThrownBy(() -> this.clasePracticaService.reservarClase(alumno, profesor, dto))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("ya se encuentra ocupado con tu profesor");
    }

    @Test
    @DisplayName("reservarClase captura DataIntegrityViolationException y la traduce a ReglaNegocioException ante colisión concurrente")
    void testReservarClaseFallaPorColisionConcurrenteEnBaseDeDatos()
    {
        Alumno alumno = new Alumno();
        Profesor profesor = new Profesor();
        LocalDateTime fechaHora = LocalDateTime.of(2026, 10, 15, 9, 30);
        ReservaClasePracticaDTO dto = new ReservaClasePracticaDTO(
                fechaHora, 45, "Plaza Mayor", ""
        );

        when(this.clasePracticaRepository.existsByProfesorAndFechaHoraAndEstadoClaseNot(
                profesor, fechaHora, EstadoClase.CANCELADA
        )).thenReturn(false);

        when(this.clasePracticaRepository.saveAndFlush(any(ClasePractica.class)))
                .thenThrow(new DataIntegrityViolationException("Unique constraint violation: uq_clase_profesor_activo"));

        assertThatThrownBy(() -> this.clasePracticaService.reservarClase(alumno, profesor, dto))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("acaba de ser reservado por otro alumno");
    }
}
