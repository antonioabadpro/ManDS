package com.autoescuela.erp.auth;

import java.time.LocalDate;
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

import com.autoescuela.erp.core.enums.EstadoUsuario;
import com.autoescuela.erp.core.enums.Rol;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.repository.AlumnoRepository;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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
@TestPropertySource(properties = {
    "spring.sql.init.mode=always",
    "spring.sql.init.data-locations=classpath:data.sql",
    "spring.jpa.defer-datasource-initialization=true"
})
/**
 * Pruebas de integración para el circuito completo de registro público de alumnos.
 * Cubre peticiones HTTP POST /registro, validaciones declarativas, reglas de negocio de unicidad,
 * encriptación segura de contraseña con BCrypt y persistencia en la base de datos.
 * RegistroAlumnoIntegrationTest contiene pruebas para verificar el registro exitoso de un alumno, así como los casos de error por duplicidad de nombre de usuario, correo, DNI y teléfono.
 * Se utilizan Mocks para simular solicitudes HTTP y verificar el contenido de la respuesta, así como la correcta persistencia de los datos en la base de datos.
 */
@Transactional
class RegistroAlumnoIntegrationTest
{
    @Autowired
    private WebApplicationContext contexto;

    @Autowired
    private AlumnoRepository alumnoRepository;

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
    @DisplayName("POST /registro con datos válidos registra al alumno en BD, hashea su clave y redirige a login")
    void testRegistroAlumnoExitoso() throws Exception
    {
        this.mockMvc.perform(post("/registro")
                .with(csrf())
                .param("nombreUsuario", "nuevo_alumno")
                .param("correo", "nuevo.alumno@autoescuela.es")
                .param("password", "claveSecreta123")
                .param("confirmPassword", "claveSecreta123")
                .param("nombre", "Alejandro")
                .param("apellidos", "Sanz Prieto")
                .param("dni", "77889900K")
                .param("fechaNacimiento", "2002-05-14")
                .param("telefono", "611223344")
                .param("direccion", "Calle Real 45, Madrid")
                .param("terminos", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registrado=true"))
                .andExpect(flash().attributeExists("mensajeExito"));

        // Comprobación de persistencia y propiedades en el repositorio
        Optional<Alumno> alumnoOpt = this.alumnoRepository.findByNombreUsuario("nuevo_alumno");
        assertTrue(alumnoOpt.isPresent(), "El alumno recién registrado debe encontrarse en el repositorio");

        Alumno alumno = alumnoOpt.get();
        assertEquals("Alejandro", alumno.getNombre());
        assertEquals("Sanz Prieto", alumno.getApellidos());
        assertEquals("77889900K", alumno.getDni());
        assertEquals("nuevo.alumno@autoescuela.es", alumno.getCorreo());
        assertEquals(LocalDate.of(2002, 5, 14), alumno.getFechaNacimiento());
        assertEquals("611223344", alumno.getTelefono());
        assertEquals("Calle Real 45, Madrid", alumno.getDireccion());
        assertEquals(EstadoUsuario.ACTIVO, alumno.getEstado());
        assertEquals(Rol.ALUMNO, alumno.getRol());

        // Comprobación de seguridad: la contraseña NO debe guardarse en texto plano y debe verificar con BCrypt
        assertNotEquals("claveSecreta123", alumno.getPassword());
        assertTrue(this.passwordEncoder.matches("claveSecreta123", alumno.getPassword()));
    }

    @Test
    @DisplayName("POST /registro con nombre de usuario existente rechaza el registro con mensaje amigable")
    void testRegistroNombreUsuarioDuplicado() throws Exception
    {
        this.mockMvc.perform(post("/registro")
                .with(csrf())
                .param("nombreUsuario", "alumno1") // Existe en data.sql
                .param("correo", "correo.unico@autoescuela.es")
                .param("password", "claveSecreta123")
                .param("confirmPassword", "claveSecreta123")
                .param("nombre", "Juan")
                .param("apellidos", "García")
                .param("dni", "99112233A")
                .param("fechaNacimiento", "2000-01-01")
                .param("telefono", "622334455")
                .param("direccion", "Avenida Sol 1")
                .param("terminos", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/registro"))
                .andExpect(model().attribute("error", "El nombre de usuario ya está registrado en el sistema."))
                .andExpect(content().string(containsString("El nombre de usuario ya está registrado en el sistema.")));
    }

    @Test
    @DisplayName("POST /registro con correo existente rechaza el registro con mensaje descriptivo")
    void testRegistroCorreoDuplicado() throws Exception
    {
        this.mockMvc.perform(post("/registro")
                .with(csrf())
                .param("nombreUsuario", "usuario_unico")
                .param("correo", "jose.alumno@autoescuela.es") // Existe en data.sql
                .param("password", "claveSecreta123")
                .param("confirmPassword", "claveSecreta123")
                .param("nombre", "Jose")
                .param("apellidos", "Martínez")
                .param("dni", "99112233B")
                .param("fechaNacimiento", "2000-01-01")
                .param("telefono", "622334455")
                .param("direccion", "Avenida Sol 1")
                .param("terminos", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/registro"))
                .andExpect(model().attribute("error", "El correo electrónico ya está registrado en el sistema."))
                .andExpect(content().string(containsString("El correo electrónico ya está registrado en el sistema.")));
    }

    @Test
    @DisplayName("POST /registro con DNI existente rechaza el registro por colisión de identidad")
    void testRegistroDniDuplicado() throws Exception
    {
        this.mockMvc.perform(post("/registro")
                .with(csrf())
                .param("nombreUsuario", "usuario_distinto")
                .param("correo", "correo.distinto@autoescuela.es")
                .param("password", "claveSecreta123")
                .param("confirmPassword", "claveSecreta123")
                .param("nombre", "Laura")
                .param("apellidos", "Sánchez")
                .param("dni", "45678901D") // DNI de alumno1 en data.sql
                .param("fechaNacimiento", "2000-01-01")
                .param("telefono", "622334455")
                .param("direccion", "Avenida Sol 1")
                .param("terminos", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/registro"))
                .andExpect(model().attribute("error", "El DNI/NIE ya está registrado en el sistema."))
                .andExpect(content().string(containsString("El DNI/NIE ya está registrado en el sistema.")));
    }

    @Test
    @DisplayName("POST /registro con contraseñas no coincidentes muestra error de validación")
    void testRegistroPasswordNoCoincide() throws Exception
    {
        this.mockMvc.perform(post("/registro")
                .with(csrf())
                .param("nombreUsuario", "alumno_pass_err")
                .param("correo", "pass.err@autoescuela.es")
                .param("password", "password123")
                .param("confirmPassword", "passwordDistinta456")
                .param("nombre", "Carlos")
                .param("apellidos", "Gómez")
                .param("dni", "88223344J")
                .param("fechaNacimiento", "2001-02-02")
                .param("telefono", "633445566")
                .param("direccion", "Calle Luna 2")
                .param("terminos", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/registro"))
                .andExpect(model().attribute("error", "Las contraseñas introducidas no coinciden."))
                .andExpect(content().string(containsString("Las contraseñas introducidas no coinciden.")));
    }

    @Test
    @DisplayName("POST /registro sin aceptar los términos de uso es rechazado por Bean Validation")
    void testRegistroTerminosNoAceptados() throws Exception
    {
        this.mockMvc.perform(post("/registro")
                .with(csrf())
                .param("nombreUsuario", "alumno_sin_terminos")
                .param("correo", "sin.terminos@autoescuela.es")
                .param("password", "password123")
                .param("confirmPassword", "password123")
                .param("nombre", "Carlos")
                .param("apellidos", "Gómez")
                .param("dni", "88223344K")
                .param("fechaNacimiento", "2001-02-02")
                .param("telefono", "633445566")
                .param("direccion", "Calle Luna 2")
                .param("terminos", "false"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/registro"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    @DisplayName("GET /registro para un usuario ya autenticado redirige a la página principal")
    @WithMockUser(username = "alumno1", roles = "ALUMNO")
    void testGetRegistroParaUsuarioAutenticado() throws Exception
    {
        this.mockMvc.perform(get("/registro"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @DisplayName("POST /usuario/validar-username con usuario duplicado devuelve fragmento de error")
    void testValidarUsuarioDuplicadoHtmx() throws Exception
    {
        this.mockMvc.perform(post("/usuario/validar-username")
                .with(csrf())
                .param("nombreUsuario", "alumno1"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/registro :: mensaje-error"))
                .andExpect(model().attribute("mensaje", "El nombre de usuario ya está registrado en el sistema."))
                .andExpect(content().string(containsString("El nombre de usuario ya está registrado en el sistema.")));
    }

    @Test
    @DisplayName("POST /usuario/validar-username con formato inválido devuelve mensaje descriptivo")
    void testValidarUsuarioInvalidoHtmx() throws Exception
    {
        this.mockMvc.perform(post("/usuario/validar-username")
                .with(csrf())
                .param("nombreUsuario", "ab"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/registro :: mensaje-error"))
                .andExpect(content().string(containsString("El nombre de usuario debe tener entre 3 y 50 caracteres.")));
    }

    @Test
    @DisplayName("POST /usuario/validar-username con usuario libre devuelve fragmento vacío")
    void testValidarUsuarioDisponibleHtmx() throws Exception
    {
        this.mockMvc.perform(post("/usuario/validar-username")
                .with(csrf())
                .param("nombreUsuario", "alumno_nuevo_totalmente_libre"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/registro :: fragmento-vacio"));
    }

    @Test
    @DisplayName("POST /usuario/validar-correo con correo duplicado devuelve fragmento de error")
    void testValidarCorreoDuplicadoHtmx() throws Exception
    {
        this.mockMvc.perform(post("/usuario/validar-correo")
                .with(csrf())
                .param("correo", "jose.alumno@autoescuela.es"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/registro :: mensaje-error"))
                .andExpect(model().attribute("mensaje", "El correo electrónico ya está registrado en el sistema."))
                .andExpect(content().string(containsString("El correo electrónico ya está registrado en el sistema.")));
    }

    @Test
    @DisplayName("POST /usuario/validar-correo con correo libre devuelve fragmento vacío")
    void testValidarCorreoDisponibleHtmx() throws Exception
    {
        this.mockMvc.perform(post("/usuario/validar-correo")
                .with(csrf())
                .param("correo", "nuevo_correo_libre@autoescuela.es"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/registro :: fragmento-vacio"));
    }

    @Test
    @DisplayName("POST /usuario/validar-dni con DNI duplicado devuelve fragmento de error")
    void testValidarDniDuplicadoHtmx() throws Exception
    {
        this.mockMvc.perform(post("/usuario/validar-dni")
                .with(csrf())
                .param("dni", "45678901D"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/registro :: mensaje-error"))
                .andExpect(model().attribute("mensaje", "El DNI/NIE ya está registrado en el sistema."))
                .andExpect(content().string(containsString("El DNI/NIE ya está registrado en el sistema.")));
    }

    @Test
    @DisplayName("POST /usuario/validar-dni con formato no válido devuelve fragmento de error")
    void testValidarDniFormatoInvalidoHtmx() throws Exception
    {
        this.mockMvc.perform(post("/usuario/validar-dni")
                .with(csrf())
                .param("dni", "123456"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/registro :: mensaje-error"))
                .andExpect(content().string(containsString("El formato del DNI/NIE no es válido")));
    }

    @Test
    @DisplayName("POST /usuario/validar-dni con DNI válido y libre devuelve fragmento vacío")
    void testValidarDniDisponibleHtmx() throws Exception
    {
        this.mockMvc.perform(post("/usuario/validar-dni")
                .with(csrf())
                .param("dni", "99887766K"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/registro :: fragmento-vacio"));
    }

    @Test
    @DisplayName("POST /registro con teléfono existente rechaza el registro por número de contacto duplicado")
    void testRegistroTelefonoDuplicado() throws Exception
    {
        this.mockMvc.perform(post("/registro")
                .with(csrf())
                .param("nombreUsuario", "alumno_nuevo_tel_dup")
                .param("correo", "correo.nuevo.tel@autoescuela.es")
                .param("password", "claveSecreta123")
                .param("confirmPassword", "claveSecreta123")
                .param("nombre", "Marcos")
                .param("apellidos", "Vega")
                .param("dni", "88776655L")
                .param("fechaNacimiento", "2000-01-01")
                .param("telefono", "600444555") // Teléfono de alumno1 en data.sql
                .param("direccion", "Avenida Sol 1")
                .param("terminos", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/registro"))
                .andExpect(model().attribute("error", "El teléfono ya está registrado en el sistema."))
                .andExpect(content().string(containsString("El teléfono ya está registrado en el sistema.")));
    }

    @Test
    @DisplayName("POST /usuario/validar-telefono con teléfono duplicado devuelve fragmento de error")
    void testValidarTelefonoDuplicadoHtmx() throws Exception
    {
        this.mockMvc.perform(post("/usuario/validar-telefono")
                .with(csrf())
                .param("telefono", "600444555")) // Teléfono de alumno1 en data.sql
                .andExpect(status().isOk())
                .andExpect(view().name("auth/registro :: mensaje-error"))
                .andExpect(model().attribute("mensaje", "El teléfono ya está registrado en el sistema."))
                .andExpect(content().string(containsString("El teléfono ya está registrado en el sistema.")));
    }

    @Test
    @DisplayName("POST /usuario/validar-telefono con formato no válido devuelve fragmento de error")
    void testValidarTelefonoInvalidoHtmx() throws Exception
    {
        this.mockMvc.perform(post("/usuario/validar-telefono")
                .with(csrf())
                .param("telefono", "12345"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/registro :: mensaje-error"))
                .andExpect(content().string(containsString("El formato del teléfono no es válido")));
    }

    @Test
    @DisplayName("POST /usuario/validar-telefono con teléfono válido y libre devuelve fragmento vacío")
    void testValidarTelefonoDisponibleHtmx() throws Exception
    {
        this.mockMvc.perform(post("/usuario/validar-telefono")
                .with(csrf())
                .param("telefono", "699112233"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/registro :: fragmento-vacio"));
    }
}

