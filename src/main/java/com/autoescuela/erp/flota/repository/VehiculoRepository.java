package com.autoescuela.erp.flota.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autoescuela.erp.core.enums.EstadoVehiculo;
import com.autoescuela.erp.flota.model.Vehiculo;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Long>
{
    /**
     * Busca un vehículo por su número de matrícula.
     */
    Optional<Vehiculo> findByMatricula(String matricula);

    /**
     * Obtiene los vehículos que no tienen profesor asignado y están en el estado indicado.
     */
    List<Vehiculo> findByProfesorIsNullAndEstado(EstadoVehiculo estado);

    /**
     * Obtiene todos los vehículos que no tienen profesor asignado (libres).
     */
    List<Vehiculo> findByProfesorIsNull();

    /**
     * Cuenta el número de vehículos cuyo estado no coincide con el especificado (ej. activos).
     */
    long countByEstadoNot(EstadoVehiculo estado);

    /**
     * Cuenta el número de vehículos en un estado determinado.
     */
    long countByEstado(EstadoVehiculo estado);

    /**
     * Recupera los primeros 5 vehículos ordenados por ID para el resumen del dashboard.
     */
    List<Vehiculo> findTop5ByOrderByIdAsc();

    /**
     * Busca un vehículo asignado a un profesor específico.
     * @param profesorId El ID del profesor.
     * @return Optional con el Vehículo asignado al profesor o vacío si no existe.
     */
    Optional<Vehiculo> findByProfesorId(Long profesorId);
}
