package com.autoescuela.erp.usuarios.dto;

import com.autoescuela.erp.core.enums.EstadoUsuario;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.enums.TipoTurno;

/**
 * DTO inmutable (record) para la visualización agregada y en tiempo real
 * del alumnado en la tabla del panel de administración.
 */
public record AlumnoResumenDTO(

    Long id,
    String nombre,
    String apellidos,
    String nombreCompleto,
    String iniciales,
    Integer edad,
    String dni,
    String correo,
    EstadoUsuario estado,
    Long profesorId,
    String profesorNombre,
    TipoTurno profesorTurno,
    boolean tieneMatriculaActiva,
    TipoCarnet tipoCarnet,
    String modalidadDescripcion,
    Integer saldoClases,
    int clasesPendientes,
    Integer convocatoriasRestantes
)
{
}
