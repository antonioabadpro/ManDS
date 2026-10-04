package com.autoescuela.erp.usuarios.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.autoescuela.erp.usuarios.dto.AltaProfesorDTO;
import com.autoescuela.erp.usuarios.dto.EditarPerfilProfesorDTO;
import com.autoescuela.erp.usuarios.dto.EditarProfesorDTO;
import com.autoescuela.erp.usuarios.dto.ProfesorDetalleDTO;
import com.autoescuela.erp.usuarios.dto.ProfesorResumenDTO;
import com.autoescuela.erp.usuarios.model.Profesor;

/**
 * Mapeador de MapStruct para transformaciones estructurales entre la entidad
 * {@link Profesor} y sus DTOs asociados (como {@link AltaProfesorDTO}, {@link EditarProfesorDTO} y {@link ProfesorResumenDTO}).
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
    @Mapping(target = "listaClasesPracticas", ignore = true)
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

    /**
     * Mapea una entidad Profesor a su DTO de resumen para el listado reactivo del panel de administración.
     *
     * @param profesor Entidad del docente.
     * @return DTO inmutable para presentación en la tabla.
     */
    @Mapping(target = "nombreCompleto", expression = "java(profesor.getNombre() + \" \" + profesor.getApellidos())")
    @Mapping(target = "iniciales", expression = "java(calcularIniciales(profesor))")
    @Mapping(target = "anioContratacion", expression = "java(profesor.getFechaContratacion() != null ? profesor.getFechaContratacion().getYear() : null)")
    @Mapping(target = "vehiculoId", expression = "java(profesor.getVehiculo() != null ? profesor.getVehiculo().getId() : null)")
    @Mapping(target = "vehiculoMatricula", expression = "java(profesor.getVehiculo() != null ? profesor.getVehiculo().getMatricula() : null)")
    @Mapping(target = "vehiculoModelo", expression = "java(profesor.getVehiculo() != null ? profesor.getVehiculo().getMarca() + \" \" + profesor.getVehiculo().getModelo() : null)")
    @Mapping(target = "vehiculoDescripcion", expression = "java(profesor.getVehiculo() != null ? profesor.getVehiculo().getMatricula() + \" (\" + profesor.getVehiculo().getMarca() + \" \" + profesor.getVehiculo().getModelo() + \")\" : \"Sin vehículo asignado\")")
    @Mapping(target = "permisos", source = "listaTiposCarnet")
    @Mapping(target = "totalAlumnos", expression = "java(profesor.getListaAlumnos() != null ? profesor.getListaAlumnos().size() : 0)")
    @Mapping(target = "totalClasesPendientes", expression = "java(profesor.getListaClasesPracticas() != null ? (int) profesor.getListaClasesPracticas().stream().filter(c -> c.getEstadoClase() == com.autoescuela.erp.core.enums.EstadoClase.PENDIENTE).count() : 0)")
    ProfesorResumenDTO toProfesorResumenDTO(Profesor profesor);

    /**
     * Mapea una entidad Profesor a su DTO de edición completa para el modal de administración.
     *
     * @param profesor Entidad del docente a editar.
     * @return DTO poblado con los datos actuales del profesor.
     */
    @Mapping(target = "vehiculoId", expression = "java(profesor.getVehiculo() != null ? profesor.getVehiculo().getId() : null)")
    @Mapping(target = "permisos", source = "listaTiposCarnet")
    EditarProfesorDTO toEditarProfesorDTO(Profesor profesor);

    /**
     * Mapea una entidad Profesor a su DTO de detalle completo para la visualización en el modal de ficha del docente.
     *
     * @param profesor Entidad del docente.
     * @return DTO inmutable con datos personales, laborales, antigüedad calculada y flota asignada.
     */
    @Mapping(target = "nombreCompleto", expression = "java(profesor.getNombre() + \" \" + profesor.getApellidos())")
    @Mapping(target = "iniciales", expression = "java(calcularIniciales(profesor))")
    @Mapping(target = "edad", expression = "java(profesor.getFechaNacimiento() != null ? java.time.Period.between(profesor.getFechaNacimiento(), java.time.LocalDate.now()).getYears() : null)")
    @Mapping(target = "anioContratacion", expression = "java(profesor.getFechaContratacion() != null ? profesor.getFechaContratacion().getYear() : null)")
    @Mapping(target = "antiguedadAnios", expression = "java(profesor.getFechaContratacion() != null ? java.time.Period.between(profesor.getFechaContratacion(), java.time.LocalDate.now()).getYears() : 0)")
    @Mapping(target = "vehiculoId", expression = "java(profesor.getVehiculo() != null ? profesor.getVehiculo().getId() : null)")
    @Mapping(target = "vehiculoMatricula", expression = "java(profesor.getVehiculo() != null ? profesor.getVehiculo().getMatricula() : null)")
    @Mapping(target = "vehiculoModelo", expression = "java(profesor.getVehiculo() != null ? profesor.getVehiculo().getMarca() + \" \" + profesor.getVehiculo().getModelo() : null)")
    @Mapping(target = "vehiculoTipo", expression = "java(profesor.getVehiculo() != null ? profesor.getVehiculo().getTipo() : null)")
    @Mapping(target = "vehiculoTipoDescripcion", expression = "java(profesor.getVehiculo() != null && profesor.getVehiculo().getTipo() != null ? profesor.getVehiculo().getTipo().getDescripcion() : null)")
    @Mapping(target = "permisos", source = "listaTiposCarnet")
    @Mapping(target = "totalAlumnos", expression = "java(profesor.getListaAlumnos() != null ? profesor.getListaAlumnos().size() : 0)")
    @Mapping(target = "totalClasesPendientes", expression = "java(profesor.getListaClasesPracticas() != null ? (int) profesor.getListaClasesPracticas().stream().filter(c -> c.getEstadoClase() == com.autoescuela.erp.core.enums.EstadoClase.PENDIENTE).count() : 0)")
    ProfesorDetalleDTO toProfesorDetalleDTO(Profesor profesor);

    /**
     * Calcula las iniciales del nombre y primer apellido de un docente para el avatar.
     */
    default String calcularIniciales(Profesor profesor)
    {
        if (profesor == null)
        {
            return "PR";
        }
        String n = (profesor.getNombre() != null && !profesor.getNombre().isBlank())
                ? profesor.getNombre().trim().substring(0, 1).toUpperCase()
                : "";
        String a = (profesor.getApellidos() != null && !profesor.getApellidos().isBlank())
                ? profesor.getApellidos().trim().substring(0, 1).toUpperCase()
                : "";
        String res = n + a;
        return res.isBlank() ? "PR" : res;
    }
}
