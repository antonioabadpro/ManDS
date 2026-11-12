package com.autoescuela.erp.flota.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.autoescuela.erp.core.enums.EstadoVehiculo;
import com.autoescuela.erp.core.enums.TipoCambio;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.enums.TipoCombustible;
import com.autoescuela.erp.core.enums.TipoTurno;
import com.autoescuela.erp.core.util.FechaHoraUtil;

/**
 * Pruebas unitarias para los DTOs de flota y sus cálculos de vigencia de revisiones ITV
 * basados en la zona horaria oficial de España.
 */
class VehiculoDTOTest
{
    @Test
    @DisplayName("AltaVehiculoDTO y EditarVehiculoDTO inicializan año actual y próxima revisión a 1 año en España")
    void constructoresPorDefecto_debenInicializarFechasEnMadrid()
    {
        LocalDate hoy = FechaHoraUtil.hoy();

        AltaVehiculoDTO alta = new AltaVehiculoDTO();
        EditarVehiculoDTO editar = new EditarVehiculoDTO();

        assertEquals(hoy.getYear(), alta.anio());
        assertEquals(hoy.plusYears(1), alta.fechaProximaRevision());
        assertEquals(hoy.getYear(), editar.anio());
        assertEquals(hoy.plusYears(1), editar.fechaProximaRevision());
        assertTrue(alta.isFechaMatriculacionValida());
        assertTrue(editar.isFechaMatriculacionValida());
        assertTrue(alta.isFechaProximaRevisionValida());
        assertTrue(editar.isFechaProximaRevisionValida());
    }

    @Test
    @DisplayName("VehiculoDetalleDTO evalúa correctamente revisión vencida, próxima, al día y nula")
    void vehiculoDetalleDTO_debeEvaluarEstadosDeRevision()
    {
        LocalDate hoy = FechaHoraUtil.hoy();

        VehiculoDetalleDTO vencida = crearDetalleConRevision(hoy.minusDays(1));
        assertTrue(vencida.isRevisionVencida());
        assertFalse(vencida.isRevisionProxima());
        assertFalse(vencida.isRevisionAlDia());

        VehiculoDetalleDTO proxima = crearDetalleConRevision(hoy.plusDays(15));
        assertFalse(proxima.isRevisionVencida());
        assertTrue(proxima.isRevisionProxima());
        assertFalse(proxima.isRevisionAlDia());

        VehiculoDetalleDTO alDia = crearDetalleConRevision(hoy.plusDays(90));
        assertFalse(alDia.isRevisionVencida());
        assertFalse(alDia.isRevisionProxima());
        assertTrue(alDia.isRevisionAlDia());

        VehiculoDetalleDTO nula = crearDetalleConRevision(null);
        assertFalse(nula.isRevisionVencida());
        assertFalse(nula.isRevisionProxima());
        assertFalse(nula.isRevisionAlDia());
    }

    @Test
    @DisplayName("VehiculoResumenDTO evalúa correctamente revisión vencida, próxima, al día y nula")
    void vehiculoResumenDTO_debeEvaluarEstadosDeRevision()
    {
        LocalDate hoy = FechaHoraUtil.hoy();

        VehiculoResumenDTO vencida = crearResumenConRevision(hoy.minusDays(1));
        assertTrue(vencida.isRevisionVencida());
        assertFalse(vencida.isRevisionProxima());
        assertFalse(vencida.isRevisionAlDia());

        VehiculoResumenDTO proxima = crearResumenConRevision(hoy.plusDays(30));
        assertFalse(proxima.isRevisionVencida());
        assertTrue(proxima.isRevisionProxima());
        assertFalse(proxima.isRevisionAlDia());

        VehiculoResumenDTO alDia = crearResumenConRevision(hoy.plusDays(60));
        assertNotNull(alDia);
        assertFalse(alDia.isRevisionVencida());
        assertFalse(alDia.isRevisionProxima());
        assertTrue(alDia.isRevisionAlDia());
    }

    private VehiculoDetalleDTO crearDetalleConRevision(LocalDate fechaProximaRevision)
    {
        return new VehiculoDetalleDTO(
                1L, "1234-BCD", "1234", "Seat", "Ibiza", "Blanco", 12000L, 110, 2023,
                TipoCombustible.GASOLINA, "Gasolina", TipoCambio.MANUAL, "Manual",
                null, fechaProximaRevision, EstadoVehiculo.DISPONIBLE, TipoCarnet.PERMISO_B,
                "Permiso B", null, null, null, TipoTurno.MATINAL, 0, 0
        );
    }

    private VehiculoResumenDTO crearResumenConRevision(LocalDate fechaProximaRevision)
    {
        return new VehiculoResumenDTO(
                1L, "1234-BCD", "1234", "Seat", "Ibiza", "Blanco", 12000L,
                null, fechaProximaRevision, EstadoVehiculo.DISPONIBLE, TipoCarnet.PERMISO_B,
                "Permiso B", null, null, TipoTurno.MATINAL
        );
    }
}
