package com.autoescuela.erp.auth.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para el formulario de restablecimiento de contraseña mediante token de verificación.
 */
public record RestablecerPasswordDTO(
        @NotBlank(message = "El token de verificación es obligatorio.")
        String token,

        @NotBlank(message = "La contraseña es obligatoria.")
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres.")
        String password,

        @NotBlank(message = "La confirmación de la contraseña es obligatoria.")
        @Size(min = 6, message = "La contraseña de confirmación debe tener al menos 6 caracteres.")
        String confirmPassword
)
{
    public RestablecerPasswordDTO()
    {
        this(null, null, null);
    }

    /**
     * Validación en el Backend de que la contraseña y la confirmación de contraseña coincidan.
     * @return true si coinciden, false en caso contrario.
     */
    @AssertTrue (message = "Las contraseñas no coinciden")
    public Boolean isPasswordCoincidente()
    {
        if (this.password == null || this.confirmPassword == null)
        {
            return true; // Delegamos el valor null a @NotBlank para que genere el mensaje de error correspondiente.
        }
        return this.password.equals(this.confirmPassword);
    }
}
