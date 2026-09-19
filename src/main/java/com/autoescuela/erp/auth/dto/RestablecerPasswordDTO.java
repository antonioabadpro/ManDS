package com.autoescuela.erp.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para el formulario de restablecimiento de contraseña mediante token de verificación.
 */
public record RestablecerPasswordDTO(
        @NotBlank(message = "El token de verificación es obligatorio.")
        String token,

        @NotBlank(message = "La contraseña es obligatoria.")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres.")
        String password,

        @NotBlank(message = "La confirmación de la contraseña es obligatoriacontraseña.")
        @Size(min = 8, message = "La contraseña de confirmación debe tener al menos 8 caracteres.")
        String confirmPassword
)
{
    public RestablecerPasswordDTO()
    {
        this(null, null, null);
    }
}

