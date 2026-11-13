package com.autoescuela.erp.examenes.dto;

import java.time.LocalDateTime;

import com.autoescuela.erp.core.enums.EstadoSolicitud;
import com.autoescuela.erp.core.enums.TipoExamen;

/**
 * DTO inmutable (record) optimizado para mostrar las últimas solicitudes de examen
 * en el panel del alumno sin cargar relaciones perezosas.
 */
public record SolicitudExamenResumenAlumnoDTO(
    LocalDateTime fechaHora,
    TipoExamen tipo,
    EstadoSolicitud estado
)
{
}
