package com.autoescuela.erp.usuarios.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autoescuela.erp.auth.model.TokenVerificacion;
import com.autoescuela.erp.auth.service.TokenVerificacionService;
import com.autoescuela.erp.core.email.EmailService;
import com.autoescuela.erp.core.enums.EstadoClase;
import com.autoescuela.erp.core.enums.EstadoUsuario;
import com.autoescuela.erp.core.enums.EstadoVehiculo;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.enums.TipoTurno;
import com.autoescuela.erp.core.excepciones.RecursoNoEncontradoException;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.flota.model.Vehiculo;
import com.autoescuela.erp.flota.repository.VehiculoRepository;
import com.autoescuela.erp.practicas.model.ClasePractica;
import com.autoescuela.erp.practicas.repository.ClasePracticaRepository;
import com.autoescuela.erp.usuarios.dto.AltaProfesorDTO;
import com.autoescuela.erp.usuarios.dto.BajaProfesorDTO;
import com.autoescuela.erp.usuarios.dto.EditarProfesorDTO;
import com.autoescuela.erp.usuarios.dto.ProfesorDetalleDTO;
import com.autoescuela.erp.usuarios.dto.ProfesorResumenDTO;
import com.autoescuela.erp.usuarios.mapper.ProfesorMapper;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.model.Profesor;
import com.autoescuela.erp.usuarios.repository.AlumnoRepository;
import com.autoescuela.erp.usuarios.repository.PersonaRepository;
import com.autoescuela.erp.usuarios.repository.ProfesorRepository;

import lombok.RequiredArgsConstructor;

/**
 * Servicio para la gestión operativa y académica de profesores.
 */
@Service
@RequiredArgsConstructor
public class ProfesorService
{
    private final ProfesorRepository profesorRepository;
    private final PersonaRepository personaRepository;
    private final VehiculoRepository vehiculoRepository;
    private final AlumnoRepository alumnoRepository;
    private final ClasePracticaRepository clasePracticaRepository;
    private final TokenVerificacionService tokenVerificacionService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final ProfesorMapper profesorMapper;

    /**
     * Da de alta a un nuevo profesor en el sistema, validando unicidad de datos,
     * asignando el vehículo si procede (Regla 7.3) y generando la invitación segura por correo (Regla 7.1).
     *
     * @param dto Datos del nuevo profesor validados sintácticamente.
     * @param baseUrl URL base del servidor para generar el enlace de activación.
     * @return Entidad Profesor persistida.
     * @throws ReglaNegocioException Si el DNI, correo o teléfono ya están registrados, o el vehículo está ocupado.
     */
    @Transactional
    public Profesor darAltaProfesor(AltaProfesorDTO dto, String baseUrl)
    {
        final int duracionTokenMinutos = 60; // Duración del token de activación en minutos (1 hora)
        if (dto == null)
        {
            throw new ReglaNegocioException("Los datos para el alta del profesor no pueden ser nulos.");
        }

        if (dto.fechaNacimiento() == null || dto.fechaNacimiento().plusYears(18).isAfter(LocalDate.now()))
        {
            throw new ReglaNegocioException("El profesor debe ser mayor de edad (al menos 18 años).");
        }

        LocalDate haceUnMesAlta = LocalDate.now().minusMonths(1);
        LocalDate enUnMesAlta = LocalDate.now().plusMonths(1);
        if (dto.fechaContratacion() == null || dto.fechaContratacion().isBefore(haceUnMesAlta) || dto.fechaContratacion().isAfter(enUnMesAlta))
        {
            throw new ReglaNegocioException("La fecha de contratación debe estar comprendida entre 1 mes antes y 1 mes después de la fecha actual.");
        }

        if (dto.permisos() == null || dto.permisos().isEmpty())
        {
            throw new ReglaNegocioException("Debe seleccionar al menos un permiso de conducción autorizado.");
        }

        String dniLimpio = dto.dni().trim().toUpperCase();
        if (this.personaRepository.existsByDni(dniLimpio))
        {
            throw new ReglaNegocioException("Ya existe un usuario registrado con el DNI/NIE " + dniLimpio + ".");
        }

        String correoLimpio = dto.correo().trim().toLowerCase();
        if (this.personaRepository.existsByCorreo(correoLimpio))
        {
            throw new ReglaNegocioException("Ya existe un usuario registrado con el correo electrónico " + correoLimpio + ".");
        }

        String telefonoLimpio = dto.telefono().trim();
        if (this.personaRepository.existsByTelefono(telefonoLimpio))
        {
            throw new ReglaNegocioException("Ya existe un usuario registrado con el número de teléfono " + telefonoLimpio + ".");
        }

        Vehiculo vehiculo = null;
        if (dto.vehiculoId() != null)
        {
            vehiculo = this.vehiculoRepository.findById(dto.vehiculoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el vehículo con ID: " + dto.vehiculoId()));

            if (vehiculo.getProfesor() != null)
            {
                throw new ReglaNegocioException("El vehículo con matrícula " + vehiculo.getMatricula() + " ya se encuentra asignado a otro profesor.");
            }

            if (dto.permisos() == null || !dto.permisos().contains(vehiculo.getTipoPermiso()))
            {
                throw new ReglaNegocioException("El profesor no cuenta con el " + vehiculo.getTipoPermiso().getDescripcion()
                        + " requerido para conducir el vehículo asignado (" + vehiculo.getMatricula() + ").");
            }

            vehiculo.setEstado(EstadoVehiculo.OCUPADO);
        }

        Profesor profesor = this.profesorMapper.toProfesor(dto);
        profesor.setDni(dniLimpio);
        profesor.setCorreo(correoLimpio);
        profesor.setTelefono(telefonoLimpio);
        profesor.setEstado(EstadoUsuario.ACTIVO);

        if (profesor.getListaTiposCarnet() == null)
        {
            profesor.setListaTiposCarnet(new ArrayList<>());
        }

        if (vehiculo != null)
        {
            profesor.setVehiculo(vehiculo);
            vehiculo.setProfesor(profesor);
        }

        // Credenciales temporales hasta activación por el profesor (Regla 7.1)
        String provisionalUsername = "profe_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        profesor.setNombreUsuario(provisionalUsername);
        profesor.actualizarPassword(this.passwordEncoder.encode(UUID.randomUUID().toString()));

        profesor = this.profesorRepository.save(profesor);
        if (vehiculo != null)
        {
            this.vehiculoRepository.save(vehiculo);
        }

        // Emisión de token criptográfico temporal para activación (Regla 7.1)
        TokenVerificacion token = this.tokenVerificacionService.generarTokenRecuperacion(profesor, duracionTokenMinutos);
        String enlaceActivacion = (baseUrl != null ? baseUrl : "") + "/activar-cuenta?token=" + token.getToken();
        this.emailService.enviarInvitacionProfesor(profesor.getCorreo(), profesor.getNombre(), enlaceActivacion, String.valueOf(duracionTokenMinutos));

        return profesor;
    }

    /**
     * Obtiene la lista completa de profesores registrados en la base de datos
     * proyectados como DTOs de resumen para visualización reactiva en tiempo real.
     */
    @Transactional(readOnly = true)
    public List<ProfesorResumenDTO> obtenerTodosLosProfesores()
    {
        return this.profesorRepository.findAllByOrderByNombreAscApellidosAsc()
                .stream()
                .map(this.profesorMapper::toProfesorResumenDTO)
                .toList();
    }

    /**
     * Obtiene los datos del profesor mapeados a {@link EditarProfesorDTO} para inicializar
     * el formulario del modal de edición.
     *
     * @param id Identificador único del profesor.
     * @return DTO con todos los datos personales, contractuales y permisos.
     */
    @Transactional(readOnly = true)
    public EditarProfesorDTO obtenerProfesorParaEdicion(Long id)
    {
        if (id == null)
        {
            throw new ReglaNegocioException("El identificador del profesor no puede ser nulo.");
        }
        Profesor profesor = this.profesorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el profesor con ID: " + id));

        if (profesor.getEstado() != EstadoUsuario.ACTIVO)
        {
            throw new ReglaNegocioException("Solo se pueden editar profesores en estado ACTIVO. Los profesores inactivos deben ser reactivados previamente.");
        }

        return this.profesorMapper.toEditarProfesorDTO(profesor);
    }

    /**
     * Obtiene la totalidad de los datos informativos del profesor para su visualización
     * en el modal de detalle del panel de administración.
     *
     * @param id Identificador único del profesor.
     * @return DTO inmutable poblado con datos personales, contractuales, antigüedad y flota asignada.
     */
    @Transactional(readOnly = true)
    public ProfesorDetalleDTO obtenerProfesorParaDetalle(Long id)
    {
        if (id == null)
        {
            throw new ReglaNegocioException("El identificador del profesor no puede ser nulo.");
        }
        Profesor profesor = this.profesorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el profesor con ID: " + id));
        return this.profesorMapper.toProfesorDetalleDTO(profesor);
    }

    /**
     * Comprueba si un profesor cuenta con clases prácticas pendientes de impartir.
     *
     * @param id Identificador único del profesor.
     * @return true si tiene al menos una clase práctica en estado PENDIENTE, false en caso contrario.
     */
    @Transactional(readOnly = true)
    public boolean tieneClasesPracticasPendientes(Long id)
    {
        if (id == null)
        {
            return false;
        }
        Profesor profesor = this.profesorRepository.findById(id).orElse(null);
        if (profesor == null)
        {
            return false;
        }
        return this.clasePracticaRepository.countByProfesorAndEstadoClase(profesor, EstadoClase.PENDIENTE) > 0;
    }

    /**
     * Modifica los datos de un profesor existente aplicando las mismas restricciones de negocio
     * que en el alta, asegurando la unicidad de datos y la regla 7.3 (asignación 1 a 1 de flota y compatibilidad de carnet).
     *
     * @param dto Datos actualizados y validados sintácticamente.
     * @return Entidad Profesor actualizada.
     */
    @Transactional
    public Profesor modificarProfesor(EditarProfesorDTO dto)
    {
        if (dto == null || dto.id() == null)
        {
            throw new ReglaNegocioException("Los datos para la modificación del profesor no pueden ser nulos.");
        }

        Profesor profesor = this.profesorRepository.findById(dto.id())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el profesor con ID: " + dto.id()));

        if (profesor.getEstado() != EstadoUsuario.ACTIVO)
        {
            throw new ReglaNegocioException("Solo se pueden modificar profesores en estado ACTIVO. Los profesores inactivos deben ser reactivados previamente.");
        }

        if (dto.fechaNacimiento() == null || dto.fechaNacimiento().plusYears(18).isAfter(LocalDate.now()))
        {
            throw new ReglaNegocioException("El profesor debe ser mayor de edad (al menos 18 años).");
        }

        if (dto.fechaNacimiento().isBefore(LocalDate.now().minusYears(100)))
        {
            throw new ReglaNegocioException("La fecha de nacimiento no puede ser anterior a hace 100 años.");
        }

        if (dto.fechaContratacion() == null || !dto.fechaContratacion().isEqual(profesor.getFechaContratacion()))
        {
            throw new ReglaNegocioException("La fecha de contratación no puede ser modificada.");
        }

        if (dto.permisos() == null || dto.permisos().isEmpty())
        {
            throw new ReglaNegocioException("Debe seleccionar al menos un permiso de conducción autorizado.");
        }

        String dniLimpio = dto.dni().trim().toUpperCase();
        if (this.personaRepository.existsByDniAndIdNot(dniLimpio, dto.id()))
        {
            throw new ReglaNegocioException("Ya existe un usuario registrado con el DNI/NIE " + dniLimpio + ".");
        }

        String correoLimpio = dto.correo().trim().toLowerCase();
        if (this.personaRepository.existsByCorreoAndIdNot(correoLimpio, dto.id()))
        {
            throw new ReglaNegocioException("Ya existe un usuario registrado con el correo electrónico " + correoLimpio + ".");
        }

        String telefonoLimpio = dto.telefono().trim();
        if (this.personaRepository.existsByTelefonoAndIdNot(telefonoLimpio, dto.id()))
        {
            throw new ReglaNegocioException("Ya existe un usuario registrado con el número de teléfono " + telefonoLimpio + ".");
        }

        // Gestión de la asignación 1 a 1 de flota y bloqueo si existen clases prácticas pendientes (Regla 6.3)
        Vehiculo vehiculoActual = profesor.getVehiculo();
        Long vehiculoActualId = vehiculoActual != null ? vehiculoActual.getId() : null;
        Long nuevoVehiculoId = dto.vehiculoId();
        boolean vehiculoModificado = !Objects.equals(vehiculoActualId, nuevoVehiculoId);

        if (vehiculoModificado && this.clasePracticaRepository.countByProfesorAndEstadoClase(profesor, EstadoClase.PENDIENTE) > 0)
        {
            throw new ReglaNegocioException("No se puede modificar ni desvincular el vehículo de un profesor que tiene clases prácticas pendientes de impartir.");
        }

        if (nuevoVehiculoId == null)
        {
            // Desvincular vehículo si poseía uno previamente
            if (vehiculoActual != null)
            {
                vehiculoActual.setProfesor(null);
                vehiculoActual.setEstado(EstadoVehiculo.DISPONIBLE);
                this.vehiculoRepository.save(vehiculoActual);
                profesor.setVehiculo(null);
            }
        }
        else
        {
            if (vehiculoActual == null || !vehiculoActual.getId().equals(nuevoVehiculoId))
            {
                Vehiculo nuevoVehiculo = this.vehiculoRepository.findById(nuevoVehiculoId)
                        .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el vehículo con ID: " + nuevoVehiculoId));

                if (nuevoVehiculo.getProfesor() != null && !nuevoVehiculo.getProfesor().getId().equals(profesor.getId()))
                {
                    throw new ReglaNegocioException("El vehículo con matrícula " + nuevoVehiculo.getMatricula() + " ya se encuentra asignado a otro profesor.");
                }

                if (!dto.permisos().contains(nuevoVehiculo.getTipoPermiso()))
                {
                    throw new ReglaNegocioException("El profesor no cuenta con el " + nuevoVehiculo.getTipoPermiso().getDescripcion()
                            + " requerido para conducir el vehículo asignado (" + nuevoVehiculo.getMatricula() + ").");
                }

                // Liberar el vehículo previo
                if (vehiculoActual != null)
                {
                    vehiculoActual.setProfesor(null);
                    vehiculoActual.setEstado(EstadoVehiculo.DISPONIBLE);
                    this.vehiculoRepository.save(vehiculoActual);
                }

                nuevoVehiculo.setProfesor(profesor);
                nuevoVehiculo.setEstado(EstadoVehiculo.OCUPADO);
                this.vehiculoRepository.save(nuevoVehiculo);
                profesor.setVehiculo(nuevoVehiculo);
            }
            else
            {
                // Mantiene el mismo vehículo: verificar compatibilidad con los nuevos permisos
                if (!dto.permisos().contains(vehiculoActual.getTipoPermiso()))
                {
                    throw new ReglaNegocioException("El profesor no cuenta con el " + vehiculoActual.getTipoPermiso().getDescripcion()
                            + " requerido para conducir el vehículo asignado (" + vehiculoActual.getMatricula() + ").");
                }
            }
        }

        profesor.setNombre(dto.nombre().trim());
        profesor.setApellidos(dto.apellidos().trim());
        profesor.setDni(dniLimpio);
        profesor.setCorreo(correoLimpio);
        profesor.setTelefono(telefonoLimpio);
        profesor.setFechaNacimiento(dto.fechaNacimiento());
        profesor.setDireccion(dto.direccion().trim());
        profesor.setTurno(dto.turno());
        profesor.setListaTiposCarnet(new ArrayList<>(dto.permisos()));

        return this.profesorRepository.save(profesor);
    }

    /**
     * Tramita la baja lógica de un profesor conforme a la Regla de Negocio 7.3:
     * - Su estado pasa a INACTIVO preservando todo su historial.
     * - El vehículo asignado se libera y pasa a estado DISPONIBLE.
     * - Se cancelan automáticamente todas sus clases prácticas pendientes.
     * - Si posee alumnos asignados, se reasignan a otro profesor activo o se dejan temporalmente sin profesor, notificando en ambos casos a los alumnos por correo electrónico.
     *
     * @param dto Parámetros de la baja (profesorId, opcionAlumnos, nuevoProfesorId).
     * @return Entidad Profesor en estado INACTIVO.
     */
    @Transactional
    public Profesor darBajaProfesor(BajaProfesorDTO dto)
    {
        if (dto == null || dto.profesorId() == null)
        {
            throw new ReglaNegocioException("El identificador del profesor es obligatorio para tramitar la baja.");
        }

        Profesor profesor = this.profesorRepository.findById(dto.profesorId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el profesor con ID: " + dto.profesorId()));

        if (profesor.getEstado() == EstadoUsuario.INACTIVO)
        {
            throw new ReglaNegocioException("El profesor " + profesor.getNombre() + " " + profesor.getApellidos() + " ya se encuentra en estado INACTIVO.");
        }

        // Liberamos el vehículo asignado si lo tuviese (Regla 7.3)
        Vehiculo vehiculoActual = profesor.getVehiculo();
        if (vehiculoActual != null)
        {
            vehiculoActual.setProfesor(null);
            vehiculoActual.setEstado(EstadoVehiculo.DISPONIBLE);
            this.vehiculoRepository.save(vehiculoActual);
            profesor.setVehiculo(null);
        }

        // Cancelamos clases prácticas pendientes del profesor directamente desde la BD (Regla 7.3)
        List<ClasePractica> clasesPendientes = this.clasePracticaRepository.findByProfesorAndEstadoClase(profesor, EstadoClase.PENDIENTE);

        for (ClasePractica clase : clasesPendientes)
        {
            clase.setEstadoClase(EstadoClase.CANCELADA);
            this.clasePracticaRepository.save(clase);
        }

        // Gestionamos los alumnos del profesor que vamos a dar de baja
        List<Alumno> alumnosAsignados = this.alumnoRepository.findByProfesor(profesor);
        if (!alumnosAsignados.isEmpty())
        {
            String opcion = dto.opcionAlumnos() != null ? dto.opcionAlumnos().trim().toUpperCase() : "";

            if ("REASIGNAR".equals(opcion))
            {
                if (dto.nuevoProfesorId() == null)
                {
                    throw new ReglaNegocioException("Debe seleccionar un profesor activo para reasignar los alumnos.");
                }

                if (dto.nuevoProfesorId().equals(profesor.getId()))
                {
                    throw new ReglaNegocioException("No se pueden reasignar los alumnos al mismo profesor que causa baja.");
                }

                Profesor nuevoProfesor = this.profesorRepository.findById(dto.nuevoProfesorId())
                        .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el profesor sustituto con ID: " + dto.nuevoProfesorId()));

                if (nuevoProfesor.getEstado() != EstadoUsuario.ACTIVO)
                {
                    throw new ReglaNegocioException("El profesor seleccionado para la reasignación debe estar en estado ACTIVO.");
                }

                if (vehiculoActual != null && vehiculoActual.getTipoPermiso() != null)
                {
                    TipoCarnet permisoRequerido = vehiculoActual.getTipoPermiso();
                    if (nuevoProfesor.getListaTiposCarnet() == null || !nuevoProfesor.getListaTiposCarnet().contains(permisoRequerido))
                    {
                        throw new ReglaNegocioException("El profesor seleccionado no dispone del carnet "
                                + permisoRequerido.getDescripcion() + " requerido para la formación de los alumnos asignados.");
                    }
                }

                String nombreProfesorBaja = profesor.getNombre() + " " + profesor.getApellidos();
                String nombreNuevoProfesor = nuevoProfesor.getNombre() + " " + nuevoProfesor.getApellidos();

                for (Alumno alumno : alumnosAsignados)
                {
                    alumno.setProfesor(nuevoProfesor);
                    this.alumnoRepository.save(alumno);

                    String asunto = "Aviso importante: Asignación de nuevo profesor de prácticas";
                    String mensaje = "Estimado/a " + alumno.getNombre() + ",\n\n"
                            + "Te comunicamos que tu profesor/a " + nombreProfesorBaja + " ha causado baja en la autoescuela.\n"
                            + "A partir de este momento, tu nuevo profesor asignado es " + nombreNuevoProfesor + ".\n\n"
                            + "Conforme al protocolo del centro, todas tus clases prácticas pendientes han sido canceladas automáticamente. "
                            + "Te invitamos a acceder a tu panel de alumno para programar tus próximas sesiones prácticas con tu nuevo profesor.\n\n"
                            + "Un cordial saludo,\n"
                            + "Equipo de Coordinación - ManDS Autoescuela";

                    this.emailService.enviarNotificacionAlumno(alumno.getCorreo(), asunto, mensaje);
                }
            }
            else
            {
                if ("SIN_PROFESOR".equals(opcion))
                {
                    String nombreProfesorBaja = profesor.getNombre() + " " + profesor.getApellidos();

                    for (Alumno alumno : alumnosAsignados)
                    {
                        alumno.setProfesor(null);
                        this.alumnoRepository.save(alumno);

                        String asunto = "Aviso importante: Baja de tu profesor de prácticas";
                        String mensaje = "Estimado/a " + alumno.getNombre() + ",\n\n"
                                + "Te comunicamos que tu profesor/a " + nombreProfesorBaja + " ha causado baja en la autoescuela.\n"
                                + "Actualmente tu expediente queda temporalmente sin profesor asignado y tus clases prácticas pendientes "
                                + "han sido canceladas de forma automática.\n\n"
                                + "El equipo de administración te asignará un nuevo profesor a la mayor brevedad posible para que puedas retomar tus clases.\n\n"
                                + "Un cordial saludo,\n"
                                + "Equipo de Coordinación - ManDS Autoescuela";

                        this.emailService.enviarNotificacionAlumno(alumno.getCorreo(), asunto, mensaje);
                    }
                }
                else
                {
                    throw new ReglaNegocioException("Debe seleccionar una opción válida para la gestión de los alumnos (Reasignar o Dejar sin profesor).");
                }
            }
        }

        // Cambiamos el estado del profesor a INACTIVO
        profesor.setEstado(EstadoUsuario.INACTIVO);
        return this.profesorRepository.save(profesor);
    }

    /**
     * Vuelve a dar de alta a un profesor inactivo (reactivación):
     * - Su estado pasa a ACTIVO.
     * - Se garantiza que NO se le asigna ningún vehículo (vehiculo = null).
     *   Para asignarle un vehículo, el administrador deberá editarlo posteriormente.
     *
     * @param id Identificador único del profesor.
     * @return Entidad Profesor reactivada en estado ACTIVO.
     */
    @Transactional
    public Profesor reactivarProfesor(Long id)
    {
        if (id == null)
        {
            throw new ReglaNegocioException("El identificador del profesor es obligatorio para la reactivación.");
        }

        Profesor profesor = this.profesorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el profesor con ID: " + id));

        if (profesor.getEstado() == EstadoUsuario.ACTIVO)
        {
            throw new ReglaNegocioException("El profesor " + profesor.getNombre() + " " + profesor.getApellidos() + " ya se encuentra en estado ACTIVO.");
        }

        profesor.setEstado(EstadoUsuario.ACTIVO);
        profesor.setVehiculo(null);

        return this.profesorRepository.save(profesor);
    }

    /**
     * Recupera el listado de profesores activos excluyendo al profesor indicado,
     * ordenados ascendentemente de menor a mayor número de alumnos asignados
     * para favorecer el balanceo de carga de alumnos al reasignar los alumnos del profesor dado de baja.
     *
     * @param profesorId Identificador del profesor a excluir.
     * @return Lista de DTOs de resumen de los profesores activos disponibles ordenados por carga de alumnos.
     */
    @Transactional(readOnly = true)
    public List<ProfesorResumenDTO> obtenerProfesoresActivosExcluyendo(Long profesorId)
    {
        List<Profesor> profesores = (profesorId != null)
                ? this.profesorRepository.findActivosExcluyendoIdOrderByAlumnosAsc(profesorId)
                : this.profesorRepository.findActivosOrderByAlumnosAsc();

        return profesores.stream()
                .map(this.profesorMapper::toProfesorResumenDTO)
                .toList();
    }

    /**
     * Recupera el listado de profesores activos que cuentan con la habilitación para el tipo de carnet indicado,
     * excluyendo al profesor actual si procede, y ordenados de menor a mayor número de alumnos asignados
     * (con desempate alfabético por nombre completo) para favorecer el balanceo de carga de alumnos.
     *
     * @param tipoCarnet Permiso de conducción requerido conforme a la matrícula del alumno.
     * @param profesorId Identificador del profesor a excluir (por ejemplo, el actual del alumno).
     * @return Lista ordenada de DTOs de resumen de los profesores activos habilitados.
     */
    @Transactional(readOnly = true)
    public List<ProfesorResumenDTO> obtenerProfesoresActivosPorCarnetExcluyendo(TipoCarnet tipoCarnet, Long profesorId)
    {
        if (tipoCarnet == null)
        {
            return List.of();
        }

        List<Profesor> profesores = (profesorId != null)
                ? this.profesorRepository.findActivosPorCarnetExcluyendoIdOrderByAlumnosAsc(tipoCarnet, profesorId)
                : this.profesorRepository.findActivosPorCarnetOrderByAlumnosAsc(tipoCarnet);

        return profesores.stream()
                .map(this.profesorMapper::toProfesorResumenDTO)
                .toList();
    }

    /**
     * Cuenta el número total de profesores en plantilla.
     */
    @Transactional(readOnly = true)
    public long contarTotalProfesores()
    {
        return this.profesorRepository.count();
    }

    /**
     * Cuenta el número de profesores adscritos a un turno determinado.
     */
    @Transactional(readOnly = true)
    public long contarProfesoresPorTurno(TipoTurno turno)
    {
        return this.profesorRepository.countByTurno(turno);
    }

    /**
     * Cuenta el número de clases prácticas en estado PENDIENTE que tiene actualmente un profesor.
     *
     * @param profesorId Identificador del profesor.
     * @return Total de clases prácticas pendientes de impartición.
     */
    @Transactional(readOnly = true)
    public int contarClasesPendientes(Long profesorId)
    {
        if (profesorId == null)
        {
            return 0;
        }
        Profesor profesor = this.profesorRepository.findById(profesorId).orElse(null);
        if (profesor == null)
        {
            return 0;
        }
        return this.clasePracticaRepository.countByProfesorAndEstadoClase(profesor, EstadoClase.PENDIENTE);
    }
}
