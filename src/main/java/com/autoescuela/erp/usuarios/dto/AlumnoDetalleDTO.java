package com.autoescuela.erp.usuarios.dto;

import java.time.LocalDate;

import com.autoescuela.erp.core.enums.EstadoUsuario;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.enums.TipoTurno;

/**
 * DTO inmutable (record) con la totalidad de los datos informativos del alumno
 * para su presentación en el modal de detalle y consulta en el panel de administración.
 */
public record AlumnoDetalleDTO(
    Long id,
    String nombre,
    String apellidos,
    String nombreCompleto,
    String iniciales,
    String nombreUsuario,
    EstadoUsuario estado,
    String dni,
    LocalDate fechaNacimiento,
    Integer edad,
    String direccion,
    String correo,
    String telefono,
    LocalDate fechaMatriculacion,
    Integer anioMatriculacion,
    Integer antiguedadAnios,
    String antiguedadTexto,
    boolean tieneMatriculaActiva,
    TipoTurno turno,
    Long profesorId,
    String profesorNombre,
    Long vehiculoId,
    String vehiculoMatricula,
    String vehiculoModelo,
    String vehiculoTipoDescripcion,
    TipoCarnet tipoCarnet,
    String tipoCarnetDescripcion
)
{
}
