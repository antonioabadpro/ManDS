package com.autoescuela.erp.core.config;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * Configuración explícita del cliente de correo electrónico transaccional {@link JavaMailSender}.
 * Vincula las propiedades de conexión para servidores SMTP locales (Mailpit en puerto 1025)
 * o servidores SMTP en producción.
 */
@Configuration
public class MailConfig
{
    @Value("${spring.mail.host:localhost}")
    private String host;

    @Value("${spring.mail.port:1025}")
    private int port;

    @Value("${spring.mail.username:}")
    private String username;

    @Value("${spring.mail.password:}")
    private String password;

    /**
     * Indica si se requiere autenticación para el envío de correos electrónicos.
     */
    @Value("${spring.mail.properties.mail.smtp.auth:false}")
    private boolean esAutenticado;

    /**
     * Habilita STARTTLS para conexiones seguras con servidores SMTP que lo requieran
     */
    @Value("${spring.mail.properties.mail.smtp.starttls.enable:false}")
    private boolean starttls;

    @Value("${app.mail.from:${MAIL_FROM:mands.notifications@gmail.com}}")
    private String correoRemitente;

    @Value("${spring.mail.properties.mail.smtp.connectiontimeout:10000}")
    private int connectionTimeout;

    @Value("${spring.mail.properties.mail.smtp.timeout:10000}")
    private int timeout;

    @Value("${spring.mail.properties.mail.smtp.writetimeout:10000}")
    private int writeTimeout;

    /**
     * Crea y configura un bean de {@link JavaMailSender} para el envío de correos electrónicos.
     * @return Instancia de JavaMailSender configurada con las propiedades de conexión SMTP.
     */
    @Bean
    public JavaMailSender javaMailSender()
    {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(this.host);
        mailSender.setPort(this.port);

        if (this.username != null && !this.username.isBlank())
        {
            mailSender.setUsername(this.username);
        }
        if (this.password != null && !this.password.isBlank())
        {
            mailSender.setPassword(this.password);
        }

        mailSender.setDefaultEncoding("UTF-8");

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", String.valueOf(this.esAutenticado));
        if (this.correoRemitente != null && !this.correoRemitente.isBlank())
        {
            props.put("mail.smtp.from", this.correoRemitente);
        }
        props.put("mail.smtp.starttls.enable", String.valueOf(this.starttls));
        props.put("mail.smtp.connectiontimeout", String.valueOf(this.connectionTimeout));
        props.put("mail.smtp.timeout", String.valueOf(this.timeout));
        props.put("mail.smtp.writetimeout", String.valueOf(this.writeTimeout));

        if (this.starttls)
        {
            props.put("mail.smtp.starttls.required", "true");
            props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
            props.put("mail.smtp.ssl.trust", "*");
        }

        return mailSender;
    }
}

