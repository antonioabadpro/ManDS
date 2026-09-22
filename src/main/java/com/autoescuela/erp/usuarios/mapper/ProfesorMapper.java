package com.autoescuela.erp.usuarios.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.autoescuela.erp.usuarios.dto.AltaProfesorDTO;
import com.autoescuela.erp.usuarios.model.Profesor;

/**
 * Mapeador de MapStruct para transformaciones estructurales entre la entidad
 * {@link Profesor} y sus DTOs asociados (como {@link AltaProfesorDTO}).
 */
@Mapper(componentModel = "spring")
public interface ProfesorMapper
{
    /**
     * Mapea los datos recibidos en AltaProfesorDTO a una nueva instancia de Profesor.
     * Se ignoran deliberadamente los atributos controlados por la lógica de negocio y seguridad
     * en el servicio (id, credenciales provisionales, estado, vehículo y listas asociadas).
     *
     * @param dto DTO validado con los datos de registro del docente.
     * @return Entidad Profesor mapeada.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "nombreUsuario", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "vehiculo", ignore = true)
    @Mapping(target = "listaAlumnos", ignore = true)
    @Mapping(target = "listaSolicitudesExamen", ignore = true)
    @Mapping(target = "listaTiposCarnet", source = "permisos")
    Profesor toProfesor(AltaProfesorDTO dto);
}
