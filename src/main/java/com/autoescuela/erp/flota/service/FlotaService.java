package com.autoescuela.erp.flota.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autoescuela.erp.core.enums.EstadoVehiculo;
import com.autoescuela.erp.flota.dto.VehiculoResumenDTO;
import com.autoescuela.erp.flota.mapper.VehiculoMapper;
import com.autoescuela.erp.flota.model.Vehiculo;
import com.autoescuela.erp.flota.repository.VehiculoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FlotaService
{
    private final VehiculoRepository vehiculoRepository;
    private final VehiculoMapper vehiculoMapper;

    /**
     * Obtiene el listado completo de vehículos del parque móvil mapeados a su DTO de resumen.
     */
    @Transactional(readOnly = true)
    public List<VehiculoResumenDTO> obtenerTodosLosVehiculos()
    {
        return this.vehiculoRepository.findAllByOrderByTipoPermisoAsc()
                .stream()
                .map(this.vehiculoMapper::toVehiculoResumenDTO)
                .toList();
    }

    /**
     * Obtiene los vehículos disponibles y sin profesor asignado para la creación o asignación a un profesor.
     */
    @Transactional(readOnly = true)
    public List<Vehiculo> obtenerVehiculosDisponiblesParaProfesor()
    {
        return this.vehiculoRepository.findByProfesorIsNullAndEstado(EstadoVehiculo.DISPONIBLE);
    }

    /**
     * Obtiene los vehículos para el formulario de edición de profesor: los disponibles
     * más el vehículo actualmente asignado al propio profesor si posee uno.
     */
    @Transactional(readOnly = true)
    public List<Vehiculo> obtenerVehiculosParaEdicionProfesor(Long profesorId)
    {
        List<Vehiculo> vehiculos = new ArrayList<>(this.vehiculoRepository.findByProfesorIsNullAndEstado(EstadoVehiculo.DISPONIBLE));
        if (profesorId != null)
        {
            this.vehiculoRepository.findByProfesorId(profesorId).ifPresent(vehiculo -> {
                if (!vehiculos.contains(vehiculo))
                {
                    vehiculos.add(0, vehiculo);
                }
            });
        }
        return vehiculos;
    }

    /**
     * Obtiene los vehículos principales para el resumen rápido del Dashboard de Administración.
     */
    @Transactional(readOnly = true)
    public List<Vehiculo> obtenerVehiculosDashboard()
    {
        return this.vehiculoRepository.findTop5ByOrderByIdAsc();
    }
}
