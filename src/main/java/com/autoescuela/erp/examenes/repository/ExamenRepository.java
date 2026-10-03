package com.autoescuela.erp.examenes.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
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
