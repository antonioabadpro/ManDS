package com.autoescuela.erp.practicas.dto;

import java.time.LocalTime;

/**
 * DTO que representa un tramo horario de 45 minutos dentro de la jornada del profesor.
 */
public record TramoHorarioDTO(
    LocalTime horaInicio,
    LocalTime horaFin,
    String etiqueta,
    String etiquetaCompleta
)
{
}
