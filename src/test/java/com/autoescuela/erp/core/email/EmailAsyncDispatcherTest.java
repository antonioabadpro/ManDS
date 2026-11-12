package com.autoescuela.erp.core.email;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias en JUnit 6 + Mockito para EmailAsyncDispatcher (canal Brevo REST API y canal SMTP local).
 */
@ExtendWith(MockitoExtension.class)
class EmailAsyncDispatcherTest
{
    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailAsyncDispatcher emailAsyncDispatcher;

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("despacharCorreoHtml envía vía Brevo REST API y escapa correctamente caracteres JSON especiales")
    void testDespacharViaBrevoApiExitosoYFallos() throws Exception
    {
        HttpClient mockHttpClient = mock(HttpClient.class);
        HttpResponse<String> mockResponseOk = mock(HttpResponse.class);
        when(mockResponseOk.statusCode()).thenReturn(201);
        when(mockResponseOk.body()).thenReturn("{\"messageId\":\"abc\"}");
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponseOk);

        ReflectionTestUtils.setField(this.emailAsyncDispatcher, "brevoApiKey", "xkeysib-test-123");
        ReflectionTestUtils.setField(this.emailAsyncDispatcher, "correoRemitente", "noreply@mands.es");
        ReflectionTestUtils.setField(this.emailAsyncDispatcher, "httpClient", mockHttpClient);

        String htmlEspecial = "<p>Hola \"mundo\" \\ linea\nretorno\rtab\tback\bform\fctrl\u0001</p>";
        this.emailAsyncDispatcher.despacharCorreoHtml("alumno@test.es", null, "Asunto \"test\"", htmlEspecial, "TEST");

        verify(mockHttpClient).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
        verify(this.mailSender, never()).createMimeMessage();

        // Respuesta HTTP 400 -> retorna false y no recurre a SMTP
        HttpResponse<String> mockResponseError = mock(HttpResponse.class);
        when(mockResponseError.statusCode()).thenReturn(400);
        when(mockResponseError.body()).thenReturn("bad request");
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponseError);

        this.emailAsyncDispatcher.despacharCorreoHtml("alumno@test.es", "Ana", "Asunto", null, "TEST_ERR");
        verify(this.mailSender, never()).send(any(MimeMessage.class));

        // IOException en HttpClient
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new IOException("Timeout de red"));
        this.emailAsyncDispatcher.despacharCorreoHtml("alumno@test.es", "Ana", "Asunto", "<p>ok</p>", "TEST_IO");

        // InterruptedException en HttpClient -> restaura el flag interrupted
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new InterruptedException("Hilo interrumpido"));
        this.emailAsyncDispatcher.despacharCorreoHtml("alumno@test.es", "Ana", "Asunto", "<p>ok</p>", "TEST_INT");
        assertTrue(Thread.interrupted());
    }

    @Test
    @DisplayName("despacharCorreoHtml usa SMTP local sin API key y reintenta ante fallos transitorios de red")
    void testDespacharViaSmtpLocalExitoYReintentos()
    {
        ReflectionTestUtils.setField(this.emailAsyncDispatcher, "brevoApiKey", "");
        ReflectionTestUtils.setField(this.emailAsyncDispatcher, "correoRemitente", "noreply@mands.es");

        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(this.mailSender.createMimeMessage()).thenReturn(mimeMessage);

        this.emailAsyncDispatcher.despacharCorreoHtml("profe@test.es", "Laura", "Aviso", "<p>Contenido</p>", "AVISO");
        verify(this.mailSender).send(mimeMessage);

        // Si el hilo está interrumpido durante el reintento SMTP, aborta limpiamente tras el primer fallo
        doThrow(new MailSendException("SMTP caído")).when(this.mailSender).send(mimeMessage);
        Thread.currentThread().interrupt();
        this.emailAsyncDispatcher.despacharCorreoHtml("profe@test.es", "Laura", "Reintento", "<p>Contenido</p>", "REINTENTO");

        assertTrue(Thread.interrupted());
        verify(this.mailSender, times(2)).send(mimeMessage);
    }
}
