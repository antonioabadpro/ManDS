package com.autoescuela.erp.usuarios.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.autoescuela.erp.academico.model.Matricula;
import com.autoescuela.erp.academico.repository.MatriculaRepository;
import com.autoescuela.erp.core.email.EmailService;
import com.autoescuela.erp.core.enums.EstadoClase;
import com.autoescuela.erp.core.enums.EstadoUsuario;
import com.autoescuela.erp.core.enums.ModalidadMatricula;
import com.autoescuela.erp.core.enums.Rol;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.enums.TipoExamen;
import com.autoescuela.erp.core.enums.TipoTurno;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.core.security.UserDetailsImpl;
import com.autoescuela.erp.examenes.model.Examen;
import com.autoescuela.erp.examenes.repository.ExamenRepository;
import com.autoescuela.erp.practicas.model.ClasePractica;
import com.autoescuela.erp.practicas.repository.ClasePracticaRepository;
import com.autoescuela.erp.usuarios.dto.AlumnoDetalleDTO;
import com.autoescuela.erp.usuarios.dto.AlumnoExpedienteDTO;
import com.autoescuela.erp.usuarios.dto.AlumnoResumenDTO;
import com.autoescuela.erp.usuarios.dto.EditarPerfilAlumnoDTO;
import com.autoescuela.erp.usuarios.dto.ReasignarAlumnoDTO;
import com.autoescuela.erp.usuarios.mapper.AlumnoMapper;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.model.Profesor;
import com.autoescuela.erp.usuarios.repository.AlumnoRepository;
import com.autoescuela.erp.usuarios.repository.PersonaRepository;
import com.autoescuela.erp.usuarios.repository.ProfesorRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
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
        verify(this.emailService).enviarNotificacionCorreo(eq("david@autoescuela.es"), any(), anyString(), anyString(), eq(Rol.ALUMNO));
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

    @Test
    @DisplayName("obtenerTodosLosAlumnos optimiza consultas agrupando clases pendientes y aprobados teóricos en batch")
    void testObtenerTodosLosAlumnosOptimizado()
    {
        Alumno alumno1 = new Alumno();
        ReflectionTestUtils.setField(alumno1, "id", 200L);
        alumno1.setNombre("Carlos");
        alumno1.setApellidos("Pérez");
        Matricula mat1 = new Matricula();
        mat1.setEstaActiva(true);
        alumno1.setHistorialMatriculas(List.of(mat1));

        Alumno alumno2 = new Alumno();
        ReflectionTestUtils.setField(alumno2, "id", 201L);
        alumno2.setNombre("Ana");
        alumno2.setApellidos("García");
        alumno2.setHistorialMatriculas(List.of());

        when(this.alumnoRepository.findAllByOrderByNombreAscApellidosAsc()).thenReturn(List.of(alumno1, alumno2));
        List<Object[]> listaConteo = new java.util.ArrayList<>();
        listaConteo.add(new Object[]{200L, 3L});
        when(this.clasePracticaRepository.countClasesPorAlumnoAndEstado(EstadoClase.PENDIENTE))
                .thenReturn(listaConteo);
        when(this.examenRepository.findAlumnoIdsConExamenAprobado(TipoExamen.TEORICO))
                .thenReturn(Set.of(200L));

        AlumnoResumenDTO dto1 = new AlumnoResumenDTO(200L, "Carlos", "Pérez", "Carlos Pérez", "CP", 20, "12345678Z", "carlos@test.es", EstadoUsuario.ACTIVO, null, null, null, true, TipoCarnet.PERMISO_B, "Teórico-Práctica", 3, 3, 2, true, true);
        AlumnoResumenDTO dto2 = new AlumnoResumenDTO(201L, "Ana", "García", "Ana García", "AG", 22, "87654321A", "ana@test.es", EstadoUsuario.ACTIVO, null, null, null, false, null, null, null, 0, null, false, false);

        when(this.alumnoMapper.toAlumnoResumenDTO(alumno1, mat1, 3, true)).thenReturn(dto1);
        when(this.alumnoMapper.toAlumnoResumenDTO(alumno2, null, 0, false)).thenReturn(dto2);

        List<AlumnoResumenDTO> resultado = this.alumnoService.obtenerTodosLosAlumnos();

        assertEquals(2, resultado.size());
        assertEquals("Carlos Pérez", resultado.get(0).nombreCompleto());
        assertEquals("Ana García", resultado.get(1).nombreCompleto());
        verify(this.clasePracticaRepository).countClasesPorAlumnoAndEstado(EstadoClase.PENDIENTE);
        verify(this.examenRepository).findAlumnoIdsConExamenAprobado(TipoExamen.TEORICO);
    }

    @Test
    @DisplayName("obtenerAlumnoParaDetalle, puedeTenerProfesorAsignado y obtenerExpedienteAlumno gestionan nulos y detalles")
    void testDetalleExpedienteYPuedeTenerProfesor()
    {
        assertThrows(ReglaNegocioException.class, () -> this.alumnoService.obtenerAlumnoParaDetalle(null));
        assertThrows(ReglaNegocioException.class, () -> this.alumnoService.obtenerExpedienteAlumno(null));
        assertFalse(this.alumnoService.puedeTenerProfesorAsignado(null));
        assertFalse(this.alumnoService.puedeTenerProfesorAsignado(999L));
        assertEquals(0, this.alumnoService.contarClasesPendientes(null));
        assertEquals(0, this.alumnoService.contarClasesPendientes(999L));

        Alumno alumno = new Alumno();
        ReflectionTestUtils.setField(alumno, "id", 210L);
        Matricula mat = new Matricula();
        mat.setEstaActiva(true);
        alumno.setHistorialMatriculas(List.of(mat));

        AlumnoDetalleDTO detalleDTO = new AlumnoDetalleDTO(210L, "Ana", "Gil", "Ana Gil", "AG", "anag",
                EstadoUsuario.ACTIVO, "12345678Z", LocalDate.of(2000, 1, 1), 26, "Calle 1", "a@m.es", "600111222",
                LocalDate.now(), 2026, 0, "menos de 1 año", true, TipoTurno.MATINAL, null, null, null, null, null, null, null,
                TipoCarnet.PERMISO_B, "Permiso B");
        AlumnoExpedienteDTO expedienteDTO = new AlumnoExpedienteDTO(210L, "Ana Gil", TipoCarnet.PERMISO_B,
                "Permiso B", true, 5, 3L, 2, List.of(), List.of());

        when(this.alumnoRepository.findWithDetallesById(210L)).thenReturn(Optional.of(alumno));
        when(this.alumnoMapper.toAlumnoDetalleDTO(alumno, mat)).thenReturn(detalleDTO);
        when(this.clasePracticaRepository.countByAlumnoAndEstadoClase(alumno, EstadoClase.RECIBIDA)).thenReturn(3);
        when(this.clasePracticaRepository.findTop3ByAlumnoOrderByFechaHoraDesc(alumno)).thenReturn(List.of(new ClasePractica()));
        when(this.examenRepository.findTop2ByAlumnoOrderByFechaHoraDesc(alumno)).thenReturn(List.of(new Examen()));
        when(this.alumnoMapper.toAlumnoExpedienteDTO(eq(alumno), eq(mat), eq(3L), any(), any())).thenReturn(expedienteDTO);

        assertSame(detalleDTO, this.alumnoService.obtenerAlumnoParaDetalle(210L));
        assertSame(expedienteDTO, this.alumnoService.obtenerExpedienteAlumno(210L));
    }

    @Test
    @DisplayName("reasignarProfesor valida dto nulo y mismo profesor actual")
    void testReasignarProfesorValidacionesExtra()
    {
        assertThrows(ReglaNegocioException.class, () -> this.alumnoService.reasignarProfesor(null));
        assertThrows(ReglaNegocioException.class, () -> this.alumnoService.reasignarProfesor(new ReasignarAlumnoDTO(null, "REASIGNAR", 1L)));

        Profesor actual = new Profesor();
        ReflectionTestUtils.setField(actual, "id", 10L);
        actual.setEstado(EstadoUsuario.ACTIVO);
        actual.setListaTiposCarnet(List.of(TipoCarnet.PERMISO_B));

        Alumno alumno = new Alumno();
        ReflectionTestUtils.setField(alumno, "id", 215L);
        alumno.setEstado(EstadoUsuario.ACTIVO);
        alumno.setProfesor(actual);

        when(this.alumnoRepository.findById(215L)).thenReturn(Optional.of(alumno));

        assertThrows(ReglaNegocioException.class,
                () -> this.alumnoService.reasignarProfesor(new ReasignarAlumnoDTO(215L, "REASIGNAR", 10L)));
    }

    @Test
    @DisplayName("Consultas auxiliares, obtenerAlumnoActual, actualizarPerfil, cambiarPassword y contactarAlumno")
    void testConsultasPerfilPasswordYContacto()
    {
        Alumno alumno = new Alumno();
        ReflectionTestUtils.setField(alumno, "id", 220L);
        alumno.setNombre("Mario");
        alumno.setApellidos("Sánchez");
        alumno.setDni("12345678Z");
        alumno.setCorreo("mario@mands.es");
        alumno.setTelefono("600111222");
        alumno.setNombreUsuario("marios");
        alumno.setEstado(EstadoUsuario.ACTIVO);
        alumno.actualizarPassword("hashActual");
        Profesor profesor = new Profesor();

        when(this.alumnoRepository.findByDni("12345678Z")).thenReturn(Optional.of(alumno));
        when(this.alumnoRepository.findByNombreUsuario("marios")).thenReturn(Optional.of(alumno));
        when(this.alumnoRepository.findByCorreo("mario@mands.es")).thenReturn(Optional.of(alumno));
        when(this.alumnoRepository.findById(220L)).thenReturn(Optional.of(alumno));
        when(this.alumnoRepository.findByProfesor(profesor)).thenReturn(List.of(alumno));
        when(this.alumnoRepository.findByProfesorId(10L)).thenReturn(List.of(alumno));
        when(this.alumnoRepository.countByEstado(EstadoUsuario.ACTIVO)).thenReturn(8L);
        when(this.alumnoRepository.findAll()).thenReturn(List.of(alumno));
        when(this.alumnoRepository.save(alumno)).thenReturn(alumno);

        assertTrue(this.alumnoService.buscarPorDni("12345678Z").isPresent());
        assertTrue(this.alumnoService.buscarPorNombreUsuario("marios").isPresent());
        assertTrue(this.alumnoService.buscarPorCorreo("mario@mands.es").isPresent());
        assertTrue(this.alumnoService.buscarPorId(220L).isPresent());
        assertEquals(1, this.alumnoService.buscarPorProfesor(profesor).size());
        assertEquals(1, this.alumnoService.buscarPorProfesorId(10L).size());
        assertEquals(8L, this.alumnoService.contarPorEstado(EstadoUsuario.ACTIVO));
        assertEquals(1, this.alumnoService.obtenerTodos().size());
        assertSame(alumno, this.alumnoService.guardar(alumno));

        // obtenerAlumnoActual
        assertNull(this.alumnoService.obtenerAlumnoActual(new UserDetailsImpl(profesor)));
        assertSame(alumno, this.alumnoService.obtenerAlumnoActual(new UserDetailsImpl(alumno)));
        assertNull(this.alumnoService.obtenerAlumnoActual(new User("prof", "pw", List.of(new SimpleGrantedAuthority("ROLE_PROFESOR")))));
        when(this.alumnoRepository.findByNombreUsuario("desconocido")).thenReturn(Optional.empty());
        assertSame(alumno, this.alumnoService.obtenerAlumnoActual(new User("desconocido", "pw", List.of(new SimpleGrantedAuthority("ROLE_ALUMNO")))));
        assertNull(this.alumnoService.obtenerAlumnoActual("otro"));

        // actualizarPerfil
        assertThrows(ReglaNegocioException.class, () -> this.alumnoService.actualizarPerfil(null, new EditarPerfilAlumnoDTO()));

        EditarPerfilAlumnoDTO dtoDniInvalido = new EditarPerfilAlumnoDTO(220L, "Mario", "Sánchez", "600111222", "Dir",
                "mario@mands.es", LocalDate.of(2000, 1, 1), "99999999A", null, null, null, null, null, null, null, null, null, null, null, null);
        when(this.usuarioService.esLetraDniValida("99999999A")).thenReturn(false);
        assertThrows(ReglaNegocioException.class, () -> this.alumnoService.actualizarPerfil(220L, dtoDniInvalido));

        when(this.usuarioService.esLetraDniValida("99999999A")).thenReturn(true);
        when(this.personaRepository.existsByDniAndIdNot("99999999A", 220L)).thenReturn(true);
        assertThrows(ReglaNegocioException.class, () -> this.alumnoService.actualizarPerfil(220L, dtoDniInvalido));

        EditarPerfilAlumnoDTO dtoTelDup = new EditarPerfilAlumnoDTO(220L, "Mario", "Sánchez", "699888777", "Dir",
                "mario@mands.es", LocalDate.of(2000, 1, 1), "12345678Z", null, null, null, null, null, null, null, null, null, null, null, null);
        when(this.personaRepository.existsByTelefonoAndIdNot("699888777", 220L)).thenReturn(true);
        assertThrows(ReglaNegocioException.class, () -> this.alumnoService.actualizarPerfil(220L, dtoTelDup));

        EditarPerfilAlumnoDTO dtoCorreoDup = new EditarPerfilAlumnoDTO(220L, "Mario", "Sánchez", "600111222", "Dir",
                "nuevo@mands.es", LocalDate.of(2000, 1, 1), "12345678Z", null, null, null, null, null, null, null, null, null, null, null, null);
        when(this.personaRepository.existsByCorreoAndIdNot("nuevo@mands.es", 220L)).thenReturn(true);
        assertThrows(ReglaNegocioException.class, () -> this.alumnoService.actualizarPerfil(220L, dtoCorreoDup));

        when(this.personaRepository.existsByDniAndIdNot("87654321X", 220L)).thenReturn(false);
        when(this.usuarioService.esLetraDniValida("87654321X")).thenReturn(true);
        when(this.personaRepository.existsByTelefonoAndIdNot("611222333", 220L)).thenReturn(false);
        when(this.personaRepository.existsByCorreoAndIdNot("nuevo@mands.es", 220L)).thenReturn(false);

        EditarPerfilAlumnoDTO dtoOk = new EditarPerfilAlumnoDTO(220L, "Mario", "Sánchez", "611222333", "Nueva Dir",
                "nuevo@mands.es", LocalDate.of(2000, 1, 1), "87654321X", null, null, null, null, null, null, null, null, null, null, null, null);
        this.alumnoService.actualizarPerfil(220L, dtoOk);
        assertEquals("87654321X", alumno.getDni());
        assertEquals("611222333", alumno.getTelefono());

        // cambiarPassword
        assertThrows(ReglaNegocioException.class, () -> this.alumnoService.cambiarPassword(null, "a", "123456", "123456"));
        when(this.passwordEncoder.matches("mal", "hashActual")).thenReturn(false);
        assertThrows(ReglaNegocioException.class, () -> this.alumnoService.cambiarPassword(220L, "mal", "123456", "123456"));

        when(this.passwordEncoder.matches("ok", "hashActual")).thenReturn(true);
        assertThrows(ReglaNegocioException.class, () -> this.alumnoService.cambiarPassword(220L, "ok", "123", "123"));
        assertThrows(ReglaNegocioException.class, () -> this.alumnoService.cambiarPassword(220L, "ok", "123456", "654321"));

        when(this.passwordEncoder.encode("nueva123")).thenReturn("hashNuevo");
        this.alumnoService.cambiarPassword(220L, "ok", "nueva123", "nueva123");
        assertEquals("hashNuevo", alumno.getPassword());

        // contactarAlumno + fallback SMTP
        assertThrows(ReglaNegocioException.class, () -> this.alumnoService.contactarAlumno(null, "Asunto", "Msg"));
        assertThrows(ReglaNegocioException.class, () -> this.alumnoService.contactarAlumno(999L, "Asunto", "Msg"));
        doThrow(new RuntimeException("SMTP offline")).when(this.emailService)
                .enviarNotificacionCorreo(anyString(), anyString(), anyString(), anyString(), eq(Rol.ALUMNO));
        assertSame(alumno, this.alumnoService.contactarAlumno(220L, "Asunto", "Mensaje"));
    }
}
