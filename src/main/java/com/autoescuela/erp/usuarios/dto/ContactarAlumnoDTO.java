package com.autoescuela.erp.usuarios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para redactar y enviar una notificación directa por correo a un alumno tutelado.
 */
public record ContactarAlumnoDTO(
    @NotNull(message = "El identificador del alumno es obligatorio.")
    Long alumnoId,

    @NotBlank(message = "El asunto del mensaje es obligatorio.")
    @Size(max = 150, message = "El asunto no puede superar los 150 caracteres.")
    String asunto,

    @NotBlank(message = "El cuerpo del mensaje no puede estar vacío.")
    @Size(max = 2000, message = "El mensaje no puede superar los 2000 caracteres.")
    String mensaje
)
{
    public ContactarAlumnoDTO()
    {
        this(null, null, null);
    }
}
