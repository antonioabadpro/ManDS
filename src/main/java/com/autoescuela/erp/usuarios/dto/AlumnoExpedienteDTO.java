package com.autoescuela.erp.usuarios.dto;

import java.util.List;

import com.autoescuela.erp.core.enums.TipoCarnet;

/**
 * DTO inmutable (record) con la totalidad de la información académica del expediente del alumno:
 * saldos de clases, convocatorias activas, y los últimos registros de prácticas y exámenes DGT.
 */
public record AlumnoExpedienteDTO(
    Long id,
    String nombreCompleto,
    TipoCarnet tipoCarnet,
    String tipoCarnetDescripcion,
    boolean tieneMatriculaActiva,
    Integer saldoClases,
    long clasesRealizadas,
    Integer convocatoriasRestantes,
    List<ClasePracticaExpedienteDTO> ultimasClases,
    List<ExamenExpedienteDTO> ultimosExamenes
)
{
}
