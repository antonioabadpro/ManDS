package com.autoescuela.erp.estadisticas.dto;

import com.autoescuela.erp.core.enums.TipoCarnet;

public record DistribucionCarnetDTO(
    TipoCarnet tipoCarnet,
    String descripcion,
    long totalAlumnos,
    double porcentaje
)
{
}
