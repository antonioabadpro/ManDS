package com.autoescuela.erp.usuarios.dto;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.enums.TipoTurno;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO inmutable (record) para la recogida de datos y validación del formulario de edición de profesor.
 * Posee el mismo contenido y verificaciones estrictas que {@link AltaProfesorDTO}.
 */
public record EditarProfesorDTO(
    @NotNull(message = "El identificador del profesor es obligatorio.")
    Long id,

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 50, message = "El nombre no puede superar los 50 caracteres.")
    String nombre,

    @NotBlank(message = "Los apellidos son obligatorios.")
    @Size(max = 100, message = "Los apellidos no pueden superar los 100 caracteres.")
    String apellidos,

    @NotBlank(message = "El DNI/NIE es obligatorio.")
    @Pattern(regexp = "^[0-9]{8}[A-Za-z]$|^[XYZxyz][0-9]{7}[A-Za-z]$", message = "Formato de DNI/NIE inválido (ej. 12345678Z o X1234567A).")
    String dni,

    @NotBlank(message = "El teléfono móvil es obligatorio.")
    @Pattern(regexp = "^[6-9][0-9]{8}$", message = "El teléfono debe contener 9 dígitos y comenzar por 6, 7, 8 o 9.")
    String telefono,

    @NotBlank(message = "El correo electrónico es obligatorio.")
    @Email(message = "El correo electrónico no tiene un formato válido.")
    @Size(max = 50, message = "El correo electrónico no puede superar los 50 caracteres.")
    String correo,

    @NotNull(message = "La fecha de nacimiento es obligatoria.")
    @Past(message = "La fecha de nacimiento debe ser anterior a la fecha actual.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate fechaNacimiento,

    @NotBlank(message = "La dirección postal es obligatoria.")
    @Size(max = 150, message = "La dirección no puede superar los 150 caracteres.")
    String direccion,

    @NotNull(message = "La fecha de contratación es obligatoria.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate fechaContratacion,

    @NotNull(message = "Debe seleccionar un turno de trabajo.")
    TipoTurno turno,

    /**
     * Identificador del vehículo asignado al profesor (relación 1 a 1 opcional).
     */
    Long vehiculoId,

    /**
     * Lista de permisos de carnet autorizados que puede impartir el profesor.
     */
    @NotEmpty(message = "Debe seleccionar al menos un permiso de conducción autorizado.")
    List<TipoCarnet> permisos
)
{
    /**
     * Constructor por defecto para la inicialización del formulario en el frontend.
     */
    public EditarProfesorDTO()
    {
        this(null, null, null, null, null, null, null, null, null, TipoTurno.MATINAL, null, List.of(TipoCarnet.PERMISO_B));
    }

    /**
     * Valida que el profesor sea mayor de edad (al menos 18 años).
     */
    @AssertTrue(message = "El profesor debe ser mayor de edad (al menos 18 años).")
    public boolean isMayorDeEdad()
    {
        if (this.fechaNacimiento == null)
        {
            return true;
        }
        return !this.fechaNacimiento.plusYears(18).isAfter(LocalDate.now());
    }

    /**
     * Valida que la edad del profesor no exceda un límite razonable (no más de 100 años).
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
}
