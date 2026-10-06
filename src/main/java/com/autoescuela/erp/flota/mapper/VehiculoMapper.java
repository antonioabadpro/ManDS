package com.autoescuela.erp.flota.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.autoescuela.erp.core.enums.EstadoIncidencia;
import com.autoescuela.erp.flota.dto.AltaVehiculoDTO;
import com.autoescuela.erp.flota.dto.VehiculoDetalleDTO;
import com.autoescuela.erp.flota.dto.VehiculoResumenDTO;
import com.autoescuela.erp.flota.model.IncidenciaVehiculo;
import com.autoescuela.erp.flota.model.Vehiculo;

/**
 * Mapeador de MapStruct para transformaciones estructurales entre la entidad
 * {@link Vehiculo} y sus DTOs asociados.
 */
@Mapper(componentModel = "spring")
public interface VehiculoMapper
{
    /**
     * Mapea un DTO de alta a una nueva entidad Vehiculo persistible.
     *
     * @param dto DTO con los datos recogidos en el formulario.
     * @return Entidad Vehiculo lista para su persistencia.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estado", constant = "DISPONIBLE")
    @Mapping(target = "matricula", expression = "java(dto.formatearMatricula())")
    @Mapping(target = "profesor", ignore = true)
    @Mapping(target = "incidencias", ignore = true)
    Vehiculo toEntity(AltaVehiculoDTO dto);

    /**
     * Mapea una entidad Vehiculo a su DTO de resumen para la tabla del parque móvil.
     *
     * @param vehiculo Entidad del vehículo persistente.
     * @return DTO inmutable con datos de identificación, especificaciones y profesor asignado.
     */
    @Mapping(target = "matriculaPrefijo", expression = "java(calcularPrefijoMatricula(vehiculo))")
    @Mapping(target = "tipoDescripcion", expression = "java(vehiculo.getTipoPermiso() != null ? vehiculo.getTipoPermiso().getDescripcion() : \"\")")
    @Mapping(target = "profesorId", expression = "java(vehiculo.getProfesor() != null ? vehiculo.getProfesor().getId() : null)")
    @Mapping(target = "profesorNombreCompleto", expression = "java(vehiculo.getProfesor() != null ? vehiculo.getProfesor().getNombre() + \" \" + vehiculo.getProfesor().getApellidos() : null)")
    @Mapping(target = "profesorTurno", expression = "java(vehiculo.getProfesor() != null ? vehiculo.getProfesor().getTurno() : null)")
    VehiculoResumenDTO toVehiculoResumenDTO(Vehiculo vehiculo);

    /**
     * Mapea una entidad Vehiculo a su DTO detallado para la ficha completa del modal.
     *
     * @param vehiculo Entidad del vehículo persistente.
     * @return DTO inmutable con la totalidad de campos mecánicos, operativos y de mantenimiento.
     */
    @Mapping(target = "matriculaPrefijo", expression = "java(calcularPrefijoMatricula(vehiculo))")
    @Mapping(target = "tipoDescripcion", expression = "java(vehiculo.getTipoPermiso() != null ? vehiculo.getTipoPermiso().getDescripcion() : \"\")")
    @Mapping(target = "combustibleDescripcion", expression = "java(calcularDescripcionCombustible(vehiculo))")
    @Mapping(target = "cambioDescripcion", expression = "java(calcularDescripcionCambio(vehiculo))")
    @Mapping(target = "profesorId", expression = "java(vehiculo.getProfesor() != null ? vehiculo.getProfesor().getId() : null)")
    @Mapping(target = "profesorNombreCompleto", expression = "java(vehiculo.getProfesor() != null ? vehiculo.getProfesor().getNombre() + \" \" + vehiculo.getProfesor().getApellidos() : null)")
    @Mapping(target = "profesorDni", expression = "java(vehiculo.getProfesor() != null ? vehiculo.getProfesor().getDni() : null)")
    @Mapping(target = "profesorTurno", expression = "java(vehiculo.getProfesor() != null ? vehiculo.getProfesor().getTurno() : null)")
    @Mapping(target = "totalIncidencias", expression = "java(vehiculo.getIncidencias() != null ? vehiculo.getIncidencias().size() : 0)")
    @Mapping(target = "incidenciasPendientes", expression = "java(calcularIncidenciasPendientes(vehiculo))")
    VehiculoDetalleDTO toVehiculoDetalleDTO(Vehiculo vehiculo);

    /**
     * Extrae el prefijo numérico identificativo de la matrícula (ej: "1234" de "1234-LMN")
     * o los primeros 4 caracteres para el badge gráfico de la tabla.
     */
    default String calcularPrefijoMatricula(Vehiculo vehiculo)
    {
        if (vehiculo == null || vehiculo.getMatricula() == null || vehiculo.getMatricula().isBlank())
        {
            return "----";
        }
        String mat = vehiculo.getMatricula().trim();
        int guionIndex = mat.indexOf("-");
        if (guionIndex > 0)
        {
            return mat.substring(0, guionIndex);
        }
        return mat.length() >= 4 ? mat.substring(0, 4) : mat;
    }

    /**
     * Devuelve la etiqueta legible en español del tipo de combustible.
     */
    default String calcularDescripcionCombustible(Vehiculo vehiculo)
    {
        if (vehiculo == null || vehiculo.getTipoCombustible() == null)
        {
            return "No especificado";
        }
        return vehiculo.getTipoCombustible().getDescripcion();
    }

    /**
     * Devuelve la etiqueta legible en español del tipo de transmisión o caja de cambios.
     */
    default String calcularDescripcionCambio(Vehiculo vehiculo)
    {
        if (vehiculo == null || vehiculo.getCajaCambios() == null)
        {
            return "No especificado";
        }
        return vehiculo.getCajaCambios().getDescripcion();
    }

    /**
     * Calcula el número de incidencias no resueltas registradas en el vehículo.
     */
    default int calcularIncidenciasPendientes(Vehiculo vehiculo)
    {
        if (vehiculo == null || vehiculo.getIncidencias() == null)
        {
            return 0;
        }
        int pendientes = 0;
        for (IncidenciaVehiculo inc : vehiculo.getIncidencias())
        {
            if (inc != null && inc.getEstado() != null && inc.getEstado() != EstadoIncidencia.RESUELTA)
            {
                pendientes++;
            }
        }
        return pendientes;
    }
}
