package com.autoescuela.erp.auth.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.autoescuela.erp.auth.dto.RegistroAlumnoDTO;
import com.autoescuela.erp.usuarios.model.Alumno;

@Mapper(componentModel = "spring")
public interface AuthenticationMapper
{
    /**
     * Mapea los datos del DTO de registro público a la entidad Alumno.
     * Se ignoran deliberadamente la contraseña (hasheada por BCrypt en el servicio),
     * el estado inicial y las relaciones con otras entidades.
     *
     * @param dto DTO con los datos validados del formulario.
     * @return Entidad Alumno inicializada.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "profesor", ignore = true)
    @Mapping(target = "historialClasesPracticas", ignore = true)
    @Mapping(target = "historialExamenes", ignore = true)
    @Mapping(target = "historialMatriculas", ignore = true)
    @Mapping(target = "listaSolicitudesExamen", ignore = true)
    Alumno toAlumno(RegistroAlumnoDTO dto);
}
