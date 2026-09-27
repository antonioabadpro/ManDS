package com.autoescuela.erp.usuarios.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
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
     * Recupera todos los alumnos tutelados por un profesor determinado.
     */
    List<Alumno> findByProfesor(Profesor profesor);

    /**
     * Recupera todos los alumnos tutelados por el identificador del profesor.
     */
    List<Alumno> findByProfesorId(Long profesorId);
}
