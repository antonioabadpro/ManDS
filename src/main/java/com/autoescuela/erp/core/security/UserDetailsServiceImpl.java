package com.autoescuela.erp.core.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autoescuela.erp.usuarios.model.Persona;
import com.autoescuela.erp.usuarios.repository.PersonaRepository;

import lombok.RequiredArgsConstructor;

/**
 * Servicio de autenticación que carga los detalles del usuario para Spring Security.
 * Soporta autenticación mediante nombre de usuario o correo electrónico según la regla 7.1.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService
{
    private final PersonaRepository personaRepository;

    /**
     * Carga los detalles del usuario por nombre de usuario o correo electrónico.
     * @param identificador El nombre de usuario o correo electrónico del usuario.
     * @return Los detalles del usuario para Spring Security.
     * @throws UsernameNotFoundException Si no se encuentra ningún usuario con el identificador proporcionado.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String identificador) throws UsernameNotFoundException
    {
        Persona persona = personaRepository.findByNombreUsuarioOrCorreo(identificador, identificador)
                .orElseThrow(() -> new UsernameNotFoundException("No se encontró ningún usuario con el identificador: " + identificador));

        return new UserDetailsImpl(persona);
    }
}
