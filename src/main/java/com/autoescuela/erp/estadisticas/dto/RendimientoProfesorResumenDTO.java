package com.autoescuela.erp.estadisticas.dto;

import com.autoescuela.erp.core.enums.TipoTurno;

public record RendimientoProfesorResumenDTO(
    Long id,
    String nombreCompleto,
    TipoTurno turno,
    long clasesImpartidas,
    double horasConduccion,
    long alumnosAsignados,
    long examenesAptos,
    long examenesNoAptos,
    double tasaAprobados
)
{
}
