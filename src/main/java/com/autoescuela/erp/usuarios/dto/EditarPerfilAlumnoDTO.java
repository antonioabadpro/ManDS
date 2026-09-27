package com.autoescuela.erp.usuarios.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.autoescuela.erp.core.enums.TipoCarnet;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para la visualización y edición del perfil del Alumno.
 */
public record EditarPerfilAlumnoDTO(
    // Campos editables por el alumno
    Long id,
    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 50, message = "El nombre no puede superar los 50 caracteres.")
    String nombre,
    @NotBlank(message = "Los apellidos son obligatorios.")
    @Size(max = 100, message = "Los apellidos no pueden superar los 100 caracteres.")
    String apellidos,
    @NotBlank(message = "El teléfono es obligatorio.")
    @Pattern(regexp = "^[6-9][0-9]{8}$", message = "El teléfono debe contener 9 dígitos y comenzar por 6, 7, 8 o 9.")
    String telefono,
    @NotBlank(message = "La dirección es obligatoria.")
    @Size(max = 200, message = "La dirección no puede superar los 200 caracteres.")
    String direccion,
    @NotBlank(message = "El correo electrónico es obligatorio.")
    @Email(message = "El formato del correo electrónico no es válido.")
    @Size(max = 100, message = "El correo no puede superar los 100 caracteres.")
    String correo,
    @NotNull(message = "La fecha de nacimiento es obligatoria.")
    @Past(message = "La fecha de nacimiento debe ser una fecha pasada.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate fechaNacimiento,

    // Campos informativos de solo lectura
    String dni,
    String nombreUsuario,
    String profesorNombre,
    String profesorTelefono,
    String profesorEmail,
    String vehiculoModelo,
    String vehiculoMatricula,
    TipoCarnet permisoActual,
    LocalDate fechaMatriculacion,
    Integer saldoClases,
    Integer convocatoriasRestantes
)
{
    public EditarPerfilAlumnoDTO()
    {
        this(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }
}
