package com.autoescuela.erp.flota.mapper;

import org.springframework.stereotype.Component;

import com.autoescuela.erp.flota.dto.IncidenciaDetalleDTO;
import com.autoescuela.erp.flota.dto.IncidenciaResumenDTO;
import com.autoescuela.erp.flota.model.IncidenciaVehiculo;

/**
 * Mapeador de entidades IncidenciaVehiculo a sus correspondientes DTOs de capa de presentación.
 */
@Component
public class IncidenciaVehiculoMapper
{
    /**
     * Transforma una entidad IncidenciaVehiculo a su representación resumida para la tabla.
     */
    public IncidenciaResumenDTO toIncidenciaResumenDTO(IncidenciaVehiculo incidencia)
    {
        if (incidencia == null)
        {
            return null;
        }

        String matricula = incidencia.getVehiculo() != null ? incidencia.getVehiculo().getMatricula() : "—";
        String marca = incidencia.getVehiculo() != null ? incidencia.getVehiculo().getMarca() : "—";
        String modelo = incidencia.getVehiculo() != null ? incidencia.getVehiculo().getModelo() : "—";
        Long vehiculoId = incidencia.getVehiculo() != null ? incidencia.getVehiculo().getId() : null;

        Long profesorId = incidencia.getProfesor() != null ? incidencia.getProfesor().getId() : null;
        String profesorNombre = incidencia.getProfesor() != null
                ? incidencia.getProfesor().getNombre() + " " + incidencia.getProfesor().getApellidos()
                : "Sin profesor asignado";

        return new IncidenciaResumenDTO(
                incidencia.getId(),
                incidencia.getFechaHora(),
                incidencia.getDescripcion(),
                incidencia.getEstado(),
                vehiculoId,
                matricula,
                marca,
                modelo,
                profesorId,
                profesorNombre
        );
    }

    /**
     * Transforma una entidad IncidenciaVehiculo a su representación de detalle para el modal de lectura.
     */
    public IncidenciaDetalleDTO toIncidenciaDetalleDTO(IncidenciaVehiculo incidencia)
    {
        if (incidencia == null)
        {
            return null;
        }

        String matricula = incidencia.getVehiculo() != null ? incidencia.getVehiculo().getMatricula() : "—";
        String marca = incidencia.getVehiculo() != null ? incidencia.getVehiculo().getMarca() : "—";
        String modelo = incidencia.getVehiculo() != null ? incidencia.getVehiculo().getModelo() : "—";
        Long vehiculoId = incidencia.getVehiculo() != null ? incidencia.getVehiculo().getId() : null;

        Long profesorId = incidencia.getProfesor() != null ? incidencia.getProfesor().getId() : null;
        String profesorNombre = incidencia.getProfesor() != null
                ? incidencia.getProfesor().getNombre() + " " + incidencia.getProfesor().getApellidos()
                : "Sin profesor asignado";

        return new IncidenciaDetalleDTO(
                incidencia.getId(),
                incidencia.getFechaHora(),
                incidencia.getDescripcion(),
                incidencia.getEstado(),
                vehiculoId,
                matricula,
                marca,
                modelo,
                profesorId,
                profesorNombre
        );
    }
}
