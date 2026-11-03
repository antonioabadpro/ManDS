package com.autoescuela.erp.flota.dto;

import java.time.LocalDateTime;

import com.autoescuela.erp.core.enums.EstadoIncidencia;

/**
 * DTO inmutable (record) para el listado resumido de incidencias en la bandeja de administración.
 */
public record IncidenciaResumenDTO(
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

