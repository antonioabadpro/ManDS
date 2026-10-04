package com.autoescuela.erp.usuarios.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autoescuela.erp.academico.model.Matricula;
import com.autoescuela.erp.academico.repository.MatriculaRepository;
import com.autoescuela.erp.core.email.EmailService;
import com.autoescuela.erp.core.enums.EstadoClase;
import com.autoescuela.erp.core.enums.EstadoUsuario;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.excepciones.RecursoNoEncontradoException;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.examenes.model.Examen;
import com.autoescuela.erp.examenes.repository.ExamenRepository;
import com.autoescuela.erp.practicas.model.ClasePractica;
import com.autoescuela.erp.practicas.repository.ClasePracticaRepository;
import com.autoescuela.erp.usuarios.dto.AlumnoDetalleDTO;
import com.autoescuela.erp.usuarios.dto.AlumnoExpedienteDTO;
import com.autoescuela.erp.usuarios.dto.AlumnoResumenDTO;
import com.autoescuela.erp.usuarios.dto.ClasePracticaExpedienteDTO;
import com.autoescuela.erp.usuarios.dto.ExamenExpedienteDTO;
import com.autoescuela.erp.usuarios.dto.ReasignarAlumnoDTO;
import com.autoescuela.erp.usuarios.mapper.AlumnoMapper;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.model.Profesor;
import com.autoescuela.erp.usuarios.repository.AlumnoRepository;
import com.autoescuela.erp.usuarios.repository.ProfesorRepository;

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
    private final ExamenRepository examenRepository;
    private final ProfesorRepository profesorRepository;
    private final EmailService emailService;
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

    /**
     * Obtiene la información académica y el expediente completo del alumno para su visualización
     * en el modal de expediente del panel de administración (últimas 3 clases y últimos 2 exámenes).
     *
     * @param id Identificador único del alumno.
     * @return DTO inmutable poblado con saldos, convocatorias, histórico reciente de clases y exámenes.
     */
    @Transactional(readOnly = true)
    public AlumnoExpedienteDTO obtenerExpedienteAlumno(Long id)
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

        long clasesRealizadas = this.clasePracticaRepository
                .countByAlumnoAndEstadoClase(alumno, EstadoClase.RECIBIDA);

        List<ClasePractica> clasesEntidades = this.clasePracticaRepository
                .findTop3ByAlumnoOrderByFechaHoraDesc(alumno);
        List<ClasePracticaExpedienteDTO> ultimasClases = clasesEntidades.stream()
                .map(this.alumnoMapper::toClasePracticaExpedienteDTO)
                .toList();

        List<Examen> examenesEntidades = this.examenRepository
                .findTop2ByAlumnoOrderByFechaHoraDesc(alumno);
        List<ExamenExpedienteDTO> ultimosExamenes = examenesEntidades.stream()
                .map(this.alumnoMapper::toExamenExpedienteDTO)
                .toList();

        return this.alumnoMapper.toAlumnoExpedienteDTO(alumno, matriculaActiva, clasesRealizadas, ultimasClases, ultimosExamenes);
    }

    /**
     * Cuenta el número de clases prácticas en estado PENDIENTE que tiene actualmente un alumno.
     *
     * @param alumnoId Identificador del alumno.
     * @return Total de clases prácticas pendientes de impartición.
     */
    @Transactional(readOnly = true)
    public int contarClasesPendientes(Long alumnoId)
    {
        if (alumnoId == null)
        {
            return 0;
        }
        Alumno alumno = this.alumnoRepository.findById(alumnoId).orElse(null);
        if (alumno == null)
        {
            return 0;
        }
        return this.clasePracticaRepository.countByAlumnoAndEstadoClase(alumno, EstadoClase.PENDIENTE);
    }

    /**
     * Reasigna el profesor de un alumno o lo deja temporalmente sin profesor.
     * Conforme a los requerimientos de negocio:
     * - Se cancelan automáticamente todas las clases prácticas pendientes del alumno.
     * - Se envía una notificación transaccional por correo electrónico informándole del cambio de docente o desasignación.
     *
     * @param dto Parámetros de la solicitud (alumnoId, opcion, nuevoProfesorId).
     */
    @Transactional
    public void reasignarProfesor(ReasignarAlumnoDTO dto)
    {
        if (dto == null || dto.alumnoId() == null)
        {
            throw new ReglaNegocioException("El identificador del alumno es obligatorio para tramitar la reasignación.");
        }

        Alumno alumno = this.alumnoRepository.findById(dto.alumnoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el alumno con ID: " + dto.alumnoId()));

        if (alumno.getEstado() != EstadoUsuario.ACTIVO)
        {
            throw new ReglaNegocioException("Solo se pueden reasignar profesores a alumnos que se encuentren en estado ACTIVO.");
        }

        boolean dejarSinProfesor = "SIN_PROFESOR".equalsIgnoreCase(dto.opcion()) || dto.nuevoProfesorId() == null;

        if (dejarSinProfesor)
        {
            if (alumno.getProfesor() == null)
            {
                throw new ReglaNegocioException("El alumno ya se encuentra sin profesor asignado actualmente.");
            }

            alumno.setProfesor(null);
            this.alumnoRepository.save(alumno);

            // Cancelación de clases prácticas pendientes
            List<ClasePractica> clasesPendientes = this.clasePracticaRepository.findByAlumnoAndEstadoClase(alumno, EstadoClase.PENDIENTE);
            for (ClasePractica clase : clasesPendientes)
            {
                clase.setEstadoClase(EstadoClase.CANCELADA);
                this.clasePracticaRepository.save(clase);
            }

            // Notificación por correo al alumno
            String asunto = "Aviso importante: Actualización en la asignación de tu profesor";
            String mensaje = "Estimado/a " + alumno.getNombre() + ",\n\n"
                    + "Te comunicamos que se ha retirado la asignación de tu profesor/a de prácticas actual.\n"
                    + "Actualmente tu expediente queda temporalmente sin profesor asignado y todas tus clases prácticas pendientes "
                    + "han sido canceladas de forma automática conforme al protocolo del centro.\n\n"
                    + "El equipo de administración te asignará un/una nuevo/a profesor/a con la mayor brevedad posible para que puedas retomar tus clases.\n\n"
                    + "Un cordial saludo,\n"
                    + "Equipo de Coordinación - ManDS Autoescuela";

            this.emailService.enviarNotificacionAlumno(alumno.getCorreo(), asunto, mensaje);
        }
        else // Si NO queremos dejar al alumno sin profesor, procedemos a reasignarle uno nuevo
        {
            Long nuevoProfesorId = dto.nuevoProfesorId();
            if (alumno.getProfesor() != null && alumno.getProfesor().getId().equals(nuevoProfesorId))
            {
                throw new ReglaNegocioException("El alumno ya tiene asignado al profesor seleccionado. Elige un docente diferente o la opción de dejarlo sin profesor.");
            }

            Profesor nuevoProfesor = this.profesorRepository.findById(nuevoProfesorId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el profesor seleccionado con ID: " + nuevoProfesorId));

            if (nuevoProfesor.getEstado() != EstadoUsuario.ACTIVO)
            {
                throw new ReglaNegocioException("El profesor seleccionado debe encontrarse en estado ACTIVO.");
            }

            Matricula matriculaActiva = this.matriculaRepository
                    .findByAlumnoAndEstaActivaTrue(alumno)
                    .orElse(null);

            if (matriculaActiva != null && matriculaActiva.getPermisoCarnet() != null)
            {
                TipoCarnet permisoRequerido = matriculaActiva.getPermisoCarnet();
                if (nuevoProfesor.getListaTiposCarnet() == null || !nuevoProfesor.getListaTiposCarnet().contains(permisoRequerido))
                {
                    throw new ReglaNegocioException("El profesor seleccionado no dispone del carnet "
                            + permisoRequerido.getDescripcion() + " requerido para la formación del alumno.");
                }
            }

            alumno.setProfesor(nuevoProfesor);
            this.alumnoRepository.save(alumno);

            // Cancelación de clases prácticas pendientes
            List<ClasePractica> clasesPendientes = this.clasePracticaRepository.findByAlumnoAndEstadoClase(alumno, EstadoClase.PENDIENTE);
            for (ClasePractica clase : clasesPendientes)
            {
                clase.setEstadoClase(EstadoClase.CANCELADA);
                this.clasePracticaRepository.save(clase);
            }

            String nombreNuevoProfesor = nuevoProfesor.getNombre() + " " + nuevoProfesor.getApellidos();
            String asunto = "Aviso importante: Asignación de nuevo profesor de prácticas";
            String mensaje = "Estimado/a " + alumno.getNombre() + ",\n\n"
                    + "Te comunicamos que se ha modificado la asignación de tu profesor/a de prácticas actual.\n"
                    + "A partir de este momento, tu nuevo/a profesor/a asignado es " + nombreNuevoProfesor + ".\n\n"
                    + "Conforme al protocolo del centro, todas tus clases prácticas pendientes han sido canceladas automáticamente. "
                    + "Te invitamos a acceder a tu panel de alumno para programar tus próximas sesiones prácticas con tu nuevo/a profesor/a.\n\n"
                    + "Un cordial saludo,\n"
                    + "Equipo de Coordinación - ManDS Autoescuela";

            this.emailService.enviarNotificacionAlumno(alumno.getCorreo(), asunto, mensaje);
        }
    }

    /**
     * Tramita la baja lógica de un alumno conforme a las reglas de negocio del ERP:
     * - Su estado pasa a INACTIVO preservando todo el histórico de clases, exámenes y facturación.
     * - Se desvincula a su profesor actual para liberar el recuento de alumnos a su cargo.
     * - Se cancelan automáticamente todas sus clases prácticas en estado PENDIENTE.
     * - Se envía un correo electrónico de notificación tanto al alumno como al profesor asignado.
     *
     * @param alumnoId Identificador único del alumno a dar de baja.
     * @return Entidad Alumno actualizada en estado INACTIVO.
     */
    @Transactional
    public Alumno darBajaAlumno(Long alumnoId)
    {
        if (alumnoId == null)
        {
            throw new ReglaNegocioException("El identificador del alumno no puede ser nulo.");
        }

        Alumno alumno = this.alumnoRepository.findById(alumnoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el alumno con ID: " + alumnoId));

        if (alumno.getEstado() == EstadoUsuario.INACTIVO)
        {
            throw new ReglaNegocioException("El alumno " + alumno.getNombre() + " " + alumno.getApellidos() + " ya se encuentra en estado INACTIVO.");
        }

        // Desvinculación del profesor asignado para liberar su cupo
        Profesor profesorAsignado = alumno.getProfesor();

        // Eliminamos el alumno
        alumno.setEstado(EstadoUsuario.INACTIVO);
        alumno.setProfesor(null);

        // Cancelación de clases prácticas pendientes
        List<ClasePractica> clasesPendientes = this.clasePracticaRepository.findByAlumnoAndEstadoClase(alumno, EstadoClase.PENDIENTE);
        for (ClasePractica clase : clasesPendientes)
        {
            clase.setEstadoClase(EstadoClase.CANCELADA);
            this.clasePracticaRepository.save(clase);
        }

        Alumno alumnoGuardado = this.alumnoRepository.save(alumno);

        // Notificación por correo electrónico al alumno
        String asuntoAlumno = "Aviso importante: Tramitación de baja de tu expediente de alumno";
        String mensajeAlumno = "Estimado/a " + alumno.getNombre() + ",\n\n"
                + "Te comunicamos que tu expediente de alumno en ManDS Autoescuela ha sido dado de baja.\n"
                + "Conforme al protocolo de la autoescuela, todas tus clases prácticas pendientes (" + clasesPendientes.size() + ") "
                + "han sido canceladas de forma automática y se ha desvinculado a tu profesor asignado.\n\n"
                + "Para cualquier consulta sobre tu expediente o una futura reactivación, por favor ponte en contacto con la administración del centro.\n\n"
                + "Un cordial saludo,\n"
                + "Equipo de Coordinación - ManDS Autoescuela";
        this.emailService.enviarNotificacionAlumno(alumno.getCorreo(), asuntoAlumno, mensajeAlumno);

        // Notificación por correo electrónico al profesor asignado si tuviese uno
        if (profesorAsignado != null && profesorAsignado.getCorreo() != null)
        {
            String asuntoProfesor = "Aviso: Baja de alumno asignado";
            String mensajeProfesor = "Estimado/a " + profesorAsignado.getNombre() + ",\n\n"
                    + "Te informamos de que el alumno " + alumno.getNombre() + " " + alumno.getApellidos() + " (DNI: " + alumno.getDni() + ") "
                    + "ha sido dado de baja en la autoescuela.\n"
                    + "Como consecuencia, dicho alumno ha sido desvinculado de tu lista de alumnos y todas las clases prácticas pendientes "
                    + "que tenía reservadas contigo (" + clasesPendientes.size() + ") han sido canceladas de forma automática, liberando dichos tramos de tu calendario de prácticas.\n\n"
                    + "Un cordial saludo,\n"
                    + "Equipo de Coordinación - ManDS Autoescuela";
            this.emailService.enviarNotificacionProfesor(profesorAsignado.getCorreo(), asuntoProfesor, mensajeProfesor);
        }

        return alumnoGuardado;
    }
}
