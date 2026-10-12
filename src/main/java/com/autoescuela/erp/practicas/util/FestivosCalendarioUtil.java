package com.autoescuela.erp.practicas.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;

/**
 * Utilidad de dominio para la determinación de festivos nacionales de España y
 * periodos vacacionales oficiales de la autoescuela.
 * Aplica el cálculo dinámico de Semana Santa (Meeus/Butcher Computus), festivos fijos,
 * la regla de traslado al lunes cuando un festivo coincide en domingo,
 * y los periodos cerrados por vacaciones (1 al 14 de agosto y 23 de diciembre al 7 de enero).
 */
public final class FestivosCalendarioUtil
{
    private FestivosCalendarioUtil()
    {
        // Constructor privado para clase de utilidades
    }

    /**
     * Determina si la fecha indicada corresponde a un festivo nacional de España,
     * incluyendo festivos fijos, Semana Santa (Jueves y Viernes Santo) y traslados al lunes.
     *
     * @param fecha Fecha a evaluar
     * @return true si es festivo nacional, false en caso contrario
     */
    public static boolean esFestivoNacional(LocalDate fecha)
    {
        return obtenerNombreFestivoNacional(fecha) != null;
    }

    /**
     * Retorna el nombre oficial de la festividad nacional si la fecha es festivo, o null si no lo es.
     *
     * @param fecha Fecha a evaluar
     * @return Nombre del festivo nacional o null
     */
    public static String obtenerNombreFestivoNacional(LocalDate fecha)
    {
        if (fecha == null)
        {
            return null;
        }

        // 1. Festivos fijos en su día natural
        String festivoFijo = obtenerFestivoFijo(MonthDay.from(fecha));
        if (festivoFijo != null)
        {
            return festivoFijo;
        }

        // 2. Festivos móviles de Semana Santa (Jueves Santo y Viernes Santo)
        LocalDate pascua = calcularDomingoPascua(fecha.getYear());
        LocalDate juevesSanto = pascua.minusDays(3);
        LocalDate viernesSanto = pascua.minusDays(2);

        if (fecha.equals(juevesSanto))
        {
            return "Jueves Santo";
        }
        if (fecha.equals(viernesSanto))
        {
            return "Viernes Santo";
        }

        // 3. Regla de traslado al lunes: si un festivo nacional fijo cayó en domingo, se pasa al lunes siguiente
        if (fecha.getDayOfWeek() == DayOfWeek.MONDAY)
        {
            LocalDate domingoAnterior = fecha.minusDays(1);
            String festivoDomingo = obtenerFestivoFijo(MonthDay.from(domingoAnterior));
            if (festivoDomingo != null)
            {
                return festivoDomingo + " (trasladado)";
            }
        }

        return null;
    }

    /**
     * Determina si la fecha se encuentra dentro de alguno de los periodos vacacionales oficiales de la autoescuela:
     * - Primera quincena de agosto (1 al 14 de agosto inclusive).
     * - Vacaciones de Navidad (23 de diciembre al 7 de enero inclusive).
     *
     * @param fecha Fecha a evaluar
     * @return true si la autoescuela está de vacaciones, false en caso contrario
     */
    public static boolean esPeriodoVacacionalAutoescuela(LocalDate fecha)
    {
        return esVacacionesAgosto(fecha) || esVacacionesNavidad(fecha);
    }

    /**
     * Retorna el motivo explicativo de las vacaciones si la fecha está en periodo vacacional, o null.
     *
     * @param fecha Fecha a evaluar
     * @return Descripción del periodo vacacional o null
     */
    public static String obtenerMotivoVacaciones(LocalDate fecha)
    {
        if (fecha == null)
        {
            return null;
        }

        if (esVacacionesAgosto(fecha))
        {
            return "Vacaciones de Verano (1 al 14 de agosto)";
        }
        if (esVacacionesNavidad(fecha))
        {
            return "Vacaciones de Navidad (23 de diciembre al 7 de enero)";
        }
        return null;
    }

    /**
     * Determina si una fecha es inhábil para reservas de clases prácticas (por ser festivo nacional o periodo vacacional).
     *
     * @param fecha Fecha a evaluar
     * @return true si es no laborable en la autoescuela, false si es laborable
     */
    public static boolean esDiaNoLaborable(LocalDate fecha)
    {
        return esFestivoNacional(fecha) || esPeriodoVacacionalAutoescuela(fecha);
    }

    /**
     * Retorna el motivo descriptivo completo por el que el día es no laborable.
     *
     * @param fecha Fecha a evaluar
     * @return Motivo explicativo de festivo o vacaciones, o null si es laborable
     */
    public static String obtenerMotivoNoLaborable(LocalDate fecha)
    {
        if (fecha == null)
        {
            return null;
        }

        String festivo = obtenerNombreFestivoNacional(fecha);
        String vacaciones = obtenerMotivoVacaciones(fecha);

        if (festivo != null && vacaciones != null)
        {
            return festivo + " (" + vacaciones + ")";
        }
        if (festivo != null)
        {
            return festivo;
        }
        if (vacaciones != null)
        {
            return vacaciones;
        }
        return null;
    }

    /**
     * Retorna la categoría principal de día no laborable: "FESTIVO", "VACACIONES" o null si es hábil.
     *
     * @param fecha Fecha a evaluar
     * @return "FESTIVO", "VACACIONES" o null
     */
    public static String obtenerTipoNoLaborable(LocalDate fecha)
    {
        if (fecha == null)
        {
            return null;
        }
        if (esFestivoNacional(fecha))
        {
            return "FESTIVO";
        }
        if (esPeriodoVacacionalAutoescuela(fecha))
        {
            return "VACACIONES";
        }
        return null;
    }

    /**
     * Comprueba si la fecha está entre el 1 y el 14 de agosto inclusive.
     */
    public static boolean esVacacionesAgosto(LocalDate fecha)
    {
        if (fecha == null)
        {
            return false;
        }
        return fecha.getMonth() == Month.AUGUST && fecha.getDayOfMonth() >= 1 && fecha.getDayOfMonth() <= 14;
    }

    /**
     * Comprueba si la fecha está entre el 23 de diciembre y el 7 de enero inclusive.
     */
    public static boolean esVacacionesNavidad(LocalDate fecha)
    {
        if (fecha == null)
        {
            return false;
        }
        return (fecha.getMonth() == Month.DECEMBER && fecha.getDayOfMonth() >= 23)
                || (fecha.getMonth() == Month.JANUARY && fecha.getDayOfMonth() <= 7);
    }

    /**
     * Calcula la fecha del Domingo de Pascua (Domingo de Resurrección) para cualquier año
     * utilizando el algoritmo canónico de Butcher / Meeus (Computus occidental).
     */
    public static LocalDate calcularDomingoPascua(int anio)
    {
        int a = anio % 19;
        int b = anio / 100;
        int c = anio % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int mes = (h + l - 7 * m + 114) / 31;
        int dia = ((h + l - 7 * m + 114) % 31) + 1;
        return LocalDate.of(anio, mes, dia);
    }

    /**
     * Identifica los festivos nacionales españoles de fecha fija en el año natural.
     */
    private static String obtenerFestivoFijo(MonthDay monthDay)
    {
        if (monthDay == null)
        {
            return null;
        }

        if (monthDay.equals(MonthDay.of(Month.JANUARY, 1)))
        {
            return "Año Nuevo";
        }
        if (monthDay.equals(MonthDay.of(Month.JANUARY, 6)))
        {
            return "Epifanía del Señor";
        }
        if (monthDay.equals(MonthDay.of(Month.MAY, 1)))
        {
            return "Fiesta del Trabajo";
        }
        if (monthDay.equals(MonthDay.of(Month.AUGUST, 15)))
        {
            return "Asunción de la Virgen";
        }
        if (monthDay.equals(MonthDay.of(Month.OCTOBER, 12)))
        {
            return "Fiesta Nacional de España";
        }
        if (monthDay.equals(MonthDay.of(Month.NOVEMBER, 1)))
        {
            return "Todos los Santos";
        }
        if (monthDay.equals(MonthDay.of(Month.DECEMBER, 6)))
        {
            return "Día de la Constitución Española";
        }
        if (monthDay.equals(MonthDay.of(Month.DECEMBER, 8)))
        {
            return "Inmaculada Concepción";
        }
        if (monthDay.equals(MonthDay.of(Month.DECEMBER, 25)))
        {
            return "Natividad del Señor";
        }

        return null;
    }
}
