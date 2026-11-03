package com.autoescuela.erp.estadisticas.dto;

/**
 * DTO para la tarjeta de desglose y distribución de clases prácticas del Alumno.
 */
public record DistribucionClasesAlumnoDTO(
    long clasesRecibidas,
    long clasesPendientes,
    long clasesCanceladas,
    long totalClases,
    double porcentajeRecibidas,
    double porcentajePendientes,
    double porcentajeCanceladas
)
{
    public DistribucionClasesAlumnoDTO()
    {
        this(0, 0, 0, 0, 0.0, 0.0, 0.0);
    }
}
