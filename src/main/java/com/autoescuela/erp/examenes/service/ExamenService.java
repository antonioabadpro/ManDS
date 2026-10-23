package com.autoescuela.erp.examenes.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autoescuela.erp.academico.model.Matricula;
import com.autoescuela.erp.academico.repository.MatriculaRepository;
import com.autoescuela.erp.core.email.EmailService;
import com.autoescuela.erp.core.enums.EstadoClase;
import com.autoescuela.erp.core.enums.EstadoSolicitud;
import com.autoescuela.erp.core.enums.TipoExamen;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.examenes.dto.CalificarExamenDTO;
import com.autoescuela.erp.examenes.model.Examen;
import com.autoescuela.erp.examenes.model.SolicitudExamen;
import com.autoescuela.erp.examenes.repository.ExamenRepository;
import com.autoescuela.erp.examenes.repository.SolicitudExamenRepository;
import com.autoescuela.erp.practicas.model.ClasePractica;
import com.autoescuela.erp.practicas.repository.ClasePracticaRepository;
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
}
