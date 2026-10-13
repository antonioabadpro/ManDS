package com.autoescuela.erp.estadisticas.dto;

import java.util.List;

public record EstadisticasAdminDTO(
    KpisAdminDTO kpis,
    DistribucionModalidadDTO modalidades,
    EfectividadExamenDTO examenes,
    List<DistribucionCarnetDTO> carnets,
    List<RendimientoProfesorResumenDTO> topProfesores,
    List<RendimientoProfesorResumenDTO> todosProfesores,
    DesgloseFinancieroAdminDTO financiero
)
{
}
