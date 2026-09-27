package com.autoescuela.erp.usuarios.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.autoescuela.erp.usuarios.dto.AltaProfesorDTO;
import com.autoescuela.erp.usuarios.dto.EditarPerfilProfesorDTO;
import com.autoescuela.erp.usuarios.model.Profesor;

/**
 * Mapeador de MapStruct para transformaciones estructurales entre la entidad
 * {@link Profesor} y sus DTOs asociados (como {@link AltaProfesorDTO} y {@link EditarPerfilProfesorDTO}).
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

    /**
     * Mapea los datos de una entidad Profesor a su DTO de edición y consulta de perfil.
     *
     * @param profesor Entidad del docente autenticado.
     * @return DTO inmutable con los datos personales y contractuales.
     */
    @Mapping(target = "permisos", source = "listaTiposCarnet")
    @Mapping(target = "vehiculoMatricula", expression = "java(profesor.getVehiculo() != null ? profesor.getVehiculo().getMatricula() : \"Sin vehículo\")")
    @Mapping(target = "vehiculoModelo", expression = "java(profesor.getVehiculo() != null ? profesor.getVehiculo().getMarca() + \" \" + profesor.getVehiculo().getModelo() : \"\")")
    EditarPerfilProfesorDTO toEditarPerfilProfesorDTO(Profesor profesor);

    /**
     * Reconstruye el DTO de perfil combinando los campos modificados por el docente
     * con los campos contractuales de solo lectura procedentes de la entidad Profesor.
     *
     * @param profesor Entidad persistente del docente con los datos contractuales.
     * @param dto DTO recibido en la petición con los campos modificables.
     * @return Nuevo DTO inmutable con la totalidad de campos repoblados.
     */
    @Mapping(target = "id", source = "profesor.id")
    @Mapping(target = "nombre", source = "dto.nombre")
    @Mapping(target = "apellidos", source = "dto.apellidos")
    @Mapping(target = "telefono", source = "dto.telefono")
    @Mapping(target = "direccion", source = "dto.direccion")
    @Mapping(target = "fechaNacimiento", source = "dto.fechaNacimiento")
    @Mapping(target = "dni", source = "profesor.dni")
    @Mapping(target = "nombreUsuario", source = "profesor.nombreUsuario")
    @Mapping(target = "correo", source = "profesor.correo")
    @Mapping(target = "turno", source = "profesor.turno")
    @Mapping(target = "fechaContratacion", source = "profesor.fechaContratacion")
    @Mapping(target = "vehiculoMatricula", expression = "java(profesor.getVehiculo() != null ? profesor.getVehiculo().getMatricula() : \"Sin vehículo\")")
    @Mapping(target = "vehiculoModelo", expression = "java(profesor.getVehiculo() != null ? profesor.getVehiculo().getMarca() + \" \" + profesor.getVehiculo().getModelo() : \"\")")
    @Mapping(target = "permisos", source = "profesor.listaTiposCarnet")
    EditarPerfilProfesorDTO repoblarPerfilDTO(Profesor profesor, EditarPerfilProfesorDTO dto);
}
