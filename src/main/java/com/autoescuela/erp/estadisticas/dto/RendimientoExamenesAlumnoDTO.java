package com.autoescuela.erp.estadisticas.dto;

/**
 * DTO para la tarjeta de exámenes DGT y convocatorias del Alumno.
 */
public record RendimientoExamenesAlumnoDTO(
    long examenesAprobados,
    long examenesSuspensos,
    int convocatoriasGastadas,
    int convocatoriasRestantes
)
{
    public RendimientoExamenesAlumnoDTO()
    {
        this(0, 0, 0, 2);
    }
}
