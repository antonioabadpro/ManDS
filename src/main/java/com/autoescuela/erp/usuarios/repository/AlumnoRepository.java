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
     * Recupera un alumno precargando su profesor, vehículo y matrículas para modales de detalle y expediente.
     */
    @EntityGraph(attributePaths = {"profesor", "profesor.vehiculo", "historialMatriculas"})
    Optional<Alumno> findWithDetallesById(Long id);

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
    @EntityGraph(attributePaths = {"historialMatriculas"})
    List<Alumno> findByProfesor(Profesor profesor);

    /**
     * Recupera todos los alumnos por el identificador del profesor.
     */
    @EntityGraph(attributePaths = {"historialMatriculas"})
    List<Alumno> findByProfesorId(Long profesorId);

    /**
     * Contabiliza los alumnos asignados por profesor en una única consulta batch agrupada.
     */
    @Query("SELECT a.profesor.id, COUNT(a) FROM Alumno a WHERE a.profesor IS NOT NULL GROUP BY a.profesor.id")
    List<Object[]> countAlumnosPorProfesor();

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
