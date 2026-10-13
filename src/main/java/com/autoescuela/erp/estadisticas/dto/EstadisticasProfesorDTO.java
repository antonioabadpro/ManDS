package com.autoescuela.erp.estadisticas.dto;

import java.util.List;

import com.autoescuela.erp.core.enums.TipoCarnet;

/**
 * DTO contenedor para el panel de estadísticas y métricas del Profesor.
 */
public record EstadisticasProfesorDTO(
    RendimientoExamenesProfesorDTO examenes,
    DistribucionClasesProfesorDTO clases,
    TiempoVolanteProfesorDTO tiempoVolante,
    long distancia,
    List<TipoCarnet> permisosHabilitados,
    ConvocatoriasAprobadosProfesorDTO convocatoriasAprobados,
    ClasesPreviasAprobadoProfesorDTO clasesPreviasAprobado
)
{
    public EstadisticasProfesorDTO()
    {
        this(
            new RendimientoExamenesProfesorDTO(),
            new DistribucionClasesProfesorDTO(),
            new TiempoVolanteProfesorDTO(),
            0L,
            List.of(),
            new ConvocatoriasAprobadosProfesorDTO(),
            new ClasesPreviasAprobadoProfesorDTO()
        );
    }

    public EstadisticasProfesorDTO(
        RendimientoExamenesProfesorDTO examenes,
        DistribucionClasesProfesorDTO clases,
        TiempoVolanteProfesorDTO tiempoVolante,
        long distancia,
        List<TipoCarnet> permisosHabilitados
    )
    {
        this(
            examenes,
            clases,
            tiempoVolante,
            distancia,
            permisosHabilitados,
            new ConvocatoriasAprobadosProfesorDTO(),
            new ClasesPreviasAprobadoProfesorDTO()
        );
    }

    public double tasaAprobados()
    {
        return this.examenes != null ? this.examenes.tasaAprobados() : 0.0;
    }

    public long aptos()
    {
        return this.examenes != null ? this.examenes.aptos() : 0;
    }

    public long noAptos()
    {
        return this.examenes != null ? this.examenes.noAptos() : 0;
    }

    public long totalEvaluados()
    {
        return this.examenes != null ? this.examenes.totalEvaluados() : 0;
    }

    public long clasesRecibidas()
    {
        return this.clases != null ? this.clases.clasesRecibidas() : 0;
    }

    public long clasesPendientes()
    {
        return this.clases != null ? this.clases.clasesPendientes() : 0;
    }

    public long clasesCanceladas()
    {
        return this.clases != null ? this.clases.clasesCanceladas() : 0;
    }

    public long totalClases()
    {
        return this.clases != null ? this.clases.totalClases() : 0;
    }

    public double horasConduccion()
    {
        return this.tiempoVolante != null ? this.tiempoVolante.horasConduccion() : 0.0;
    }

    public long minutosConduccion()
    {
        return this.tiempoVolante != null ? this.tiempoVolante.minutosConduccion() : 0;
    }

    public long aprobadosPrimeraConvocatoria()
    {
        return this.convocatoriasAprobados != null ? this.convocatoriasAprobados.aprobadosPrimeraConvocatoria() : 0;
    }

    public long aprobadosSegundaConvocatoria()
    {
        return this.convocatoriasAprobados != null ? this.convocatoriasAprobados.aprobadosSegundaConvocatoria() : 0;
    }

    public long aprobadosMasDeDosConvocatorias()
    {
        return this.convocatoriasAprobados != null ? this.convocatoriasAprobados.aprobadosMasDeDosConvocatorias() : 0;
    }

    public double mediaClasesPrevias()
    {
        return this.clasesPreviasAprobado != null ? this.clasesPreviasAprobado.mediaClases() : 0.0;
    }

    public int minClasesPrevias()
    {
        return this.clasesPreviasAprobado != null ? this.clasesPreviasAprobado.minClases() : 0;
    }

    public int maxClasesPrevias()
    {
        return this.clasesPreviasAprobado != null ? this.clasesPreviasAprobado.maxClases() : 0;
    }

}
