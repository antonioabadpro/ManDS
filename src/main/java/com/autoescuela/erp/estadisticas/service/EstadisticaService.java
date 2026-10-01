package com.autoescuela.erp.estadisticas.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autoescuela.erp.core.enums.EstadoSolicitud;
import com.autoescuela.erp.core.enums.EstadoUsuario;
import com.autoescuela.erp.core.enums.EstadoVehiculo;
import com.autoescuela.erp.estadisticas.dto.EstadisticasAutoescuelaDTO;
import com.autoescuela.erp.examenes.repository.SolicitudExamenRepository;
import com.autoescuela.erp.flota.repository.VehiculoRepository;
import com.autoescuela.erp.usuarios.repository.AlumnoRepository;
import com.autoescuela.erp.usuarios.repository.ProfesorRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EstadisticaService
{
    private final AlumnoRepository alumnoRepository;
    private final ProfesorRepository profesorRepository;
    private final VehiculoRepository vehiculoRepository;
    private final SolicitudExamenRepository solicitudExamenRepository;

    /**
     * Recopila y calcula las métricas globales clave de la autoescuela para el Dashboard de Administración.
     *
     * @return DTO con los totales agregados en tiempo real.
     */
    @Transactional(readOnly = true)
    public EstadisticasAutoescuelaDTO obtenerEstadisticasDashboard()
    {
        long totalAlumnos = this.alumnoRepository.countByEstado(EstadoUsuario.ACTIVO);
        long totalProfesores = this.profesorRepository.countByEstado(EstadoUsuario.ACTIVO);
        long totalVehiculosActivos = this.vehiculoRepository.countByEstadoNot(EstadoVehiculo.INACTIVO);
        long totalVehiculosOperativos = this.vehiculoRepository.countByEstado(EstadoVehiculo.DISPONIBLE)
                + this.vehiculoRepository.countByEstado(EstadoVehiculo.OCUPADO);
        long totalVehiculosEnMantenimiento = this.vehiculoRepository.countByEstado(EstadoVehiculo.MANTENIMIENTO);
        long totalSolicitudesExamenPendientes = this.solicitudExamenRepository.countByEstado(EstadoSolicitud.PENDIENTE);

        return EstadisticasAutoescuelaDTO.builder()
                .totalAlumnos(totalAlumnos)
                .totalProfesores(totalProfesores)
                .totalVehiculosActivos(totalVehiculosActivos)
                .totalVehiculosOperativos(totalVehiculosOperativos)
                .totalVehiculosEnMantenimiento(totalVehiculosEnMantenimiento)
                .totalSolicitudesExamenPendientes(totalSolicitudesExamenPendientes)
                .build();
    }
}
