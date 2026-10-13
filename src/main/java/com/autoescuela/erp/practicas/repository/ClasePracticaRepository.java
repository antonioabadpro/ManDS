package com.autoescuela.erp.practicas.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.autoescuela.erp.core.enums.EstadoClase;
import com.autoescuela.erp.practicas.model.ClasePractica;
import com.autoescuela.erp.usuarios.model.Alumno;
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
     * Recupera todas las clases prácticas de un profesor en un determinado estado (RECIBIDA, PENDIENTE, CANCELADA).
     */
    List<ClasePractica> findByProfesorAndEstadoClase(Profesor profesor, EstadoClase estadoClase);

    /**
     * Contabiliza las clases de un profesor en un determinado estado (RECIBIDA, PENDIENTE, CANCELADA).
     */
    int countByProfesorAndEstadoClase(Profesor profesor, EstadoClase estadoClase);

    /**
     * Contabiliza el total de clases asociadas a un profesor.
     */
    int countByProfesor(Profesor profesor);

    /**
     * Recupera todas las clases prácticas asociadas a un alumno ordenadas ascendentemente por fecha/hora.
     */
    List<ClasePractica> findByAlumnoOrderByFechaHoraAsc(Alumno alumno);

    /**
     * Recupera todas las clases prácticas asociadas a un alumno ordenadas descendentemente por fecha/hora.
     */
    List<ClasePractica> findByAlumnoOrderByFechaHoraDesc(Alumno alumno);

    /**
     * Recupera las últimas 3 clases prácticas asociadas a un alumno ordenadas descendentemente por fecha/hora.
     */
    List<ClasePractica> findTop3ByAlumnoOrderByFechaHoraDesc(Alumno alumno);

    /**
     * Recupera las clases prácticas de un alumno con un determinado estado.
     */
    List<ClasePractica> findByAlumnoAndEstadoClase(Alumno alumno, EstadoClase estadoClase);

    /**
     * Recupera las clases prácticas de un alumno que no estén en un determinado estado.
     * @param alumno Alumno del que se quieren recuperar las clases prácticas.
     * @param estadoClase Estado de clase que se quiere excluir de la búsqueda.
     * @return Lista de clases prácticas del alumno que no estén en el estado especificado.
     */
    List<ClasePractica> findByAlumnoAndEstadoClaseNot(Alumno alumno, EstadoClase estadoClase);

    /**
     * Contabiliza las clases de un alumno en un determinado estado (RECIBIDA, PENDIENTE, CANCELADA).
     */
    int countByAlumnoAndEstadoClase(Alumno alumno, EstadoClase estadoClase);

    /**
     * Contabiliza el total de clases asociadas a un alumno.
     */
    int countByAlumno(Alumno alumno);

    /**
     * Comprueba si el profesor ya tiene una clase programada en una fecha y hora concreta que no esté cancelada.
     */
    boolean existsByProfesorAndFechaHoraAndEstadoClaseNot(Profesor profesor, LocalDateTime fechaHora, EstadoClase estadoClase);

    /**
     * Búsqueda filtrada de clases prácticas para el panel del Administrador,
     * optimizada con FETCH JOIN para evitar problemas de N+1 y ordenadas descendentemente por fecha/hora.
     */
    @Query("""
        SELECT c FROM ClasePractica c
        JOIN FETCH c.alumno a
        LEFT JOIN FETCH c.profesor p
        LEFT JOIN FETCH p.vehiculo v
        WHERE (:profesorId IS NULL OR p.id = :profesorId)
          AND (:estadoClase IS NULL OR c.estadoClase = :estadoClase)
        ORDER BY c.fechaHora DESC
    """)
    List<ClasePractica> buscarClasesParaAdmin(@Param("profesorId") Long profesorId, @Param("estadoClase") EstadoClase estadoClase);

    /**
     * Recupera una clase práctica por su identificador único junto con sus relaciones
     * (alumno, profesor y vehículo) precargadas para la visualización del modal informativo.
     */
    @Query("""
        SELECT c FROM ClasePractica c
        JOIN FETCH c.alumno a
        LEFT JOIN FETCH c.profesor p
        LEFT JOIN FETCH p.vehiculo v
        WHERE c.id = :id
    """)
    Optional<ClasePractica> findByIdConDetalles(@Param("id") Long id);
}
