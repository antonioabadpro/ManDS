package com.autoescuela.erp.flota.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.autoescuela.erp.core.util.FechaHoraUtil;

/**
 * Pruebas unitarias para {@link VehiculoValidacionUtil}.
 * Verifican las reglas temporales de matriculación y revisiones ITV, así como la normalización de matrículas.
 */
class VehiculoValidacionUtilTest
{
    @Test
    @DisplayName("esAnioMatriculacionValido valida el rango entre 1990 y el año actual")
    void esAnioMatriculacionValido_debeValidarRango()
    {
        int anioActual = FechaHoraUtil.hoy().getYear();

        assertTrue(VehiculoValidacionUtil.esAnioMatriculacionValido(null));
        assertTrue(VehiculoValidacionUtil.esAnioMatriculacionValido(1990));
        assertTrue(VehiculoValidacionUtil.esAnioMatriculacionValido(anioActual));
        assertFalse(VehiculoValidacionUtil.esAnioMatriculacionValido(1989));
        assertFalse(VehiculoValidacionUtil.esAnioMatriculacionValido(anioActual + 1));
    }

    @Test
    @DisplayName("sonRevisionesCoherentes exige que la próxima revisión sea posterior a la última")
    void sonRevisionesCoherentes_debeValidarOrdenTemporal()
    {
        LocalDate hoy = FechaHoraUtil.hoy();

        assertTrue(VehiculoValidacionUtil.sonRevisionesCoherentes(null, hoy));
        assertTrue(VehiculoValidacionUtil.sonRevisionesCoherentes(hoy, null));
        assertTrue(VehiculoValidacionUtil.sonRevisionesCoherentes(hoy.minusDays(1), hoy));
        assertFalse(VehiculoValidacionUtil.sonRevisionesCoherentes(hoy, hoy));
        assertFalse(VehiculoValidacionUtil.sonRevisionesCoherentes(hoy.plusDays(1), hoy));
    }

    @Test
    @DisplayName("esFechaProximaRevisionValida y esFechaUltimaRevisionValida validan sus respectivas ventanas")
    void fechasRevision_debenValidarVentanasPermitidas()
    {
        LocalDate hoy = FechaHoraUtil.hoy();

        assertTrue(VehiculoValidacionUtil.esFechaProximaRevisionValida(null));
        assertTrue(VehiculoValidacionUtil.esFechaProximaRevisionValida(hoy));
        assertTrue(VehiculoValidacionUtil.esFechaProximaRevisionValida(hoy.plusYears(10).minusDays(1)));
        assertFalse(VehiculoValidacionUtil.esFechaProximaRevisionValida(hoy.minusDays(1)));
        assertFalse(VehiculoValidacionUtil.esFechaProximaRevisionValida(hoy.plusYears(10)));

        assertTrue(VehiculoValidacionUtil.esFechaUltimaRevisionValida(null));
        assertTrue(VehiculoValidacionUtil.esFechaUltimaRevisionValida(hoy));
        assertTrue(VehiculoValidacionUtil.esFechaUltimaRevisionValida(hoy.minusYears(4).plusDays(1)));
        assertFalse(VehiculoValidacionUtil.esFechaUltimaRevisionValida(hoy.minusYears(4)));
        assertFalse(VehiculoValidacionUtil.esFechaUltimaRevisionValida(hoy.plusDays(1)));
    }

    @Test
    @DisplayName("formatearMatricula normaliza espacios, guiones y mayúsculas")
    void formatearMatricula_debeNormalizarFormatoOficial()
    {
        assertNull(VehiculoValidacionUtil.formatearMatricula(null));
        assertEquals("1234-BCD", VehiculoValidacionUtil.formatearMatricula(" 1234 bcd "));
        assertEquals("1234-BCD", VehiculoValidacionUtil.formatearMatricula("1234bcd"));
    }
}
