package com.autoescuela.erp.estadisticas.dto;

/**
 * DTO para la tarjeta de balance económico formativo del Alumno.
 */
public record BalanceEconomicoAlumnoDTO(
    double totalMatriculas,
    double gastoClases,
    double gastoTotal,
    double tarifaClase,
    int saldoClasesActual,
    long totalClasesContratadas,
    boolean esVehiculoPesado
)
{
    public BalanceEconomicoAlumnoDTO()
    {
        this(0.0, 0.0, 0.0, 30.0, 0, 0, false);
    }
}
