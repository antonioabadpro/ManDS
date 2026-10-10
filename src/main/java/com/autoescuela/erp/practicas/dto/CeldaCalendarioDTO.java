package com.autoescuela.erp.practicas.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * DTO que representa una celda individual en la cuadrícula de calendario.
 */
public record CeldaCalendarioDTO(
    LocalDate fecha,
    LocalTime horaInicio,
    LocalTime horaFin,
    LocalDateTime fechaHora,
    boolean ocupada,
    boolean esExamenDgt,
    String estado,
    Long claseId,
    String titulo,
    String subtitulo,
    boolean esPropia,
    boolean esClicable,
    String modalUrl
)
{
}
