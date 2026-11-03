package com.autoescuela.erp.estadisticas.dto;

/**
 * DTO para la tarjeta de clases prácticas y distribución de sesiones del profesor.
 */
public record DistribucionClasesProfesorDTO(
    long clasesRecibidas,
    long clasesPendientes,
    long clasesCanceladas,
    long totalClases,
    double porcentajeRecibidas,
    double porcentajePendientes,
    double porcentajeCanceladas
)
{
    public DistribucionClasesProfesorDTO()
    {
        this(0, 0, 0, 0, 0.0, 0.0, 0.0);
    }
}
