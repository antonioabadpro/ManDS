package com.autoescuela.erp.estadisticas.dto;

import java.util.List;

public record EstadisticasAdminDTO(
    KpisAdminDTO kpis,
    DistribucionModalidadDTO modalidades,
    EfectividadExamenDTO examenes,
    List<DistribucionCarnetDTO> topCarnets,
    List<DistribucionCarnetDTO> todosCarnets,
    List<RendimientoProfesorResumenDTO> topProfesores,
    List<RendimientoProfesorResumenDTO> todosProfesores,
    DesgloseFinancieroAdminDTO financiero
)
{
    /**
     * Alias de retrocompatibilidad para plantillas o llamadas previas que consuman carnets().
     */
    public List<DistribucionCarnetDTO> carnets()
    {
        return this.topCarnets;
    }
}
