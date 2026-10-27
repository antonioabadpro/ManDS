package com.autoescuela.erp.examenes.dto;

import java.time.LocalDate;
import com.autoescuela.erp.examenes.model.SolicitudExamen;

/**
 * DTO que encapsula el diagnóstico y las opciones para resolver/gestionar una solicitud de examen en el panel de Administración.
 * Contiene el análisis de cupo y disponibilidad para Fecha 1 y Fecha 2, así como validaciones pedagógicas y el mes evaluado.
 */
public record GestionarSolicitudExamenDTO(
        SolicitudExamen solicitud,
        DisponibilidadConvocatoriaDTO convocatoria1,
        DisponibilidadConvocatoriaDTO convocatoria2,
        boolean puedeAceptar,
        String motivoBloqueoGeneral,
        LocalDate mesSeleccionado
)
{
    public GestionarSolicitudExamenDTO(SolicitudExamen solicitud, DisponibilidadConvocatoriaDTO convocatoria1, DisponibilidadConvocatoriaDTO convocatoria2, boolean puedeAceptar, String motivoBloqueoGeneral)
    {
        this(solicitud, convocatoria1, convocatoria2, puedeAceptar, motivoBloqueoGeneral, LocalDate.now().withDayOfMonth(1));
    }
}
