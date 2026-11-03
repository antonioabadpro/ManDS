package com.autoescuela.erp.estadisticas.dto;

public record EfectividadExamenDTO(
    long teoricosAptos,
    long teoricosNoAptos,
    long teoricosTotal,
    double porcentajeTeoricos,
    long practicosAptos,
    long practicosNoAptos,
    long practicosTotal,
    double porcentajePracticos
)
{
}
