package com.autoescuela.erp.usuarios.dto;

import java.time.LocalDate;
import java.util.ArrayList;
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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para la recogida de datos y validación del formulario de alta de nuevo profesor.
 * Cubre todos los datos necesarios declarados en la entidad Profesor y su superclase Persona.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AltaProfesorDTO
{
    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 50, message = "El nombre no puede superar los 50 caracteres.")
    private String nombre;

    @NotBlank(message = "Los apellidos son obligatorios.")
    @Size(max = 100, message = "Los apellidos no pueden superar los 100 caracteres.")
    private String apellidos;

    @NotBlank(message = "El DNI/NIE es obligatorio.")
    @Pattern(regexp = "^[0-9]{8}[A-Za-z]$|^[XYZxyz][0-9]{7}[A-Za-z]$", message = "Formato de DNI/NIE inválido (ej. 12345678Z o X1234567A).")
    private String dni;

    @NotBlank(message = "El teléfono móvil es obligatorio.")
    @Pattern(regexp = "^[6-9][0-9]{8}$", message = "El teléfono debe contener 9 dígitos y comenzar por 6, 7, 8 o 9.")
    private String telefono;

    @NotBlank(message = "El correo electrónico es obligatorio.")
    @Email(message = "El correo electrónico no tiene un formato válido.")
    @Size(max = 50, message = "El correo electrónico no puede superar los 50 caracteres.")
    private String correo;

    @NotNull(message = "La fecha de nacimiento es obligatoria.")
    @Past(message = "La fecha de nacimiento debe ser anterior a la fecha actual.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaNacimiento;

    @NotBlank(message = "La dirección postal es obligatoria.")
    @Size(max = 150, message = "La dirección no puede superar los 150 caracteres.")
    private String direccion;

    @NotNull(message = "La fecha de contratación es obligatoria.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaContratacion = LocalDate.now();

    @NotNull(message = "Debe seleccionar un turno de trabajo.")
    private TipoTurno turno = TipoTurno.MATINAL;

    /**
     * Identificador del vehículo asignado al profesor (relación 1 a 1 opcional en el alta).
     */
    private Long vehiculoId;

    /**
     * Lista de permisos de carnet autorizados que puede impartir el docente.
     */
    @NotEmpty(message = "Debe seleccionar al menos un permiso de conducción autorizado.")
    private List<TipoCarnet> permisos = new ArrayList<>();

    /**
     * Valida que el profesor sea mayor de edad (al menos 18 años).
     */
    @AssertTrue(message = "El profesor debe ser mayor de edad (al menos 18 años).")
    public boolean isMayorDeEdad()
    {
        if (this.fechaNacimiento == null)
        {
            return true; // Se valida con @NotNull
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

    /**
     * Valida que la fecha de contratación no sea previa a la mayoría de edad del profesor.
     */
    @AssertTrue(message = "La fecha de contratación no puede ser anterior a la fecha en que el profesor cumplió la mayoría de edad.")
    public boolean isFechaContratacionValida()
    {
        if (this.fechaNacimiento == null || this.fechaContratacion == null)
        {
            return true;
        }
        return !this.fechaContratacion.isBefore(this.fechaNacimiento.plusYears(18));
    }

    /**
     * Valida que la fecha de contratación esté acotada a +- 1 mes respecto a la fecha actual.
     */
    @AssertTrue(message = "La fecha de contratación debe estar comprendida entre 1 mes antes y 1 mes después de la fecha actual.")
    public boolean isFechaContratacionEnRango()
    {
        if (this.fechaContratacion == null)
        {
            return true;
        }
        LocalDate haceUnMes = LocalDate.now().minusMonths(1);
        LocalDate enUnMes = LocalDate.now().plusMonths(1);
        return !this.fechaContratacion.isBefore(haceUnMes) && !this.fechaContratacion.isAfter(enUnMes);
    }
}
