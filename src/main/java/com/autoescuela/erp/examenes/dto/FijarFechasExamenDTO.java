package com.autoescuela.erp.examenes.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotNull;

/**
 * DTO para la fijación manual de las dos fechas y horas de examen DGT de un mes determinado.
 */
public record FijarFechasExamenDTO(
        @NotNull(message = "El mes de convocatoria es obligatorio.")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate mes,

        @NotNull(message = "La fecha y hora de la 1ª convocatoria es obligatoria.")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime fechaHora1,

        @NotNull(message = "La fecha y hora de la 2ª convocatoria es obligatoria.")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime fechaHora2
) {}
