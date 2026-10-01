package com.autoescuela.erp.estadisticas;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.autoescuela.erp.core.enums.EstadoSolicitud;
import com.autoescuela.erp.core.enums.EstadoUsuario;
import com.autoescuela.erp.core.enums.EstadoVehiculo;
import com.autoescuela.erp.estadisticas.dto.EstadisticasAutoescuelaDTO;
import com.autoescuela.erp.estadisticas.service.EstadisticaService;
import com.autoescuela.erp.examenes.repository.SolicitudExamenRepository;
import com.autoescuela.erp.flota.repository.VehiculoRepository;
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
}

