package com.autoescuela.erp.estadisticas.dto;

public record KpisAdminDTO(
    double tasaAprobadosGlobal,
    long totalExamenesAptos,
    long totalExamenesEvaluados,
    double horasPracticas,
    long minutosPracticas,
    long totalKmRecorridos,
    double facturacionBruta,
    long totalMatriculasAbonadas,
    long alumnosFormandose,
    long bolsaClasesPendientes
)
{
}
