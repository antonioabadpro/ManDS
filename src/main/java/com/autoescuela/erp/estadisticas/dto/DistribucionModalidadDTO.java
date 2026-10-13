package com.autoescuela.erp.estadisticas.dto;

public record DistribucionModalidadDTO(
    long teoricoPractica,
    long soloPractica,
    double porcentajeTeoricoPractica,
    double porcentajeSoloPractica,
    long matriculasNuevas,
    long matriculasRenovacion,
    double porcentajeNuevas,
    double porcentajeRenovaciones,
    long totalMatriculasActivas
)
{
}
