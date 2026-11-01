package com.autoescuela.erp.usuarios.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.autoescuela.erp.core.enums.EstadoUsuario;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.model.Profesor;

public interface AlumnoRepository extends JpaRepository<Alumno, Long>
{
    /**
     * Busca un alumno a partir de su Documento Nacional de Identidad o NIE.
     *
     * @param dni Documento de identidad del alumno.
     * @return Optional con el Alumno encontrado o vacío si no existe.
     */
    Optional<Alumno> findByDni(String dni);

    /**
     * Busca un alumno a partir de su nombre de usuario.
     *
     * @param nombreUsuario Nombre de usuario único del alumno.
     * @return Optional con el Alumno encontrado o vacío si no existe.
     */
    Optional<Alumno> findByNombreUsuario(String nombreUsuario);

    /**
     * Busca un alumno a partir de su dirección de correo electrónico.
     *
     * @param correo Correo electrónico del alumno.
     * @return Optional con el Alumno encontrado o vacío si no existe.
     */
    Optional<Alumno> findByCorreo(String correo);

    /**
     * Busca un alumno a partir de su número de teléfono.
     *
     * @param telefono Número de teléfono del alumno.
     * @return Optional con el Alumno encontrado o vacío si no existe.
     */
    Optional<Alumno> findByTelefono(String telefono);

    /**
     * Recupera todos los alumnos huérfanos registrados en estado INACTIVO
     * que carecen de matrículas asociadas en su historial formativo.
     *
     * @return Lista de alumnos inactivos sin matrículas.
     */
    @Query("SELECT a FROM Alumno a WHERE a.estado = com.autoescuela.erp.core.enums.EstadoUsuario.INACTIVO AND a.historialMatriculas IS EMPTY")
    List<Alumno> findAlumnosInactivosSinMatricula();

    /**
     * Recupera todos los alumnos asignados a un profesor determinado.
     */
    List<Alumno> findByProfesor(Profesor profesor);

    /**
     * Recupera todos los alumnos tutelados por el identificador del profesor.
     */
    List<Alumno> findByProfesorId(Long profesorId);

    /**
     * Cuenta el número de alumnos activos en el sistema
     * @return Número de alumnos activos.
     */
    long countByEstado(EstadoUsuario estado);

    /**
     * Recupera todos los alumnos ordenados alfabéticamente cargando en una sola consulta
     * su profesor asignado y sus matrículas para evitar consultas N+1.
     */
    @EntityGraph(attributePaths = {"profesor", "historialMatriculas"})
    List<Alumno> findAllByOrderByNombreAscApellidosAsc();
}
