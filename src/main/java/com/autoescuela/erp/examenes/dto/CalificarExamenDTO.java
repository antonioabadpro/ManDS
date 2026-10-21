package com.autoescuela.erp.examenes.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO para el registro de la calificación oficial de un examen práctico.
 */
public record CalificarExamenDTO(
    @NotNull(message = "El identificador del examen es obligatorio.")
    Long examenId,

    @NotNull(message = "El resultado (Apto / No Apto) es obligatorio.")
    Boolean esApto
)
{
    public CalificarExamenDTO()
    {
        this(null, null);
    }
}

