package com.autoescuela.erp.practicas.dto;

import java.util.Map;

/**
 * DTO para la serialización de eventos en FullCalendar.
 */
public record EventoCalendarioDTO(
    String id,
    String title,
    String start,
    String end,
    boolean allDay,
    String backgroundColor,
    String borderColor,
    String textColor,
    Map<String, Object> extendedProps
)
{
}
