package com.autoescuela.erp.usuarios.dto;

import java.time.LocalDate;
import java.util.List;

import com.autoescuela.erp.core.enums.EstadoUsuario;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.enums.TipoTurno;

/**
 * DTO inmutable (record) para la visualización agregada y en tiempo real
 * del profesorado en el panel de administración.
 */
public record ProfesorResumenDTO(
    Long id,
    String nombre,
    String apellidos,
    String nombreCompleto,
    String iniciales,
    String dni,
    String correo,
    String telefono,
    LocalDate fechaContratacion,
    Integer anioContratacion,
    TipoTurno turno,
    Long vehiculoId,
    String vehiculoMatricula,
    String vehiculoModelo,
    String vehiculoDescripcion,
    List<TipoCarnet> permisos,
    int totalAlumnos,
    EstadoUsuario estado
)
{
}
