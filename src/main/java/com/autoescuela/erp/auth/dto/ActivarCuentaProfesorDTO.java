package com.autoescuela.erp.auth.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para el formulario de activación de cuenta e introducción de credenciales por parte del nuevo Profesor.
 */
public record ActivarCuentaProfesorDTO(
        @NotBlank(message = "El token de activación es obligatorio.")
        String token,

        @NotBlank(message = "El nombre de usuario es obligatorio.")
        @Size(min = 3, max = 30, message = "El nombre de usuario debe tener entre 3 y 30 caracteres.")
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "El nombre de usuario solo puede contener letras, números, puntos y guiones.")
        String nombreUsuario,

        @NotBlank(message = "La contraseña es obligatoria.")
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres.")
        String password,

        @NotBlank(message = "La confirmación de la contraseña es obligatoria.")
        @Size(min = 6, message = "La contraseña de confirmación debe tener al menos 6 caracteres.")
        String confirmPassword
)
{
    public ActivarCuentaProfesorDTO()
    {
        this(null, null, null, null);
    }

    /**
     * Validación en Backend de que la contraseña y la confirmación coincidan.
     * @return true si coinciden, false en caso contrario.
     */
    @AssertTrue(message = "Las contraseñas introducidas no coinciden.")
    public Boolean isPasswordCoincidente()
    {
        if (this.password == null || this.confirmPassword == null)
        {
            return true;
        }
        return this.password.equals(this.confirmPassword);
    }
}

