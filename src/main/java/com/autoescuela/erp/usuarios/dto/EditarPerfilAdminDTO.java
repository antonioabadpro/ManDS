package com.autoescuela.erp.usuarios.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para la consulta y actualización de los datos del perfil del Administrador.
 * Contiene tanto los campos editables como los de solo lectura para la vista.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EditarPerfilAdminDTO
{
    private Long id;

    // Campos estrictamente de solo lectura para la vista (no modificables por el Administrador)
    private String nombreUsuario;
    private String correo;
    private String rol;

    // Campos modificables por el Administrador
    @NotBlank(message = "El DNI/NIE es obligatorio")
    @Pattern(regexp = "^[0-9]{8}[A-Za-z]$|^[XYZxyz][0-9]{7}[A-Za-z]$", message = "El formato del DNI/NIE no es válido (ej. 12345678Z o X1234567Z)")
    private String dni;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre no puede superar los 50 caracteres")
    private String nombre;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100, message = "Los apellidos no pueden superar los 100 caracteres")
    private String apellidos;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^(\\+34|0034)?[6789]\\d{8}$", message = "El teléfono debe ser un número válido español (9 dígitos)")
    private String telefono;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 200, message = "La dirección no puede superar los 200 caracteres")
    private String direccion;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser una fecha pasada")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaNacimiento;

    /**
     * Valida que el administrador sea mayor de edad (al menos 18 años cumplidos).
     */
    @AssertTrue(message = "El administrador debe ser mayor de edad (al menos 18 años).")
    public boolean isMayorDeEdad()
    {
        if (this.fechaNacimiento == null)
        {
            return true; // Se valida con @NotNull
        }
        return !this.fechaNacimiento.plusYears(18).isAfter(LocalDate.now());
    }

    /**
     * Valida que la edad del administrador no exceda un límite razonable (no más de 100 años).
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

