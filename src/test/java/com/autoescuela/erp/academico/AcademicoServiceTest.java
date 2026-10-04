package com.autoescuela.erp.academico;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.autoescuela.erp.academico.model.Matricula;
import com.autoescuela.erp.academico.repository.MatriculaRepository;
import com.autoescuela.erp.academico.service.AcademicoService;
import com.autoescuela.erp.core.email.EmailService;
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
        String dni = "12345678Z";
        Alumno alumno = new Alumno();
        alumno.setDni(dni);

        when(this.alumnoRepository.findByDni(dni)).thenReturn(Optional.of(alumno));
        when(this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno)).thenReturn(Optional.empty());
        when(this.matriculaRepository.save(any(Matricula.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Matricula resultado = this.academicoService.matricularTrasPago(dni, "PERMISO_B", 250.00f);

        assertNotNull(resultado);
        assertTrue(resultado.getEstaActiva());
        assertEquals(TipoCarnet.PERMISO_B, resultado.getPermisoCarnet());
        assertEquals(2, resultado.getConvocatorias());
        assertEquals(0, resultado.getSaldoClases());
        assertEquals(0, resultado.getConvocatoriasGastadas());
        assertEquals(250.00f, resultado.getPrecio());
        assertEquals(TipoMatricula.NUEVA, resultado.getTipo());
        assertEquals(alumno, resultado.getAlumno());

        verify(this.alumnoRepository).findByDni(dni);
        verify(this.matriculaRepository).save(any(Matricula.class));
    }

    @Test
    @DisplayName("matricularTrasPago crea matrícula de vehículo pesado (Permiso C) con tarifa de 450 €")
    void testMatricularTrasPagoPermisoPesado()
    {
        String dni = "87654321X";
        Alumno alumno = new Alumno();
        alumno.setDni(dni);

        when(this.alumnoRepository.findByDni(dni)).thenReturn(Optional.of(alumno));
        when(this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno)).thenReturn(Optional.empty());
        when(this.matriculaRepository.save(any(Matricula.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Matricula resultado = this.academicoService.matricularTrasPago(dni, "PERMISO_C", 450.00f);

        assertNotNull(resultado);
        assertEquals(TipoCarnet.PERMISO_C, resultado.getPermisoCarnet());
        assertEquals(450.00f, resultado.getPrecio());
        assertTrue(resultado.getEstaActiva());
    }

    @Test
    @DisplayName("matricularTrasPago lanza ReglaNegocioException cuando el alumno no existe en el sistema")
    void testMatricularTrasPagoAlumnoNoExiste()
    {
        String dni = "99999999R";
        when(this.alumnoRepository.findByDni(dni)).thenReturn(Optional.empty());

        ReglaNegocioException excepcion = assertThrows(ReglaNegocioException.class, () ->
                this.academicoService.matricularTrasPago(dni, "PERMISO_B", 250.00f)
        );

        assertTrue(excepcion.getMessage().contains(dni));
        verify(this.matriculaRepository, never()).save(any(Matricula.class));
    }

    @Test
    @DisplayName("matricularTrasPago lanza ReglaNegocioException si el DNI es nulo o está en blanco")
    void testMatricularTrasPagoDniInvalido()
    {
        assertThrows(ReglaNegocioException.class, () ->
                this.academicoService.matricularTrasPago(null, "PERMISO_B", 250.00f)
        );

        assertThrows(ReglaNegocioException.class, () ->
                this.academicoService.matricularTrasPago("   ", "PERMISO_B", 250.00f)
        );
    }

    @Test
    @DisplayName("matricularTrasPago aplica idempotencia: si el alumno ya tiene matrícula activa para ese carnet, la reutiliza")
    void testMatricularTrasPagoIdempotencia()
    {
        String dni = "12345678Z";
        Alumno alumno = new Alumno();
        alumno.setDni(dni);

        Matricula matriculaExistente = new Matricula();
        matriculaExistente.setEstaActiva(true);
        matriculaExistente.setPermisoCarnet(TipoCarnet.PERMISO_B);
        matriculaExistente.setPrecio(200.00f);
        matriculaExistente.setAlumno(alumno);

        when(this.alumnoRepository.findByDni(dni)).thenReturn(Optional.of(alumno));
        when(this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno)).thenReturn(Optional.of(matriculaExistente));

        Matricula resultado = this.academicoService.matricularTrasPago(dni, "PERMISO_B", 250.00f);

        assertNotNull(resultado);
        assertEquals(200.00f, resultado.getPrecio());
        verify(this.matriculaRepository, never()).save(any(Matricula.class));
    }

    @Test
    @DisplayName("matricularTrasPago asigna PERMISO_B por defecto si el tipo de carnet recibido no es reconocido")
    void testMatricularTrasPagoCarnetInvalido()
    {
        String dni = "12345678Z";
        Alumno alumno = new Alumno();
        alumno.setDni(dni);

        when(this.alumnoRepository.findByDni(dni)).thenReturn(Optional.of(alumno));
        when(this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno)).thenReturn(Optional.empty());
        when(this.matriculaRepository.save(any(Matricula.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Matricula resultado = this.academicoService.matricularTrasPago(dni, "PERMISO_DESCONOCIDO_XYZ", 250.00f);

        assertNotNull(resultado);
        assertEquals(TipoCarnet.PERMISO_B, resultado.getPermisoCarnet());
    }

    @Test
    @DisplayName("matricularTrasPago lanza ReglaNegocioException si el alumno ya tiene otra matrícula activa diferente")
    void testMatricularTrasPagoLanzaExcepcionSiYaTieneMatriculaActivaParaOtroCarnet()
    {
        String dni = "12345678Z";
        Alumno alumno = new Alumno();
        alumno.setDni(dni);

        Matricula matriculaExistente = new Matricula();
        matriculaExistente.setEstaActiva(true);
        matriculaExistente.setPermisoCarnet(TipoCarnet.PERMISO_B);

        when(this.alumnoRepository.findByDni(dni)).thenReturn(Optional.of(alumno));
        when(this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno)).thenReturn(Optional.of(matriculaExistente));

        ReglaNegocioException excepcion = assertThrows(ReglaNegocioException.class, () ->
                this.academicoService.matricularTrasPago(dni, "PERMISO_C", 450.00f)
        );

        assertTrue(excepcion.getMessage().contains("No es posible cursar dos permisos de conducir simultáneamente"));
    }

    @Test
    @DisplayName("matricularTrasPago crea nueva matrícula activa si el alumno tenía una matrícula previa cerrada/inactiva")
    void testMatricularTrasPagoCreaNuevaMatriculaSiMatriculaPreviaEstabaInactiva()
    {
        String dni = "12345678Z";
        Alumno alumno = new Alumno();
        alumno.setDni(dni);

        when(this.alumnoRepository.findByDni(dni)).thenReturn(Optional.of(alumno));
        when(this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno)).thenReturn(Optional.empty());
        when(this.matriculaRepository.save(any(Matricula.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Matricula nueva = this.academicoService.matricularTrasPago(dni, "PERMISO_C", 450.00f);

        assertNotNull(nueva);
        assertEquals(TipoCarnet.PERMISO_C, nueva.getPermisoCarnet());
        assertTrue(nueva.getEstaActiva());
        assertEquals(450.00f, nueva.getPrecio());
        verify(this.matriculaRepository).save(any(Matricula.class));
    }
}
