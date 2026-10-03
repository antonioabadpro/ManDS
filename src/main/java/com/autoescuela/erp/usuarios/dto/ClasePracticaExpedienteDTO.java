package com.autoescuela.erp.usuarios.dto;

import java.time.LocalDateTime;

import com.autoescuela.erp.core.enums.EstadoClase;

/**
 * DTO inmutable (record) representativo de una clase práctica en el histórico del expediente del alumno.
 */
public record ClasePracticaExpedienteDTO(
    Long id,
    LocalDateTime fechaHora,
    Integer duracion,
    String puntoRecogida,
    Integer kmInicio,
    Integer kmFin,
    String kmFormateado,
    EstadoClase estadoClase,
    String profesorNombre,
    String observaciones
)
{
}
