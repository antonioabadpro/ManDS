package com.autoescuela.erp.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para solicitar el restablecimiento de contraseña mediante correo electrónico.
 */
public record RecuperarPasswordDTO(
        @NotBlank(message = "El correo electrónico es obligatorio.")
        @Email(message = "El formato del correo electrónico no es válido.")
        @Size(max = 50, message = "El correo electrónico no puede superar los 50 caracteres.")
        String correo
)
{
    public RecuperarPasswordDTO()
    {
        this(null);
    }
}
