package com.autoescuela.erp.usuarios.dto;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.enums.TipoTurno;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para la visualización y edición del perfil del Profesor.
 */
public record EditarPerfilProfesorDTO(
    // Campos editables por el profesor
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
    @NotNull(message = "La fecha de nacimiento es obligatoria.")
    @Past(message = "La fecha de nacimiento debe ser una fecha pasada.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate fechaNacimiento,

    // Campos de solo lectura informativos
    String dni,
    String nombreUsuario,
    String correo,
    TipoTurno turno,
    LocalDate fechaContratacion,
    String vehiculoMatricula,
    String vehiculoModelo,
    List<TipoCarnet> permisos
)
{
    public EditarPerfilProfesorDTO()
    {
        this(null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }
}
