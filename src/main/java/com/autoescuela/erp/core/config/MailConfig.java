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
        props.put("mail.smtp.starttls.enable", String.valueOf(this.starttls));

        return mailSender;
    }
}

