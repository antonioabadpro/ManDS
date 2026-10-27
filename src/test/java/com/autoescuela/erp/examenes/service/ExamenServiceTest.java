package com.autoescuela.erp.examenes.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.autoescuela.erp.academico.model.Matricula;
import com.autoescuela.erp.academico.repository.MatriculaRepository;
import com.autoescuela.erp.core.email.EmailService;
import com.autoescuela.erp.core.enums.EstadoClase;
import com.autoescuela.erp.core.enums.EstadoSolicitud;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.enums.TipoExamen;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.examenes.dto.CupoExamenDTO;
import com.autoescuela.erp.examenes.dto.GestionarSolicitudExamenDTO;
import com.autoescuela.erp.examenes.model.CupoExamen;
import com.autoescuela.erp.examenes.model.Examen;
import com.autoescuela.erp.examenes.model.FechaExamen;
import com.autoescuela.erp.examenes.model.SolicitudExamen;
import com.autoescuela.erp.examenes.repository.CupoExamenRepository;
import com.autoescuela.erp.examenes.repository.ExamenRepository;
import com.autoescuela.erp.examenes.repository.FechaExamenRepository;
import com.autoescuela.erp.examenes.repository.SolicitudExamenRepository;
import com.autoescuela.erp.practicas.model.ClasePractica;
import com.autoescuela.erp.practicas.repository.ClasePracticaRepository;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.model.Profesor;
import com.autoescuela.erp.usuarios.repository.AlumnoRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Batería de pruebas unitarias para la lógica de gestión de exámenes DGT,
 * cálculo de cupos, fijación de fechas oficiales y resolución de solicitudes.
 */
@ExtendWith(MockitoExtension.class)
class ExamenServiceTest
{
    @Mock
    private SolicitudExamenRepository solicitudExamenRepository;

    @Mock
    private ExamenRepository examenRepository;

    @Mock
    private MatriculaRepository matriculaRepository;

    @Mock
    private AlumnoRepository alumnoRepository;

    @Mock
    private ClasePracticaRepository clasePracticaRepository;

    @Mock
    private FechaExamenRepository fechaExamenRepository;

    @Mock
    private CupoExamenRepository cupoExamenRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ExamenService examenService;

    private Alumno alumnoEjemplo;
    private Profesor profesorEjemplo;
    private Matricula matriculaEjemplo;

    @BeforeEach
    void setUp()
    {
        this.alumnoEjemplo = new Alumno();
        this.alumnoEjemplo.setNombre("David");
        this.alumnoEjemplo.setApellidos("Ruiz");
        this.alumnoEjemplo.setDni("12345678Z");
        this.alumnoEjemplo.setCorreo("david.ruiz@example.com");

        this.profesorEjemplo = new Profesor();
        this.profesorEjemplo.setNombre("Laura");
        this.profesorEjemplo.setApellidos("Sánchez");
        this.alumnoEjemplo.setProfesor(this.profesorEjemplo);

        this.matriculaEjemplo = new Matricula();
        this.matriculaEjemplo.setEstaActiva(true);
        this.matriculaEjemplo.setPermisoCarnet(TipoCarnet.PERMISO_B);
        this.matriculaEjemplo.setAlumno(this.alumnoEjemplo);
    }

    // =========================================================================
    // 1. FIJAR FECHAS OFICIALES DGT
    // =========================================================================

    @Test
    @DisplayName("Fijar fechas de examen exitosamente cuando son laborables y fecha 2 posterior a fecha 1")
    void testFijarFechasExamen_Exito()
    {
        LocalDate mes = LocalDate.of(2026, 11, 1);
        LocalDateTime f1 = LocalDateTime.of(2026, 11, 12, 8, 30); // Jueves laborable
        LocalDateTime f2 = LocalDateTime.of(2026, 11, 26, 8, 30); // Jueves laborable

        when(this.fechaExamenRepository.existsByMes(mes)).thenReturn(false);
        when(this.fechaExamenRepository.save(any(FechaExamen.class))).thenAnswer(i -> i.getArgument(0));

        FechaExamen resultado = this.examenService.fijarFechasExamen(mes, f1, f2);

        assertNotNull(resultado);
        assertEquals(mes, resultado.getMes());
        assertEquals(f1, resultado.getFechaHora1());
        assertEquals(f2, resultado.getFechaHora2());
    }

    @Test
    @DisplayName("Lanza excepción si fecha 2 es anterior a fecha 1")
    void testFijarFechasExamen_ErrorFecha2Anterior()
    {
        LocalDate mes = LocalDate.of(2026, 11, 1);
        LocalDateTime f1 = LocalDateTime.of(2026, 11, 26, 8, 30);
        LocalDateTime f2 = LocalDateTime.of(2026, 11, 12, 8, 30);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () ->
                this.examenService.fijarFechasExamen(mes, f1, f2));

        assertTrue(ex.getMessage().contains("igual o posterior"));
    }

    @Test
    @DisplayName("Lanza excepción si alguna fecha coincide en fin de semana")
    void testFijarFechasExamen_ErrorFinDeSemana()
    {
        LocalDate mes = LocalDate.of(2026, 11, 1);
        LocalDateTime f1 = LocalDateTime.of(2026, 11, 14, 8, 30); // Sábado
        LocalDateTime f2 = LocalDateTime.of(2026, 11, 26, 8, 30);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () ->
                this.examenService.fijarFechasExamen(mes, f1, f2));

        assertTrue(ex.getMessage().contains("fin de semana"));
    }

    @Test
    @DisplayName("Lanza excepción si alguna fecha coincide en periodo de vacaciones oficial")
    void testFijarFechasExamen_ErrorVacaciones()
    {
        LocalDate mes = LocalDate.of(2026, 8, 1);
        LocalDateTime f1 = LocalDateTime.of(2026, 8, 6, 8, 30); // 1-14 agosto vacaciones
        LocalDateTime f2 = LocalDateTime.of(2026, 8, 20, 8, 30);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () ->
                this.examenService.fijarFechasExamen(mes, f1, f2));

        assertTrue(ex.getMessage().contains("día inhábil"));
    }

    @Test
    @DisplayName("Lanza excepción si las fechas del mes ya fueron fijadas previamente (Irreversibilidad)")
    void testFijarFechasExamen_ErrorYaFijadas()
    {
        LocalDate mes = LocalDate.of(2026, 11, 1);
        LocalDateTime f1 = LocalDateTime.of(2026, 11, 12, 8, 30);
        LocalDateTime f2 = LocalDateTime.of(2026, 11, 26, 8, 30);

        when(this.fechaExamenRepository.existsByMes(mes)).thenReturn(true);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () ->
                this.examenService.fijarFechasExamen(mes, f1, f2));

        assertTrue(ex.getMessage().contains("ya han sido fijadas"));
    }

    // =========================================================================
    // 2. FIJAR CUPOS DE EXAMEN POR CARNET
    // =========================================================================

    @Test
    @DisplayName("Fijar cupos del mes correctamente")
    void testFijarCuposExamen_Exito()
    {
        LocalDate mes = LocalDate.of(2026, 11, 1);
        when(this.cupoExamenRepository.existsByMes(mes)).thenReturn(false);
        when(this.cupoExamenRepository.save(any(CupoExamen.class))).thenAnswer(i -> i.getArgument(0));

        List<CupoExamenDTO> items = List.of(
                new CupoExamenDTO(TipoCarnet.PERMISO_B, 5, 4),
                new CupoExamenDTO(TipoCarnet.PERMISO_A2, 4, 3)
        );

        List<CupoExamen> resultado = this.examenService.fijarCuposExamen(mes, items);

        assertEquals(2, resultado.size());
        assertEquals(5, resultado.get(0).getCupoTeorico());
        assertEquals(4, resultado.get(0).getCupoPractico());
    }

    @Test
    @DisplayName("Lanza excepción si los cupos del mes ya fueron fijados previamente (Irreversibilidad)")
    void testFijarCuposExamen_ErrorYaFijados()
    {
        LocalDate mes = LocalDate.of(2026, 11, 1);
        when(this.cupoExamenRepository.existsByMes(mes)).thenReturn(true);

        List<CupoExamenDTO> items = List.of(new CupoExamenDTO(TipoCarnet.PERMISO_B, 5, 4));

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () ->
                this.examenService.fijarCuposExamen(mes, items));

        assertTrue(ex.getMessage().contains("ya han sido fijados"));
    }

    // =========================================================================
    // 3. GESTIÓN Y ALGORITMO DE CUPOS Y CLASES PENDIENTES
    // =========================================================================

    @Test
    @DisplayName("Permite citar si las clases pendientes son anteriores a la fecha de examen")
    void testObtenerGestionarSolicitud_ClasesPendientesAnterioresPermitidas()
    {
        SolicitudExamen solicitud = new SolicitudExamen();
        solicitud.setAlumno(this.alumnoEjemplo);
        solicitud.setMatricula(this.matriculaEjemplo);
        solicitud.setTipo(TipoExamen.PRACTICO);
        solicitud.setFechaHora(LocalDateTime.of(2026, 11, 5, 10, 0));

        LocalDateTime f1 = LocalDateTime.of(2026, 11, 12, 8, 30);
        LocalDateTime f2 = LocalDateTime.of(2026, 11, 26, 8, 30);
        FechaExamen fechas = new FechaExamen(LocalDate.of(2026, 11, 1), f1, f2);

        CupoExamen cupoB = new CupoExamen(LocalDate.of(2026, 11, 1), TipoCarnet.PERMISO_B, 5, 4);

        // Clase pendiente fechada el 10 de noviembre (anterior a f1 y f2)
        ClasePractica claseAnterior = new ClasePractica();
        claseAnterior.setFechaHora(LocalDateTime.of(2026, 11, 10, 10, 0));
        claseAnterior.setEstadoClase(EstadoClase.PENDIENTE);

        when(this.solicitudExamenRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(this.fechaExamenRepository.findByMes(LocalDate.of(2026, 11, 1))).thenReturn(Optional.of(fechas));
        when(this.cupoExamenRepository.findByMesAndTipoPermiso(LocalDate.of(2026, 11, 1), TipoCarnet.PERMISO_B))
                .thenReturn(Optional.of(cupoB));
        when(this.clasePracticaRepository.countByAlumnoAndEstadoClase(this.alumnoEjemplo, EstadoClase.RECIBIDA))
                .thenReturn(3);
        when(this.clasePracticaRepository.findByAlumnoAndEstadoClase(this.alumnoEjemplo, EstadoClase.PENDIENTE))
                .thenReturn(List.of(claseAnterior));
        when(this.solicitudExamenRepository.countAlumnosCitados(f1, TipoCarnet.PERMISO_B, TipoExamen.PRACTICO))
                .thenReturn(2L);
        when(this.solicitudExamenRepository.countAlumnosCitados(f2, TipoCarnet.PERMISO_B, TipoExamen.PRACTICO))
                .thenReturn(1L);

        GestionarSolicitudExamenDTO diagnostico = this.examenService.obtenerGestionarSolicitud(1L);

        assertTrue(diagnostico.puedeAceptar());
        assertTrue(diagnostico.convocatoria1().disponible());
        assertTrue(diagnostico.convocatoria2().disponible());
    }

    @Test
    @DisplayName("Bloquea Fecha 1 si hay clases entre Fecha 1 y Fecha 2, pero permite Fecha 2")
    void testObtenerGestionarSolicitud_ClaseEntreFechasBloqueaFecha1PeroPermiteFecha2()
    {
        SolicitudExamen solicitud = new SolicitudExamen();
        solicitud.setAlumno(this.alumnoEjemplo);
        solicitud.setMatricula(this.matriculaEjemplo);
        solicitud.setTipo(TipoExamen.PRACTICO);
        solicitud.setFechaHora(LocalDateTime.of(2026, 11, 5, 10, 0));

        LocalDateTime f1 = LocalDateTime.of(2026, 11, 12, 8, 30);
        LocalDateTime f2 = LocalDateTime.of(2026, 11, 26, 8, 30);
        FechaExamen fechas = new FechaExamen(LocalDate.of(2026, 11, 1), f1, f2);

        CupoExamen cupoB = new CupoExamen(LocalDate.of(2026, 11, 1), TipoCarnet.PERMISO_B, 5, 4);

        // Clase pendiente el 20 de noviembre (posterior a f1, pero anterior a f2)
        ClasePractica claseIntermedia = new ClasePractica();
        claseIntermedia.setFechaHora(LocalDateTime.of(2026, 11, 20, 10, 0));
        claseIntermedia.setEstadoClase(EstadoClase.PENDIENTE);

        when(this.solicitudExamenRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(this.fechaExamenRepository.findByMes(LocalDate.of(2026, 11, 1))).thenReturn(Optional.of(fechas));
        when(this.cupoExamenRepository.findByMesAndTipoPermiso(LocalDate.of(2026, 11, 1), TipoCarnet.PERMISO_B))
                .thenReturn(Optional.of(cupoB));
        when(this.clasePracticaRepository.countByAlumnoAndEstadoClase(this.alumnoEjemplo, EstadoClase.RECIBIDA))
                .thenReturn(4);
        when(this.clasePracticaRepository.findByAlumnoAndEstadoClase(this.alumnoEjemplo, EstadoClase.PENDIENTE))
                .thenReturn(List.of(claseIntermedia));
        when(this.solicitudExamenRepository.countAlumnosCitados(f1, TipoCarnet.PERMISO_B, TipoExamen.PRACTICO))
                .thenReturn(1L);
        when(this.solicitudExamenRepository.countAlumnosCitados(f2, TipoCarnet.PERMISO_B, TipoExamen.PRACTICO))
                .thenReturn(1L);

        GestionarSolicitudExamenDTO diagnostico = this.examenService.obtenerGestionarSolicitud(1L);

        assertTrue(diagnostico.puedeAceptar());
        assertFalse(diagnostico.convocatoria1().disponible());
        assertTrue(diagnostico.convocatoria1().motivoNoDisponible().contains("clase(s) pendiente(s) posterior(es)"));
        assertTrue(diagnostico.convocatoria2().disponible());
    }

    @Test
    @DisplayName("Bloquea convocatoria con motivo 'Convocatoria ya celebrada' si su fecha pertenece al pasado")
    void testObtenerGestionarSolicitud_FechaEnElPasadoBloqueada()
    {
        SolicitudExamen solicitud = new SolicitudExamen();
        solicitud.setAlumno(this.alumnoEjemplo);
        solicitud.setMatricula(this.matriculaEjemplo);
        solicitud.setTipo(TipoExamen.PRACTICO);
        solicitud.setFechaHora(LocalDateTime.now().minusDays(2));

        // f1 en el pasado, f2 en el futuro
        LocalDateTime f1 = LocalDateTime.now().minusDays(5);
        LocalDateTime f2 = LocalDateTime.now().plusDays(10);
        FechaExamen fechas = new FechaExamen(LocalDate.now().withDayOfMonth(1), f1, f2);

        CupoExamen cupoB = new CupoExamen(LocalDate.now().withDayOfMonth(1), TipoCarnet.PERMISO_B, 5, 4);

        when(this.solicitudExamenRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(this.fechaExamenRepository.findByMes(LocalDate.now().withDayOfMonth(1))).thenReturn(Optional.of(fechas));
        when(this.cupoExamenRepository.findByMesAndTipoPermiso(LocalDate.now().withDayOfMonth(1), TipoCarnet.PERMISO_B))
                .thenReturn(Optional.of(cupoB));
        when(this.clasePracticaRepository.countByAlumnoAndEstadoClase(this.alumnoEjemplo, EstadoClase.RECIBIDA))
                .thenReturn(3);
        when(this.clasePracticaRepository.findByAlumnoAndEstadoClase(this.alumnoEjemplo, EstadoClase.PENDIENTE))
                .thenReturn(List.of());
        when(this.solicitudExamenRepository.countAlumnosCitados(f1, TipoCarnet.PERMISO_B, TipoExamen.PRACTICO))
                .thenReturn(0L);
        when(this.solicitudExamenRepository.countAlumnosCitados(f2, TipoCarnet.PERMISO_B, TipoExamen.PRACTICO))
                .thenReturn(0L);

        GestionarSolicitudExamenDTO diagnostico = this.examenService.obtenerGestionarSolicitud(1L);

        assertFalse(diagnostico.convocatoria1().disponible());
        assertEquals("Convocatoria ya celebrada", diagnostico.convocatoria1().motivoNoDisponible());
        assertTrue(diagnostico.convocatoria2().disponible());
    }

    @Test
    @DisplayName("Bloquea examen práctico si el alumno tiene 0 clases en estado RECIBIDA")
    void testObtenerGestionarSolicitud_SinClasesRecibidasBloqueado()
    {
        SolicitudExamen solicitud = new SolicitudExamen();
        solicitud.setAlumno(this.alumnoEjemplo);
        solicitud.setMatricula(this.matriculaEjemplo);
        solicitud.setTipo(TipoExamen.PRACTICO);
        solicitud.setFechaHora(LocalDateTime.of(2026, 11, 5, 10, 0));

        LocalDateTime f1 = LocalDateTime.of(2026, 11, 12, 8, 30);
        LocalDateTime f2 = LocalDateTime.of(2026, 11, 26, 8, 30);
        FechaExamen fechas = new FechaExamen(LocalDate.of(2026, 11, 1), f1, f2);

        when(this.solicitudExamenRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(this.fechaExamenRepository.findByMes(LocalDate.of(2026, 11, 1))).thenReturn(Optional.of(fechas));
        when(this.clasePracticaRepository.countByAlumnoAndEstadoClase(this.alumnoEjemplo, EstadoClase.RECIBIDA))
                .thenReturn(0);

        GestionarSolicitudExamenDTO diagnostico = this.examenService.obtenerGestionarSolicitud(1L);

        assertFalse(diagnostico.puedeAceptar());
        assertNotNull(diagnostico.motivoBloqueoGeneral());
        assertTrue(diagnostico.motivoBloqueoGeneral().contains("RECIBIDA"));
        assertFalse(diagnostico.convocatoria1().disponible());
        assertFalse(diagnostico.convocatoria2().disponible());
    }

    @Test
    @DisplayName("Bloquea solicitud si el alumno no dispone de matrícula activa")
    void testObtenerGestionarSolicitud_MatriculaInactivaBloqueado()
    {
        SolicitudExamen solicitud = new SolicitudExamen();
        solicitud.setAlumno(this.alumnoEjemplo);
        this.matriculaEjemplo.setEstaActiva(false);
        solicitud.setMatricula(this.matriculaEjemplo);
        solicitud.setTipo(TipoExamen.TEORICO);
        solicitud.setFechaHora(LocalDateTime.of(2026, 11, 5, 10, 0));

        LocalDateTime f1 = LocalDateTime.of(2026, 11, 12, 8, 30);
        LocalDateTime f2 = LocalDateTime.of(2026, 11, 26, 8, 30);
        FechaExamen fechas = new FechaExamen(LocalDate.of(2026, 11, 1), f1, f2);

        when(this.solicitudExamenRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(this.fechaExamenRepository.findByMes(LocalDate.of(2026, 11, 1))).thenReturn(Optional.of(fechas));

        GestionarSolicitudExamenDTO diagnostico = this.examenService.obtenerGestionarSolicitud(1L);

        assertFalse(diagnostico.puedeAceptar());
        assertNotNull(diagnostico.motivoBloqueoGeneral());
        assertTrue(diagnostico.motivoBloqueoGeneral().contains("matrícula activa"));
    }

    @Test
    @DisplayName("buscarSolicitudesConFiltros pasa null al repositorio cuando estado es null (Todos los estados)")
    void testBuscarSolicitudesConFiltros_TodosLosEstadosPasaNullAlRepositorio()
    {
        when(this.solicitudExamenRepository.buscarConFiltros(eq("David"), eq(100L), eq(TipoCarnet.PERMISO_B), eq(TipoExamen.PRACTICO), eq(null)))
                .thenReturn(List.of());

        List<SolicitudExamen> resultado = this.examenService.buscarSolicitudesConFiltros("David", 100L, TipoCarnet.PERMISO_B, TipoExamen.PRACTICO, null);

        assertNotNull(resultado);
        verify(this.solicitudExamenRepository).buscarConFiltros("David", 100L, TipoCarnet.PERMISO_B, TipoExamen.PRACTICO, null);
    }

    // =========================================================================
    // 4. ACEPTAR Y RECHAZAR SOLICITUDES
    // =========================================================================

    @Test
    @DisplayName("Aceptar solicitud exitosamente cita al alumno, bloquea clases del vehículo y envía citación")
    void testAceptarSolicitudExamen_Exito()
    {
        SolicitudExamen solicitud = new SolicitudExamen();
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);
        solicitud.setAlumno(this.alumnoEjemplo);
        solicitud.setProfesor(this.profesorEjemplo);
        solicitud.setMatricula(this.matriculaEjemplo);
        solicitud.setTipo(TipoExamen.PRACTICO);
        solicitud.setFechaHora(LocalDateTime.of(2026, 11, 10, 10, 0));

        LocalDateTime f1 = LocalDateTime.of(2026, 11, 12, 8, 30);
        LocalDateTime f2 = LocalDateTime.of(2026, 11, 26, 8, 30);
        FechaExamen fechas = new FechaExamen(LocalDate.of(2026, 11, 1), f1, f2);

        CupoExamen cupoB = new CupoExamen(LocalDate.of(2026, 11, 1), TipoCarnet.PERMISO_B, 5, 4);

        when(this.solicitudExamenRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(this.fechaExamenRepository.findByMes(LocalDate.of(2026, 11, 1))).thenReturn(Optional.of(fechas));
        when(this.cupoExamenRepository.findByMesAndTipoPermiso(LocalDate.of(2026, 11, 1), TipoCarnet.PERMISO_B))
                .thenReturn(Optional.of(cupoB));
        when(this.clasePracticaRepository.countByAlumnoAndEstadoClase(this.alumnoEjemplo, EstadoClase.RECIBIDA))
                .thenReturn(5);
        when(this.clasePracticaRepository.findByAlumnoAndEstadoClase(this.alumnoEjemplo, EstadoClase.PENDIENTE))
                .thenReturn(List.of());
        when(this.solicitudExamenRepository.countAlumnosCitados(f1, TipoCarnet.PERMISO_B, TipoExamen.PRACTICO))
                .thenReturn(1L);
        when(this.examenRepository.save(any(Examen.class))).thenAnswer(i -> i.getArgument(0));
        when(this.solicitudExamenRepository.save(any(SolicitudExamen.class))).thenAnswer(i -> i.getArgument(0));

        SolicitudExamen resultado = this.examenService.aceptarSolicitudExamen(1L, 1);

        assertEquals(EstadoSolicitud.ACEPTADA, resultado.getEstado());
        assertNotNull(resultado.getExamen());
        assertEquals(f1, resultado.getExamen().getFechaHora());
        verify(this.emailService).enviarNotificacionAlumno(eq("david.ruiz@example.com"), anyString(), anyString());
    }

    @Test
    @DisplayName("Rechazar solicitud con motivo válido cambia el estado y envía correo")
    void testRechazarSolicitudExamen_Exito()
    {
        SolicitudExamen solicitud = new SolicitudExamen();
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);
        solicitud.setAlumno(this.alumnoEjemplo);
        solicitud.setMatricula(this.matriculaEjemplo);

        when(this.solicitudExamenRepository.findById(2L)).thenReturn(Optional.of(solicitud));
        when(this.solicitudExamenRepository.save(any(SolicitudExamen.class))).thenAnswer(i -> i.getArgument(0));

        SolicitudExamen resultado = this.examenService.rechazarSolicitudExamen(2L, "Falta dominio en estacionamiento.");

        assertEquals(EstadoSolicitud.RECHAZADA, resultado.getEstado());
        assertEquals("Falta dominio en estacionamiento.", resultado.getComentarioJustificacion());
        verify(this.emailService).enviarNotificacionAlumno(eq("david.ruiz@example.com"), anyString(), anyString());
    }

    @Test
    @DisplayName("Lanza excepción si se intenta rechazar sin indicar motivo obligatorio")
    void testRechazarSolicitudExamen_ErrorSinMotivo()
    {
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () ->
                this.examenService.rechazarSolicitudExamen(2L, "   "));

        assertTrue(ex.getMessage().contains("obligatoriamente el motivo"));
    }
}
