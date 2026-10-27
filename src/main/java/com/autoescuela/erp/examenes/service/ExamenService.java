package com.autoescuela.erp.examenes.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autoescuela.erp.academico.model.Matricula;
import com.autoescuela.erp.academico.repository.MatriculaRepository;
import com.autoescuela.erp.core.email.EmailService;
import com.autoescuela.erp.core.enums.EstadoClase;
import com.autoescuela.erp.core.enums.EstadoSolicitud;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.enums.TipoExamen;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.examenes.dto.CalificarExamenDTO;
import com.autoescuela.erp.examenes.dto.CupoExamenDTO;
import com.autoescuela.erp.examenes.dto.DisponibilidadConvocatoriaDTO;
import com.autoescuela.erp.examenes.dto.GestionarSolicitudExamenDTO;
import com.autoescuela.erp.examenes.model.CupoExamen;
import com.autoescuela.erp.examenes.model.Examen;
import com.autoescuela.erp.examenes.model.FechaExamen;
import com.autoescuela.erp.examenes.model.SolicitudExamen;
import com.autoescuela.erp.examenes.repository.CupoExamenRepository;
import com.autoescuela.erp.examenes.repository.ExamenRepository;
import com.autoescuela.erp.examenes.repository.FechaExamenRepository;
import com.autoescuela.erp.examenes.repository.SolicitudExamenRepository;
import com.autoescuela.erp.practicas.model.ClasePractica;
import com.autoescuela.erp.practicas.repository.ClasePracticaRepository;
import com.autoescuela.erp.practicas.util.FestivosCalendarioUtil;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.model.Profesor;
import com.autoescuela.erp.usuarios.repository.AlumnoRepository;

import lombok.RequiredArgsConstructor;

/**
 * Servicio de gestión operativa y académica para solicitudes y actas de exámenes oficiales DGT.
 */
@Service
@RequiredArgsConstructor
public class ExamenService
{
    private final SolicitudExamenRepository solicitudExamenRepository;
    private final ExamenRepository examenRepository;
    private final MatriculaRepository matriculaRepository;
    private final AlumnoRepository alumnoRepository;
    private final ClasePracticaRepository clasePracticaRepository;
    private final FechaExamenRepository fechaExamenRepository;
    private final CupoExamenRepository cupoExamenRepository;
    private final EmailService emailService;

    /**
     * Recupera las solicitudes de examen más recientes para el resumen rápido del Dashboard de Administración.
     */
    @Transactional(readOnly = true)
    public List<SolicitudExamen> obtenerSolicitudesExamenDashboard()
    {
        return this.solicitudExamenRepository.findTop5ByOrderByIdDesc();
    }

    /**
     * Busca un examen por su identificador único.
     */
    @Transactional(readOnly = true)
    public Optional<Examen> buscarExamenPorId(Long id)
    {
        if (id == null)
        {
            return Optional.empty();
        }
        return this.examenRepository.findById(id);
    }

    /**
     * Recupera el histórico de exámenes de un alumno ordenados descendentemente (de más reciente a más antiguo).
     */
    @Transactional(readOnly = true)
    public List<Examen> obtenerExamenesPorAlumno(Alumno alumno)
    {
        if (alumno == null)
        {
            return List.of();
        }
        return this.examenRepository.findByAlumnoOrderByFechaHoraDesc(alumno);
    }

    /**
     * Recupera los exámenes asociados a los alumnos de un profesor ordenados descendentemente (de más reciente a más antiguo).
     */
    @Transactional(readOnly = true)
    public List<Examen> obtenerExamenesPorProfesor(Profesor profesor)
    {
        if (profesor == null)
        {
            return List.of();
        }
        return this.examenRepository.findByAlumnoProfesorOrderByFechaHoraDesc(profesor);
    }

    /**
     * Determina si el alumno ha resultado APTO en el examen teórico oficial.
     */
    @Transactional(readOnly = true)
    public boolean tieneTeoricoAprobado(Alumno alumno)
    {
        if (alumno == null)
        {
            return false;
        }
        return this.examenRepository.existsByAlumnoAndTipoAndEsAptoTrue(alumno, TipoExamen.TEORICO);
    }

    /**
     * Determina si el alumno ha resultado APTO en el examen práctico oficial.
     */
    @Transactional(readOnly = true)
    public boolean tienePracticoAprobado(Alumno alumno)
    {
        if (alumno == null)
        {
            return false;
        }
        return this.examenRepository.existsByAlumnoAndTipoAndEsAptoTrue(alumno, TipoExamen.PRACTICO);
    }

    /**
     * Cuenta el total de exámenes en estado APTO de un alumno.
     */
    @Transactional(readOnly = true)
    public long contarAptosPorAlumno(Alumno alumno)
    {
        if (alumno == null)
        {
            return 0L;
        }
        return this.examenRepository.countByAlumnoAndEsAptoTrue(alumno);
    }

    /**
     * Cuenta el total de exámenes en estado NO APTO de un alumno.
     */
    @Transactional(readOnly = true)
    public long contarNoAptosPorAlumno(Alumno alumno)
    {
        if (alumno == null)
        {
            return 0L;
        }
        return this.examenRepository.countByAlumnoAndEsAptoFalse(alumno);
    }

    /**
     * Persiste o actualiza un acta de examen oficial.
     */
    @Transactional
    public Examen guardar(Examen examen)
    {
        return this.examenRepository.save(examen);
    }

    /**
     * Recupera las solicitudes de examen asociadas a una matrícula ordenadas descendentemente (de más reciente a más antiguo).
     */
    @Transactional(readOnly = true)
    public List<SolicitudExamen> obtenerSolicitudesPorMatriculaDesc(Matricula matricula)
    {
        if (matricula == null)
        {
            return List.of();
        }
        return this.solicitudExamenRepository.findByMatriculaOrderByFechaHoraDesc(matricula);
    }

    /**
     * Recupera las solicitudes de examen asociadas a un alumno ordenadas descendentemente (de más reciente a más antiguo).
     */
    @Transactional(readOnly = true)
    public List<SolicitudExamen> obtenerSolicitudesPorAlumnoDesc(Alumno alumno)
    {
        if (alumno == null)
        {
            return List.of();
        }
        return this.solicitudExamenRepository.findByAlumnoOrderByFechaHoraDesc(alumno);
    }

    /**
     * Recupera las solicitudes de examen más recientes de un alumno ordenadas por ID descendente (de mayor a menor).
     */
    @Transactional(readOnly = true)
    public List<SolicitudExamen> obtenerSolicitudesRecientesPorAlumno(Alumno alumno)
    {
        if (alumno == null)
        {
            return List.of();
        }
        return this.solicitudExamenRepository.findByAlumnoOrderByIdDesc(alumno);
    }

    /**
     * Comprueba si el alumno tiene actualmente una solicitud de examen en estado PENDIENTE.
     */
    @Transactional(readOnly = true)
    public boolean tieneSolicitudPendiente(Alumno alumno)
    {
        if (alumno == null)
        {
            return false;
        }
        return this.solicitudExamenRepository.existsByAlumnoAndEstado(alumno, EstadoSolicitud.PENDIENTE);
    }

    /**
     * Comprueba si el alumno tiene una solicitud ACEPTADA con un examen fijado pendiente de celebración.
     */
    @Transactional(readOnly = true)
    public boolean tieneSolicitudAceptadaConExamenPendiente(Alumno alumno)
    {
        if (alumno == null)
        {
            return false;
        }
        return this.solicitudExamenRepository.existeSolicitudAceptadaConExamenPendiente(alumno, EstadoSolicitud.ACEPTADA, LocalDateTime.now());
    }

    /**
     * Registra una nueva solicitud de examen oficial para el alumno.
     */
    @Transactional
    public SolicitudExamen registrarSolicitudExamen(Alumno alumno, TipoExamen tipo, LocalDateTime fechaHora, Matricula matricula)
    {
        if (alumno == null)
        {
            throw new ReglaNegocioException("El alumno es obligatorio para registrar la solicitud de examen.");
        }
        SolicitudExamen solicitud = new SolicitudExamen();
        solicitud.setAlumno(alumno);
        solicitud.setMatricula(matricula);
        solicitud.setTipo(tipo);
        solicitud.setProfesor(tipo == TipoExamen.TEORICO ? null : alumno.getProfesor());
        solicitud.setFechaHora(fechaHora != null ? fechaHora : LocalDateTime.now());
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);

        return this.solicitudExamenRepository.save(solicitud);
    }

    /**
     * Asienta la calificación oficial de un examen práctico o teórico DGT (CU-025),
     * aplicando las transiciones de matrícula, desasignación de profesor y notificaciones por correo.
     *
     * @param dto DTO con identificador del examen y resultado Apto/No Apto.
     * @return Examen calificado y persistido.
     */
    @Transactional
    public Examen calificarExamenOficial(CalificarExamenDTO dto)
    {
        if (dto == null || dto.examenId() == null)
        {
            throw new ReglaNegocioException("El identificador del examen es obligatorio.");
        }

        Examen examen = this.examenRepository.findById(dto.examenId())
                .orElseThrow(() -> new ReglaNegocioException("No se encontró el examen a calificar."));

        if (examen.getEsApto() != null)
        {
            throw new ReglaNegocioException("Este examen ya ha sido calificado previamente y no puede modificarse.");
        }

        examen.setEsApto(dto.esApto());
        Examen examenGuardado = this.examenRepository.save(examen);

        // Si suspende, computar convocatoria gastada en la matrícula y notificar por email
        Alumno alumno = examen.getAlumno();
        if (Boolean.FALSE.equals(dto.esApto()) && alumno != null)
        {
            Optional<Matricula> matriculaOptional = this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno);
            int convocatoriasGastadas = 0;
            String tipoCarnet = "";
            if (matriculaOptional.isPresent())
            {
                Matricula matricula = matriculaOptional.get();
                convocatoriasGastadas = matricula.getConvocatoriasGastadas() != null ? matricula.getConvocatoriasGastadas() : 0;
                convocatoriasGastadas++;
                matricula.setConvocatoriasGastadas(convocatoriasGastadas);
                this.matriculaRepository.save(matricula);
                if (matricula.getPermisoCarnet() != null)
                {
                    tipoCarnet = matricula.getPermisoCarnet().getDescripcion();
                }
            }

            int convocatoriasRestantes = Math.max(0, 2 - convocatoriasGastadas);

            // Notificación por correo electrónico oficial de suspenso al alumno según tipo de examen
            if (alumno.getCorreo() != null && !alumno.getCorreo().isBlank())
            {
                // Si ha suspendido el examen teórico
                if (examen.getTipo() == TipoExamen.TEORICO)
                {
                    String correoAlumno = alumno.getCorreo();
                    String asunto = "Resultado oficial de tu Examen Teórico DGT - ManDS Autoescuela";
                    String mensaje = "Estimado/a " + alumno.getNombre() + ",\n\n"
                            + "Te comunicamos oficialmente que el resultado de tu examen teórico oficial de conducir"
                            + (tipoCarnet.isBlank() ? "" : " para el " + tipoCarnet) + " ha sido calificado como NO APTO.\n\n"
                            + "Queremos transmitirte mucho ánimo. En esta convocatoria se ha consumido 1 intento de tu matrícula. Convocatorias oficiales restantes: " + convocatoriasRestantes + " de 2.\n\n"
                            + (convocatoriasGastadas >= 2
                                    ? "Has agotado las 2 convocatorias oficiales incluidas en tu matrícula. Para poder presentarte a una nueva convocatoria oficial de examen deberás abonar las tasas de Renovación de Matrícula desde tu panel de pagos.\n\n"
                                    : "Te recomendamos continuar repasando los test y el temario teórico. Podrás solicitar una nueva fecha de examen oficial desde tu panel de alumno en cuanto te sientas preparado/a.\n\n")
                            + "Recuerda que el acceso a clases prácticas de circulación se habilitará una vez superada la prueba teórica oficial ante la DGT.\n\n"
                            + "¡Mucho ánimo y a seguir adelante!\n\n"
                            + "Un cordial saludo,\n"
                            + "Equipo de Coordinación - ManDS Autoescuela";

                    this.emailService.enviarNotificacionAlumno(correoAlumno, asunto, mensaje);
                }
                else // Si ha suspendido el examen práctico
                {
                    String correoAlumno = alumno.getCorreo();
                    String asunto = "Resultado oficial de tu Examen Práctico DGT - ManDS Autoescuela";
                    String mensaje = "Estimado/a " + alumno.getNombre() + ",\n\n"
                            + "Te comunicamos oficialmente que el resultado de tu examen práctico oficial de conducir"
                            + (tipoCarnet.isBlank() ? "" : " para el " + tipoCarnet) + " ha sido calificado como NO APTO.\n\n"
                            + "No te desanimes, es una etapa habitual en el aprendizaje de la conducción. En esta prueba se ha consumido 1 intento de tu matrícula. Convocatorias oficiales restantes: " + convocatoriasRestantes + " de 2.\n\n"
                            + (convocatoriasGastadas >= 2
                                    ? "Has acumulado 2 convocatorias agotadas. Por normativa oficial, la adquisición de saldo y la reserva de clases prácticas quedan temporalmente pausadas hasta que tramites la Renovación de Matrícula desde tu panel de pagos.\n\n"
                                    : "Mantienes asignado a tu profesor de autoescuela. Te recomendamos reservar clases prácticas adicionales en el calendario para pulir las maniobras antes de tu siguiente examen.\n\n")
                            + "¡Mucho ánimo, estamos a tu lado para conseguirlo!\n\n"
                            + "Un cordial saludo,\n"
                            + "Equipo de Coordinación - ManDS Autoescuela";

                    this.emailService.enviarNotificacionAlumno(correoAlumno, asunto, mensaje);
                }
            }
        }
        // Si aprueba el examen
        else
        {
            if (Boolean.TRUE.equals(dto.esApto()) && alumno != null)
            {
                // Si aprueba el examen práctico, finalizar el expediente del carnet y liberar cupo del profesor
                if(examen.getTipo() == TipoExamen.PRACTICO)
                {
                    Optional<Matricula> matriculaOptional = this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno);
                    String tipoCarnet = "";
                    if (matriculaOptional.isPresent())
                    {
                        Matricula matricula = matriculaOptional.get();
                        matricula.setEstaActiva(false);
                        this.matriculaRepository.save(matricula);
                        if (matricula.getPermisoCarnet() != null)
                        {
                            tipoCarnet = matricula.getPermisoCarnet().getDescripcion();
                        }
                    }

                    // Desvinculación de profesor asignado para liberar su cupo oficial
                    alumno.setProfesor(null);
                    this.alumnoRepository.save(alumno);

                    // Cancelación de clases prácticas futuras pendientes si quedase alguna agendada
                    List<ClasePractica> clasesPendientes = this.clasePracticaRepository.findByAlumnoAndEstadoClase(alumno, EstadoClase.PENDIENTE);
                    for (ClasePractica clase : clasesPendientes)
                    {
                        clase.setEstadoClase(EstadoClase.CANCELADA);
                        this.clasePracticaRepository.save(clase);
                    }

                    // Notificación por correo electrónico oficial de felicitación al alumno
                    if (alumno.getCorreo() != null && !alumno.getCorreo().isBlank())
                    {
                        String correoAlumno = alumno.getCorreo();
                        String asunto = "¡Enhorabuena! Has aprobado tu examen práctico en ManDS Autoescuela";
                        String mensaje = "Estimado/a " + alumno.getNombre() + ",\n\n"
                                + "¡Muchísimas felicidades! Te comunicamos oficialmente que has resultado APTO en tu examen práctico de conducir"
                                + (tipoCarnet.isBlank() ? "" : " para el " + tipoCarnet) + ".\n\n"
                                + "Tu expediente para este permiso ha finalizado con éxito. A partir de ahora podrás consultar todo tu historial de clases y notas desde tu panel de usuario.\n\n"
                                + "Si en el futuro deseas matricularte en un nuevo permiso de conducir, podrás gestionarlo directamente desde tu panel personal.\n\n"
                                + "¡Gracias por confiar en ManDS Autoescuela!\n\n"
                                + "Un cordial saludo,\n"
                                + "Equipo de Coordinación - ManDS Autoescuela";

                        this.emailService.enviarNotificacionAlumno(correoAlumno, asunto, mensaje);
                    }
                }

                // Si aprueba el examen teórico, notificar por correo electrónico oficial de felicitación e inicio de prácticas
                if(examen.getTipo() == TipoExamen.TEORICO)
                {
                    Optional<Matricula> matriculaOptional = this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno);
                    String tipoCarnet = "";
                    if (matriculaOptional.isPresent())
                    {
                        Matricula matricula = matriculaOptional.get();
                        if (matricula.getPermisoCarnet() != null)
                        {
                            tipoCarnet = matricula.getPermisoCarnet().getDescripcion();
                        }
                    }

                    // Notificación por correo electrónico oficial de felicitación al alumno
                    if (alumno.getCorreo() != null && !alumno.getCorreo().isBlank())
                    {
                        String correoAlumno = alumno.getCorreo();
                        String asunto = "¡Enhorabuena! Has aprobado tu examen teórico en ManDS Autoescuela";
                        String mensaje = "Estimado/a " + alumno.getNombre() + ",\n\n"
                                + "¡Muchísimas felicidades! Te comunicamos oficialmente que has resultado APTO en tu examen teórico oficial de conducir"
                                + (tipoCarnet.isBlank() ? "" : " para el " + tipoCarnet) + ".\n\n"
                                + "A partir de este momento tienes habilitado el acceso al calendario de reservas para comenzar tus clases prácticas de circulación con tu profesor de autoescuela.\n\n"
                                + "¡Mucho éxito en esta nueva etapa formativa hacia tu carnet!\n\n"
                                + "Un cordial saludo,\n"
                                + "Equipo de Coordinación - ManDS Autoescuela";

                        this.emailService.enviarNotificacionAlumno(correoAlumno, asunto, mensaje);
                    }
                }
            }
        }

        return examenGuardado;
    }

    // =========================================================================
    // 4. GESTIÓN ADMINISTRATIVA DE CONVOCATORIAS, CUPOS Y SOLICITUDES DGT
    // =========================================================================

    /**
     * Fija de manera irreversible las dos fechas oficiales de examen DGT de un mes concreto.
     */
    @Transactional
    public FechaExamen fijarFechasExamen(LocalDate mes, LocalDateTime fechaHora1, LocalDateTime fechaHora2)
    {
        if (mes == null || fechaHora1 == null || fechaHora2 == null)
        {
            throw new ReglaNegocioException("El mes y ambas fechas de convocatoria son obligatorios.");
        }

        LocalDate mesNormalizado = mes.withDayOfMonth(1);

        if (fechaHora1.getYear() != mesNormalizado.getYear() || fechaHora1.getMonthValue() != mesNormalizado.getMonthValue())
        {
            throw new ReglaNegocioException("La 1ª fecha de examen debe pertenecer al mes seleccionado.");
        }

        if (fechaHora2.getYear() != mesNormalizado.getYear() || fechaHora2.getMonthValue() != mesNormalizado.getMonthValue())
        {
            throw new ReglaNegocioException("La 2ª fecha de examen debe pertenecer al mes seleccionado.");
        }

        if (fechaHora2.isBefore(fechaHora1))
        {
            throw new ReglaNegocioException("La 2ª fecha de examen debe ser igual o posterior a la 1ª fecha.");
        }

        if (fechaHora1.getDayOfWeek() == DayOfWeek.SATURDAY || fechaHora1.getDayOfWeek() == DayOfWeek.SUNDAY)
        {
            throw new ReglaNegocioException("La 1ª fecha de examen no puede celebrarse en fin de semana.");
        }

        if (fechaHora2.getDayOfWeek() == DayOfWeek.SATURDAY || fechaHora2.getDayOfWeek() == DayOfWeek.SUNDAY)
        {
            throw new ReglaNegocioException("La 2ª fecha de examen no puede celebrarse en fin de semana.");
        }

        if (FestivosCalendarioUtil.esDiaNoLaborable(fechaHora1.toLocalDate()))
        {
            throw new ReglaNegocioException("La 1ª fecha coincide con un día inhábil ("
                    + FestivosCalendarioUtil.obtenerMotivoNoLaborable(fechaHora1.toLocalDate()) + ").");
        }

        if (FestivosCalendarioUtil.esDiaNoLaborable(fechaHora2.toLocalDate()))
        {
            throw new ReglaNegocioException("La 2ª fecha coincide con un día inhábil ("
                    + FestivosCalendarioUtil.obtenerMotivoNoLaborable(fechaHora2.toLocalDate()) + ").");
        }

        if (this.fechaExamenRepository.existsByMes(mesNormalizado))
        {
            throw new ReglaNegocioException("Las fechas oficiales de examen para este mes ya han sido fijadas y no pueden modificarse.");
        }

        FechaExamen nuevaConvocatoria = new FechaExamen(mesNormalizado, fechaHora1, fechaHora2);
        return this.fechaExamenRepository.save(nuevaConvocatoria);
    }

    /**
     * Fija de manera irreversible los cupos teóricos y prácticos por carnet para el mes especificado.
     */
    @Transactional
    public List<CupoExamen> fijarCuposExamen(LocalDate mes, List<CupoExamenDTO> cupos)
    {
        if (mes == null)
        {
            throw new ReglaNegocioException("El mes de aplicación es obligatorio.");
        }

        LocalDate mesNormalizado = mes.withDayOfMonth(1);

        if (this.cupoExamenRepository.existsByMes(mesNormalizado))
        {
            throw new ReglaNegocioException("Los cupos de examen para este mes ya han sido fijados y no pueden modificarse.");
        }

        if (cupos == null || cupos.isEmpty())
        {
            throw new ReglaNegocioException("Debes configurar los cupos para al menos un permiso.");
        }

        List<CupoExamen> listaPersistida = new ArrayList<>();
        for (CupoExamenDTO item : cupos)
        {
            if (item.tipoPermiso() == null)
            {
                continue;
            }
            int cupoTeorico = item.cupoTeorico() != null ? Math.max(0, item.cupoTeorico()) : 0;
            int cupoPractico = item.cupoPractico() != null ? Math.max(0, item.cupoPractico()) : 0;

            CupoExamen cupo = new CupoExamen(mesNormalizado, item.tipoPermiso(), cupoTeorico, cupoPractico);
            listaPersistida.add(this.cupoExamenRepository.save(cupo));
        }

        return listaPersistida;
    }

    /**
     * Recupera las fechas oficiales de examen fijadas para un mes concreto.
     */
    @Transactional(readOnly = true)
    public Optional<FechaExamen> obtenerFechasExamen(LocalDate mes)
    {
        if (mes == null)
        {
            return Optional.empty();
        }
        return this.fechaExamenRepository.findByMes(mes.withDayOfMonth(1));
    }

    /**
     * Recupera los cupos fijados para un mes concreto.
     */
    @Transactional(readOnly = true)
    public List<CupoExamen> obtenerCuposExamen(LocalDate mes)
    {
        if (mes == null)
        {
            return List.of();
        }
        return this.cupoExamenRepository.findByMes(mes.withDayOfMonth(1));
    }

    /**
     * Busca solicitudes de examen aplicando filtros opcionales.
     * Si no se especifica estado, filtra por defecto por PENDIENTE (orden FIFO).
     */
    @Transactional(readOnly = true)
    public List<SolicitudExamen> buscarSolicitudesConFiltros(
            String terminoBusqueda,
            Long profesorId,
            TipoCarnet tipoPermiso,
            TipoExamen tipoExamen,
            EstadoSolicitud estado)
    {
        return this.solicitudExamenRepository.buscarConFiltros(
                terminoBusqueda,
                profesorId,
                tipoPermiso,
                tipoExamen,
                estado);
    }

    /**
     * Busca una solicitud de examen por su identificador.
     */
    @Transactional(readOnly = true)
    public Optional<SolicitudExamen> buscarSolicitudPorId(Long id)
    {
        if (id == null)
        {
            return Optional.empty();
        }
        return this.solicitudExamenRepository.findById(id);
    }

    /**
     * Evalúa la disponibilidad de cupo y prerrequisitos académicos para gestionar una solicitud de examen en el mes activo o por defecto.
     */
    @Transactional(readOnly = true)
    public GestionarSolicitudExamenDTO obtenerGestionarSolicitud(Long solicitudId)
    {
        return this.obtenerGestionarSolicitud(solicitudId, null);
    }

    /**
     * Evalúa la disponibilidad de cupo y prerrequisitos académicos para gestionar una solicitud de examen en un mes específico.
     * Si no se indica mes, calcula automáticamente el mes pertinente sin retroceder a meses anteriores al actual.
     */
    @Transactional(readOnly = true)
    public GestionarSolicitudExamenDTO obtenerGestionarSolicitud(Long solicitudId, LocalDate mesSolicitado)
    {
        SolicitudExamen solicitud = this.solicitudExamenRepository.findById(solicitudId)
                .orElseThrow(() -> new ReglaNegocioException("No se encontró la solicitud de examen indicada."));

        LocalDateTime ahora = LocalDateTime.now();
        LocalDate mesActual = ahora.toLocalDate().withDayOfMonth(1);

        LocalDate mesAUsar;
        if (mesSolicitado != null)
        {
            mesAUsar = mesSolicitado.withDayOfMonth(1);
            if (mesAUsar.isBefore(mesActual))
            {
                mesAUsar = mesActual;
            }
        }
        else
        {
            // Determinar el mes de las convocatorias a consultar:
            // Si la solicitud es de un mes anterior o sus fechas ya expiraron, avanzamos al mes actual o siguiente
            LocalDate mesSolicitud = solicitud.getFechaHora() != null ? solicitud.getFechaHora().toLocalDate().withDayOfMonth(1) : mesActual;
            if (mesSolicitud.isBefore(mesActual))
            {
                mesSolicitud = mesActual;
            }

            Optional<FechaExamen> fechasOptDefault = this.fechaExamenRepository.findByMes(mesSolicitud);
            // Si las fechas del mes seleccionado ya se celebraron, avanzamos al siguiente mes con fechas configuradas
            if (fechasOptDefault.isPresent() && fechasOptDefault.get().getFechaHora2() != null && fechasOptDefault.get().getFechaHora2().isBefore(ahora))
            {
                LocalDate mesSiguiente = mesSolicitud.plusMonths(1).withDayOfMonth(1);
                Optional<FechaExamen> fechasSiguienteOpt = this.fechaExamenRepository.findByMes(mesSiguiente);
                if (fechasSiguienteOpt.isPresent())
                {
                    mesSolicitud = mesSiguiente;
                }
            }
            else if (fechasOptDefault.isEmpty())
            {
                fechasOptDefault = this.fechaExamenRepository.findByMes(mesActual);
                if (fechasOptDefault.isEmpty() || (fechasOptDefault.get().getFechaHora2() != null && fechasOptDefault.get().getFechaHora2().isBefore(ahora)))
                {
                    mesSolicitud = mesActual.plusMonths(1).withDayOfMonth(1);
                }
                else
                {
                    mesSolicitud = mesActual;
                }
            }
            mesAUsar = mesSolicitud;
        }

        Optional<FechaExamen> fechasOpt = this.fechaExamenRepository.findByMes(mesAUsar);

        // Validación de matrícula activa
        String motivoBloqueo = null;
        if (solicitud.getMatricula() == null || !Boolean.TRUE.equals(solicitud.getMatricula().getEstaActiva()))
        {
            motivoBloqueo = "El alumno no dispone de una matrícula activa en el permiso correspondiente.";
        }

        List<ClasePractica> clasesPendientes = List.of();
        if (solicitud.getTipo() == TipoExamen.PRACTICO && motivoBloqueo == null)
        {
            int clasesRecibidas = this.clasePracticaRepository.countByAlumnoAndEstadoClase(solicitud.getAlumno(), EstadoClase.RECIBIDA);
            if (clasesRecibidas == 0)
            {
                motivoBloqueo = "Es obligatorio haber completado clases en estado RECIBIDA antes de ser citado a la prueba práctica DGT.";
            }
            clasesPendientes = this.clasePracticaRepository.findByAlumnoAndEstadoClase(solicitud.getAlumno(), EstadoClase.PENDIENTE);
        }

        DisponibilidadConvocatoriaDTO disp1;
        DisponibilidadConvocatoriaDTO disp2;

        if (fechasOpt.isEmpty())
        {
            disp1 = new DisponibilidadConvocatoriaDTO(1, null, 0, 0, 0, false, "Sin Fijar");
            disp2 = new DisponibilidadConvocatoriaDTO(2, null, 0, 0, 0, false, "Sin Fijar");
        }
        else
        {
            FechaExamen fechas = fechasOpt.get();
            TipoCarnet carnet = solicitud.getMatricula() != null ? solicitud.getMatricula().getPermisoCarnet() : null;

            Optional<CupoExamen> cupoOpt = carnet != null
                    ? this.cupoExamenRepository.findByMesAndTipoPermiso(fechas.getMes(), carnet)
                    : Optional.empty();

            int cupoMaximo = 0;
            if (cupoOpt.isPresent())
            {
                cupoMaximo = solicitud.getTipo() == TipoExamen.TEORICO
                        ? cupoOpt.get().getCupoTeorico()
                        : cupoOpt.get().getCupoPractico();
            }

            if (cupoOpt.isEmpty() || cupoMaximo == 0)
            {
                disp1 = new DisponibilidadConvocatoriaDTO(1, fechas.getFechaHora1(), 0, 0, 0, false, "Sin Fijar");
                disp2 = new DisponibilidadConvocatoriaDTO(2, fechas.getFechaHora2(), 0, 0, 0, false, "Sin Fijar");
            }
            else
            {
                int citados1 = carnet != null
                        ? (int) this.solicitudExamenRepository.countAlumnosCitados(fechas.getFechaHora1(), carnet, solicitud.getTipo())
                        : 0;
                int citados2 = carnet != null
                        ? (int) this.solicitudExamenRepository.countAlumnosCitados(fechas.getFechaHora2(), carnet, solicitud.getTipo())
                        : 0;

                int libres1 = Math.max(0, cupoMaximo - citados1);
                int libres2 = Math.max(0, cupoMaximo - citados2);

                // Regla Temporal: las convocatorias pasadas no admiten asignación
                boolean fecha1Pasada = fechas.getFechaHora1() != null && fechas.getFechaHora1().isBefore(ahora);
                boolean fecha2Pasada = fechas.getFechaHora2() != null && fechas.getFechaHora2().isBefore(ahora);

                // Regla de Clases Pendientes vs Fecha de Examen:
                // Se bloquea la fecha si el alumno tiene alguna clase práctica pendiente con fecha posterior a dicha convocatoria.
                final List<ClasePractica> pendientesFinal = clasesPendientes;
                boolean tieneClasesPosteriores1 = solicitud.getTipo() == TipoExamen.PRACTICO &&
                        pendientesFinal.stream().anyMatch(c -> c.getFechaHora() != null && c.getFechaHora().isAfter(fechas.getFechaHora1()));

                boolean tieneClasesPosteriores2 = solicitud.getTipo() == TipoExamen.PRACTICO &&
                        pendientesFinal.stream().anyMatch(c -> c.getFechaHora() != null && c.getFechaHora().isAfter(fechas.getFechaHora2()));

                boolean disponible1 = motivoBloqueo == null && !fecha1Pasada && libres1 > 0 && !tieneClasesPosteriores1;
                String motivo1 = motivoBloqueo != null ? motivoBloqueo
                        : (fecha1Pasada ? "Convocatoria ya celebrada"
                        : (libres1 == 0 ? "Cupo completo (" + citados1 + "/" + cupoMaximo + ")"
                        : (tieneClasesPosteriores1 ? "El alumno tiene clase(s) pendiente(s) posterior(es) a esta fecha" : null)));

                boolean disponible2 = motivoBloqueo == null && !fecha2Pasada && libres2 > 0 && !tieneClasesPosteriores2;
                String motivo2 = motivoBloqueo != null ? motivoBloqueo
                        : (fecha2Pasada ? "Convocatoria ya celebrada"
                        : (libres2 == 0 ? "Cupo completo (" + citados2 + "/" + cupoMaximo + ")"
                        : (tieneClasesPosteriores2 ? "El alumno tiene clase(s) pendiente(s) posterior(es) a esta fecha" : null)));

                disp1 = new DisponibilidadConvocatoriaDTO(1, fechas.getFechaHora1(), cupoMaximo, citados1, libres1, disponible1, motivo1);
                disp2 = new DisponibilidadConvocatoriaDTO(2, fechas.getFechaHora2(), cupoMaximo, citados2, libres2, disponible2, motivo2);
            }
        }

        boolean puedeAceptar = disp1.disponible() || disp2.disponible();

        return new GestionarSolicitudExamenDTO(solicitud, disp1, disp2, puedeAceptar, motivoBloqueo, mesAUsar);
    }

    /**
     * Acepta una solicitud de examen y cita oficialmente al alumno para Fecha 1 o Fecha 2 del mes.
     * Operación irreversible.
     */
    @Transactional
    public SolicitudExamen aceptarSolicitudExamen(Long solicitudId, int numeroConvocatoria)
    {
        return this.aceptarSolicitudExamen(solicitudId, numeroConvocatoria, null);
    }

    /**
     * Acepta una solicitud de examen y cita oficialmente al alumno para Fecha 1 o Fecha 2 del mes especificado.
     * Operación irreversible.
     */
    @Transactional
    public SolicitudExamen aceptarSolicitudExamen(Long solicitudId, int numeroConvocatoria, LocalDate mes)
    {
        if (solicitudId == null)
        {
            throw new ReglaNegocioException("El identificador de la solicitud es obligatorio.");
        }

        if (numeroConvocatoria != 1 && numeroConvocatoria != 2)
        {
            throw new ReglaNegocioException("Debes seleccionar Fecha 1 o Fecha 2 oficial del mes.");
        }

        SolicitudExamen solicitud = this.solicitudExamenRepository.findById(solicitudId)
                .orElseThrow(() -> new ReglaNegocioException("No se encontró la solicitud de examen indicada."));

        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE)
        {
            throw new ReglaNegocioException("Esta solicitud ya ha sido resuelta previamente y no puede modificarse.");
        }

        GestionarSolicitudExamenDTO diagnostico = this.obtenerGestionarSolicitud(solicitudId, mes);
        DisponibilidadConvocatoriaDTO convElegida = numeroConvocatoria == 1 ? diagnostico.convocatoria1() : diagnostico.convocatoria2();

        if (!convElegida.disponible())
        {
            String motivo = convElegida.motivoNoDisponible() != null
                    ? convElegida.motivoNoDisponible()
                    : "No hay cupo disponible en la fecha seleccionada.";
            throw new ReglaNegocioException("No es posible citar al alumno en la Fecha " + numeroConvocatoria + ": " + motivo);
        }

        LocalDateTime fechaExamen = convElegida.fechaHora();

        // 1. Marcar solicitud como ACEPTADA
        solicitud.setEstado(EstadoSolicitud.ACEPTADA);

        // 2. Crear y persistir el Examen vinculado
        Examen examen = new Examen();
        examen.setFechaHora(fechaExamen);
        examen.setDuracion(solicitud.getTipo() == TipoExamen.TEORICO ? 30 : 35);
        examen.setTipo(solicitud.getTipo());
        examen.setAlumno(solicitud.getAlumno());
        examen.setSolicitudExamen(solicitud);
        examen.setEsApto(null);
        Examen examenGuardado = this.examenRepository.save(examen);
        solicitud.setExamen(examenGuardado);

        SolicitudExamen solicitudGuardada = this.solicitudExamenRepository.save(solicitud);

        // 3. Si es práctico, bloquear jornada completa del vehículo y profesor cancelando clases prácticas del día (Regla 3.3 y 3.5)
        if (solicitud.getTipo() == TipoExamen.PRACTICO)
        {
            Profesor profesor = solicitud.getProfesor() != null ? solicitud.getProfesor() : solicitud.getAlumno().getProfesor();
            if (profesor != null)
            {
                LocalDateTime inicioDia = fechaExamen.toLocalDate().atStartOfDay();
                LocalDateTime finDia = fechaExamen.toLocalDate().atTime(23, 59, 59);

                List<ClasePractica> clasesJornada = this.clasePracticaRepository
                        .findByProfesorAndFechaHoraBetweenOrderByFechaHoraAsc(profesor, inicioDia, finDia);

                for (ClasePractica clase : clasesJornada)
                {
                    if (clase.getEstadoClase() == EstadoClase.PENDIENTE)
                    {
                        clase.setEstadoClase(EstadoClase.CANCELADA);
                        clase.setIsCancelacionJornada(true);
                        clase.setMotivoCancelacion("Jornada bloqueada por convocatoria oficial de examen práctico DGT.");
                        this.clasePracticaRepository.save(clase);

                        if (clase.getAlumno() != null && clase.getAlumno().getCorreo() != null && !clase.getAlumno().getCorreo().isBlank())
                        {
                            this.emailService.enviarNotificacionAlumno(
                                    clase.getAlumno().getCorreo(),
                                    "Cancelación de clase práctica por jornada oficial de exámenes DGT",
                                    "Estimado/a " + clase.getAlumno().getNombre() + ",\n\n"
                                    + "Te informamos de que tu clase práctica fijada para el día "
                                    + fechaExamen.toLocalDate() + " ha sido cancelada por Coordinación debido a que tu profesor y su vehículo estarán destinados a la jornada oficial de exámenes prácticos de la DGT.\n\n"
                                    + "Se ha restituido tu capacidad de reserva para que puedas seleccionar una nueva clase en el calendario cuando lo desees.\n\n"
                                    + "Disculpa las molestias,\n"
                                    + "Equipo de Coordinación - ManDS Autoescuela"
                            );
                        }
                    }
                }
            }
        }

        // 4. Enviar citación oficial por correo al alumno
        Alumno alumno = solicitud.getAlumno();
        if (alumno != null && alumno.getCorreo() != null && !alumno.getCorreo().isBlank())
        {
            String fechaTexto = fechaExamen.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            String permisoDesc = solicitud.getMatricula() != null && solicitud.getMatricula().getPermisoCarnet() != null
                    ? solicitud.getMatricula().getPermisoCarnet().getDescripcion()
                    : "conducción";

            this.emailService.enviarNotificacionAlumno(
                    alumno.getCorreo(),
                    "Citación Oficial a Examen DGT - ManDS Autoescuela",
                    "Estimado/a " + alumno.getNombre() + ",\n\n"
                    + "Te comunicamos que tu solicitud de examen oficial "
                    + (solicitud.getTipo() == TipoExamen.TEORICO ? "teórico" : "práctico")
                    + " para el " + permisoDesc + " ha sido ACEPTADA.\n\n"
                    + "Datos oficiales de la citación:\n"
                    + "• Fecha y hora: " + fechaTexto + " h\n"
                    + "• Tipo de prueba: " + (solicitud.getTipo() == TipoExamen.TEORICO ? "Teórico Oficial DGT" : "Práctico de Circulación DGT") + "\n\n"
                    + "Recuerda presentarte con tu documento identificativo en vigor (DNI / NIE) y con 15 minutos de antelación.\n\n"
                    + "¡Mucho éxito en la prueba!\n"
                    + "Equipo de Coordinación - ManDS Autoescuela"
            );
        }

        return solicitudGuardada;
    }

    /**
     * Rechaza una solicitud de examen indicando obligatoriamente el motivo pedagógico o técnico.
     * Operación irreversible.
     */
    @Transactional
    public SolicitudExamen rechazarSolicitudExamen(Long solicitudId, String comentarioJustificacion)
    {
        if (solicitudId == null)
        {
            throw new ReglaNegocioException("El identificador de la solicitud es obligatorio.");
        }

        if (comentarioJustificacion == null || comentarioJustificacion.trim().isBlank())
        {
            throw new ReglaNegocioException("Debes indicar obligatoriamente el motivo pedagógico o técnico de la denegación.");
        }

        SolicitudExamen solicitud = this.solicitudExamenRepository.findById(solicitudId)
                .orElseThrow(() -> new ReglaNegocioException("No se encontró la solicitud de examen indicada."));

        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE)
        {
            throw new ReglaNegocioException("Esta solicitud ya ha sido resuelta previamente y no puede modificarse.");
        }

        solicitud.setEstado(EstadoSolicitud.RECHAZADA);
        solicitud.setComentarioJustificacion(comentarioJustificacion.trim());
        SolicitudExamen solicitudGuardada = this.solicitudExamenRepository.save(solicitud);

        // Notificación por correo con el motivo oficial
        Alumno alumno = solicitud.getAlumno();
        if (alumno != null && alumno.getCorreo() != null && !alumno.getCorreo().isBlank())
        {
            this.emailService.enviarNotificacionAlumno(
                    alumno.getCorreo(),
                    "Resolución de tu Solicitud de Examen DGT - ManDS Autoescuela",
                    "Estimado/a " + alumno.getNombre() + ",\n\n"
                    + "Te comunicamos que tu solicitud de examen oficial ha sido RECHAZADA por la Administración del centro.\n\n"
                    + "Motivo o justificación indicada:\n"
                    + comentarioJustificacion.trim() + "\n\n"
                    + "Podrás formular una nueva solicitud desde tu panel de alumno una vez subsanadas las observaciones notificadas.\n\n"
                    + "Un cordial saludo,\n"
                    + "Equipo de Coordinación - ManDS Autoescuela"
            );
        }

        return solicitudGuardada;
    }
}
