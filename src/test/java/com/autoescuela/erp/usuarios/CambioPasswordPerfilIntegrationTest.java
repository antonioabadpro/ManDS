package com.autoescuela.erp.usuarios;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import com.autoescuela.erp.BaseIntegrationTest;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class CambioPasswordPerfilIntegrationTest extends BaseIntegrationTest
{
    @Test
    @DisplayName("POST /usuario/perfil/solicitar-cambio-password sin autenticación redirige a login")
    void testSolicitarCambioPasswordAnonimoRedirigeLogin() throws Exception
    {
        this.mockMvc.perform(post("/usuario/perfil/solicitar-cambio-password")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("POST /usuario/perfil/solicitar-cambio-password con Admin autenticado devuelve fragmento de confirmación")
    void testSolicitarCambioPasswordAdminExito() throws Exception
    {
        this.mockMvc.perform(post("/usuario/perfil/solicitar-cambio-password")
                        .with(csrf())
                        .param("correo", "admin@autoescuela.es"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/seguridad-perfil :: tarjeta-seguridad-confirmada"))
                .andExpect(model().attributeExists("correo"));
    }

    @Test
    @WithMockUser(username = "alumno", roles = {"ALUMNO"})
    @DisplayName("POST /usuario/perfil/solicitar-cambio-password con Alumno autenticado devuelve fragmento de confirmación")
    void testSolicitarCambioPasswordAlumnoExito() throws Exception
    {
        this.mockMvc.perform(post("/usuario/perfil/solicitar-cambio-password")
                        .with(csrf())
                        .param("correo", "alumno@autoescuela.es"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/seguridad-perfil :: tarjeta-seguridad-confirmada"))
                .andExpect(model().attributeExists("correo"));
    }

    @Test
    @WithMockUser(username = "profesor", roles = {"PROFESOR"})
    @DisplayName("POST /usuario/perfil/solicitar-cambio-password con Profesor autenticado devuelve fragmento de confirmación")
    void testSolicitarCambioPasswordProfesorExito() throws Exception
    {
        this.mockMvc.perform(post("/usuario/perfil/solicitar-cambio-password")
                        .with(csrf())
                        .param("correo", "profesor@autoescuela.es"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/seguridad-perfil :: tarjeta-seguridad-confirmada"))
                .andExpect(model().attributeExists("correo"));
    }
}
