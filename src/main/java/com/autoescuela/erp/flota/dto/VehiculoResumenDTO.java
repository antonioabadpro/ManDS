package com.autoescuela.erp.flota.dto;

import java.time.LocalDate;

import com.autoescuela.erp.core.enums.EstadoVehiculo;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.enums.TipoTurno;

/**
 * DTO inmutable (record) para la visualización del parque móvil de vehículos
 * en el panel de administración.
 */
public record VehiculoResumenDTO(
    Long id,
    String matricula,
    String matriculaPrefijo,
    String marca,
    String modelo,
    String color,
    Long km,
    LocalDate fechaUltimaRevision,
    LocalDate fechaProximaRevision,
    EstadoVehiculo estado,
    TipoCarnet tipo,
    String tipoDescripcion,
    Long profesorId,
    String profesorNombreCompleto,
    TipoTurno profesorTurno
)
{
    /**
     * Comprueba si la fecha de la próxima revisión o ITV ya ha vencido.
     */
    public boolean isRevisionVencida()
    {
        return this.fechaProximaRevision != null && this.fechaProximaRevision.isBefore(LocalDate.now());
    }

    /**
     * Comprueba si la fecha de la próxima revisión o ITV está próxima a vencer (en menos de 60 días).
     */
    public boolean isRevisionProxima()
    {
        return this.fechaProximaRevision != null
                && !this.isRevisionVencida()
                && this.fechaProximaRevision.isBefore(LocalDate.now().plusDays(60));
    }

    /**
     * Comprueba si la revisión o ITV está al día con margen suficiente (60 días o más).
     */
    public boolean isRevisionAlDia()
    {
        return this.fechaProximaRevision != null
                && !this.fechaProximaRevision.isBefore(LocalDate.now().plusDays(60));
    }
}
