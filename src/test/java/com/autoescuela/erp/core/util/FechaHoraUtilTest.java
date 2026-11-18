package com.autoescuela.erp.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.TimeZone;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias para {@link FechaHoraUtil}.
 * Verifican la fijación de la zona horaria oficial de España (Europe/Madrid)
 * y la consistencia de fecha, hora y mes actuales independientemente de la zona de la JVM.
 */
class FechaHoraUtilTest
{
    private static final ZoneId MADRID = ZoneId.of("Europe/Madrid");

    @Test
    @DisplayName("ZONA_SPAIN debe apuntar a la zona horaria oficial Europe/Madrid")
    void zonaSpain_debeSerEuropeMadrid()
    {
        assertEquals(MADRID, FechaHoraUtil.ZONA_SPAIN);
        assertEquals("Europe/Madrid", FechaHoraUtil.ZONA_SPAIN.getId());
    }

    @Test
    @DisplayName("hoy devuelve la fecha actual en Europe/Madrid")
    void hoy_debeDevolverFechaActualEnMadrid()
    {
        LocalDate antes = LocalDate.now(MADRID);
        LocalDate resultado = FechaHoraUtil.hoy();
        LocalDate despues = LocalDate.now(MADRID);

        assertNotNull(resultado);
        assertTrue(resultado.equals(antes) || resultado.equals(despues));
    }

    @Test
    @DisplayName("ahora devuelve la fecha y hora actuales dentro del intervalo en Europe/Madrid")
    void ahora_debeDevolverFechaHoraActualEnMadrid()
    {
        LocalDateTime antes = LocalDateTime.now(MADRID);
        LocalDateTime resultado = FechaHoraUtil.ahora();
        LocalDateTime despues = LocalDateTime.now(MADRID);

        assertNotNull(resultado);
        assertFalse(resultado.isBefore(antes));
        assertFalse(resultado.isAfter(despues));
    }

    @Test
    @DisplayName("mesActual devuelve el YearMonth actual en Europe/Madrid")
    void mesActual_debeDevolverMesActualEnMadrid()
    {
        YearMonth antes = YearMonth.now(MADRID);
        YearMonth resultado = FechaHoraUtil.mesActual();
        YearMonth despues = YearMonth.now(MADRID);

        assertNotNull(resultado);
        assertTrue(resultado.equals(antes) || resultado.equals(despues));
    }

    @Test
    @DisplayName("Los métodos temporales mantienen la zona de España aunque la JVM esté en UTC")
    void metodosTemporales_debenIgnorarZonaSistemaUtc()
    {
        TimeZone zonaOriginal = TimeZone.getDefault();
        try
        {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

            LocalDateTime antes = LocalDateTime.now(MADRID);
            LocalDate hoy = FechaHoraUtil.hoy();
            LocalDateTime ahora = FechaHoraUtil.ahora();
            YearMonth mesActual = FechaHoraUtil.mesActual();
            LocalDateTime despues = LocalDateTime.now(MADRID);

            assertFalse(ahora.isBefore(antes));
            assertFalse(ahora.isAfter(despues));
            assertTrue(hoy.equals(antes.toLocalDate()) || hoy.equals(despues.toLocalDate()));
            assertTrue(mesActual.equals(YearMonth.from(antes)) || mesActual.equals(YearMonth.from(despues)));
        }
        finally
        {
            TimeZone.setDefault(zonaOriginal);
        }
    }

    @Test
    @DisplayName("esMayorDeEdad valida correctamente el umbral de 18 años y nulos")
    void esMayorDeEdad_debeValidarUmbral()
    {
        LocalDate hoy = FechaHoraUtil.hoy();

        assertTrue(FechaHoraUtil.esMayorDeEdad(null));
        assertTrue(FechaHoraUtil.esMayorDeEdad(hoy.minusYears(18)));
        assertTrue(FechaHoraUtil.esMayorDeEdad(hoy.minusYears(25)));
        assertFalse(FechaHoraUtil.esMayorDeEdad(hoy.minusYears(18).plusDays(1)));
    }

    @Test
    @DisplayName("esEdadRazonable valida correctamente el umbral de 100 años y nulos")
    void esEdadRazonable_debeValidarUmbral()
    {
        LocalDate hoy = FechaHoraUtil.hoy();

        assertTrue(FechaHoraUtil.esEdadRazonable(null));
        assertTrue(FechaHoraUtil.esEdadRazonable(hoy.minusYears(100)));
        assertFalse(FechaHoraUtil.esEdadRazonable(hoy.minusYears(100).minusDays(1)));
    }

    @Test
    @DisplayName("esFinDeSemana y esDiaInhabil detectan correctamente fines de semana y festivos")
    void testEsFinDeSemanaYEsDiaInhabil()
    {
        assertFalse(FechaHoraUtil.esFinDeSemana(null));
        assertFalse(FechaHoraUtil.esDiaInhabil(null));

        LocalDate miercolesHabil = LocalDate.of(2026, 10, 14);
        assertFalse(FechaHoraUtil.esFinDeSemana(miercolesHabil));
        assertFalse(FechaHoraUtil.esDiaInhabil(miercolesHabil));

        LocalDate sabado = LocalDate.of(2026, 10, 17);
        LocalDate domingo = LocalDate.of(2026, 10, 18);
        assertTrue(FechaHoraUtil.esFinDeSemana(sabado));
        assertTrue(FechaHoraUtil.esFinDeSemana(domingo));
        assertTrue(FechaHoraUtil.esDiaInhabil(sabado));
        assertTrue(FechaHoraUtil.esDiaInhabil(domingo));

        LocalDate festivoNacional = LocalDate.of(2026, 10, 12);
        assertFalse(FechaHoraUtil.esFinDeSemana(festivoNacional));
        assertTrue(FechaHoraUtil.esDiaInhabil(festivoNacional));
    }

    @Test
    @DisplayName("obtenerSiguienteDiaLectivo avanza correctamente saltando fines de semana y festivos")
    void testObtenerSiguienteDiaLectivo()
    {
        // Desde sábado 17/10/2026 salta a lunes 19/10/2026
        LocalDate sabado = LocalDate.of(2026, 10, 17);
        assertEquals(LocalDate.of(2026, 10, 19), FechaHoraUtil.obtenerSiguienteDiaLectivo(sabado));

        // Desde domingo festivo 01/11/2026 (traslado al lunes 2) salta al martes 03/11/2026
        LocalDate domingoFestivo = LocalDate.of(2026, 11, 1);
        assertEquals(LocalDate.of(2026, 11, 3), FechaHoraUtil.obtenerSiguienteDiaLectivo(domingoFestivo));

        // Nulo devuelve nulo
        assertEquals(null, FechaHoraUtil.obtenerSiguienteDiaLectivo(null));
    }

    @Test
    @DisplayName("obtenerUltimoDiaLectivoAnterior retrocede correctamente saltando fines de semana y festivos")
    void testObtenerUltimoDiaLectivoAnterior()
    {
        // Desde lunes 19/10/2026 retrocede a viernes 16/10/2026
        LocalDate lunes = LocalDate.of(2026, 10, 19);
        assertEquals(LocalDate.of(2026, 10, 16), FechaHoraUtil.obtenerUltimoDiaLectivoAnterior(lunes));

        // Nulo devuelve nulo
        assertEquals(null, FechaHoraUtil.obtenerUltimoDiaLectivoAnterior(null));
    }

    @Test
    @DisplayName("obtenerDiaLectivoFuturo genera una fecha lectiva válida y hábil")
    void testObtenerDiaLectivoFuturo()
    {
        LocalDate diaFuturo = FechaHoraUtil.obtenerDiaLectivoFuturo(5);
        assertNotNull(diaFuturo);
        assertTrue(diaFuturo.isAfter(FechaHoraUtil.hoy()));
        assertFalse(FechaHoraUtil.esDiaInhabil(diaFuturo));
    }
}
