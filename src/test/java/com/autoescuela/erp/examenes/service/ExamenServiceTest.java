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
import com.autoescuela.erp.core.enums.Rol;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.enums.TipoExamen;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.core.util.FechaHoraUtil;
import com.autoescuela.erp.examenes.dto.CalificarExamenDTO;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
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
        LocalDate mesActual = FechaHoraUtil.hoy().withDayOfMonth(1);
        LocalDateTime ahora = FechaHoraUtil.ahora().withSecond(0).withNano(0);

        SolicitudExamen solicitud = new SolicitudExamen();
        solicitud.setAlumno(this.alumnoEjemplo);
        solicitud.setMatricula(this.matriculaEjemplo);
        solicitud.setTipo(TipoExamen.PRACTICO);
        solicitud.setFechaHora(ahora.minusDays(1));

        LocalDateTime f1 = ahora.plusDays(5).withHour(8).withMinute(30);
        LocalDateTime f2 = ahora.plusDays(15).withHour(8).withMinute(30);
        FechaExamen fechas = new FechaExamen(mesActual, f1, f2);

        CupoExamen cupoB = new CupoExamen(mesActual, TipoCarnet.PERMISO_B, 5, 4);

        // Clase pendiente anterior a f1 y f2
        ClasePractica claseAnterior = new ClasePractica();
        claseAnterior.setFechaHora(ahora.plusDays(2).withHour(10).withMinute(0));
        claseAnterior.setEstadoClase(EstadoClase.PENDIENTE);

        when(this.solicitudExamenRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(this.fechaExamenRepository.findByMes(mesActual)).thenReturn(Optional.of(fechas));
        when(this.cupoExamenRepository.findByMesAndTipoPermiso(mesActual, TipoCarnet.PERMISO_B))
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
        LocalDate mesActual = FechaHoraUtil.hoy().withDayOfMonth(1);
        LocalDateTime ahora = FechaHoraUtil.ahora().withSecond(0).withNano(0);

        SolicitudExamen solicitud = new SolicitudExamen();
        solicitud.setAlumno(this.alumnoEjemplo);
        solicitud.setMatricula(this.matriculaEjemplo);
        solicitud.setTipo(TipoExamen.PRACTICO);
        solicitud.setFechaHora(ahora.minusDays(1));

        LocalDateTime f1 = ahora.plusDays(5).withHour(8).withMinute(30);
        LocalDateTime f2 = ahora.plusDays(15).withHour(8).withMinute(30);
        FechaExamen fechas = new FechaExamen(mesActual, f1, f2);

        CupoExamen cupoB = new CupoExamen(mesActual, TipoCarnet.PERMISO_B, 5, 4);

        // Clase pendiente posterior a f1, pero anterior a f2
        ClasePractica claseIntermedia = new ClasePractica();
        claseIntermedia.setFechaHora(ahora.plusDays(10).withHour(10).withMinute(0));
        claseIntermedia.setEstadoClase(EstadoClase.PENDIENTE);

        when(this.solicitudExamenRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(this.fechaExamenRepository.findByMes(mesActual)).thenReturn(Optional.of(fechas));
        when(this.cupoExamenRepository.findByMesAndTipoPermiso(mesActual, TipoCarnet.PERMISO_B))
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
        solicitud.setFechaHora(FechaHoraUtil.ahora().minusDays(2));

        // f1 en el pasado, f2 en el futuro
        LocalDateTime f1 = FechaHoraUtil.ahora().minusDays(5);
        LocalDateTime f2 = FechaHoraUtil.ahora().plusDays(10);
        FechaExamen fechas = new FechaExamen(FechaHoraUtil.hoy().withDayOfMonth(1), f1, f2);

        CupoExamen cupoB = new CupoExamen(FechaHoraUtil.hoy().withDayOfMonth(1), TipoCarnet.PERMISO_B, 5, 4);

        when(this.solicitudExamenRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(this.fechaExamenRepository.findByMes(FechaHoraUtil.hoy().withDayOfMonth(1))).thenReturn(Optional.of(fechas));
        when(this.cupoExamenRepository.findByMesAndTipoPermiso(FechaHoraUtil.hoy().withDayOfMonth(1), TipoCarnet.PERMISO_B))
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
        LocalDate mesActual = FechaHoraUtil.hoy().withDayOfMonth(1);
        LocalDateTime ahora = FechaHoraUtil.ahora().withSecond(0).withNano(0);

        SolicitudExamen solicitud = new SolicitudExamen();
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);
        solicitud.setAlumno(this.alumnoEjemplo);
        solicitud.setProfesor(this.profesorEjemplo);
        solicitud.setMatricula(this.matriculaEjemplo);
        solicitud.setTipo(TipoExamen.PRACTICO);
        solicitud.setFechaHora(ahora.minusDays(1));

        LocalDateTime f1 = ahora.plusDays(5).withHour(8).withMinute(30);
        LocalDateTime f2 = ahora.plusDays(15).withHour(8).withMinute(30);
        FechaExamen fechas = new FechaExamen(mesActual, f1, f2);

        CupoExamen cupoB = new CupoExamen(mesActual, TipoCarnet.PERMISO_B, 5, 4);

        when(this.solicitudExamenRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(this.fechaExamenRepository.findByMes(mesActual)).thenReturn(Optional.of(fechas));
        when(this.cupoExamenRepository.findByMesAndTipoPermiso(mesActual, TipoCarnet.PERMISO_B))
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
        verify(this.emailService).enviarNotificacionCorreo(eq("david.ruiz@example.com"), any(), anyString(), anyString(), eq(Rol.ALUMNO));
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
        verify(this.emailService).enviarNotificacionCorreo(eq("david.ruiz@example.com"), any(), anyString(), anyString(), eq(Rol.ALUMNO));
    }

    @Test
    @DisplayName("Lanza excepción si se intenta rechazar sin indicar motivo obligatorio")
    void testRechazarSolicitudExamen_ErrorSinMotivo()
    {
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () ->
                this.examenService.rechazarSolicitudExamen(2L, "   "));

        assertTrue(ex.getMessage().contains("obligatoriamente el motivo"));
    }

    // =========================================================================
    // 5. NAVEGACIÓN MENSUAL Y ESTADO "SIN FIJAR"
    // =========================================================================

    @Test
    @DisplayName("Mes sin fechas fijadas muestra 'Sin Fijar' en ambas convocatorias")
    void testObtenerGestionarSolicitud_MesSinFechasMuestraSinFijar()
    {
        SolicitudExamen solicitud = new SolicitudExamen();
        solicitud.setAlumno(this.alumnoEjemplo);
        solicitud.setMatricula(this.matriculaEjemplo);
        solicitud.setTipo(TipoExamen.TEORICO);
        solicitud.setFechaHora(LocalDateTime.of(2026, 11, 1, 10, 0));

        LocalDate mesNoviembre = LocalDate.of(2026, 11, 1);

        when(this.solicitudExamenRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(this.fechaExamenRepository.findByMes(mesNoviembre)).thenReturn(Optional.empty());

        GestionarSolicitudExamenDTO diagnostico = this.examenService.obtenerGestionarSolicitud(1L, mesNoviembre);

        assertFalse(diagnostico.puedeAceptar());
        assertFalse(diagnostico.convocatoria1().disponible());
        assertEquals("Sin Fijar", diagnostico.convocatoria1().motivoNoDisponible());
        assertFalse(diagnostico.convocatoria2().disponible());
        assertEquals("Sin Fijar", diagnostico.convocatoria2().motivoNoDisponible());
        assertEquals(mesNoviembre, diagnostico.mesSeleccionado());
    }

    @Test
    @DisplayName("Mes con fechas pero sin cupos fijados para el carnet muestra 'Sin Fijar' en ambas convocatorias")
    void testObtenerGestionarSolicitud_MesSinCuposMuestraSinFijar()
    {
        SolicitudExamen solicitud = new SolicitudExamen();
        solicitud.setAlumno(this.alumnoEjemplo);
        solicitud.setMatricula(this.matriculaEjemplo);
        solicitud.setTipo(TipoExamen.TEORICO);
        solicitud.setFechaHora(LocalDateTime.of(2026, 11, 1, 10, 0));

        LocalDate mesNoviembre = LocalDate.of(2026, 11, 1);
        LocalDateTime f1 = LocalDateTime.of(2026, 11, 12, 8, 30);
        LocalDateTime f2 = LocalDateTime.of(2026, 11, 26, 8, 30);
        FechaExamen fechas = new FechaExamen(mesNoviembre, f1, f2);

        when(this.solicitudExamenRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(this.fechaExamenRepository.findByMes(mesNoviembre)).thenReturn(Optional.of(fechas));
        when(this.cupoExamenRepository.findByMesAndTipoPermiso(mesNoviembre, TipoCarnet.PERMISO_B)).thenReturn(Optional.empty());

        GestionarSolicitudExamenDTO diagnostico = this.examenService.obtenerGestionarSolicitud(1L, mesNoviembre);

        assertFalse(diagnostico.puedeAceptar());
        assertFalse(diagnostico.convocatoria1().disponible());
        assertEquals("Sin Fijar", diagnostico.convocatoria1().motivoNoDisponible());
        assertFalse(diagnostico.convocatoria2().disponible());
        assertEquals("Sin Fijar", diagnostico.convocatoria2().motivoNoDisponible());
    }

    @Test
    @DisplayName("Navegación hacia mes anterior al actual fuerza el mes actual")
    void testObtenerGestionarSolicitud_MesAnteriorAlActualForzadoAlMesActual()
    {
        SolicitudExamen solicitud = new SolicitudExamen();
        solicitud.setAlumno(this.alumnoEjemplo);
        solicitud.setMatricula(this.matriculaEjemplo);
        solicitud.setTipo(TipoExamen.TEORICO);

        LocalDate mesPasado = LocalDate.of(2020, 1, 1);
        LocalDate mesActual = FechaHoraUtil.hoy().withDayOfMonth(1);

        when(this.solicitudExamenRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(this.fechaExamenRepository.findByMes(mesActual)).thenReturn(Optional.empty());

        GestionarSolicitudExamenDTO diagnostico = this.examenService.obtenerGestionarSolicitud(1L, mesPasado);

        assertEquals(mesActual, diagnostico.mesSeleccionado());
    }

    @Test
    @DisplayName("Aceptar solicitud con mes especificado cita en la fecha oficial de dicho mes")
    void testAceptarSolicitudExamen_ConMesEspecificado_Exito()
    {
        SolicitudExamen solicitud = new SolicitudExamen();
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);
        solicitud.setAlumno(this.alumnoEjemplo);
        solicitud.setProfesor(this.profesorEjemplo);
        solicitud.setMatricula(this.matriculaEjemplo);
        solicitud.setTipo(TipoExamen.PRACTICO);
        solicitud.setFechaHora(LocalDateTime.of(2026, 10, 20, 10, 0));

        LocalDate mesFuturo = FechaHoraUtil.hoy().plusMonths(1).withDayOfMonth(1);
        LocalDateTime f1Futuro = mesFuturo.plusDays(11).atTime(8, 30);
        LocalDateTime f2Futuro = mesFuturo.plusDays(25).atTime(8, 30);
        FechaExamen fechasFuturas = new FechaExamen(mesFuturo, f1Futuro, f2Futuro);

        CupoExamen cupoFuturo = new CupoExamen(mesFuturo, TipoCarnet.PERMISO_B, 5, 4);

        when(this.solicitudExamenRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(this.fechaExamenRepository.findByMes(mesFuturo)).thenReturn(Optional.of(fechasFuturas));
        when(this.cupoExamenRepository.findByMesAndTipoPermiso(mesFuturo, TipoCarnet.PERMISO_B))
                .thenReturn(Optional.of(cupoFuturo));
        when(this.clasePracticaRepository.countByAlumnoAndEstadoClase(this.alumnoEjemplo, EstadoClase.RECIBIDA))
                .thenReturn(5);
        when(this.clasePracticaRepository.findByAlumnoAndEstadoClase(this.alumnoEjemplo, EstadoClase.PENDIENTE))
                .thenReturn(List.of());
        when(this.solicitudExamenRepository.countAlumnosCitados(f1Futuro, TipoCarnet.PERMISO_B, TipoExamen.PRACTICO))
                .thenReturn(0L);
        when(this.examenRepository.save(any(Examen.class))).thenAnswer(i -> i.getArgument(0));
        when(this.solicitudExamenRepository.save(any(SolicitudExamen.class))).thenAnswer(i -> i.getArgument(0));

        SolicitudExamen resultado = this.examenService.aceptarSolicitudExamen(1L, 1, mesFuturo);

        assertEquals(EstadoSolicitud.ACEPTADA, resultado.getEstado());
        assertNotNull(resultado.getExamen());
        assertEquals(f1Futuro, resultado.getExamen().getFechaHora());
        verify(this.emailService).enviarNotificacionCorreo(eq("david.ruiz@example.com"), any(), anyString(), anyString(), eq(Rol.ALUMNO));
    }

    @Test
    @DisplayName("Calificar examen oficial antes de su fecha y hora lanza ReglaNegocioException")
    void testCalificarExamenAntesDeFechaHoraLanzaExcepcion()
    {
        Long examenId = 50L;
        Examen examen = new Examen();
        examen.setFechaHora(FechaHoraUtil.ahora().plusHours(2));
        examen.setTipo(TipoExamen.PRACTICO);
        examen.setDuracion(35);

        when(this.examenRepository.findById(examenId)).thenReturn(Optional.of(examen));

        CalificarExamenDTO dto = new CalificarExamenDTO(examenId, true);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () ->
                this.examenService.calificarExamenOficial(dto));

        assertTrue(ex.getMessage().contains("antes de su fecha y hora de finalización"));
    }

    @Test
    @DisplayName("Calificar examen oficial ya evaluado lanza ReglaNegocioException")
    void testCalificarExamenYaCalificadoLanzaExcepcion()
    {
        Long examenId = 51L;
        Examen examen = new Examen();
        examen.setFechaHora(FechaHoraUtil.ahora().minusHours(2));
        examen.setEsApto(true);
        examen.setTipo(TipoExamen.TEORICO);
        examen.setDuracion(30);

        when(this.examenRepository.findById(examenId)).thenReturn(Optional.of(examen));

        CalificarExamenDTO dto = new CalificarExamenDTO(examenId, false);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () ->
                this.examenService.calificarExamenOficial(dto));

        assertTrue(ex.getMessage().contains("ya ha sido calificado previamente"));
    }

    @Test
    @DisplayName("Calificar examen oficial tras su finalización registra la nota con éxito")
    void testCalificarExamenOficialExito()
    {
        Long examenId = 52L;
        Examen examen = new Examen();
        examen.setFechaHora(FechaHoraUtil.ahora().minusMinutes(45));
        examen.setTipo(TipoExamen.TEORICO);
        examen.setAlumno(this.alumnoEjemplo);
        examen.setDuracion(30);

        when(this.examenRepository.findById(examenId)).thenReturn(Optional.of(examen));
        when(this.examenRepository.save(any(Examen.class))).thenAnswer(i -> i.getArgument(0));

        CalificarExamenDTO dto = new CalificarExamenDTO(examenId, true);

        Examen resultado = this.examenService.calificarExamenOficial(dto);

        assertNotNull(resultado);
        assertTrue(resultado.getEsApto());
        verify(this.examenRepository).save(examen);
    }

    @Test
    @DisplayName("Registrar solicitud de examen sin fecha asigna fechaHora actual en España")
    void testRegistrarSolicitudExamen_FechaNulaAsignaFechaActual()
    {
        when(this.solicitudExamenRepository.save(any(SolicitudExamen.class))).thenAnswer(i -> i.getArgument(0));

        SolicitudExamen resultado = this.examenService.registrarSolicitudExamen(
                this.alumnoEjemplo, TipoExamen.TEORICO, null, this.matriculaEjemplo);

        assertNotNull(resultado);
        assertNotNull(resultado.getFechaHora());
        assertEquals(EstadoSolicitud.PENDIENTE, resultado.getEstado());

        GestionarSolicitudExamenDTO dtoSinMes = new GestionarSolicitudExamenDTO(
                resultado, null, null, false, "Sin cupo");
        assertEquals(FechaHoraUtil.hoy().withDayOfMonth(1), dtoSinMes.mesSeleccionado());
    }

    @Test
    @DisplayName("Consultas auxiliares y guardado de ExamenService gestionan argumentos nulos y delegan en repositorios")
    void testConsultasAuxiliaresExamenService()
    {
        assertTrue(this.examenService.buscarExamenPorId(null).isEmpty());
        assertTrue(this.examenService.obtenerExamenesPorAlumno(null).isEmpty());
        assertTrue(this.examenService.obtenerExamenesPracticosPorProfesor(null).isEmpty());
        assertFalse(this.examenService.tieneTeoricoAprobado(null));
        assertFalse(this.examenService.tienePracticoAprobado(null));
        assertEquals(0L, this.examenService.contarAptosPorAlumno(null));
        assertEquals(0L, this.examenService.contarNoAptosPorAlumno(null));
        assertTrue(this.examenService.obtenerSolicitudesPorMatriculaDesc(null).isEmpty());
        assertTrue(this.examenService.obtenerSolicitudesPorAlumnoDesc(null).isEmpty());
        assertTrue(this.examenService.obtenerSolicitudesRecientesPorAlumno(null).isEmpty());
        assertFalse(this.examenService.tieneSolicitudPendiente(null));
        assertFalse(this.examenService.tieneSolicitudAceptadaConExamenPendiente(null));
        assertTrue(this.examenService.obtenerFechasExamen(null).isEmpty());
        assertTrue(this.examenService.obtenerCuposExamen(null).isEmpty());
        assertTrue(this.examenService.buscarSolicitudPorId(null).isEmpty());
        assertThrows(ReglaNegocioException.class,
                () -> this.examenService.registrarSolicitudExamen(null, TipoExamen.TEORICO, null, null));

        Examen examen = new Examen();
        SolicitudExamen solicitud = new SolicitudExamen();
        LocalDate mes = LocalDate.of(2026, 11, 15);
        LocalDate mesNorm = LocalDate.of(2026, 11, 1);
        Profesor profesor = new Profesor();
        FechaExamen fechas = new FechaExamen(mesNorm, LocalDateTime.of(2026, 11, 10, 9, 0), LocalDateTime.of(2026, 11, 20, 9, 0));

        when(this.examenRepository.findById(1L)).thenReturn(Optional.of(examen));
        when(this.examenRepository.findByAlumnoOrderByFechaHoraDesc(this.alumnoEjemplo)).thenReturn(List.of(examen));
        when(this.examenRepository.findByTipoOrderByFechaHoraDesc(TipoExamen.TEORICO)).thenReturn(List.of(examen));
        when(this.examenRepository.findByAlumnoProfesorAndTipoOrderByFechaHoraDesc(profesor, TipoExamen.PRACTICO)).thenReturn(List.of(examen));
        when(this.examenRepository.existsByAlumnoAndTipoAndEsAptoTrue(this.alumnoEjemplo, TipoExamen.TEORICO)).thenReturn(true);
        when(this.examenRepository.existsByAlumnoAndTipoAndEsAptoTrue(this.alumnoEjemplo, TipoExamen.PRACTICO)).thenReturn(false);
        when(this.examenRepository.countByAlumnoAndEsAptoTrue(this.alumnoEjemplo)).thenReturn(1L);
        when(this.examenRepository.countByAlumnoAndEsAptoFalse(this.alumnoEjemplo)).thenReturn(2L);
        when(this.examenRepository.save(examen)).thenReturn(examen);
        when(this.solicitudExamenRepository.findByMatriculaOrderByFechaHoraDesc(this.matriculaEjemplo)).thenReturn(List.of(solicitud));
        when(this.solicitudExamenRepository.findByAlumnoOrderByFechaHoraDesc(this.alumnoEjemplo)).thenReturn(List.of(solicitud));
        when(this.solicitudExamenRepository.findByAlumnoOrderByIdDesc(this.alumnoEjemplo)).thenReturn(List.of(solicitud));
        when(this.solicitudExamenRepository.existsByAlumnoAndEstado(this.alumnoEjemplo, EstadoSolicitud.PENDIENTE)).thenReturn(true);
        when(this.solicitudExamenRepository.existeSolicitudAceptadaConExamenPendiente(eq(this.alumnoEjemplo), eq(EstadoSolicitud.ACEPTADA), any())).thenReturn(true);
        when(this.fechaExamenRepository.findByMes(mesNorm)).thenReturn(Optional.of(fechas));
        when(this.cupoExamenRepository.findByMes(mesNorm)).thenReturn(List.of(new CupoExamen(mesNorm, TipoCarnet.PERMISO_B, 5, 5)));
        when(this.solicitudExamenRepository.findById(2L)).thenReturn(Optional.of(solicitud));
        when(this.solicitudExamenRepository.buscarConFiltros("ana", 1L, TipoCarnet.PERMISO_B, TipoExamen.TEORICO, EstadoSolicitud.PENDIENTE))
                .thenReturn(List.of(solicitud));

        assertTrue(this.examenService.buscarExamenPorId(1L).isPresent());
        assertEquals(1, this.examenService.obtenerExamenesPorAlumno(this.alumnoEjemplo).size());
        assertEquals(1, this.examenService.obtenerExamenesTeoricos().size());
        assertEquals(1, this.examenService.obtenerExamenesPracticosPorProfesor(profesor).size());
        assertTrue(this.examenService.tieneTeoricoAprobado(this.alumnoEjemplo));
        assertFalse(this.examenService.tienePracticoAprobado(this.alumnoEjemplo));
        assertEquals(1L, this.examenService.contarAptosPorAlumno(this.alumnoEjemplo));
        assertEquals(2L, this.examenService.contarNoAptosPorAlumno(this.alumnoEjemplo));
        assertSame(examen, this.examenService.guardar(examen));
        assertEquals(1, this.examenService.obtenerSolicitudesPorMatriculaDesc(this.matriculaEjemplo).size());
        assertEquals(1, this.examenService.obtenerSolicitudesPorAlumnoDesc(this.alumnoEjemplo).size());
        assertEquals(1, this.examenService.obtenerSolicitudesRecientesPorAlumno(this.alumnoEjemplo).size());
        assertTrue(this.examenService.tieneSolicitudPendiente(this.alumnoEjemplo));
        assertTrue(this.examenService.tieneSolicitudAceptadaConExamenPendiente(this.alumnoEjemplo));
        assertTrue(this.examenService.obtenerFechasExamen(mes).isPresent());
        assertEquals(1, this.examenService.obtenerCuposExamen(mes).size());
        assertTrue(this.examenService.buscarSolicitudPorId(2L).isPresent());
        assertEquals(1, this.examenService.buscarSolicitudesConFiltros("ana", 1L, TipoCarnet.PERMISO_B, TipoExamen.TEORICO, EstadoSolicitud.PENDIENTE).size());
    }

    @Test
    @DisplayName("calificarExamenOficial valida dto nulo, procesa NO APTO en 2ª convocatoria y APTO práctico con cierre de matrícula")
    void testCalificarExamenOficialRamasNoAptoYCierrePractico()
    {
        assertThrows(ReglaNegocioException.class, () -> this.examenService.calificarExamenOficial(null));
        assertThrows(ReglaNegocioException.class, () -> this.examenService.calificarExamenOficial(new CalificarExamenDTO(null, true)));

        // Suspenso práctico en 2ª convocatoria (agota convocatorias)
        this.matriculaEjemplo.setConvocatoriasGastadas(null);
        Examen practicoNoApto = new Examen();
        practicoNoApto.setTipo(TipoExamen.PRACTICO);
        practicoNoApto.setAlumno(this.alumnoEjemplo);
        practicoNoApto.setFechaHora(FechaHoraUtil.ahora().minusHours(2));

        when(this.examenRepository.findById(60L)).thenReturn(Optional.of(practicoNoApto));
        when(this.examenRepository.save(any(Examen.class))).thenAnswer(i -> i.getArgument(0));
        when(this.matriculaRepository.findByAlumnoAndEstaActivaTrue(this.alumnoEjemplo)).thenReturn(Optional.of(this.matriculaEjemplo));

        this.examenService.calificarExamenOficial(new CalificarExamenDTO(60L, false));
        assertEquals(1, this.matriculaEjemplo.getConvocatorias());
        verify(this.emailService).enviarNotificacionCorreo(eq(this.alumnoEjemplo.getCorreo()), anyString(), anyString(), anyString(), eq(Rol.ALUMNO));

        // Segundo suspenso teórico agotando convocatorias
        this.matriculaEjemplo.setConvocatoriasGastadas(1);
        Examen teoricoNoApto2 = new Examen();
        teoricoNoApto2.setTipo(TipoExamen.TEORICO);
        teoricoNoApto2.setAlumno(this.alumnoEjemplo);
        teoricoNoApto2.setFechaHora(FechaHoraUtil.ahora().minusHours(2));
        when(this.examenRepository.findById(61L)).thenReturn(Optional.of(teoricoNoApto2));

        this.examenService.calificarExamenOficial(new CalificarExamenDTO(61L, false));
        assertEquals(0, this.matriculaEjemplo.getConvocatorias());

        // Práctico APTO: inactiva matrícula, desvincula profesor y cancela clases pendientes
        Profesor profesor = new Profesor();
        this.alumnoEjemplo.setProfesor(profesor);
        this.matriculaEjemplo.setEstaActiva(true);
        ClasePractica pendiente = new ClasePractica();
        pendiente.setEstadoClase(EstadoClase.PENDIENTE);

        Examen practicoApto = new Examen();
        practicoApto.setTipo(TipoExamen.PRACTICO);
        practicoApto.setAlumno(this.alumnoEjemplo);
        practicoApto.setFechaHora(FechaHoraUtil.ahora().minusHours(2));

        when(this.examenRepository.findById(62L)).thenReturn(Optional.of(practicoApto));
        when(this.clasePracticaRepository.findByAlumnoAndEstadoClase(this.alumnoEjemplo, EstadoClase.PENDIENTE))
                .thenReturn(List.of(pendiente));

        this.examenService.calificarExamenOficial(new CalificarExamenDTO(62L, true));
        assertFalse(this.matriculaEjemplo.getEstaActiva());
        assertNull(this.alumnoEjemplo.getProfesor());
        assertEquals(EstadoClase.CANCELADA, pendiente.getEstadoClase());
    }

    @Test
    @DisplayName("fijarFechasExamen y fijarCuposExamen validan parámetros, meses, fines de semana, festivos y duplicados")
    void testFijarFechasYCuposValidaciones()
    {
        LocalDate mes = LocalDate.of(2026, 10, 1);
        LocalDateTime fechaLaboral1 = LocalDateTime.of(2026, 10, 14, 9, 0);
        LocalDateTime fechaLaboral2 = LocalDateTime.of(2026, 10, 21, 9, 0);

        assertThrows(ReglaNegocioException.class, () -> this.examenService.fijarFechasExamen(null, fechaLaboral1, fechaLaboral2));
        assertThrows(ReglaNegocioException.class, () -> this.examenService.fijarFechasExamen(mes, LocalDateTime.of(2026, 11, 2, 9, 0), fechaLaboral2));
        assertThrows(ReglaNegocioException.class, () -> this.examenService.fijarFechasExamen(mes, fechaLaboral1, LocalDateTime.of(2026, 11, 2, 9, 0)));
        assertThrows(ReglaNegocioException.class, () -> this.examenService.fijarFechasExamen(mes, fechaLaboral2, fechaLaboral1));
        assertThrows(ReglaNegocioException.class, () -> this.examenService.fijarFechasExamen(mes, LocalDateTime.of(2026, 10, 10, 9, 0), fechaLaboral2));
        assertThrows(ReglaNegocioException.class, () -> this.examenService.fijarFechasExamen(mes, fechaLaboral1, LocalDateTime.of(2026, 10, 24, 9, 0)));
        // 12 octubre es festivo nacional
        assertThrows(ReglaNegocioException.class, () -> this.examenService.fijarFechasExamen(mes, LocalDateTime.of(2026, 10, 12, 9, 0), fechaLaboral2));
        assertThrows(ReglaNegocioException.class, () -> this.examenService.fijarFechasExamen(LocalDate.of(2026, 12, 1), LocalDateTime.of(2026, 12, 10, 9, 0), LocalDateTime.of(2026, 12, 25, 9, 0)));

        when(this.fechaExamenRepository.existsByMes(mes)).thenReturn(true);
        assertThrows(ReglaNegocioException.class, () -> this.examenService.fijarFechasExamen(mes, fechaLaboral1, fechaLaboral2));

        assertThrows(ReglaNegocioException.class, () -> this.examenService.fijarCuposExamen(null, List.of()));
        when(this.cupoExamenRepository.existsByMes(mes)).thenReturn(true);
        assertThrows(ReglaNegocioException.class, () -> this.examenService.fijarCuposExamen(mes, List.of(new CupoExamenDTO(TipoCarnet.PERMISO_B, 5, 5))));

        when(this.cupoExamenRepository.existsByMes(mes)).thenReturn(false);
        assertThrows(ReglaNegocioException.class, () -> this.examenService.fijarCuposExamen(mes, List.of()));

        when(this.cupoExamenRepository.save(any(CupoExamen.class))).thenAnswer(i -> i.getArgument(0));
        List<CupoExamen> guardados = this.examenService.fijarCuposExamen(mes, List.of(
                new CupoExamenDTO(null, 5, 5),
                new CupoExamenDTO(TipoCarnet.PERMISO_B, null, -3)));
        assertEquals(1, guardados.size());
        assertEquals(0, guardados.get(0).getCupoTeorico());
        assertEquals(0, guardados.get(0).getCupoPractico());
    }

    @Test
    @DisplayName("obtenerGestionarSolicitud y aceptar/rechazar cubren bloqueos de cupo, fechas vencidas, clases pendientes y validaciones")
    void testGestionYAceptarRechazarRamas()
    {
        LocalDate mesPasado = FechaHoraUtil.hoy().minusMonths(2).withDayOfMonth(1);
        LocalDate mesActual = FechaHoraUtil.hoy().withDayOfMonth(1);
        LocalDate mesSiguiente = mesActual.plusMonths(1);
        LocalDateTime pasado = FechaHoraUtil.ahora().minusDays(2);
        LocalDateTime futuro1 = FechaHoraUtil.ahora().plusDays(5);
        LocalDateTime futuro2 = FechaHoraUtil.ahora().plusDays(12);

        SolicitudExamen solSinMatricula = new SolicitudExamen();
        solSinMatricula.setTipo(TipoExamen.PRACTICO);
        solSinMatricula.setAlumno(this.alumnoEjemplo);
        solSinMatricula.setEstado(EstadoSolicitud.PENDIENTE);
        solSinMatricula.setFechaHora(pasado);

        when(this.solicitudExamenRepository.findById(70L)).thenReturn(Optional.of(solSinMatricula));
        when(this.fechaExamenRepository.findByMes(mesActual)).thenReturn(Optional.of(new FechaExamen(mesActual, pasado, pasado)));
        when(this.fechaExamenRepository.findByMes(mesSiguiente)).thenReturn(Optional.of(new FechaExamen(mesSiguiente, futuro1, futuro2)));

        GestionarSolicitudExamenDTO diagSinMat = this.examenService.obtenerGestionarSolicitud(70L, mesPasado);
        assertFalse(diagSinMat.puedeAceptar());

        // Sin fechas en mes actual -> avanza a mes siguiente automáticamente (mesSolicitado = null)
        when(this.fechaExamenRepository.findByMes(mesActual)).thenReturn(Optional.empty());
        when(this.fechaExamenRepository.findByMes(mesSiguiente)).thenReturn(Optional.empty());
        GestionarSolicitudExamenDTO diagSinFechas = this.examenService.obtenerGestionarSolicitud(70L);
        assertFalse(diagSinFechas.puedeAceptar());
        assertEquals("Sin Fijar", diagSinFechas.convocatoria1().motivoNoDisponible());

        // Solicitud práctica con clase pendiente posterior a fecha1 y cupo completo en fecha2
        this.matriculaEjemplo.setEstaActiva(true);
        solSinMatricula.setMatricula(this.matriculaEjemplo);
        ClasePractica clasePosterior = new ClasePractica();
        clasePosterior.setFechaHora(futuro1.plusDays(1));
        FechaExamen fechasFuturas = new FechaExamen(mesSiguiente, futuro1, futuro2);
        CupoExamen cupo = new CupoExamen(mesSiguiente, TipoCarnet.PERMISO_B, 5, 2);

        when(this.clasePracticaRepository.countByAlumnoAndEstadoClase(this.alumnoEjemplo, EstadoClase.RECIBIDA)).thenReturn(3);
        when(this.clasePracticaRepository.findByAlumnoAndEstadoClase(this.alumnoEjemplo, EstadoClase.PENDIENTE))
                .thenReturn(List.of(clasePosterior));
        when(this.fechaExamenRepository.findByMes(mesSiguiente)).thenReturn(Optional.of(fechasFuturas));
        when(this.cupoExamenRepository.findByMesAndTipoPermiso(mesSiguiente, TipoCarnet.PERMISO_B)).thenReturn(Optional.of(cupo));
        when(this.solicitudExamenRepository.countAlumnosCitados(futuro1, TipoCarnet.PERMISO_B, TipoExamen.PRACTICO)).thenReturn(0L);
        when(this.solicitudExamenRepository.countAlumnosCitados(futuro2, TipoCarnet.PERMISO_B, TipoExamen.PRACTICO)).thenReturn(2L);

        GestionarSolicitudExamenDTO diagBloqueos = this.examenService.obtenerGestionarSolicitud(70L, mesSiguiente);
        assertFalse(diagBloqueos.puedeAceptar());
        assertTrue(diagBloqueos.convocatoria1().motivoNoDisponible().contains("posterior"));
        assertTrue(diagBloqueos.convocatoria2().motivoNoDisponible().contains("Cupo completo"));

        // Validaciones de aceptarSolicitudExamen
        assertThrows(ReglaNegocioException.class, () -> this.examenService.aceptarSolicitudExamen(null, 1, mesSiguiente));
        assertThrows(ReglaNegocioException.class, () -> this.examenService.aceptarSolicitudExamen(70L, 3, mesSiguiente));
        assertThrows(ReglaNegocioException.class, () -> this.examenService.aceptarSolicitudExamen(70L, 1, mesSiguiente));

        solSinMatricula.setEstado(EstadoSolicitud.ACEPTADA);
        assertThrows(ReglaNegocioException.class, () -> this.examenService.aceptarSolicitudExamen(70L, 1, mesSiguiente));

        // Validaciones de rechazarSolicitudExamen
        assertThrows(ReglaNegocioException.class, () -> this.examenService.rechazarSolicitudExamen(null, "Motivo"));
        assertThrows(ReglaNegocioException.class, () -> this.examenService.rechazarSolicitudExamen(70L, "  "));
        assertThrows(ReglaNegocioException.class, () -> this.examenService.rechazarSolicitudExamen(70L, "Motivo válido"));

        // Aceptar práctico con éxito cancelando clase pendiente del mismo día del profesor
        SolicitudExamen solPracticaOk = new SolicitudExamen();
        solPracticaOk.setTipo(TipoExamen.PRACTICO);
        solPracticaOk.setAlumno(this.alumnoEjemplo);
        solPracticaOk.setMatricula(this.matriculaEjemplo);
        solPracticaOk.setEstado(EstadoSolicitud.PENDIENTE);
        Profesor profesor = new Profesor();
        solPracticaOk.setProfesor(profesor);

        Alumno otroAlumno = new Alumno();
        otroAlumno.setNombre("Luis");
        otroAlumno.setCorreo("luis@mands.es");
        ClasePractica claseMismoDia = new ClasePractica();
        claseMismoDia.setEstadoClase(EstadoClase.PENDIENTE);
        claseMismoDia.setAlumno(otroAlumno);

        when(this.solicitudExamenRepository.findById(71L)).thenReturn(Optional.of(solPracticaOk));
        when(this.clasePracticaRepository.findByAlumnoAndEstadoClase(this.alumnoEjemplo, EstadoClase.PENDIENTE))
                .thenReturn(List.of());
        when(this.solicitudExamenRepository.countAlumnosCitados(futuro1, TipoCarnet.PERMISO_B, TipoExamen.PRACTICO)).thenReturn(0L);
        when(this.examenRepository.save(any(Examen.class))).thenAnswer(i -> i.getArgument(0));
        when(this.solicitudExamenRepository.save(any(SolicitudExamen.class))).thenAnswer(i -> i.getArgument(0));
        when(this.clasePracticaRepository.findByProfesorAndFechaHoraBetweenOrderByFechaHoraAsc(eq(profesor), any(), any()))
                .thenReturn(List.of(claseMismoDia));

        SolicitudExamen aceptada = this.examenService.aceptarSolicitudExamen(71L, 1, mesSiguiente);
        assertEquals(EstadoSolicitud.ACEPTADA, aceptada.getEstado());
        assertEquals(EstadoClase.CANCELADA, claseMismoDia.getEstadoClase());
    }

    @Test
    @DisplayName("obtenerSolicitudesExamenDashboard devuelve las 5 primeras solicitudes pendientes en orden FIFO")
    void testObtenerSolicitudesExamenDashboard_SoloPendientesFIFO()
    {
        SolicitudExamen solPendiente = new SolicitudExamen();
        solPendiente.setEstado(EstadoSolicitud.PENDIENTE);
        solPendiente.setTipo(TipoExamen.PRACTICO);
        solPendiente.setAlumno(this.alumnoEjemplo);

        when(this.solicitudExamenRepository.findTop5ByEstadoOrderByFechaHoraAsc(EstadoSolicitud.PENDIENTE))
                .thenReturn(List.of(solPendiente));

        List<SolicitudExamen> resultado = this.examenService.obtenerSolicitudesExamenDashboard();

        assertEquals(1, resultado.size());
        assertEquals(EstadoSolicitud.PENDIENTE, resultado.get(0).getEstado());
        verify(this.solicitudExamenRepository).findTop5ByEstadoOrderByFechaHoraAsc(EstadoSolicitud.PENDIENTE);
    }
}
