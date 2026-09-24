package com.autoescuela.erp.auth.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autoescuela.erp.auth.model.TokenVerificacion;
import com.autoescuela.erp.auth.repository.TokenVerificacionRepository;
import com.autoescuela.erp.usuarios.model.Persona;

import lombok.RequiredArgsConstructor;

/**
 * Servicio de negocio para la emisión, validación e invalidación de tokens de verificación.
 */
@Service
@RequiredArgsConstructor
public class TokenVerificacionService
{
    private final TokenVerificacionRepository tokenVerificacionRepository;

    /**
     * Genera un nuevo token criptográfico efímero de verificación para la persona indicada.
     * Establece la expiración según los minutos indicados (p. ej. 15 minutos para recuperación o 60 minutos para activación docente).
     *
     * @param persona Persona destinataria del token.
     * @param duracionMinutos Duración en minutos antes de que el token caduque.
     * @return TokenVerificacion persistido en base de datos.
     */
    @Transactional
    public TokenVerificacion generarTokenRecuperacion(Persona persona, int duracionMinutos)
    {
        if (persona == null)
        {
            throw new IllegalArgumentException("No se puede generar un token para una persona nula.");
        }
        if (duracionMinutos <= 0)
        {
            throw new IllegalArgumentException("La duración del token debe ser un valor positivo.");
        }

        String uuid = UUID.randomUUID().toString();
        LocalDateTime fechaExpiracion = LocalDateTime.now().plusMinutes(duracionMinutos);

        TokenVerificacion nuevoToken = new TokenVerificacion(uuid, persona, fechaExpiracion);
        return this.tokenVerificacionRepository.save(nuevoToken);
    }

    /**
     * Busca y valida un token de verificación.
     * Comprueba que exista, no haya sido utilizado previamente y no haya expirado.
     *
     * @param token Cadena UUID del token a verificar.
     * @return Optional con el TokenVerificacion si es plenamente válido, o vacío en caso contrario.
     */
    @Transactional(readOnly = true)
    public Optional<TokenVerificacion> obtenerTokenValido(String token)
    {
        if (token == null || token.isBlank())
        {
            return Optional.empty();
        }

        return this.tokenVerificacionRepository.findByTokenAndUsadoFalse(token.trim())
                .filter(t -> t.isValido());
    }

    /**
     * Comprueba de manera booleana si un token de verificación es utilizable actualmente.
     *
     * @param token Cadena UUID del token.
     * @return true si el token existe, está activo y no ha caducado, y false en caso contrario.
     */
    @Transactional(readOnly = true)
    public boolean esTokenValido(String token)
    {
        return obtenerTokenValido(token).isPresent();
    }

    /**
     * Marca un token de verificación como consumido/usado.
     *
     * @param tokenVerificacion Instancia del token a invalidar.
     */
    @Transactional
    public void marcarComoUsado(TokenVerificacion tokenVerificacion)
    {
        if (tokenVerificacion != null)
        {
            tokenVerificacion.marcarComoUsado();
            this.tokenVerificacionRepository.save(tokenVerificacion);
        }
    }
}
