package com.autoescuela.erp.core.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Set;

import com.autoescuela.erp.practicas.util.FestivosCalendarioUtil;

/**
 * Utilidad transversal para centralizar la obtención de fechas y horas actuales
 * aplicando de forma explícita la zona horaria oficial de España ({@code Europe/Madrid}),
 * validaciones etarias y el cálculo estructurado de jornadas lectivas hábiles.
 */
public final class FechaHoraUtil
{
    public static final ZoneId ZONA_SPAIN = ZoneId.of("Europe/Madrid");

    private FechaHoraUtil()
    {
        // Clase de utilidad no instanciable
    }

    /**
     * Devuelve la fecha actual en la zona horaria de España ({@code Europe/Madrid}).
     *
     * @return Fecha actual en España.
     */
    public static LocalDate hoy()
    {
        return LocalDate.now(ZONA_SPAIN);
    }

    /**
     * Devuelve la fecha y hora actuales en la zona horaria de España ({@code Europe/Madrid}).
     *
     * @return Fecha y hora actuales en España.
     */
    public static LocalDateTime ahora()
    {
        return LocalDateTime.now(ZONA_SPAIN);
    }

    /**
     * Devuelve el año y mes actuales en la zona horaria de España ({@code Europe/Madrid}).
     *
     * @return Año y mes actuales en España.
     */
    public static YearMonth mesActual()
    {
        return YearMonth.now(ZONA_SPAIN);
    }

    /**
     * Valida que una persona sea mayor de edad (al menos 18 años cumplidos en la fecha actual de España).
     *
     * @param fechaNacimiento Fecha de nacimiento (si es {@code null}, devuelve {@code true} para delegar en {@code @NotNull}).
     * @return {@code true} si tiene al menos 18 años o es nula.
     */
    public static boolean esMayorDeEdad(LocalDate fechaNacimiento)
    {
        if (fechaNacimiento == null)
        {
            return true;
        }
        return !fechaNacimiento.plusYears(18).isAfter(hoy());
    }

    /**
     * Valida que una fecha de nacimiento no exceda un límite razonable de 100 años respecto a la fecha actual de España.
     *
     * @param fechaNacimiento Fecha de nacimiento (si es {@code null}, devuelve {@code true} para delegar en {@code @NotNull}).
     * @return {@code true} si tiene 100 años o menos, o es nula.
     */
    public static boolean esEdadRazonable(LocalDate fechaNacimiento)
    {
        if (fechaNacimiento == null)
        {
            return true;
        }
        return !fechaNacimiento.isBefore(hoy().minusYears(100));
    }

    /**
     * Comprueba si una fecha corresponde al fin de semana (sábado o domingo).
     *
     * @param fecha Fecha a evaluar
     * @return {@code true} si es sábado o domingo, {@code false} en caso contrario.
     */
    public static boolean esFinDeSemana(LocalDate fecha)
    {
        if (fecha == null)
        {
            return false;
        }
        DayOfWeek dia = fecha.getDayOfWeek();
        return dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY;
    }

    /**
     * Evalúa si una fecha corresponde a un día inhábil para la actividad de la autoescuela
     * (fin de semana, festivo nacional o periodo vacacional).
     *
     * @param fecha Fecha a evaluar
     * @return {@code true} si es fin de semana, festivo o vacaciones, {@code false} si es día lectivo hábil.
     */
    public static boolean esDiaInhabil(LocalDate fecha)
    {
        if (fecha == null)
        {
            return false;
        }
        return FechaHoraUtil.esFinDeSemana(fecha) || FestivosCalendarioUtil.esDiaNoLaborable(fecha);
    }

    /**
     * Evalúa si una fecha es inhábil o coincide con una jornada reservada para examen práctico oficial DGT.
     *
     * @param fecha Fecha a evaluar
     * @param diasExamenProfesor Conjunto de fechas ocupadas por exámenes DGT
     * @return {@code true} si es inhábil o tiene examen asignado, {@code false} en caso contrario.
     */
    public static boolean esDiaInhabilOConExamen(LocalDate fecha, Set<LocalDate> diasExamenProfesor)
    {
        if (FechaHoraUtil.esDiaInhabil(fecha))
        {
            return true;
        }
        return diasExamenProfesor != null && diasExamenProfesor.contains(fecha);
    }

    /**
     * Calcula el siguiente día lectivo hábil a partir de una fecha de inicio (excluyendo fines de semana,
     * festivos nacionales y vacaciones oficiales).
     *
     * @param inicio Fecha inicial de búsqueda
     * @return Siguiente día lectivo hábil
     */
    public static LocalDate obtenerSiguienteDiaLectivo(LocalDate inicio)
    {
        return FechaHoraUtil.obtenerSiguienteDiaLectivo(inicio, Collections.emptySet());
    }

    /**
     * Calcula el siguiente día lectivo hábil a partir de una fecha de inicio, aplicando bucle estructurado
     * con poda de fines de semana y descartando festivos nacionales, vacaciones oficiales y exámenes prácticos DGT.
     *
     * @param inicio Fecha inicial de búsqueda
     * @param diasExamenProfesor Conjunto opcional de fechas ocupadas por exámenes DGT
     * @return Siguiente día lectivo hábil disponible
     */
    public static LocalDate obtenerSiguienteDiaLectivo(LocalDate inicio, Set<LocalDate> diasExamenProfesor)
    {
        if (inicio == null)
        {
            return null;
        }
        LocalDate fecha = inicio;

        // Poda inicial si la fecha de partida cae en fin de semana
        if (fecha.getDayOfWeek() == DayOfWeek.SATURDAY)
        {
            fecha = fecha.plusDays(2);
        }
        else
        {
            if (fecha.getDayOfWeek() == DayOfWeek.SUNDAY)
            {
                fecha = fecha.plusDays(1);
            }
        }

        // Bucle estructurado: avanza de forma continua mientras la fecha sea inhábil o coincida con examen DGT
        while (FechaHoraUtil.esDiaInhabilOConExamen(fecha, diasExamenProfesor))
        {
            fecha = fecha.plusDays(1);

            // Poda durante el avance: si al sumar un día cae en sábado, salta directamente al lunes
            if (fecha.getDayOfWeek() == DayOfWeek.SATURDAY)
            {
                fecha = fecha.plusDays(2);
            }
        }

        return fecha;
    }

    /**
     * Calcula el último día lectivo hábil inmediatamente anterior a la fecha de referencia.
     *
     * @param inicio Fecha de referencia
     * @return Último día lectivo anterior hábil
     */
    public static LocalDate obtenerUltimoDiaLectivoAnterior(LocalDate inicio)
    {
        return FechaHoraUtil.obtenerUltimoDiaLectivoAnterior(inicio, Collections.emptySet());
    }

    /**
     * Calcula el último día lectivo hábil inmediatamente anterior a la fecha de referencia,
     * aplicando bucle estructurado con poda inversa de fin de semana y descartando festivos, vacaciones y exámenes.
     *
     * @param inicio Fecha de referencia
     * @param diasExamenProfesor Conjunto opcional de fechas ocupadas por exámenes DGT
     * @return Último día lectivo hábil anterior disponible
     */
    public static LocalDate obtenerUltimoDiaLectivoAnterior(LocalDate inicio, Set<LocalDate> diasExamenProfesor)
    {
        if (inicio == null)
        {
            return null;
        }
        LocalDate fecha = inicio.minusDays(1);

        // Poda inversa inicial si cae en fin de semana
        if (fecha.getDayOfWeek() == DayOfWeek.SUNDAY)
        {
            fecha = fecha.minusDays(2);
        }
        else
        {
            if (fecha.getDayOfWeek() == DayOfWeek.SATURDAY)
            {
                fecha = fecha.minusDays(1);
            }
        }

        // Bucle estructurado inverso: retrocede mientras la fecha sea inhábil o coincida con examen DGT
        while (FechaHoraUtil.esDiaInhabilOConExamen(fecha, diasExamenProfesor))
        {
            fecha = fecha.minusDays(1);

            // Poda durante el retroceso: si al restar un día cae en domingo, salta directamente a viernes
            if (fecha.getDayOfWeek() == DayOfWeek.SUNDAY)
            {
                fecha = fecha.minusDays(2);
            }
        }

        return fecha;
    }

    /**
     * Proyecta un número determinado de días lectivos hábiles hacia adelante a partir de la fecha actual de España.
     *
     * @param diasLectivosAdelante Número de jornadas lectivas a avanzar (mínimo 1)
     * @return Fecha del día lectivo proyectado
     */
    public static LocalDate obtenerDiaLectivoFuturo(int diasLectivosAdelante)
    {
        LocalDate fecha = hoy();
        int dias = Math.max(1, diasLectivosAdelante);
        for (int i = 0; i < dias; i++)
        {
            fecha = FechaHoraUtil.obtenerSiguienteDiaLectivo(fecha.plusDays(1));
        }
        return fecha;
    }
}
