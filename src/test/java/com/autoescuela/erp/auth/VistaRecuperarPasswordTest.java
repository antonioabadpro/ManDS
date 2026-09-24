package com.autoescuela.erp.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

import com.autoescuela.erp.auth.model.TokenVerificacion;
import com.autoescuela.erp.auth.service.TokenVerificacionService;
import com.autoescuela.erp.usuarios.model.Persona;
import com.autoescuela.erp.usuarios.repository.PersonaRepository;

@SpringBootTest
@TestPropertySource(properties =
{
        "spring.sql.init.mode=always",
        "spring.sql.init.data-locations=classpath:data.sql",
        "spring.jpa.defer-datasource-initialization=true"
})
/**
 * Pruebas de unidad para la vista de recuperación de contraseña.
 * VistaRecuperarPasswordTest contiene pruebas para verificar el renderizado correcto de la vista de recuperación de contraseña en diferentes escenarios (sin token, con token válido, con token inválido, etc.).
 * Se utilizan Mocks para simular solicitudes HTTP y verificar el contenido de la respuesta.
 */
class VistaRecuperarPasswordTest
{
    @Autowired
    private WebApplicationContext contexto;

    @Autowired
    private TokenVerificacionService tokenVerificacionService;

    @Autowired
    private PersonaRepository personaRepository;

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
    @DisplayName("GET /recuperar-password sin token renderiza el formulario de solicitud de enlace con branding ManDS")
    void testRenderizadoSolicitudSinToken() throws Exception
    {
        this.mockMvc.perform(get("/recuperar-password"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/recuperar-password"))
                .andExpect(content().string(containsString("ManDS")))
                .andExpect(content().string(containsString("Recuperación Segura de Acceso")))
                .andExpect(content().string(containsString("id=\"panel-solicitud\"")))
                .andExpect(content().string(containsString("id=\"recuperar-correo\"")))
                .andExpect(content().string(containsString("action=\"/recuperar-password\"")))
                .andExpect(content().string(containsString("hx-post=\"/recuperar-password\"")))
                .andExpect(content().string(containsString("hx-disabled-elt=\"#btn-solicitar-recuperacion\"")))
                .andExpect(content().string(containsString("id=\"btn-solicitar-recuperacion\"")))
                .andExpect(content().string(containsString("htmx-indicator")))
                .andExpect(content().string(containsString("href=\"/login\"")))
                .andExpect(content().string(containsString("/js/recuperar-password.js")));
    }

    @Test
    @DisplayName("GET /recuperar-password?enviado=true muestra alerta de correo enviado")
    void testAlertaCorreoEnviado() throws Exception
    {
        this.mockMvc.perform(get("/recuperar-password?enviado=true"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Instrucciones enviadas")));
    }

    @Test
    @DisplayName("GET /recuperar-password con parámetro token válido renderiza el formulario de restablecimiento de contraseña")
    void testRenderizadoFormularioConToken() throws Exception
    {
        Persona persona = this.personaRepository.findByCorreo("admin@autoescuela.es").orElseThrow();
        TokenVerificacion token = this.tokenVerificacionService.generarTokenRecuperacion(persona, 15);

        this.mockMvc.perform(get("/recuperar-password?token=" + token.getToken()))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/recuperar-password"))
                .andExpect(content().string(containsString("id=\"panel-restablecer\"")))
                .andExpect(content().string(containsString(token.getToken())))
                .andExpect(content().string(containsString("id=\"nueva-password\"")))
                .andExpect(content().string(containsString("id=\"confirmar-password\"")))
                .andExpect(content().string(containsString("action=\"/recuperar-password/restablecer\"")))
                .andExpect(content().string(containsString("id=\"btn-toggle-nueva-pass\"")))
                .andExpect(content().string(containsString("id=\"btn-toggle-confirmar-pass\"")));
    }

    @Test
    @DisplayName("GET /recuperar-password con token inexistente redirige a ?tokenInvalido=true")
    void testTokenInexistenteRedirige() throws Exception
    {
        this.mockMvc.perform(get("/recuperar-password?token=token_falso_inexistente"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recuperar-password?tokenInvalido=true"));
    }

    @Test
    @DisplayName("GET /recuperar-password?tokenInvalido=true muestra alerta de token no válido o caducado")
    void testAlertaTokenInvalido() throws Exception
    {
        this.mockMvc.perform(get("/recuperar-password?tokenInvalido=true"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Enlace caducado o no válido")));
    }

    @Test
    @DisplayName("GET /recuperar-password?exito=true muestra la pantalla de contraseña restablecida con botón a login")
    void testPantallaExitoRestablecimiento() throws Exception
    {
        this.mockMvc.perform(get("/recuperar-password?exito=true"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("¡Contraseña restablecida!")))
                .andExpect(content().string(containsString("href=\"/login\"")));
    }
}
