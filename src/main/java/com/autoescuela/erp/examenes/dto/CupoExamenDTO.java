package com.autoescuela.erp.examenes.dto;

import com.autoescuela.erp.core.enums.TipoCarnet;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO que encapsula el cupo teórico y práctico fijado para un tipo de carnet.
 */
public record CupoExamenDTO(
        @NotNull(message = "El tipo de permiso es obligatorio.")
        TipoCarnet tipoPermiso,

        @NotNull(message = "El cupo teórico es obligatorio.")
        @Min(value = 0, message = "El cupo teórico no puede ser negativo.")
        Integer cupoTeorico,

        @NotNull(message = "El cupo práctico es obligatorio.")
        @Min(value = 0, message = "El cupo práctico no puede ser negativo.")
        Integer cupoPractico
)
{

}
