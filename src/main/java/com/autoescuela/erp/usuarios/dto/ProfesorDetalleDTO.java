package com.autoescuela.erp.usuarios.dto;

import java.time.LocalDate;
import java.util.List;

import com.autoescuela.erp.core.enums.EstadoUsuario;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.enums.TipoTurno;

/**
 * DTO inmutable (record) con la totalidad de los datos informativos del profesor
 * para su presentación en el modal de detalle y consulta en el panel de administración.
 */
public record ProfesorDetalleDTO(
    Long id,
    String nombre,
    String apellidos,
    String nombreCompleto,
    String iniciales,
    String nombreUsuario,
    String dni,
    String correo,
    String telefono,
    LocalDate fechaNacimiento,
    Integer edad,
    String direccion,
    LocalDate fechaContratacion,
    Integer anioContratacion,
    Integer antiguedadAnios,
    TipoTurno turno,
    Long vehiculoId,
    String vehiculoMatricula,
    String vehiculoModelo,
    TipoCarnet vehiculoTipo,
    String vehiculoTipoDescripcion,
    List<TipoCarnet> permisos,
    int totalAlumnos,
    int totalClasesPendientes,
    EstadoUsuario estado
)
{
}
