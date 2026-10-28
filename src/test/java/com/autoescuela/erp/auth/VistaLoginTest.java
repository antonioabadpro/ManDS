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
 * Verifica la resolución de plantillas, protección contra manipulación de URLs y estados de cierre/expiración.
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
    @DisplayName("GET /login con flash attribute errorLogin muestra la vista de login")
    void testAlertaErrorCredencialesFlashAttribute() throws Exception
    {
        this.mockMvc.perform(get("/login").flashAttr("errorLogin", true))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    @DisplayName("GET /login con flash attribute logoutExitoso muestra la vista de login")
    void testAlertaLogoutExitosoFlashAttribute() throws Exception
    {
        this.mockMvc.perform(get("/login").flashAttr("logoutExitoso", true))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    @DisplayName("GET /login con flash attribute sesionExpirada muestra la vista de login")
    void testAlertaSesionExpiradaFlashAttribute() throws Exception
    {
        this.mockMvc.perform(get("/login").flashAttr("sesionExpirada", true))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    @DisplayName("GET /login con flash attribute registroExitoso muestra la vista de login")
    void testAlertaRegistroExitosoFlashAttribute() throws Exception
    {
        this.mockMvc.perform(get("/login").flashAttr("registroExitoso", true))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    @DisplayName("GET /login con flash attribute cuentaActivada muestra la vista de login")
    void testAlertaCuentaActivadaFlashAttribute() throws Exception
    {
        this.mockMvc.perform(get("/login").flashAttr("cuentaActivada", true))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    @DisplayName("GET /login con flash attribute tokenInvalido muestra la vista de login")
    void testAlertaTokenInvalidoFlashAttribute() throws Exception
    {
        this.mockMvc.perform(get("/login").flashAttr("tokenInvalido", true))
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

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("Usuario autenticado que accede a GET / -> Cierra sesión y redirige a /login")
    void testUsuarioAutenticadoAccediendoAUrlRaizCierraSesion() throws Exception
    {
        this.mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("GET / (URL raíz del programa) redirige automáticamente a /login")
    void testAccesoUrlRaizRedirigeALogin() throws Exception
    {
        this.mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("GET /login/ (con barra final) redirige automáticamente a /login")
    void testAccesoLoginConBarraFinalRedirigeALogin() throws Exception
    {
        this.mockMvc.perform(get("/login/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}
