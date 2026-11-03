package com.autoescuela.erp.practicas.dto;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para la reserva de una clase práctica por parte del Alumno (CU-034).
 */
public record ReservaClasePracticaDTO(
    @NotNull(message = "La fecha y hora de la clase es obligatoria.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    LocalDateTime fechaHora,

    @NotNull (message = "La duración de la clase es obligatoria.")
    @Min(value = 30, message = "La duración mínima es de 30 minutos.")
    Integer duracion,

    @NotBlank(message = "El punto de recogida es obligatorio.")
    @Size(max = 100, message = "El punto de recogida no puede superar los 100 caracteres.")
    String puntoRecogida,

    @Size(max = 500, message = "Las observaciones no pueden superar los 500 caracteres.")
    String observaciones
)
{
    public ReservaClasePracticaDTO()
    {
        this(null, 45, null, null);
    }
}
