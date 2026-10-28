package com.autoescuela.erp.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.autoescuela.erp.BaseIntegrationTest;
import com.autoescuela.erp.auth.model.TokenVerificacion;
import com.autoescuela.erp.auth.service.TokenVerificacionService;
import com.autoescuela.erp.usuarios.model.Persona;
import com.autoescuela.erp.usuarios.repository.PersonaRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Pruebas de integración para la vista de recuperación de contraseña.
 * Verifica la resolución de plantillas y las redirecciones de validación de tokens heredando de BaseIntegrationTest.
 */
class VistaRecuperarPasswordTest extends BaseIntegrationTest
{
    @Autowired
    private TokenVerificacionService tokenVerificacionService;

    @Autowired
    private PersonaRepository personaRepository;

    @Test
    @DisplayName("GET /recuperar-password sin token renderiza el formulario de solicitud de enlace")
    void testRenderizadoSolicitudSinToken() throws Exception
    {
        this.mockMvc.perform(get("/recuperar-password"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/recuperar-password"));
    }

    @Test
    @DisplayName("GET /recuperar-password con flash attribute mensajeEnviado responde 200 OK y resuelve la plantilla")
    void testAlertaCorreoEnviadoConFlashAttribute() throws Exception
    {
        this.mockMvc.perform(get("/recuperar-password").flashAttr("mensajeEnviado", true))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/recuperar-password"));
    }

    @Test
    @DisplayName("GET /recuperar-password con token válido renderiza el formulario de restablecimiento")
    void testRenderizadoFormularioConToken() throws Exception
    {
        Persona persona = this.personaRepository.findByCorreo("admin@autoescuela.es").orElseThrow();
        TokenVerificacion token = this.tokenVerificacionService.generarTokenRecuperacion(persona, 15);

        this.mockMvc.perform(get("/recuperar-password?token=" + token.getToken()))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/recuperar-password"));
    }

    @Test
    @DisplayName("GET /recuperar-password con token inexistente redirige a /recuperar-password con flash attribute tokenInvalido")
    void testTokenInexistenteRedirige() throws Exception
    {
        this.mockMvc.perform(get("/recuperar-password?token=token_falso_inexistente"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recuperar-password"))
                .andExpect(flash().attribute("tokenInvalido", true));
    }

    @Test
    @DisplayName("GET /recuperar-password con flash attribute tokenInvalido responde 200 OK y resuelve la plantilla")
    void testAlertaTokenInvalidoConFlashAttribute() throws Exception
    {
        this.mockMvc.perform(get("/recuperar-password").flashAttr("tokenInvalido", true))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/recuperar-password"));
    }

    @Test
    @DisplayName("GET /recuperar-password con flash attribute mensajeExito responde 200 OK y resuelve la plantilla")
    void testPantallaExitoRestablecimientoConFlashAttribute() throws Exception
    {
        this.mockMvc.perform(get("/recuperar-password").flashAttr("mensajeExito", true))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/recuperar-password"));
    }
}
