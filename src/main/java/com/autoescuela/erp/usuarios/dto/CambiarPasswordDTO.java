package com.autoescuela.erp.usuarios.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para el cambio seguro de contraseña de acceso.
 */
public record CambiarPasswordDTO(
    @NotBlank(message = "La contraseña actual es obligatoria.")
    String passwordActual,

    @NotBlank(message = "La nueva contraseña es obligatoria.")
    @Size(min = 6, message = "La nueva contraseña debe tener al menos 6 caracteres.")
    String nuevaPassword,

    @NotBlank(message = "Debes confirmar la nueva contraseña.")
    @Size (min = 6, message = "La confirmación de la nueva contraseña debe tener al menos 6 caracteres.")
    String confirmPassword
)
{
    public CambiarPasswordDTO()
    {
        this(null, null, null);
    }

    /**
     * Validación en Backend de que la nueva contraseña y la confirmación coincidan.
     * @return true si coinciden, false en caso contrario.
     */
    @AssertTrue(message = "Las contraseñas introducidas no coinciden.")
    public Boolean isPasswordCoincidente()
    {
        if (this.nuevaPassword == null || this.confirmPassword == null)
        {
            return true;
        }
        return this.nuevaPassword.equals(this.confirmPassword);
    }
}
