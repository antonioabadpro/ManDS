package com.autoescuela.erp.estadisticas;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.autoescuela.erp.academico.model.Matricula;
import com.autoescuela.erp.academico.repository.MatriculaRepository;
import com.autoescuela.erp.core.enums.EstadoClase;
import com.autoescuela.erp.core.enums.EstadoSolicitud;
import com.autoescuela.erp.core.enums.EstadoUsuario;
import com.autoescuela.erp.core.enums.EstadoVehiculo;
import com.autoescuela.erp.core.enums.ModalidadMatricula;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.enums.TipoExamen;
import com.autoescuela.erp.core.enums.TipoMatricula;
import com.autoescuela.erp.core.enums.TipoTurno;
import com.autoescuela.erp.estadisticas.dto.EstadisticasAdminDTO;
import com.autoescuela.erp.estadisticas.dto.EstadisticasAutoescuelaDTO;
import com.autoescuela.erp.estadisticas.dto.EstadisticasProfesorDTO;
import com.autoescuela.erp.estadisticas.service.EstadisticaService;
import com.autoescuela.erp.examenes.model.Examen;
import com.autoescuela.erp.examenes.repository.ExamenRepository;
import com.autoescuela.erp.examenes.repository.SolicitudExamenRepository;
import com.autoescuela.erp.flota.repository.VehiculoRepository;
import com.autoescuela.erp.practicas.model.ClasePractica;
import com.autoescuela.erp.practicas.repository.ClasePracticaRepository;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.model.Profesor;
import com.autoescuela.erp.usuarios.repository.AlumnoRepository;
import com.autoescuela.erp.usuarios.repository.ProfesorRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.autoescuela.erp.estadisticas.dto.EstadisticasAlumnoDTO;

@ExtendWith(MockitoExtension.class)
class EstadisticaServiceTest
{
    @Mock
    private AlumnoRepository alumnoRepository;

    @Mock
    private ProfesorRepository profesorRepository;

    @Mock
    private VehiculoRepository vehiculoRepository;

    @Mock
    private SolicitudExamenRepository solicitudExamenRepository;

    @Mock
    private MatriculaRepository matriculaRepository;

    @Mock
    private ClasePracticaRepository clasePracticaRepository;

    @Mock
    private ExamenRepository examenRepository;

    @InjectMocks
    private EstadisticaService estadisticaService;

    @Test
    @DisplayName("obtenerEstadisticasDashboard calcula agregaciones correctamente a través de repositorios")
    void testObtenerEstadisticasDashboard()
    {
        when(this.alumnoRepository.countByEstado(EstadoUsuario.ACTIVO)).thenReturn(26L);
        when(this.profesorRepository.countByEstado(EstadoUsuario.ACTIVO)).thenReturn(5L);
        when(this.vehiculoRepository.countByEstadoNot(EstadoVehiculo.INACTIVO)).thenReturn(10L);
        when(this.vehiculoRepository.countByEstado(EstadoVehiculo.DISPONIBLE)).thenReturn(6L);
        when(this.vehiculoRepository.countByEstado(EstadoVehiculo.OCUPADO)).thenReturn(2L);
        when(this.vehiculoRepository.countByEstado(EstadoVehiculo.MANTENIMIENTO)).thenReturn(2L);
        when(this.solicitudExamenRepository.countByEstado(EstadoSolicitud.PENDIENTE)).thenReturn(3L);

        EstadisticasAutoescuelaDTO stats = this.estadisticaService.obtenerEstadisticasDashboardAdmin();

        assertNotNull(stats);
        assertEquals(26L, stats.totalAlumnos());
        assertEquals(5L, stats.totalProfesores());
        assertEquals(10L, stats.totalVehiculosActivos());
        assertEquals(8L, stats.totalVehiculosOperativos());
        assertEquals(2L, stats.totalVehiculosEnMantenimiento());
        assertEquals(3L, stats.totalSolicitudesExamenPendientes());

        verify(this.alumnoRepository).countByEstado(EstadoUsuario.ACTIVO);
        verify(this.profesorRepository).countByEstado(EstadoUsuario.ACTIVO);
        verify(this.vehiculoRepository).countByEstadoNot(EstadoVehiculo.INACTIVO);
        verify(this.vehiculoRepository).countByEstado(EstadoVehiculo.DISPONIBLE);
        verify(this.vehiculoRepository).countByEstado(EstadoVehiculo.OCUPADO);
        verify(this.vehiculoRepository).countByEstado(EstadoVehiculo.MANTENIMIENTO);
        verify(this.solicitudExamenRepository).countByEstado(EstadoSolicitud.PENDIENTE);
    }

    @Test
    @DisplayName("obtenerEstadisticasAdmin calcula KPIs, modalidades, carnets, profesores y finanzas correctamente")
    void testObtenerEstadisticasAdmin()
    {
        // 1. Mock de matrículas
        Matricula m1 = new Matricula();
        m1.setEstaActiva(true);
        m1.setPermisoCarnet(TipoCarnet.PERMISO_B);
        m1.setModalidad(ModalidadMatricula.TEORICO_PRACTICA);
        m1.setTipo(TipoMatricula.NUEVA);
        m1.setPrecio(250.0f);
        m1.setSaldoClases(5);

        Matricula m2 = new Matricula();
        m2.setEstaActiva(true);
        m2.setPermisoCarnet(TipoCarnet.PERMISO_A2);
        m2.setModalidad(ModalidadMatricula.PRACTICA);
        m2.setTipo(TipoMatricula.RENOVACION);
        m2.setPrecio(250.0f);
        m2.setSaldoClases(2);

        when(this.matriculaRepository.findAll()).thenReturn(List.of(m1, m2));

        // 2. Mock de exámenes
        Examen ex1 = new Examen();
        ex1.setTipo(TipoExamen.TEORICO);
        ex1.setEsApto(true);

        Examen ex2 = new Examen();
        ex2.setTipo(TipoExamen.PRACTICO);
        ex2.setEsApto(false);

        when(this.examenRepository.findAll()).thenReturn(List.of(ex1, ex2));

        // 3. Mock de clases prácticas
        ClasePractica cp1 = new ClasePractica();
        cp1.setEstadoClase(EstadoClase.RECIBIDA);
        cp1.setDuracion(45);
        cp1.setKmInicio(100);
        cp1.setKmFin(130);

        when(this.clasePracticaRepository.findAll()).thenReturn(List.of(cp1));

        // 4. Mock de profesores
        Profesor p1 = new Profesor();
        p1.setNombre("Laura");
        p1.setApellidos("Sánchez");
        p1.setEstado(EstadoUsuario.ACTIVO);
        p1.setTurno(TipoTurno.MATINAL);

        when(this.profesorRepository.findAll()).thenReturn(List.of(p1));
        when(this.clasePracticaRepository.findByProfesorAndEstadoClase(p1, EstadoClase.RECIBIDA)).thenReturn(List.of(cp1));
        when(this.examenRepository.findByAlumnoProfesorOrderByFechaHoraDesc(p1)).thenReturn(List.of(ex1));

        EstadisticasAdminDTO resultado = this.estadisticaService.obtenerEstadisticasAdmin();

        assertNotNull(resultado);
        assertNotNull(resultado.kpis());
        assertEquals(50.0, resultado.kpis().tasaAprobadosGlobal());
        assertEquals(0.8, resultado.kpis().horasPracticas());
        assertEquals(30, resultado.kpis().totalKmRecorridos());
        assertEquals(2, resultado.kpis().alumnosFormandose());
        assertEquals(7, resultado.kpis().bolsaClasesPendientes());

        assertNotNull(resultado.modalidades());
        assertEquals(1, resultado.modalidades().teoricoPractica());
        assertEquals(1, resultado.modalidades().soloPractica());
        assertEquals(1, resultado.modalidades().matriculasNuevas());
        assertEquals(1, resultado.modalidades().matriculasRenovacion());

        assertNotNull(resultado.examenes());
        assertEquals(1, resultado.examenes().teoricosAptos());
        assertEquals(1, resultado.examenes().teoricosTotal());
        assertEquals(100.0, resultado.examenes().porcentajeTeoricos());
        assertEquals(0, resultado.examenes().practicosAptos());
        assertEquals(1, resultado.examenes().practicosTotal());
        assertEquals(0.0, resultado.examenes().porcentajePracticos());

        assertNotNull(resultado.topCarnets());
        assertEquals(4, resultado.topCarnets().size());
        assertNotNull(resultado.todosCarnets());
        assertEquals(12, resultado.todosCarnets().size());
        assertEquals(resultado.topCarnets(), resultado.carnets());

        assertNotNull(resultado.topProfesores());
        assertEquals(1, resultado.topProfesores().size());
        assertEquals("Laura Sánchez", resultado.topProfesores().get(0).nombreCompleto());
        assertEquals(100.0, resultado.topProfesores().get(0).tasaAprobados());

        assertNotNull(resultado.financiero());
        assertEquals(250.0, resultado.financiero().ingresosTeoricoPractica());
        assertEquals(500.0, resultado.financiero().ingresosRenovaciones());
    }

    @Test
    @DisplayName("obtenerEstadisticasProfesor calcula correctamente exámenes, clases, horas, km y permisos")
    void testObtenerEstadisticasProfesor()
    {
        // 1. Profesor con permisos
        Profesor profesor = new Profesor();
        profesor.setNombre("Carlos");
        profesor.setApellidos("Gómez");
        profesor.setListaTiposCarnet(List.of(
                TipoCarnet.PERMISO_B,
                TipoCarnet.PERMISO_A2
        ));

        // 2. Mock de clases prácticas
        ClasePractica cp1 = new ClasePractica();
        cp1.setEstadoClase(EstadoClase.RECIBIDA);
        cp1.setDuracion(45);
        cp1.setKmInicio(100);
        cp1.setKmFin(140);

        ClasePractica cp2 = new ClasePractica();
        cp2.setEstadoClase(EstadoClase.RECIBIDA);
        cp2.setDuracion(45);
        cp2.setKmInicio(140);
        cp2.setKmFin(170);

        ClasePractica cp3 = new ClasePractica();
        cp3.setEstadoClase(EstadoClase.PENDIENTE);

        ClasePractica cp4 = new ClasePractica();
        cp4.setEstadoClase(EstadoClase.CANCELADA);

        when(this.clasePracticaRepository.findByProfesorOrderByFechaHoraAsc(profesor))
                .thenReturn(List.of(cp1, cp2, cp3, cp4));

        // 3. Mock de exámenes (2 aptos, 1 suspenso)
        Examen ex1 = new Examen();
        ex1.setEsApto(true);

        Examen ex2 = new Examen();
        ex2.setEsApto(true);

        Examen ex3 = new Examen();
        ex3.setEsApto(false);

        when(this.examenRepository.findByAlumnoProfesorOrderByFechaHoraDesc(profesor))
                .thenReturn(List.of(ex1, ex2, ex3));

        // 4. Ejecución
        EstadisticasProfesorDTO stats = this.estadisticaService.obtenerEstadisticasProfesor(profesor);

        // 5. Verificaciones
        assertNotNull(stats);

        // Exámenes
        assertNotNull(stats.examenes());
        assertEquals(2, stats.examenes().aptos());
        assertEquals(1, stats.examenes().noAptos());
        assertEquals(3, stats.examenes().totalEvaluados());
        assertEquals(66.7, stats.examenes().tasaAprobados());
        assertEquals(33.3, stats.examenes().porcentajeNoAptos());

        // Clases
        assertNotNull(stats.clases());
        assertEquals(2, stats.clases().clasesRecibidas());
        assertEquals(1, stats.clases().clasesPendientes());
        assertEquals(1, stats.clases().clasesCanceladas());
        assertEquals(4, stats.clases().totalClases());
        assertEquals(50.0, stats.clases().porcentajeRecibidas());
        assertEquals(25.0, stats.clases().porcentajePendientes());
        assertEquals(25.0, stats.clases().porcentajeCanceladas());

        // Horas
        assertNotNull(stats.tiempoVolante());
        assertEquals(90, stats.tiempoVolante().minutosConduccion());
        assertEquals(1.5, stats.tiempoVolante().horasConduccion());

        // Km
        assertNotNull(stats.distancia());
        assertEquals(70, stats.distancia());

        // Permisos
        assertNotNull(stats.permisosHabilitados());
        assertEquals(2, stats.permisosHabilitados().size());
        assertEquals(TipoCarnet.PERMISO_B, stats.permisosHabilitados().get(0));

        // Métodos de compatibilidad
        assertEquals(66.7, stats.tasaAprobados());
        assertEquals(2, stats.aptos());
        assertEquals(1, stats.noAptos());
        assertEquals(2, stats.clasesRecibidas());
        assertEquals(4, stats.totalClases());
        assertEquals(1.5, stats.horasConduccion());
        assertEquals(70, stats.distancia());

        // Convocatorias y clases previas
        assertNotNull(stats.convocatoriasAprobados());
        assertNotNull(stats.clasesPreviasAprobado());
        assertEquals(2, stats.convocatoriasAprobados().totalAprobados());
        assertEquals(2, stats.aprobadosPrimeraConvocatoria());
        assertEquals(0, stats.aprobadosSegundaConvocatoria());
        assertEquals(0, stats.aprobadosMasDeDosConvocatorias());
        assertEquals(0.0, stats.mediaClasesPrevias());
        assertEquals(0, stats.minClasesPrevias());
        assertEquals(0, stats.maxClasesPrevias());
    }

    @Test
    @DisplayName("obtenerEstadisticasProfesor calcula con precisión convocatorias 1ª, 2ª, +3 y media, mín y máx de clases previas")
    void testObtenerEstadisticasProfesor_ConvocatoriasYClasesPreviasDetalladas()
    {
        Profesor profesor = new Profesor();
        profesor.setNombre("Marta");
        profesor.setApellidos("Díaz");

        when(this.clasePracticaRepository.findByProfesorOrderByFechaHoraAsc(profesor))
                .thenReturn(List.of());

        // Alumno 1: Aprobado en 1ª convocatoria con 10 clases
        Alumno a1 = new Alumno();
        a1.setNombre("Alumno1");
        a1.setDni("11111111A");
        Examen exA1 = new Examen();
        exA1.setAlumno(a1);
        exA1.setEsApto(true);
        exA1.setFechaHora(LocalDateTime.of(2026, 10, 1, 10, 0));

        when(this.examenRepository.findByAlumnoOrderByFechaHoraDesc(a1))
                .thenReturn(List.of(exA1));

        ClasePractica cpA1 = new ClasePractica();
        cpA1.setEstadoClase(EstadoClase.RECIBIDA);
        cpA1.setFechaHora(LocalDateTime.of(2026, 9, 25, 10, 0));
        List<ClasePractica> clasesA1 = new ArrayList<>();
        for (int i = 0; i < 10; i++)
        {
            clasesA1.add(cpA1);
        }
        when(this.clasePracticaRepository.findByAlumnoAndEstadoClase(a1, EstadoClase.RECIBIDA))
                .thenReturn(clasesA1);

        // Alumno 2: Suspenso previo + Aprobado en 2ª convocatoria con 20 clases
        Alumno a2 = new Alumno();
        a2.setNombre("Alumno2");
        a2.setDni("22222222B");
        Examen exA2_1 = new Examen();
        exA2_1.setAlumno(a2);
        exA2_1.setEsApto(false);
        exA2_1.setFechaHora(LocalDateTime.of(2026, 9, 10, 10, 0));

        Examen exA2_2 = new Examen();
        exA2_2.setAlumno(a2);
        exA2_2.setEsApto(true);
        exA2_2.setFechaHora(LocalDateTime.of(2026, 10, 5, 10, 0));

        when(this.examenRepository.findByAlumnoOrderByFechaHoraDesc(a2))
                .thenReturn(List.of(exA2_2, exA2_1));

        ClasePractica cpA2 = new ClasePractica();
        cpA2.setEstadoClase(EstadoClase.RECIBIDA);
        cpA2.setFechaHora(LocalDateTime.of(2026, 9, 20, 10, 0));
        List<ClasePractica> clasesA2 = new ArrayList<>();
        for (int i = 0; i < 20; i++)
        {
            clasesA2.add(cpA2);
        }
        when(this.clasePracticaRepository.findByAlumnoAndEstadoClase(a2, EstadoClase.RECIBIDA))
                .thenReturn(clasesA2);

        // Alumno 3: 2 Suspensos previos + Aprobado en 3ª convocatoria con 30 clases
        Alumno a3 = new Alumno();
        a3.setNombre("Alumno3");
        a3.setDni("33333333C");
        Examen exA3_1 = new Examen();
        exA3_1.setAlumno(a3);
        exA3_1.setEsApto(false);
        exA3_1.setFechaHora(LocalDateTime.of(2026, 8, 1, 10, 0));

        Examen exA3_2 = new Examen();
        exA3_2.setAlumno(a3);
        exA3_2.setEsApto(false);
        exA3_2.setFechaHora(LocalDateTime.of(2026, 9, 1, 10, 0));

        Examen exA3_3 = new Examen();
        exA3_3.setAlumno(a3);
        exA3_3.setEsApto(true);
        exA3_3.setFechaHora(LocalDateTime.of(2026, 10, 8, 10, 0));

        when(this.examenRepository.findByAlumnoOrderByFechaHoraDesc(a3))
                .thenReturn(List.of(exA3_3, exA3_2, exA3_1));

        ClasePractica cpA3 = new ClasePractica();
        cpA3.setEstadoClase(EstadoClase.RECIBIDA);
        cpA3.setFechaHora(LocalDateTime.of(2026, 9, 30, 10, 0));
        List<ClasePractica> clasesA3 = new ArrayList<>();
        for (int i = 0; i < 30; i++)
        {
            clasesA3.add(cpA3);
        }
        when(this.clasePracticaRepository.findByAlumnoAndEstadoClase(a3, EstadoClase.RECIBIDA))
                .thenReturn(clasesA3);

        // Mock de exámenes de los alumnos del profesor
        when(this.examenRepository.findByAlumnoProfesorOrderByFechaHoraDesc(profesor))
                .thenReturn(List.of(exA1, exA2_2, exA3_3));

        EstadisticasProfesorDTO stats = this.estadisticaService.obtenerEstadisticasProfesor(profesor);

        assertNotNull(stats);
        // Convocatorias
        assertEquals(3, stats.convocatoriasAprobados().totalAprobados());
        assertEquals(1, stats.convocatoriasAprobados().aprobadosPrimeraConvocatoria());
        assertEquals(1, stats.convocatoriasAprobados().aprobadosSegundaConvocatoria());
        assertEquals(1, stats.convocatoriasAprobados().aprobadosMasDeDosConvocatorias());
        assertEquals(33.3, stats.convocatoriasAprobados().porcentajePrimeraConvocatoria());
        assertEquals(33.3, stats.convocatoriasAprobados().porcentajeSegundaConvocatoria());
        assertEquals(33.3, stats.convocatoriasAprobados().porcentajeMasDeDosConvocatorias());

        // Clases previas
        assertEquals(3, stats.clasesPreviasAprobado().totalAlumnosAprobados());
        assertEquals(10, stats.clasesPreviasAprobado().minClases());
        assertEquals(30, stats.clasesPreviasAprobado().maxClases());
        assertEquals(20.0, stats.clasesPreviasAprobado().mediaClases());
    }

    @Test
    @DisplayName("obtenerEstadisticasProfesor con profesor null devuelve DTO con valores por defecto")
    void testObtenerEstadisticasProfesor_ProfesorNull()
    {
        EstadisticasProfesorDTO stats = this.estadisticaService.obtenerEstadisticasProfesor(null);

        assertNotNull(stats);
        assertEquals(0, stats.totalClases());
        assertEquals(0.0, stats.tasaAprobados());
        assertEquals(0L, stats.distancia());
    }

    @Test
    @DisplayName("obtenerEstadisticasAlumno calcula la distribución de clases con 1 decimal exacto sin sumar 101%")
    void testObtenerEstadisticasAlumno_DistribucionClasesConDecimalesExactos()
    {
        Alumno alumno = new Alumno();
        alumno.setNombre("Pablo");
        alumno.setApellidos("Ruiz");

        when(this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno)).thenReturn(Optional.empty());
        when(this.clasePracticaRepository.findByAlumnoAndEstadoClase(alumno, EstadoClase.RECIBIDA)).thenReturn(List.of());
        when(this.examenRepository.countByAlumnoAndEsAptoTrue(alumno)).thenReturn(0L);
        when(this.examenRepository.countByAlumnoAndEsAptoFalse(alumno)).thenReturn(0L);
        when(this.matriculaRepository.findByAlumnoOrderByFechaMatriculacionDesc(alumno)).thenReturn(List.of());

        // 70 recibidas, 10 pendientes, 0 canceladas -> Total 80
        when(this.clasePracticaRepository.countByAlumnoAndEstadoClase(alumno, EstadoClase.RECIBIDA)).thenReturn(70);
        when(this.clasePracticaRepository.countByAlumnoAndEstadoClase(alumno, EstadoClase.PENDIENTE)).thenReturn(10);
        when(this.clasePracticaRepository.countByAlumnoAndEstadoClase(alumno, EstadoClase.CANCELADA)).thenReturn(0);
        when(this.clasePracticaRepository.countByAlumno(alumno)).thenReturn(80);

        EstadisticasAlumnoDTO stats = this.estadisticaService.obtenerEstadisticasAlumno(alumno);

        assertNotNull(stats);
        assertEquals(70L, stats.clasesRecibidas());
        assertEquals(10L, stats.clasesPendientes());
        assertEquals(0L, stats.clasesCanceladas());
        assertEquals(80L, stats.totalClases());

        // 70 / 80 = 87.5% (NO truncado a 88.0%)
        assertEquals(87.5, stats.porcentajeRecibidas());

        // 10 / 80 = 12.5% (NO truncado a 13.0%)
        assertEquals(12.5, stats.porcentajePendientes());

        // La suma debe ser exactamente 100.0% y no 101%
        assertEquals(100.0, stats.porcentajeRecibidas() + stats.porcentajePendientes());
    }
}

