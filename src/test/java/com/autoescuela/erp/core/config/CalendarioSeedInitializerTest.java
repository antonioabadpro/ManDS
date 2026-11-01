package com.autoescuela.erp.core.config;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.autoescuela.erp.core.enums.EstadoClase;
import com.autoescuela.erp.core.enums.TipoExamen;
import com.autoescuela.erp.examenes.model.Examen;
import com.autoescuela.erp.examenes.model.SolicitudExamen;
import com.autoescuela.erp.examenes.repository.ExamenRepository;
import com.autoescuela.erp.practicas.model.ClasePractica;
import com.autoescuela.erp.practicas.repository.ClasePracticaRepository;
import com.autoescuela.erp.usuarios.model.Profesor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalendarioSeedInitializerTest
{
    @Mock
    private ClasePracticaRepository clasePracticaRepository;

    @Mock
    private ExamenRepository examenRepository;

    @InjectMocks
    private CalendarioSeedInitializer calendarioSeedInitializer;

    @BeforeEach
    void setUp()
    {
        ReflectionTestUtils.setField(this.calendarioSeedInitializer, "activado", true);
    }

    @Test
    @DisplayName("Poda algorítmica: al iniciar en sábado salta directamente dos días al lunes")
    void testObtenerSiguienteDiaLectivo_InicioSabado_SaltaDirectoALunes()
    {
        // 17 de octubre de 2026 es sábado
        LocalDate sabado = LocalDate.of(2026, 10, 17);
        assertEquals(DayOfWeek.SATURDAY, sabado.getDayOfWeek());

        LocalDate siguienteLectivo = this.calendarioSeedInitializer.obtenerSiguienteDiaLectivo(sabado, Collections.emptySet());

        // Debe llegar al lunes 19 de octubre de 2026
        assertEquals(LocalDate.of(2026, 10, 19), siguienteLectivo);
        assertEquals(DayOfWeek.MONDAY, siguienteLectivo.getDayOfWeek());
    }

    @Test
    @DisplayName("Al iniciar en domingo salta directamente un día al lunes hábil")
    void testObtenerSiguienteDiaLectivo_InicioDomingo_SaltaDirectoALunes()
    {
        // 18 de octubre de 2026 es domingo
        LocalDate domingo = LocalDate.of(2026, 10, 18);
        assertEquals(DayOfWeek.SUNDAY, domingo.getDayOfWeek());

        LocalDate siguienteLectivo = this.calendarioSeedInitializer.obtenerSiguienteDiaLectivo(domingo, Collections.emptySet());

        assertEquals(LocalDate.of(2026, 10, 19), siguienteLectivo);
        assertEquals(DayOfWeek.MONDAY, siguienteLectivo.getDayOfWeek());
    }

    @Test
    @DisplayName("Si el lunes es festivo nacional o traslado, avanza automáticamente al martes hábil")
    void testObtenerSiguienteDiaLectivo_LunesFestivo_AvanzaAMartes()
    {
        // 1 de noviembre de 2026 es domingo (Todos los Santos), trasladado al lunes 2 de noviembre
        LocalDate domingoFestivo = LocalDate.of(2026, 11, 1);

        LocalDate siguienteLectivo = this.calendarioSeedInitializer.obtenerSiguienteDiaLectivo(domingoFestivo, Collections.emptySet());

        // Debe saltar el lunes 2 (festivo trasladado) y posicionarse en el martes 3 de noviembre
        assertEquals(LocalDate.of(2026, 11, 3), siguienteLectivo);
        assertEquals(DayOfWeek.TUESDAY, siguienteLectivo.getDayOfWeek());
    }

    @Test
    @DisplayName("Si la fecha cae en periodo vacacional de verano, avanza tras el 15 de agosto al primer día hábil")
    void testObtenerSiguienteDiaLectivo_PeriodoVacacionalAgosto_AvanzaPostVacaciones()
    {
        // 5 de agosto de 2026 (dentro del periodo vacacional 1-14 de agosto)
        LocalDate diaAgosto = LocalDate.of(2026, 8, 5);

        LocalDate siguienteLectivo = this.calendarioSeedInitializer.obtenerSiguienteDiaLectivo(diaAgosto, Collections.emptySet());

        // El 15 de agosto es sábado y festivo nacional, el 16 es domingo -> primer hábil es lunes 17 de agosto
        assertEquals(LocalDate.of(2026, 8, 17), siguienteLectivo);
        assertEquals(DayOfWeek.MONDAY, siguienteLectivo.getDayOfWeek());
    }

    @Test
    @DisplayName("Si el día lectivo tiene examen práctico DGT del profesor, se descarta y avanza al día siguiente hábil")
    void testObtenerSiguienteDiaLectivo_ConExamenDgtProfesor_AvanzaSiguienteDia()
    {
        // Lunes 19 de octubre de 2026
        LocalDate lunes = LocalDate.of(2026, 10, 19);
        Set<LocalDate> diasExamen = Set.of(lunes);

        LocalDate siguienteLectivo = this.calendarioSeedInitializer.obtenerSiguienteDiaLectivo(lunes, diasExamen);

        // Al estar ocupado el lunes con examen práctico oficial, avanza al martes 20
        assertEquals(LocalDate.of(2026, 10, 20), siguienteLectivo);
    }

    @Test
    @DisplayName("Poda algorítmica inversa: desde el lunes retrocede como máximo al viernes anterior")
    void testObtenerUltimoDiaLectivoAnterior_DesdeLunes_RetrocedeAViernes()
    {
        // Lunes 19 de octubre de 2026
        LocalDate lunes = LocalDate.of(2026, 10, 19);

        LocalDate ultimoLectivo = this.calendarioSeedInitializer.obtenerUltimoDiaLectivoAnterior(lunes, Collections.emptySet());

        // El último día lectivo anterior al lunes es el viernes 16 de octubre
        assertEquals(LocalDate.of(2026, 10, 16), ultimoLectivo);
        assertEquals(DayOfWeek.FRIDAY, ultimoLectivo.getDayOfWeek());
    }

    @Test
    @DisplayName("Desde el martes retrocede exactamente al lunes anterior si es hábil")
    void testObtenerUltimoDiaLectivoAnterior_DesdeMartes_RetrocedeALunes()
    {
        // Martes 20 de octubre de 2026
        LocalDate martes = LocalDate.of(2026, 10, 20);

        LocalDate ultimoLectivo = this.calendarioSeedInitializer.obtenerUltimoDiaLectivoAnterior(martes, Collections.emptySet());

        assertEquals(LocalDate.of(2026, 10, 19), ultimoLectivo);
        assertEquals(DayOfWeek.MONDAY, ultimoLectivo.getDayOfWeek());
    }

    @Test
    @DisplayName("Identifica correctamente días inhábiles por fin de semana y festivos")
    void testEsDiaInhabil()
    {
        LocalDate sabado = LocalDate.of(2026, 10, 17);
        LocalDate domingo = LocalDate.of(2026, 10, 18);
        LocalDate festivoNacional = LocalDate.of(2026, 10, 12); // Fiesta Nacional de España
        LocalDate laborable = LocalDate.of(2026, 10, 14); // Miércoles ordinario

        assertTrue(this.calendarioSeedInitializer.esDiaInhabil(sabado));
        assertTrue(this.calendarioSeedInitializer.esDiaInhabil(domingo));
        assertTrue(this.calendarioSeedInitializer.esDiaInhabil(festivoNacional));
        assertFalse(this.calendarioSeedInitializer.esDiaInhabil(laborable));
    }

    @Test
    @DisplayName("Reubica una clase práctica pendiente que cae en sábado al siguiente día hábil")
    void testAjustarClasesPracticasPendientes_ClaseEnSabado_ReubicaALunes()
    {
        // GIVEN: Clase en sábado futuro
        Profesor profesor = new Profesor();
        ReflectionTestUtils.setField(profesor, "id", 100L);

        ClasePractica clase = new ClasePractica();
        ReflectionTestUtils.setField(clase, "id", 37L);
        clase.setProfesor(profesor);
        clase.setEstadoClase(EstadoClase.PENDIENTE);
        // Sábado a las 08:00
        LocalDate sabadoFuturo = LocalDate.now().plusWeeks(2).with(DayOfWeek.SATURDAY);
        clase.setFechaHora(sabadoFuturo.atTime(LocalTime.of(8, 0)));

        when(this.examenRepository.findByTipoOrderByFechaHoraDesc(TipoExamen.PRACTICO)).thenReturn(Collections.emptyList());
        when(this.clasePracticaRepository.findByEstadoClaseOrderByFechaHoraAsc(EstadoClase.PENDIENTE)).thenReturn(List.of(clase));
        when(this.clasePracticaRepository.findByProfesorOrderByFechaHoraAsc(profesor)).thenReturn(List.of(clase));

        // WHEN
        this.calendarioSeedInitializer.ajustarClasesPracticasPendientes();

        // THEN: La fecha no debe ser ni sábado ni domingo
        DayOfWeek dowResultante = clase.getFechaHora().getDayOfWeek();
        assertFalse(dowResultante == DayOfWeek.SATURDAY || dowResultante == DayOfWeek.SUNDAY);
        assertEquals(LocalTime.of(8, 0), clase.getFechaHora().toLocalTime());
        verify(this.clasePracticaRepository).save(clase);
    }

    @Test
    @DisplayName("Reubica un examen oficial programado en domingo hacia un día hábil lectivo")
    void testAjustarExamenesOficiales_ExamenEnDomingo_ReubicaADiaHabil()
    {
        // GIVEN: Examen en domingo
        Examen examen = new Examen();
        ReflectionTestUtils.setField(examen, "id", 10L);
        LocalDate domingo = LocalDate.of(2026, 11, 1); // Domingo festivo
        examen.setFechaHora(domingo.atTime(LocalTime.of(10, 0)));

        SolicitudExamen solicitud = new SolicitudExamen();
        examen.setSolicitudExamen(solicitud);

        when(this.examenRepository.findAll()).thenReturn(List.of(examen));

        // WHEN
        this.calendarioSeedInitializer.ajustarExamenesOficiales();

        // THEN: Se reubica a día hábil (en este caso martes 3 de nov por traslado)
        assertEquals(LocalDate.of(2026, 11, 3), examen.getFechaHora().toLocalDate());
        assertEquals(LocalDate.of(2026, 11, 3), solicitud.getFechaHora().toLocalDate());
        verify(this.examenRepository).save(examen);
    }
}
