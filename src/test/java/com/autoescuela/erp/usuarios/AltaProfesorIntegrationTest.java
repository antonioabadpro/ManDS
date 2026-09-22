package com.autoescuela.erp.usuarios;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.autoescuela.erp.auth.model.TokenVerificacion;
import com.autoescuela.erp.auth.repository.TokenVerificacionRepository;
import com.autoescuela.erp.core.enums.EstadoVehiculo;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.enums.TipoTurno;
import com.autoescuela.erp.flota.model.Vehiculo;
import com.autoescuela.erp.flota.repository.VehiculoRepository;
import com.autoescuela.erp.usuarios.model.Profesor;
import com.autoescuela.erp.usuarios.repository.ProfesorRepository;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@TestPropertySource(properties =
{
        "spring.sql.init.mode=always",
        "spring.sql.init.data-locations=classpath:data.sql",
        "spring.jpa.defer-datasource-initialization=true",
        "spring.mail.host=localhost",
        "spring.mail.port=1025"
})
@Transactional
/**
 * Pruebas de integración para el flujo completo de Alta de Profesor desde el Dashboard
 * y activación de cuenta con configuración de credenciales (Regla de negocio 7.1).
 */
class AltaProfesorIntegrationTest
{
    @Autowired
    private WebApplicationContext contexto;

    @Autowired
    private ProfesorRepository profesorRepository;

    @Autowired
    private VehiculoRepository vehiculoRepository;

    @Autowired
    private TokenVerificacionRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp()
    {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(this.contexto)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /admin/dashboard renderiza el modal de Alta de Profesor con campos estilizados y opciones")
    void testDashboardRenderizaModalAltaProfesor() throws Exception
    {
        this.mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attributeExists("altaProfesorDTO"))
                .andExpect(model().attributeExists("vehiculosDisponibles"))
                .andExpect(model().attributeExists("turnos"))
                .andExpect(model().attributeExists("tiposCarnet"))
                .andExpect(content().string(containsString("id=\"modal-alta-profesor\"")))
                .andExpect(content().string(containsString("action=\"/admin/profesores/alta\"")))
                .andExpect(content().string(containsString("Turno Matinal")))
                .andExpect(content().string(containsString("Turno de Tarde")))
                .andExpect(content().string(containsString("Permisos de Conducción Autorizados")))
                .andExpect(content().string(containsString("Vehículo Asignado")));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("POST /admin/profesores/alta registra un nuevo profesor, asigna vehículo y genera token de invitación")
    void testAltaProfesorExito() throws Exception
    {
        String nuevoDni = "87654321Z";
        String nuevoCorreo = "nuevo.profesor@autoescuela.es";

        this.mockMvc.perform(post("/admin/profesores/alta")
                .with(csrf())
                .param("nombre", "Roberto")
                .param("apellidos", "Gómez Bolaños")
                .param("dni", nuevoDni)
                .param("fechaNacimiento", "1982-05-20")
                .param("correo", nuevoCorreo)
                .param("telefono", "699887766")
                .param("direccion", "Calle de la Primavera 12, Madrid")
                .param("fechaContratacion", "2026-09-01")
                .param("turno", "TARDE")
                .param("vehiculoId", "3")
                .param("permisos", "PERMISO_B", "PERMISO_A2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"))
                .andExpect(flash().attributeExists("mensajeExito"));

        // Comprobación de persistencia del profesor
        Optional<Profesor> profesorOpt = this.profesorRepository.findByDni(nuevoDni);
        assertTrue(profesorOpt.isPresent(), "El profesor debe haberse persistido en la base de datos.");

        Profesor profesor = profesorOpt.get();
        assertEquals("Roberto", profesor.getNombre());
        assertEquals("Gómez Bolaños", profesor.getApellidos());
        assertEquals(TipoTurno.TARDE, profesor.getTurno());
        assertEquals(LocalDate.of(2026, 9, 1), profesor.getFechaContratacion());
        assertNotNull(profesor.getPassword(), "Debe poseer una contraseña provisional no nula.");
        assertNotNull(profesor.getNombreUsuario(), "Debe poseer un nombre de usuario provisional.");
        assertTrue(profesor.getListaTiposCarnet().contains(TipoCarnet.PERMISO_B));
        assertTrue(profesor.getListaTiposCarnet().contains(TipoCarnet.PERMISO_A2));

        // Comprobación de cambio de estado del vehículo asignado
        assertNotNull(profesor.getVehiculo());
        assertEquals(3L, profesor.getVehiculo().getId());
        Vehiculo vehiculoActualizado = this.vehiculoRepository.findById(3L).orElseThrow();
        assertEquals(EstadoVehiculo.OCUPADO, vehiculoActualizado.getEstado());

        // Comprobación de emisión del token de verificación para activación de cuenta
        List<TokenVerificacion> tokens = this.tokenRepository.findByPersona(profesor);
        assertFalse(tokens.isEmpty(), "Debe haberse generado un TokenVerificacion para el profesor.");
        TokenVerificacion token = tokens.getFirst();
        assertFalse(token.isUsado(), "El token no debe estar marcado como usado.");
        assertTrue(token.isValido(), "El token debe encontrarse vigente y no expirado.");
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("POST /admin/profesores/alta con DNI duplicado falla y muestra alerta sin redirección externa")
    void testAltaProfesorDniDuplicado() throws Exception
    {
        // DNI ya registrado para el Profesor Laura Sánchez (id = 2) en data.sql
        String dniExistente = "23456789B";

        this.mockMvc.perform(post("/admin/profesores/alta")
                .with(csrf())
                .param("nombre", "Laura Segunda")
                .param("apellidos", "Pérez")
                .param("dni", dniExistente)
                .param("fechaNacimiento", "1990-01-01")
                .param("correo", "otro.correo@autoescuela.es")
                .param("telefono", "611000111")
                .param("direccion", "Calle Falsa 123")
                .param("fechaContratacion", "2026-09-01")
                .param("turno", "MATINAL")
                .param("permisos", "PERMISO_B"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attribute("abrirModalAltaProfesor", true))
                .andExpect(model().attributeExists("errorAltaProfesor"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("POST /admin/profesores/alta con campos obligatorios vacíos no persiste y reabre el modal con errores")
    void testAltaProfesorValidacionCamposVacios() throws Exception
    {
        this.mockMvc.perform(post("/admin/profesores/alta")
                .with(csrf())
                .param("nombre", "")
                .param("apellidos", "")
                .param("dni", "")
                .param("correo", "formato-invalido"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attribute("abrirModalAltaProfesor", true));
    }

    @Test
    @DisplayName("GET /activar-cuenta con token inexistente redirige a /login con flag de token inválido")
    void testActivarCuentaTokenInvalido() throws Exception
    {
        this.mockMvc.perform(get("/activar-cuenta").param("token", "token-no-existente-xyz"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?tokenInvalido=true"));
    }

    @Test
    @DisplayName("Flujo de activación de cuenta exitoso a través de /activar-cuenta")
    void testActivarCuentaProfesorExito() throws Exception
    {
        // 1. Damos de alta primero a un profesor para generar su token de invitación
        String dni = "77788899K";
        String correo = "docente.activar@autoescuela.es";

        this.mockMvc.perform(post("/admin/profesores/alta")
                .with(csrf())
                .with(user("admin").roles("ADMIN"))
                .param("nombre", "Clara")
                .param("apellidos", "Campoamor")
                .param("dni", dni)
                .param("fechaNacimiento", "1992-02-10")
                .param("correo", correo)
                .param("telefono", "654321987")
                .param("direccion", "Calle Libertad 5, Madrid")
                .param("fechaContratacion", "2026-09-10")
                .param("turno", "MATINAL")
                .param("permisos", "PERMISO_B"))
                .andExpect(status().is3xxRedirection());

        Profesor profesor = this.profesorRepository.findByDni(dni).orElseThrow();
        TokenVerificacion token = this.tokenRepository.findByPersona(profesor).getFirst();

        // 2. Acceso por GET a la vista de activación con el token recibido
        this.mockMvc.perform(get("/activar-cuenta").param("token", token.getToken()))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/activar-cuenta"))
                .andExpect(model().attributeExists("activarDTO"))
                .andExpect(content().string(containsString("Activa tu Cuenta Docente")))
                .andExpect(content().string(containsString("Nombre de Usuario deseado")));

        // 3. Envío del formulario de activación con nuevo nombre de usuario y contraseña
        String usernameDefinitivo = "clara.campoamor";
        String passwordDefinitiva = "Docente2026Segura*";

        this.mockMvc.perform(post("/activar-cuenta")
                .with(csrf())
                .param("token", token.getToken())
                .param("nombreUsuario", usernameDefinitivo)
                .param("password", passwordDefinitiva)
                .param("confirmPassword", passwordDefinitiva))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?activado=true"));

        // 4. Verificaciones en la base de datos
        Profesor profesorActualizado = this.profesorRepository.findById(profesor.getId()).orElseThrow();
        assertEquals(usernameDefinitivo, profesorActualizado.getNombreUsuario());
        assertTrue(this.passwordEncoder.matches(passwordDefinitiva, profesorActualizado.getPassword()),
                "La contraseña almacenada debe coincidir con el hash BCrypt de la nueva contraseña.");

        TokenVerificacion tokenActualizado = this.tokenRepository.findById(token.getId()).orElseThrow();
        assertTrue(tokenActualizado.isUsado(), "El token debe quedar marcado como usado tras la activación.");
    }

    @Test
    @DisplayName("POST /activar-cuenta con contraseñas no coincidentes retorna error en el formulario")
    void testActivarCuentaProfesorPasswordNoCoincide() throws Exception
    {
        String dni = "55566677M";
        String correo = "docente.error@autoescuela.es";

        this.mockMvc.perform(post("/admin/profesores/alta")
                .with(csrf())
                .with(user("admin").roles("ADMIN"))
                .param("nombre", "Marcos")
                .param("apellidos", "Alonso")
                .param("dni", dni)
                .param("fechaNacimiento", "1989-08-14")
                .param("correo", correo)
                .param("telefono", "622334455")
                .param("direccion", "Avenida Mayor 4")
                .param("fechaContratacion", "2026-09-15")
                .param("turno", "MATINAL")
                .param("permisos", "PERMISO_B"))
                .andExpect(status().is3xxRedirection());

        Profesor profesor = this.profesorRepository.findByDni(dni).orElseThrow();
        TokenVerificacion token = this.tokenRepository.findByPersona(profesor).getFirst();

        this.mockMvc.perform(post("/activar-cuenta")
                .with(csrf())
                .param("token", token.getToken())
                .param("nombreUsuario", "marcos.alonso")
                .param("password", "Contrasena123*")
                .param("confirmPassword", "ContrasenaDiferente456*"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/activar-cuenta"))
                .andExpect(model().attributeExists("mensajeError"));
    }
}
