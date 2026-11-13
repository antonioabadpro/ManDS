package com.autoescuela.erp.examenes.dto;

import java.time.LocalDateTime;

import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.enums.TipoExamen;

/**
 * DTO inmutable (record) para el listado y gestión de actas de exámenes oficiales DGT
 * en las vistas de Administración, Profesor y cálculo de bloqueos en Calendario.
 */
public record ExamenResumenDTO(
    Long id,
    LocalDateTime fechaHora,
    Integer duracion,
    TipoExamen tipo,
    Boolean esApto,
    String alumnoNombreCompleto,
    String alumnoDni,
    TipoCarnet permisoCarnet
)
{
}
