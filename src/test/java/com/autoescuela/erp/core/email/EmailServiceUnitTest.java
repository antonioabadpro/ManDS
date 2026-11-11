package com.autoescuela.erp.core.email;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import com.autoescuela.erp.core.enums.Rol;

import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

@ExtendWith(MockitoExtension.class)
class EmailServiceUnitTest
{
    @Mock
    private JavaMailSender mailSender;

    private EmailAsyncDispatcher emailAsyncDispatcher;
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp()
    {
        this.emailAsyncDispatcher = new EmailAsyncDispatcher(this.mailSender);
        ReflectionTestUtils.setField(this.emailAsyncDispatcher, "correoRemitente", "mands.notifications@gmail.com");

        this.emailService = new EmailServiceImpl(this.emailAsyncDispatcher);
        ReflectionTestUtils.setField(this.emailService, "appBaseUrl", "http://localhost:8080");
    }

    @Test
    @DisplayName("enviarBienvenidaAlumno despacha HTML corporativo con enlace al portal y sin barras duplicadas")
    void enviarBienvenidaAlumno_debeDespacharHtmlConEnlaceNormalizado() throws Exception
    {
        ReflectionTestUtils.setField(this.emailService, "appBaseUrl", "https://mands.onrender.com///");
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(this.mailSender.createMimeMessage()).thenReturn(mimeMessage);

        assertDoesNotThrow(() ->
            this.emailService.enviarBienvenidaAlumno("antonioabadpro@gmail.com", "Antonio", "Permiso B", 250.0f)
        );

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(this.mailSender).send(captor.capture());

        String html = this.extraerHtml(captor.getValue());
        assertNotNull(html);
        assertEquals("ManDS - ¡Bienvenido/a a la autoescuela!", captor.getValue().getSubject());
        assertTrue(html.contains("https://mands.onrender.com/login"));
        assertFalse(html.contains("com//login"));
        assertTrue(html.contains("enlace-alternativo"));
        assertFalse(html.contains("monospace"));
        assertFalse(html.contains("ERP"));
    }

    @Test
    @DisplayName("enviarCorreoRecuperacion normaliza enlace relativo usando appBaseUrl y despacha HTML vía SMTP")
    void enviarCorreoRecuperacion_debeDespacharViaSmtpYNormalizarEnlace() throws Exception
    {
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(this.mailSender.createMimeMessage()).thenReturn(mimeMessage);

        assertDoesNotThrow(() ->
            this.emailService.enviarCorreoRecuperacion("alumno@autoescuela.es", "Alumno", "/recuperar-password?token=abc123", "15")
        );

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(this.mailSender).send(captor.capture());

        String html = this.extraerHtml(captor.getValue());
        assertNotNull(html);
        assertEquals("ManDS - Recuperación de contraseña", captor.getValue().getSubject());
        assertTrue(html.contains("http://localhost:8080/recuperar-password?token=abc123"));
        assertTrue(html.contains("enlace-alternativo"));
    }

    @Test
    @DisplayName("enviarInvitacionProfesor despacha HTML corporativo con enlace absoluto y duración de vigencia")
    void enviarInvitacionProfesor_debeDespacharViaSmtp() throws Exception
    {
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(this.mailSender.createMimeMessage()).thenReturn(mimeMessage);

        assertDoesNotThrow(() ->
            this.emailService.enviarInvitacionProfesor("profesor@autoescuela.es", "Profesor", "https://mands.onrender.com/activar-cuenta?token=tok1", "60")
        );

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(this.mailSender).send(captor.capture());

        String html = this.extraerHtml(captor.getValue());
        assertNotNull(html);
        assertEquals("ManDS - Bienvenida y activación de cuenta de Profesor", captor.getValue().getSubject());
        assertTrue(html.contains("https://mands.onrender.com/activar-cuenta?token=tok1"));
        assertTrue(html.contains("60 minutos"));
    }

    @Test
    @DisplayName("enviarNotificacionCorreo para Rol.ALUMNO genera HTML formal con botón al panel de alumno, listas y escape XSS")
    void enviarNotificacionCorreo_alumno_debeDespacharHtmlFormateado() throws Exception
    {
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(this.mailSender.createMimeMessage()).thenReturn(mimeMessage);

        String mensaje = "Tu solicitud <script>alert(1)</script> ha sido aceptada.\n\nDatos:\n• Fecha: 15/11/2026\n• Hora: 10:00";
        assertDoesNotThrow(() ->
            this.emailService.enviarNotificacionCorreo("alumno@autoescuela.es", "David", "Citación DGT", mensaje, Rol.ALUMNO)
        );

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(this.mailSender).send(captor.capture());

        String html = this.extraerHtml(captor.getValue());
        assertNotNull(html);
        assertEquals("ManDS - Citación DGT", captor.getValue().getSubject());
        assertTrue(html.contains("http://localhost:8080/alumno/dashboard"));
        assertTrue(html.contains("Acceder a mi Portal de Alumno"));
        assertTrue(html.contains("<ul><li>Fecha: 15/11/2026</li><li>Hora: 10:00</li></ul>"));
        assertTrue(html.contains("&lt;script&gt;alert(1)&lt;/script&gt;"));
        assertFalse(html.contains("<script>"));
    }

    @Test
    @DisplayName("enviarNotificacionCorreo para Rol.PROFESOR, Rol.ADMIN y rol nulo dirige al panel correcto o login")
    void enviarNotificacionCorreo_rolesYGenerico_debenApuntarARutaEsperada() throws Exception
    {
        MimeMessage msgProfesor = new MimeMessage((Session) null);
        MimeMessage msgAdmin = new MimeMessage((Session) null);
        MimeMessage msgGenerico = new MimeMessage((Session) null);
        when(this.mailSender.createMimeMessage()).thenReturn(msgProfesor, msgAdmin, msgGenerico);

        this.emailService.enviarNotificacionCorreo("profesor@autoescuela.es", "Laura", "Aviso Flota", "Vehículo operativo.", Rol.PROFESOR);
        this.emailService.enviarNotificacionCorreo("admin@autoescuela.es", "Admin", "Resumen Diario", "Todo en orden.", Rol.ADMIN);
        this.emailService.enviarNotificacionCorreo("persona@autoescuela.es", null, "Aviso General", "Mensaje informativo.", null);

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(this.mailSender, org.mockito.Mockito.times(3)).send(captor.capture());

        String htmlProfesor = this.extraerHtml(captor.getAllValues().get(0));
        String htmlAdmin = this.extraerHtml(captor.getAllValues().get(1));
        String htmlGenerico = this.extraerHtml(captor.getAllValues().get(2));

        assertTrue(htmlProfesor.contains("http://localhost:8080/profesor/dashboard"));
        assertTrue(htmlProfesor.contains("Acceder a mi Portal de Profesor"));
        assertTrue(htmlAdmin.contains("http://localhost:8080/admin/dashboard"));
        assertTrue(htmlAdmin.contains("Acceder al Panel de Administración"));
        assertTrue(htmlGenerico.contains("http://localhost:8080/login"));
        assertTrue(htmlGenerico.contains("Acceder a ManDS"));
        assertTrue(htmlGenerico.contains("Estimado/a usuario/a:"));
    }

    @Test
    @DisplayName("enviarNotificacionIncidenciaAdmin despacha HTML corporativo sin tipografía monospace ni texto ERP")
    void enviarNotificacionIncidenciaAdmin_debeDespacharViaSmtp() throws Exception
    {
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(this.mailSender.createMimeMessage()).thenReturn(mimeMessage);

        assertDoesNotThrow(() ->
            this.emailService.enviarNotificacionIncidenciaAdmin("admin@autoescuela.es", "Admin", "Profesor", "Seat Ibiza", "1234-BBB", "Rueda pinchada", "http://localhost:8080/admin/incidencias")
        );

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(this.mailSender).send(captor.capture());

        String html = this.extraerHtml(captor.getValue());
        assertNotNull(html);
        assertTrue(html.contains("http://localhost:8080/admin/incidencias"));
        assertTrue(html.contains("1234-BBB"));
        assertFalse(html.contains("monospace"));
        assertFalse(html.contains("ERP"));
    }

    @Test
    @DisplayName("Las plantillas HTML omiten CSS problemático para clientes de correo y fragmentan URLs largas con wbr")
    void plantillasHtml_debenUsarMaquetacionTabularInlineCompatibleSinEstilosPenalizados() throws Exception
    {
        MimeMessage msgBienvenida = new MimeMessage((Session) null);
        MimeMessage msgRecuperacion = new MimeMessage((Session) null);
        MimeMessage msgNotificacion = new MimeMessage((Session) null);
        when(this.mailSender.createMimeMessage()).thenReturn(msgBienvenida, msgRecuperacion, msgNotificacion);

        String tokenLargo = "a".repeat(36);
        this.emailService.enviarBienvenidaAlumno("alumno@autoescuela.es", "Lucía", "Permiso B", 250.0f);
        this.emailService.enviarCorreoRecuperacion("alumno@autoescuela.es", "Lucía", "/recuperar-password?token=" + tokenLargo, "15");
        this.emailService.enviarNotificacionCorreo("alumno@autoescuela.es", "Lucía", "Citación Oficial", "Aviso:\n\n• Punto 1", Rol.ALUMNO);

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(this.mailSender, org.mockito.Mockito.times(3)).send(captor.capture());

        for (MimeMessage mensaje : captor.getAllValues())
        {
            String html = this.extraerHtml(mensaje);
            assertNotNull(html);
            assertTrue(html.contains("role=\"presentation\""));
            assertTrue(html.contains("border-radius:"));
            assertFalse(html.contains("<style"));
            assertFalse(html.contains("!important"));
            assertFalse(html.contains("box-shadow"));
            assertFalse(html.contains("rgba("));
            assertFalse(html.contains("word-break"));
            assertFalse(html.contains("linear-gradient"));
            assertFalse(html.contains("overflow:"));
            assertFalse(html.contains("margin:"));
            assertFalse(html.contains("text-align:"));
            assertFalse(html.contains("display:"));
            assertFalse(html.contains("font-weight:"));
            assertFalse(html.contains("text-transform:"));
        }

        String htmlRecuperacion = this.extraerHtml(captor.getAllValues().get(1));
        assertTrue(htmlRecuperacion.contains("href=\"http://localhost:8080/recuperar-password?token=" + tokenLargo + "\""));
        assertTrue(htmlRecuperacion.contains("<wbr>"));
    }

    @Test
    @DisplayName("Cuando brevoApiKey está configurada y la API HTTP responde 201, despacha por Brevo API y omite SMTP")
    @SuppressWarnings("unchecked")
    void despachar_conBrevoApiKeyExitosa_debeOmitirSmtp() throws IOException, InterruptedException
    {
        ReflectionTestUtils.setField(this.emailAsyncDispatcher, "brevoApiKey", "xkeysib-test-fake-key-12345");

        HttpClient mockHttpClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(201);
        when(mockResponse.body()).thenReturn("{\"messageId\":\"<test-1234@smtp-relay.mailin.fr>\"}");
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);

        ReflectionTestUtils.setField(this.emailAsyncDispatcher, "httpClient", mockHttpClient);

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
        ReflectionTestUtils.setField(this.emailAsyncDispatcher, "brevoApiKey", "xkeysib-test-fake-key-12345");

        HttpClient mockHttpClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(401);
        when(mockResponse.body()).thenReturn("{\"code\":\"unauthorized\"}");
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);

        ReflectionTestUtils.setField(this.emailAsyncDispatcher, "httpClient", mockHttpClient);

        assertDoesNotThrow(() ->
            this.emailService.enviarBienvenidaAlumno("antonio@test.es", "Antonio", "Permiso B", 250.0f)
        );

        verify(mockHttpClient).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
        verify(this.mailSender, never()).send(any(MimeMessage.class));
    }

    private String extraerHtml(Part part) throws Exception
    {
        if (part instanceof MimeMessage mime)
        {
            mime.saveChanges();
        }
        if (part.isMimeType("text/html"))
        {
            return String.valueOf(part.getContent());
        }
        Object contenido = part.getContent();
        if (contenido instanceof Multipart multipart)
        {
            for (int i = 0; i < multipart.getCount(); i++)
            {
                String html = this.extraerHtml(multipart.getBodyPart(i));
                if (html != null)
                {
                    return html;
                }
            }
        }
        return null;
    }
}
