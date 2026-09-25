package com.autoescuela.erp.practicas.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para cumplimentar la ficha técnica de una clase práctica (CU-024).
 * Al registrarse, la clase pasa a RECIBIDA y se descuenta el saldo automáticamente.
 */
public record CompletarClaseDTO(
    @NotNull(message = "El identificador de la clase es obligatorio.")
    Long claseId,

    @NotNull(message = "El kilometraje inicial es obligatorio.")
    @Min(value = 0, message = "El kilometraje inicial no puede ser negativo.")
    Integer kmInicio,

    @NotNull(message = "El kilometraje final es obligatorio.")
    @Min(value = 0, message = "El kilometraje final no puede ser negativo.")
    Integer kmFin,

    @Size(max = 1000, message = "Las observaciones no pueden superar los 1000 caracteres.")
    String observaciones
)
{
    public CompletarClaseDTO()
    {
        this(null, null, null, null);
    }

    /**
     * Método auxiliar para validar que el kilometraje final es mayor que el inicial.
     *
     * @return true si kmFin es mayor que kmInicio, false en caso contrario.
     */
    public boolean isKmFinMayorQueKmInicio() {
        return this.kmFin != null && this.kmInicio != null && this.kmFin > this.kmInicio;
    }
}
