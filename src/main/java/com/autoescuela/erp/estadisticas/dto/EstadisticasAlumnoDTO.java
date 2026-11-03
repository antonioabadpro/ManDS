package com.autoescuela.erp.estadisticas.dto;

/**
 * DTO para la agregación de métricas y estadísticas pedagógicas del Alumno (CU-037).
 */
public record EstadisticasAlumnoDTO(
    long clasesRecibidas,
    long clasesPendientes,
    long clasesCanceladas,
    long totalClases,
    long minutosConduccion,
    double horasConduccion,
    long kilometrosRecorridos,
    int convocatoriasGastadas,
    int convocatoriasRestantes,
    long examenesAprobados,
    long examenesSuspensos,
    double gastoTotal,
    double totalMatriculas,
    double gastoClases,
    long totalClasesContratadas,
    int saldoClasesActual,
    double tarifaClase,
    boolean esVehiculoPesado
)
{
    public EstadisticasAlumnoDTO()
    {
        this(0, 0, 0, 0, 0, 0.0, 0, 0, 2, 0, 0, 0.0, 0.0, 0.0, 0, 0, 30.0, false);
    }

    /**
     * Constructor de compatibilidad con la firma original de 12 campos.
     */
    public EstadisticasAlumnoDTO(
        long clasesRecibidas,
        long clasesPendientes,
        long clasesCanceladas,
        long totalClases,
        long minutosConduccion,
        double horasConduccion,
        long kilometrosRecorridos,
        int convocatoriasGastadas,
        int convocatoriasRestantes,
        long examenesAprobados,
        long examenesSuspensos,
        double gastoTotal)
    {
        this(clasesRecibidas, clasesPendientes, clasesCanceladas, totalClases,
             minutosConduccion, horasConduccion, kilometrosRecorridos,
             convocatoriasGastadas, convocatoriasRestantes,
             examenesAprobados, examenesSuspensos, gastoTotal,
             0.0, 0.0, 0, 0, 30.0, false);
    }

    /**
     * Constructor modular que consolida los cálculos de cada tarjeta independiente.
     */
    public EstadisticasAlumnoDTO(
        TiempoVolanteDTO tiempoVolante,
        long kilometrosRecorridos,
        RendimientoExamenesAlumnoDTO examenes,
        DistribucionClasesAlumnoDTO clases,
        BalanceEconomicoAlumnoDTO balance)
    {
        this(
            clases.clasesRecibidas(),
            clases.clasesPendientes(),
            clases.clasesCanceladas(),
            clases.totalClases(),
            tiempoVolante.minutosConduccion(),
            tiempoVolante.horasConduccion(),
            kilometrosRecorridos,
            examenes.convocatoriasGastadas(),
            examenes.convocatoriasRestantes(),
            examenes.examenesAprobados(),
            examenes.examenesSuspensos(),
            balance.gastoTotal(),
            balance.totalMatriculas(),
            balance.gastoClases(),
            balance.totalClasesContratadas(),
            balance.saldoClasesActual(),
            balance.tarifaClase(),
            balance.esVehiculoPesado()
        );
    }

    public double porcentajeRecibidas()
    {
        return this.totalClases > 0 ? Math.round((this.clasesRecibidas * 100.0 / this.totalClases) * 10.0) / 10.0 : 0.0;
    }

    public double porcentajePendientes()
    {
        return this.totalClases > 0 ? Math.round((this.clasesPendientes * 100.0 / this.totalClases) * 10.0) / 10.0 : 0.0;
    }

    public double porcentajeCanceladas()
    {
        return this.totalClases > 0 ? Math.round((this.clasesCanceladas * 100.0 / this.totalClases) * 10.0) / 10.0 : 0.0;
    }
}
