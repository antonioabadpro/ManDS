package com.autoescuela.erp.usuarios.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.autoescuela.erp.core.enums.EstadoUsuario;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.enums.TipoTurno;
import com.autoescuela.erp.usuarios.model.Profesor;

public interface ProfesorRepository extends JpaRepository<Profesor, Long>
{
    /**
     * Recupera todos los profesores ordenados alfabéticamente por nombre y apellidos.
     */
    List<Profesor> findAllByOrderByNombreAscApellidosAsc();

    /**
     * Recupera todos los profesores en un estado determinado ordenados alfabéticamente por nombre y apellidos.
     */
    List<Profesor> findByEstadoOrderByNombreAscApellidosAsc(EstadoUsuario estado);

    /**
     * Recupera todos los profesores en un estado determinado excluyendo a uno en particular por su ID,
     * ordenados alfabéticamente por nombre y apellidos.
     */
    List<Profesor> findByEstadoAndIdNotOrderByNombreAscApellidosAsc(EstadoUsuario estado, Long id);

    /**
     * Recupera los profesores en estado ACTIVO excluyendo a un profesor en particular por su ID,
     * ordenados ascendentemente por número de alumnos asignados (para balanceo de carga en sustituciones)
     * y por nombre y apellidos en caso de empate.
     */
    @Query("""
        SELECT p FROM Profesor p
        WHERE p.estado = com.autoescuela.erp.core.enums.EstadoUsuario.ACTIVO
          AND p.id != :profesorId
        ORDER BY SIZE(p.listaAlumnos) ASC, p.nombre ASC, p.apellidos ASC
    """)
    List<Profesor> findActivosExcluyendoIdOrderByAlumnosAsc(@Param("profesorId") Long profesorId);

    /**
     * Recupera todos los profesores en estado ACTIVO ordenados ascendentemente por número de alumnos asignados
     * y por nombre y apellidos en caso de empate.
     */
    @Query("""
        SELECT p FROM Profesor p
        WHERE p.estado = com.autoescuela.erp.core.enums.EstadoUsuario.ACTIVO
        ORDER BY SIZE(p.listaAlumnos) ASC, p.nombre ASC, p.apellidos ASC
    """)
    List<Profesor> findActivosOrderByAlumnosAsc();

    /**
     * Recupera los profesores en estado ACTIVO que cuenten con el permiso de carnet especificado,
     * ordenados ascendentemente por número de alumnos asignados (para balanceo de carga)
     * y por nombre y apellidos en caso de empate.
     */
    @Query("""
        SELECT p FROM Profesor p
        WHERE p.estado = com.autoescuela.erp.core.enums.EstadoUsuario.ACTIVO
          AND :tipoCarnet MEMBER OF p.listaTiposCarnet
        ORDER BY SIZE(p.listaAlumnos) ASC, p.nombre ASC, p.apellidos ASC
    """)
    List<Profesor> findActivosPorCarnetOrderByAlumnosAsc(@Param("tipoCarnet") TipoCarnet tipoCarnet);

    /**
     * Recupera los profesores en estado ACTIVO que cuenten con el permiso de carnet especificado,
     * excluyendo a un profesor en particular por su ID, ordenados ascendentemente por número de alumnos asignados
     * (para balanceo de carga) y por nombre y apellidos en caso de empate.
     */
    @Query("""
        SELECT p FROM Profesor p
        WHERE p.estado = com.autoescuela.erp.core.enums.EstadoUsuario.ACTIVO
          AND p.id != :profesorId
          AND :tipoCarnet MEMBER OF p.listaTiposCarnet
        ORDER BY SIZE(p.listaAlumnos) ASC, p.nombre ASC, p.apellidos ASC
    """)
    List<Profesor> findActivosPorCarnetExcluyendoIdOrderByAlumnosAsc(
            @Param("tipoCarnet") TipoCarnet tipoCarnet,
            @Param("profesorId") Long profesorId);

    /**
     * Cuenta el número de profesores adscritos a un turno específico.
     */
    long countByTurno(TipoTurno turno);

    /**
     * Cuenta el número de profesores en un estado determinado.
     */
    long countByEstado(EstadoUsuario estado);
    /**
     * Busca un profesor a partir de su Documento Nacional de Identidad o NIE.
     *
     * @param dni DNI o NIE del docente.
     * @return Optional con el Profesor encontrado o vacío si no existe.
     */
    Optional<Profesor> findByDni(String dni);

    /**
     * Busca un profesor por su dirección de correo electrónico.
     *
     * @param correo Dirección de correo electrónico.
     * @return Optional con el Profesor encontrado o vacío si no existe.
     */
    Optional<Profesor> findByCorreo(String correo);

    /**
     * Busca un profesor por su nombre de usuario.
     *
     * @param nombreUsuario Nombre de usuario.
     * @return Optional con el Profesor encontrado o vacío si no existe.
     */
    Optional<Profesor> findByNombreUsuario(String nombreUsuario);
}
