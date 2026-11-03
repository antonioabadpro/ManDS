package com.autoescuela.erp.examenes.repository;

import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.autoescuela.erp.core.enums.TipoExamen;
import com.autoescuela.erp.examenes.model.Examen;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.model.Profesor;

public interface ExamenRepository extends JpaRepository<Examen, Long>
{
    /**
     * Recupera todos los exámenes oficiales de los alumnos del profesor indicado por parametro.
     * @return Devuelve la lista de exámenes oficiales de los alumnos del profesor.
     */
    List<Examen> findByAlumnoProfesorOrderByFechaHoraDesc(Profesor profesor);

    /**
     * Recupera todos los exámenes oficiales del tipo especificado para los alumnos del profesor indicado.
     */
    @EntityGraph(attributePaths = {"alumno"})
    List<Examen> findByAlumnoProfesorAndTipoOrderByFechaHoraDesc(Profesor profesor, TipoExamen tipo);

    /**
     * Recupera todos los exámenes oficiales del tipo especificado a nivel global ordenados por fecha y hora descendente,
     * precargando el alumno para optimizar la vista de calificaciones.
     */
    @EntityGraph(attributePaths = {"alumno"})
    List<Examen> findByTipoOrderByFechaHoraDesc(TipoExamen tipo);

    /**
     * Recupera todos los exámenes asociados a las solicitudes de examen del profesor indicado por parametro.
     * @return Devuelve la lista de exámenes asociados a las solicitudes de examen del profesor.
     */
    List<Examen> findBySolicitudExamenProfesor(Profesor profesor);

    /**
     * Recupera todos los exámenes oficiales realizados por un alumno ordenados por fecha y hora descendente.
     */
    List<Examen> findByAlumnoOrderByFechaHoraDesc(Alumno alumno);

    /**
     * Recupera los últimos 2 exámenes oficiales realizados por un alumno ordenados por fecha y hora descendente.
     */
    List<Examen> findTop2ByAlumnoOrderByFechaHoraDesc(Alumno alumno);

    /**
     * Determina si el alumno ha superado un examen oficial del tipo indicado (esApto = true).
     */
    boolean existsByAlumnoAndTipoAndEsAptoTrue(Alumno alumno, TipoExamen tipo);

    /**
     * Recupera el conjunto de IDs de alumnos que han superado un examen oficial del tipo indicado en una única consulta batch.
     */
    @Query("SELECT DISTINCT e.alumno.id FROM Examen e WHERE e.tipo = :tipo AND e.esApto = true")
    Set<Long> findAlumnoIdsConExamenAprobado(@Param("tipo") TipoExamen tipo);

    /**
     * Contabiliza los exámenes aprobados (esApto = true) de un alumno.
     */
    long countByAlumnoAndEsAptoTrue(Alumno alumno);

    /**
     * Contabiliza los exámenes suspendidos (esApto = false) de un alumno.
     */
    long countByAlumnoAndEsAptoFalse(Alumno alumno);

    /**
     * Contabiliza el total de exámenes realizados por un alumno.
     */
    long countByAlumno(Alumno alumno);
}
