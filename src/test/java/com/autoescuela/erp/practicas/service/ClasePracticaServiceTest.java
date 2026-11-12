package com.autoescuela.erp.practicas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.autoescuela.erp.academico.model.Matricula;
import com.autoescuela.erp.academico.repository.MatriculaRepository;
import com.autoescuela.erp.core.email.EmailService;
import com.autoescuela.erp.core.enums.EstadoClase;
import com.autoescuela.erp.core.enums.ModalidadMatricula;
import com.autoescuela.erp.core.enums.Rol;
import com.autoescuela.erp.core.enums.TipoExamen;
import com.autoescuela.erp.core.enums.TipoTurno;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.core.util.FechaHoraUtil;
import com.autoescuela.erp.examenes.repository.ExamenRepository;
import com.autoescuela.erp.flota.model.Vehiculo;
import com.autoescuela.erp.practicas.dto.CompletarClaseDTO;
import com.autoescuela.erp.flota.repository.VehiculoRepository;
import com.autoescuela.erp.practicas.dto.ClasePracticaAdminDTO;
import com.autoescuela.erp.practicas.dto.ReservaClasePracticaDTO;
import com.autoescuela.erp.practicas.dto.TramoHorarioDTO;
import com.autoescuela.erp.practicas.mapper.ClasePracticaMapper;
import com.autoescuela.erp.practicas.model.ClasePractica;
import com.autoescuela.erp.practicas.repository.ClasePracticaRepository;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.model.Profesor;
import com.autoescuela.erp.usuarios.repository.AlumnoRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios de ClasePracticaService (Concurrencia FCFS)")
class ClasePracticaServiceTest
{
    @Mock
    private ClasePracticaRepository clasePracticaRepository;

    @Mock
    private ClasePracticaMapper clasePracticaMapper;

    @Mock
    private MatriculaRepository matriculaRepository;

    @Mock
    private ExamenRepository examenRepository;

    @Mock
    private VehiculoRepository vehiculoRepository;

    @Mock
    private AlumnoRepository alumnoRepository;

    @Mock
    private CalendarioService calendarioService;

    @Mock
    private EmailService emailService;

    private ClasePracticaService clasePracticaService;

    @BeforeEach
    void setUp()
    {
        this.clasePracticaService = new ClasePracticaService(
                this.clasePracticaRepository,
                this.clasePracticaMapper,
                this.matriculaRepository,
                this.examenRepository,
                this.vehiculoRepository,
                this.alumnoRepository,
                this.calendarioService,
                this.emailService
        );
    }

    private Matricula crearMatriculaValidaConvocatorias(Alumno alumno, ModalidadMatricula modalidad)
    {
        Matricula matricula = new Matricula();
        matricula.setAlumno(alumno);
        matricula.setEstaActiva(true);
        matricula.setConvocatorias(2);
        matricula.setModalidad(modalidad);
        return matricula;
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

        Matricula matricula = this.crearMatriculaValidaConvocatorias(alumno, ModalidadMatricula.PRACTICA);
        when(this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno)).thenReturn(Optional.of(matricula));

        when(this.clasePracticaRepository.existeClaseActivaOBloqueadaPorProfesor(
                profesor, fechaHora
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

        Matricula matricula = this.crearMatriculaValidaConvocatorias(alumno, ModalidadMatricula.PRACTICA);
        when(this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno)).thenReturn(Optional.of(matricula));

        when(this.clasePracticaRepository.existeClaseActivaOBloqueadaPorProfesor(
                profesor, fechaHora
        )).thenReturn(true);

        assertThatThrownBy(() -> this.clasePracticaService.reservarClase(alumno, profesor, dto))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("ya se encuentra ocupado o cancelado con tu profesor");
    }

    @Test
    @DisplayName("reservarClase lanza ReglaNegocioException si la franja tiene una clase cancelada por el profesor")
    void testReservarClaseFallaPorFranjaCanceladaPorProfesor()
    {
        Alumno alumno = new Alumno();
        Profesor profesor = new Profesor();
        LocalDateTime fechaHora = LocalDateTime.of(2026, 10, 15, 9, 30);
        ReservaClasePracticaDTO dto = new ReservaClasePracticaDTO(
                fechaHora, 45, "Plaza Mayor", ""
        );

        Matricula matricula = this.crearMatriculaValidaConvocatorias(alumno, ModalidadMatricula.PRACTICA);
        when(this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno)).thenReturn(Optional.of(matricula));

        when(this.clasePracticaRepository.existeClaseActivaOBloqueadaPorProfesor(
                profesor, fechaHora
        )).thenReturn(true);

        assertThatThrownBy(() -> this.clasePracticaService.reservarClase(alumno, profesor, dto))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("ya se encuentra ocupado o cancelado con tu profesor");
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

        Matricula matricula = this.crearMatriculaValidaConvocatorias(alumno, ModalidadMatricula.PRACTICA);
        when(this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno)).thenReturn(Optional.of(matricula));

        when(this.clasePracticaRepository.existeClaseActivaOBloqueadaPorProfesor(
                profesor, fechaHora
        )).thenReturn(false);

        when(this.clasePracticaRepository.saveAndFlush(any(ClasePractica.class)))
                .thenThrow(new DataIntegrityViolationException("Unique constraint violation: uq_clase_profesor_activo"));

        assertThatThrownBy(() -> this.clasePracticaService.reservarClase(alumno, profesor, dto))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("acaba de ser reservado por otro alumno");
    }

    @Test
    @DisplayName("reservarClase lanza ReglaNegocioException si el alumno tiene las convocatorias agotadas")
    void testReservarClaseFallaPorConvocatoriasAgotadas()
    {
        Alumno alumno = new Alumno();
        Profesor profesor = new Profesor();
        LocalDateTime fechaHora = LocalDateTime.of(2026, 10, 15, 9, 30);
        ReservaClasePracticaDTO dto = new ReservaClasePracticaDTO(fechaHora, 45, "Plaza Mayor", "");

        Matricula matricula = new Matricula();
        matricula.setAlumno(alumno);
        matricula.setEstaActiva(true);
        matricula.setConvocatorias(0);
        when(this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno)).thenReturn(Optional.of(matricula));

        assertThatThrownBy(() -> this.clasePracticaService.reservarClase(alumno, profesor, dto))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Has agotado las convocatorias oficiales de tu matrícula");
    }

    @Test
    @DisplayName("reservarClase lanza ReglaNegocioException si el alumno en TEORICO_PRACTICA no ha aprobado el examen teórico")
    void testReservarClaseFallaPorTeoricoNoAprobadoEnTeoricoPractica()
    {
        Alumno alumno = new Alumno();
        Profesor profesor = new Profesor();
        LocalDateTime fechaHora = LocalDateTime.of(2026, 10, 15, 9, 30);
        ReservaClasePracticaDTO dto = new ReservaClasePracticaDTO(fechaHora, 45, "Plaza Mayor", "");

        Matricula matricula = this.crearMatriculaValidaConvocatorias(alumno, ModalidadMatricula.TEORICO_PRACTICA);
        when(this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno)).thenReturn(Optional.of(matricula));
        when(this.examenRepository.existsByAlumnoAndTipoAndEsAptoTrue(alumno, TipoExamen.TEORICO)).thenReturn(false);

        assertThatThrownBy(() -> this.clasePracticaService.reservarClase(alumno, profesor, dto))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Es obligatorio haber aprobado el examen teórico oficial");
    }

    @Test
    @DisplayName("reservarClase permite reservar en TEORICO_PRACTICA si el examen teórico ha sido aprobado")
    void testReservarClaseExitoEnTeoricoPracticaConTeoricoAprobado()
    {
        Alumno alumno = new Alumno();
        Profesor profesor = new Profesor();
        profesor.setTurno(TipoTurno.MATINAL);
        LocalDateTime fechaHora = LocalDateTime.of(2026, 10, 15, 9, 30);
        ReservaClasePracticaDTO dto = new ReservaClasePracticaDTO(fechaHora, 45, "Plaza Mayor", "");

        Matricula matricula = this.crearMatriculaValidaConvocatorias(alumno, ModalidadMatricula.TEORICO_PRACTICA);
        when(this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno)).thenReturn(Optional.of(matricula));
        when(this.examenRepository.existsByAlumnoAndTipoAndEsAptoTrue(alumno, TipoExamen.TEORICO)).thenReturn(true);
        when(this.clasePracticaRepository.existeClaseActivaOBloqueadaPorProfesor(profesor, fechaHora)).thenReturn(false);
        when(this.clasePracticaRepository.saveAndFlush(any(ClasePractica.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClasePractica resultado = this.clasePracticaService.reservarClase(alumno, profesor, dto);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getEstadoClase()).isEqualTo(EstadoClase.PENDIENTE);
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
        clase.setFechaHora(FechaHoraUtil.ahora().plusDays(2));

        when(this.clasePracticaRepository.save(any(ClasePractica.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ClasePractica resultado = this.clasePracticaService.cancelarClasePorAlumno(clase, alumno);

        assertThat(resultado.getEstadoClase()).isEqualTo(EstadoClase.CANCELADA);
        assertThat(resultado.getIsCanceladaPorProfesor()).isFalse();
        assertThat(resultado.getIsCancelacionJornada()).isFalse();
        assertThat(resultado.getMotivoCancelacion()).isNull();
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
        clase.setFechaHora(FechaHoraUtil.ahora().minusMinutes(10));

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

    @Test
    @DisplayName("obtenerClasesParaAdmin recupera las clases y delega el mapeo al ClasePracticaMapper")
    void testObtenerClasesParaAdminExito()
    {
        Long profesorId = 1L;
        EstadoClase estado = EstadoClase.RECIBIDA;
        ClasePractica clase = new ClasePractica();
        List<ClasePractica> listaEntidades = List.of(clase);

        ClasePracticaAdminDTO dto = new ClasePracticaAdminDTO(
                10L, LocalDateTime.now(), 45, "Sede Central", 100L, 150L, "100 a 150 km (+50 km)",
                EstadoClase.RECIBIDA, 2L, "Juan Perez", "juanp", 1L, "Laura Gomez", "laurag",
                "1234-BBB", "Seat Ibiza", "Seat Ibiza (1234-BBB)", null, null, "Sin notas", null
        );
        List<ClasePracticaAdminDTO> listaDTOs = List.of(dto);

        when(this.clasePracticaRepository.buscarClasesParaAdmin(profesorId, estado)).thenReturn(listaEntidades);
        when(this.clasePracticaMapper.toClasePracticaAdminDTOList(listaEntidades)).thenReturn(listaDTOs);

        List<ClasePracticaAdminDTO> resultado = this.clasePracticaService.obtenerClasesParaAdmin(profesorId, estado);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).id()).isEqualTo(10L);
        verify(this.clasePracticaRepository).buscarClasesParaAdmin(profesorId, estado);
        verify(this.clasePracticaMapper).toClasePracticaAdminDTOList(listaEntidades);
    }

    @Test
    @DisplayName("obtenerClaseParaDetalleAdmin recupera la clase con relaciones y la proyecta con ClasePracticaMapper")
    void testObtenerClaseParaDetalleAdminExito()
    {
        Long id = 5L;
        ClasePractica clase = new ClasePractica();
        ClasePracticaAdminDTO dto = new ClasePracticaAdminDTO(
                id, LocalDateTime.now(), 45, "Plaza Norte", 200L, 240L, "200 a 240 km (+40 km)",
                EstadoClase.RECIBIDA, 3L, "Mario Casas", "marioc", 1L, "Laura Gomez", "laurag",
                "1234-BBB", "Seat Ibiza", "Seat Ibiza (1234-BBB)", null, null, null, null
        );

        when(this.clasePracticaRepository.findByIdConDetalles(id)).thenReturn(Optional.of(clase));
        when(this.clasePracticaMapper.toClasePracticaAdminDTO(clase)).thenReturn(dto);

        ClasePracticaAdminDTO resultado = this.clasePracticaService.obtenerClaseParaDetalleAdmin(id);

        assertThat(resultado).isNotNull();
        assertThat(resultado.id()).isEqualTo(id);
        verify(this.clasePracticaRepository).findByIdConDetalles(id);
        verify(this.clasePracticaMapper).toClasePracticaAdminDTO(clase);
    }

    @Test
    @DisplayName("obtenerClaseParaDetalleAdmin lanza ReglaNegocioException si el id es nulo")
    void testObtenerClaseParaDetalleAdminIdNulo()
    {
        assertThatThrownBy(() -> this.clasePracticaService.obtenerClaseParaDetalleAdmin(null))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("no puede ser nulo");
    }

    @Test
    @DisplayName("obtenerClaseParaDetalleAdmin lanza ReglaNegocioException si la clase no existe")
    void testObtenerClaseParaDetalleAdminNoEncontrada()
    {
        Long id = 99L;
        when(this.clasePracticaRepository.findByIdConDetalles(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.clasePracticaService.obtenerClaseParaDetalleAdmin(id))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("No se ha encontrado la clase práctica solicitada");
    }

    @Test
    @DisplayName("completarClasePractica lanza ReglaNegocioException si la clase no ha alcanzado la hora de finalización")
    void testCompletarClasePracticaAntesDeFinLanzaExcepcion()
    {
        Long id = 10L;
        ClasePractica clase = new ClasePractica();
        clase.setFechaHora(FechaHoraUtil.ahora().plusHours(2));
        clase.setDuracion(45);
        clase.setEstadoClase(EstadoClase.PENDIENTE);

        when(this.clasePracticaRepository.findById(id)).thenReturn(Optional.of(clase));

        CompletarClaseDTO dto = new CompletarClaseDTO(id, 1000L, 1030L, "Sesión de aparcamiento");

        assertThatThrownBy(() -> this.clasePracticaService.completarClasePractica(dto))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("antes de su fecha y hora de finalización");
    }

    @Test
    @DisplayName("completarClasePractica completa la clase si ya ha alcanzado la hora de finalización")
    void testCompletarClasePracticaTrasFinExito()
    {
        Long id = 10L;
        ClasePractica clase = new ClasePractica();
        clase.setFechaHora(FechaHoraUtil.ahora().minusMinutes(60));
        clase.setDuracion(45);
        clase.setEstadoClase(EstadoClase.PENDIENTE);

        when(this.clasePracticaRepository.findById(id)).thenReturn(Optional.of(clase));
        when(this.clasePracticaRepository.save(any(ClasePractica.class))).thenAnswer(i -> i.getArgument(0));

        CompletarClaseDTO dto = new CompletarClaseDTO(id, 1000L, 1030L, "Sesión en autovía");

        ClasePractica resultado = this.clasePracticaService.completarClasePractica(dto);

        assertThat(resultado.getEstadoClase()).isEqualTo(EstadoClase.RECIBIDA);
        assertThat(resultado.getKmInicio()).isEqualTo(1000L);
        assertThat(resultado.getKmFin()).isEqualTo(1030L);
    }

    @Test
    @DisplayName("reservarClase rechaza alumno sin matrícula activa y bloquea días con jornada cancelada")
    void testReservarClaseSinMatriculaYConJornadaCancelada()
    {
        Profesor profesor = new Profesor();
        profesor.setTurno(TipoTurno.MATINAL);
        Alumno alumno = new Alumno();
        LocalDateTime fechaLaborable = LocalDateTime.of(2026, 10, 14, 10, 15);
        ReservaClasePracticaDTO dto = new ReservaClasePracticaDTO(fechaLaborable, null, null, null);

        when(this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> this.clasePracticaService.reservarClase(alumno, profesor, dto))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("matrícula activo");

        Matricula matricula = new Matricula();
        matricula.setEstaActiva(true);
        matricula.setConvocatorias(2);
        matricula.setConvocatoriasGastadas(0);
        matricula.setModalidad(ModalidadMatricula.PRACTICA);
        when(this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno)).thenReturn(Optional.of(matricula));

        ClasePractica cancelacionJornada = new ClasePractica();
        cancelacionJornada.setEstadoClase(EstadoClase.CANCELADA);
        cancelacionJornada.setIsCanceladaPorProfesor(true);
        cancelacionJornada.setObservaciones("Cancelación de jornada por formación");
        when(this.clasePracticaRepository.findByProfesorAndFechaHoraBetweenOrderByFechaHoraAsc(any(), any(), any()))
                .thenReturn(List.of(cancelacionJornada));

        assertThatThrownBy(() -> this.clasePracticaService.reservarClase(alumno, profesor, dto))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("jornada seleccionada ha sido cancelada");
    }

    @Test
    @DisplayName("reservarClase asigna valores por defecto cuando duración, puntoRecogida u observaciones son nulos y copia km del vehículo")
    void testReservarClaseCamposOpcionalesNulosYKmVehiculo()
    {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setKm(54321L);
        Profesor profesor = new Profesor();
        profesor.setTurno(TipoTurno.MATINAL);
        profesor.setVehiculo(vehiculo);

        Alumno alumno = new Alumno();
        Matricula matricula = new Matricula();
        matricula.setEstaActiva(true);
        matricula.setConvocatorias(2);
        matricula.setConvocatoriasGastadas(0);
        matricula.setModalidad(ModalidadMatricula.PRACTICA);

        LocalDateTime fechaLaborable = LocalDateTime.of(2026, 10, 14, 11, 0);
        ReservaClasePracticaDTO dto = new ReservaClasePracticaDTO(fechaLaborable, null, null, null);

        when(this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno)).thenReturn(Optional.of(matricula));
        when(this.clasePracticaRepository.findByProfesorAndFechaHoraBetweenOrderByFechaHoraAsc(any(), any(), any()))
                .thenReturn(List.of());
        when(this.clasePracticaRepository.existeClaseActivaOBloqueadaPorProfesor(profesor, fechaLaborable))
                .thenReturn(false);
        when(this.clasePracticaRepository.saveAndFlush(any(ClasePractica.class))).thenAnswer(i -> i.getArgument(0));

        ClasePractica guardada = this.clasePracticaService.reservarClase(alumno, profesor, dto);

        assertThat(guardada.getDuracion()).isEqualTo(45);
        assertThat(guardada.getPuntoRecogida()).isEmpty();
        assertThat(guardada.getObservaciones()).isEmpty();
        assertThat(guardada.getKmInicio()).isEqualTo(54321L);
        assertThat(guardada.getKmFin()).isEqualTo(54321L);
    }

    @Test
    @DisplayName("cancelarClasePorAlumno lanza excepción si la clase es nula")
    void testCancelarClasePorAlumnoClaseNula()
    {
        assertThatThrownBy(() -> this.clasePracticaService.cancelarClasePorAlumno(null, new Alumno()))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("No se ha encontrado la clase");
    }

    @Test
    @DisplayName("completarClasePractica valida dto/kmFin y actualiza odómetro del vehículo y saldo del alumno")
    void testCompletarClasePracticaValidacionesYActualizaciones()
    {
        assertThatThrownBy(() -> this.clasePracticaService.completarClasePractica(null))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> this.clasePracticaService.completarClasePractica(new CompletarClaseDTO(null, 10L, 20L, "")))
                .isInstanceOf(ReglaNegocioException.class);

        Long id = 22L;
        ClasePractica claseInvalidKm = new ClasePractica();
        claseInvalidKm.setFechaHora(FechaHoraUtil.ahora().minusHours(2));
        claseInvalidKm.setDuracion(45);
        when(this.clasePracticaRepository.findById(id)).thenReturn(Optional.of(claseInvalidKm));

        assertThatThrownBy(() -> this.clasePracticaService.completarClasePractica(new CompletarClaseDTO(id, 500L, 500L, "Igual km")))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("estrictamente superior");

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setKm(1000L);
        Profesor profesor = new Profesor();
        profesor.setVehiculo(vehiculo);
        Alumno alumno = new Alumno();
        Matricula matricula = new Matricula();
        matricula.setEstaActiva(true);
        matricula.setSaldoClases(4);

        ClasePractica clase = new ClasePractica();
        clase.setFechaHora(FechaHoraUtil.ahora().minusHours(2));
        clase.setDuracion(null);
        clase.setProfesor(profesor);
        clase.setAlumno(alumno);
        clase.setEstadoClase(EstadoClase.PENDIENTE);

        when(this.clasePracticaRepository.findById(33L)).thenReturn(Optional.of(clase));
        when(this.clasePracticaRepository.save(any(ClasePractica.class))).thenAnswer(i -> i.getArgument(0));
        when(this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno)).thenReturn(Optional.of(matricula));

        this.clasePracticaService.completarClasePractica(new CompletarClaseDTO(33L, 1000L, 1045L, "Buena sesión"));

        assertThat(vehiculo.getKm()).isEqualTo(1045L);
        assertThat(matricula.getSaldoClases()).isEqualTo(3);
        verify(this.vehiculoRepository).save(vehiculo);
        verify(this.matriculaRepository).save(matricula);
    }

    @Test
    @DisplayName("cancelarClasePorProfesor valida ID, cancela con motivo, notifica por email y tolera fallo de correo")
    void testCancelarClasePorProfesorCompletoYFallbackEmail()
    {
        assertThatThrownBy(() -> this.clasePracticaService.cancelarClasePorProfesor(null, "Motivo"))
                .isInstanceOf(ReglaNegocioException.class);

        Alumno alumno = new Alumno();
        alumno.setNombre("Elena");
        alumno.setCorreo("elena@mands.es");

        ClasePractica clase = new ClasePractica();
        clase.setAlumno(alumno);
        clase.setFechaHora(LocalDateTime.of(2026, 10, 14, 10, 15));
        clase.setEstadoClase(EstadoClase.PENDIENTE);

        when(this.clasePracticaRepository.findById(44L)).thenReturn(Optional.of(clase));
        when(this.clasePracticaRepository.save(any(ClasePractica.class))).thenAnswer(i -> i.getArgument(0));

        ClasePractica cancelada = this.clasePracticaService.cancelarClasePorProfesor(44L, "  Avería puntual  ");
        assertThat(cancelada.getEstadoClase()).isEqualTo(EstadoClase.CANCELADA);
        assertThat(cancelada.getIsCanceladaPorProfesor()).isTrue();
        assertThat(cancelada.getMotivoCancelacion()).isEqualTo("Avería puntual");
        verify(this.emailService).enviarNotificacionCorreo(anyString(), anyString(), anyString(), anyString(), any(Rol.class));

        doThrow(new RuntimeException("SMTP offline")).when(this.emailService)
                .enviarNotificacionCorreo(anyString(), anyString(), anyString(), anyString(), any(Rol.class));
        ClasePractica canceladaConFalloMail = this.clasePracticaService.cancelarClasePorProfesor(44L, "   ");
        assertThat(canceladaConFalloMail.getEstadoClase()).isEqualTo(EstadoClase.CANCELADA);
    }

    @Test
    @DisplayName("cancelarJornadaProfesor valida parámetros, bloquea doble cancelación, cancela pendientes y notifica alumnos")
    void testCancelarJornadaProfesorCompleto()
    {
        LocalDate fecha = LocalDate.of(2026, 10, 15);
        assertThatThrownBy(() -> this.clasePracticaService.cancelarJornadaProfesor(null, fecha, "Motivo"))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> this.clasePracticaService.cancelarJornadaProfesor(new Profesor(), null, "Motivo"))
                .isInstanceOf(ReglaNegocioException.class);

        Profesor profesor = new Profesor();
        profesor.setTurno(TipoTurno.MATINAL);

        ClasePractica yaCanceladaJornada = new ClasePractica();
        yaCanceladaJornada.setEstadoClase(EstadoClase.CANCELADA);
        yaCanceladaJornada.setIsCancelacionJornada(true);
        when(this.clasePracticaRepository.findByProfesorAndFechaHoraBetweenOrderByFechaHoraAsc(any(), any(), any()))
                .thenReturn(List.of(yaCanceladaJornada));

        assertThatThrownBy(() -> this.clasePracticaService.cancelarJornadaProfesor(profesor, fecha, "Repetición"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("ya ha sido cancelada previamente");

        ClasePractica pendiente = new ClasePractica();
        pendiente.setEstadoClase(EstadoClase.PENDIENTE);
        pendiente.setFechaHora(fecha.atTime(8, 0));

        List<TramoHorarioDTO> tramos = List.of(
                new TramoHorarioDTO(LocalTime.of(8, 0), LocalTime.of(8, 45), "08:00", "08:00 - 08:45"),
                new TramoHorarioDTO(LocalTime.of(8, 45), LocalTime.of(9, 30), "08:45", "08:45 - 09:30")
        );

        Alumno alumnoConCorreo = new Alumno();
        alumnoConCorreo.setNombre("Mario");
        alumnoConCorreo.setCorreo("mario@mands.es");
        Alumno alumnoSinCorreo = new Alumno();
        alumnoSinCorreo.setNombre("SinCorreo");

        when(this.clasePracticaRepository.findByProfesorAndFechaHoraBetweenOrderByFechaHoraAsc(any(), any(), any()))
                .thenReturn(List.of(pendiente));
        when(this.calendarioService.generarTramos(TipoTurno.MATINAL)).thenReturn(tramos);
        when(this.alumnoRepository.findByProfesor(profesor)).thenReturn(List.of(alumnoConCorreo, alumnoSinCorreo));
        doThrow(new RuntimeException("SMTP apagado")).when(this.emailService)
                .enviarNotificacionCorreo(anyString(), anyString(), anyString(), anyString(), any(Rol.class));

        int total = this.clasePracticaService.cancelarJornadaProfesor(profesor, fecha, "  Asunto médico ");

        assertThat(total).isEqualTo(1);
        assertThat(pendiente.getEstadoClase()).isEqualTo(EstadoClase.CANCELADA);
        assertThat(pendiente.getIsCancelacionJornada()).isTrue();
        // 1 pendiente actualizada + 1 tramo libre bloqueado
        verify(this.clasePracticaRepository, org.mockito.Mockito.times(2)).save(any(ClasePractica.class));
    }

    @Test
    @DisplayName("Métodos de consulta auxiliares gestionan nulos y delegan correctamente en el repositorio")
    void testConsultasAuxiliaresYGuardar()
    {
        assertThat(this.clasePracticaService.buscarPorId(null)).isEmpty();
        assertThat(this.clasePracticaService.contarClasesPendientesPorAlumno(null)).isZero();
        assertThat(this.clasePracticaService.contarClasesRecibidasPorAlumno(null)).isZero();
        assertThat(this.clasePracticaService.obtenerClasesPorAlumnoAsc(null)).isEmpty();
        assertThat(this.clasePracticaService.obtenerClasesPorAlumnoDesc(null)).isEmpty();
        assertThat(this.clasePracticaService.obtenerClasesPorAlumnoExcluyendoEstado(null, EstadoClase.CANCELADA)).isEmpty();
        assertThat(this.clasePracticaService.obtenerClasesPorProfesorAsc(null)).isEmpty();
        assertThat(this.clasePracticaService.obtenerClasesPorProfesorYRango(null, LocalDateTime.now(), LocalDateTime.now())).isEmpty();

        Alumno alumno = new Alumno();
        Profesor profesor = new Profesor();
        ClasePractica clase = new ClasePractica();
        LocalDateTime inicio = LocalDateTime.of(2026, 10, 1, 8, 0);
        LocalDateTime fin = LocalDateTime.of(2026, 10, 31, 22, 0);

        when(this.clasePracticaRepository.findById(5L)).thenReturn(Optional.of(clase));
        when(this.clasePracticaRepository.countByAlumnoAndEstadoClase(alumno, EstadoClase.PENDIENTE)).thenReturn(2);
        when(this.clasePracticaRepository.countByAlumnoAndEstadoClase(alumno, EstadoClase.RECIBIDA)).thenReturn(7);
        when(this.clasePracticaRepository.findByAlumnoOrderByFechaHoraAsc(alumno)).thenReturn(List.of(clase));
        when(this.clasePracticaRepository.findByAlumnoOrderByFechaHoraDesc(alumno)).thenReturn(List.of(clase));
        when(this.clasePracticaRepository.findByAlumnoAndEstadoClaseNot(alumno, EstadoClase.CANCELADA)).thenReturn(List.of(clase));
        when(this.clasePracticaRepository.findByProfesorOrderByFechaHoraAsc(profesor)).thenReturn(List.of(clase));
        when(this.clasePracticaRepository.findByProfesorAndFechaHoraBetweenOrderByFechaHoraAsc(profesor, inicio, fin)).thenReturn(List.of(clase));
        when(this.clasePracticaRepository.save(clase)).thenReturn(clase);

        assertThat(this.clasePracticaService.buscarPorId(5L)).contains(clase);
        assertThat(this.clasePracticaService.contarClasesPendientesPorAlumno(alumno)).isEqualTo(2);
        assertThat(this.clasePracticaService.contarClasesRecibidasPorAlumno(alumno)).isEqualTo(7L);
        assertThat(this.clasePracticaService.obtenerClasesPorAlumnoAsc(alumno)).hasSize(1);
        assertThat(this.clasePracticaService.obtenerClasesPorAlumnoDesc(alumno)).hasSize(1);
        assertThat(this.clasePracticaService.obtenerClasesPorAlumnoExcluyendoEstado(alumno, EstadoClase.CANCELADA)).hasSize(1);
        assertThat(this.clasePracticaService.obtenerClasesPorProfesorAsc(profesor)).hasSize(1);
        assertThat(this.clasePracticaService.obtenerClasesPorProfesorYRango(profesor, inicio, fin)).hasSize(1);
        assertThat(this.clasePracticaService.guardar(clase)).isSameAs(clase);
    }
}
