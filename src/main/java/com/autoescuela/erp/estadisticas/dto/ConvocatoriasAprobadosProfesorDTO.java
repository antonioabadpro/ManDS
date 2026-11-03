package com.autoescuela.erp.estadisticas.dto;

/**
 * DTO para la tarjeta de aprobados por convocatoria del profesor (Opción C).
 * Contabiliza los alumnos aprobados en primera, segunda o posteriores convocatorias DGT.
 */
public record ConvocatoriasAprobadosProfesorDTO(
    long aprobadosPrimeraConvocatoria,
    long aprobadosSegundaConvocatoria,
    long aprobadosMasDeDosConvocatorias,
    long totalAprobados,
    double porcentajePrimeraConvocatoria,
    double porcentajeSegundaConvocatoria,
    double porcentajeMasDeDosConvocatorias
)
{
    public ConvocatoriasAprobadosProfesorDTO()
    {
        this(0, 0, 0, 0, 0.0, 0.0, 0.0);
    }
}
