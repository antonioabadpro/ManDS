package com.autoescuela.erp.practicas.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.autoescuela.erp.core.enums.EstadoClase;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.flota.model.Vehiculo;
import com.autoescuela.erp.practicas.dto.ClasePracticaAdminDTO;
import com.autoescuela.erp.practicas.model.ClasePractica;

/**
 * Mapeador de MapStruct para transformaciones estructurales entre la entidad
 * {@link ClasePractica} y sus DTOs asociados.
 */
@Mapper(componentModel = "spring")
public interface ClasePracticaMapper
{
    /**
     * Mapea una entidad ClasePractica con sus relaciones cargadas al DTO
     * representativo para la supervisión y control del Administrador.
     *
     * @param clase Entidad de la clase práctica persistente.
     * @return DTO inmutable con datos de la sesión, alumno, profesor, vehículo y odómetro.
     */
    @Mapping(target = "kmFormateado", expression = "java(calcularKmFormateado(clase))")
    @Mapping(target = "alumnoId", expression = "java(clase.getAlumno() != null ? clase.getAlumno().getId() : null)")
    @Mapping(target = "alumnoNombreCompleto", expression = "java(calcularAlumnoNombreCompleto(clase))")
    @Mapping(target = "alumnoUsername", expression = "java(clase.getAlumno() != null && clase.getAlumno().getNombreUsuario() != null ? clase.getAlumno().getNombreUsuario() : \"-\")")
    @Mapping(target = "profesorId", expression = "java(clase.getProfesor() != null ? clase.getProfesor().getId() : null)")
    @Mapping(target = "profesorNombreCompleto", expression = "java(calcularProfesorNombreCompleto(clase))")
    @Mapping(target = "profesorUsername", expression = "java(clase.getProfesor() != null && clase.getProfesor().getNombreUsuario() != null ? clase.getProfesor().getNombreUsuario() : \"-\")")
    @Mapping(target = "vehiculoMatricula", expression = "java(calcularVehiculoMatricula(clase))")
    @Mapping(target = "vehiculoModelo", expression = "java(calcularVehiculoModelo(clase))")
    @Mapping(target = "vehiculoDescripcion", expression = "java(calcularVehiculoDescripcion(clase))")
    @Mapping(target = "tipoPermiso", expression = "java(calcularTipoPermiso(clase))")
    @Mapping(target = "tipoPermisoDescripcion", expression = "java(calcularTipoPermisoDescripcion(clase))")
    ClasePracticaAdminDTO toClasePracticaAdminDTO(ClasePractica clase);

    /**
     * Mapea una lista de entidades ClasePractica a una lista de DTOs ClasePracticaAdminDTO.
     *
     * @param clases Lista de entidades persistentes.
     * @return Lista de DTOs inmutables para la capa de presentación.
     */
    List<ClasePracticaAdminDTO> toClasePracticaAdminDTOList(List<ClasePractica> clases);

    default String calcularAlumnoNombreCompleto(ClasePractica clase)
    {
        if (clase == null || clase.getAlumno() == null)
        {
            return "Sin alumno";
        }
        String nombre = clase.getAlumno().getNombre() != null ? clase.getAlumno().getNombre() : "";
        String apellidos = clase.getAlumno().getApellidos() != null ? clase.getAlumno().getApellidos() : "";
        return (nombre + " " + apellidos).trim();
    }

    default String calcularProfesorNombreCompleto(ClasePractica clase)
    {
        if (clase == null || clase.getProfesor() == null)
        {
            return null;
        }
        String nombre = clase.getProfesor().getNombre() != null ? clase.getProfesor().getNombre() : "";
        String apellidos = clase.getProfesor().getApellidos() != null ? clase.getProfesor().getApellidos() : "";
        return (nombre + " " + apellidos).trim();
    }

    default String calcularVehiculoMatricula(ClasePractica clase)
    {
        Vehiculo v = extraerVehiculo(clase);
        return v != null ? v.getMatricula() : null;
    }

    default String calcularVehiculoModelo(ClasePractica clase)
    {
        Vehiculo v = extraerVehiculo(clase);
        if (v == null)
        {
            return null;
        }
        String marca = v.getMarca() != null ? v.getMarca().trim() : "";
        String modelo = v.getModelo() != null ? v.getModelo().trim() : "";
        return (marca + " " + modelo).trim();
    }

    default String calcularVehiculoDescripcion(ClasePractica clase)
    {
        Vehiculo v = extraerVehiculo(clase);
        if (v == null)
        {
            return null;
        }
        String modelo = calcularVehiculoModelo(clase);
        String matricula = v.getMatricula() != null ? v.getMatricula() : "";
        return modelo + " (" + matricula + ")";
    }

    default TipoCarnet calcularTipoPermiso(ClasePractica clase)
    {
        Vehiculo v = extraerVehiculo(clase);
        return v != null ? v.getTipoPermiso() : null;
    }

    default String calcularTipoPermisoDescripcion(ClasePractica clase)
    {
        TipoCarnet permiso = calcularTipoPermiso(clase);
        return permiso != null ? permiso.getDescripcion() : null;
    }

    default String calcularKmFormateado(ClasePractica clase)
    {
        if (clase == null || clase.getEstadoClase() != EstadoClase.RECIBIDA)
        {
            return "--";
        }
        int ini = clase.getKmInicio() != null ? clase.getKmInicio() : 0;
        int fin = clase.getKmFin() != null ? clase.getKmFin() : 0;
        int dif = Math.max(0, fin - ini);
        return String.format("%,d a %,d km (+%d km)", ini, fin, dif).replace(',', '.');
    }

    default Vehiculo extraerVehiculo(ClasePractica clase)
    {
        if (clase == null || clase.getProfesor() == null)
        {
            return null;
        }
        return clase.getProfesor().getVehiculo();
    }
}
