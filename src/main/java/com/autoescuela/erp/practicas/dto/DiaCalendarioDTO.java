package com.autoescuela.erp.practicas.dto;

import java.time.LocalDate;

/**
 * DTO que representa un día de la semana laboral en la cuadrícula de calendario.
 */
public record DiaCalendarioDTO(
    LocalDate fecha,
    String nombreDia,
    String fechaFormateada,
    String encabezadoCompleto,
    boolean esHoy,
    boolean esExamenDgt,
    String examenDgtTexto,
    boolean esNoLaborable,
    String motivoNoLaborable,
    String tipoNoLaborable
)
{
    public DiaCalendarioDTO(LocalDate fecha, String nombreDia, String fechaFormateada, String encabezadoCompleto, boolean esHoy, boolean esExamenDgt, String examenDgtTexto)
    {
        this(fecha, nombreDia, fechaFormateada, encabezadoCompleto, esHoy, esExamenDgt, examenDgtTexto, false, null, null);
    }
}
