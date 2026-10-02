package com.autoescuela.erp.usuarios.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autoescuela.erp.core.excepciones.RecursoNoEncontradoException;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.usuarios.dto.EditarPerfilAdminDTO;
import com.autoescuela.erp.usuarios.mapper.UsuarioMapper;
import com.autoescuela.erp.usuarios.model.Persona;
import com.autoescuela.erp.usuarios.repository.PersonaRepository;

import lombok.RequiredArgsConstructor;

/**
 * Servicio transversal para la consulta y gestión de perfiles de usuarios.
 */
@Service
@RequiredArgsConstructor
public class UsuarioService
{
    private final PersonaRepository personaRepository;
    private final UsuarioMapper usuarioMapper;

    /**
     * Comprueba si un nombre de usuario ya está registrado en el sistema.
     *
     * @param nombreUsuario Nombre de usuario a comprobar.
     * @return true si ya existe, false en caso contrario.
     */
    @Transactional(readOnly = true)
    public boolean existeNombreUsuario(String nombreUsuario)
    {
        if (nombreUsuario == null || nombreUsuario.isBlank())
        {
            return false;
        }
        return this.personaRepository.existsByNombreUsuario(nombreUsuario.trim());
    }

    /**
     * Comprueba si una dirección de correo electrónico ya está registrada en el sistema.
     *
     * @param correo Correo electrónico a comprobar.
     * @return true si ya existe, false en caso contrario.
     */
    @Transactional(readOnly = true)
    public boolean existeCorreo(String correo)
    {
        if (correo == null || correo.isBlank())
        {
            return false;
        }
        return this.personaRepository.existsByCorreo(correo.trim().toLowerCase());
    }

    /**
     * Comprueba si un correo ya está registrado por otro usuario (distinto ID).
     */
    @Transactional(readOnly = true)
    public boolean existeCorreoOtroUsuario(String correo, Long id)
    {
        if (correo == null || correo.isBlank())
        {
            return false;
        }
        if (id == null)
        {
            return existeCorreo(correo);
        }
        return this.personaRepository.existsByCorreoAndIdNot(correo.trim().toLowerCase(), id);
    }

    /**
     * Comprueba si un DNI/NIE ya está registrado en el sistema.
     *
     * @param dni DNI/NIE a comprobar.
     * @return true si ya existe, false en caso contrario.
     */
    @Transactional(readOnly = true)
    public boolean existeDni(String dni)
    {
        if (dni == null || dni.isBlank())
        {
            return false;
        }
        return this.personaRepository.existsByDni(dni.trim().toUpperCase());
    }

    /**
     * Comprueba si un DNI ya está registrado por otro usuario (distinto ID).
     */
    @Transactional(readOnly = true)
    public boolean existeDniOtroUsuario(String dni, Long id)
    {
        if (dni == null || dni.isBlank())
        {
            return false;
        }
        if (id == null)
        {
            return existeDni(dni);
        }
        return this.personaRepository.existsByDniAndIdNot(dni.trim().toUpperCase(), id);
    }

    /**
     * Comprueba si un número de teléfono ya está registrado en el sistema.
     *
     * @param telefono Número de teléfono a comprobar.
     * @return true si ya existe, false en caso contrario.
     */
    @Transactional(readOnly = true)
    public boolean existeTelefono(String telefono)
    {
        if (telefono == null || telefono.isBlank())
        {
            return false;
        }
        return this.personaRepository.existsByTelefono(telefono.trim());
    }

    /**
     * Comprueba si un teléfono ya está registrado por otro usuario (distinto ID).
     */
    @Transactional(readOnly = true)
    public boolean existeTelefonoOtroUsuario(String telefono, Long id)
    {
        if (telefono == null || telefono.isBlank())
        {
            return false;
        }
        if (id == null)
        {
            return existeTelefono(telefono);
        }
        return this.personaRepository.existsByTelefonoAndIdNot(telefono.trim(), id);
    }

    /**
     * Obtiene los datos del perfil de un usuario para su edición o visualización.
     *
     * @param personaId Identificador único del usuario.
     * @return DTO poblado con los datos personales actuales.
     * @throws RecursoNoEncontradoException Si no existe el usuario.
     */
    @Transactional(readOnly = true)
    public EditarPerfilAdminDTO obtenerPerfilAdmin(Long personaId)
    {
        Persona persona = this.personaRepository.findById(personaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el usuario con ID: " + personaId));

        return this.usuarioMapper.toEditarPerfilAdminDTO(persona);
    }

    /**
     * Obtiene los datos del perfil de un usuario a partir de su nombre de usuario o correo.
     */
    @Transactional(readOnly = true)
    public EditarPerfilAdminDTO obtenerPerfilAdminPorUsername(String username)
    {
        Persona persona = this.personaRepository.findByNombreUsuarioOrCorreo(username, username)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el usuario: " + username));

        return this.usuarioMapper.toEditarPerfilAdminDTO(persona);
    }

    /**
     * Actualiza los datos personales modificables del perfil del Administrador.
     *
     * @param personaId Identificador único del usuario autenticado.
     * @param dto Datos recibidos desde el formulario de perfil.
     * @throws ReglaNegocioException Si el teléfono ya pertenece a otro usuario.
     * @throws RecursoNoEncontradoException Si el usuario no existe.
     */
    @Transactional
    public void actualizarPerfilAdmin(Long personaId, EditarPerfilAdminDTO dto)
    {
        Persona persona = this.personaRepository.findById(personaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el usuario con ID: " + personaId));

        if (dto.getDni() == null || dto.getDni().isBlank())
        {
            throw new ReglaNegocioException("El DNI/NIE es obligatorio.");
        }

        String dniLimpio = dto.getDni().trim().toUpperCase();
        if (this.personaRepository.existsByDniAndIdNot(dniLimpio, personaId))
        {
            throw new ReglaNegocioException("El DNI/NIE ya se encuentra registrado por otro usuario.");
        }

        String telefonoLimpio = dto.getTelefono().trim();
        if (this.personaRepository.existsByTelefonoAndIdNot(telefonoLimpio, personaId))
        {
            throw new ReglaNegocioException("El número de teléfono ya se encuentra registrado por otro usuario.");
        }

        if (dto.getFechaNacimiento() != null)
        {
            LocalDate hoy = LocalDate.now();
            if (dto.getFechaNacimiento().plusYears(18).isAfter(hoy))
            {
                throw new ReglaNegocioException("El administrador debe ser mayor de edad (al menos 18 años).");
            }
            if (dto.getFechaNacimiento().isBefore(hoy.minusYears(100)))
            {
                throw new ReglaNegocioException("La fecha de nacimiento no puede ser anterior a hace 100 años.");
            }
        }

        persona.setDni(dniLimpio);
        persona.setNombre(dto.getNombre().trim());
        persona.setApellidos(dto.getApellidos().trim());
        persona.setTelefono(telefonoLimpio);
        persona.setDireccion(dto.getDireccion().trim());
        persona.setFechaNacimiento(dto.getFechaNacimiento());

        this.personaRepository.save(persona);
    }
}
