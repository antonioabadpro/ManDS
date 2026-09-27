package com.autoescuela.erp.examenes.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.autoescuela.erp.examenes.model.Examen;
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
}
