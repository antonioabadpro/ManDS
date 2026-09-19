package com.autoescuela.erp.auth;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.autoescuela.erp.auth.dto.RegistroAlumnoDTO;
import com.autoescuela.erp.auth.service.AuthenticationService;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.core.security.UserDetailsImpl;
import com.autoescuela.erp.usuarios.model.Persona;
import com.autoescuela.erp.usuarios.repository.PersonaRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;

import java.time.LocalDateTime;

import com.autoescuela.erp.auth.dto.RestablecerPasswordDTO;
import com.autoescuela.erp.auth.model.TokenVerificacion;
import com.autoescuela.erp.auth.service.TokenVerificacionService;
import com.autoescuela.erp.core.email.EmailService;
import com.autoescuela.erp.core.enums.EstadoUsuario;

/**
 * Pruebas unitarias para {@link AuthenticationService}.
 * AutenticacionServiceTest contiene pruebas para la verificación de autenticación, obtención de detalles del usuario, cierre de sesión, registro de alumnos y recuperación/restablecimiento de contraseña.
 * Se utilizan mocks para simular el comportamiento de los repositorios y servicios dependientes, y se verifican los resultados esperados en cada caso.
 */
import org.springframework.security.crypto.password.PasswordEncoder;
import com.autoescuela.erp.auth.mapper.AuthenticationMapper;
import com.autoescuela.erp.usuarios.repository.AlumnoRepository;

@ExtendWith(MockitoExtension.class)
class AutenticacionServiceTest
{
    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private AlumnoRepository alumnoRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationMapper authenticationMapper;

    @Mock
    private TokenVerificacionService tokenVerificacionService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthenticationService autenticacionService;

    @BeforeEach
    void setUp()
    {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown()
    {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("'estaAutenticado()' devuelve false cuando Authentication es null")
    void testEstaAutenticadoConAuthNull()
    {
        assertFalse(autenticacionService.estaAutenticado((Authentication) null));
        assertFalse(autenticacionService.estaAutenticado());
    }

    @Test
    @DisplayName("'estaAutenticado()' devuelve false cuando el usuario es AnonymousAuthenticationToken")
    void testEstaAutenticadoConUsuarioAnonimo()
    {
        AnonymousAuthenticationToken anonimo = new AnonymousAuthenticationToken(
                "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(anonimo);
        SecurityContextHolder.setContext(context);

        assertFalse(autenticacionService.estaAutenticado(anonimo));
        assertFalse(autenticacionService.estaAutenticado());
    }

    @Test
    @DisplayName("'estaAutenticado()' devuelve true cuando el usuario tiene autenticación válida")
    void testEstaAutenticadoConUsuarioValido()
    {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "admin", "password", AuthorityUtils.createAuthorityList("ROLE_ADMIN"));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        assertTrue(autenticacionService.estaAutenticado(auth));
        assertTrue(autenticacionService.estaAutenticado());
    }

    @Test
    @DisplayName("'obtenerUserDetails()' devuelve el UserDetailsImpl del usuario autenticado")
    void testObtenerUserDetailsExitoso()
    {
        UserDetailsImpl userDetailsMock = mock(UserDetailsImpl.class);
        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetailsMock, "password", AuthorityUtils.createAuthorityList("ROLE_ADMIN"));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        Optional<UserDetailsImpl> resultado = autenticacionService.obtenerUserDetails();

        assertTrue(resultado.isPresent());
        assertEquals(userDetailsMock, resultado.get());
    }

    @Test
    @DisplayName("'obtenerPersonaAutenticada()' busca y devuelve la entidad Persona por ID")
    void testObtenerPersonaAutenticada()
    {
        UserDetailsImpl userDetailsMock = mock(UserDetailsImpl.class);
        when(userDetailsMock.getId()).thenReturn(1L);

        Persona personaMock = mock(Persona.class);
        when(personaRepository.findById(1L)).thenReturn(Optional.of(personaMock));

        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetailsMock, "password", AuthorityUtils.createAuthorityList("ROLE_ADMIN"));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        Optional<Persona> resultado = autenticacionService.obtenerPersonaAutenticada();

        assertTrue(resultado.isPresent());
        assertEquals(personaMock, resultado.get());
        verify(personaRepository).findById(1L);
    }

    @Test
    @DisplayName("'cerrarSesion()' invalida la sesión y limpia el contexto de seguridad")
    void testCerrarSesion()
    {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.getSession(true); // Crea la sesión activa

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "alumno", "password", AuthorityUtils.createAuthorityList("ROLE_ALUMNO"));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        autenticacionService.cerrarSesion(request, response, auth);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNull(request.getSession(false));
    }

    @Test
    @DisplayName("'existeTelefono()' devuelve true cuando el teléfono ya existe en PersonaRepository")
    void testExisteTelefonoExistente()
    {
        when(personaRepository.existsByTelefono("600111222")).thenReturn(true);

        assertTrue(autenticacionService.existeTelefono("600111222"));
        verify(personaRepository).existsByTelefono("600111222");
    }

    @Test
    @DisplayName("'existeTelefono()' devuelve false cuando el teléfono no existe o está vacío")
    void testExisteTelefonoNoExistenteOEnBlanco()
    {
        when(personaRepository.existsByTelefono("699888777")).thenReturn(false);

        assertFalse(autenticacionService.existeTelefono("699888777"));
        assertFalse(autenticacionService.existeTelefono(null));
        assertFalse(autenticacionService.existeTelefono("   "));
    }

    @Test
    @DisplayName("'registrarAlumno()' lanza ReglaNegocioException cuando el teléfono ya existe")
    void testRegistrarAlumnoTelefonoDuplicado()
    {
        RegistroAlumnoDTO dto = new RegistroAlumnoDTO(
                "alumno_tel_dup",
                "correo.libre@autoescuela.es",
                "password123",
                "password123",
                "Carlos",
                "Gómez",
                "12345678Z",
                LocalDate.of(2000, 1, 1),
                "600111222",
                "Calle Principal 1",
                true
        );

        when(personaRepository.existsByNombreUsuario("alumno_tel_dup")).thenReturn(false);
        when(personaRepository.existsByCorreo("correo.libre@autoescuela.es")).thenReturn(false);
        when(personaRepository.existsByDni("12345678Z")).thenReturn(false);
        when(personaRepository.existsByTelefono("600111222")).thenReturn(true);

        ReglaNegocioException excepcion = assertThrows(ReglaNegocioException.class, () ->
                autenticacionService.registrarAlumno(dto)
        );

        assertEquals("El teléfono ya está registrado en el sistema.", excepcion.getMessage());
    }

    @Test
    @DisplayName("'solicitarRecuperacionPassword()' no hace nada si el correo es nulo o vacío")
    void testSolicitarRecuperacionCorreoVacio()
    {
        autenticacionService.solicitarRecuperacionPassword(null, "http://localhost:8080");
        autenticacionService.solicitarRecuperacionPassword("   ", "http://localhost:8080");

        verify(personaRepository, never()).findByCorreo(any());
        verify(emailService, never()).enviarCorreoRecuperacion(any(), any(), any());
    }

    @Test
    @DisplayName("'solicitarRecuperacionPassword()' no lanza excepción ni envía correo si el correo no existe en BD (Anti-User Enumeration)")
    void testSolicitarRecuperacionCorreoNoExiste()
    {
        when(personaRepository.findByCorreo("desconocido@autoescuela.es")).thenReturn(Optional.empty());

        autenticacionService.solicitarRecuperacionPassword("desconocido@autoescuela.es", "http://localhost:8080");

        verify(tokenVerificacionService, never()).generarTokenRecuperacion(any());
        verify(emailService, never()).enviarCorreoRecuperacion(any(), any(), any());
    }

    @Test
    @DisplayName("'solicitarRecuperacionPassword()' genera token y envía correo cuando el usuario existe y está ACTIVO")
    void testSolicitarRecuperacionUsuarioActivo()
    {
        Persona persona = mock(Persona.class);
        when(persona.getEstado()).thenReturn(EstadoUsuario.ACTIVO);
        when(persona.getCorreo()).thenReturn("alumno@autoescuela.es");
        when(persona.getNombre()).thenReturn("Juan");

        when(personaRepository.findByCorreo("alumno@autoescuela.es")).thenReturn(Optional.of(persona));

        TokenVerificacion token = new TokenVerificacion("token-uuid-123", persona, LocalDateTime.now().plusMinutes(15));
        when(tokenVerificacionService.generarTokenRecuperacion(persona)).thenReturn(token);

        autenticacionService.solicitarRecuperacionPassword("alumno@autoescuela.es", "http://localhost:8080");

        verify(tokenVerificacionService).generarTokenRecuperacion(persona);
        verify(emailService).enviarCorreoRecuperacion(
                eq("alumno@autoescuela.es"),
                eq("Juan"),
                contains("token=token-uuid-123")
        );
    }

    @Test
    @DisplayName("'solicitarRecuperacionPassword()' no genera token ni envía correo si el usuario está INACTIVO")
    void testSolicitarRecuperacionUsuarioInactivo()
    {
        Persona persona = mock(Persona.class);
        when(persona.getEstado()).thenReturn(EstadoUsuario.INACTIVO);

        when(personaRepository.findByCorreo("baja@autoescuela.es")).thenReturn(Optional.of(persona));

        autenticacionService.solicitarRecuperacionPassword("baja@autoescuela.es", "http://localhost:8080");

        verify(tokenVerificacionService, never()).generarTokenRecuperacion(any());
        verify(emailService, never()).enviarCorreoRecuperacion(any(), any(), any());
    }

    @Test
    @DisplayName("'validarTokenRecuperacion()' delega en TokenVerificacionService")
    void testValidarTokenRecuperacion()
    {
        when(tokenVerificacionService.esTokenValido("token-ok")).thenReturn(true);
        when(tokenVerificacionService.esTokenValido("token-bad")).thenReturn(false);

        assertTrue(autenticacionService.validarTokenRecuperacion("token-ok"));
        assertFalse(autenticacionService.validarTokenRecuperacion("token-bad"));
    }

    @Test
    @DisplayName("'restablecerPassword()' lanza ReglaNegocioException si DTO es nulo o contraseñas no coinciden")
    void testRestablecerPasswordValidaciones()
    {
        assertThrows(ReglaNegocioException.class, () ->
                autenticacionService.restablecerPassword(null)
        );

        RestablecerPasswordDTO dtoDistintas = new RestablecerPasswordDTO("token-123", "password123", "otraPassword");
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () ->
                autenticacionService.restablecerPassword(dtoDistintas)
        );
        assertEquals("Las contraseñas introducidas no coinciden.", ex.getMessage());

        RestablecerPasswordDTO dtoCorta = new RestablecerPasswordDTO("token-123", "corta", "corta");
        ReglaNegocioException exCorta = assertThrows(ReglaNegocioException.class, () ->
                autenticacionService.restablecerPassword(dtoCorta)
        );
        assertEquals("La contraseña debe tener al menos 8 caracteres.", exCorta.getMessage());
    }

    @Test
    @DisplayName("'restablecerPassword()' lanza ReglaNegocioException si el token no es válido o está expirado")
    void testRestablecerPasswordTokenInvalido()
    {
        RestablecerPasswordDTO dto = new RestablecerPasswordDTO("token-invalido", "nuevaPassword123", "nuevaPassword123");
        when(tokenVerificacionService.obtenerTokenValido("token-invalido")).thenReturn(Optional.empty());

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () ->
                autenticacionService.restablecerPassword(dto)
        );
        assertTrue(ex.getMessage().contains("caducado") || ex.getMessage().contains("inválido"));
    }

    @Test
    @DisplayName("'restablecerPassword()' actualiza la contraseña codificada y marca el token como usado")
    void testRestablecerPasswordExito()
    {
        Persona persona = mock(Persona.class);
        TokenVerificacion token = new TokenVerificacion("token-valido", persona, LocalDateTime.now().plusMinutes(15));

        when(tokenVerificacionService.obtenerTokenValido("token-valido")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("nuevaPassword123")).thenReturn("$2a$10$encodedHashPassword");

        RestablecerPasswordDTO dto = new RestablecerPasswordDTO("token-valido", "nuevaPassword123", "nuevaPassword123");
        autenticacionService.restablecerPassword(dto);

        verify(persona).actualizarPassword("$2a$10$encodedHashPassword");
        verify(personaRepository).save(persona);
        verify(tokenVerificacionService).marcarComoUsado(token);
    }
}
