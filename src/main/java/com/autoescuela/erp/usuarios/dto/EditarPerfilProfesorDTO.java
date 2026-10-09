package com.autoescuela.erp.usuarios.dto;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.autoescuela.erp.core.enums.EstadoVehiculo;
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
    EstadoVehiculo vehiculoEstado,
    List<TipoCarnet> permisos
)
{
    public EditarPerfilProfesorDTO()
    {
        this(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    public EditarPerfilProfesorDTO(
        Long id, String nombre, String apellidos, String telefono, String direccion, LocalDate fechaNacimiento,
        String dni, String nombreUsuario, String correo, TipoTurno turno, LocalDate fechaContratacion,
        String vehiculoMatricula, String vehiculoModelo, List<TipoCarnet> permisos
    )
    {
        this(id, nombre, apellidos, telefono, direccion, fechaNacimiento, dni, nombreUsuario, correo, turno, fechaContratacion, vehiculoMatricula, vehiculoModelo, null, permisos);
    }

    /**
     * Calcula la antigüedad descriptiva del profesor en la autoescuela en función de su fecha de contratación.
     *
     * @return Cadena con la antigüedad calculada (ej. "menos de 1 año", "1 año", "X años") o null si no consta fecha.
     */
    public String antiguedad()
    {
        if (this.fechaContratacion == null)
        {
            return null;
        }
        int anios = Period.between(this.fechaContratacion, LocalDate.now()).getYears();
        if (anios <= 0)
        {
            return "menos de 1 año";
        }
        if (anios == 1)
        {
            return "1 año";
        }
        return anios + " años";
    }

    public String getAntiguedad()
    {
        return this.antiguedad();
    }

    public String antiguedadTexto()
    {
        return this.antiguedad();
    }

    public String getAntiguedadTexto()
    {
        return this.antiguedad();
    }
}
