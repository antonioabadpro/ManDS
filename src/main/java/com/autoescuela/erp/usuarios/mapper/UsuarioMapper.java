package com.autoescuela.erp.usuarios.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.autoescuela.erp.usuarios.dto.EditarPerfilAdminDTO;
import com.autoescuela.erp.usuarios.model.Persona;

/**
 * Mapeador de MapStruct para transformaciones estructurales entre la jerarquía
 * de entidades de Usuario (Persona) y los DTOs de perfil y consulta.
 */
@Mapper(componentModel = "spring")
public interface UsuarioMapper
{
    /**
     * Mapea los datos de una Persona a su DTO de edición de perfil administrativo.
     * Convierte el enumerado del rol a su representación textual.
     *
     * @param persona Entidad persistente de usuario.
     * @return DTO poblado con los datos personales actuales.
     */
    @Mapping(target = "rol", expression = "java(persona.getRol() != null ? persona.getRol().name() : null)")
    EditarPerfilAdminDTO toEditarPerfilAdminDTO(Persona persona);
}

