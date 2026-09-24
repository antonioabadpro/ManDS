package com.autoescuela.erp.core.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Objects;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del servicio de envío de correos electrónicos transaccionales.
 * Emplea {@link JavaMailSender} para despachar mensajes en formato HTML con estilos integrados.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService
{
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:no-reply-mands@autoescuela.es}")
    private String correoRemitente;

    /**
     * Envía un correo de recuperación de contraseña.
     *
     * @param correoDestinatario Dirección de correo del destinatario.
     * @param nombreDestinatario Nombre del destinatario.
     * @param enlaceRecuperacion Enlace para recuperar la contraseña.
     * @param duracionTokenMinutos Duración del token en minutos.
     */
    @Override
    public void enviarCorreoRecuperacion(String correoDestinatario, String nombreDestinatario, String enlaceRecuperacion, String duracionTokenMinutos)
    {
        try
        {
            Boolean multipart = true; // Permite adjuntar archivos en el correo

            // Creamos el mensaje MIME con soporte para HTML y codificación UTF-8
            MimeMessage mensaje = this.mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, multipart, "UTF-8");

            helper.setFrom(this.correoRemitente);
            helper.setTo(correoDestinatario);
            helper.setSubject("ManDS - Recuperación de contraseña");

            // Construimos el cuerpo del mensaje en formato HTML
            String cuerpoHtml = construirHtmlRecuperacion(nombreDestinatario, enlaceRecuperacion, duracionTokenMinutos);
            helper.setText(cuerpoHtml, true);

            this.mailSender.send(mensaje);
            log.info("Correo de recuperación enviado con éxito a {}", correoDestinatario);
        }
        catch (MessagingException | MailException ex)
        {
            log.error("Error al enviar el correo de recuperación a {}: {}", correoDestinatario, ex.getMessage());
            // No propagamos la excepción técnica para no romper la experiencia de usuario y preservar la seguridad
        }
    }

    /**
     * Envía un correo de invitación a un nuevo profesor para que active su cuenta.
     *
     * @param correoDestinatario Dirección de correo del profesor.
     * @param nombreDestinatario Nombre del profesor.
     * @param enlaceActivacion Enlace para activar la cuenta del profesor.
     * @param duracionTokenMinutos Duración del token en minutos.
     */
    @Override
    public void enviarInvitacionProfesor(String correoDestinatario, String nombreDestinatario, String enlaceActivacion, String duracionTokenMinutos)
    {
        try
        {
            Boolean multipart = true;

            MimeMessage mensaje = this.mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, multipart, "UTF-8");

            helper.setFrom(this.correoRemitente);
            helper.setTo(correoDestinatario);
            helper.setSubject("ManDS - Bienvenida y activación de cuenta de Profesor");

            // Construimos el cuerpo del mensaje en formato HTML
            String cuerpoHtml = construirHtmlInvitacionProfesor(nombreDestinatario, enlaceActivacion, duracionTokenMinutos);
            helper.setText(cuerpoHtml, true);

            this.mailSender.send(mensaje);
            log.info("Correo de invitación de profesor enviado con éxito a {}", correoDestinatario);
        }
        catch (MessagingException | MailException ex)
        {
            log.error("Error al enviar la invitación de profesor a {}: {}", correoDestinatario, ex.getMessage());
        }
    }

    /**
     * Genera la plantilla HTML responsive con branding corporativo ManDS para el restablecimiento de contraseña.
     * @param nombreDestinatario Nombre del destinatario para personalizar el saludo.
     * @param enlace Enlace de recuperación de contraseña con token.
     * @param duracionTokenMinutos Duración del token en minutos.
     * @return Cadena HTML lista para ser enviada como cuerpo del correo.
     */
    private String construirHtmlRecuperacion(String nombreDestinatario, String enlace, String duracionTokenMinutos)
    {
        String nombreMostrado = Objects.requireNonNullElse(nombreDestinatario, "Usuario");

        String contenidoHTML = """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Recuperación de Contraseña</title>
                <style>
                    body { font-family: 'Helvetica Neue', Arial, sans-serif; background-color: #f8fafc; margin: 0; padding: 20px; color: #1e293b; }
                    .contenedor { max-width: 580px; margin: 0 auto; background-color: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.06); border: 1px solid #e2e8f0; }
                    .cabecera { background: linear-gradient(135deg, #0f172a 0%%, #1e3a8a 100%%); padding: 32px 24px; text-align: center; }
                    .logo-titulo { color: #ffffff; font-size: 26px; font-weight: 800; letter-spacing: -0.5px; margin: 0; }
                    .subtitulo-marca { color: #93c5fd; font-size: 12px; font-weight: 600; text-transform: uppercase; letter-spacing: 1px; margin-top: 4px; }
                    .contenido { padding: 36px 32px; }
                    .saludo { font-size: 20px; font-weight: 700; color: #0f172a; margin-top: 0; margin-bottom: 16px; }
                    .texto { font-size: 15px; line-height: 1.6; color: #475569; margin-bottom: 24px; }
                    .boton-contenedor { text-align: center; margin: 32px 0; }
                    .boton { display: inline-block; background-color: #2563eb; color: #ffffff !important; font-size: 15px; font-weight: 600; text-decoration: none; padding: 14px 32px; border-radius: 12px; box-shadow: 0 4px 14px rgba(37,99,235,0.35); }
                    .alerta-seguridad { background-color: #eff6ff; border-left: 4px solid #3b82f6; padding: 14px 16px; border-radius: 6px; margin: 24px 0; font-size: 13px; color: #1e40af; }
                    .enlace-alternativo { word-break: break-all; font-size: 12px; color: #64748b; background-color: #f1f5f9; padding: 12px; border-radius: 8px; border: 1px dashed #cbd5e1; margin-top: 20px; }
                    .pie { background-color: #f8fafc; padding: 24px; text-align: center; font-size: 12px; color: #94a3b8; border-top: 1px solid #e2e8f0; }
                </style>
            </head>
            <body>
                <div class="contenedor">
                    <div class="cabecera">
                        <h1 class="logo-titulo">ManDS</h1>
                        <div class="subtitulo-marca">Manager Driving School</div>
                    </div>
                    <div class="contenido">
                        <h2 class="saludo">Hola, %1$s:</h2>
                        <p class="texto">
                            Hemos recibido una solicitud para restablecer la contraseña de tu cuenta en la autoescuela.
                            Para crear una nueva clave, pulsa en el botón que verás a continuación:
                        </p>
                        <div class="boton-contenedor">
                            <a href="%2$s" class="boton" target="_blank">Restablecer mi contraseña</a>
                        </div>
                        <div class="alerta-seguridad">
                            <strong>Importante:</strong> Este enlace caducará en <strong>%3$s minutos</strong> y solo se puede utilizar una única vez por motivos de seguridad.
                        </div>
                        <p class="texto" style="font-size: 13px; color: #64748b; margin-bottom: 8px;">
                            Si no has solicitado este cambio, puedes ignorar este correo de forma segura. Tu contraseña actual no se modificará.
                        </p>
                        <div class="enlace-alternativo">
                            Si el botón no funciona, copia y pega este enlace en tu navegador:<br>
                            <a href="%2$s" style="color: #2563eb;">%2$s</a>
                        </div>
                    </div>
                    <div class="pie">
                        © 2026 ManDS Autoescuela ERP. Todos los derechos reservados.
                    </div>
                </div>
            </body>
            </html>
            """;

        return contenidoHTML.formatted(nombreMostrado, enlace, duracionTokenMinutos);
    }

    /**
     * Genera la plantilla HTML responsive para la invitación y activación de cuenta de nuevo profesor.
     * @param nombreDestinatario Nombre del destinatario.
     * @param enlace Enlace de activación.
     * @param duracionTokenMinutos Duración del token en minutos.
     * @return Cadena con el contenido HTML.
     */
    private String construirHtmlInvitacionProfesor(String nombreDestinatario, String enlace, String duracionTokenMinutos)
    {
        String nombreMostrado = Objects.requireNonNullElse(nombreDestinatario, "Profesor");

        String contenidoHTML = """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Activación de Cuenta de Profesor</title>
                <style>
                    body { font-family: 'Helvetica Neue', Arial, sans-serif; background-color: #f8fafc; margin: 0; padding: 20px; color: #1e293b; }
                    .contenedor { max-width: 580px; margin: 0 auto; background-color: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.06); border: 1px solid #e2e8f0; }
                    .cabecera { background: linear-gradient(135deg, #0f172a 0%%, #1e3a8a 100%%); padding: 32px 24px; text-align: center; }
                    .logo-titulo { color: #ffffff; font-size: 26px; font-weight: 800; letter-spacing: -0.5px; margin: 0; }
                    .subtitulo-marca { color: #93c5fd; font-size: 12px; font-weight: 600; text-transform: uppercase; letter-spacing: 1px; margin-top: 4px; }
                    .contenido { padding: 36px 32px; }
                    .saludo { font-size: 20px; font-weight: 700; color: #0f172a; margin-top: 0; margin-bottom: 16px; }
                    .texto { font-size: 15px; line-height: 1.6; color: #475569; margin-bottom: 24px; }
                    .boton-contenedor { text-align: center; margin: 32px 0; }
                    .boton { display: inline-block; background-color: #2563eb; color: #ffffff !important; font-size: 15px; font-weight: 600; text-decoration: none; padding: 14px 32px; border-radius: 12px; box-shadow: 0 4px 14px rgba(37,99,235,0.35); }
                    .alerta-seguridad { background-color: #eff6ff; border-left: 4px solid #3b82f6; padding: 14px 16px; border-radius: 6px; margin: 24px 0; font-size: 13px; color: #1e40af; }
                    .enlace-alternativo { word-break: break-all; font-size: 12px; color: #64748b; background-color: #f1f5f9; padding: 12px; border-radius: 8px; border: 1px dashed #cbd5e1; margin-top: 20px; }
                    .pie { background-color: #f8fafc; padding: 24px; text-align: center; font-size: 12px; color: #94a3b8; border-top: 1px solid #e2e8f0; }
                </style>
            </head>
            <body>
                <div class="contenedor">
                    <div class="cabecera">
                        <h1 class="logo-titulo">ManDS</h1>
                        <div class="subtitulo-marca">Manager Driving School</div>
                    </div>
                    <div class="contenido">
                        <h2 class="saludo">¡Hola, %1$s!</h2>
                        <p class="texto">
                            Te damos la bienvenida a la aplicación de gestión <strong>ManDS</strong>.
                            La administración ha tramitado tu alta en la plataforma. Para poder acceder a tu panel de profesor,
                            gestionar tus clases prácticas y consultar tus alumnos, pulsa en el botón inferior para configurar
                            tu <strong>nombre de usuario</strong> y tu <strong>contraseña</strong> para acceder a la aplicación:
                        </p>
                        <div class="boton-contenedor">
                            <a href="%2$s" class="boton" target="_blank">Activar cuenta y crear credenciales</a>
                        </div>
                        <div class="alerta-seguridad">
                            <strong>Importante:</strong> Este enlace caducará en <strong>%3$s minutos</strong> y solo se puede utilizar una única vez por motivos de seguridad.
                        </div>
                        <p class="texto" style="font-size: 13px; color: #64748b; margin-bottom: 8px;">
                            Si no reconoces este registro o ha sido un error, por favor contacta con el administrador de la autoescuela.
                        </p>
                        <div class="enlace-alternativo">
                            Si tienes problemas con el botón, copia y pega este enlace en tu navegador:<br>
                            <a href="%2$s" style="color: #2563eb;">%2$s</a>
                        </div>
                    </div>
                    <div class="pie">
                        © 2026 ManDS Autoescuela ERP. Todos los derechos reservados.
                    </div>
                </div>
            </body>
            </html>
            """;

        return contenidoHTML.formatted(nombreMostrado, enlace, duracionTokenMinutos);
    }

}
