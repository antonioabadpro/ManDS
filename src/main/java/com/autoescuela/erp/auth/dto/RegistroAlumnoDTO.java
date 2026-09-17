package com.autoescuela.erp.auth.dto;

/**
 * DTO para el registro público de nuevos alumnos.
 */
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para el registro público de nuevos alumnos (CU-002).
 * Captura y valida los datos de los 3 pasos del asistente de registro.
 */
public record RegistroAlumnoDTO(
        @NotBlank(message = "El nombre de usuario es obligatorio")
        @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres")
        @Pattern(regexp = "^[a-zA-Z0-9_.-]+$", message = "El nombre de usuario solo puede contener letras, números, puntos y guiones")
        String nombreUsuario,

        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "El formato del correo electrónico no es válido")
        @Size(max = 50, message = "El correo electrónico no puede superar los 50 caracteres")
        String correo,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, max = 100, message = "La contraseña debe tener al menos 6 caracteres")
        String password,

        @NotBlank(message = "Debe confirmar la contraseña")
        String confirmPassword,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 50, message = "El nombre no puede superar los 50 caracteres")
        String nombre,

        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(max = 100, message = "Los apellidos no pueden superar los 100 caracteres")
        String apellidos,

        @NotBlank(message = "El DNI/NIE es obligatorio")
        @Pattern(regexp = "^[0-9]{8}[A-Za-z]$|^[XYZxyz][0-9]{7}[A-Za-z]$", message = "El formato del DNI/NIE no es válido (ej. 12345678Z o X1234567Z)")
        String dni,

        @NotNull(message = "La fecha de nacimiento es obligatoria")
        @Past(message = "La fecha de nacimiento debe ser una fecha pasada")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate fechaNacimiento,

        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(regexp = "^(\\+34|0034)?[6789]\\d{8}$", message = "El teléfono debe ser un número válido español (9 dígitos)")
        String telefono,

        @NotBlank(message = "La dirección de residencia es obligatoria")
        @Size(max = 200, message = "La dirección no puede superar los 200 caracteres")
        String direccion,

        @NotNull(message = "Debe aceptar los términos de uso y la política de privacidad")
        @AssertTrue(message = "Debe aceptar los términos de uso y la política de privacidad")
        Boolean terminos)
{
    public RegistroAlumnoDTO()
    {
        this(null, null, null, null, null, null, null, null, null, null, null);
    }
}
