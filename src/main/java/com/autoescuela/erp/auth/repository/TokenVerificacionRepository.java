package com.autoescuela.erp.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autoescuela.erp.auth.model.TokenVerificacion;
import com.autoescuela.erp.usuarios.model.Persona;

public interface TokenVerificacionRepository extends JpaRepository<TokenVerificacion, Long>
{
    /**
     * Busca un token de verificación por su cadena de texto única.
     *
     * @param token Cadena UUID del token.
     * @return Optional con el TokenVerificacion si existe.
     */
    Optional<TokenVerificacion> findByToken(String token);

    /**
     * Busca un token de verificación que no haya sido consumido todavía.
     *
     * @param token Cadena UUID del token.
     * @return Optional con el TokenVerificacion activo si existe.
     */
    Optional<TokenVerificacion> findByTokenAndUsadoFalse(String token);

    /**
     * Elimina todos los tokens previos asociados a una persona.
     *
     * @param persona Entidad persona titular de los tokens.
     */
    void deleteByPersona(Persona persona);
}
