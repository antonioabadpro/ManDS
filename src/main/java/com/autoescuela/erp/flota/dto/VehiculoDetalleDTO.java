package com.autoescuela.erp.flota.dto;

import java.time.LocalDate;

import com.autoescuela.erp.core.enums.EstadoVehiculo;
import com.autoescuela.erp.core.enums.TipoCambio;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.enums.TipoCombustible;
import com.autoescuela.erp.core.enums.TipoTurno;

/**
 * DTO inmutable (record) con el desglose integral de datos técnicos, administrativos,
 * de mantenimiento y asignación de un vehículo para su presentación en el modal de detalle.
 */
public record VehiculoDetalleDTO(
    Long id,
    String matricula,
    String matriculaPrefijo,
    String marca,
    String modelo,
    String color,
    Long km,
    Integer cv,
    Integer anio,
    TipoCombustible tipoCombustible,
    String combustibleDescripcion,
    TipoCambio cajaCambios,
    String cambioDescripcion,
    LocalDate fechaUltimaRevision,
    LocalDate fechaProximaRevision,
    EstadoVehiculo estado,
    TipoCarnet tipoPermiso,
    String tipoDescripcion,
    Long profesorId,
    String profesorNombreCompleto,
    String profesorDni,
    TipoTurno profesorTurno,
    int totalIncidencias,
    int incidenciasPendientes
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
