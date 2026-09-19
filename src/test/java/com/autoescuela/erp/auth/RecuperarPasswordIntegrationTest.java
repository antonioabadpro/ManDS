package com.autoescuela.erp.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.autoescuela.erp.auth.model.TokenVerificacion;
import com.autoescuela.erp.auth.repository.TokenVerificacionRepository;
import com.autoescuela.erp.auth.service.TokenVerificacionService;
import com.autoescuela.erp.usuarios.model.Persona;
import com.autoescuela.erp.usuarios.repository.PersonaRepository;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

@SpringBootTest
@TestPropertySource(properties =
{
    "spring.sql.init.mode=always",
    "spring.sql.init.data-locations=classpath:data.sql",
    "spring.jpa.defer-datasource-initialization=true"
})
/**
 * Pruebas de integración del circuito completo de recuperación y restablecimiento de contraseña.
 * RecuperarPasswordIntegrationTest contiene pruebas para verificar el flujo completo de recuperación de contraseña, incluyendo la generación de tokens, validación de tokens, restablecimiento de contraseña y verificación de acceso con la nueva contraseña.
 * Se utilizan Mocks para simular solicitudes HTTP y se verifican los resultados esperados en cada paso del flujo.
 */
@Transactional
class RecuperarPasswordIntegrationTest
{
    @Autowired
    private WebApplicationContext contexto;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private TokenVerificacionRepository tokenVerificacionRepository;

    @Autowired
    private TokenVerificacionService tokenVerificacionService;

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
    @DisplayName("POST /recuperar-password con correo existente genera token y redirige a ?enviado=true")
    void testSolicitudRecuperacionCorreoExistente() throws Exception
    {
        this.mockMvc.perform(post("/recuperar-password")
                        .with(csrf())
                        .param("correo", "elena.alumno@autoescuela.es"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recuperar-password?enviado=true"));

        Persona persona = this.personaRepository.findByCorreo("elena.alumno@autoescuela.es").orElseThrow();

        // Comprobamos si existe el token
        List<TokenVerificacion> listaTokens = this.tokenVerificacionRepository.findAll();
        boolean tieneToken = false;

        for (TokenVerificacion token : listaTokens)
        {
            if (token.getPersona() != null && persona.getId().equals(token.getPersona().getId()) && token.isValido())
            {
                tieneToken = true;
                break;
            }
        }

        assertTrue(tieneToken, "Debe existir un token activo generado para el usuario.");
    }

    @Test
    @DisplayName("POST /recuperar-password con correo desconocido redirige a ?enviado=true sin generar token (Anti-Enumeration)")
    void testSolicitudRecuperacionCorreoDesconocido() throws Exception
    {
        long numTokensAntes = this.tokenVerificacionRepository.count();

        this.mockMvc.perform(post("/recuperar-password")
                        .with(csrf())
                        .param("correo", "inexistente@autoescuela.es"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recuperar-password?enviado=true"));

        long numTokensDespues = this.tokenVerificacionRepository.count();
        assertTrue(numTokensAntes == numTokensDespues, "No debe crearse ningún token para correos no registrados.");
    }

    @Test
    @DisplayName("POST /recuperar-password/restablecer con contraseñas no coincidentes muestra error")
    void testRestablecerPasswordNoCoinciden() throws Exception
    {
        Persona persona = this.personaRepository.findByCorreo("elena.alumno@autoescuela.es").orElseThrow();
        TokenVerificacion token = this.tokenVerificacionService.generarTokenRecuperacion(persona);

        this.mockMvc.perform(post("/recuperar-password/restablecer")
                        .with(csrf())
                        .param("token", token.getToken())
                        .param("password", "nuevaClave123")
                        .param("confirmPassword", "otraClaveDiferente"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/recuperar-password"))
                .andExpect(model().attributeExists("mensajeError"))
                .andExpect(content().string(containsString("Las contraseñas introducidas no coinciden.")));
    }

    @Test
    @DisplayName("POST /recuperar-password/restablecer con clave de menos de 8 caracteres muestra error")
    void testRestablecerPasswordCorta() throws Exception
    {
        Persona persona = this.personaRepository.findByCorreo("elena.alumno@autoescuela.es").orElseThrow();
        TokenVerificacion token = this.tokenVerificacionService.generarTokenRecuperacion(persona);

        this.mockMvc.perform(post("/recuperar-password/restablecer")
                        .with(csrf())
                        .param("token", token.getToken())
                        .param("password", "12345")
                        .param("confirmPassword", "12345"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/recuperar-password"))
                .andExpect(model().attributeExists("mensajeError"))
                .andExpect(content().string(containsString("debe tener al menos 8 caracteres")));
    }

    @Test
    @DisplayName("POST /recuperar-password/restablecer con token inválido redirige a ?tokenInvalido=true")
    void testRestablecerPasswordTokenInvalido() throws Exception
    {
        this.mockMvc.perform(post("/recuperar-password/restablecer")
                        .with(csrf())
                        .param("token", "token-inventado-falso")
                        .param("password", "nuevaClave123")
                        .param("confirmPassword", "nuevaClave123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recuperar-password?tokenInvalido=true"));
    }

    @Test
    @DisplayName("Flujo completo: restablecimiento exitoso de contraseña, token consumido y nuevo acceso en login")
    void testFlujoCompletoRestablecimientoExitoso() throws Exception
    {
        Persona persona = this.personaRepository.findByCorreo("elena.alumno@autoescuela.es").orElseThrow();
        TokenVerificacion token = this.tokenVerificacionService.generarTokenRecuperacion(persona);

        // Accedemos al enlace con el token
        this.mockMvc.perform(get("/recuperar-password?token=" + token.getToken()))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/recuperar-password"))
                .andExpect(content().string(containsString("id=\"panel-restablecer\"")));

        // Enviamos la nueva contraseña
        this.mockMvc.perform(post("/recuperar-password/restablecer")
                        .with(csrf())
                        .param("token", token.getToken())
                        .param("password", "nuevaPasswordSegura123")
                        .param("confirmPassword", "nuevaPasswordSegura123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recuperar-password?exito=true"));

        // Verificamos que el token se marcó como usado en la BD
        TokenVerificacion tokenEnBd = this.tokenVerificacionRepository.findByToken(token.getToken()).orElseThrow();
        assertTrue(tokenEnBd.isUsado(), "El token debe estar marcado como usado tras el restablecimiento.");
        assertFalse(tokenEnBd.isValido(), "El token ya no debe ser válido.");

        // Verificamos que la contraseña de la Persona se actualizó correctamente con BCrypt
        Persona personaActualizada = this.personaRepository.findByCorreo("elena.alumno@autoescuela.es").orElseThrow();
        assertTrue(this.passwordEncoder.matches("nuevaPasswordSegura123", personaActualizada.getPassword()),
                "El hash de la contraseña en base de datos debe coincidir con la nueva clave.");

        // Intentamos reutilizar el mismo token debe fallar
        this.mockMvc.perform(get("/recuperar-password?token=" + token.getToken()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recuperar-password?tokenInvalido=true"));

        // Verificamos que el usuario puede autenticarse en /login con la nueva contraseña
        this.mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "alumno1")
                        .param("password", "nuevaPasswordSegura123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/alumno/dashboard"));
    }
}
