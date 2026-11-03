package com.autoescuela.erp.estadisticas.dto;

/**
 * DTO para la tarjeta de tiempo al volante del Alumno (minutos y horas de conducción).
 */
public record TiempoVolanteDTO(
    long minutosConduccion,
    double horasConduccion
)
{
    public TiempoVolanteDTO()
    {
        this(0, 0.0);
    }
}
