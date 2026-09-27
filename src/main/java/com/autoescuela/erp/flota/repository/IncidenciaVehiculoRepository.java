package com.autoescuela.erp.flota.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
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
}
