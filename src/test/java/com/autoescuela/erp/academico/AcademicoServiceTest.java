package com.autoescuela.erp.academico;

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
import com.autoescuela.erp.core.enums.ModalidadMatricula;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.enums.TipoMatricula;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.repository.AlumnoRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
}
