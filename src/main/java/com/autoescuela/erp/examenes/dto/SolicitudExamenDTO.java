package com.autoescuela.erp.examenes.dto;

import com.autoescuela.erp.core.enums.TipoExamen;

import jakarta.validation.constraints.NotNull;

/**
 * DTO para la tramitación de solicitud de examen DGT por parte del Alumno (CU-038).
 */
public record SolicitudExamenDTO(
    @NotNull(message = "Debe seleccionar el tipo de examen (Teórico o Práctico).")
    TipoExamen tipoExamen
)
{
    public SolicitudExamenDTO()
    {
        this(null);
    }
}
