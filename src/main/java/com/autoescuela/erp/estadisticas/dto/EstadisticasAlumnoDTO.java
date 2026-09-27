package com.autoescuela.erp.estadisticas.dto;

/**
 * DTO para la agregación de métricas y estadísticas pedagógicas del Alumno (CU-037).
 */
public record EstadisticasAlumnoDTO(
    long clasesRecibidas,
    long clasesPendientes,
    long clasesCanceladas,
    long totalClases,
    double horasConduccion,
    long kilometrosRecorridos,
    int convocatoriasGastadas,
    int convocatoriasRestantes,
    long examenesAprobados,
    long examenesSuspensos,
    double gastoTotal
)
{

    public EstadisticasAlumnoDTO()
    {
        this(0, 0, 0, 0, 0.0, 0, 0, 2, 0, 0, 0.0);
    }
}
