package com.autoescuela.erp.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.autoescuela.erp.BaseIntegrationTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Pruebas de integración de vista para el asistente de registro de alumnos (registro.html).
 * Verifica la resolución de plantillas y endpoints del proceso de alta heredando de BaseIntegrationTest.
 */
class VistaRegistroTest extends BaseIntegrationTest
{
    @Test
    @DisplayName("GET /registro renderiza la vista auth/registro")
    void testRenderizadoBasicoYBranding() throws Exception
    {
        this.mockMvc.perform(get("/registro"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/registro"));
    }

    @Test
    @DisplayName("GET /pagos/checkout responde 200 OK con tarifas de carnet estándar y pesado")
    void testRenderizadoCheckoutPasarela() throws Exception
    {
        this.mockMvc.perform(get("/pagos/checkout").param("tipoCarnet", "PERMISO_B"))
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/pagos/checkout").param("tipoCarnet", "PERMISO_C"))
                .andExpect(status().isOk());
    }
}
