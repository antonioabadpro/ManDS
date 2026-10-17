package com.autoescuela.erp.practicas.dto;

import java.time.LocalDateTime;

import com.autoescuela.erp.core.enums.EstadoClase;
import com.autoescuela.erp.core.enums.TipoCarnet;

/**
 * DTO inmutable (record) representativo de una clase práctica para la supervisión
 * y control transversal en el panel del Administrador.
 */
public record ClasePracticaAdminDTO(
    Long id,
    LocalDateTime fechaHora,
    Integer duracion,
    String puntoRecogida,
    Long kmInicio,
    Long kmFin,
    String kmFormateado,
    EstadoClase estadoClase,
    Long alumnoId,
    String alumnoNombreCompleto,
    String alumnoUsername,
    Long profesorId,
    String profesorNombreCompleto,
    String profesorUsername,
    String vehiculoMatricula,
    String vehiculoModelo,
    String vehiculoDescripcion,
    TipoCarnet tipoPermiso,
    String tipoPermisoDescripcion,
    String observaciones,
    String motivoCancelacion
)
{
}
