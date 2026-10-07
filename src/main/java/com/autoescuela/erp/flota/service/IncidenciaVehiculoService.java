package com.autoescuela.erp.flota.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autoescuela.erp.core.email.EmailService;
import com.autoescuela.erp.core.enums.EstadoClase;
import com.autoescuela.erp.core.enums.EstadoIncidencia;
import com.autoescuela.erp.core.enums.EstadoVehiculo;
import com.autoescuela.erp.core.excepciones.RecursoNoEncontradoException;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.flota.dto.IncidenciaDetalleDTO;
import com.autoescuela.erp.flota.dto.IncidenciaResumenDTO;
import com.autoescuela.erp.flota.mapper.IncidenciaVehiculoMapper;
import com.autoescuela.erp.flota.model.IncidenciaVehiculo;
import com.autoescuela.erp.flota.model.Vehiculo;
import com.autoescuela.erp.flota.repository.IncidenciaVehiculoRepository;
import com.autoescuela.erp.flota.repository.VehiculoRepository;
import com.autoescuela.erp.practicas.model.ClasePractica;
import com.autoescuela.erp.practicas.repository.ClasePracticaRepository;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.model.Profesor;
import com.autoescuela.erp.usuarios.repository.AlumnoRepository;

import lombok.RequiredArgsConstructor;

/**
 * Servicio de dominio responsable de la gestión integral del ciclo de vida de incidencias de flota
 * y de las transiciones operativas a estado MANTENIMIENTO, cancelaciones de clases y notificaciones.
 */
@Service
@RequiredArgsConstructor
public class IncidenciaVehiculoService
{
    private final IncidenciaVehiculoRepository incidenciaVehiculoRepository;
    private final VehiculoRepository vehiculoRepository;
    private final ClasePracticaRepository clasePracticaRepository;
    private final AlumnoRepository alumnoRepository;
    private final EmailService emailService;
    private final IncidenciaVehiculoMapper incidenciaVehiculoMapper;

    /**
     * Recupera todas las incidencias de flota ordenadas por estado (PENDIENTE, EN_PROCESO, RESUELTA)
     * y de forma secundaria por fecha/hora ascendente.
     */
    @Transactional(readOnly = true)
    public List<IncidenciaResumenDTO> obtenerTodasLasIncidencias()
    {
        return this.incidenciaVehiculoRepository.findAllOrdenadasPorEstadoYFechaAsc()
                .stream()
                .map(this.incidenciaVehiculoMapper::toIncidenciaResumenDTO)
                .toList();
    }

    /**
     * Recupera el detalle completo de una incidencia para su visualización en modal informativo.
     */
    @Transactional(readOnly = true)
    public IncidenciaDetalleDTO obtenerIncidenciaParaDetalle(Long id)
    {
        if (id == null)
        {
            throw new ReglaNegocioException("El identificador de la incidencia no puede ser nulo.");
        }

        return this.incidenciaVehiculoRepository.findById(id)
                .map(this.incidenciaVehiculoMapper::toIncidenciaDetalleDTO)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la incidencia mecánica con ID: " + id));
    }

    /**
     * Contabiliza el número total de incidencias en un estado específico.
     */
    @Transactional(readOnly = true)
    public long contarPorEstado(EstadoIncidencia estado)
    {
        if (estado == null)
        {
            return 0;
        }
        return this.incidenciaVehiculoRepository.countByEstado(estado);
    }

    /**
     * Gestiona el cambio de estado de una incidencia pendiente a EN_PROCESO o RESUELTA,
     * aplicando el control de transiciones de flota, cancelación atómica de clases y notificaciones transaccionales.
     * Implementa un switch exhaustivo para la resolución de cada estado.
     *
     * @param id Identificador de la incidencia.
     * @param nuevoEstado Estado de destino solicitado (EN_PROCESO o RESUELTA).
     * @return Entidad IncidenciaVehiculo actualizada.
     */
    @Transactional
    public IncidenciaVehiculo gestionarIncidencia(Long id, EstadoIncidencia nuevoEstado)
    {
        if (id == null)
        {
            throw new ReglaNegocioException("El identificador de la incidencia es obligatorio.");
        }

        if (nuevoEstado == null)
        {
            throw new ReglaNegocioException("El nuevo estado de la incidencia es obligatorio.");
        }

        IncidenciaVehiculo incidencia = this.incidenciaVehiculoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la incidencia mecánica con ID: " + id));

        if (incidencia.getEstado() == EstadoIncidencia.RESUELTA)
        {
            throw new ReglaNegocioException("No se puede gestionar una incidencia que ya ha sido resuelta.");
        }

        switch (nuevoEstado)
        {
            case PENDIENTE -> throw new ReglaNegocioException("Una incidencia en gestión NO puede volver a establecerse en estado PENDIENTE.");
            case EN_PROCESO -> {
                Vehiculo vehiculo = incidencia.getVehiculo();
                vehiculo.setEstado(EstadoVehiculo.MANTENIMIENTO);
                this.vehiculoRepository.save(vehiculo);

                Profesor profesor = vehiculo.getProfesor();
                if (profesor != null)
                {
                    List<ClasePractica> clasesPendientes = this.clasePracticaRepository.findByProfesorAndEstadoClase(profesor, EstadoClase.PENDIENTE);
                    for (ClasePractica clase : clasesPendientes)
                    {
                        clase.setEstadoClase(EstadoClase.CANCELADA);
                        this.clasePracticaRepository.save(clase);
                    }

                    String asuntoProfesor = "Aviso urgente: Vehículo en mantenimiento - Clases canceladas";
                    String mensajeProfesor = "Estimado/a " + profesor.getNombre() + ",\n\n"
                            + "Te comunicamos que tu vehículo asignado " + vehiculo.getMarca() + " " + vehiculo.getModelo()
                            + " (" + vehiculo.getMatricula() + ") ha entrado en mantenimiento debido a la incidencia reportada.\n"
                            + "Conforme al protocolo de la autoescuela, todas tus clases prácticas pendientes han sido canceladas automáticamente.\n\n"
                            + "Te avisaremos por correo en cuanto el vehículo esté reparado y disponible de nuevo.\n\n"
                            + "Atentamente,\n"
                            + "Equipo de Coordinación - ManDS Autoescuela";
                    this.emailService.enviarNotificacionProfesor(profesor.getCorreo(), asuntoProfesor, mensajeProfesor);

                    List<Alumno> alumnosAsignados = this.alumnoRepository.findByProfesor(profesor);
                    for (Alumno alumno : alumnosAsignados)
                    {
                        String asuntoAlumno = "Aviso importante: Vehículo en mantenimiento - Clases prácticas suspendidas";
                        String mensajeAlumno = "Estimado/a " + alumno.getNombre() + ",\n\n"
                                + "Te comunicamos que el vehículo de prácticas de tu profesor ha entrado en mantenimiento debido a una incidencia.\n"
                                + "Tus clases prácticas pendientes han sido canceladas automáticamente y la reserva de nuevas sesiones queda temporalmente suspendida hasta nuevo aviso.\n\n"
                                + "Te avisaremos por correo en cuanto el vehículo esté reparado y disponible de nuevo para que puedas volver a reservar clases prácticas.\n\n"
                                + "Disculpa las molestias,\n"
                                + "Atentamente,\n"
                                + "Equipo de Coordinación - ManDS Autoescuela";
                        this.emailService.enviarNotificacionAlumno(alumno.getCorreo(), asuntoAlumno, mensajeAlumno);
                    }
                }
            }
            case RESUELTA -> {
                Vehiculo vehiculo = incidencia.getVehiculo();
                EstadoVehiculo estadoRestituido = (vehiculo.getProfesor() != null) ? EstadoVehiculo.OCUPADO : EstadoVehiculo.DISPONIBLE;
                vehiculo.setEstado(estadoRestituido);
                this.vehiculoRepository.save(vehiculo);

                Profesor profesor = vehiculo.getProfesor();
                if (profesor != null)
                {
                    String asuntoProfesor = "Vehículo reparado - Reapertura de clases prácticas";
                    String mensajeProfesor = "Estimado/a " + profesor.getNombre() + ",\n\n"
                            + "Te informamos de que la incidencia mecánica de tu vehículo asignado " + vehiculo.getMarca() + " " + vehiculo.getModelo()
                            + " (" + vehiculo.getMatricula() + ") ha sido resuelta satisfactoriamente.\n"
                            + "El vehículo vuelve a estar plenamente operativo para impartir clases prácticas.\n\n"
                            + "Atentamente,\n"
                            + "Equipo de Coordinación - ManDS Autoescuela";
                    this.emailService.enviarNotificacionProfesor(profesor.getCorreo(), asuntoProfesor, mensajeProfesor);

                    List<Alumno> alumnosAsignados = this.alumnoRepository.findByProfesor(profesor);
                    for (Alumno alumno : alumnosAsignados)
                    {
                        String asuntoAlumno = "Aviso: Vehículo reparado - Reapertura de reservas de clases prácticas";
                        String mensajeAlumno = "Estimado/a " + alumno.getNombre() + ",\n\n"
                                + "Te informamos de que el vehículo de tu profesor ya ha sido reparado y se encuentra plenamente operativo.\n"
                                + "Ya puedes acceder a tu calendario en el portal del alumno para reservar tus próximas clases prácticas.\n\n"
                                + "Un cordial saludo,\n"
                                + "Equipo de Coordinación - ManDS Autoescuela";
                        this.emailService.enviarNotificacionAlumno(alumno.getCorreo(), asuntoAlumno, mensajeAlumno);
                    }
                }
            }
            default -> throw new ReglaNegocioException("Estado de incidencia no reconocido o no admitido para la gestión.");
        }

        incidencia.setEstado(nuevoEstado);

        return this.incidenciaVehiculoRepository.save(incidencia);
    }
}

