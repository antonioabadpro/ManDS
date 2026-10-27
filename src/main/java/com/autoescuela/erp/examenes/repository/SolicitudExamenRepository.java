package com.autoescuela.erp.examenes.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.autoescuela.erp.academico.model.Matricula;
import com.autoescuela.erp.core.enums.EstadoSolicitud;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.enums.TipoExamen;
import com.autoescuela.erp.examenes.model.SolicitudExamen;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.model.Profesor;

public interface SolicitudExamenRepository extends JpaRepository<SolicitudExamen, Long>
{
    /**
     * Recupera las solicitudes de examen asociadas a un profesor.
     */
    List<SolicitudExamen> findByProfesor(Profesor profesor);

    /**
     * Recupera las solicitudes de examen asociadas a un profesor con un estado específico.
     */
    List<SolicitudExamen> findByProfesorAndEstado(Profesor profesor, EstadoSolicitud estado);

    /**
     * Recupera las solicitudes de examen formuladas por un alumno ordenadas por fecha y hora descendente.
     */
    List<SolicitudExamen> findByAlumnoOrderByFechaHoraDesc(Alumno alumno);

    /**
     * Recupera las solicitudes de examen formuladas por un alumno ordenadas por ID descendente.
     */
    List<SolicitudExamen> findByAlumnoOrderByIdDesc(Alumno alumno);

    /**
     * Recupera las solicitudes de examen asociadas a una matrícula ordenadas por fecha y hora descendente.
     */
    List<SolicitudExamen> findByMatriculaOrderByFechaHoraDesc(Matricula matricula);

    /**
     * Comprueba si el alumno tiene alguna solicitud de examen en un determinado estado.
     */
    boolean existsByAlumnoAndEstado(Alumno alumno, EstadoSolicitud estado);

    /**
     * Comprueba si el alumno tiene una solicitud aceptada con fecha de examen posterior a la actual.
     */
    @Query("SELECT COUNT(s) > 0 FROM SolicitudExamen s WHERE s.alumno = :alumno AND s.estado = :estado AND s.examen.fechaHora > :fechaActual")
    boolean existeSolicitudAceptadaConExamenPendiente(@Param("alumno") Alumno alumno, @Param("estado") EstadoSolicitud estado, @Param("fechaActual") LocalDateTime fechaActual);

    /**
     * Cuenta las solicitudes de examen en un determinado estado.
     */
    long countByEstado(EstadoSolicitud estado);

    /**
     * Recupera las últimas 5 solicitudes de examen ordenadas por ID descendente.
     */
    List<SolicitudExamen> findTop5ByOrderByIdDesc();

    /**
     * Recupera las solicitudes de examen en un determinado estado ordenadas cronológicamente (FIFO).
     */
    List<SolicitudExamen> findByEstadoOrderByFechaHoraAsc(EstadoSolicitud estado);

    /**
     * Contabiliza los alumnos ya citados a examen para una fecha y hora, permiso y tipo determinados.
     */
    @Query("SELECT COUNT(e) FROM Examen e " +
           "JOIN e.solicitudExamen s " +
           "JOIN s.matricula m " +
           "WHERE e.fechaHora = :fechaHora " +
           "AND m.permisoCarnet = :permiso " +
           "AND e.tipo = :tipo")
    long countAlumnosCitados(@Param("fechaHora") LocalDateTime fechaHora,
                            @Param("permiso") TipoCarnet permiso,
                            @Param("tipo") TipoExamen tipo);

    /**
     * Búsqueda transversal de solicitudes de examen con filtros opcionales ordenadas cronológicamente por FIFO.
     */
    @Query("SELECT s FROM SolicitudExamen s " +
           "JOIN s.alumno a " +
           "JOIN s.matricula m " +
           "LEFT JOIN s.profesor p " +
           "WHERE (:terminoBusqueda IS NULL OR :terminoBusqueda = '' " +
           "   OR LOWER(CONCAT(a.nombre, ' ', a.apellidos)) LIKE LOWER(CONCAT('%', :terminoBusqueda, '%')) " +
           "   OR LOWER(a.dni) LIKE LOWER(CONCAT('%', :terminoBusqueda, '%'))) " +
           "AND (:profesorId IS NULL OR (p IS NOT NULL AND p.id = :profesorId)) " +
           "AND (:tipoPermiso IS NULL OR m.permisoCarnet = :tipoPermiso) " +
           "AND (:tipoExamen IS NULL OR s.tipo = :tipoExamen) " +
           "AND (:estado IS NULL OR s.estado = :estado) " +
           "ORDER BY s.fechaHora ASC")
    List<SolicitudExamen> buscarConFiltros(@Param("terminoBusqueda") String terminoBusqueda,
                                          @Param("profesorId") Long profesorId,
                                          @Param("tipoPermiso") TipoCarnet tipoPermiso,
                                          @Param("tipoExamen") TipoExamen tipoExamen,
                                          @Param("estado") EstadoSolicitud estado);
}
