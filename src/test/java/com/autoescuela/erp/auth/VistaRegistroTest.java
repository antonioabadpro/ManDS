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
import static org.hamcrest.Matchers.not;
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
                .webAppContextSetup(this.contexto)
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
    @DisplayName("GET /registro contiene la estructura visual del indicador de progreso (Stepper de 4 pasos)")
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
                .andExpect(content().string(containsString("Contacto")))
                .andExpect(content().string(containsString("id=\"step-line-3\"")))
                .andExpect(content().string(containsString("id=\"step-indicator-4\"")))
                .andExpect(content().string(containsString("id=\"step-label-4\"")))
                .andExpect(content().string(containsString("Pago")));
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
    @DisplayName("GET /registro contiene el Paso 3: Contacto, carnet, RGPD y botón de avance a pago")
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
                .andExpect(content().string(containsString("id=\"reg-tipoCarnet\"")))
                .andExpect(content().string(containsString("name=\"tipoCarnet\"")))
                .andExpect(content().string(containsString("id=\"terminos\"")))
                .andExpect(content().string(containsString("name=\"terminos\"")))
                .andExpect(content().string(containsString("id=\"btn-prev-step-3\"")))
                .andExpect(content().string(containsString("id=\"btn-next-step-3\"")));
    }

    @Test
    @DisplayName("GET /registro contiene el Paso 4: Información de tasa, enlace a pasarela y botón de finalización")
    void testPaso4TasaMatriculacionYPasarela() throws Exception
    {
        this.mockMvc.perform(get("/registro"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"panel-step-4\"")))
                .andExpect(content().string(containsString("Abono de Tasa de Matrícula")))
                .andExpect(content().string(containsString("id=\"btn-ir-pago\"")))
                .andExpect(content().string(containsString("target=\"_blank\"")))
                .andExpect(content().string(containsString("/pagos/checkout")))
                .andExpect(content().string(containsString("id=\"btn-prev-step-4\"")))
                .andExpect(content().string(containsString("id=\"btn-submit-registro\"")))
                .andExpect(content().string(containsString("Finalizar Registro")));
    }

    @Test
    @DisplayName("GET /pagos/checkout renderiza la interfaz bancaria con tarifas de carnet correctas")
    void testRenderizadoCheckoutPasarela() throws Exception
    {
        // Carnet Estándar (Permiso B -> 250,00 €)
        this.mockMvc.perform(get("/pagos/checkout").param("tipoCarnet", "PERMISO_B"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("250,00 €")))
                .andExpect(content().string(containsString("Permiso B")))
                .andExpect(content().string(containsString("id=\"card-holder\"")))
                .andExpect(content().string(containsString("id=\"card-number\"")))
                .andExpect(content().string(containsString("id=\"card-expiry\"")))
                .andExpect(content().string(containsString("id=\"card-cvv\"")))
                .andExpect(content().string(containsString("id=\"feedback-card-holder\"")))
                .andExpect(content().string(containsString("id=\"feedback-card-number\"")))
                .andExpect(content().string(containsString("id=\"feedback-card-expiry\"")))
                .andExpect(content().string(containsString("id=\"feedback-card-cvv\"")))
                .andExpect(content().string(not(containsString("id=\"guardar-tarjeta\""))));

        // Carnet Pesado (Permiso C -> 450,00 €)
        this.mockMvc.perform(get("/pagos/checkout").param("tipoCarnet", "PERMISO_C"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("450,00 €")))
                .andExpect(content().string(containsString("Vehículos Pesados")));
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
