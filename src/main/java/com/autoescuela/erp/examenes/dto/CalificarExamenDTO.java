package com.autoescuela.erp.examenes.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para el registro de la calificación oficial de un examen práctico (CU-025).
 */
public record CalificarExamenDTO(
    @NotNull(message = "El identificador del examen es obligatorio.")
    Long examenId,

    @NotNull(message = "El resultado (Apto / No Apto) es obligatorio.")
    Boolean esApto,

    @Size(max = 500, message = "Las observaciones no pueden superar los 500 caracteres.")
    String observaciones
)
{
    public CalificarExamenDTO()
    {
        this(null, null, null);
    }
}

