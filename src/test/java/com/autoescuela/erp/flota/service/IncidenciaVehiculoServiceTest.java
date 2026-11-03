package com.autoescuela.erp.flota.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.autoescuela.erp.core.email.EmailService;
import com.autoescuela.erp.core.enums.EstadoClase;
import com.autoescuela.erp.core.enums.EstadoIncidencia;
import com.autoescuela.erp.core.enums.EstadoVehiculo;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.excepciones.RecursoNoEncontradoException;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.flota.dto.IncidenciaDetalleDTO;
import com.autoescuela.erp.flota.dto.IncidenciaResumenDTO;
import com.autoescuela.erp.flota.mapper.IncidenciaVehiculoMapper;
import com.autoescuela.erp.flota.model.IncidenciaVehiculo;
import com.autoescuela.erp.flota.model.Vehiculo;
import com.autoescuela.erp.flota.repository.IncidenciaVehiculoRepository;
import com.autoescuela.erp.flota.repository.VehiculoRepository;
import com.autoescuela.erp.practicas.model.ClasePractica;
import com.autoescuela.erp.practicas.repository.ClasePracticaRepository;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.model.Profesor;
import com.autoescuela.erp.usuarios.repository.AlumnoRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IncidenciaVehiculoServiceTest
{
    @Mock
    private IncidenciaVehiculoRepository incidenciaVehiculoRepository;

    @Mock
    private VehiculoRepository vehiculoRepository;

    @Mock
    private ClasePracticaRepository clasePracticaRepository;

    @Mock
    private AlumnoRepository alumnoRepository;

    @Mock
    private IncidenciaVehiculoMapper incidenciaVehiculoMapper;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private IncidenciaVehiculoService incidenciaVehiculoService;

    @Test
    @DisplayName("obtenerTodasLasIncidencias recupera la lista ordenada y la transforma a DTOs")
    void testObtenerTodasLasIncidencias()
    {
        IncidenciaVehiculo inc = new IncidenciaVehiculo();
        ReflectionTestUtils.setField(inc, "id", 1L);
        inc.setEstado(EstadoIncidencia.PENDIENTE);

        IncidenciaResumenDTO dto = new IncidenciaResumenDTO(
                1L, LocalDateTime.now(), "Fallo en frenos", EstadoIncidencia.PENDIENTE,
                10L, "1234-BBB", "SEAT", "Ibiza", 20L, "Laura Sánchez"
        );

        when(this.incidenciaVehiculoRepository.findAllOrdenadasPorEstadoYFechaAsc()).thenReturn(List.of(inc));
        when(this.incidenciaVehiculoMapper.toIncidenciaResumenDTO(inc)).thenReturn(dto);

        List<IncidenciaResumenDTO> resultado = this.incidenciaVehiculoService.obtenerTodasLasIncidencias();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).id()).isEqualTo(1L);
        assertThat(resultado.get(0).vehiculoMatricula()).isEqualTo("1234-BBB");
        verify(this.incidenciaVehiculoRepository).findAllOrdenadasPorEstadoYFechaAsc();
        verify(this.incidenciaVehiculoMapper).toIncidenciaResumenDTO(inc);
    }

    @Test
    @DisplayName("contarPorEstado delega correctamente en el repositorio")
    void testContarPorEstado()
    {
        when(this.incidenciaVehiculoRepository.countByEstado(EstadoIncidencia.PENDIENTE)).thenReturn(3L);

        long total = this.incidenciaVehiculoService.contarPorEstado(EstadoIncidencia.PENDIENTE);

        assertThat(total).isEqualTo(3L);
        verify(this.incidenciaVehiculoRepository).countByEstado(EstadoIncidencia.PENDIENTE);
    }

    @Test
    @DisplayName("obtenerIncidenciaParaDetalle retorna el DTO si la incidencia existe")
    void testObtenerIncidenciaParaDetalleExito()
    {
        IncidenciaVehiculo inc = new IncidenciaVehiculo();
        ReflectionTestUtils.setField(inc, "id", 5L);
        inc.setDescripcion("Avería en embrague");

        IncidenciaDetalleDTO dto = new IncidenciaDetalleDTO(
                5L, LocalDateTime.now(), "Avería en embrague", EstadoIncidencia.PENDIENTE,
                2L, "5678-CCC", "Renault", "Clio", 3L, "Carlos López"
        );

        when(this.incidenciaVehiculoRepository.findById(5L)).thenReturn(Optional.of(inc));
        when(this.incidenciaVehiculoMapper.toIncidenciaDetalleDTO(inc)).thenReturn(dto);

        IncidenciaDetalleDTO resultado = this.incidenciaVehiculoService.obtenerIncidenciaParaDetalle(5L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.id()).isEqualTo(5L);
        assertThat(resultado.descripcion()).isEqualTo("Avería en embrague");
        verify(this.incidenciaVehiculoRepository).findById(5L);
        verify(this.incidenciaVehiculoMapper).toIncidenciaDetalleDTO(inc);
    }

    @Test
    @DisplayName("obtenerIncidenciaParaDetalle lanza RecursoNoEncontradoException si no existe")
    void testObtenerIncidenciaParaDetalleNoEncontrada()
    {
        when(this.incidenciaVehiculoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.incidenciaVehiculoService.obtenerIncidenciaParaDetalle(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("gestionarIncidencia transiciona a EN_PROCESO, pone el vehículo en MANTENIMIENTO, cancela clases pendientes y notifica por email")
    void testGestionarIncidenciaTransicionEnProcesoExito()
    {
        Vehiculo vehiculo = new Vehiculo();
        ReflectionTestUtils.setField(vehiculo, "id", 10L);
        vehiculo.setMatricula("1234-LMN");
        vehiculo.setMarca("SEAT");
        vehiculo.setModelo("Ibiza");
        vehiculo.setEstado(EstadoVehiculo.OCUPADO);
        vehiculo.setTipoPermiso(TipoCarnet.PERMISO_B);

        Profesor profesor = new Profesor();
        ReflectionTestUtils.setField(profesor, "id", 20L);
        profesor.setNombre("Laura");
        profesor.setApellidos("Sánchez");
        profesor.setCorreo("laura@autoescuela.es");
        vehiculo.setProfesor(profesor);

        Alumno alumno = new Alumno();
        ReflectionTestUtils.setField(alumno, "id", 30L);
        alumno.setNombre("Mario");
        alumno.setApellidos("Gómez");
        alumno.setCorreo("mario@alumno.es");
        alumno.setProfesor(profesor);

        IncidenciaVehiculo inc = new IncidenciaVehiculo();
        ReflectionTestUtils.setField(inc, "id", 1L);
        inc.setVehiculo(vehiculo);
        inc.setProfesor(profesor);
        inc.setEstado(EstadoIncidencia.PENDIENTE);
        inc.setDescripcion("Pérdida de líquido refrigerante");

        ClasePractica clasePendiente = new ClasePractica();
        ReflectionTestUtils.setField(clasePendiente, "id", 100L);
        clasePendiente.setProfesor(profesor);
        clasePendiente.setAlumno(alumno);
        clasePendiente.setEstadoClase(EstadoClase.PENDIENTE);

        when(this.incidenciaVehiculoRepository.findById(1L)).thenReturn(Optional.of(inc));
        when(this.clasePracticaRepository.findByProfesorAndEstadoClase(profesor, EstadoClase.PENDIENTE)).thenReturn(List.of(clasePendiente));
        when(this.alumnoRepository.findByProfesor(profesor)).thenReturn(List.of(alumno));
        when(this.incidenciaVehiculoRepository.save(inc)).thenReturn(inc);

        IncidenciaVehiculo resultado = this.incidenciaVehiculoService.gestionarIncidencia(1L, EstadoIncidencia.EN_PROCESO);

        assertThat(resultado.getEstado()).isEqualTo(EstadoIncidencia.EN_PROCESO);
        assertThat(vehiculo.getEstado()).isEqualTo(EstadoVehiculo.MANTENIMIENTO);
        assertThat(clasePendiente.getEstadoClase()).isEqualTo(EstadoClase.CANCELADA);

        verify(this.vehiculoRepository).save(vehiculo);
        verify(this.clasePracticaRepository).save(clasePendiente);
        verify(this.incidenciaVehiculoRepository).save(inc);
        verify(this.emailService).enviarNotificacionProfesor(eq("laura@autoescuela.es"), anyString(), anyString());
        verify(this.emailService).enviarNotificacionAlumno(eq("mario@alumno.es"), anyString(), anyString());
    }

    @Test
    @DisplayName("gestionarIncidencia lanza ReglaNegocioException al intentar pasar a EN_PROCESO si la incidencia no está PENDIENTE")
    void testGestionarIncidenciaEnProcesoFallaSiNoEstaPendiente()
    {
        IncidenciaVehiculo inc = new IncidenciaVehiculo();
        ReflectionTestUtils.setField(inc, "id", 2L);
        inc.setEstado(EstadoIncidencia.EN_PROCESO);

        when(this.incidenciaVehiculoRepository.findById(2L)).thenReturn(Optional.of(inc));

        assertThatThrownBy(() -> this.incidenciaVehiculoService.gestionarIncidencia(2L, EstadoIncidencia.EN_PROCESO))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("PENDIENTE");

        verify(this.incidenciaVehiculoRepository, never()).save(any());
        verify(this.vehiculoRepository, never()).save(any());
    }

    @Test
    @DisplayName("gestionarIncidencia transiciona a RESUELTA, restituye a OCUPADO si tiene profesor y envía emails de reapertura")
    void testGestionarIncidenciaTransicionResueltaConProfesor()
    {
        Vehiculo vehiculo = new Vehiculo();
        ReflectionTestUtils.setField(vehiculo, "id", 10L);
        vehiculo.setMatricula("1234-LMN");
        vehiculo.setMarca("SEAT");
        vehiculo.setModelo("Ibiza");
        vehiculo.setEstado(EstadoVehiculo.MANTENIMIENTO);

        Profesor profesor = new Profesor();
        ReflectionTestUtils.setField(profesor, "id", 20L);
        profesor.setNombre("Laura");
        profesor.setCorreo("laura@autoescuela.es");
        vehiculo.setProfesor(profesor);

        Alumno alumno = new Alumno();
        ReflectionTestUtils.setField(alumno, "id", 30L);
        alumno.setNombre("Mario");
        alumno.setCorreo("mario@alumno.es");

        IncidenciaVehiculo inc = new IncidenciaVehiculo();
        ReflectionTestUtils.setField(inc, "id", 3L);
        inc.setVehiculo(vehiculo);
        inc.setProfesor(profesor);
        inc.setEstado(EstadoIncidencia.PENDIENTE);

        when(this.incidenciaVehiculoRepository.findById(3L)).thenReturn(Optional.of(inc));
        when(this.alumnoRepository.findByProfesor(profesor)).thenReturn(List.of(alumno));
        when(this.incidenciaVehiculoRepository.save(inc)).thenReturn(inc);

        IncidenciaVehiculo resultado = this.incidenciaVehiculoService.gestionarIncidencia(3L, EstadoIncidencia.RESUELTA);

        assertThat(resultado.getEstado()).isEqualTo(EstadoIncidencia.RESUELTA);
        assertThat(vehiculo.getEstado()).isEqualTo(EstadoVehiculo.OCUPADO);

        verify(this.vehiculoRepository).save(vehiculo);
        verify(this.incidenciaVehiculoRepository).save(inc);
        verify(this.emailService).enviarNotificacionProfesor(eq("laura@autoescuela.es"), anyString(), anyString());
        verify(this.emailService).enviarNotificacionAlumno(eq("mario@alumno.es"), anyString(), anyString());
    }

    @Test
    @DisplayName("gestionarIncidencia transiciona a RESUELTA y restituye a DISPONIBLE si el vehículo no tiene profesor")
    void testGestionarIncidenciaTransicionResueltaSinProfesor()
    {
        Vehiculo vehiculo = new Vehiculo();
        ReflectionTestUtils.setField(vehiculo, "id", 11L);
        vehiculo.setMatricula("5678-XYZ");
        vehiculo.setMarca("Renault");
        vehiculo.setModelo("Clio");
        vehiculo.setEstado(EstadoVehiculo.MANTENIMIENTO);
        vehiculo.setProfesor(null);

        IncidenciaVehiculo inc = new IncidenciaVehiculo();
        ReflectionTestUtils.setField(inc, "id", 4L);
        inc.setVehiculo(vehiculo);
        inc.setEstado(EstadoIncidencia.PENDIENTE);

        when(this.incidenciaVehiculoRepository.findById(4L)).thenReturn(Optional.of(inc));
        when(this.incidenciaVehiculoRepository.save(inc)).thenReturn(inc);

        IncidenciaVehiculo resultado = this.incidenciaVehiculoService.gestionarIncidencia(4L, EstadoIncidencia.RESUELTA);

        assertThat(resultado.getEstado()).isEqualTo(EstadoIncidencia.RESUELTA);
        assertThat(vehiculo.getEstado()).isEqualTo(EstadoVehiculo.DISPONIBLE);

        verify(this.vehiculoRepository).save(vehiculo);
        verify(this.incidenciaVehiculoRepository).save(inc);
        verify(this.emailService, never()).enviarNotificacionProfesor(anyString(), anyString(), anyString());
        verify(this.emailService, never()).enviarNotificacionAlumno(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("gestionarIncidencia lanza ReglaNegocioException si la incidencia ya no está PENDIENTE (ej. RESUELTA)")
    void testGestionarIncidenciaResueltaFallaSiYaEstaResuelta()
    {
        IncidenciaVehiculo inc = new IncidenciaVehiculo();
        ReflectionTestUtils.setField(inc, "id", 5L);
        inc.setEstado(EstadoIncidencia.RESUELTA);

        when(this.incidenciaVehiculoRepository.findById(5L)).thenReturn(Optional.of(inc));

        assertThatThrownBy(() -> this.incidenciaVehiculoService.gestionarIncidencia(5L, EstadoIncidencia.RESUELTA))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("PENDIENTE");

        verify(this.incidenciaVehiculoRepository, never()).save(any());
        verify(this.vehiculoRepository, never()).save(any());
    }

    @Test
    @DisplayName("gestionarIncidencia lanza ReglaNegocioException si se intenta transicionar a PENDIENTE")
    void testGestionarIncidenciaTransicionInvalida()
    {
        IncidenciaVehiculo inc = new IncidenciaVehiculo();
        ReflectionTestUtils.setField(inc, "id", 6L);
        inc.setEstado(EstadoIncidencia.PENDIENTE);

        when(this.incidenciaVehiculoRepository.findById(6L)).thenReturn(Optional.of(inc));

        assertThatThrownBy(() -> this.incidenciaVehiculoService.gestionarIncidencia(6L, EstadoIncidencia.PENDIENTE))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("PENDIENTE");

        verify(this.incidenciaVehiculoRepository, never()).save(any());
    }
}

