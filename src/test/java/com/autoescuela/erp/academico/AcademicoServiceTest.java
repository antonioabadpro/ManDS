package com.autoescuela.erp.academico;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.autoescuela.erp.academico.model.Matricula;
import com.autoescuela.erp.academico.repository.MatriculaRepository;
import com.autoescuela.erp.academico.service.AcademicoService;
import com.autoescuela.erp.core.email.EmailService;
import com.autoescuela.erp.core.enums.EstadoUsuario;
import com.autoescuela.erp.core.enums.ModalidadMatricula;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.enums.TipoMatricula;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.repository.AlumnoRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Suite de pruebas unitarias para el servicio de gestión académica (AcademicoService).
 * Valida la formalización de matrículas tras la confirmación de cobros desde Stripe,
 * garantizando la idempotencia ante reintentos de webhooks y el cumplimiento de reglas de negocio.
 */
@ExtendWith(MockitoExtension.class)
class AcademicoServiceTest
{
    @Mock
    private AlumnoRepository alumnoRepository;

    @Mock
    private MatriculaRepository matriculaRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AcademicoService academicoService;

    @Test
    @DisplayName("matricularTrasPago crea y persiste una nueva matrícula activa con 2 convocatorias para carnet estándar")
    void testMatricularTrasPagoExitoso()
    {
        Long alumnoId = 201L;
        Alumno alumno = new Alumno();
        ReflectionTestUtils.setField(alumno, "id", alumnoId);
        alumno.setDni("12345678Z");

        when(this.alumnoRepository.findById(alumnoId)).thenReturn(Optional.of(alumno));
        when(this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno)).thenReturn(Optional.empty());
        when(this.matriculaRepository.save(any(Matricula.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Matricula resultado = this.academicoService.matricularTrasPago(alumnoId, "PERMISO_B", ModalidadMatricula.TEORICO_PRACTICA, 250.00f);

        assertNotNull(resultado);
        assertTrue(resultado.getEstaActiva());
        assertEquals(TipoCarnet.PERMISO_B, resultado.getPermisoCarnet());
        assertEquals(2, resultado.getConvocatorias());
        assertEquals(0, resultado.getSaldoClases());
        assertEquals(0, resultado.getConvocatoriasGastadas());
        assertEquals(250.00f, resultado.getPrecio());
        assertEquals(TipoMatricula.NUEVA, resultado.getTipo());
        assertEquals(alumno, resultado.getAlumno());

        verify(this.alumnoRepository).findById(alumnoId);
        verify(this.matriculaRepository).save(any(Matricula.class));
    }

    @Test
    @DisplayName("matricularTrasPago crea matrícula de vehículo pesado (Permiso C) con tarifa de 450 €")
    void testMatricularTrasPagoPermisoPesado()
    {
        Long alumnoId = 202L;
        Alumno alumno = new Alumno();
        ReflectionTestUtils.setField(alumno, "id", alumnoId);
        alumno.setDni("87654321X");

        when(this.alumnoRepository.findById(alumnoId)).thenReturn(Optional.of(alumno));
        when(this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno)).thenReturn(Optional.empty());
        when(this.matriculaRepository.save(any(Matricula.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Matricula resultado = this.academicoService.matricularTrasPago(alumnoId, "PERMISO_C", ModalidadMatricula.TEORICO_PRACTICA, 450.00f);

        assertNotNull(resultado);
        assertEquals(TipoCarnet.PERMISO_C, resultado.getPermisoCarnet());
        assertEquals(450.00f, resultado.getPrecio());
        assertTrue(resultado.getEstaActiva());
    }

    @Test
    @DisplayName("matricularTrasPago lanza ReglaNegocioException cuando el alumno no existe en el sistema")
    void testMatricularTrasPagoAlumnoNoExiste()
    {
        Long alumnoId = 999L;
        when(this.alumnoRepository.findById(alumnoId)).thenReturn(Optional.empty());

        ReglaNegocioException excepcion = assertThrows(ReglaNegocioException.class, () ->
                this.academicoService.matricularTrasPago(alumnoId, "PERMISO_B", ModalidadMatricula.TEORICO_PRACTICA, 250.00f)
        );

        assertTrue(excepcion.getMessage().contains("999"));
        verify(this.matriculaRepository, never()).save(any(Matricula.class));
    }

    @Test
    @DisplayName("matricularTrasPago lanza ReglaNegocioException si el ID del alumno es nulo")
    void testMatricularTrasPagoDniInvalido()
    {
        assertThrows(ReglaNegocioException.class, () ->
                this.academicoService.matricularTrasPago(null, "PERMISO_B", ModalidadMatricula.TEORICO_PRACTICA, 250.00f)
        );
    }

    @Test
    @DisplayName("matricularTrasPago aplica idempotencia: si el alumno ya tiene matrícula activa para ese carnet, la reutiliza")
    void testMatricularTrasPagoIdempotencia()
    {
        Long alumnoId = 201L;
        Alumno alumno = new Alumno();
        ReflectionTestUtils.setField(alumno, "id", alumnoId);
        alumno.setDni("12345678Z");

        Matricula matriculaExistente = new Matricula();
        matriculaExistente.setEstaActiva(true);
        matriculaExistente.setPermisoCarnet(TipoCarnet.PERMISO_B);
        matriculaExistente.setPrecio(200.00f);
        matriculaExistente.setAlumno(alumno);

        when(this.alumnoRepository.findById(alumnoId)).thenReturn(Optional.of(alumno));
        when(this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno)).thenReturn(Optional.of(matriculaExistente));

        Matricula resultado = this.academicoService.matricularTrasPago(alumnoId, "PERMISO_B", ModalidadMatricula.TEORICO_PRACTICA, 250.00f);

        assertNotNull(resultado);
        assertEquals(200.00f, resultado.getPrecio());
        verify(this.matriculaRepository, never()).save(any(Matricula.class));
    }

    @Test
    @DisplayName("matricularTrasPago asigna PERMISO_B por defecto si el tipo de carnet recibido no es reconocido")
    void testMatricularTrasPagoCarnetInvalido()
    {
        Long alumnoId = 201L;
        Alumno alumno = new Alumno();
        ReflectionTestUtils.setField(alumno, "id", alumnoId);
        alumno.setDni("12345678Z");

        when(this.alumnoRepository.findById(alumnoId)).thenReturn(Optional.of(alumno));
        when(this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno)).thenReturn(Optional.empty());
        when(this.matriculaRepository.save(any(Matricula.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Matricula resultado = this.academicoService.matricularTrasPago(alumnoId, "PERMISO_DESCONOCIDO_XYZ", ModalidadMatricula.TEORICO_PRACTICA, 250.00f);

        assertNotNull(resultado);
        assertEquals(TipoCarnet.PERMISO_B, resultado.getPermisoCarnet());
    }

    @Test
    @DisplayName("matricularTrasPago lanza ReglaNegocioException si el alumno ya tiene otra matrícula activa diferente")
    void testMatricularTrasPagoLanzaExcepcionSiYaTieneMatriculaActivaParaOtroCarnet()
    {
        Long alumnoId = 201L;
        Alumno alumno = new Alumno();
        ReflectionTestUtils.setField(alumno, "id", alumnoId);
        alumno.setDni("12345678Z");

        Matricula matriculaExistente = new Matricula();
        matriculaExistente.setEstaActiva(true);
        matriculaExistente.setPermisoCarnet(TipoCarnet.PERMISO_B);

        when(this.alumnoRepository.findById(alumnoId)).thenReturn(Optional.of(alumno));
        when(this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno)).thenReturn(Optional.of(matriculaExistente));

        ReglaNegocioException excepcion = assertThrows(ReglaNegocioException.class, () ->
                this.academicoService.matricularTrasPago(alumnoId, "PERMISO_C", ModalidadMatricula.TEORICO_PRACTICA, 450.00f)
        );

        assertTrue(excepcion.getMessage().contains("No es posible cursar dos permisos de conducir simultáneamente"));
    }

    @Test
    @DisplayName("matricularTrasPago crea nueva matrícula activa si el alumno tenía una matrícula previa cerrada/inactiva")
    void testMatricularTrasPagoCreaNuevaMatriculaSiMatriculaPreviaEstabaInactiva()
    {
        Long alumnoId = 201L;
        Alumno alumno = new Alumno();
        ReflectionTestUtils.setField(alumno, "id", alumnoId);
        alumno.setDni("12345678Z");

        when(this.alumnoRepository.findById(alumnoId)).thenReturn(Optional.of(alumno));
        when(this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno)).thenReturn(Optional.empty());
        when(this.matriculaRepository.save(any(Matricula.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Matricula nueva = this.academicoService.matricularTrasPago(alumnoId, "PERMISO_C", ModalidadMatricula.TEORICO_PRACTICA, 450.00f);

        assertNotNull(nueva);
        assertEquals(TipoCarnet.PERMISO_C, nueva.getPermisoCarnet());
        assertTrue(nueva.getEstaActiva());
        assertEquals(450.00f, nueva.getPrecio());
        verify(this.matriculaRepository).save(any(Matricula.class));
    }

    @Test
    @DisplayName("renovarMatriculaTrasPago acumula el importe de renovación sobre el precio previo de la matrícula y restablece 2 convocatorias")
    void testRenovarMatriculaTrasPagoAcumulaPrecio()
    {
        Long alumnoId = 201L;
        Alumno alumno = new Alumno();
        ReflectionTestUtils.setField(alumno, "id", alumnoId);
        alumno.setDni("12345678Z");

        Matricula matricula = new Matricula();
        matricula.setEstaActiva(true);
        matricula.setPermisoCarnet(TipoCarnet.PERMISO_B);
        matricula.setConvocatorias(0);
        matricula.setPrecio(250.00f);
        matricula.setTipo(TipoMatricula.NUEVA);
        matricula.setAlumno(alumno);

        when(this.alumnoRepository.findById(alumnoId)).thenReturn(Optional.of(alumno));
        when(this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno)).thenReturn(Optional.of(matricula));
        when(this.matriculaRepository.save(any(Matricula.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Matricula renovada = this.academicoService.renovarMatriculaTrasPago(alumnoId, "cs_test_renovacion_1", 250.00f);

        assertNotNull(renovada);
        assertEquals(TipoMatricula.RENOVACION, renovada.getTipo());
        assertEquals(2, renovada.getConvocatorias());
        assertEquals(500.00f, renovada.getPrecio(), "El precio acumulado debe sumar la matrícula inicial (250€) y la renovación (250€) = 500€");
        verify(this.matriculaRepository).save(matricula);
    }

    @Test
    @DisplayName("renovarMatriculaTrasPago aplica idempotencia evitando duplicar la acumulación ante sesiones idénticas")
    void testRenovarMatriculaTrasPagoIdempotencia()
    {
        Long alumnoId = 201L;
        Alumno alumno = new Alumno();
        ReflectionTestUtils.setField(alumno, "id", alumnoId);
        alumno.setDni("12345678Z");

        Matricula matricula = new Matricula();
        matricula.setEstaActiva(true);
        matricula.setPermisoCarnet(TipoCarnet.PERMISO_B);
        matricula.setConvocatorias(0);
        matricula.setPrecio(250.00f);
        matricula.setTipo(TipoMatricula.NUEVA);
        matricula.setAlumno(alumno);

        when(this.alumnoRepository.findById(alumnoId)).thenReturn(Optional.of(alumno));
        when(this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno)).thenReturn(Optional.of(matricula));
        when(this.matriculaRepository.save(any(Matricula.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Matricula primera = this.academicoService.renovarMatriculaTrasPago(alumnoId, "cs_test_idempotente_rep", 250.00f);
        assertEquals(500.00f, primera.getPrecio());

        // Segunda llamada con el mismo sessionId no debe duplicar la suma
        Matricula segunda = this.academicoService.renovarMatriculaTrasPago(alumnoId, "cs_test_idempotente_rep", 250.00f);
        assertEquals(500.00f, segunda.getPrecio());
    }

    @Test
    @DisplayName("matricularTrasPago activa un alumno INACTIVO y asigna modalidad TEORICO_PRACTICA si es nula")
    void testMatricularTrasPagoActivaAlumnoInactivoYModalidadNula()
    {
        Long alumnoId = 15L;
        Alumno alumno = new Alumno();
        ReflectionTestUtils.setField(alumno, "id", alumnoId);
        alumno.setNombre("Carlos");
        alumno.setCorreo("carlos@mands.es");
        alumno.setEstado(EstadoUsuario.INACTIVO);

        when(this.alumnoRepository.findById(alumnoId)).thenReturn(Optional.of(alumno));
        when(this.alumnoRepository.save(any(Alumno.class))).thenAnswer(i -> i.getArgument(0));
        when(this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno)).thenReturn(Optional.empty());
        when(this.matriculaRepository.save(any(Matricula.class))).thenAnswer(i -> i.getArgument(0));

        Matricula resultado = this.academicoService.matricularTrasPago(alumnoId, "PERMISO_A2", null, 250.0f);

        assertEquals(EstadoUsuario.ACTIVO, alumno.getEstado());
        assertEquals(ModalidadMatricula.TEORICO_PRACTICA, resultado.getModalidad());
        assertEquals(TipoCarnet.PERMISO_A2, resultado.getPermisoCarnet());
        verify(this.alumnoRepository).save(alumno);
    }

    @Test
    @DisplayName("matricularTrasPago lanza ReglaNegocioException cuando el tipo de carnet es nulo o vacío")
    void testMatricularTrasPagoCarnetNuloOVacio()
    {
        Long alumnoId = 16L;
        Alumno alumno = new Alumno();
        ReflectionTestUtils.setField(alumno, "id", alumnoId);
        alumno.setEstado(EstadoUsuario.ACTIVO);

        when(this.alumnoRepository.findById(alumnoId)).thenReturn(Optional.of(alumno));

        assertThrows(ReglaNegocioException.class,
                () -> this.academicoService.matricularTrasPago(alumnoId, null, ModalidadMatricula.PRACTICA, 250.0f));
        assertThrows(ReglaNegocioException.class,
                () -> this.academicoService.matricularTrasPago(alumnoId, "   ", ModalidadMatricula.PRACTICA, 250.0f));
    }

    @Test
    @DisplayName("recargarSaldoClasesTrasPago valida parámetros, convocatorias y alumno existente")
    void testRecargarSaldoClasesTrasPagoValidaciones()
    {
        assertThrows(ReglaNegocioException.class,
                () -> this.academicoService.recargarSaldoClasesTrasPago(null, 5, "cs_1"));
        assertThrows(ReglaNegocioException.class,
                () -> this.academicoService.recargarSaldoClasesTrasPago(1L, 0, "cs_1"));

        when(this.alumnoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ReglaNegocioException.class,
                () -> this.academicoService.recargarSaldoClasesTrasPago(99L, 5, "cs_1"));

        Alumno alumno = new Alumno();
        ReflectionTestUtils.setField(alumno, "id", 1L);
        when(this.alumnoRepository.findById(1L)).thenReturn(Optional.of(alumno));
        when(this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno)).thenReturn(Optional.empty());
        assertThrows(ReglaNegocioException.class,
                () -> this.academicoService.recargarSaldoClasesTrasPago(1L, 5, "cs_1"));

        Matricula agotada = new Matricula();
        agotada.setEstaActiva(true);
        agotada.setConvocatorias(0);
        agotada.setConvocatoriasGastadas(2);
        when(this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno)).thenReturn(Optional.of(agotada));
        assertThrows(ReglaNegocioException.class,
                () -> this.academicoService.recargarSaldoClasesTrasPago(1L, 5, "cs_1"));
    }

    @Test
    @DisplayName("recargarSaldoClasesTrasPago suma saldo inicial nulo, respeta idempotencia y admite sessionId vacío")
    void testRecargarSaldoClasesTrasPagoExitoEIdempotencia()
    {
        Long alumnoId = 20L;
        Alumno alumno = new Alumno();
        ReflectionTestUtils.setField(alumno, "id", alumnoId);

        Matricula matricula = new Matricula();
        matricula.setEstaActiva(true);
        matricula.setConvocatorias(2);
        matricula.setConvocatoriasGastadas(0);
        matricula.setSaldoClases(null);

        when(this.alumnoRepository.findById(alumnoId)).thenReturn(Optional.of(alumno));
        when(this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno)).thenReturn(Optional.of(matricula));
        when(this.matriculaRepository.save(any(Matricula.class))).thenAnswer(i -> i.getArgument(0));

        Matricula primera = this.academicoService.recargarSaldoClasesTrasPago(alumnoId, 10, "cs_recarga_unica");
        assertEquals(10, primera.getSaldoClases());

        // Reintento con mismo sessionId no duplica saldo
        Matricula duplicada = this.academicoService.recargarSaldoClasesTrasPago(alumnoId, 10, "cs_recarga_unica");
        assertEquals(10, duplicada.getSaldoClases());

        // Con sessionId nulo o blanco vuelve a recargar normalmente sobre saldo no nulo
        Matricula sinSessionNula = this.academicoService.recargarSaldoClasesTrasPago(alumnoId, 5, null);
        assertEquals(15, sinSessionNula.getSaldoClases());

        Matricula sinSessionBlanca = this.academicoService.recargarSaldoClasesTrasPago(alumnoId, 1, "  ");
        assertEquals(16, sinSessionBlanca.getSaldoClases());
    }

    @Test
    @DisplayName("renovarMatriculaTrasPago valida parámetros y suma sobre precio nulo sin sessionId")
    void testRenovarMatriculaTrasPagoValidacionesYPrecioNulo()
    {
        assertThrows(ReglaNegocioException.class,
                () -> this.academicoService.renovarMatriculaTrasPago(null, "cs_ren", 250.0f));

        when(this.alumnoRepository.findById(88L)).thenReturn(Optional.empty());
        assertThrows(ReglaNegocioException.class,
                () -> this.academicoService.renovarMatriculaTrasPago(88L, "cs_ren", 250.0f));

        Alumno alumno = new Alumno();
        ReflectionTestUtils.setField(alumno, "id", 8L);
        when(this.alumnoRepository.findById(8L)).thenReturn(Optional.of(alumno));
        when(this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno)).thenReturn(Optional.empty());
        assertThrows(ReglaNegocioException.class,
                () -> this.academicoService.renovarMatriculaTrasPago(8L, "cs_ren", 250.0f));

        Matricula matricula = new Matricula();
        matricula.setEstaActiva(true);
        matricula.setPrecio(null);
        when(this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno)).thenReturn(Optional.of(matricula));
        when(this.matriculaRepository.save(any(Matricula.class))).thenAnswer(i -> i.getArgument(0));

        Matricula renovada = this.academicoService.renovarMatriculaTrasPago(8L, null, 250.0f);
        assertEquals(250.0f, renovada.getPrecio());
        assertEquals(2, renovada.getConvocatorias());
        assertEquals(TipoMatricula.RENOVACION, renovada.getTipo());

        Matricula renovadaSinSessionBlanca = this.academicoService.renovarMatriculaTrasPago(8L, "  ", 100.0f);
        assertEquals(350.0f, renovadaSinSessionBlanca.getPrecio());
    }

    @Test
    @DisplayName("Consultas auxiliares y guardar gestionan alumno nulo y delegan en el repositorio")
    void testConsultasAuxiliaresYGuardar()
    {
        assertTrue(this.academicoService.buscarMatriculaActiva(null).isEmpty());
        assertTrue(this.academicoService.obtenerHistoricoMatriculas(null).isEmpty());
        assertTrue(this.academicoService.obtenerUltimaMatricula(null).isEmpty());

        Alumno alumno = new Alumno();
        Matricula matricula = new Matricula();
        when(this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno)).thenReturn(Optional.of(matricula));
        when(this.matriculaRepository.findByAlumnoOrderByFechaMatriculacionDesc(alumno)).thenReturn(List.of(matricula));
        when(this.matriculaRepository.findFirstByAlumnoOrderByIdDesc(alumno)).thenReturn(Optional.of(matricula));
        when(this.matriculaRepository.save(matricula)).thenReturn(matricula);

        assertTrue(this.academicoService.buscarMatriculaActiva(alumno).isPresent());
        assertFalse(this.academicoService.obtenerHistoricoMatriculas(alumno).isEmpty());
        assertTrue(this.academicoService.obtenerUltimaMatricula(alumno).isPresent());
        assertSame(matricula, this.academicoService.guardar(matricula));
    }
}
