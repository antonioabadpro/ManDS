package com.autoescuela.erp.flota.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autoescuela.erp.core.enums.EstadoVehiculo;
import com.autoescuela.erp.core.excepciones.RecursoNoEncontradoException;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.flota.dto.AltaVehiculoDTO;
import com.autoescuela.erp.flota.dto.VehiculoDetalleDTO;
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

    /**
     * Obtiene los datos detallados de un vehículo para su visualización en el modal de detalle.
     *
     * @param id Identificador único del vehículo.
     * @return DTO inmutable con la información integral del vehículo.
     * @throws RecursoNoEncontradoException Si no existe ningún vehículo con el ID especificado.
     */
    @Transactional(readOnly = true)
    public VehiculoDetalleDTO obtenerVehiculoParaDetalle(Long id)
    {
        return this.vehiculoRepository.findById(id)
                .map(this.vehiculoMapper::toVehiculoDetalleDTO)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el vehículo con ID: " + id));
    }

    /**
     * Da de alta un nuevo vehículo en el parque móvil de la autoescuela en estado DISPONIBLE.
     * Valida que la matrícula no esté previamente registrada y la persiste en formato normalizado.
     *
     * @param dto DTO con los datos del formulario de alta.
     * @return Entidad Vehiculo persistida.
     * @throws ReglaNegocioException Si ya existe un vehículo con la misma matrícula.
     */
    @Transactional
    public Vehiculo darAltaVehiculo(AltaVehiculoDTO dto)
    {
        if (dto == null)
        {
            throw new ReglaNegocioException("Los datos del vehículo no pueden ser nulos.");
        }

        if (!dto.isFechaMatriculacionValida())
        {
            throw new ReglaNegocioException("El año de matriculación debe estar comprendido entre 1990 y el año actual.");
        }

        if (!dto.isFechaUltimaRevisionValida())
        {
            throw new ReglaNegocioException("La fecha de la última revisión no puede ser anterior a hace 4 años ni posterior a hoy.");
        }

        if (!dto.isRevisionesCoherentes())
        {
            throw new ReglaNegocioException("La fecha de la próxima revisión debe ser estrictamente posterior a la fecha de la última revisión.");
        }

        if (!dto.isFechaProximaRevisionValida())
        {
            throw new ReglaNegocioException("La fecha de la próxima revisión no puede superar los 10 años en el futuro.");
        }

        String matriculaFormateada = dto.formatearMatricula();
        if (this.vehiculoRepository.findByMatricula(matriculaFormateada).isPresent())
        {
            throw new ReglaNegocioException("Ya existe un vehículo registrado en la flota con la matrícula " + matriculaFormateada + ".");
        }
        Vehiculo nuevoVehiculo = this.vehiculoMapper.toEntity(dto);
        nuevoVehiculo.setMatricula(matriculaFormateada);
        nuevoVehiculo.setEstado(EstadoVehiculo.DISPONIBLE);

        return this.vehiculoRepository.save(nuevoVehiculo);
    }

    /**
     * Comprueba si una matrícula ya se encuentra registrada en la base de datos de la flota.
     * Normaliza la cadena para comparar en formato canónico '0000-XXX'.
     *
     * @param matricula Matrícula a comprobar.
     * @return true si ya existe un vehículo con esa matrícula, false en caso contrario.
     */
    @Transactional(readOnly = true)
    public boolean existeMatricula(String matricula)
    {
        if (matricula == null || matricula.isBlank())
        {
            return false;
        }
        String limpia = matricula.trim().toUpperCase().replace(" ", "").replace("-", "");
        String formateada = limpia.length() == 7 ? limpia.substring(0, 4) + "-" + limpia.substring(4) : limpia;
        return this.vehiculoRepository.findByMatricula(formateada).isPresent();
    }
}
