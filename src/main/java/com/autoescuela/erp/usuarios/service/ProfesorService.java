package com.autoescuela.erp.usuarios.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autoescuela.erp.auth.model.TokenVerificacion;
import com.autoescuela.erp.auth.service.TokenVerificacionService;
import com.autoescuela.erp.core.email.EmailService;
import com.autoescuela.erp.core.enums.EstadoUsuario;
import com.autoescuela.erp.core.enums.EstadoVehiculo;
import com.autoescuela.erp.core.enums.TipoTurno;
import com.autoescuela.erp.core.excepciones.RecursoNoEncontradoException;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.flota.model.Vehiculo;
import com.autoescuela.erp.flota.repository.VehiculoRepository;
import com.autoescuela.erp.usuarios.dto.AltaProfesorDTO;
import com.autoescuela.erp.usuarios.dto.EditarProfesorDTO;
import com.autoescuela.erp.usuarios.dto.ProfesorResumenDTO;
import com.autoescuela.erp.usuarios.mapper.ProfesorMapper;
import com.autoescuela.erp.usuarios.model.Profesor;
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
                throw new ReglaNegocioException("El vehículo con matrícula " + vehiculo.getMatricula() + " ya se encuentra asignado a otro docente.");
            }

            if (dto.permisos() == null || !dto.permisos().contains(vehiculo.getTipo()))
            {
                throw new ReglaNegocioException("El profesor no cuenta con el " + vehiculo.getTipo().getDescripcion()
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
        return this.profesorMapper.toEditarProfesorDTO(profesor);
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

        if (dto.fechaNacimiento() == null || dto.fechaNacimiento().plusYears(18).isAfter(LocalDate.now()))
        {
            throw new ReglaNegocioException("El profesor debe ser mayor de edad (al menos 18 años).");
        }

        if (dto.fechaNacimiento().isBefore(LocalDate.now().minusYears(100)))
        {
            throw new ReglaNegocioException("La fecha de nacimiento no puede ser anterior a hace 100 años.");
        }

        if (dto.fechaContratacion() == null || dto.fechaContratacion().isBefore(dto.fechaNacimiento().plusYears(18)))
        {
            throw new ReglaNegocioException("La fecha de contratación no puede ser anterior a la fecha en que el profesor cumplió la mayoría de edad.");
        }

        if (dto.fechaContratacion().isAfter(LocalDate.now().plusMonths(1)))
        {
            throw new ReglaNegocioException("La fecha de contratación no puede situarse a más de 1 mes en el futuro.");
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

        // Gestión de la asignación 1 a 1 de flota (Regla 7.3)
        Vehiculo vehiculoActual = profesor.getVehiculo();
        Long nuevoVehiculoId = dto.vehiculoId();

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
                    throw new ReglaNegocioException("El vehículo con matrícula " + nuevoVehiculo.getMatricula() + " ya se encuentra asignado a otro docente.");
                }

                if (!dto.permisos().contains(nuevoVehiculo.getTipo()))
                {
                    throw new ReglaNegocioException("El profesor no cuenta con el " + nuevoVehiculo.getTipo().getDescripcion()
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
                if (!dto.permisos().contains(vehiculoActual.getTipo()))
                {
                    throw new ReglaNegocioException("El profesor no cuenta con el " + vehiculoActual.getTipo().getDescripcion()
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
        profesor.setFechaContratacion(dto.fechaContratacion());
        profesor.setTurno(dto.turno());
        profesor.setListaTiposCarnet(new ArrayList<>(dto.permisos()));

        return this.profesorRepository.save(profesor);
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
}
