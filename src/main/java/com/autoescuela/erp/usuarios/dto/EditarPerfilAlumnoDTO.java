package com.autoescuela.erp.usuarios.dto;

import java.time.LocalDate;
import java.time.Period;

import org.springframework.format.annotation.DateTimeFormat;

import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.enums.TipoTurno;

import jakarta.validation.constraints.AssertTrue;
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
    @Size(max = 100, message = "El correo no puede superar los 100 caracteres.")
    String correo,
    @NotNull(message = "La fecha de nacimiento es obligatoria.")
    @Past(message = "La fecha de nacimiento debe ser una fecha pasada.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate fechaNacimiento,

    // DNI / NIE editable con validación
    @Pattern(regexp = "^$|^[0-9]{8}[A-Za-z]$|^[XYZxyz][0-9]{7}[A-Za-z]$", message = "El formato del DNI/NIE no es válido (ej. 12345678Z o X1234567Z).")
    String dni,

    // Campos informativos de identidad
    String nombreUsuario,

    // Campos informativos del Profesor asignado
    String profesorNombre,
    String profesorTelefono,
    String profesorEmail,
    TipoTurno profesorTurno,

    // Campos informativos del Vehículo de prácticas
    String vehiculoMarca,
    String vehiculoModelo,
    String vehiculoMatricula,

    // Campos informativos de la Matrícula activa
    TipoCarnet permisoActual,
    LocalDate fechaMatriculacion,
    Integer saldoClases,
    Integer convocatoriasRestantes
)
{
    public EditarPerfilAlumnoDTO()
    {
        this(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    /**
     * Valida que el alumno sea mayor de edad (al menos 18 años cumplidos).
     */
    @AssertTrue(message = "El alumno debe ser mayor de edad (al menos 18 años).")
    public boolean isMayorDeEdad()
    {
        if (this.fechaNacimiento == null)
        {
            return true;
        }
        return !this.fechaNacimiento.plusYears(18).isAfter(LocalDate.now());
    }

    /**
     * Valida que la edad del alumno no exceda un límite razonable (no más de 100 años).
     */
    @AssertTrue(message = "La fecha de nacimiento no puede ser anterior a hace 100 años.")
    public boolean isEdadRazonable()
    {
        if (this.fechaNacimiento == null)
        {
            return true;
        }
        return !this.fechaNacimiento.isBefore(LocalDate.now().minusYears(100));
    }

    /**
     * Calcula la antigüedad descriptiva de la matrícula del alumno ("menos de 1 año" o "X años").
     */
    public String antiguedad()
    {
        if (this.fechaMatriculacion == null)
        {
            return null;
        }
        int anios = Period.between(this.fechaMatriculacion, LocalDate.now()).getYears();
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
}
