package com.autoescuela.erp.estadisticas.dto;

/**
 * DTO para la tarjeta de tasa de aprobados y balance de exámenes DGT del profesor.
 */
public record RendimientoExamenesProfesorDTO(
    long aptos,
    long noAptos,
    long totalEvaluados,
    double tasaAprobados,
    double porcentajeNoAptos
)
{
    public RendimientoExamenesProfesorDTO()
    {
        this(0, 0, 0, 0.0, 0.0);
    }
}
