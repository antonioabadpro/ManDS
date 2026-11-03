package com.autoescuela.erp.usuarios.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

/**
 * DTO inmutable (record) para transportar la solicitud de reasignación de profesor a un alumno.
 *
 * @param alumnoId Identificador del alumno a reasignar.
 * @param opcion Estrategia de asignación: REASIGNAR o SIN_PROFESOR.
 * @param nuevoProfesorId Identificador del nuevo profesor en caso de reasignación.
 */
public record ReasignarAlumnoDTO(
    @NotNull(message = "El identificador del alumno es obligatorio.")
    Long alumnoId,
    String opcion,
    Long nuevoProfesorId
)
{
    /**
     * Valida que si se selecciona la opción de reasignar, se haya elegido un nuevo profesor de destino.
     */
    @AssertTrue(message = "Debes seleccionar un profesor activo disponible para reasignar al alumno.")
    public boolean isNuevoProfesorValido()
    {
        if ("REASIGNAR".equalsIgnoreCase(this.opcion))
        {
            return this.nuevoProfesorId != null;
        }
        return true;
    }
}
