package com.autoescuela.erp.examenes.dto;

import com.autoescuela.erp.core.enums.TipoExamen;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para la tramitación de solicitud de examen DGT por parte del Alumno (CU-038).
 */
public record SolicitudExamenDTO(
    @NotNull(message = "Debe seleccionar el tipo de examen (Teórico o Práctico).")
    TipoExamen tipoExamen,

    @Size(max = 500, message = "El comentario o justificación no puede superar los 500 caracteres.")
    String comentarioJustificacion
)
{

    public SolicitudExamenDTO()
    {
        this(null, null);
    }
}
