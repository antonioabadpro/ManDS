package com.autoescuela.erp.examenes.dto;

import java.time.LocalDateTime;

/**
 * Representa la disponibilidad y estado de cupo de una fecha oficial de examen para una solicitud.
 */
public record DisponibilidadConvocatoriaDTO(
        int numeroConvocatoria,
        LocalDateTime fechaHora,
        int cupoMaximo,
        int alumnosCitados,
        int plazasLibres,
        boolean disponible,
        String motivoNoDisponible
)
{

}
