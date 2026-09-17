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

/**
 * Pruebas unitarias con Mockito para la clase AutenticacionService.
 * AutenticacionServiceTest
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
}
