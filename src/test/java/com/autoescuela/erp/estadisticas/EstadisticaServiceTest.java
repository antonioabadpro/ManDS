package com.autoescuela.erp.estadisticas;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.autoescuela.erp.academico.repository.MatriculaRepository;
import com.autoescuela.erp.core.enums.EstadoSolicitud;
import com.autoescuela.erp.core.enums.EstadoUsuario;
import com.autoescuela.erp.core.enums.EstadoVehiculo;
import com.autoescuela.erp.estadisticas.dto.EstadisticasAutoescuelaDTO;
import com.autoescuela.erp.estadisticas.service.EstadisticaService;
import com.autoescuela.erp.examenes.repository.ExamenRepository;
import com.autoescuela.erp.examenes.repository.SolicitudExamenRepository;
import com.autoescuela.erp.flota.repository.VehiculoRepository;
import com.autoescuela.erp.practicas.repository.ClasePracticaRepository;
import com.autoescuela.erp.usuarios.repository.AlumnoRepository;
import com.autoescuela.erp.usuarios.repository.ProfesorRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

        EstadisticasAutoescuelaDTO stats = this.estadisticaService.obtenerEstadisticasDashboard();

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
        com.autoescuela.erp.academico.model.Matricula m1 = new com.autoescuela.erp.academico.model.Matricula();
        m1.setEstaActiva(true);
        m1.setPermisoCarnet(com.autoescuela.erp.core.enums.TipoCarnet.PERMISO_B);
        m1.setModalidad(com.autoescuela.erp.core.enums.ModalidadMatricula.TEORICO_PRACTICA);
        m1.setTipo(com.autoescuela.erp.core.enums.TipoMatricula.NUEVA);
        m1.setPrecio(250.0f);
        m1.setSaldoClases(5);

        com.autoescuela.erp.academico.model.Matricula m2 = new com.autoescuela.erp.academico.model.Matricula();
        m2.setEstaActiva(true);
        m2.setPermisoCarnet(com.autoescuela.erp.core.enums.TipoCarnet.PERMISO_A2);
        m2.setModalidad(com.autoescuela.erp.core.enums.ModalidadMatricula.PRACTICA);
        m2.setTipo(com.autoescuela.erp.core.enums.TipoMatricula.RENOVACION);
        m2.setPrecio(250.0f);
        m2.setSaldoClases(2);

        when(this.matriculaRepository.findAll()).thenReturn(java.util.List.of(m1, m2));

        // 2. Mock de exámenes
        com.autoescuela.erp.examenes.model.Examen ex1 = new com.autoescuela.erp.examenes.model.Examen();
        ex1.setTipo(com.autoescuela.erp.core.enums.TipoExamen.TEORICO);
        ex1.setEsApto(true);

        com.autoescuela.erp.examenes.model.Examen ex2 = new com.autoescuela.erp.examenes.model.Examen();
        ex2.setTipo(com.autoescuela.erp.core.enums.TipoExamen.PRACTICO);
        ex2.setEsApto(false);

        when(this.examenRepository.findAll()).thenReturn(java.util.List.of(ex1, ex2));

        // 3. Mock de clases prácticas
        com.autoescuela.erp.practicas.model.ClasePractica cp1 = new com.autoescuela.erp.practicas.model.ClasePractica();
        cp1.setEstadoClase(com.autoescuela.erp.core.enums.EstadoClase.RECIBIDA);
        cp1.setDuracion(45);
        cp1.setKmInicio(100);
        cp1.setKmFin(130);

        when(this.clasePracticaRepository.findAll()).thenReturn(java.util.List.of(cp1));

        // 4. Mock de profesores
        com.autoescuela.erp.usuarios.model.Profesor p1 = new com.autoescuela.erp.usuarios.model.Profesor();
        p1.setNombre("Laura");
        p1.setApellidos("Sánchez");
        p1.setEstado(EstadoUsuario.ACTIVO);
        p1.setTurno(com.autoescuela.erp.core.enums.TipoTurno.MATINAL);

        when(this.profesorRepository.findAll()).thenReturn(java.util.List.of(p1));
        when(this.clasePracticaRepository.findByProfesorAndEstadoClase(p1, com.autoescuela.erp.core.enums.EstadoClase.RECIBIDA)).thenReturn(java.util.List.of(cp1));
        when(this.examenRepository.findByAlumnoProfesorOrderByFechaHoraDesc(p1)).thenReturn(java.util.List.of(ex1));

        com.autoescuela.erp.estadisticas.dto.EstadisticasAdminDTO resultado = this.estadisticaService.obtenerEstadisticasAdmin();

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

        assertNotNull(resultado.topProfesores());
        assertEquals(1, resultado.topProfesores().size());
        assertEquals("Laura Sánchez", resultado.topProfesores().get(0).nombreCompleto());
        assertEquals(100.0, resultado.topProfesores().get(0).tasaAprobados());

        assertNotNull(resultado.financiero());
        assertEquals(250.0, resultado.financiero().ingresosTeoricoPractica());
        assertEquals(500.0, resultado.financiero().ingresosRenovaciones());
    }
}

