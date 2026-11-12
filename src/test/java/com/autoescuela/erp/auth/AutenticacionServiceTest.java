package com.autoescuela.erp.auth;

import java.time.LocalDate;
import java.util.List;
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

import com.autoescuela.erp.auth.dto.ActivarCuentaProfesorDTO;
import com.autoescuela.erp.auth.dto.RegistroAlumnoDTO;
import com.autoescuela.erp.auth.service.AuthenticationService;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.core.security.UserDetailsImpl;
import com.autoescuela.erp.core.util.FechaHoraUtil;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.model.Persona;
import com.autoescuela.erp.usuarios.model.Profesor;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;

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
import org.springframework.test.util.ReflectionTestUtils;
import com.autoescuela.erp.auth.mapper.AuthenticationMapper;
import com.autoescuela.erp.usuarios.repository.AlumnoRepository;
import com.autoescuela.erp.usuarios.service.UsuarioService;

@ExtendWith(MockitoExtension.class)
class AutenticacionServiceTest
{
    @Mock
    private UsuarioService usuarioService;

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
        assertFalse(this.autenticacionService.estaAutenticado((Authentication) null));
        assertFalse(this.autenticacionService.estaAutenticado());
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

        assertFalse(this.autenticacionService.estaAutenticado(anonimo));
        assertFalse(this.autenticacionService.estaAutenticado());
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

        assertTrue(this.autenticacionService.estaAutenticado(auth));
        assertTrue(this.autenticacionService.estaAutenticado());
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

        Optional<UserDetailsImpl> resultado = this.autenticacionService.obtenerUserDetails();

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
        when(this.personaRepository.findById(1L)).thenReturn(Optional.of(personaMock));

        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetailsMock, "password", AuthorityUtils.createAuthorityList("ROLE_ADMIN"));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        Optional<Persona> resultado = this.autenticacionService.obtenerPersonaAutenticada();

        assertTrue(resultado.isPresent());
        assertEquals(personaMock, resultado.get());
        verify(this.personaRepository).findById(1L);
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

        this.autenticacionService.cerrarSesion(request, response, auth);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNull(request.getSession(false));
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
                true,
                TipoCarnet.PERMISO_B
        );

        when(this.personaRepository.existsByNombreUsuario("alumno_tel_dup")).thenReturn(false);
        when(this.personaRepository.existsByCorreo("correo.libre@autoescuela.es")).thenReturn(false);
        when(this.personaRepository.existsByDni("12345678Z")).thenReturn(false);
        when(this.usuarioService.esLetraDniValida("12345678Z")).thenReturn(true);
        when(this.personaRepository.existsByTelefono("600111222")).thenReturn(true);

        ReglaNegocioException excepcion = assertThrows(ReglaNegocioException.class, () ->
                this.autenticacionService.registrarAlumno(dto)
        );

        assertEquals("El teléfono ya está registrado en el sistema.", excepcion.getMessage());
    }

    @Test
    @DisplayName("'registrarAlumno()' lanza ReglaNegocioException cuando la letra del DNI no es válida")
    void testRegistrarAlumnoDniLetraInvalida()
    {
        RegistroAlumnoDTO dto = new RegistroAlumnoDTO(
                "alumno_dni_invalido",
                "correo.libre@autoescuela.es",
                "password123",
                "password123",
                "Carlos",
                "Gómez",
                "12345678A",
                LocalDate.of(2000, 1, 1),
                "600111222",
                "Calle Principal 1",
                true,
                TipoCarnet.PERMISO_B
        );

        when(this.personaRepository.existsByNombreUsuario("alumno_dni_invalido")).thenReturn(false);
        when(this.personaRepository.existsByCorreo("correo.libre@autoescuela.es")).thenReturn(false);
        when(this.personaRepository.existsByDni("12345678A")).thenReturn(false);
        when(this.usuarioService.esLetraDniValida("12345678A")).thenReturn(false);

        ReglaNegocioException excepcion = assertThrows(ReglaNegocioException.class, () ->
                this.autenticacionService.registrarAlumno(dto)
        );

        assertEquals("La letra del DNI/NIE no es válida o no coincide con los números.", excepcion.getMessage());
    }

    @Test
    @DisplayName("'solicitarRecuperacionPassword()' no hace nada si el correo es nulo o vacío")
    void testSolicitarRecuperacionCorreoVacio()
    {
        this.autenticacionService.solicitarRecuperacionPassword(null, "http://localhost:8080");
        this.autenticacionService.solicitarRecuperacionPassword("   ", "http://localhost:8080");

        verify(this.personaRepository, never()).findByCorreo(any());
        verify(this.emailService, never()).enviarCorreoRecuperacion(any(), any(), any(), any());
    }

    @Test
    @DisplayName("'solicitarRecuperacionPassword()' no lanza excepción ni envía correo si el correo no existe en BD (Anti-User Enumeration)")
    void testSolicitarRecuperacionCorreoNoExiste()
    {
        when(this.personaRepository.findByCorreo("desconocido@autoescuela.es")).thenReturn(Optional.empty());

        this.autenticacionService.solicitarRecuperacionPassword("desconocido@autoescuela.es", "http://localhost:8080");

        verify(this.tokenVerificacionService, never()).generarTokenRecuperacion(any(), anyInt());
        verify(this.emailService, never()).enviarCorreoRecuperacion(any(), any(), any(), any());
    }

    @Test
    @DisplayName("'solicitarRecuperacionPassword()' genera token y envía correo cuando el usuario existe y está ACTIVO")
    void testSolicitarRecuperacionUsuarioActivo()
    {
        Persona persona = mock(Persona.class);
        when(persona.getEstado()).thenReturn(EstadoUsuario.ACTIVO);
        when(persona.getCorreo()).thenReturn("alumno@autoescuela.es");
        when(persona.getNombre()).thenReturn("Juan");

        when(this.personaRepository.findByCorreo("alumno@autoescuela.es")).thenReturn(Optional.of(persona));

        TokenVerificacion token = new TokenVerificacion("token-uuid-123", persona, FechaHoraUtil.ahora().plusMinutes(15));
        when(this.tokenVerificacionService.generarTokenRecuperacion(persona, 15)).thenReturn(token);

        this.autenticacionService.solicitarRecuperacionPassword("alumno@autoescuela.es", "http://localhost:8080");

        verify(this.tokenVerificacionService).generarTokenRecuperacion(persona, 15);
        verify(this.emailService).enviarCorreoRecuperacion(
                eq("alumno@autoescuela.es"),
                eq("Juan"),
                contains("token=token-uuid-123"),
                eq("15")
        );
    }

    @Test
    @DisplayName("'solicitarRecuperacionPassword()' normaliza barras finales duplicadas y aplica appBaseUrlConfigurada de respaldo")
    void testSolicitarRecuperacionNormalizaBarrasYRespaldo()
    {
        ReflectionTestUtils.setField(this.autenticacionService, "appBaseUrlConfigurada", "https://respaldo.mands.es///");

        Persona persona = mock(Persona.class);
        when(persona.getEstado()).thenReturn(EstadoUsuario.ACTIVO);
        when(persona.getCorreo()).thenReturn("alumno@autoescuela.es");
        when(persona.getNombre()).thenReturn("Juan");

        when(this.personaRepository.findByCorreo("alumno@autoescuela.es")).thenReturn(Optional.of(persona));

        TokenVerificacion token = new TokenVerificacion("token-uuid-456", persona, FechaHoraUtil.ahora().plusMinutes(15));
        when(this.tokenVerificacionService.generarTokenRecuperacion(persona, 15)).thenReturn(token);

        this.autenticacionService.solicitarRecuperacionPassword("alumno@autoescuela.es", "https://mands.onrender.com///");
        this.autenticacionService.solicitarRecuperacionPassword("alumno@autoescuela.es", "   ");

        verify(this.emailService).enviarCorreoRecuperacion(
                eq("alumno@autoescuela.es"),
                eq("Juan"),
                eq("https://mands.onrender.com/recuperar-password?token=token-uuid-456"),
                eq("15")
        );
        verify(this.emailService).enviarCorreoRecuperacion(
                eq("alumno@autoescuela.es"),
                eq("Juan"),
                eq("https://respaldo.mands.es/recuperar-password?token=token-uuid-456"),
                eq("15")
        );
    }

    @Test
    @DisplayName("'solicitarRecuperacionPassword()' no genera token ni envía correo si el usuario está INACTIVO")
    void testSolicitarRecuperacionUsuarioInactivo()
    {
        Persona persona = mock(Persona.class);
        when(persona.getEstado()).thenReturn(EstadoUsuario.INACTIVO);

        when(this.personaRepository.findByCorreo("baja@autoescuela.es")).thenReturn(Optional.of(persona));

        this.autenticacionService.solicitarRecuperacionPassword("baja@autoescuela.es", "http://localhost:8080");

        verify(this.tokenVerificacionService, never()).generarTokenRecuperacion(any(), anyInt());
        verify(this.emailService, never()).enviarCorreoRecuperacion(any(), any(), any(), any());
    }

    @Test
    @DisplayName("'validarTokenRecuperacion()' delega en TokenVerificacionService")
    void testValidarTokenRecuperacion()
    {
        when(this.tokenVerificacionService.esTokenValido("token-ok")).thenReturn(true);
        when(this.tokenVerificacionService.esTokenValido("token-bad")).thenReturn(false);

        assertTrue(this.autenticacionService.validarTokenRecuperacion("token-ok"));
        assertFalse(this.autenticacionService.validarTokenRecuperacion("token-bad"));
    }

    @Test
    @DisplayName("'restablecerPassword()' lanza ReglaNegocioException si DTO es nulo o contraseñas no coinciden")
    void testRestablecerPasswordValidaciones()
    {
        assertThrows(ReglaNegocioException.class, () ->
                this.autenticacionService.restablecerPassword(null)
        );

        RestablecerPasswordDTO dtoDistintas = new RestablecerPasswordDTO("token-123", "password123", "otraPassword");
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () ->
                this.autenticacionService.restablecerPassword(dtoDistintas)
        );
        assertEquals("Las contraseñas introducidas no coinciden.", ex.getMessage());

        RestablecerPasswordDTO dtoCorta = new RestablecerPasswordDTO("token-123", "corta", "corta");
        ReglaNegocioException exCorta = assertThrows(ReglaNegocioException.class, () ->
                this.autenticacionService.restablecerPassword(dtoCorta)
        );
        assertEquals("La contraseña debe tener al menos 6 caracteres.", exCorta.getMessage());
    }

    @Test
    @DisplayName("'restablecerPassword()' lanza ReglaNegocioException si el token no es válido o está expirado")
    void testRestablecerPasswordTokenInvalido()
    {
        RestablecerPasswordDTO dto = new RestablecerPasswordDTO("token-invalido", "nuevaPassword123", "nuevaPassword123");
        when(this.tokenVerificacionService.obtenerTokenValido("token-invalido")).thenReturn(Optional.empty());

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () ->
                this.autenticacionService.restablecerPassword(dto)
        );
        assertTrue(ex.getMessage().contains("caducado") || ex.getMessage().contains("inválido"));
    }

    @Test
    @DisplayName("'restablecerPassword()' actualiza la contraseña codificada y marca el token como usado")
    void testRestablecerPasswordExito()
    {
        Persona persona = mock(Persona.class);
        TokenVerificacion token = new TokenVerificacion("token-valido", persona, FechaHoraUtil.ahora().plusMinutes(15));

        when(this.tokenVerificacionService.obtenerTokenValido("token-valido")).thenReturn(Optional.of(token));
        when(this.passwordEncoder.encode("nuevaPassword123")).thenReturn("$2a$10$encodedHashPassword");

        RestablecerPasswordDTO dto = new RestablecerPasswordDTO("token-valido", "nuevaPassword123", "nuevaPassword123");
        this.autenticacionService.restablecerPassword(dto);

        verify(persona).actualizarPassword("$2a$10$encodedHashPassword");
        verify(this.personaRepository).save(persona);
        verify(this.tokenVerificacionService).marcarComoUsado(token);
    }

    @Test
    @DisplayName("'obtenerUserDetails()' y 'obtenerPersonaAutenticada()' retornan vacío sin sesión o sin UserDetailsImpl")
    void testObtenerUserDetailsYPersonaNoAutenticado()
    {
        assertTrue(this.autenticacionService.obtenerUserDetails().isEmpty());
        assertTrue(this.autenticacionService.obtenerPersonaAutenticada().isEmpty());

        Authentication authString = new UsernamePasswordAuthenticationToken(
                "soloString", "password", AuthorityUtils.createAuthorityList("ROLE_ALUMNO"));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authString);
        SecurityContextHolder.setContext(context);

        assertTrue(this.autenticacionService.obtenerUserDetails().isEmpty());
        assertTrue(this.autenticacionService.obtenerPersonaAutenticada().isEmpty());
    }

    @Test
    @DisplayName("'registrarAlumno()' purga huérfanos inactivos previos y valida DTO/duplicados")
    void testRegistrarAlumnoPurgaYValidaciones()
    {
        assertThrows(ReglaNegocioException.class, () -> this.autenticacionService.registrarAlumno(null));

        RegistroAlumnoDTO dtoDistintas = new RegistroAlumnoDTO("u1", "u1@m.es", "123456", "654321",
                "Nom", "Ape", "12345678Z", LocalDate.of(2000, 1, 1), "600111222", "Dir", true, TipoCarnet.PERMISO_B);
        assertThrows(ReglaNegocioException.class, () -> this.autenticacionService.registrarAlumno(dtoDistintas));

        Alumno huerfano = new Alumno();
        huerfano.setEstado(EstadoUsuario.INACTIVO);
        huerfano.setHistorialMatriculas(List.of());

        RegistroAlumnoDTO dto = new RegistroAlumnoDTO("nuevo", "nuevo@m.es", "123456", "123456",
                "Ana", "Ruiz", "12345678Z", LocalDate.of(2000, 1, 1), "600111222", "Calle 1", true, TipoCarnet.PERMISO_B);

        when(this.alumnoRepository.findAlumnosInactivosSinMatricula()).thenReturn(List.of(huerfano));
        when(this.alumnoRepository.findByDni("12345678Z")).thenReturn(Optional.of(huerfano));
        when(this.alumnoRepository.findByCorreo("nuevo@m.es")).thenReturn(Optional.of(huerfano));
        when(this.alumnoRepository.findByNombreUsuario("nuevo")).thenReturn(Optional.of(huerfano));
        when(this.alumnoRepository.findByTelefono("600111222")).thenReturn(Optional.of(huerfano));
        when(this.personaRepository.existsByNombreUsuario("nuevo")).thenReturn(true);

        assertThrows(ReglaNegocioException.class, () -> this.autenticacionService.registrarAlumno(dto));

        when(this.personaRepository.existsByNombreUsuario("nuevo")).thenReturn(false);
        when(this.personaRepository.existsByCorreo("nuevo@m.es")).thenReturn(true);
        assertThrows(ReglaNegocioException.class, () -> this.autenticacionService.registrarAlumno(dto));

        when(this.personaRepository.existsByCorreo("nuevo@m.es")).thenReturn(false);
        when(this.personaRepository.existsByDni("12345678Z")).thenReturn(true);
        assertThrows(ReglaNegocioException.class, () -> this.autenticacionService.registrarAlumno(dto));
    }

    @Test
    @DisplayName("eliminarAlumnoInactivoPorId y eliminarAlumnoInactivoPorDni purgan solo inactivos sin matrícula")
    void testEliminarAlumnoInactivoPorIdYDni()
    {
        this.autenticacionService.eliminarAlumnoInactivoPorId(null);
        this.autenticacionService.eliminarAlumnoInactivoPorDni(null);
        this.autenticacionService.eliminarAlumnoInactivoPorDni("  ");

        Alumno huerfano = new Alumno();
        huerfano.setEstado(EstadoUsuario.INACTIVO);
        huerfano.setHistorialMatriculas(List.of());

        when(this.alumnoRepository.findById(50L)).thenReturn(Optional.of(huerfano));
        when(this.alumnoRepository.findByDni("12345678Z")).thenReturn(Optional.of(huerfano));

        this.autenticacionService.eliminarAlumnoInactivoPorId(50L);
        this.autenticacionService.eliminarAlumnoInactivoPorDni(" 12345678z ");

        verify(this.alumnoRepository, org.mockito.Mockito.times(2)).delete(huerfano);
        verify(this.alumnoRepository, org.mockito.Mockito.times(2)).flush();
    }

    @Test
    @DisplayName("'activarCuentaProfesor()' valida DTO, contraseñas, token, username duplicado y activa cuenta")
    void testActivarCuentaProfesorCompleto()
    {
        assertThrows(ReglaNegocioException.class, () -> this.autenticacionService.activarCuentaProfesor(null));
        assertThrows(ReglaNegocioException.class, () -> this.autenticacionService.activarCuentaProfesor(
                new ActivarCuentaProfesorDTO("tok", "profe", "123456", "654321")));
        assertThrows(ReglaNegocioException.class, () -> this.autenticacionService.activarCuentaProfesor(
                new ActivarCuentaProfesorDTO("tok", "profe", "123", "123")));

        ActivarCuentaProfesorDTO dtoValido = new ActivarCuentaProfesorDTO("tok-ok", "profeNuevo", "password123", "password123");
        when(this.tokenVerificacionService.obtenerTokenValido("tok-ok")).thenReturn(Optional.empty());
        assertThrows(ReglaNegocioException.class, () -> this.autenticacionService.activarCuentaProfesor(dtoValido));

        Profesor profesor = new Profesor();
        profesor.setNombreUsuario("profe_temp");
        TokenVerificacion token = new TokenVerificacion("tok-ok", profesor, FechaHoraUtil.ahora().plusMinutes(60));
        when(this.tokenVerificacionService.obtenerTokenValido("tok-ok")).thenReturn(Optional.of(token));
        when(this.personaRepository.existsByNombreUsuario("profeNuevo")).thenReturn(true);
        assertThrows(ReglaNegocioException.class, () -> this.autenticacionService.activarCuentaProfesor(dtoValido));

        when(this.personaRepository.existsByNombreUsuario("profeNuevo")).thenReturn(false);
        when(this.passwordEncoder.encode("password123")).thenReturn("hashProfe");
        this.autenticacionService.activarCuentaProfesor(dtoValido);

        assertEquals("profeNuevo", profesor.getNombreUsuario());
        assertEquals("hashProfe", profesor.getPassword());
        verify(this.personaRepository).save(profesor);
        verify(this.tokenVerificacionService).marcarComoUsado(token);
    }
}
