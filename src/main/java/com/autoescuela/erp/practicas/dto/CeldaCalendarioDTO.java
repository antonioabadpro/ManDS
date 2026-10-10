package com.autoescuela.erp.practicas.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import lombok.Builder;

/**
 * DTO que representa una celda individual en la cuadrícula de calendario.
 */
@Builder(toBuilder = true)
public record CeldaCalendarioDTO(
    LocalDate fecha,
    LocalTime horaInicio,
    LocalTime horaFin,
    LocalDateTime fechaHora,
    boolean ocupada,
    boolean esExamenDgt,
    String estado,
    Long claseId,
    String titulo,
    String subtitulo,
    boolean esPropia,
    boolean esClicable,
    String modalUrl
)
{
    public static CeldaCalendarioDTO deExamenDgt(LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, LocalDateTime fechaHora, String subtitulo)
    {
        return CeldaCalendarioDTO.builder()
                .fecha(fecha)
                .horaInicio(horaInicio)
                .horaFin(horaFin)
                .fechaHora(fechaHora)
                .ocupada(true)
                .esExamenDgt(true)
                .estado("EXAMEN_DGT")
                .titulo("Jornada Examen Oficial DGT")
                .subtitulo(subtitulo)
                .esPropia(false)
                .esClicable(false)
                .build();
    }

    public static CeldaCalendarioDTO deOcupadaProfesor(LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, LocalDateTime fechaHora, Long claseId, String estado, String nombreAlumno, String subtitulo)
    {
        return CeldaCalendarioDTO.builder()
                .fecha(fecha)
                .horaInicio(horaInicio)
                .horaFin(horaFin)
                .fechaHora(fechaHora)
                .ocupada(true)
                .esExamenDgt(false)
                .estado(estado)
                .claseId(claseId)
                .titulo(nombreAlumno)
                .subtitulo(subtitulo)
                .esPropia(true)
                .esClicable(true)
                .modalUrl("/profesor/clases/" + claseId + "/modal")
                .build();
    }

    public static CeldaCalendarioDTO deLibreProfesor(LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, LocalDateTime fechaHora)
    {
        return CeldaCalendarioDTO.builder()
                .fecha(fecha)
                .horaInicio(horaInicio)
                .horaFin(horaFin)
                .fechaHora(fechaHora)
                .ocupada(false)
                .esExamenDgt(false)
                .estado("LIBRE")
                .titulo("Libre")
                .subtitulo("Disponible")
                .esPropia(false)
                .esClicable(false)
                .build();
    }

    public static CeldaCalendarioDTO dePropiaAlumno(LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, LocalDateTime fechaHora, Long claseId, String estado, String subtitulo)
    {
        return CeldaCalendarioDTO.builder()
                .fecha(fecha)
                .horaInicio(horaInicio)
                .horaFin(horaFin)
                .fechaHora(fechaHora)
                .ocupada(true)
                .esExamenDgt(false)
                .estado(estado)
                .claseId(claseId)
                .titulo("Clase Práctica")
                .subtitulo(subtitulo)
                .esPropia(true)
                .esClicable(true)
                .modalUrl("/alumno/clases/" + claseId + "/modal")
                .build();
    }

    public static CeldaCalendarioDTO deOcupadaAjenaAlumno(LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, LocalDateTime fechaHora)
    {
        return CeldaCalendarioDTO.builder()
                .fecha(fecha)
                .horaInicio(horaInicio)
                .horaFin(horaFin)
                .fechaHora(fechaHora)
                .ocupada(true)
                .esExamenDgt(false)
                .estado("OCUPADO")
                .titulo("Clase Práctica")
                .subtitulo("Horario Ocupado")
                .esPropia(false)
                .esClicable(false)
                .build();
    }

    public static CeldaCalendarioDTO deLibreAlumno(LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, LocalDateTime fechaHora, boolean puedeReservar, LocalDateTime ahora)
    {
        boolean esFuturo = fechaHora.isAfter(ahora);
        boolean slotHabilitadoReserva = puedeReservar && esFuturo;
        String modalUrl = slotHabilitadoReserva
                ? "/alumno/clases/reservar-modal?fecha=" + fechaHora.toString()
                : null;

        return CeldaCalendarioDTO.builder()
                .fecha(fecha)
                .horaInicio(horaInicio)
                .horaFin(horaFin)
                .fechaHora(fechaHora)
                .ocupada(false)
                .esExamenDgt(false)
                .estado("LIBRE")
                .titulo(slotHabilitadoReserva ? "Reservar" : "Libre")
                .subtitulo(slotHabilitadoReserva ? "Disponible para reserva" : "No disponible")
                .esPropia(false)
                .esClicable(slotHabilitadoReserva)
                .modalUrl(modalUrl)
                .build();
    }
}
