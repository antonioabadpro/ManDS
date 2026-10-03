package com.autoescuela.erp.usuarios.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autoescuela.erp.academico.model.Matricula;
import com.autoescuela.erp.academico.repository.MatriculaRepository;
import com.autoescuela.erp.core.enums.EstadoClase;
import com.autoescuela.erp.core.excepciones.RecursoNoEncontradoException;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.practicas.repository.ClasePracticaRepository;
import com.autoescuela.erp.usuarios.dto.AlumnoDetalleDTO;
import com.autoescuela.erp.usuarios.dto.AlumnoResumenDTO;
import com.autoescuela.erp.usuarios.mapper.AlumnoMapper;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.repository.AlumnoRepository;

import lombok.RequiredArgsConstructor;

/**
 * Servicio para la gestión operativa y académica de alumnos.
 */
@Service
@RequiredArgsConstructor
public class AlumnoService
{
    private final AlumnoRepository alumnoRepository;
    private final MatriculaRepository matriculaRepository;
    private final ClasePracticaRepository clasePracticaRepository;
    private final AlumnoMapper alumnoMapper;

    /**
     * Recupera todos los alumnos registrados en el sistema ordenados alfabéticamente
     * y mapeados a su DTO de resumen para la tabla del panel de administración.
     *
     * @return Lista inmutable de AlumnoResumenDTO.
     */
    @Transactional(readOnly = true)
    public List<AlumnoResumenDTO> obtenerTodosLosAlumnos()
    {
        List<Alumno> listaAlumnos = this.alumnoRepository.findAllByOrderByNombreAscApellidosAsc();
        List<AlumnoResumenDTO> listaAlumnoResumen = new ArrayList<>();

        for (Alumno alumno : listaAlumnos)
        {
            Matricula matriculaActiva = this.matriculaRepository
                    .findByAlumnoAndEstaActivaTrue(alumno)
                    .orElse(null);
            int clasesPendientes = this.clasePracticaRepository
                    .countByAlumnoAndEstadoClase(alumno, EstadoClase.PENDIENTE);

            listaAlumnoResumen.add(this.alumnoMapper.toAlumnoResumenDTO(alumno, matriculaActiva, clasesPendientes));
        }

        return listaAlumnoResumen;
    }

    /**
     * Obtiene la totalidad de los datos informativos del alumno para su visualización
     * en el modal de detalle del panel de administración.
     *
     * @param id Identificador único del alumno.
     * @return DTO inmutable poblado con datos personales, vías de contacto, expediente y flota.
     */
    @Transactional(readOnly = true)
    public AlumnoDetalleDTO obtenerAlumnoParaDetalle(Long id)
    {
        if (id == null)
        {
            throw new ReglaNegocioException("El identificador del alumno no puede ser nulo.");
        }
        Alumno alumno = this.alumnoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el alumno con ID: " + id));

        Matricula matriculaActiva = this.matriculaRepository
                .findByAlumnoAndEstaActivaTrue(alumno)
                .orElse(null);

        return this.alumnoMapper.toAlumnoDetalleDTO(alumno, matriculaActiva);
    }
}
