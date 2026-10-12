package com.autoescuela.erp.practicas.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Tests Unitarios de FestivosCalendarioUtil")
class FestivosCalendarioUtilTest
{
    @Test
    @DisplayName("Identifica correctamente todos los festivos nacionales fijos")
    void testFestivosFijos()
    {
        int anio = 2026;
        assertThat(FestivosCalendarioUtil.esFestivoNacional(LocalDate.of(anio, 1, 1))).isTrue();
        assertThat(FestivosCalendarioUtil.obtenerNombreFestivoNacional(LocalDate.of(anio, 1, 1))).isEqualTo("Año Nuevo");

        assertThat(FestivosCalendarioUtil.esFestivoNacional(LocalDate.of(anio, 1, 6))).isTrue();
        assertThat(FestivosCalendarioUtil.obtenerNombreFestivoNacional(LocalDate.of(anio, 1, 6))).isEqualTo("Epifanía del Señor");

        assertThat(FestivosCalendarioUtil.esFestivoNacional(LocalDate.of(anio, 5, 1))).isTrue();
        assertThat(FestivosCalendarioUtil.obtenerNombreFestivoNacional(LocalDate.of(anio, 5, 1))).isEqualTo("Fiesta del Trabajo");

        assertThat(FestivosCalendarioUtil.esFestivoNacional(LocalDate.of(anio, 8, 15))).isTrue();
        assertThat(FestivosCalendarioUtil.obtenerNombreFestivoNacional(LocalDate.of(anio, 8, 15))).isEqualTo("Asunción de la Virgen");

        assertThat(FestivosCalendarioUtil.esFestivoNacional(LocalDate.of(anio, 10, 12))).isTrue();
        assertThat(FestivosCalendarioUtil.obtenerNombreFestivoNacional(LocalDate.of(anio, 10, 12))).isEqualTo("Fiesta Nacional de España");

        assertThat(FestivosCalendarioUtil.esFestivoNacional(LocalDate.of(anio, 11, 1))).isTrue();
        assertThat(FestivosCalendarioUtil.obtenerNombreFestivoNacional(LocalDate.of(anio, 11, 1))).isEqualTo("Todos los Santos");

        assertThat(FestivosCalendarioUtil.esFestivoNacional(LocalDate.of(anio, 12, 6))).isTrue();
        assertThat(FestivosCalendarioUtil.obtenerNombreFestivoNacional(LocalDate.of(anio, 12, 6))).isEqualTo("Día de la Constitución Española");

        assertThat(FestivosCalendarioUtil.esFestivoNacional(LocalDate.of(anio, 12, 8))).isTrue();
        assertThat(FestivosCalendarioUtil.obtenerNombreFestivoNacional(LocalDate.of(anio, 12, 8))).isEqualTo("Inmaculada Concepción");

        assertThat(FestivosCalendarioUtil.esFestivoNacional(LocalDate.of(anio, 12, 25))).isTrue();
        assertThat(FestivosCalendarioUtil.obtenerNombreFestivoNacional(LocalDate.of(anio, 12, 25))).isEqualTo("Natividad del Señor");
    }

    @Test
    @DisplayName("Calcula dinámicamente Semana Santa identificando Jueves Santo y Viernes Santo")
    void testSemanaSantaComputus()
    {
        // Año 2026: Pascua es el 5 de abril de 2026. Jueves Santo = 2 abr, Viernes Santo = 3 abr.
        LocalDate pascua2026 = FestivosCalendarioUtil.calcularDomingoPascua(2026);
        assertThat(pascua2026).isEqualTo(LocalDate.of(2026, 4, 5));

        LocalDate juevesSanto2026 = LocalDate.of(2026, 4, 2);
        LocalDate viernesSanto2026 = LocalDate.of(2026, 4, 3);
        assertThat(FestivosCalendarioUtil.esFestivoNacional(juevesSanto2026)).isTrue();
        assertThat(FestivosCalendarioUtil.obtenerNombreFestivoNacional(juevesSanto2026)).isEqualTo("Jueves Santo");

        assertThat(FestivosCalendarioUtil.esFestivoNacional(viernesSanto2026)).isTrue();
        assertThat(FestivosCalendarioUtil.obtenerNombreFestivoNacional(viernesSanto2026)).isEqualTo("Viernes Santo");

        // Año 2027: Pascua es el 28 de marzo de 2027. Jueves Santo = 25 mar, Viernes Santo = 26 mar.
        LocalDate pascua2027 = FestivosCalendarioUtil.calcularDomingoPascua(2027);
        assertThat(pascua2027).isEqualTo(LocalDate.of(2027, 3, 28));

        assertThat(FestivosCalendarioUtil.esFestivoNacional(LocalDate.of(2027, 3, 25))).isTrue();
        assertThat(FestivosCalendarioUtil.obtenerNombreFestivoNacional(LocalDate.of(2027, 3, 25))).isEqualTo("Jueves Santo");
        assertThat(FestivosCalendarioUtil.esFestivoNacional(LocalDate.of(2027, 3, 26))).isTrue();
        assertThat(FestivosCalendarioUtil.obtenerNombreFestivoNacional(LocalDate.of(2027, 3, 26))).isEqualTo("Viernes Santo");
    }

    @Test
    @DisplayName("Aplica regla de traslado al lunes cuando un festivo nacional coincide en domingo")
    void testTrasladoFestivoEnDomingoAlLunes()
    {
        // En 2026, el 6 de diciembre (Día de la Constitución) cae en domingo
        LocalDate seisDic2026 = LocalDate.of(2026, 12, 6);
        assertThat(seisDic2026.getDayOfWeek().name()).isEqualTo("SUNDAY");
        assertThat(FestivosCalendarioUtil.esFestivoNacional(seisDic2026)).isTrue();

        // El lunes 7 de diciembre de 2026 debe ser festivo trasladado
        LocalDate sieteDic2026 = LocalDate.of(2026, 12, 7);
        assertThat(sieteDic2026.getDayOfWeek().name()).isEqualTo("MONDAY");
        assertThat(FestivosCalendarioUtil.esFestivoNacional(sieteDic2026)).isTrue();
        assertThat(FestivosCalendarioUtil.obtenerNombreFestivoNacional(sieteDic2026))
                .isEqualTo("Día de la Constitución Española (trasladado)");
    }

    @Test
    @DisplayName("Valida los primeros 14 días de agosto como periodo vacacional de la autoescuela")
    void testVacacionesAgosto()
    {
        // 31 de julio: laborable
        assertThat(FestivosCalendarioUtil.esVacacionesAgosto(LocalDate.of(2026, 7, 31))).isFalse();
        assertThat(FestivosCalendarioUtil.esDiaNoLaborable(LocalDate.of(2026, 7, 31))).isFalse();

        // Del 1 al 14 de agosto: vacaciones
        for (int dia = 1; dia <= 14; dia++)
        {
            LocalDate fechaAgosto = LocalDate.of(2026, 8, dia);
            assertThat(FestivosCalendarioUtil.esVacacionesAgosto(fechaAgosto)).isTrue();
            assertThat(FestivosCalendarioUtil.esDiaNoLaborable(fechaAgosto)).isTrue();
            assertThat(FestivosCalendarioUtil.obtenerMotivoVacaciones(fechaAgosto))
                    .isEqualTo("Vacaciones de Verano (1 al 14 de agosto)");
        }

        // 15 de agosto: no son vacaciones de agosto, pero es festivo nacional (Asunción de la Virgen)
        LocalDate quinceAgosto = LocalDate.of(2026, 8, 15);
        assertThat(FestivosCalendarioUtil.esVacacionesAgosto(quinceAgosto)).isFalse();
        assertThat(FestivosCalendarioUtil.esFestivoNacional(quinceAgosto)).isTrue();
        assertThat(FestivosCalendarioUtil.esDiaNoLaborable(quinceAgosto)).isTrue();

        // 17 de agosto (lunes laborable)
        LocalDate diecisieteAgosto = LocalDate.of(2026, 8, 17);
        assertThat(FestivosCalendarioUtil.esVacacionesAgosto(diecisieteAgosto)).isFalse();
        assertThat(FestivosCalendarioUtil.esFestivoNacional(diecisieteAgosto)).isFalse();
        assertThat(FestivosCalendarioUtil.esDiaNoLaborable(diecisieteAgosto)).isFalse();
    }

    @Test
    @DisplayName("Valida las 2 semanas de Navidad (23 de diciembre al 7 de enero inclusive) como vacaciones")
    void testVacacionesNavidad()
    {
        // 22 de diciembre: día laborable
        LocalDate veintidosDic = LocalDate.of(2026, 12, 22);
        assertThat(FestivosCalendarioUtil.esVacacionesNavidad(veintidosDic)).isFalse();
        assertThat(FestivosCalendarioUtil.esDiaNoLaborable(veintidosDic)).isFalse();

        // 23 de diciembre a 31 de diciembre
        for (int dia = 23; dia <= 31; dia++)
        {
            LocalDate fechaNavidad = LocalDate.of(2026, 12, dia);
            assertThat(FestivosCalendarioUtil.esVacacionesNavidad(fechaNavidad)).isTrue();
            assertThat(FestivosCalendarioUtil.esDiaNoLaborable(fechaNavidad)).isTrue();
        }

        // 1 de enero a 7 de enero (cambio de año)
        for (int dia = 1; dia <= 7; dia++)
        {
            LocalDate fechaEnero = LocalDate.of(2027, 1, dia);
            assertThat(FestivosCalendarioUtil.esVacacionesNavidad(fechaEnero)).isTrue();
            assertThat(FestivosCalendarioUtil.esDiaNoLaborable(fechaEnero)).isTrue();
        }

        // 8 de enero: laborable
        LocalDate ochoEnero = LocalDate.of(2027, 1, 8);
        assertThat(FestivosCalendarioUtil.esVacacionesNavidad(ochoEnero)).isFalse();
        assertThat(FestivosCalendarioUtil.esFestivoNacional(ochoEnero)).isFalse();
        assertThat(FestivosCalendarioUtil.esDiaNoLaborable(ochoEnero)).isFalse();
    }

    @Test
    @DisplayName("Identifica días laborables comunes retornando false y motivo nulo")
    void testDiasLaborablesHabituales()
    {
        LocalDate diaNormal = LocalDate.of(2026, 10, 15);
        assertThat(FestivosCalendarioUtil.esFestivoNacional(diaNormal)).isFalse();
        assertThat(FestivosCalendarioUtil.esPeriodoVacacionalAutoescuela(diaNormal)).isFalse();
        assertThat(FestivosCalendarioUtil.esDiaNoLaborable(diaNormal)).isFalse();
        assertThat(FestivosCalendarioUtil.obtenerMotivoNoLaborable(diaNormal)).isNull();
        assertThat(FestivosCalendarioUtil.obtenerTipoNoLaborable(diaNormal)).isNull();
    }
}
