package com.autoescuela.erp.examenes.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.autoescuela.erp.core.enums.EstadoSolicitud;
import com.autoescuela.erp.examenes.model.SolicitudExamen;
import com.autoescuela.erp.usuarios.model.Profesor;

public interface SolicitudExamenRepository extends JpaRepository<SolicitudExamen, Long>
{
    /**
     * Recupera las solicitudes de examen asociadas a un profesor tutor.
     */
    List<SolicitudExamen> findByProfesor(Profesor profesor);

    /**
     * Recupera las solicitudes de examen asociadas a un profesor con un estado específico.
     */
    List<SolicitudExamen> findByProfesorAndEstado(Profesor profesor, EstadoSolicitud estado);
}
