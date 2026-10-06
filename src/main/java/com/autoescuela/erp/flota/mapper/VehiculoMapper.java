package com.autoescuela.erp.flota.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.autoescuela.erp.flota.dto.VehiculoResumenDTO;
import com.autoescuela.erp.flota.model.Vehiculo;

/**
 * Mapeador de MapStruct para transformaciones estructurales entre la entidad
 * {@link Vehiculo} y sus DTOs asociados.
 */
@Mapper(componentModel = "spring")
public interface VehiculoMapper
{
    /**
     * Mapea una entidad Vehiculo a su DTO de resumen para la tabla del parque móvil.
     *
     * @param vehiculo Entidad del vehículo persistente.
     * @return DTO inmutable con datos de identificación, especificaciones y profesor asignado.
     */
    @Mapping(target = "matriculaPrefijo", expression = "java(calcularPrefijoMatricula(vehiculo))")
    @Mapping(target = "tipoDescripcion", expression = "java(vehiculo.getTipo() != null ? vehiculo.getTipo().getDescripcion() : \"\")")
    @Mapping(target = "profesorId", expression = "java(vehiculo.getProfesor() != null ? vehiculo.getProfesor().getId() : null)")
    @Mapping(target = "profesorNombreCompleto", expression = "java(vehiculo.getProfesor() != null ? vehiculo.getProfesor().getNombre() + \" \" + vehiculo.getProfesor().getApellidos() : null)")
    @Mapping(target = "profesorTurno", expression = "java(vehiculo.getProfesor() != null ? vehiculo.getProfesor().getTurno() : null)")
    VehiculoResumenDTO toVehiculoResumenDTO(Vehiculo vehiculo);

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
}
