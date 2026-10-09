package com.autoescuela.erp.flota.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autoescuela.erp.core.enums.EstadoVehiculo;
import com.autoescuela.erp.core.excepciones.RecursoNoEncontradoException;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.flota.dto.AltaVehiculoDTO;
import com.autoescuela.erp.flota.dto.EditarVehiculoDTO;
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
     * Normaliza la cadena para comparar en formato canónico '0000-ZZZ'.
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

    /**
     * Comprueba si una matrícula ya se encuentra registrada en la base de datos de la flota
     * excluyendo un ID específico (utilizado para validación en la edición de vehículos).
     * Normaliza la cadena para comparar en formato canónico '0000-ZZZ'.
     *
     * @param matricula Matrícula a comprobar.
     * @param id Identificador del vehículo a excluir de la búsqueda.
     * @return true si ya existe otro vehículo con esa matrícula, false en caso contrario.
     */
    @Transactional(readOnly = true)
    public boolean existeMatriculaOtroVehiculo(String matricula, Long id)
    {
        if (matricula == null || matricula.isBlank())
        {
            return false;
        }
        String limpia = matricula.trim().toUpperCase().replace(" ", "").replace("-", "");
        String formateada = limpia.length() == 7 ? limpia.substring(0, 4) + "-" + limpia.substring(4) : limpia;
        return this.vehiculoRepository.existsByMatriculaAndIdNot(formateada, id);
    }

    /**
     * Obtiene los datos de un vehículo mapeados a su DTO de edición para poblar el formulario de modificación.
     * Valida la existencia del vehículo y que no se encuentre en estado INACTIVO.
     *
     * @param id Identificador único del vehículo.
     * @return DTO inmutable con los datos del vehículo para su edición.
     * @throws RecursoNoEncontradoException Si no existe ningún vehículo con el ID especificado.
     * @throws ReglaNegocioException Si el vehículo se encuentra en estado INACTIVO.
     */
    @Transactional(readOnly = true)
    public EditarVehiculoDTO obtenerVehiculoParaEdicion(Long id)
    {
        if (id == null)
        {
            throw new ReglaNegocioException("El identificador del vehículo no puede ser nulo.");
        }

        Vehiculo vehiculo = this.vehiculoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el vehículo con ID: " + id));

        if (vehiculo.getEstado() == EstadoVehiculo.INACTIVO)
        {
            throw new ReglaNegocioException("No se puede modificar un vehículo en estado INACTIVO.");
        }

        return this.vehiculoMapper.toEditarVehiculoDTO(vehiculo);
    }

    /**
     * Comprueba si un vehículo tiene un profesor asignado actualmente.
     *
     * @param id Identificador único del vehículo.
     * @return true si el vehículo existe y tiene profesor asignado, false en caso contrario.
     */
    @Transactional(readOnly = true)
    public boolean tieneProfesorAsignado(Long id)
    {
        if (id == null)
        {
            return false;
        }
        return this.vehiculoRepository.findById(id)
                .map(v -> v.getProfesor() != null)
                .orElse(false);
    }

    /**
     * Modifica los datos de un vehículo existente en el parque móvil aplicando las restricciones de negocio:
     * 1. No se puede modificar un vehículo en estado INACTIVO.
     * 2. Si el vehículo tiene un profesor asignado, no se puede alterar el tipo de carnet ni la matrícula.
     * 3. Unicidad de matrícula comprobando que no exista otro vehículo con la misma mediante existsByMatriculaAndIdNot.
     * 4. Validaciones de fechas de matriculación, revisiones e ITV coherentes.
     * 5. Kilometraje, potencia y año no pueden ser negativos.
     *
     * @param dto DTO con los datos actualizados del vehículo.
     * @return Entidad Vehiculo modificada y persistida.
     * @throws ReglaNegocioException Si se vulnera alguna de las restricciones de dominio.
     */
    @Transactional
    public Vehiculo modificarVehiculo(EditarVehiculoDTO dto)
    {
        if (dto == null || dto.id() == null)
        {
            throw new ReglaNegocioException("Los datos del vehículo no pueden ser nulos.");
        }

        Vehiculo vehiculo = this.vehiculoRepository.findById(dto.id())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el vehículo con ID: " + dto.id()));

        if (vehiculo.getEstado() == EstadoVehiculo.INACTIVO)
        {
            throw new ReglaNegocioException("No se puede modificar un vehículo en estado INACTIVO.");
        }

        if (dto.km() != null && dto.km() < 0)
        {
            throw new ReglaNegocioException("El kilometraje no puede ser negativo.");
        }

        if (dto.cv() != null && (dto.cv() < 0 || dto.cv() > 1000))
        {
            throw new ReglaNegocioException("La potencia no puede ser negativa ni superar los 1000 CV.");
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
            throw new ReglaNegocioException("La fecha de la próxima revisión no puede superar los 10 años en el futuro, ni puede ser anterior a la fecha actual.");
        }

        boolean tieneProfesor = vehiculo.getProfesor() != null;
        String matriculaFormateada = dto.formatearMatricula();

        if (tieneProfesor)
        {
            if (dto.tipoPermiso() != vehiculo.getTipoPermiso())
            {
                throw new ReglaNegocioException("No se puede modificar el tipo de carnet de un vehículo que tiene un profesor asignado.");
            }
            if (matriculaFormateada != null && !matriculaFormateada.equalsIgnoreCase(vehiculo.getMatricula()))
            {
                throw new ReglaNegocioException("No se puede modificar la matrícula de un vehículo que tiene un profesor asignado.");
            }
        }
        else
        {
            if (this.vehiculoRepository.existsByMatriculaAndIdNot(matriculaFormateada, dto.id()))
            {
                throw new ReglaNegocioException("Ya existe un vehículo registrado en la flota con la matrícula " + matriculaFormateada + ".");
            }
            vehiculo.setMatricula(matriculaFormateada);
            vehiculo.setTipoPermiso(dto.tipoPermiso());
        }

        vehiculo.setMarca(dto.marca().trim());
        vehiculo.setModelo(dto.modelo().trim());
        vehiculo.setColor(dto.color().trim());
        vehiculo.setKm(dto.km());
        vehiculo.setCv(dto.cv());
        vehiculo.setAnio(dto.anio());
        vehiculo.setTipoCombustible(dto.tipoCombustible());
        vehiculo.setCajaCambios(dto.cajaCambios());
        vehiculo.setFechaUltimaRevision(dto.fechaUltimaRevision());
        vehiculo.setFechaProximaRevision(dto.fechaProximaRevision());

        return this.vehiculoRepository.save(vehiculo);
    }

    /**
     * Tramita la baja lógica (borrado lógico) de un vehículo del parque móvil.
     * Restricciones de dominio:
     * 1. El vehículo debe existir.
     * 2. No se puede dar de baja un vehículo que ya se encuentra en estado INACTIVO.
     * 3. No se puede dar de baja un vehículo si tiene un profesor asignado. Debe desvincularse previamente desde Gestión de Profesores.
     * 4. Solo se pueden dar de baja vehículos que se encuentren en estado DISPONIBLE.
     *
     * @param id Identificador único del vehículo.
     * @return Entidad Vehiculo actualizada en estado INACTIVO.
     * @throws RecursoNoEncontradoException Si no existe ningún vehículo con el ID especificado.
     * @throws ReglaNegocioException Si se incumple alguna restricción de dominio.
     */
    @Transactional
    public Vehiculo darBajaVehiculo(Long id)
    {
        if (id == null)
        {
            throw new ReglaNegocioException("El identificador del vehículo no puede ser nulo.");
        }

        Vehiculo vehiculo = this.vehiculoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el vehículo con ID: " + id));

        if (vehiculo.getEstado() == EstadoVehiculo.INACTIVO)
        {
            throw new ReglaNegocioException("El vehículo ya se encuentra en estado INACTIVO.");
        }

        if (vehiculo.getProfesor() != null)
        {
            String nombreProfesor = vehiculo.getProfesor().getNombre() + " " + vehiculo.getProfesor().getApellidos();
            throw new ReglaNegocioException("No se puede dar de baja el vehículo porque tiene asignado al profesor "
                    + nombreProfesor + ". Para poder realizar esta operación, antes debe desvincular al profesor del vehículo desde el panel de Gestión de Profesores.");
        }

        if (vehiculo.getEstado() != EstadoVehiculo.DISPONIBLE)
        {
            throw new ReglaNegocioException("Solo se pueden dar de baja vehículos que se encuentren en estado DISPONIBLE.");
        }

        vehiculo.setEstado(EstadoVehiculo.INACTIVO);
        return this.vehiculoRepository.save(vehiculo);
    }
}
