package com.autoescuela.erp.examenes.dto;

import com.autoescuela.erp.core.enums.EstadoSolicitud;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.enums.TipoExamen;

/**
 * DTO inmutable (record) para el resumen rápido de solicitudes de examen pendientes en el Dashboard de Administración.
 */
public record SolicitudExamenDashboardDTO(
    Long id,
    String alumnoNombreCompleto,
    String profesorNombreCompleto,
    TipoCarnet permisoCarnet,
    TipoExamen tipo,
    EstadoSolicitud estado
)
{
}
