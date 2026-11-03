package com.autoescuela.erp.practicas.dto;

import java.util.List;

/**
 * DTO que representa una fila horaria en la cuadrícula de calendario conteniendo las celdas de lunes a viernes.
 */
public record FilaCalendarioDTO(
    TramoHorarioDTO tramo,
    List<CeldaCalendarioDTO> celdas
)
{
}
