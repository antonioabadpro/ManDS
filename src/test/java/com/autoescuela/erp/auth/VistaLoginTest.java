package com.autoescuela.erp.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.junit.jupiter.api.BeforeEach;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@TestPropertySource(properties =
{
        "spring.sql.init.mode=always",
        "spring.sql.init.data-locations=classpath:data.sql",
        "spring.jpa.defer-datasource-initialization=true"
})
class VistaLoginTest
{
    @Autowired
    private WebApplicationContext contexto;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp()
    {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(contexto)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("GET /login renderiza correctamente la plantilla con branding ManDS y enlace a registro")
    void testRenderizadoLoginExitoso() throws Exception
    {
        this.mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(content().string(containsString("ManDS")))
                .andExpect(content().string(containsString("Manager Driving School")))
                .andExpect(content().string(containsString("action=\"/login\"")))
                .andExpect(content().string(containsString("id=\"login-username\"")))
                .andExpect(content().string(containsString("id=\"login-password\"")))
                .andExpect(content().string(containsString("href=\"/registro\"")))
                .andExpect(content().string(containsString("/js/login.js")))
                .andExpect(content().string(containsString("id=\"btn-ocultar-password-login\"")));
    }

    @Test
    @DisplayName("GET /registro renderiza correctamente el asistente multi-paso con branding ManDS")
    void testRenderizadoRegistroExitoso() throws Exception
    {
        this.mockMvc.perform(get("/registro"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/registro"))
                .andExpect(content().string(containsString("ManDS")))
                .andExpect(content().string(containsString("action=\"/registro\"")))
                .andExpect(content().string(containsString("id=\"step-indicator-1\"")))
                .andExpect(content().string(containsString("id=\"panel-step-1\"")))
                .andExpect(content().string(containsString("id=\"panel-step-2\"")))
                .andExpect(content().string(containsString("id=\"panel-step-3\"")))
                .andExpect(content().string(containsString("/js/registro.js")))
                .andExpect(content().string(containsString("id=\"btn-next-step-1\"")));
    }

    @Test
    @DisplayName("GET /login?error=true muestra la alerta de credenciales incorrectas")
    void testAlertaErrorCredenciales() throws Exception
    {
        this.mockMvc.perform(get("/login?error=true"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Credenciales incorrectas")));
    }

    @Test
    @DisplayName("GET /login?logout=true muestra la alerta de cierre de sesión")
    void testAlertaLogoutExitoso() throws Exception
    {
        this.mockMvc.perform(get("/login?logout=true"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Sesión finalizada con éxito")));
    }

    @Test
    @DisplayName("GET /login?expirada=true muestra la alerta de sesión expirada")
    void testAlertaSesionExpirada() throws Exception
    {
        this.mockMvc.perform(get("/login?expirada=true"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Sesión caducada")));
    }
}
