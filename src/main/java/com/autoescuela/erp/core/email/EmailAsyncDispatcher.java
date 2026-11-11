package com.autoescuela.erp.core.email;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * Despachador asíncrono de correos electrónicos transaccionales.
 * Ejecuta el envío de red fuera del hilo HTTP principal utilizando el pool {@code emailTaskExecutor}:
 * emplea la API REST v3 de Brevo sobre HTTPS (puerto 443) en producción cuando {@code brevo.api.key}
 * está configurada, y recurre a {@link JavaMailSender} (Mailpit SMTP) en desarrollo local.
 */
@Slf4j
@Component
public class EmailAsyncDispatcher
{
    private final JavaMailSender mailSender;
    private HttpClient httpClient;

    @Value("${brevo.api.key:${BREVO_API_KEY:}}")
    private String brevoApiKey;

    @Value("${app.mail.from:${MAIL_FROM:mands.notifications@gmail.com}}")
    private String correoRemitente;

    public EmailAsyncDispatcher(JavaMailSender mailSender)
    {
        this.mailSender = mailSender;
    }

    /**
     * Despacha de forma asíncrona un correo electrónico HTML por el canal activo (Brevo REST API o SMTP local).
     *
     * @param destinatario Dirección de correo del destinatario.
     * @param nombreDestinatario Nombre de pila del destinatario (opcional).
     * @param asunto Asunto completo del correo.
     * @param cuerpoHtml Contenido completo del correo en formato HTML.
     * @param tipoCorreo Descripción semántica del correo para trazabilidad en logs.
     */
    @Async("emailTaskExecutor")
    public void despacharCorreoHtml(String destinatario, String nombreDestinatario, String asunto, String cuerpoHtml, String tipoCorreo)
    {
        if (this.brevoApiKey != null && !this.brevoApiKey.isBlank())
        {
            boolean enviadoPorApi = this.despacharViaBrevoApi(destinatario, nombreDestinatario, asunto, cuerpoHtml, tipoCorreo);
            if (!enviadoPorApi)
            {
                log.error("[EmailService] Falló definitivamente el despacho por Brevo REST API para {}. Se cancela el envío sin recurrir a SMTP en producción.", destinatario);
            }
            return;
        }

        try
        {
            MimeMessage mensaje = this.mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setFrom(this.correoRemitente, "ManDS Autoescuela");
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(cuerpoHtml, true);

            this.despacharConReintentos(mensaje, destinatario, tipoCorreo);
        }
        catch (MessagingException | UnsupportedEncodingException ex)
        {
            log.error("[EmailService] Error al preparar el mensaje MIME ({}) para {}: {}", tipoCorreo, destinatario, ex.getMessage());
        }
    }

    private boolean despacharViaBrevoApi(String destinatario, String nombreDestinatario, String asunto, String cuerpoHtml, String tipoCorreo)
    {
        try
        {
            String nombreDestinatarioFinal = (nombreDestinatario != null && !nombreDestinatario.isBlank()) ? nombreDestinatario : "Usuario";

            String jsonPayload = String.format("""
                {"sender":{"name":"ManDS Autoescuela","email":"%s"},"to":[{"email":"%s","name":"%s"}],"subject":"%s","htmlContent":"%s"}
                """,
                this.escaparJson(this.correoRemitente),
                this.escaparJson(destinatario),
                this.escaparJson(nombreDestinatarioFinal),
                this.escaparJson(asunto),
                this.escaparJson(cuerpoHtml)
            ).trim();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                    .header("api-key", this.brevoApiKey.trim())
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                    .build();

            HttpClient clienteHttp = (this.httpClient != null)
                    ? this.httpClient
                    : HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

            HttpResponse<String> response = clienteHttp.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() == 200 || response.statusCode() == 201)
            {
                log.info("[EmailService-BrevoAPI] {} enviado con éxito a {} vía HTTPS (HTTP {} - {})",
                        tipoCorreo, destinatario, response.statusCode(), response.body());
                return true;
            }

            log.error("[EmailService-BrevoAPI] Error al despachar {} a {} vía API (HTTP {}): {}",
                    tipoCorreo, destinatario, response.statusCode(), response.body());
            return false;
        }
        catch (Exception ex)
        {
            log.error("[EmailService-BrevoAPI] Excepción al conectar con la API de Brevo para {}: {}", destinatario, ex.getMessage());
            return false;
        }
    }

    private void despacharConReintentos(MimeMessage mensaje, String destinatario, String tipoCorreo)
    {
        final int maxIntentos = 3;
        final long retardoBaseMs = 1500L;

        for (int intento = 1; intento <= maxIntentos; intento++)
        {
            try
            {
                this.mailSender.send(mensaje);
                log.info("[EmailService] {} enviado con éxito a {} (intento {}/{})", tipoCorreo, destinatario, intento, maxIntentos);
                return;
            }
            catch (MailException ex)
            {
                if (intento < maxIntentos)
                {
                    long espera = retardoBaseMs * intento;
                    log.warn("[EmailService] Error transitorio al enviar {} a {} (intento {}/{}): {}. Reintentando en {}ms...",
                            tipoCorreo, destinatario, intento, maxIntentos, ex.getMessage(), espera);
                    try
                    {
                        Thread.sleep(espera);
                    }
                    catch (InterruptedException ie)
                    {
                        Thread.currentThread().interrupt();
                        log.error("[EmailService] Reintento de envío interrumpido para {}: {}", destinatario, ie.getMessage());
                        return;
                    }
                }
                else
                {
                    log.error("[EmailService] Error persistente tras {} intentos al enviar {} a {}: {}",
                            maxIntentos, tipoCorreo, destinatario, ex.getMessage());
                }
            }
        }
    }

    private String escaparJson(String texto)
    {
        if (texto == null)
        {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (char c : texto.toCharArray())
        {
            switch (c)
            {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < ' ')
                    {
                        sb.append(String.format("\\u%04x", (int) c));
                    }
                    else
                    {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }
}
