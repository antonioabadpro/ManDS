package com.autoescuela.erp.usuarios.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.autoescuela.erp.academico.model.Matricula;
import com.autoescuela.erp.usuarios.dto.AlumnoDetalleDTO;
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
     * Mapea una entidad Alumno y su matrícula activa a su DTO de detalle completo
     * para la visualización en el modal de ficha del alumno.
     *
     * @param alumno Entidad del alumno con datos personales y profesor asignado.
     * @param matriculaActiva Matrícula activa vigente del alumno (o null si no tiene).
     * @return DTO inmutable con datos personales, vías de contacto, expediente, turno y flota asignada.
     */
    @Mapping(target = "id", source = "alumno.id")
    @Mapping(target = "nombre", source = "alumno.nombre")
    @Mapping(target = "apellidos", source = "alumno.apellidos")
    @Mapping(target = "nombreCompleto", expression = "java(alumno != null ? alumno.getNombre() + \" \" + alumno.getApellidos() : null)")
    @Mapping(target = "iniciales", expression = "java(calcularIniciales(alumno))")
    @Mapping(target = "nombreUsuario", source = "alumno.nombreUsuario")
    @Mapping(target = "estado", source = "alumno.estado")
    @Mapping(target = "dni", source = "alumno.dni")
    @Mapping(target = "fechaNacimiento", source = "alumno.fechaNacimiento")
    @Mapping(target = "edad", expression = "java(alumno != null && alumno.getFechaNacimiento() != null ? java.time.Period.between(alumno.getFechaNacimiento(), java.time.LocalDate.now()).getYears() : null)")
    @Mapping(target = "direccion", source = "alumno.direccion")
    @Mapping(target = "correo", source = "alumno.correo")
    @Mapping(target = "telefono", source = "alumno.telefono")
    @Mapping(target = "fechaMatriculacion", expression = "java(matriculaActiva != null ? matriculaActiva.getFechaMatriculacion() : null)")
    @Mapping(target = "anioMatriculacion", expression = "java(matriculaActiva != null && matriculaActiva.getFechaMatriculacion() != null ? matriculaActiva.getFechaMatriculacion().getYear() : null)")
    @Mapping(target = "antiguedadAnios", expression = "java(matriculaActiva != null && matriculaActiva.getFechaMatriculacion() != null ? java.time.Period.between(matriculaActiva.getFechaMatriculacion(), java.time.LocalDate.now()).getYears() : 0)")
    @Mapping(target = "antiguedadTexto", expression = "java(calcularAntiguedadTexto(matriculaActiva))")
    @Mapping(target = "tieneMatriculaActiva", expression = "java(matriculaActiva != null)")
    @Mapping(target = "turno", source = "alumno.profesor.turno")
    @Mapping(target = "profesorId", source = "alumno.profesor.id")
    @Mapping(target = "profesorNombre", expression = "java(alumno != null && alumno.getProfesor() != null ? alumno.getProfesor().getNombre() + \" \" + alumno.getProfesor().getApellidos() : null)")
    @Mapping(target = "vehiculoId", expression = "java((alumno != null && alumno.getProfesor() != null && alumno.getProfesor().getVehiculo() != null) ? alumno.getProfesor().getVehiculo().getId() : null)")
    @Mapping(target = "vehiculoMatricula", expression = "java((alumno != null && alumno.getProfesor() != null && alumno.getProfesor().getVehiculo() != null) ? alumno.getProfesor().getVehiculo().getMatricula() : null)")
    @Mapping(target = "vehiculoModelo", expression = "java((alumno != null && alumno.getProfesor() != null && alumno.getProfesor().getVehiculo() != null) ? alumno.getProfesor().getVehiculo().getMarca() + \" \" + alumno.getProfesor().getVehiculo().getModelo() : null)")
    @Mapping(target = "vehiculoTipoDescripcion", expression = "java((alumno != null && alumno.getProfesor() != null && alumno.getProfesor().getVehiculo() != null && alumno.getProfesor().getVehiculo().getTipo() != null) ? alumno.getProfesor().getVehiculo().getTipo().getDescripcion() : null)")
    @Mapping(target = "tipoCarnet", expression = "java(matriculaActiva != null ? matriculaActiva.getPermisoCarnet() : null)")
    @Mapping(target = "tipoCarnetDescripcion", expression = "java(matriculaActiva != null && matriculaActiva.getPermisoCarnet() != null ? matriculaActiva.getPermisoCarnet().getDescripcion() : null)")
    AlumnoDetalleDTO toAlumnoDetalleDTO(Alumno alumno, Matricula matriculaActiva);

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

    /**
     * Calcula el texto representativo de la antigüedad de matriculación del alumno.
     *
     * @param matricula Matrícula activa del alumno.
     * @return Cadena descriptiva (ej. "2 años", "1 año", "5 meses", "Menos de 1 mes").
     */
    default String calcularAntiguedadTexto(Matricula matricula)
    {
        if (matricula == null || matricula.getFechaMatriculacion() == null)
        {
            return "Sin antigüedad";
        }
        java.time.Period p = java.time.Period.between(matricula.getFechaMatriculacion(), java.time.LocalDate.now());
        if (p.getYears() > 1)
        {
            return p.getYears() + " años";
        }
        else if (p.getYears() == 1)
        {
            return "1 año";
        }
        else if (p.getMonths() > 1)
        {
            return p.getMonths() + " meses";
        }
        else if (p.getMonths() == 1)
        {
            return "1 mes";
        }
        else
        {
            return "Menos de 1 mes";
        }
    }
}
