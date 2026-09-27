package com.autoescuela.erp.academico.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.autoescuela.erp.academico.model.Matricula;
import com.autoescuela.erp.usuarios.model.Alumno;

public interface MatriculaRepository extends JpaRepository<Matricula, Long>
{
    /**
     * Busca la matrícula activa vigente de un alumno.
     */
    Optional<Matricula> findByAlumnoAndEstaActivaTrue(Alumno alumno);

    /**
     * Recupera el histórico completo de matrículas de un alumno.
     */
    List<Matricula> findByAlumnoOrderByFechaMatriculacionDesc(Alumno alumno);
}
