package com.autoescuela.erp.flota.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para reportar incidencias o averías mecánicas en el vehículo asignado (CU-027).
 */
public record ReportarIncidenciaDTO(
    @NotNull(message = "El vehículo es obligatorio.")
    Long vehiculoId,

    @NotBlank(message = "La descripción de la avería o incidencia es obligatoria.")
    @Size(min = 10, max = 1000, message = "La descripción debe tener entre 10 y 1000 caracteres.")
    String descripcion
)
{
    public ReportarIncidenciaDTO()
    {
        this(null, null);
    }
}

