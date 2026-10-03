package com.autoescuela.erp.usuarios.dto;

import java.time.LocalDateTime;

import com.autoescuela.erp.core.enums.TipoExamen;

/**
 * DTO inmutable (record) representativo de una prueba DGT en el histórico del expediente del alumno.
 */
public record ExamenExpedienteDTO(
    Long id,
    TipoExamen tipo,
    String titulo,
    LocalDateTime fechaHora,
    Integer duracion,
    Boolean esApto,
    String centroDgt
)
{
}
