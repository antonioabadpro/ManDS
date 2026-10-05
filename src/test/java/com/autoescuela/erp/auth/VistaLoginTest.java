package com.autoescuela.erp.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import com.autoescuela.erp.BaseIntegrationTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Pruebas de integración para la vista de login.
 * Verifica la resolución de plantillas y los estados de cierre/expiración heredando de BaseIntegrationTest.
 */
class VistaLoginTest extends BaseIntegrationTest
{
    @Test
    @DisplayName("GET /login renderiza correctamente la plantilla de login")
    void testRenderizadoLoginExitoso() throws Exception
    {
        this.mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    @DisplayName("GET /login?error=true responde 200 OK y resuelve la vista de login")
    void testAlertaErrorCredenciales() throws Exception
    {
        this.mockMvc.perform(get("/login?error=true"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    @DisplayName("GET /login?logout=true responde 200 OK y resuelve la vista de login")
    void testAlertaLogoutExitoso() throws Exception
    {
        this.mockMvc.perform(get("/login?logout=true"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    @DisplayName("GET /login?expirada=true responde 200 OK y resuelve la vista de login")
    void testAlertaSesionExpirada() throws Exception
    {
        this.mockMvc.perform(get("/login?expirada=true"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("Usuario autenticado que accede a GET /login -> Cierra sesión y redirige a /login")
    void testUsuarioAutenticadoAccediendoALoginCierraSesion() throws Exception
    {
        this.mockMvc.perform(get("/login"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}
