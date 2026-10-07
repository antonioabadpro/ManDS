package com.autoescuela.erp.flota.dto;

import java.time.LocalDateTime;

import com.autoescuela.erp.core.enums.EstadoIncidencia;

/**
 * DTO inmutable (record) con el detalle completo de una incidencia mecánica para visualización informativa.
 */
public record IncidenciaDetalleDTO(
    Long id,
    LocalDateTime fechaHora,
    String descripcion,
    EstadoIncidencia estado,
    Long vehiculoId,
    String vehiculoMatricula,
    String vehiculoMarca,
    String vehiculoModelo,
    Long profesorId,
    String profesorNombreCompleto
)
{
}

