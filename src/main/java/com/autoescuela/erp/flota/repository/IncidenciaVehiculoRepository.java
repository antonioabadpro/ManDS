package com.autoescuela.erp.flota.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.autoescuela.erp.core.enums.EstadoIncidencia;
import com.autoescuela.erp.flota.model.IncidenciaVehiculo;
import com.autoescuela.erp.flota.model.Vehiculo;
import com.autoescuela.erp.usuarios.model.Profesor;

public interface IncidenciaVehiculoRepository extends JpaRepository<IncidenciaVehiculo, Long>
{
    /**
     * Recupera todas las incidencias reportadas por un profesor ordenadas de más reciente a más antigua.
     */
    List<IncidenciaVehiculo> findByProfesorOrderByFechaHoraDesc(Profesor profesor);

    /**
     * Recupera todas las incidencias mecánicas sufridas por un vehículo específico.
     */
    List<IncidenciaVehiculo> findByVehiculoOrderByFechaHoraDesc(Vehiculo vehiculo);

    /**
     * Recupera todas las incidencias ordenadas por estado (PENDIENTE, EN_PROCESO, RESUELTA)
     * y secundariamente por fecha y hora ascendente.
     */
    @Query("SELECT i FROM IncidenciaVehiculo i ORDER BY CASE i.estado " +
           "WHEN com.autoescuela.erp.core.enums.EstadoIncidencia.PENDIENTE THEN 1 " +
           "WHEN com.autoescuela.erp.core.enums.EstadoIncidencia.EN_PROCESO THEN 2 " +
           "WHEN com.autoescuela.erp.core.enums.EstadoIncidencia.RESUELTA THEN 3 " +
           "ELSE 4 END ASC, i.fechaHora ASC")
    List<IncidenciaVehiculo> findAllOrdenadasPorEstadoYFechaAsc();

    /**
     * Contabiliza el total de incidencias en un estado concreto.
     */
    long countByEstado(EstadoIncidencia estado);
}
