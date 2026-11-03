package com.autoescuela.erp.flota.dto;

import com.autoescuela.erp.core.enums.EstadoIncidencia;

import jakarta.validation.constraints.NotNull;

/**
 * DTO para la actualización del estado de una incidencia mecánica.
 */
public record GestionarIncidenciaDTO(
    @NotNull(message = "El identificador de la incidencia es obligatorio.")
    Long id,

    @NotNull(message = "El nuevo estado de la incidencia es obligatorio.")
    EstadoIncidencia estado
)
{
}

