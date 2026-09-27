package com.autoescuela.erp.practicas.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.autoescuela.erp.core.enums.EstadoClase;
import com.autoescuela.erp.practicas.model.ClasePractica;
import com.autoescuela.erp.usuarios.model.Profesor;

public interface ClasePracticaRepository extends JpaRepository<ClasePractica, Long>
{
    /**
     * Recupera todas las clases prácticas asociadas a un profesor ordenadas ascendentemente (de más reciente a más antigua).
     */
    List<ClasePractica> findByProfesorOrderByFechaHoraAsc(Profesor profesor);

    /**
     * Recupera las clases de un profesor por su identificador único ordenadas ascendentemente (de más reciente a más antigua).
     */
    List<ClasePractica> findByProfesorIdOrderByFechaHoraAsc(Long profesorId);

    /**
     * Recupera las clases prácticas de un profesor dentro de una ventana temporal concreta ordenadas ascendentemente (de más reciente a más antigua).
     */
    List<ClasePractica> findByProfesorAndFechaHoraBetweenOrderByFechaHoraAsc(Profesor profesor, LocalDateTime inicio, LocalDateTime fin);

    /**
     * Contabiliza las clases de un profesor en un determinado estado (RECIBIDA, PENDIENTE, CANCELADA).
     */
    long countByProfesorAndEstadoClase(Profesor profesor, EstadoClase estadoClase);

    /**
     * Contabiliza el total de clases asociadas a un profesor.
     */
    long countByProfesor(Profesor profesor);
}
