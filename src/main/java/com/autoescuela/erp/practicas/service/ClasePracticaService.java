package com.autoescuela.erp.practicas.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autoescuela.erp.academico.model.Matricula;
import com.autoescuela.erp.academico.repository.MatriculaRepository;
import com.autoescuela.erp.core.email.EmailService;
import com.autoescuela.erp.core.enums.EstadoClase;
import com.autoescuela.erp.core.enums.ModalidadMatricula;
import com.autoescuela.erp.core.enums.TipoExamen;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.examenes.repository.ExamenRepository;
import com.autoescuela.erp.flota.model.Vehiculo;
import com.autoescuela.erp.flota.repository.VehiculoRepository;
import com.autoescuela.erp.practicas.dto.ClasePracticaAdminDTO;
import com.autoescuela.erp.practicas.dto.CompletarClaseDTO;
import com.autoescuela.erp.practicas.dto.ReservaClasePracticaDTO;
import com.autoescuela.erp.practicas.dto.TramoHorarioDTO;
import com.autoescuela.erp.practicas.mapper.ClasePracticaMapper;
import com.autoescuela.erp.practicas.model.ClasePractica;
import com.autoescuela.erp.practicas.repository.ClasePracticaRepository;
import com.autoescuela.erp.practicas.util.FestivosCalendarioUtil;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.model.Profesor;
import com.autoescuela.erp.usuarios.repository.AlumnoRepository;

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
    private final MatriculaRepository matriculaRepository;
    private final ExamenRepository examenRepository;
    private final VehiculoRepository vehiculoRepository;
    private final AlumnoRepository alumnoRepository;
    private final CalendarioService calendarioService;
    private final EmailService emailService;

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

        // Barrera Preventiva de Convocatorias y Aprobado Teórico Oficial
        if (alumno != null)
        {
            Matricula matricula = this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno).orElse(null);
            if (matricula == null)
            {
                throw new ReglaNegocioException("El alumno no dispone de un expediente de matrícula activo para reservar clases prácticas.");
            }
            if (matricula.tieneConvocatoriasAgotadas())
            {
                throw new ReglaNegocioException("Has agotado las convocatorias oficiales de tu matrícula. Debes tramitar la renovación antes de reservar clases prácticas.");
            }
            if (matricula.getModalidad() == ModalidadMatricula.TEORICO_PRACTICA)
            {
                boolean teoricoAprobado = this.examenRepository.existsByAlumnoAndTipoAndEsAptoTrue(alumno, TipoExamen.TEORICO);
                if (!teoricoAprobado)
                {
                    throw new ReglaNegocioException("Es obligatorio haber aprobado el examen teórico oficial ante la DGT antes de reservar clases prácticas.");
                }
            }
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

    /**
     * Busca una clase práctica por su identificador único.
     */
    @Transactional(readOnly = true)
    public Optional<ClasePractica> buscarPorId(Long id)
    {
        if (id == null)
        {
            return Optional.empty();
        }
        return this.clasePracticaRepository.findById(id);
    }

    /**
     * Cuenta el total de clases en estado PENDIENTE de un alumno.
     */
    @Transactional(readOnly = true)
    public int contarClasesPendientesPorAlumno(Alumno alumno)
    {
        if (alumno == null)
        {
            return 0;
        }
        return this.clasePracticaRepository.countByAlumnoAndEstadoClase(alumno, EstadoClase.PENDIENTE);
    }

    /**
     * Cuenta el total de clases en estado RECIBIDA de un alumno.
     */
    @Transactional(readOnly = true)
    public long contarClasesRecibidasPorAlumno(Alumno alumno)
    {
        if (alumno == null)
        {
            return 0L;
        }
        return this.clasePracticaRepository.countByAlumnoAndEstadoClase(alumno, EstadoClase.RECIBIDA);
    }

    /**
     * Recupera todas las clases de un alumno ordenadas ascendentemente (de más antigua a más nueva).
     */
    @Transactional(readOnly = true)
    public List<ClasePractica> obtenerClasesPorAlumnoAsc(Alumno alumno)
    {
        if (alumno == null)
        {
            return List.of();
        }
        return this.clasePracticaRepository.findByAlumnoOrderByFechaHoraAsc(alumno);
    }

    /**
     * Recupera todas las clases de un alumno ordenadas descendentemente (de más nueva a más antigua).
     */
    @Transactional(readOnly = true)
    public List<ClasePractica> obtenerClasesPorAlumnoDesc(Alumno alumno)
    {
        if (alumno == null)
        {
            return List.of();
        }
        return this.clasePracticaRepository.findByAlumnoOrderByFechaHoraDesc(alumno);
    }

    /**
     * Recupera las clases de un alumno excluyendo las que coincidan con un determinado estado.
     */
    @Transactional(readOnly = true)
    public List<ClasePractica> obtenerClasesPorAlumnoExcluyendoEstado(Alumno alumno, EstadoClase estado)
    {
        if (alumno == null)
        {
            return List.of();
        }
        return this.clasePracticaRepository.findByAlumnoAndEstadoClaseNot(alumno, estado);
    }

    /**
     * Recupera todas las clases de un profesor ordenadas ascendentemente (de más antigua a más nueva).
     */
    @Transactional(readOnly = true)
    public List<ClasePractica> obtenerClasesPorProfesorAsc(Profesor profesor)
    {
        if (profesor == null)
        {
            return List.of();
        }
        return this.clasePracticaRepository.findByProfesorOrderByFechaHoraAsc(profesor);
    }

    /**
     * Recupera las clases de un profesor dentro de una franja temporal delimitada.
     */
    @Transactional(readOnly = true)
    public List<ClasePractica> obtenerClasesPorProfesorYRango(Profesor profesor, LocalDateTime inicio, LocalDateTime fin)
    {
        if (profesor == null)
        {
            return List.of();
        }
        return this.clasePracticaRepository.findByProfesorAndFechaHoraBetweenOrderByFechaHoraAsc(profesor, inicio, fin);
    }

    /**
     * Persiste o actualiza una clase práctica en el sistema.
     */
    @Transactional
    public ClasePractica guardar(ClasePractica clase)
    {
        return this.clasePracticaRepository.save(clase);
    }

    /**
     * Registra la ficha técnica de una clase práctica impartida.
     * Pasa la clase a RECIBIDA, actualiza odómetro de vehículo y deduce 1 unidad de saldo en matrícula.
     *
     * @param dto Datos del kilometraje y observaciones.
     * @return ClasePractica actualizada en estado RECIBIDA.
     */
    @Transactional
    public ClasePractica completarClasePractica(CompletarClaseDTO dto)
    {
        if (dto == null || dto.claseId() == null)
        {
            throw new ReglaNegocioException("El identificador de la clase es obligatorio.");
        }

        ClasePractica clase = this.clasePracticaRepository.findById(dto.claseId())
                .orElseThrow(() -> new ReglaNegocioException("No se encontró la clase práctica solicitada."));

        if (dto.kmFin() != null && dto.kmInicio() != null && dto.kmFin() <= dto.kmInicio())
        {
            throw new ReglaNegocioException("El kilometraje final debe ser estrictamente superior al kilometraje inicial.");
        }

        clase.setKmInicio(dto.kmInicio());
        clase.setKmFin(dto.kmFin());
        clase.setObservaciones(dto.observaciones());
        clase.setEstadoClase(EstadoClase.RECIBIDA);
        ClasePractica claseGuardada = this.clasePracticaRepository.save(clase);

        // Actualizamos odómetro del vehículo automáticamente con el kilometraje final
        Profesor profesor = clase.getProfesor();
        if (profesor != null && profesor.getVehiculo() != null && dto.kmFin() != null)
        {
            Vehiculo vehiculo = profesor.getVehiculo();
            vehiculo.setKm(dto.kmFin());
            this.vehiculoRepository.save(vehiculo);
        }

        // Deducción atómica de 1 unidad de saldo en la matrícula activa del alumno
        Alumno alumno = clase.getAlumno();
        if (alumno != null)
        {
            Optional<Matricula> matriculaOptional = this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno);
            if (matriculaOptional.isPresent())
            {
                Matricula matricula = matriculaOptional.get();
                if (matricula.getSaldoClases() != null && matricula.getSaldoClases() > 0)
                {
                    matricula.setSaldoClases(matricula.getSaldoClases() - 1);
                    this.matriculaRepository.save(matricula);
                }
            }
        }

        return claseGuardada;
    }

    /**
     * Cancela una clase individual por imprevisto del profesor, notificando al alumno por correo.
     *
     * @param claseId Identificador de la clase a cancelar.
     * @param motivo Justificación o motivo de la cancelación.
     * @return ClasePractica cancelada.
     */
    @Transactional
    public ClasePractica cancelarClasePorProfesor(Long claseId, String motivo)
    {
        if (claseId == null)
        {
            throw new ReglaNegocioException("El identificador de la clase a cancelar es obligatorio.");
        }

        ClasePractica clase = this.clasePracticaRepository.findById(claseId)
                .orElseThrow(() -> new ReglaNegocioException("No se encontró la clase práctica a cancelar."));

        clase.setEstadoClase(EstadoClase.CANCELADA);
        clase.setIsCanceladaPorProfesor(true);
        clase.setIsCancelacionJornada(false);
        if (motivo != null && !motivo.isBlank())
        {
            clase.setMotivoCancelacion(motivo.trim());
        }
        ClasePractica claseCancelada = this.clasePracticaRepository.save(clase);

        // Notificación al alumno por email
        Alumno alumno = clase.getAlumno();
        if (alumno != null && alumno.getCorreo() != null && !alumno.getCorreo().isBlank())
        {
            try
            {
                String correoAlumno = alumno.getCorreo();
                String asunto = "Cancelación de clase práctica programada";
                String mensaje = "Estimado/a " + alumno.getNombre() + ",\n\nTu clase práctica del "
                        + clase.getFechaHora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + " ha sido cancelada por el profesor. Motivo: "
                        + (motivo != null && !motivo.isBlank() ? motivo.trim() : "Imprevisto del Profesor") + ".\n\nTu saldo de clases permanece intacto para que reserves una nueva fecha.";

                this.emailService.enviarNotificacionAlumno(correoAlumno, asunto, mensaje);

            }
            catch (Exception ex)
            {
                // Fallback silencioso ante servidor SMTP apagado
            }
        }

        return claseCancelada;
    }

    /**
     * Cancela la jornada completa de un profesor para una fecha concreta,
     * cancelando todas las clases pendientes, bloqueando las franjas restantes y notificando a sus alumnos.
     *
     * @param profesor Profesor titular de la jornada.
     * @param fecha Fecha laboral a cancelar.
     * @param motivo Justificación de la cancelación.
     * @return Total de clases prácticas canceladas.
     */
    @Transactional
    public int cancelarJornadaProfesor(Profesor profesor, LocalDate fecha, String motivo)
    {
        if (profesor == null || fecha == null)
        {
            throw new ReglaNegocioException("El profesor y la fecha son obligatorios para cancelar la jornada.");
        }

        LocalDateTime inicioDia = fecha.atStartOfDay();
        LocalDateTime finDia = fecha.atTime(23, 59, 59);

        List<ClasePractica> clasesJornada = this.clasePracticaRepository.findByProfesorAndFechaHoraBetweenOrderByFechaHoraAsc(profesor, inicioDia, finDia);

        boolean yaCanceladaPreviamente = false;
        boolean hayPendientes = false;
        for (ClasePractica c : clasesJornada)
        {
            if (c.getEstadoClase() == EstadoClase.PENDIENTE)
            {
                hayPendientes = true;
            }
            else if (c.getEstadoClase() == EstadoClase.CANCELADA && Boolean.TRUE.equals(c.getIsCancelacionJornada()))
            {
                yaCanceladaPreviamente = true;
            }
        }

        if (yaCanceladaPreviamente && !hayPendientes)
        {
            throw new ReglaNegocioException("Esta jornada ya ha sido cancelada previamente por el profesor.");
        }

        int totalCanceladas = 0;

        for (ClasePractica clase : clasesJornada)
        {
            if (clase.getEstadoClase() == EstadoClase.PENDIENTE)
            {
                clase.setEstadoClase(EstadoClase.CANCELADA);
                clase.setIsCanceladaPorProfesor(true);
                clase.setIsCancelacionJornada(true);
                clase.setMotivoCancelacion(motivo != null ? motivo.trim() : "Cancelación de jornada");
                this.clasePracticaRepository.save(clase);
                totalCanceladas++;
            }
        }

        // Generar bloqueos en los tramos horarios restantes del turno que estuvieran libres
        List<TramoHorarioDTO> tramos = this.calendarioService.generarTramos(profesor.getTurno());
        Long kmActual = (profesor.getVehiculo() != null && profesor.getVehiculo().getKm() != null)
                ? profesor.getVehiculo().getKm()
                : 0L;

        for (TramoHorarioDTO tramo : tramos)
        {
            LocalDateTime slotFechaHora = fecha.atTime(tramo.horaInicio());
            boolean existeClaseEnSlot = false;
            for (ClasePractica c : clasesJornada)
            {
                if (c.getFechaHora() != null && c.getFechaHora().toLocalTime().equals(tramo.horaInicio()))
                {
                    existeClaseEnSlot = true;
                    break;
                }
            }

            if (!existeClaseEnSlot)
            {
                ClasePractica claseCancelada = new ClasePractica();
                claseCancelada.setProfesor(profesor);
                claseCancelada.setAlumno(null);
                claseCancelada.setFechaHora(slotFechaHora);
                claseCancelada.setDuracion(45);
                claseCancelada.setPuntoRecogida("Autoescuela");
                claseCancelada.setKmInicio(kmActual);
                claseCancelada.setKmFin(kmActual);
                claseCancelada.setEstadoClase(EstadoClase.CANCELADA);
                claseCancelada.setIsCanceladaPorProfesor(true);
                claseCancelada.setIsCancelacionJornada(true);
                claseCancelada.setMotivoCancelacion(motivo != null ? motivo.trim() : "Cancelación de jornada");
                this.clasePracticaRepository.save(claseCancelada);
            }
        }

        // Notificación por correo a todos los alumnos asignados al profesor
        List<Alumno> alumnosDelProfesor = this.alumnoRepository.findByProfesor(profesor);
        String fechaFormatoLegible = fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        for (Alumno alumno : alumnosDelProfesor)
        {
            if (alumno.getCorreo() != null && !alumno.getCorreo().isBlank())
            {
                try
                {
                    String correoAlumno = alumno.getCorreo();
                    String asunto = "Cancelación de clases prácticas - Jornada del " + fechaFormatoLegible;
                    String mensaje = "Estimado/a " + alumno.getNombre() + ",\n\nTe informamos de que todas las clases prácticas programadas con tu profesor para el día " + fechaFormatoLegible + " han sido canceladas.\n\nMotivo de la cancelación: " + (motivo != null ? motivo.trim() : "Imprevisto") + ".\n\nSi disponías de una reserva para dicho día, tu saldo de clases permanece intacto para que reserves una nueva fecha.\n\nDisculpa las molestias.";

                    this.emailService.enviarNotificacionAlumno(correoAlumno, asunto, mensaje);
                }
                catch (Exception ex)
                {
                    // Fallback silencioso ante servidor SMTP apagado
                }
            }
        }

        return totalCanceladas;
    }
}
