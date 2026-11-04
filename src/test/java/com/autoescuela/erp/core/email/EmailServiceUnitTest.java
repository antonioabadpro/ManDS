package com.autoescuela.erp.core.email;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

@ExtendWith(MockitoExtension.class)
class EmailServiceUnitTest
{
    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp()
    {
        ReflectionTestUtils.setField(this.emailService, "correoRemitente", "mands.notifications@gmail.com");
        ReflectionTestUtils.setField(this.emailService, "appBaseUrl", "http://localhost:8080");
    }

    @Test
    @DisplayName("enviarBienvenidaAlumno despacha vía SMTP fallback cuando brevoApiKey no está configurada")
    void enviarBienvenidaAlumno_debeDespacharViaSmtp()
    {
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(this.mailSender.createMimeMessage()).thenReturn(mimeMessage);

        assertDoesNotThrow(() ->
            this.emailService.enviarBienvenidaAlumno("antonioabadpro@gmail.com", "Antonio", "Permiso B", 250.0f)
        );

        verify(this.mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("enviarCorreoRecuperacion despacha vía SMTP fallback correctamente")
    void enviarCorreoRecuperacion_debeDespacharViaSmtp()
    {
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(this.mailSender.createMimeMessage()).thenReturn(mimeMessage);

        assertDoesNotThrow(() ->
            this.emailService.enviarCorreoRecuperacion("alumno@autoescuela.es", "Alumno", "http://localhost:8080/token", "15")
        );

        verify(this.mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("enviarInvitacionProfesor despacha vía SMTP fallback correctamente")
    void enviarInvitacionProfesor_debeDespacharViaSmtp()
    {
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(this.mailSender.createMimeMessage()).thenReturn(mimeMessage);

        assertDoesNotThrow(() ->
            this.emailService.enviarInvitacionProfesor("profesor@autoescuela.es", "Profesor", "http://localhost:8080/activar", "60")
        );

        verify(this.mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("enviarNotificacionAlumno despacha texto plano vía SMTP fallback correctamente")
    void enviarNotificacionAlumno_debeDespacharViaSmtp()
    {
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(this.mailSender.createMimeMessage()).thenReturn(mimeMessage);

        assertDoesNotThrow(() ->
            this.emailService.enviarNotificacionAlumno("alumno@autoescuela.es", "Aviso de Clase", "Tu clase de mañana cambia de hora")
        );

        verify(this.mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("enviarNotificacionProfesor despacha texto plano vía SMTP fallback correctamente")
    void enviarNotificacionProfesor_debeDespacharViaSmtp()
    {
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(this.mailSender.createMimeMessage()).thenReturn(mimeMessage);

        assertDoesNotThrow(() ->
            this.emailService.enviarNotificacionProfesor("profesor@autoescuela.es", "Asignación de Alumno", "Se te ha asignado un nuevo alumno")
        );

        verify(this.mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("enviarNotificacionIncidenciaAdmin despacha vía SMTP fallback correctamente")
    void enviarNotificacionIncidenciaAdmin_debeDespacharViaSmtp()
    {
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(this.mailSender.createMimeMessage()).thenReturn(mimeMessage);

        assertDoesNotThrow(() ->
            this.emailService.enviarNotificacionIncidenciaAdmin("admin@autoescuela.es", "Admin", "Profesor", "Seat Ibiza", "1234-BBB", "Rueda pinchada", "http://localhost:8080/incidencias")
        );

        verify(this.mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Cuando brevoApiKey está configurada y la API HTTP responde 201, despacha por Brevo API y omite SMTP")
    @SuppressWarnings("unchecked")
    void despachar_conBrevoApiKeyExitosa_debeOmitirSmtp() throws IOException, InterruptedException
    {
        ReflectionTestUtils.setField(this.emailService, "brevoApiKey", "xkeysib-test-fake-key-12345");

        HttpClient mockHttpClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(201);
        when(mockResponse.body()).thenReturn("{\"messageId\":\"<test-1234@smtp-relay.mailin.fr>\"}");
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);

        ReflectionTestUtils.setField(this.emailService, "httpClient", mockHttpClient);

        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(this.mailSender.createMimeMessage()).thenReturn(mimeMessage);

        assertDoesNotThrow(() ->
            this.emailService.enviarBienvenidaAlumno("antonio@test.es", "Antonio", "Permiso B", 250.0f)
        );

        verify(mockHttpClient).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
        verify(this.mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Cuando brevoApiKey falla con HTTP 401, registra el error y no ejecuta SMTP en producción")
    @SuppressWarnings("unchecked")
    void despachar_conBrevoApiKeyFallida_noDebeEjecutarSmtpEnProduccion() throws IOException, InterruptedException
    {
        ReflectionTestUtils.setField(this.emailService, "brevoApiKey", "xkeysib-test-fake-key-12345");

        HttpClient mockHttpClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(401);
        when(mockResponse.body()).thenReturn("{\"code\":\"unauthorized\"}");
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);

        ReflectionTestUtils.setField(this.emailService, "httpClient", mockHttpClient);

        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(this.mailSender.createMimeMessage()).thenReturn(mimeMessage);

        assertDoesNotThrow(() ->
            this.emailService.enviarBienvenidaAlumno("antonio@test.es", "Antonio", "Permiso B", 250.0f)
        );

        verify(mockHttpClient).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
        verify(this.mailSender, never()).send(any(MimeMessage.class));
    }
}
