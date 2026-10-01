package com.autoescuela.erp.estadisticas.dto;

import lombok.Builder;

/**
 * DTO inmutable (record) para transportar las métricas globales clave al Dashboard de Administración.
 */
@Builder
public record EstadisticasAutoescuelaDTO(
        long totalAlumnos,
        long totalProfesores,
        long totalVehiculosActivos,
        long totalVehiculosOperativos,
        long totalVehiculosEnMantenimiento,
        long totalSolicitudesExamenPendientes
)
{
}
