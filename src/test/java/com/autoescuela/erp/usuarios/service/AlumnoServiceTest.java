package com.autoescuela.erp.usuarios.service;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.autoescuela.erp.academico.model.Matricula;
import com.autoescuela.erp.academico.repository.MatriculaRepository;
import com.autoescuela.erp.core.email.EmailService;
import com.autoescuela.erp.core.enums.EstadoClase;
import com.autoescuela.erp.core.enums.EstadoUsuario;
import com.autoescuela.erp.core.enums.ModalidadMatricula;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.enums.TipoExamen;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.examenes.repository.ExamenRepository;
import com.autoescuela.erp.practicas.repository.ClasePracticaRepository;
import com.autoescuela.erp.usuarios.dto.ReasignarAlumnoDTO;
import com.autoescuela.erp.usuarios.mapper.AlumnoMapper;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.model.Profesor;
import com.autoescuela.erp.usuarios.repository.AlumnoRepository;
import com.autoescuela.erp.usuarios.repository.PersonaRepository;
import com.autoescuela.erp.usuarios.repository.ProfesorRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios de AlumnoService (Reglas de Vinculación Alumno-Profesor)")
class AlumnoServiceTest
{
    @Mock
    private UsuarioService usuarioService;

    @Mock
    private AlumnoRepository alumnoRepository;

    @Mock
    private MatriculaRepository matriculaRepository;

    @Mock
    private ClasePracticaRepository clasePracticaRepository;

    @Mock
    private ExamenRepository examenRepository;

    @Mock
    private ProfesorRepository profesorRepository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private AlumnoMapper alumnoMapper;

    @InjectMocks
    private AlumnoService alumnoService;

    @Test
    @DisplayName("reasignarProfesor lanza ReglaNegocioException si el alumno no dispone de matrícula activa")
    void testReasignarProfesorFallaSinMatriculaActiva()
    {
        Long alumnoId = 200L;
        Long nuevoProfesorId = 100L;

        Alumno alumno = new Alumno();
        ReflectionTestUtils.setField(alumno, "id", alumnoId);
        alumno.setEstado(EstadoUsuario.ACTIVO);

        Profesor nuevoProfesor = new Profesor();
        ReflectionTestUtils.setField(nuevoProfesor, "id", nuevoProfesorId);
        nuevoProfesor.setEstado(EstadoUsuario.ACTIVO);

        when(this.alumnoRepository.findById(alumnoId)).thenReturn(Optional.of(alumno));
        when(this.profesorRepository.findById(nuevoProfesorId)).thenReturn(Optional.of(nuevoProfesor));
        when(this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno)).thenReturn(Optional.empty());

        ReasignarAlumnoDTO dto = new ReasignarAlumnoDTO(alumnoId, "REASIGNAR", nuevoProfesorId);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () ->
                this.alumnoService.reasignarProfesor(dto));

        assertTrue(ex.getMessage().contains("matrícula activa"));
    }

    @Test
    @DisplayName("reasignarProfesor lanza ReglaNegocioException si alumno en TEORICO_PRACTICA no ha aprobado el examen teórico")
    void testReasignarProfesorFallaTeoricoPendienteEnModalidadTeoricoPractica()
    {
        Long alumnoId = 200L;
        Long nuevoProfesorId = 100L;

        Alumno alumno = new Alumno();
        ReflectionTestUtils.setField(alumno, "id", alumnoId);
        alumno.setEstado(EstadoUsuario.ACTIVO);

        Profesor nuevoProfesor = new Profesor();
        ReflectionTestUtils.setField(nuevoProfesor, "id", nuevoProfesorId);
        nuevoProfesor.setEstado(EstadoUsuario.ACTIVO);
        nuevoProfesor.setListaTiposCarnet(List.of(TipoCarnet.PERMISO_B));

        Matricula matricula = new Matricula();
        matricula.setPermisoCarnet(TipoCarnet.PERMISO_B);
        matricula.setModalidad(ModalidadMatricula.TEORICO_PRACTICA);
        matricula.setEstaActiva(true);

        when(this.alumnoRepository.findById(alumnoId)).thenReturn(Optional.of(alumno));
        when(this.profesorRepository.findById(nuevoProfesorId)).thenReturn(Optional.of(nuevoProfesor));
        when(this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno)).thenReturn(Optional.of(matricula));
        when(this.examenRepository.existsByAlumnoAndTipoAndEsAptoTrue(alumno, TipoExamen.TEORICO)).thenReturn(false);

        ReasignarAlumnoDTO dto = new ReasignarAlumnoDTO(alumnoId, "REASIGNAR", nuevoProfesorId);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () ->
                this.alumnoService.reasignarProfesor(dto));

        assertTrue(ex.getMessage().contains("examen teórico oficial"));
    }

    @Test
    @DisplayName("reasignarProfesor reasigna con éxito a alumno en TEORICO_PRACTICA con examen teórico aprobado")
    void testReasignarProfesorExitoConTeoricoAprobado()
    {
        Long alumnoId = 200L;
        Long nuevoProfesorId = 100L;

        Alumno alumno = new Alumno();
        ReflectionTestUtils.setField(alumno, "id", alumnoId);
        alumno.setNombre("David");
        alumno.setCorreo("david@autoescuela.es");
        alumno.setEstado(EstadoUsuario.ACTIVO);

        Profesor nuevoProfesor = new Profesor();
        ReflectionTestUtils.setField(nuevoProfesor, "id", nuevoProfesorId);
        nuevoProfesor.setNombre("Laura");
        nuevoProfesor.setApellidos("Sánchez");
        nuevoProfesor.setEstado(EstadoUsuario.ACTIVO);
        nuevoProfesor.setListaTiposCarnet(List.of(TipoCarnet.PERMISO_B));

        Matricula matricula = new Matricula();
        matricula.setPermisoCarnet(TipoCarnet.PERMISO_B);
        matricula.setModalidad(ModalidadMatricula.TEORICO_PRACTICA);
        matricula.setEstaActiva(true);

        when(this.alumnoRepository.findById(alumnoId)).thenReturn(Optional.of(alumno));
        when(this.profesorRepository.findById(nuevoProfesorId)).thenReturn(Optional.of(nuevoProfesor));
        when(this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno)).thenReturn(Optional.of(matricula));
        when(this.examenRepository.existsByAlumnoAndTipoAndEsAptoTrue(alumno, TipoExamen.TEORICO)).thenReturn(true);
        when(this.clasePracticaRepository.findByAlumnoAndEstadoClase(alumno, EstadoClase.PENDIENTE)).thenReturn(List.of());

        ReasignarAlumnoDTO dto = new ReasignarAlumnoDTO(alumnoId, "REASIGNAR", nuevoProfesorId);

        this.alumnoService.reasignarProfesor(dto);

        assertEquals(nuevoProfesor, alumno.getProfesor());
        verify(this.alumnoRepository).save(alumno);
        verify(this.emailService).enviarNotificacionAlumno(eq("david@autoescuela.es"), anyString(), anyString());
    }

    @Test
    @DisplayName("reasignarProfesor reasigna con éxito a alumno en modalidad PRACTICA sin requerir examen teórico")
    void testReasignarProfesorExitoEnModalidadPractica()
    {
        Long alumnoId = 201L;
        Long nuevoProfesorId = 100L;

        Alumno alumno = new Alumno();
        ReflectionTestUtils.setField(alumno, "id", alumnoId);
        alumno.setNombre("Lucía");
        alumno.setCorreo("lucia@autoescuela.es");
        alumno.setEstado(EstadoUsuario.ACTIVO);

        Profesor nuevoProfesor = new Profesor();
        ReflectionTestUtils.setField(nuevoProfesor, "id", nuevoProfesorId);
        nuevoProfesor.setNombre("Laura");
        nuevoProfesor.setApellidos("Sánchez");
        nuevoProfesor.setEstado(EstadoUsuario.ACTIVO);
        nuevoProfesor.setListaTiposCarnet(List.of(TipoCarnet.PERMISO_B));

        Matricula matricula = new Matricula();
        matricula.setPermisoCarnet(TipoCarnet.PERMISO_B);
        matricula.setModalidad(ModalidadMatricula.PRACTICA);
        matricula.setEstaActiva(true);

        when(this.alumnoRepository.findById(alumnoId)).thenReturn(Optional.of(alumno));
        when(this.profesorRepository.findById(nuevoProfesorId)).thenReturn(Optional.of(nuevoProfesor));
        when(this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno)).thenReturn(Optional.of(matricula));
        when(this.clasePracticaRepository.findByAlumnoAndEstadoClase(alumno, EstadoClase.PENDIENTE)).thenReturn(List.of());

        ReasignarAlumnoDTO dto = new ReasignarAlumnoDTO(alumnoId, "REASIGNAR", nuevoProfesorId);

        this.alumnoService.reasignarProfesor(dto);

        assertEquals(nuevoProfesor, alumno.getProfesor());
        verify(this.alumnoRepository).save(alumno);
    }

    @Test
    @DisplayName("puedeTenerProfesorAsignado evalúa correctamente las precondiciones de modalidad y teórico aprobado")
    void testPuedeTenerProfesorAsignado()
    {
        Long alumnoId = 202L;
        Alumno alumno = new Alumno();
        ReflectionTestUtils.setField(alumno, "id", alumnoId);

        when(this.alumnoRepository.findById(alumnoId)).thenReturn(Optional.of(alumno));

        // Caso 1: Sin matrícula activa -> false
        when(this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno)).thenReturn(Optional.empty());
        assertFalse(this.alumnoService.puedeTenerProfesorAsignado(alumnoId));

        // Caso 2: TEORICO_PRACTICA y no aprobado -> false
        Matricula matTeoricoPractica = new Matricula();
        matTeoricoPractica.setModalidad(ModalidadMatricula.TEORICO_PRACTICA);
        matTeoricoPractica.setEstaActiva(true);
        when(this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno)).thenReturn(Optional.of(matTeoricoPractica));
        when(this.examenRepository.existsByAlumnoAndTipoAndEsAptoTrue(alumno, TipoExamen.TEORICO)).thenReturn(false);
        assertFalse(this.alumnoService.puedeTenerProfesorAsignado(alumnoId));

        // Caso 3: TEORICO_PRACTICA y aprobado -> true
        when(this.examenRepository.existsByAlumnoAndTipoAndEsAptoTrue(alumno, TipoExamen.TEORICO)).thenReturn(true);
        assertTrue(this.alumnoService.puedeTenerProfesorAsignado(alumnoId));

        // Caso 4: PRACTICA -> true
        Matricula matPractica = new Matricula();
        matPractica.setModalidad(ModalidadMatricula.PRACTICA);
        matPractica.setEstaActiva(true);
        when(this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno)).thenReturn(Optional.of(matPractica));
        assertTrue(this.alumnoService.puedeTenerProfesorAsignado(alumnoId));
    }
}
