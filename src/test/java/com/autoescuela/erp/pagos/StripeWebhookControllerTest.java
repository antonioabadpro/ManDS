package com.autoescuela.erp.pagos;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.autoescuela.erp.academico.model.Matricula;
import com.autoescuela.erp.academico.service.AcademicoService;
import com.autoescuela.erp.auth.service.AuthenticationService;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.pagos.service.PagoStripeService;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.stripe.model.checkout.Session;
import org.springframework.mock.web.MockHttpSession;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.springframework.test.context.TestPropertySource;

/**
 * Suite de pruebas de integración para StripeWebhookController.
 * Valida los endpoints públicos de la pasarela (/pagos/checkout,
 * /pagos/checkout/success, /pagos/checkout/cancel) y la verificación criptográfica de Webhooks.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "stripe.webhook.secret=whsec_dummy_secret_for_testing"
})
class StripeWebhookControllerTest
{
    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private AcademicoService academicoService;

    @MockitoBean
    private PagoStripeService pagoStripeService;

    @MockitoBean
    private AuthenticationService autenticacionService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp()
    {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(this.context)
                .apply(springSecurity())
                .build();
    }

    private String generarStripeSignatureHeader(String payload, String secret)
    {
        try
        {
            long timestamp = Instant.now().getEpochSecond();
            String signedPayload = timestamp + "." + payload;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(signedPayload.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash)
            {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return "t=" + timestamp + ",v1=" + hexString;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error al calcular la firma de Stripe en test", e);
        }
    }

    @Test
    @DisplayName("GET /pagos/checkout renderiza la vista 'pagos/checkout' con desglose tarifario")
    void testMostrarCheckoutMatricula() throws Exception
    {
        this.mockMvc.perform(get("/pagos/checkout").param("tipoCarnet", "PERMISO_B"))
                .andExpect(status().isOk())
                .andExpect(view().name("pagos/checkout"))
                .andExpect(content().string(containsString("250,00 €")))
                .andExpect(content().string(containsString("Permiso B")));
    }

    @Test
    @DisplayName("GET /pagos/checkout/success con alumno en sesión formaliza el alta y matrícula")
    void testMostrarExitoCheckoutConSesion() throws Exception
    {
        Matricula matriculaMock = new Matricula();
        Alumno alumnoMock = new Alumno();
        alumnoMock.setNombre("Juan");
        alumnoMock.setDni("12345678Z");
        matriculaMock.setAlumno(alumnoMock);
        matriculaMock.setPermisoCarnet(TipoCarnet.PERMISO_B);

        when(this.academicoService.matricularTrasPago(eq(205L), eq("PERMISO_B"), any(), eq(250.00f))).thenReturn(matriculaMock);

        MockHttpSession sesion = new MockHttpSession();
        sesion.setAttribute("alumnoId", 205L);
        sesion.setAttribute("tipoCarnet", "PERMISO_B");
        sesion.setAttribute("importeTotal", 250.00f);

        this.mockMvc.perform(get("/pagos/checkout/success")
                        .session(sesion)
                        .param("session_id", "cs_test_success_123"))
                .andExpect(status().isOk())
                .andExpect(view().name("pagos/matricula-success"))
                .andExpect(content().string(containsString("¡Pago Confirmado con Éxito!")))
                .andExpect(content().string(containsString("250,00 €")));

        verify(this.academicoService).matricularTrasPago(eq(205L), eq("PERMISO_B"), any(), eq(250.00f));
    }

    @Test
    @DisplayName("GET /pagos/checkout/success renderiza la vista de confirmación exitosa")
    void testMostrarExitoCheckout() throws Exception
    {
        this.mockMvc.perform(get("/pagos/checkout/success").param("session_id", "cs_test_success_123"))
                .andExpect(status().isOk())
                .andExpect(view().name("pagos/matricula-success"))
                .andExpect(content().string(containsString("¡Pago Confirmado con Éxito!")))
                .andExpect(content().string(containsString("Tasa de Matrícula Oficial")));
    }

    @Test
    @DisplayName("GET /pagos/checkout/cancel renderiza la vista de aviso de pago cancelado")
    void testMostrarCancelacionCheckout() throws Exception
    {
        this.mockMvc.perform(get("/pagos/checkout/cancel"))
                .andExpect(status().isOk())
                .andExpect(view().name("pagos/matricula-cancel"))
                .andExpect(content().string(containsString("Pago No Completado")));
    }

    @Test
    @DisplayName("GET /pagos/clases/success procesa y renderiza confirmación de recarga de clases")
    void testMostrarExitoPagoClases() throws Exception
    {
        MockHttpSession sesion = new MockHttpSession();
        sesion.setAttribute("pagoClases_alumnoId", 205L);
        sesion.setAttribute("pagoClases_numero", 15);
        sesion.setAttribute("pagoClases_importe", 400.00f);
        sesion.setAttribute("pagoClases_tipoCarnet", "Permiso B");

        this.mockMvc.perform(get("/pagos/clases/success")
                        .session(sesion)
                        .param("session_id", "cs_test_clases_123"))
                .andExpect(status().isOk())
                .andExpect(view().name("pagos/clases-success"))
                .andExpect(content().string(containsString("¡Clases Adquiridas con Éxito!")))
                .andExpect(content().string(containsString("+15 clases")));

        verify(this.academicoService).recargarSaldoClasesTrasPago(eq(205L), eq(15), eq("cs_test_clases_123"));
    }

    @Test
    @DisplayName("GET /pagos/clases/cancel renderiza aviso de compra de clases cancelada")
    void testMostrarCancelacionPagoClases() throws Exception
    {
        this.mockMvc.perform(get("/pagos/clases/cancel"))
                .andExpect(status().isOk())
                .andExpect(view().name("pagos/clases-cancel"))
                .andExpect(content().string(containsString("Pago de Clases Interrumpido")));
    }

    @Test
    @DisplayName("POST /pagos/webhook rechaza con 400 cuando falta la cabecera 'Stripe-Signature'")
    void testWebhookSinFirma() throws Exception
    {
        this.mockMvc.perform(post("/pagos/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Falta la firma del webhook")));
    }

    @Test
    @DisplayName("POST /pagos/webhook rechaza con 400 cuando la firma criptográfica es inválida")
    void testWebhookFirmaInvalida() throws Exception
    {
        this.mockMvc.perform(post("/pagos/webhook")
                        .header("Stripe-Signature", "t=123456,v1=firma_invalida_falsa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"evt_123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Firma no válida")));
    }

    @Test
    @DisplayName("POST /pagos/webhook procesa exitosamente checkout.session.completed para MATRICULA")
    void testWebhookCheckoutSessionCompletedMatricula() throws Exception
    {
        String payload = String.format("""
        {
          "id": "evt_test_completed_1",
          "object": "event",
          "api_version": "%s",
          "type": "checkout.session.completed",
          "data": {
            "object": {
              "id": "cs_test_session_1",
              "object": "checkout.session",
              "amount_total": 25000,
              "currency": "eur",
              "metadata": {
                "tipoOperacion": "MATRICULA",
                "tipoCarnet": "PERMISO_B",
                "alumnoId": "205"
              }
            }
          }
        }
        """, com.stripe.Stripe.API_VERSION);

        // Obtenemos el secreto configurado en el bean del controlador
        String sigHeader = generarStripeSignatureHeader(payload, "whsec_dummy_secret_for_testing");

        this.mockMvc.perform(post("/pagos/webhook")
                        .header("Stripe-Signature", sigHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Evento de matrícula procesado correctamente")));
    }

    @Test
    @DisplayName("POST /pagos/webhook procesa exitosamente checkout.session.completed para CLASES_PRACTICAS")
    void testWebhookCheckoutSessionCompletedClasesPracticas() throws Exception
    {
        String payload = String.format("""
        {
          "id": "evt_test_clases_1",
          "object": "event",
          "api_version": "%s",
          "type": "checkout.session.completed",
          "data": {
            "object": {
              "id": "cs_test_session_clases_1",
              "object": "checkout.session",
              "amount_total": 40000,
              "currency": "eur",
              "metadata": {
                "tipoOperacion": "CLASES_PRACTICAS",
                "tipoCarnet": "PERMISO_B",
                "alumnoId": "205",
                "numeroClases": "15"
              }
            }
          }
        }
        """, com.stripe.Stripe.API_VERSION);

        String sigHeader = generarStripeSignatureHeader(payload, "whsec_dummy_secret_for_testing");

        this.mockMvc.perform(post("/pagos/webhook")
                        .header("Stripe-Signature", sigHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Evento de clases prácticas procesado correctamente")));

        verify(this.academicoService).recargarSaldoClasesTrasPago(eq(205L), eq(15), eq("cs_test_session_clases_1"));
    }

    @Test
    @DisplayName("POST /pagos/webhook procesa checkout.session.expired purgando al alumno huérfano inactivo")
    void testWebhookCheckoutSessionExpired() throws Exception
    {
        String payload = String.format("""
        {
          "id": "evt_test_expired_1",
          "object": "event",
          "api_version": "%s",
          "type": "checkout.session.expired",
          "data": {
            "object": {
              "id": "cs_test_session_expired",
              "object": "checkout.session",
              "metadata": {
                "tipoOperacion": "MATRICULA",
                "tipoCarnet": "PERMISO_B",
                "alumnoId": "205"
              }
            }
          }
        }
        """, com.stripe.Stripe.API_VERSION);

        String sigHeader = generarStripeSignatureHeader(payload, "whsec_dummy_secret_for_testing");

        this.mockMvc.perform(post("/pagos/webhook")
                        .header("Stripe-Signature", sigHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Evento de sesión de checkout expirada procesado correctamente")));

        verify(this.autenticacionService).eliminarAlumnoInactivoPorId(205L);
    }

    @Test
    @DisplayName("POST /pagos/webhook responde 200 sin 500 si checkout.session.completed llega sin metadata")
    void testWebhookCheckoutSessionCompletedSinMetadata() throws Exception
    {
        String payload = String.format("""
        {
          "id": "evt_test_no_meta",
          "object": "event",
          "api_version": "%s",
          "type": "checkout.session.completed",
          "data": {
            "object": {
              "id": "cs_test_no_meta",
              "object": "checkout.session"
            }
          }
        }
        """, com.stripe.Stripe.API_VERSION);

        String sigHeader = generarStripeSignatureHeader(payload, "whsec_dummy_secret_for_testing");

        this.mockMvc.perform(post("/pagos/webhook")
                        .header("Stripe-Signature", sigHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        verifyNoInteractions(this.academicoService);
    }

    @Test
    @DisplayName("POST /pagos/webhook responde 200 sin 500 cuando falta alumnoId o amount_total es nulo")
    void testWebhookCheckoutSessionCompletedSinAlumnoIdNiAmountTotal() throws Exception
    {
        String payload = String.format("""
        {
          "id": "evt_test_missing_fields",
          "object": "event",
          "api_version": "%s",
          "type": "checkout.session.completed",
          "data": {
            "object": {
              "id": "cs_test_missing_fields",
              "object": "checkout.session",
              "metadata": {
                "tipoOperacion": "MATRICULA"
              }
            }
          }
        }
        """, com.stripe.Stripe.API_VERSION);

        String sigHeader = generarStripeSignatureHeader(payload, "whsec_dummy_secret_for_testing");

        this.mockMvc.perform(post("/pagos/webhook")
                        .header("Stripe-Signature", sigHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        verifyNoInteractions(this.academicoService);
    }

    @Test
    @DisplayName("POST /pagos/webhook responde 200 sin 500 cuando alumnoId en metadata no es numérico")
    void testWebhookCheckoutSessionCompletedAlumnoIdInvalido() throws Exception
    {
        String payload = String.format("""
        {
          "id": "evt_test_invalid_id",
          "object": "event",
          "api_version": "%s",
          "type": "checkout.session.completed",
          "data": {
            "object": {
              "id": "cs_test_invalid_id",
              "object": "checkout.session",
              "amount_total": 25000,
              "metadata": {
                "tipoOperacion": "MATRICULA",
                "alumnoId": "abc"
              }
            }
          }
        }
        """, com.stripe.Stripe.API_VERSION);

        String sigHeader = generarStripeSignatureHeader(payload, "whsec_dummy_secret_for_testing");

        this.mockMvc.perform(post("/pagos/webhook")
                        .header("Stripe-Signature", sigHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        verifyNoInteractions(this.academicoService);
    }

    @Test
    @DisplayName("POST /pagos/webhook procesa exitosamente checkout.session.completed para RENOVACION_MATRICULA")
    void testWebhookCheckoutSessionCompletedRenovacionMatricula() throws Exception
    {
        String payload = String.format("""
        {
          "id": "evt_test_renovacion_1",
          "object": "event",
          "api_version": "%s",
          "type": "checkout.session.completed",
          "data": {
            "object": {
              "id": "cs_test_session_renovacion",
              "object": "checkout.session",
              "amount_total": 25000,
              "currency": "eur",
              "metadata": {
                "tipoOperacion": "RENOVACION_MATRICULA",
                "tipoCarnet": "PERMISO_B",
                "alumnoId": "205"
              }
            }
          }
        }
        """, com.stripe.Stripe.API_VERSION);

        String sigHeader = generarStripeSignatureHeader(payload, "whsec_dummy_secret_for_testing");

        this.mockMvc.perform(post("/pagos/webhook")
                        .header("Stripe-Signature", sigHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Evento de renovación de matrícula procesado correctamente")));

        verify(this.academicoService).renovarMatriculaTrasPago(eq(205L), eq("cs_test_session_renovacion"), eq(250.0f));
    }

    @Test
    @DisplayName("POST /pagos/webhook devuelve 200 OK si un evento obsoleto lanza ReglaNegocioException")
    void testWebhookCheckoutSessionCompletedReglaNegocioExceptionDevuelve200() throws Exception
    {
        doThrow(new ReglaNegocioException("Alumno no encontrado"))
                .when(this.academicoService)
                .matricularTrasPago(eq(9999L), eq("PERMISO_B"), any(), anyFloat());

        String payload = String.format("""
        {
          "id": "evt_test_obsoleto",
          "object": "event",
          "api_version": "%s",
          "type": "checkout.session.completed",
          "data": {
            "object": {
              "id": "cs_test_obsoleto",
              "object": "checkout.session",
              "amount_total": 25000,
              "metadata": {
                "tipoOperacion": "MATRICULA",
                "tipoCarnet": "PERMISO_B",
                "alumnoId": "9999"
              }
            }
          }
        }
        """, com.stripe.Stripe.API_VERSION);

        String sigHeader = generarStripeSignatureHeader(payload, "whsec_dummy_secret_for_testing");

        this.mockMvc.perform(post("/pagos/webhook")
                        .header("Stripe-Signature", sigHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Evento de matrícula procesado correctamente")));
    }

    @Test
    @DisplayName("GET /pagos/clases/success recupera datos desde Stripe cuando falta HttpSession y el pago es paid")
    void testMostrarExitoPagoClasesRecuperaSesionStripeSinHttpSession() throws Exception
    {
        Session stripeSession = new Session();
        stripeSession.setId("cs_test_clases_recover");
        stripeSession.setPaymentStatus("paid");
        stripeSession.setAmountTotal(27000L);
        stripeSession.setMetadata(Map.of(
                "tipoOperacion", "CLASES_PRACTICAS",
                "alumnoId", "205",
                "numeroClases", "10",
                "tipoCarnet", "PERMISO_B"
        ));

        Matricula matriculaMock = new Matricula();
        matriculaMock.setSaldoClases(10);
        matriculaMock.setPermisoCarnet(TipoCarnet.PERMISO_B);

        when(this.pagoStripeService.obtenerSesionStripe("cs_test_clases_recover")).thenReturn(stripeSession);
        when(this.academicoService.recargarSaldoClasesTrasPago(205L, 10, "cs_test_clases_recover")).thenReturn(matriculaMock);

        this.mockMvc.perform(get("/pagos/clases/success")
                        .param("session_id", "cs_test_clases_recover"))
                .andExpect(status().isOk())
                .andExpect(view().name("pagos/clases-success"))
                .andExpect(model().attribute("numeroClases", 10))
                .andExpect(model().attribute("saldoTotal", 10))
                .andExpect(model().attribute("importeTotal", "270,00"));

        verify(this.academicoService).recargarSaldoClasesTrasPago(205L, 10, "cs_test_clases_recover");
    }

    @Test
    @DisplayName("GET /pagos/clases/success ignora sesiones de Stripe cuyo estado no sea paid")
    void testMostrarExitoPagoClasesIgnoraSesionStripeNoPagada() throws Exception
    {
        Session stripeSession = new Session();
        stripeSession.setId("cs_test_unpaid");
        stripeSession.setPaymentStatus("unpaid");
        stripeSession.setAmountTotal(27000L);
        stripeSession.setMetadata(Map.of(
                "tipoOperacion", "CLASES_PRACTICAS",
                "alumnoId", "205",
                "numeroClases", "10"
        ));

        when(this.pagoStripeService.obtenerSesionStripe("cs_test_unpaid")).thenReturn(stripeSession);

        this.mockMvc.perform(get("/pagos/clases/success")
                        .param("session_id", "cs_test_unpaid"))
                .andExpect(status().isOk())
                .andExpect(view().name("pagos/clases-success"))
                .andExpect(model().attribute("numeroClases", 0));

        verifyNoInteractions(this.academicoService);
    }

    @Test
    @DisplayName("GET /pagos/renovacion/success recupera datos desde Stripe cuando falta HttpSession y el pago es paid")
    void testMostrarExitoPagoRenovacionRecuperaSesionStripeSinHttpSession() throws Exception
    {
        Session stripeSession = new Session();
        stripeSession.setId("cs_test_renov_recover");
        stripeSession.setPaymentStatus("paid");
        stripeSession.setAmountTotal(25000L);
        stripeSession.setMetadata(Map.of(
                "tipoOperacion", "RENOVACION_MATRICULA",
                "alumnoId", "205",
                "tipoCarnet", "PERMISO_B"
        ));

        Matricula matriculaMock = new Matricula();
        matriculaMock.setConvocatorias(2);
        matriculaMock.setPermisoCarnet(TipoCarnet.PERMISO_B);

        when(this.pagoStripeService.obtenerSesionStripe("cs_test_renov_recover")).thenReturn(stripeSession);
        when(this.academicoService.renovarMatriculaTrasPago(205L, "cs_test_renov_recover", 250.0f)).thenReturn(matriculaMock);

        this.mockMvc.perform(get("/pagos/renovacion/success")
                        .param("session_id", "cs_test_renov_recover"))
                .andExpect(status().isOk())
                .andExpect(view().name("pagos/renovacion-success"))
                .andExpect(model().attribute("convocatorias", 2))
                .andExpect(model().attribute("importeTotal", "250,00"));

        verify(this.academicoService).renovarMatriculaTrasPago(205L, "cs_test_renov_recover", 250.0f);
    }
}
