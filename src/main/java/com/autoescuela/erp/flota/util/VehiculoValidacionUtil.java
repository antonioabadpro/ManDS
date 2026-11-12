package com.autoescuela.erp.flota.util;

import java.time.LocalDate;

import com.autoescuela.erp.core.util.FechaHoraUtil;

/**
 * Utilidad transversal de validación y normalización para los formularios de vehículos de flota.
 * Centraliza las reglas de año de matriculación, coherencia temporal de revisiones ITV y formato de matrícula.
 */
public final class VehiculoValidacionUtil
{
    private VehiculoValidacionUtil()
    {
        // Clase de utilidad no instanciable
    }

    /**
     * Valida que el año de matriculación esté comprendido entre 1990 y el año actual.
     *
     * @param anio Año de matriculación (si es {@code null}, devuelve {@code true} para delegar en {@code @NotNull}).
     * @return {@code true} si el año es válido o nulo.
     */
    public static boolean esAnioMatriculacionValido(Integer anio)
    {
        if (anio == null)
        {
            return true;
        }
        int anioActual = FechaHoraUtil.hoy().getYear();
        return anio >= 1990 && anio <= anioActual;
    }

    /**
     * Valida que la fecha de la próxima revisión sea estrictamente posterior a la fecha de la última revisión.
     *
     * @param fechaUltimaRevision Fecha de última revisión.
     * @param fechaProximaRevision Fecha de próxima revisión.
     * @return {@code true} si son coherentes o alguna es nula.
     */
    public static boolean sonRevisionesCoherentes(LocalDate fechaUltimaRevision, LocalDate fechaProximaRevision)
    {
        if (fechaUltimaRevision == null || fechaProximaRevision == null)
        {
            return true;
        }
        return fechaProximaRevision.isAfter(fechaUltimaRevision);
    }

    /**
     * Valida que la fecha de la próxima revisión esté comprendida entre hoy (inclusive) y dentro de menos de 10 años.
     *
     * @param fechaProximaRevision Fecha de próxima revisión.
     * @return {@code true} si la fecha es válida o nula.
     */
    public static boolean esFechaProximaRevisionValida(LocalDate fechaProximaRevision)
    {
        if (fechaProximaRevision == null)
        {
            return true;
        }
        LocalDate hoy = FechaHoraUtil.hoy();
        return fechaProximaRevision.isBefore(hoy.plusYears(10)) && !fechaProximaRevision.isBefore(hoy);
    }

    /**
     * Valida que la fecha de última revisión no sea anterior a hace 4 años ni posterior a hoy.
     *
     * @param fechaUltimaRevision Fecha de última revisión.
     * @return {@code true} si la fecha es válida o nula.
     */
    public static boolean esFechaUltimaRevisionValida(LocalDate fechaUltimaRevision)
    {
        if (fechaUltimaRevision == null)
        {
            return true;
        }
        LocalDate hoy = FechaHoraUtil.hoy();
        return fechaUltimaRevision.isAfter(hoy.minusYears(4)) && !fechaUltimaRevision.isAfter(hoy);
    }

    /**
     * Formatea la matrícula eliminando espacios redundantes y aplicando el formato oficial {@code 0000-ZZZ}.
     *
     * @param matricula Matrícula de entrada.
     * @return Matrícula normalizada en mayúsculas o {@code null} si la entrada es nula.
     */
    public static String formatearMatricula(String matricula)
    {
        if (matricula == null)
        {
            return null;
        }
        String limpia = matricula.trim().toUpperCase().replace(" ", "").replace("-", "");
        if (limpia.length() == 7)
        {
            return limpia.substring(0, 4) + "-" + limpia.substring(4);
        }
        return matricula.trim().toUpperCase();
    }
}
