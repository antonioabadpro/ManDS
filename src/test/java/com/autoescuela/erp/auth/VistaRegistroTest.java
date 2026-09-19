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

@SpringBootTest
@TestPropertySource(properties =
    {
        "spring.sql.init.mode=always",
        "spring.sql.init.data-locations=classpath:data.sql",
        "spring.jpa.defer-datasource-initialization=true"
    })
    /**
     * Pruebas de integración de vista para el asistente multi-paso de registro de alumnos (registro.html).
     * Verifica la resolución de la plantilla Thymeleaf, branding corporativo, metaetiquetas de seguridad,
     * componentes del stepper y estructura íntegra de los tres paneles del formulario.
     * VistaReistroTest contiene pruebas para verificar el renderizado correcto de la vista de registro en diferentes escenarios (sin token, con token válido, con token inválido, etc.).
     * Se utilizan Mocks para simular solicitudes HTTP y verificar el contenido de la respuesta.
     */
class VistaRegistroTest
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
    @DisplayName("GET /registro renderiza la vista auth/registro")
    void testRenderizadoBasicoYBranding() throws Exception
    {
        this.mockMvc.perform(get("/registro"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/registro"));
    }

    @Test
    @DisplayName("GET /registro incluye las metaetiquetas CSRF y los scripts clientes requeridos")
    void testMetadatosSeguridadYScripts() throws Exception
    {
        this.mockMvc.perform(get("/registro"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"_csrf\"")))
                .andExpect(content().string(containsString("name=\"_csrf_header\"")))
                .andExpect(content().string(containsString("htmx.org")))
                .andExpect(content().string(containsString("/js/htmx-config.js")))
                .andExpect(content().string(containsString("/js/registro.js")));
    }

    @Test
    @DisplayName("GET /registro contiene la estructura visual del indicador de progreso (Stepper de 3 pasos)")
    void testEstructuraStepperProgreso() throws Exception
    {
        this.mockMvc.perform(get("/registro"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"step-indicator-1\"")))
                .andExpect(content().string(containsString("id=\"step-label-1\"")))
                .andExpect(content().string(containsString("Acceso")))
                .andExpect(content().string(containsString("id=\"step-line-1\"")))
                .andExpect(content().string(containsString("id=\"step-indicator-2\"")))
                .andExpect(content().string(containsString("id=\"step-label-2\"")))
                .andExpect(content().string(containsString("Datos")))
                .andExpect(content().string(containsString("id=\"step-line-2\"")))
                .andExpect(content().string(containsString("id=\"step-indicator-3\"")))
                .andExpect(content().string(containsString("id=\"step-label-3\"")))
                .andExpect(content().string(containsString("Contacto")));
    }

    @Test
    @DisplayName("GET /registro contiene el Paso 1: Credenciales de acceso y botones de control")
    void testPaso1CredencialesAcceso() throws Exception
    {
        this.mockMvc.perform(get("/registro"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"form-registro\"")))
                .andExpect(content().string(containsString("action=\"/registro\"")))
                .andExpect(content().string(containsString("method=\"post\"")))
                .andExpect(content().string(containsString("id=\"panel-step-1\"")))
                .andExpect(content().string(containsString("Crea tu cuenta de alumno")))
                .andExpect(content().string(containsString("id=\"reg-nombreUsuario\"")))
                .andExpect(content().string(containsString("name=\"nombreUsuario\"")))
                .andExpect(content().string(containsString("id=\"reg-correo\"")))
                .andExpect(content().string(containsString("name=\"correo\"")))
                .andExpect(content().string(containsString("id=\"reg-password\"")))
                .andExpect(content().string(containsString("name=\"password\"")))
                .andExpect(content().string(containsString("id=\"reg-confirmPassword\"")))
                .andExpect(content().string(containsString("name=\"confirmPassword\"")))
                .andExpect(content().string(containsString("id=\"btn-ocultar-password-registro\"")))
                .andExpect(content().string(containsString("id=\"btn-ocultar-confirm-password\"")))
                .andExpect(content().string(containsString("id=\"btn-next-step-1\"")));
    }

    @Test
    @DisplayName("GET /registro contiene el Paso 2: Datos personales del alumno y controles de navegación")
    void testPaso2DatosPersonales() throws Exception
    {
        this.mockMvc.perform(get("/registro"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"panel-step-2\"")))
                .andExpect(content().string(containsString("Datos personales")))
                .andExpect(content().string(containsString("id=\"reg-nombre\"")))
                .andExpect(content().string(containsString("name=\"nombre\"")))
                .andExpect(content().string(containsString("id=\"reg-apellidos\"")))
                .andExpect(content().string(containsString("name=\"apellidos\"")))
                .andExpect(content().string(containsString("id=\"reg-dni\"")))
                .andExpect(content().string(containsString("name=\"dni\"")))
                .andExpect(content().string(containsString("id=\"reg-fechaNacimiento\"")))
                .andExpect(content().string(containsString("name=\"fechaNacimiento\"")))
                .andExpect(content().string(containsString("id=\"btn-prev-step-2\"")))
                .andExpect(content().string(containsString("id=\"btn-next-step-2\"")));
    }

    @Test
    @DisplayName("GET /registro contiene el Paso 3: Contacto, RGPD y botón de envío final")
    void testPaso3ContactoYTerminos() throws Exception
    {
        this.mockMvc.perform(get("/registro"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"panel-step-3\"")))
                .andExpect(content().string(containsString("Contacto y finalización")))
                .andExpect(content().string(containsString("id=\"reg-telefono\"")))
                .andExpect(content().string(containsString("name=\"telefono\"")))
                .andExpect(content().string(containsString("id=\"reg-direccion\"")))
                .andExpect(content().string(containsString("name=\"direccion\"")))
                .andExpect(content().string(containsString("id=\"terminos\"")))
                .andExpect(content().string(containsString("name=\"terminos\"")))
                .andExpect(content().string(containsString("id=\"btn-prev-step-3\"")))
                .andExpect(content().string(containsString("type=\"submit\"")))
                .andExpect(content().string(containsString("Finalizar Registro")));
    }

    @Test
    @DisplayName("GET /registro contiene el enlace de retorno hacia la pantalla de inicio de sesión (/login)")
    void testEnlaceRetornoLogin() throws Exception
    {
        this.mockMvc.perform(get("/registro"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/login\"")))
                .andExpect(content().string(containsString("Inicia sesión aquí")));
    }
}
