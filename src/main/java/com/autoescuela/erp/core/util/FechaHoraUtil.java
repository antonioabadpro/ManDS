package com.autoescuela.erp.core.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;

/**
 * Utilidad transversal para centralizar la obtención de fechas y horas actuales
 * aplicando de forma explícita la zona horaria oficial de España ({@code Europe/Madrid}).
 * Evita desajustes horarios cuando la aplicación se ejecuta en entornos o contenedores configurados en UTC.
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
}
