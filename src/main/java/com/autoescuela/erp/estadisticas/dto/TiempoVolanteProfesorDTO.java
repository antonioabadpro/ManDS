package com.autoescuela.erp.estadisticas.dto;

/**
 * DTO para la tarjeta de horas al volante y tiempo acumulado en prácticas del profesor.
 */
public record TiempoVolanteProfesorDTO(
    long minutosConduccion,
    double horasConduccion
)
{
    public TiempoVolanteProfesorDTO()
    {
        this(0, 0.0);
    }
}
