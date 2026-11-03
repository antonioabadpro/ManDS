package com.autoescuela.erp.estadisticas.dto;

public record DesgloseFinancieroAdminDTO(
    double totalFacturado,
    double ingresosTeoricoPractica,
    long countTeoricoPractica,
    double ingresosSoloPractica,
    long countSoloPractica,
    double ingresosClases,
    long countClases,
    double ingresosRenovaciones,
    long countRenovaciones
)
{
}
