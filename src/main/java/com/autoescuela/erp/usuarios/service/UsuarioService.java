package com.autoescuela.erp.usuarios.service;

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

        String telefonoLimpio = dto.getTelefono().trim();
        if (this.personaRepository.existsByTelefonoAndIdNot(telefonoLimpio, personaId))
        {
            throw new ReglaNegocioException("El número de teléfono ya se encuentra registrado por otro usuario.");
        }

        persona.setNombre(dto.getNombre().trim());
        persona.setApellidos(dto.getApellidos().trim());
        persona.setTelefono(telefonoLimpio);
        persona.setDireccion(dto.getDireccion().trim());
        persona.setFechaNacimiento(dto.getFechaNacimiento());

        this.personaRepository.save(persona);
    }
}
