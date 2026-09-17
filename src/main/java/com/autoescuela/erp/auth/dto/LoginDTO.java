package com.autoescuela.erp.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para la solicitud de inicio de sesión.
 * Permite la autenticación dual mediante nombre de usuario o correo electrónico (Regla de negocio 7.1).
 *
 * @param username Nombre de usuario o correo electrónico introducido por el usuario.
 * @param password Contraseña en texto plano antes de ser verificada por BCrypt.
 */
public record LoginDTO(
    @NotBlank(message = "El nombre de usuario o correo electrónico no puede estar en blanco")
    String username,

    @NotBlank(message = "La contraseña no puede estar en blanco")
    String password)
{
}
