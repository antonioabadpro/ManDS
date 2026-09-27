package com.autoescuela.erp.academico.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para la adquisición de saldo de clases prácticas o bonos por parte del Alumno (CU-032).
 */
public record ComprarClasePracticaDTO(
    @NotNull(message = "El tipo de compra es obligatorio.")
    String tipoProducto, // "INDIVIDUAL", "BONO_10", "BONO_15", "BONO_20"

    @NotNull(message = "La cantidad de clases es obligatoria.")
    @Min(value = 1, message = "Debe adquirir al menos 1 clase práctica.")
    Integer cantidadClases,

    Float precioTotal
)
{
    public ComprarClasePracticaDTO()
    {
        this("INDIVIDUAL", 1, 30.0f);
    }
}
