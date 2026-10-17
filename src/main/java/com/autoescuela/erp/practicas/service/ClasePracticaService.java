package com.autoescuela.erp.practicas.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autoescuela.erp.core.enums.EstadoClase;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.practicas.dto.ClasePracticaAdminDTO;
import com.autoescuela.erp.practicas.dto.ReservaClasePracticaDTO;
import com.autoescuela.erp.practicas.mapper.ClasePracticaMapper;
import com.autoescuela.erp.practicas.model.ClasePractica;
import com.autoescuela.erp.practicas.repository.ClasePracticaRepository;
import com.autoescuela.erp.practicas.util.FestivosCalendarioUtil;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.model.Profesor;

import lombok.RequiredArgsConstructor;

/**
 * Servicio del dominio para la gestión del ciclo de vida y reservas de clases prácticas.
 * Aplica control de concurrencia First-Come, First-Served con doble barrera defensiva
 * (lógica atómica en repositorio y captura de colisión en base de datos).
 */
@Service
@RequiredArgsConstructor
public class ClasePracticaService
{
    private final ClasePracticaRepository clasePracticaRepository;
    private final ClasePracticaMapper clasePracticaMapper;

    /**
     * Procesa la reserva transaccional de una clase práctica aplicando el algoritmo First-Come, First-Served.
     *
     * @param alumno   Alumno solicitante
     * @param profesor Profesor asignado al alumno
     * @param dto      Datos de la reserva (fecha, tramo, duración, punto de recogida, observaciones)
     * @return ClasePractica persistida en estado PENDIENTE
     * @throws ReglaNegocioException si la franja horaria ya está ocupada o colisiona concurrentemente
     */
    @Transactional
    public ClasePractica reservarClase(Alumno alumno, Profesor profesor, ReservaClasePracticaDTO dto)
    {
        // Barrera Preventiva de Días No Laborables (Festivos Nacionales y Periodos Vacacionales)
        if (dto.fechaHora() != null && FestivosCalendarioUtil.esDiaNoLaborable(dto.fechaHora().toLocalDate()))
        {
            String motivo = FestivosCalendarioUtil.obtenerMotivoNoLaborable(dto.fechaHora().toLocalDate());
            throw new ReglaNegocioException("No se pueden reservar clases prácticas en días festivos nacionales ni durante vacaciones de la autoescuela (" + motivo + ").");
        }

        // Barrera Preventiva de Jornada Cancelada por el Profesor
        if (dto.fechaHora() != null && profesor != null)
        {
            LocalDateTime inicioDia = dto.fechaHora().toLocalDate().atStartOfDay();
            LocalDateTime finDia = dto.fechaHora().toLocalDate().atTime(23, 59, 59);
            List<ClasePractica> clasesDelDia = this.clasePracticaRepository
                    .findByProfesorAndFechaHoraBetweenOrderByFechaHoraAsc(profesor, inicioDia, finDia);
            if (clasesDelDia != null)
            {
                for (ClasePractica c : clasesDelDia)
                {
                    if (c.getEstadoClase() == EstadoClase.CANCELADA
                            && Boolean.TRUE.equals(c.getIsCanceladaPorProfesor())
                            && c.getObservaciones() != null
                            && c.getObservaciones().contains("Cancelación de jornada"))
                    {
                        throw new ReglaNegocioException("La jornada seleccionada ha sido cancelada por el profesor. No es posible realizar reservas para este día.");
                    }
                }
            }
        }

        // Implementamos la Barrera Lógica: comprobación preventiva de solapamiento
        boolean tramoOcupado = this.clasePracticaRepository.existeClaseActivaOBloqueadaPorProfesor(profesor, dto.fechaHora());
        if (tramoOcupado)
        {
            throw new ReglaNegocioException("El tramo horario seleccionado ya se encuentra ocupado o cancelado con tu profesor. Por favor, selecciona otro horario.");
        }

        // Construimos la entidad
        ClasePractica nuevaClase = new ClasePractica();
        nuevaClase.setAlumno(alumno);
        nuevaClase.setProfesor(profesor);
        nuevaClase.setFechaHora(dto.fechaHora());
        nuevaClase.setDuracion(dto.duracion() != null ? dto.duracion() : 45);
        nuevaClase.setPuntoRecogida(dto.puntoRecogida() != null ? dto.puntoRecogida().trim() : "");
        nuevaClase.setObservaciones(dto.observaciones() != null ? dto.observaciones().trim() : "");
        nuevaClase.setEstadoClase(EstadoClase.PENDIENTE);

        long kmActual = (profesor != null && profesor.getVehiculo() != null && profesor.getVehiculo().getKm() != null) ? profesor.getVehiculo().getKm().longValue() : 0;
        nuevaClase.setKmInicio(kmActual);
        nuevaClase.setKmFin(kmActual);

        // Implementamos la Barrera Física: persistencia atómica con flush forzado y captura de colisiones concurrentes
        try
        {
            return this.clasePracticaRepository.saveAndFlush(nuevaClase);
        }
        catch (DataIntegrityViolationException ex)
        {
            throw new ReglaNegocioException("El tramo horario seleccionado acaba de ser reservado por otro alumno. Por favor, selecciona otro horario libre.", ex);
        }
    }

    /**
     * Procesa la cancelación de una clase práctica por parte del alumno.
     * Valida que la clase pertenezca al alumno, se encuentre en estado PENDIENTE
     * y que no haya comenzado ya (fechaHora > now), liberando la franja horaria.
     *
     * @param clase Clase práctica a cancelar
     * @param alumno Alumno que solicita la cancelación
     * @return ClasePractica persistida en estado CANCELADA
     * @throws ReglaNegocioException si no cumple las precondiciones de estado o límite horario
     */
    @Transactional
    public ClasePractica cancelarClasePorAlumno(ClasePractica clase, Alumno alumno)
    {
        if (clase == null)
        {
            throw new ReglaNegocioException("No se ha encontrado la clase práctica solicitada.");
        }

        if (clase.getAlumno() != null && alumno != null && !clase.getAlumno().getId().equals(alumno.getId()))
        {
            throw new ReglaNegocioException("No tienes permisos para cancelar una clase práctica de otro alumno.");
        }

        if (clase.getEstadoClase() != EstadoClase.PENDIENTE)
        {
            throw new ReglaNegocioException("Solo se pueden cancelar clases que se encuentren en estado PENDIENTE.");
        }

        if (clase.getFechaHora() == null || !clase.getFechaHora().isAfter(LocalDateTime.now()))
        {
            throw new ReglaNegocioException("No es posible cancelar una clase práctica que ya ha comenzado o cuya hora de inicio ha transcurrido.");
        }

        clase.setEstadoClase(EstadoClase.CANCELADA);
        clase.setIsCanceladaPorProfesor(false);
        clase.setIsCancelacionJornada(false);
        clase.setMotivoCancelacion(null);
        return this.clasePracticaRepository.save(clase);
    }

    /**
     * Recupera y proyecta las clases prácticas filtradas por profesor y/o estado
     * para la supervisión del panel del Administrador, ordenadas de más reciente a más antigua.
     *
     * @param profesorId Identificador del profesor (opcional).
     * @param estadoClase Estado de la clase (opcional).
     * @return Lista de DTOs inmutables representativos de las clases encontradas.
     */
    @Transactional(readOnly = true)
    public List<ClasePracticaAdminDTO> obtenerClasesParaAdmin(Long profesorId, EstadoClase estadoClase)
    {
        List<ClasePractica> clases = this.clasePracticaRepository.buscarClasesParaAdmin(profesorId, estadoClase);
        return this.clasePracticaMapper.toClasePracticaAdminDTOList(clases);
    }

    /**
     * Recupera una clase práctica por su identificador único y la proyecta al DTO
     * informativo para su visualización en el modal del Administrador.
     *
     * @param id Identificador de la clase práctica.
     * @return DTO inmutable con los detalles completos de la clase.
     * @throws ReglaNegocioException si la clase no existe en el sistema.
     */
    @Transactional(readOnly = true)
    public ClasePracticaAdminDTO obtenerClaseParaDetalleAdmin(Long id)
    {
        if (id == null)
        {
            throw new ReglaNegocioException("El identificador de la clase práctica no puede ser nulo.");
        }

        ClasePractica clase = this.clasePracticaRepository.findByIdConDetalles(id)
                .orElseThrow(() -> new ReglaNegocioException("No se ha encontrado la clase práctica solicitada."));

        return this.clasePracticaMapper.toClasePracticaAdminDTO(clase);
    }
}
