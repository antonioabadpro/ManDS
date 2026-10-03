package com.autoescuela.erp.usuarios.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO inmutable para transportar la solicitud de baja lógica de un profesor (Regla 7.3).
 *
 * @param profesorId Identificador del profesor que causará baja lógica.
 * @param opcionAlumnos Estrategia para los alumnos asignados: REASIGNAR o SIN_PROFESOR.
 * @param nuevoProfesorId Identificador del nuevo profesor en caso de reasignación.
 */
public record BajaProfesorDTO(
    @NotNull(message = "El identificador del profesor es obligatorio.")
    Long profesorId,
    String opcionAlumnos,
    Long nuevoProfesorId
)
{
}
