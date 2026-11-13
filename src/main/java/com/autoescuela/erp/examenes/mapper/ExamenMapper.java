package com.autoescuela.erp.examenes.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.autoescuela.erp.examenes.dto.ExamenResumenDTO;
import com.autoescuela.erp.examenes.dto.SolicitudExamenDashboardDTO;
import com.autoescuela.erp.examenes.model.Examen;
import com.autoescuela.erp.examenes.model.SolicitudExamen;
import com.autoescuela.erp.usuarios.model.Persona;

/**
 * Mapeador de MapStruct para transformaciones estructurales entre las entidades
 * del módulo de exámenes ({@link SolicitudExamen}, {@link Examen}) y sus DTOs de proyección.
 */
@Mapper(componentModel = "spring")
public interface ExamenMapper
{
    /**
     * Proyecta una solicitud de examen pendiente a su DTO de resumen para el Dashboard de Administración.
     *
     * @param solicitud Entidad de la solicitud de examen con alumno, profesor y matrícula cargados.
     * @return DTO inmutable de resumen.
     */
    @Mapping(target = "alumnoNombreCompleto", expression = "java(construirNombreCompletoNullable(solicitud.getAlumno()))")
    @Mapping(target = "profesorNombreCompleto", expression = "java(construirNombreCompletoNullable(solicitud.getProfesor()))")
    @Mapping(target = "permisoCarnet", source = "matricula.permisoCarnet")
    SolicitudExamenDashboardDTO toSolicitudExamenDashboardDTO(SolicitudExamen solicitud);

    /**
     * Proyecta una lista de solicitudes de examen a sus DTOs de resumen para el Dashboard.
     *
     * @param solicitudes Lista de solicitudes persistentes.
     * @return Lista de DTOs inmutables.
     */
    List<SolicitudExamenDashboardDTO> toSolicitudExamenDashboardDTOList(List<SolicitudExamen> solicitudes);

    /**
     * Proyecta un acta de examen oficial DGT a su DTO de resumen para listados y calendarios.
     *
     * @param examen Entidad del examen con alumno, solicitud y matrícula cargados.
     * @return DTO inmutable con datos del acta y alumno examinado.
     */
    @Mapping(target = "alumnoNombreCompleto", expression = "java(construirNombreCompletoObligatorio(examen.getAlumno()))")
    @Mapping(target = "alumnoDni", expression = "java(extraerAlumnoDni(examen))")
    @Mapping(target = "permisoCarnet", source = "solicitudExamen.matricula.permisoCarnet")
    ExamenResumenDTO toExamenResumenDTO(Examen examen);

    /**
     * Proyecta una lista de actas de examen oficial a sus DTOs de resumen.
     *
     * @param examenes Lista de actas persistentes.
     * @return Lista de DTOs inmutables.
     */
    List<ExamenResumenDTO> toExamenResumenDTOList(List<Examen> examenes);

    /**
     * Construye el nombre completo de una persona devolviendo {@code null} si la referencia es nula.
     */
    default String construirNombreCompletoNullable(Persona persona)
    {
        if (persona == null)
        {
            return null;
        }
        String nombre = persona.getNombre() != null ? persona.getNombre().trim() : "";
        String apellidos = persona.getApellidos() != null ? persona.getApellidos().trim() : "";
        return (nombre + " " + apellidos).trim();
    }

    /**
     * Construye el nombre completo de una persona devolviendo cadena vacía si la referencia es nula.
     */
    default String construirNombreCompletoObligatorio(Persona persona)
    {
        String nombreCompleto = this.construirNombreCompletoNullable(persona);
        return nombreCompleto != null ? nombreCompleto : "";
    }

    /**
     * Extrae de forma segura el DNI del alumno asociado al examen.
     */
    default String extraerAlumnoDni(Examen examen)
    {
        if (examen == null || examen.getAlumno() == null || examen.getAlumno().getDni() == null)
        {
            return "";
        }
        return examen.getAlumno().getDni();
    }
}
