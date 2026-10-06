package com.autoescuela.erp.flota.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.autoescuela.erp.core.enums.TipoCambio;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.enums.TipoCombustible;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO inmutable (record) para la recogida y validación declarativa
 * de los datos del formulario de alta de nuevo vehículo en flota.
 */
public record AltaVehiculoDTO(
    @NotBlank(message = "La matrícula es obligatoria.")
    @Pattern(regexp = "^[0-9]{4}[ -]?[A-Za-z]{3}$", message = "Formato de matrícula inválido (ej. 1234-LMN o 1234LMN).")
    @Size(max = 8, message = "La matrícula no puede superar los 8 caracteres.")
    String matricula,

    @NotBlank(message = "La marca es obligatoria.")
    @Size(max = 50, message = "La marca no puede superar los 50 caracteres.")
    String marca,

    @NotBlank(message = "El modelo es obligatorio.")
    @Size(max = 50, message = "El modelo no puede superar los 50 caracteres.")
    String modelo,

    @NotBlank(message = "El color es obligatorio.")
    @Size(max = 30, message = "El color no puede superar los 30 caracteres.")
    String color,

    @NotNull(message = "El kilometraje inicial es obligatorio.")
    @Min(value = 0, message = "El kilometraje no puede ser negativo.")
    Long km,

    @NotNull(message = "La potencia es obligatoria.")
    @Min(value = 0, message = "La potencia no puede ser negativa (0 CV para remolques).")
    @Max(value = 1000, message = "La potencia no puede superar los 1000 CV.")
    Integer cv,

    @NotNull(message = "El año de matriculación es obligatorio.")
    @Min(value = 1990, message = "El año de matriculación no puede ser anterior a 1990.")
    Integer anio,

    @NotNull(message = "Debe seleccionar un tipo de combustible.")
    TipoCombustible tipoCombustible,

    @NotNull(message = "Debe seleccionar el tipo de caja de cambios.")
    TipoCambio cajaCambios,

    @NotNull(message = "Debe seleccionar el tipo de permiso DGT requerido.")
    TipoCarnet tipoPermiso,

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate fechaUltimaRevision,

    @NotNull(message = "La fecha de próxima revisión o ITV es obligatoria.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate fechaProximaRevision
)
{
    /**
     * Constructor por defecto para la inicialización del formulario en Thymeleaf
     * con valores por defecto acordes a la operativa habitual.
     */
    public AltaVehiculoDTO()
    {
        this(null, null, null, null, 0L, 110, LocalDate.now().getYear(), TipoCombustible.GASOLINA, TipoCambio.MANUAL, TipoCarnet.PERMISO_B, null, LocalDate.now().plusYears(1));
    }

    /**
     * Valida que el año de matriculación esté comprendido entre 1990 y el año actual de forma automatizada.
     */
    @AssertTrue(message = "El año de matriculación debe estar comprendido entre 1990 y el año actual.")
    public boolean isFechaMatriculacionValida()
    {
        if (this.anio == null)
        {
            return true;
        }
        int anioActual = LocalDate.now().getYear();
        return this.anio >= 1990 && this.anio <= anioActual;
    }

    /**
     * Valida que la fecha de la próxima revisión sea estrictamente posterior a la fecha de la última revisión registrada.
     */
    @AssertTrue(message = "La fecha de la próxima revisión debe ser posterior a la fecha de la última revisión.")
    public boolean isRevisionesCoherentes()
    {
        if (this.fechaUltimaRevision == null || this.fechaProximaRevision == null)
        {
            return true;
        }
        return this.fechaProximaRevision.isAfter(this.fechaUltimaRevision);
    }

    /**
     * Valida que la fecha de la próxima revisión no sea excesivamente lejana en el tiempo (más de 10 años desde la fecha actual).
     * Esto evita errores de introducción de fechas y mantiene la coherencia del registro.
     * @return true si la fecha de próxima revisión es válida, false en caso contrario.
     */
    @AssertTrue(message = "La fecha de la próxima revisión no puede superar los 10 años en el futuro, ni puede ser anterior a la fecha actual.")
    public boolean isFechaProximaRevisionValida()
    {
        if (this.fechaProximaRevision == null)
        {
            return true;
        }
        LocalDate hoy = LocalDate.now();
        return this.fechaProximaRevision.isBefore(hoy.plusYears(10)) && !this.fechaProximaRevision.isBefore(hoy);
    }

    /**
     * Valida que la fecha de la última revisión no sea excesivamente lejana en el tiempo (menos de 4 años desde la fecha actual)
     * ni sea una fecha futura posterior al día de hoy.
     * @return true si la fecha de última revisión es válida, false en caso contrario.
     */
    @AssertTrue(message = "La fecha de la última revisión no puede ser anterior a hace 4 años ni posterior a hoy.")
    public boolean isFechaUltimaRevisionValida()
    {
        if (this.fechaUltimaRevision == null)
        {
            return true;
        }
        LocalDate hoy = LocalDate.now();
        return this.fechaUltimaRevision.isAfter(hoy.minusYears(4)) && !this.fechaUltimaRevision.isAfter(hoy);
    }

    /**
     * Formatea la matrícula ingresada eliminando espacios redundantes y aplicando el formato oficial '0000-XXX'.
     */
    public String formatearMatricula()
    {
        if (this.matricula == null)
        {
            return null;
        }
        String limpia = this.matricula.trim().toUpperCase().replace(" ", "").replace("-", "");
        if (limpia.length() == 7)
        {
            return limpia.substring(0, 4) + "-" + limpia.substring(4);
        }
        return this.matricula.trim().toUpperCase();
    }
}
