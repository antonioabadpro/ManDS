package com.autoescuela.erp.auth;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.autoescuela.erp.auth.model.TokenVerificacion;
import com.autoescuela.erp.auth.repository.TokenVerificacionRepository;
import com.autoescuela.erp.auth.service.TokenVerificacionService;
import com.autoescuela.erp.usuarios.model.Persona;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para {@link TokenVerificacionService}.
 * TokenVerificacionServiceTest contiene pruebas para la generación de tokens de verificación, validación de tokens y marcado de tokens como usados.
 * Se utilizan mocks para simular el comportamiento del repositorio de tokens y se verifican los resultados esperados en cada caso.
 */
@ExtendWith(MockitoExtension.class)
class TokenVerificacionServiceTest
{
    @Mock
    private TokenVerificacionRepository tokenVerificacionRepository;

    @InjectMocks
    private TokenVerificacionService tokenVerificacionService;

    @Test
    @DisplayName("generarTokenRecuperacion() crea un token con UUID y 15 minutos de caducidad")
    void testGenerarTokenRecuperacion()
    {
        Persona persona = mock(Persona.class);
        when(this.tokenVerificacionRepository.save(any(TokenVerificacion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TokenVerificacion resultado = this.tokenVerificacionService.generarTokenRecuperacion(persona, 15);

        assertNotNull(resultado);
        assertNotNull(resultado.getToken());
        assertEquals(persona, resultado.getPersona());
        assertFalse(resultado.isUsado());
        assertTrue(resultado.getFechaExpiracion().isAfter(LocalDateTime.now().plusMinutes(14)));
        assertTrue(resultado.getFechaExpiracion().isBefore(LocalDateTime.now().plusMinutes(16)));
        assertTrue(resultado.isValido());
    }

    @Test
    @DisplayName("generarTokenRecuperacion() con persona nula lanza IllegalArgumentException")
    void testGenerarTokenPersonaNula()
    {
        assertThrows(IllegalArgumentException.class, () ->
                this.tokenVerificacionService.generarTokenRecuperacion(null, 15)
        );
    }

    @Test
    @DisplayName("generarTokenRecuperacion() con duración de 60 minutos asigna correctamente el tiempo de expiración")
    void testGenerarTokenRecuperacionDuracion60Minutos()
    {
        Persona persona = mock(Persona.class);
        when(this.tokenVerificacionRepository.save(any(TokenVerificacion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TokenVerificacion resultado = this.tokenVerificacionService.generarTokenRecuperacion(persona, 60);

        assertNotNull(resultado);
        assertEquals(persona, resultado.getPersona());
        assertFalse(resultado.isUsado());
        assertTrue(resultado.getFechaExpiracion().isAfter(LocalDateTime.now().plusMinutes(59)));
        assertTrue(resultado.getFechaExpiracion().isBefore(LocalDateTime.now().plusMinutes(61)));
        assertTrue(resultado.isValido());
    }

    @Test
    @DisplayName("generarTokenRecuperacion() con duración menor o igual a cero lanza IllegalArgumentException")
    void testGenerarTokenDuracionInvalida()
    {
        Persona persona = mock(Persona.class);

        assertThrows(IllegalArgumentException.class, () ->
                this.tokenVerificacionService.generarTokenRecuperacion(persona, 0)
        );

        assertThrows(IllegalArgumentException.class, () ->
                this.tokenVerificacionService.generarTokenRecuperacion(persona, -10)
        );
    }

    @Test
    @DisplayName("obtenerTokenValido() devuelve el token si no está usado ni expirado")
    void testObtenerTokenValidoExito()
    {
        Persona persona = mock(Persona.class);
        TokenVerificacion token = new TokenVerificacion("token-valido-123", persona, LocalDateTime.now().plusMinutes(10));
        when(this.tokenVerificacionRepository.findByTokenAndUsadoFalse("token-valido-123"))
                .thenReturn(Optional.of(token));

        Optional<TokenVerificacion> resultado = this.tokenVerificacionService.obtenerTokenValido("token-valido-123");

        assertTrue(resultado.isPresent());
        assertEquals("token-valido-123", resultado.get().getToken());
    }

    @Test
    @DisplayName("obtenerTokenValido() devuelve Optional vacío si el token ya expiró")
    void testObtenerTokenValidoExpirado()
    {
        Persona persona = mock(Persona.class);
        TokenVerificacion tokenExpirado = new TokenVerificacion("token-caducado", persona, LocalDateTime.now().minusMinutes(1));
        when(this.tokenVerificacionRepository.findByTokenAndUsadoFalse("token-caducado"))
                .thenReturn(Optional.of(tokenExpirado));

        Optional<TokenVerificacion> resultado = this.tokenVerificacionService.obtenerTokenValido("token-caducado");

        assertFalse(resultado.isPresent());
    }

    @Test
    @DisplayName("obtenerTokenValido() devuelve Optional vacío con cadenas nulas o en blanco sin consultar BD")
    void testObtenerTokenValidoNuloOBlanco()
    {
        assertFalse(this.tokenVerificacionService.obtenerTokenValido(null).isPresent());
        assertFalse(this.tokenVerificacionService.obtenerTokenValido("   ").isPresent());

        verify(this.tokenVerificacionRepository, never()).findByTokenAndUsadoFalse(any());
    }

    @Test
    @DisplayName("esTokenValido() devuelve true para token vigente y false para caducado o inexistente")
    void testEsTokenValido()
    {
        Persona persona = mock(Persona.class);
        TokenVerificacion tokenOk = new TokenVerificacion("token-ok", persona, LocalDateTime.now().plusMinutes(10));
        when(this.tokenVerificacionRepository.findByTokenAndUsadoFalse("token-ok")).thenReturn(Optional.of(tokenOk));
        when(this.tokenVerificacionRepository.findByTokenAndUsadoFalse("token-ko")).thenReturn(Optional.empty());

        assertTrue(this.tokenVerificacionService.esTokenValido("token-ok"));
        assertFalse(this.tokenVerificacionService.esTokenValido("token-ko"));
    }

    @Test
    @DisplayName("marcarComoUsado() establece usado a true y persiste la entidad")
    void testMarcarComoUsado()
    {
        Persona persona = mock(Persona.class);
        TokenVerificacion token = new TokenVerificacion("token-a-usar", persona, LocalDateTime.now().plusMinutes(10));

        this.tokenVerificacionService.marcarComoUsado(token);

        assertTrue(token.isUsado());
        verify(this.tokenVerificacionRepository).save(token);
    }
}

