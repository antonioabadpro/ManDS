package com.autoescuela.erp.usuarios.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autoescuela.erp.auth.model.TokenVerificacion;
import com.autoescuela.erp.auth.service.TokenVerificacionService;
import com.autoescuela.erp.core.email.EmailService;
import com.autoescuela.erp.core.enums.EstadoUsuario;
import com.autoescuela.erp.core.enums.EstadoVehiculo;
import com.autoescuela.erp.core.excepciones.RecursoNoEncontradoException;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.flota.model.Vehiculo;
import com.autoescuela.erp.flota.repository.VehiculoRepository;
import com.autoescuela.erp.usuarios.dto.AltaProfesorDTO;
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
        if (dto == null)
        {
            throw new ReglaNegocioException("Los datos para el alta del profesor no pueden ser nulos.");
        }

        if (dto.getFechaNacimiento() == null || dto.getFechaNacimiento().plusYears(18).isAfter(LocalDate.now()))
        {
            throw new ReglaNegocioException("El profesor debe ser mayor de edad (al menos 18 años).");
        }

        if (dto.getPermisos() == null || dto.getPermisos().isEmpty())
        {
            throw new ReglaNegocioException("Debe seleccionar al menos un permiso de conducción autorizado.");
        }

        String dniLimpio = dto.getDni().trim().toUpperCase();
        if (this.personaRepository.existsByDni(dniLimpio))
        {
            throw new ReglaNegocioException("Ya existe un usuario registrado con el DNI/NIE " + dniLimpio + ".");
        }

        String correoLimpio = dto.getCorreo().trim().toLowerCase();
        if (this.personaRepository.existsByCorreo(correoLimpio))
        {
            throw new ReglaNegocioException("Ya existe un usuario registrado con el correo electrónico " + correoLimpio + ".");
        }

        String telefonoLimpio = dto.getTelefono().trim();
        if (this.personaRepository.existsByTelefono(telefonoLimpio))
        {
            throw new ReglaNegocioException("Ya existe un usuario registrado con el número de teléfono " + telefonoLimpio + ".");
        }

        Vehiculo vehiculo = null;
        if (dto.getVehiculoId() != null)
        {
            vehiculo = this.vehiculoRepository.findById(dto.getVehiculoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el vehículo con ID: " + dto.getVehiculoId()));

            if (vehiculo.getProfesor() != null)
            {
                throw new ReglaNegocioException("El vehículo con matrícula " + vehiculo.getMatricula() + " ya se encuentra asignado a otro docente.");
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
        TokenVerificacion token = this.tokenVerificacionService.generarTokenRecuperacion(profesor);
        String enlaceActivacion = (baseUrl != null ? baseUrl : "") + "/activar-cuenta?token=" + token.getToken();
        this.emailService.enviarInvitacionProfesor(profesor.getCorreo(), profesor.getNombre(), enlaceActivacion);

        return profesor;
    }
}
