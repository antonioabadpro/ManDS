package com.autoescuela.erp.usuarios.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

/**
 * DTO inmutable para transportar la solicitud de baja lógica de un profesor.
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
    /**
     * Valida que si se selecciona la opción de reasignar alumnos, se haya elegido un profesor de destino.
     */
    @AssertTrue(message = "Debes seleccionar un profesor activo disponible para reasignar a los alumnos.")
    public boolean isNuevoProfesorValido()
    {
        if ("REASIGNAR".equalsIgnoreCase(this.opcionAlumnos))
        {
            return this.nuevoProfesorId != null;
        }
        return true;
    }
}
