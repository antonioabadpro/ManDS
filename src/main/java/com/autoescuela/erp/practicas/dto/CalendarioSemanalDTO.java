package com.autoescuela.erp.practicas.dto;

import java.time.LocalDate;
import java.util.List;
import com.autoescuela.erp.core.enums.TipoTurno;

/**
 * DTO principal que transporta la estructura matricial completa de la semana laboral para la cuadrícula SSR.
 */
public record CalendarioSemanalDTO(
    LocalDate fechaInicio,
    LocalDate fechaFin,
    String rangoFechasTexto,
    LocalDate semanaAnterior,
    LocalDate semanaSiguiente,
    LocalDate hoy,
    List<DiaCalendarioDTO> dias,
    List<FilaCalendarioDTO> filas,
    TipoTurno turno,
    boolean tieneProfesorAsignado,
    boolean puedeReservar,
    String motivoBloqueoReserva
)
{
}
