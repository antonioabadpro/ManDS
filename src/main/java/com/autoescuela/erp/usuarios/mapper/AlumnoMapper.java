package com.autoescuela.erp.usuarios.mapper;

import java.util.List;
import java.util.Locale;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.autoescuela.erp.academico.model.Matricula;
import com.autoescuela.erp.examenes.model.Examen;
import com.autoescuela.erp.practicas.model.ClasePractica;
import com.autoescuela.erp.usuarios.dto.AlumnoDetalleDTO;
import com.autoescuela.erp.usuarios.dto.AlumnoExpedienteDTO;
import com.autoescuela.erp.usuarios.dto.AlumnoResumenDTO;
import com.autoescuela.erp.usuarios.dto.ClasePracticaExpedienteDTO;
import com.autoescuela.erp.usuarios.dto.EditarPerfilAlumnoDTO;
import com.autoescuela.erp.usuarios.dto.ExamenExpedienteDTO;
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
    @Mapping(target = "correo", expression = "java(dto != null && dto.correo() != null && !dto.correo().isBlank() ? dto.correo() : (alumno != null ? alumno.getCorreo() : null))")
    @Mapping(target = "fechaNacimiento", source = "dto.fechaNacimiento")
    @Mapping(target = "dni", expression = "java(dto != null && dto.dni() != null && !dto.dni().isBlank() ? dto.dni() : (alumno != null ? alumno.getDni() : null))")
    @Mapping(target = "nombreUsuario", source = "alumno.nombreUsuario")
    @Mapping(target = "profesorNombre", expression = "java(alumno != null && alumno.getProfesor() != null ? alumno.getProfesor().getNombre() + \" \" + alumno.getProfesor().getApellidos() : null)")
    @Mapping(target = "profesorTelefono", expression = "java(alumno != null && alumno.getProfesor() != null ? alumno.getProfesor().getTelefono() : null)")
    @Mapping(target = "profesorEmail", expression = "java(alumno != null && alumno.getProfesor() != null ? alumno.getProfesor().getCorreo() : null)")
    @Mapping(target = "profesorTurno", expression = "java(alumno != null && alumno.getProfesor() != null ? alumno.getProfesor().getTurno() : null)")
    @Mapping(target = "vehiculoMarca", expression = "java((alumno != null && alumno.getProfesor() != null && alumno.getProfesor().getVehiculo() != null) ? alumno.getProfesor().getVehiculo().getMarca() : null)")
    @Mapping(target = "vehiculoModelo", expression = "java((alumno != null && alumno.getProfesor() != null && alumno.getProfesor().getVehiculo() != null) ? alumno.getProfesor().getVehiculo().getModelo() : null)")
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
    @Mapping(target = "vehiculoTipoDescripcion", expression = "java((alumno != null && alumno.getProfesor() != null && alumno.getProfesor().getVehiculo() != null && alumno.getProfesor().getVehiculo().getTipoPermiso() != null) ? alumno.getProfesor().getVehiculo().getTipoPermiso().getDescripcion() : null)")
    @Mapping(target = "tipoCarnet", expression = "java(matriculaActiva != null ? matriculaActiva.getPermisoCarnet() : null)")
    @Mapping(target = "tipoCarnetDescripcion", expression = "java(matriculaActiva != null && matriculaActiva.getPermisoCarnet() != null ? matriculaActiva.getPermisoCarnet().getDescripcion() : null)")
    AlumnoDetalleDTO toAlumnoDetalleDTO(Alumno alumno, Matricula matriculaActiva);

    /**
     * Mapea una clase práctica a su DTO de resumen para el expediente del alumno.
     */
    @Mapping(target = "id", source = "clase.id")
    @Mapping(target = "fechaHora", source = "clase.fechaHora")
    @Mapping(target = "duracion", source = "clase.duracion")
    @Mapping(target = "puntoRecogida", source = "clase.puntoRecogida")
    @Mapping(target = "kmInicio", source = "clase.kmInicio")
    @Mapping(target = "kmFin", source = "clase.kmFin")
    @Mapping(target = "kmFormateado", expression = "java(formatearKilometraje(clase))")
    @Mapping(target = "estadoClase", source = "clase.estadoClase")
    @Mapping(target = "profesorNombre", expression = "java(clase != null && clase.getProfesor() != null ? clase.getProfesor().getNombre() + \" \" + clase.getProfesor().getApellidos() : null)")
    @Mapping(target = "observaciones", source = "clase.observaciones")
    ClasePracticaExpedienteDTO toClasePracticaExpedienteDTO(ClasePractica clase);

    /**
     * Mapea una prueba oficial de examen a su DTO para el expediente del alumno.
     */
    @Mapping(target = "id", source = "examen.id")
    @Mapping(target = "tipo", source = "examen.tipo")
    @Mapping(target = "titulo", expression = "java(calcularTituloExamen(examen))")
    @Mapping(target = "fechaHora", source = "examen.fechaHora")
    @Mapping(target = "duracion", source = "examen.duracion")
    @Mapping(target = "esApto", source = "examen.esApto")
    @Mapping(target = "centroDgt", expression = "java(obtenerCentroDgt(examen))")
    ExamenExpedienteDTO toExamenExpedienteDTO(Examen examen);

    /**
     * Construye el DTO completo del expediente del alumno agrupando métricas, últimas clases y últimos exámenes.
     */
    @Mapping(target = "id", source = "alumno.id")
    @Mapping(target = "nombreCompleto", expression = "java(alumno != null ? alumno.getNombre() + \" \" + alumno.getApellidos() : null)")
    @Mapping(target = "tipoCarnet", expression = "java(matriculaActiva != null ? matriculaActiva.getPermisoCarnet() : null)")
    @Mapping(target = "tipoCarnetDescripcion", expression = "java(matriculaActiva != null && matriculaActiva.getPermisoCarnet() != null ? matriculaActiva.getPermisoCarnet().getDescripcion() : null)")
    @Mapping(target = "tieneMatriculaActiva", expression = "java(matriculaActiva != null)")
    @Mapping(target = "saldoClases", expression = "java(matriculaActiva != null && matriculaActiva.getSaldoClases() != null ? matriculaActiva.getSaldoClases() : 0)")
    @Mapping(target = "clasesRealizadas", source = "clasesRealizadas")
    @Mapping(target = "convocatoriasRestantes", expression = "java(matriculaActiva != null && matriculaActiva.getConvocatorias() != null ? matriculaActiva.getConvocatorias() : 0)")
    @Mapping(target = "ultimasClases", source = "ultimasClases")
    @Mapping(target = "ultimosExamenes", source = "ultimosExamenes")
    AlumnoExpedienteDTO toAlumnoExpedienteDTO(Alumno alumno, Matricula matriculaActiva, long clasesRealizadas,
                                             List<ClasePracticaExpedienteDTO> ultimasClases,
                                             List<ExamenExpedienteDTO> ultimosExamenes);

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

    /**
     * Formatea el intervalo de kilometraje recorrido durante la clase práctica.
     */
    default String formatearKilometraje(ClasePractica clase)
    {
        if (clase == null || clase.getKmInicio() == null || clase.getKmFin() == null)
        {
            return "Km no registrados";
        }
        String textoKm = "Km: " + clase.getKmInicio() + " a " + clase.getKmFin();
        return String.format(Locale.GERMAN, "%s (Total: %,d km)", textoKm, clase.getKmFin() - clase.getKmInicio());
    }

    /**
     * Devuelve el título oficial según el tipo de examen DGT.
     */
    default String calcularTituloExamen(Examen examen)
    {
        if (examen == null || examen.getTipo() == null)
        {
            return "Examen Oficial DGT";
        }
        return "Examen " + (examen.getTipo().name().equals("TEORICO") ? "Teórico" : "Práctico") + " Oficial";
    }

    /**
     * Devuelve el centro examinador oficial asignado al examen DGT.
     */
    default String obtenerCentroDgt(Examen examen)
    {
        return "Centro DGT Móstoles";
    }
}
