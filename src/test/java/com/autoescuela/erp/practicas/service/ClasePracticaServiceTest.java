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

    @Test
    @DisplayName("cancelarClasePorAlumno cancela con éxito una clase en PENDIENTE con fecha futura")
    void testCancelarClasePorAlumnoExito()
    {
        Alumno alumno = new Alumno();
        org.springframework.test.util.ReflectionTestUtils.setField(alumno, "id", 201L);

        ClasePractica clase = new ClasePractica();
        clase.setAlumno(alumno);
        clase.setEstadoClase(EstadoClase.PENDIENTE);
        clase.setFechaHora(LocalDateTime.now().plusDays(2));

        when(this.clasePracticaRepository.save(any(ClasePractica.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ClasePractica resultado = this.clasePracticaService.cancelarClasePorAlumno(clase, alumno);

        assertThat(resultado.getEstadoClase()).isEqualTo(EstadoClase.CANCELADA);
        verify(this.clasePracticaRepository).save(clase);
    }

    @Test
    @DisplayName("cancelarClasePorAlumno lanza ReglaNegocioException si la clase ya ha comenzado o es pasada")
    void testCancelarClasePorAlumnoFallaPorClaseYaIniciada()
    {
        Alumno alumno = new Alumno();
        org.springframework.test.util.ReflectionTestUtils.setField(alumno, "id", 201L);

        ClasePractica clase = new ClasePractica();
        clase.setAlumno(alumno);
        clase.setEstadoClase(EstadoClase.PENDIENTE);
        clase.setFechaHora(LocalDateTime.now().minusMinutes(10));

        assertThatThrownBy(() -> this.clasePracticaService.cancelarClasePorAlumno(clase, alumno))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("ya ha comenzado o cuya hora de inicio ha transcurrido");
    }

    @Test
    @DisplayName("cancelarClasePorAlumno lanza ReglaNegocioException si la clase no está en estado PENDIENTE")
    void testCancelarClasePorAlumnoFallaPorEstadoNoPendiente()
    {
        Alumno alumno = new Alumno();
        org.springframework.test.util.ReflectionTestUtils.setField(alumno, "id", 201L);

        ClasePractica clase = new ClasePractica();
        clase.setAlumno(alumno);
        clase.setEstadoClase(EstadoClase.RECIBIDA);
        clase.setFechaHora(LocalDateTime.now().plusDays(1));

        assertThatThrownBy(() -> this.clasePracticaService.cancelarClasePorAlumno(clase, alumno))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Solo se pueden cancelar clases que se encuentren en estado PENDIENTE");
    }

    @Test
    @DisplayName("cancelarClasePorAlumno lanza ReglaNegocioException si la clase pertenece a otro alumno")
    void testCancelarClasePorAlumnoFallaPorAlumnoDiferente()
    {
        Alumno alumno1 = new Alumno();
        org.springframework.test.util.ReflectionTestUtils.setField(alumno1, "id", 201L);

        Alumno alumno2 = new Alumno();
        org.springframework.test.util.ReflectionTestUtils.setField(alumno2, "id", 202L);

        ClasePractica clase = new ClasePractica();
        clase.setAlumno(alumno1);
        clase.setEstadoClase(EstadoClase.PENDIENTE);
        clase.setFechaHora(LocalDateTime.now().plusDays(1));

        assertThatThrownBy(() -> this.clasePracticaService.cancelarClasePorAlumno(clase, alumno2))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("No tienes permisos para cancelar una clase práctica de otro alumno");
    }

    @Test
    @DisplayName("reservarClase lanza ReglaNegocioException si la fecha es un festivo nacional")
    void testReservarClaseFallaEnFestivoNacional()
    {
        Alumno alumno = new Alumno();
        Profesor profesor = new Profesor();

        // 12 de octubre (Fiesta Nacional de España)
        LocalDateTime fechaFestivo = LocalDateTime.of(2026, 10, 12, 10, 0);
        ReservaClasePracticaDTO dto = new ReservaClasePracticaDTO(
                fechaFestivo, 45, "Sede Central", null
        );

        assertThatThrownBy(() -> this.clasePracticaService.reservarClase(alumno, profesor, dto))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("días festivos nacionales")
                .hasMessageContaining("Fiesta Nacional de España");
    }

    @Test
    @DisplayName("reservarClase lanza ReglaNegocioException si la fecha está en vacaciones de agosto (1 al 14 de agosto)")
    void testReservarClaseFallaEnVacacionesAgosto()
    {
        Alumno alumno = new Alumno();
        Profesor profesor = new Profesor();

        // 5 de agosto (primera quincena de agosto)
        LocalDateTime fechaAgosto = LocalDateTime.of(2026, 8, 5, 11, 0);
        ReservaClasePracticaDTO dto = new ReservaClasePracticaDTO(
                fechaAgosto, 45, "Sede Central", null
        );

        assertThatThrownBy(() -> this.clasePracticaService.reservarClase(alumno, profesor, dto))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("vacaciones de la autoescuela")
                .hasMessageContaining("Vacaciones de Verano");
    }

    @Test
    @DisplayName("reservarClase lanza ReglaNegocioException si la fecha está en vacaciones de Navidad (23 dic al 7 ene)")
    void testReservarClaseFallaEnVacacionesNavidad()
    {
        Alumno alumno = new Alumno();
        Profesor profesor = new Profesor();

        // 28 de diciembre (periodo navideño)
        LocalDateTime fechaNavidad = LocalDateTime.of(2026, 12, 28, 16, 0);
        ReservaClasePracticaDTO dto = new ReservaClasePracticaDTO(
                fechaNavidad, 45, "Sede Central", null
        );

        assertThatThrownBy(() -> this.clasePracticaService.reservarClase(alumno, profesor, dto))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("vacaciones de la autoescuela")
                .hasMessageContaining("Vacaciones de Navidad");
    }
}
