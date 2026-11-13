package com.autoescuela.erp.practicas.dto;

import java.time.LocalDateTime;

import com.autoescuela.erp.core.enums.EstadoClase;

/**
 * DTO inmutable (record) optimizado para mostrar las últimas clases prácticas
 * en el panel del alumno sin cargar relaciones innecesarias.
 */
public record ClasePracticaResumenAlumnoDTO(
    Long id,
    LocalDateTime fechaHora,
    String puntoRecogida,
    EstadoClase estadoClase
)
{
}
