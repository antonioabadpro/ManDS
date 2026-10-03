package com.autoescuela.erp.usuarios.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.autoescuela.erp.academico.model.Matricula;
import com.autoescuela.erp.usuarios.dto.AlumnoResumenDTO;
import com.autoescuela.erp.usuarios.dto.EditarPerfilAlumnoDTO;
import com.autoescuela.erp.usuarios.model.Alumno;

/**
 * Mapeador de MapStruct para transformaciones estructurales entre la entidad
 * {@link Alumno} y sus DTOs asociados para la gestión en el panel de administración.
 */
@Mapper(componentModel = "spring")
public interface AlumnoMapper
{
    /**
     * Mapea una entidad Alumno a su DTO de resumen para la tabla del panel de administración
     * recibiendo su matrícula activa y el total de clases prácticas pendientes.
     *
     * @param alumno Entidad del alumno con datos personales y profesor asignado.
     * @param matriculaActiva Matrícula activa vigente del alumno (o null si no tiene).
     * @param clasesPendientes Total de clases prácticas en estado PENDIENTE.
     * @return DTO inmutable con datos personales, profesor asignado, matrícula activa y saldos.
     */
    @Mapping(target = "id", source = "alumno.id")
    @Mapping(target = "nombreCompleto", expression = "java(alumno != null ? alumno.getNombre() + \" \" + alumno.getApellidos() : null)")
    @Mapping(target = "iniciales", expression = "java(calcularIniciales(alumno))")
    @Mapping(target = "edad", expression = "java(alumno != null && alumno.getFechaNacimiento() != null ? java.time.Period.between(alumno.getFechaNacimiento(), java.time.LocalDate.now()).getYears() : null)")
    @Mapping(target = "profesorId", source = "alumno.profesor.id")
    @Mapping(target = "profesorNombre", expression = "java(alumno != null && alumno.getProfesor() != null ? alumno.getProfesor().getNombre() + \" \" + alumno.getProfesor().getApellidos() : null)")
    @Mapping(target = "profesorTurno", source = "alumno.profesor.turno")
    @Mapping(target = "tieneMatriculaActiva", expression = "java(matriculaActiva != null)")
    @Mapping(target = "tipoCarnet", expression = "java(matriculaActiva != null ? matriculaActiva.getPermisoCarnet() : null)")
    @Mapping(target = "modalidadDescripcion", expression = "java(matriculaActiva != null && matriculaActiva.getModalidad() != null ? matriculaActiva.getModalidad().getDescripcion() : null)")
    @Mapping(target = "saldoClases", expression = "java(matriculaActiva != null ? matriculaActiva.getSaldoClases() : null)")
    @Mapping(target = "clasesPendientes", source = "clasesPendientes")
    @Mapping(target = "convocatoriasRestantes", expression = "java(matriculaActiva != null ? matriculaActiva.getConvocatorias() : null)")
    AlumnoResumenDTO toAlumnoResumenDTO(Alumno alumno, Matricula matriculaActiva, int clasesPendientes);

    /**
     * Reconstruye el DTO de perfil del alumno combinando los campos modificados por el usuario
     * con los campos contractuales y de solo lectura procedentes de la entidad Alumno y su Matrícula activa.
     *
     * @param alumno Entidad persistente del alumno con sus datos base y profesor asignado.
     * @param matriculaActiva Matrícula activa vigente del alumno (o null si no tiene).
     * @param dto DTO recibido en la petición con los campos modificables.
     * @return Nuevo DTO inmutable con la totalidad de campos repoblados.
     */
    @Mapping(target = "id", source = "alumno.id")
    @Mapping(target = "nombre", source = "dto.nombre")
    @Mapping(target = "apellidos", source = "dto.apellidos")
    @Mapping(target = "telefono", source = "dto.telefono")
    @Mapping(target = "direccion", source = "dto.direccion")
    @Mapping(target = "correo", source = "dto.correo")
    @Mapping(target = "fechaNacimiento", source = "dto.fechaNacimiento")
    @Mapping(target = "dni", source = "alumno.dni")
    @Mapping(target = "nombreUsuario", source = "alumno.nombreUsuario")
    @Mapping(target = "profesorNombre", expression = "java(alumno != null && alumno.getProfesor() != null ? alumno.getProfesor().getNombre() + \" \" + alumno.getProfesor().getApellidos() : null)")
    @Mapping(target = "profesorTelefono", expression = "java(alumno != null && alumno.getProfesor() != null ? alumno.getProfesor().getTelefono() : null)")
    @Mapping(target = "profesorEmail", expression = "java(alumno != null && alumno.getProfesor() != null ? alumno.getProfesor().getCorreo() : null)")
    @Mapping(target = "vehiculoModelo", expression = "java((alumno != null && alumno.getProfesor() != null && alumno.getProfesor().getVehiculo() != null) ? alumno.getProfesor().getVehiculo().getMarca() + \" \" + alumno.getProfesor().getVehiculo().getModelo() : null)")
    @Mapping(target = "vehiculoMatricula", expression = "java((alumno != null && alumno.getProfesor() != null && alumno.getProfesor().getVehiculo() != null) ? alumno.getProfesor().getVehiculo().getMatricula() : null)")
    @Mapping(target = "permisoActual", expression = "java(matriculaActiva != null ? matriculaActiva.getPermisoCarnet() : null)")
    @Mapping(target = "fechaMatriculacion", expression = "java(matriculaActiva != null ? matriculaActiva.getFechaMatriculacion() : null)")
    @Mapping(target = "saldoClases", expression = "java(matriculaActiva != null ? matriculaActiva.getSaldoClases() : 0)")
    @Mapping(target = "convocatoriasRestantes", expression = "java(matriculaActiva != null ? matriculaActiva.getConvocatorias() : 2)")
    EditarPerfilAlumnoDTO repoblarPerfilDTO(Alumno alumno, Matricula matriculaActiva, EditarPerfilAlumnoDTO dto);

    /**
     * Calcula las iniciales del alumno a partir de su nombre y apellidos para el avatar.
     *
     * @param alumno Entidad del alumno.
     * @return Cadena con 1 o 2 caracteres en mayúsculas.
     */
    default String calcularIniciales(Alumno alumno)
    {
        if (alumno == null)
        {
            return "AL";
        }
        String n = (alumno.getNombre() != null && !alumno.getNombre().isBlank())
                ? alumno.getNombre().trim().substring(0, 1).toUpperCase()
                : "";
        String a = (alumno.getApellidos() != null && !alumno.getApellidos().isBlank())
                ? alumno.getApellidos().trim().substring(0, 1).toUpperCase()
                : "";
        String res = n + a;
        return res.isBlank() ? "AL" : res;
    }
}
