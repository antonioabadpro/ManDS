package com.autoescuela.erp.pagos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.autoescuela.erp.core.enums.ModalidadMatricula;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.pagos.dto.SesionPagoDTO;
import com.autoescuela.erp.pagos.service.PagoStripeService;
import com.stripe.StripeClient;
import com.stripe.exception.ApiException;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Suite de pruebas unitarias para PagoStripeService.
 * Valida la creación de sesiones de pago con Stripe Checkout, asignación de tarifas
 * según tipo de vehículo (estándar 250 € vs pesado 450 €) y manejo resiliente de StripeException.
 */
@ExtendWith(MockitoExtension.class)
class PagoStripeServiceTest
{
    private StripeClient stripeClient;
    private PagoStripeService pagoStripeService;

    @BeforeEach
    void setUp()
    {
        this.stripeClient = mock(StripeClient.class, RETURNS_DEEP_STUBS);
        this.pagoStripeService = new PagoStripeService(this.stripeClient);
        ReflectionTestUtils.setField(this.pagoStripeService, "appBaseUrl", "http://localhost:8080");
    }

    @Test
    @DisplayName("crearSesionPagoMatricula crea sesión para Permiso B con 250,00 € (25000 céntimos)")
    void testCrearSesionPagoMatriculaPermisoEstandar() throws StripeException
    {
        Session sessionMock = new Session();
        sessionMock.setId("cs_test_abc123");
        sessionMock.setUrl("https://checkout.stripe.com/pay/cs_test_abc123");

        when(this.stripeClient.v1().checkout().sessions().create(any(SessionCreateParams.class)))
                .thenReturn(sessionMock);

        SesionPagoDTO resultado = this.pagoStripeService.crearSesionPagoMatricula(TipoCarnet.PERMISO_B, ModalidadMatricula.TEORICO_PRACTICA, 201L);

        assertNotNull(resultado);
        assertEquals("cs_test_abc123", resultado.idSesion());
        assertEquals("https://checkout.stripe.com/pay/cs_test_abc123", resultado.urlStripe());
        assertEquals(25000L, resultado.importeTotal());
        assertEquals("EUR", resultado.moneda());

        ArgumentCaptor<SessionCreateParams> paramsCaptor = ArgumentCaptor.forClass(SessionCreateParams.class);
        verify(this.stripeClient.v1().checkout().sessions()).create(paramsCaptor.capture());

        SessionCreateParams capturedParams = paramsCaptor.getValue();
        assertEquals("MATRICULA", capturedParams.getMetadata().get("tipoOperacion"));
        assertEquals("PERMISO_B", capturedParams.getMetadata().get("tipoCarnet"));
        assertEquals("201", capturedParams.getMetadata().get("alumnoId"));
        assertEquals(2, capturedParams.getLineItems().size());
        assertEquals(9405L, capturedParams.getLineItems().get(0).getPriceData().getUnitAmount());
        assertEquals(15595L, capturedParams.getLineItems().get(1).getPriceData().getUnitAmount());
    }

    @Test
    @DisplayName("crearSesionPagoMatricula crea sesión para Permiso C (pesado) con 450,00 € (45000 céntimos)")
    void testCrearSesionPagoMatriculaPermisoPesado() throws StripeException
    {
        Session sessionMock = new Session();
        sessionMock.setId("cs_test_heavy450");
        sessionMock.setUrl("https://checkout.stripe.com/pay/cs_test_heavy450");

        when(this.stripeClient.v1().checkout().sessions().create(any(SessionCreateParams.class)))
                .thenReturn(sessionMock);

        SesionPagoDTO resultado = this.pagoStripeService.crearSesionPagoMatricula(TipoCarnet.PERMISO_C, ModalidadMatricula.TEORICO_PRACTICA, 202L);

        assertNotNull(resultado);
        assertEquals(45000L, resultado.importeTotal());
        assertEquals("cs_test_heavy450", resultado.idSesion());
    }

    @Test
    @DisplayName("crearSesionPagoMatricula lanza ReglaNegocioException si tipoCarnet o alumnoId son nulos")
    void testCrearSesionPagoMatriculaCarnetNulo()
    {
        ReglaNegocioException excepcionCarnet = assertThrows(ReglaNegocioException.class, () ->
                this.pagoStripeService.crearSesionPagoMatricula(null, ModalidadMatricula.TEORICO_PRACTICA, 201L)
        );
        assertTrue(excepcionCarnet.getMessage().contains("obligatorios para formalizar la matrícula"));

        ReglaNegocioException excepcionAlumno = assertThrows(ReglaNegocioException.class, () ->
                this.pagoStripeService.crearSesionPagoMatricula(TipoCarnet.PERMISO_B, ModalidadMatricula.TEORICO_PRACTICA, null)
        );
        assertTrue(excepcionAlumno.getMessage().contains("obligatorios para formalizar la matrícula"));
    }

    @Test
    @DisplayName("crearSesionPagoMatricula transforma StripeException en ReglaNegocioException controlada")
    void testCrearSesionPagoStripeException() throws StripeException
    {
        ApiException apiException = new ApiException("Stripe API down", "req_123", "code_err", 500, null);

        when(this.stripeClient.v1().checkout().sessions().create(any(SessionCreateParams.class)))
                .thenThrow(apiException);

        ReglaNegocioException excepcion = assertThrows(ReglaNegocioException.class, () ->
                this.pagoStripeService.crearSesionPagoMatricula(TipoCarnet.PERMISO_B, ModalidadMatricula.TEORICO_PRACTICA, 201L)
        );

        assertTrue(excepcion.getMessage().contains("No se pudo crear la sesión de pago"));
    }

    @Test
    @DisplayName("crearSesionPagoClasesPracticas para Bono 10 en vehículo estándar aplica tarifa con 1 clase gratis (270 €)")
    void testCrearSesionPagoBono10PermisoEstandar() throws StripeException
    {
        Session sessionMock = new Session();
        sessionMock.setId("cs_test_bono10_estandar");
        sessionMock.setUrl("https://checkout.stripe.com/pay/cs_test_bono10_estandar");

        when(this.stripeClient.v1().checkout().sessions().create(any(SessionCreateParams.class)))
                .thenReturn(sessionMock);

        SesionPagoDTO resultado = this.pagoStripeService.crearSesionPagoClasesPracticas(TipoCarnet.PERMISO_B, "12345678Z", 10, true);

        assertNotNull(resultado);
        assertEquals("cs_test_bono10_estandar", resultado.idSesion());
        assertEquals(27000L, resultado.importeTotal());

        ArgumentCaptor<SessionCreateParams> paramsCaptor = ArgumentCaptor.forClass(SessionCreateParams.class);
        verify(this.stripeClient.v1().checkout().sessions()).create(paramsCaptor.capture());

        SessionCreateParams capturedParams = paramsCaptor.getValue();
        assertEquals("CLASES_PRACTICAS", capturedParams.getMetadata().get("tipoOperacion"));
        assertEquals("10", capturedParams.getMetadata().get("numeroClases"));
        assertEquals("true", capturedParams.getMetadata().get("esOferta"));
        assertEquals("12345678Z", capturedParams.getMetadata().get("dniAlumno"));
        assertEquals(1, capturedParams.getLineItems().size());
        assertEquals(27000L, capturedParams.getLineItems().get(0).getPriceData().getUnitAmount());
        assertEquals("Bono 10 Clases Prácticas (1 Clase Gratis) - Permiso B", capturedParams.getLineItems().get(0).getPriceData().getProductData().getName());
        assertEquals("Pack de 10 clases prácticas oficiales de 45 min con 1 clase gratis de regalo incluida para Permiso B (Alumno DNI: 12345678Z).", capturedParams.getLineItems().get(0).getPriceData().getProductData().getDescription());
    }

    @Test
    @DisplayName("crearSesionPagoClasesPracticas para Bono 10 en vehículo pesado aplica tarifa con 1 clase gratis (540 €)")
    void testCrearSesionPagoBono10PermisoPesado() throws StripeException
    {
        Session sessionMock = new Session();
        sessionMock.setId("cs_test_bono10_pesado");
        sessionMock.setUrl("https://checkout.stripe.com/pay/cs_test_bono10_pesado");

        when(this.stripeClient.v1().checkout().sessions().create(any(SessionCreateParams.class)))
                .thenReturn(sessionMock);

        SesionPagoDTO resultado = this.pagoStripeService.crearSesionPagoClasesPracticas(TipoCarnet.PERMISO_C, "87654321X", 10, true);

        assertNotNull(resultado);
        assertEquals(54000L, resultado.importeTotal());

        ArgumentCaptor<SessionCreateParams> paramsCaptor = ArgumentCaptor.forClass(SessionCreateParams.class);
        verify(this.stripeClient.v1().checkout().sessions()).create(paramsCaptor.capture());

        SessionCreateParams capturedParams = paramsCaptor.getValue();
        assertEquals(1, capturedParams.getLineItems().size());
        assertEquals(54000L, capturedParams.getLineItems().get(0).getPriceData().getUnitAmount());
        assertEquals("Bono 10 Clases Prácticas (1 Clase Gratis) - Permiso C", capturedParams.getLineItems().get(0).getPriceData().getProductData().getName());
        assertEquals("Pack de 10 clases prácticas oficiales de 45 min con 1 clase gratis de regalo incluida para Permiso C (Alumno DNI: 87654321X).", capturedParams.getLineItems().get(0).getPriceData().getProductData().getDescription());
    }

    @Test
    @DisplayName("crearSesionPagoMatricula con TEORICO_PRACTICA e ID incluye plataforma de tests sin DNI")
    void testCrearSesionPagoMatriculaTeoricoPracticaConId() throws StripeException
    {
        Session sessionMock = new Session();
        sessionMock.setId("cs_test_tp_250");
        sessionMock.setUrl("https://checkout.stripe.com/pay/cs_test_tp_250");

        when(this.stripeClient.v1().checkout().sessions().create(any(SessionCreateParams.class)))
                .thenReturn(sessionMock);

        SesionPagoDTO resultado = this.pagoStripeService.crearSesionPagoMatricula(TipoCarnet.PERMISO_B, ModalidadMatricula.TEORICO_PRACTICA, 205L);

        assertNotNull(resultado);
        assertEquals(25000L, resultado.importeTotal());

        ArgumentCaptor<SessionCreateParams> paramsCaptor = ArgumentCaptor.forClass(SessionCreateParams.class);
        verify(this.stripeClient.v1().checkout().sessions()).create(paramsCaptor.capture());

        SessionCreateParams capturedParams = paramsCaptor.getValue();
        assertEquals("MATRICULA", capturedParams.getMetadata().get("tipoOperacion"));
        assertEquals("PERMISO_B", capturedParams.getMetadata().get("tipoCarnet"));
        assertEquals("TEORICO_PRACTICA", capturedParams.getMetadata().get("modalidad"));
        assertEquals("205", capturedParams.getMetadata().get("alumnoId"));
        assertTrue(capturedParams.getLineItems().get(1).getPriceData().getProductData().getDescription().contains("credenciales de acceso a la plataforma de tests"));
    }

    @Test
    @DisplayName("crearSesionPagoMatricula con PRACTICA e ID no incluye tests teóricos")
    void testCrearSesionPagoMatriculaPracticaConId() throws StripeException
    {
        Session sessionMock = new Session();
        sessionMock.setId("cs_test_p_250");
        sessionMock.setUrl("https://checkout.stripe.com/pay/cs_test_p_250");

        when(this.stripeClient.v1().checkout().sessions().create(any(SessionCreateParams.class)))
                .thenReturn(sessionMock);

        SesionPagoDTO resultado = this.pagoStripeService.crearSesionPagoMatricula(TipoCarnet.PERMISO_B, ModalidadMatricula.PRACTICA, 206L);

        assertNotNull(resultado);
        assertEquals(25000L, resultado.importeTotal());

        ArgumentCaptor<SessionCreateParams> paramsCaptor = ArgumentCaptor.forClass(SessionCreateParams.class);
        verify(this.stripeClient.v1().checkout().sessions()).create(paramsCaptor.capture());

        SessionCreateParams capturedParams = paramsCaptor.getValue();
        assertEquals("PRACTICA", capturedParams.getMetadata().get("modalidad"));
        assertEquals("206", capturedParams.getMetadata().get("alumnoId"));
        assertTrue(capturedParams.getLineItems().get(1).getPriceData().getProductData().getDescription().contains("examen práctico"));
    }

    @Test
    @DisplayName("crearSesionPagoRenovacionMatricula crea sesión con alumnoId en metadata sin DNI")
    void testCrearSesionPagoRenovacionMatriculaConId() throws StripeException
    {
        Session sessionMock = new Session();
        sessionMock.setId("cs_test_renovacion_250");
        sessionMock.setUrl("https://checkout.stripe.com/pay/cs_test_renovacion_250");

        when(this.stripeClient.v1().checkout().sessions().create(any(SessionCreateParams.class)))
                .thenReturn(sessionMock);

        SesionPagoDTO resultado = this.pagoStripeService.crearSesionPagoRenovacionMatricula(TipoCarnet.PERMISO_B, 205L);

        assertNotNull(resultado);
        assertEquals(25000L, resultado.importeTotal());

        ArgumentCaptor<SessionCreateParams> paramsCaptor = ArgumentCaptor.forClass(SessionCreateParams.class);
        verify(this.stripeClient.v1().checkout().sessions()).create(paramsCaptor.capture());

        SessionCreateParams capturedParams = paramsCaptor.getValue();
        assertEquals("RENOVACION_MATRICULA", capturedParams.getMetadata().get("tipoOperacion"));
        assertEquals("PERMISO_B", capturedParams.getMetadata().get("tipoCarnet"));
        assertEquals("205", capturedParams.getMetadata().get("alumnoId"));
        assertNull(capturedParams.getMetadata().get("dniAlumno"));
    }
}
