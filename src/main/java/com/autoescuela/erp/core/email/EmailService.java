package com.autoescuela.erp.core.email;

/**
 * Servicio transversal para el envío de correos electrónicos transaccionales.
 * Gestiona notificaciones automáticas
 */
public interface EmailService
{

    /**
     * Envía un correo electrónico con el enlace de recuperación de contraseña.
     *
     * @param correoDestinatario Dirección de correo del destinatario.
     * @param nombreDestinatario Nombre del usuario para personalizar el saludo.
     * @param enlaceRecuperacion URL completa con el token para restablecer la contraseña.
     */
    void enviarCorreoRecuperacion(String correoDestinatario, String nombreDestinatario, String enlaceRecuperacion);
}
