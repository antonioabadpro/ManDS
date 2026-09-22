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

    /**
     * Envía un correo electrónico de invitación formal a un nuevo profesor.
     * Incluye un enlace seguro y temporal para que establezca su nombre de usuario y contraseña iniciales.
     *
     * @param correoDestinatario Dirección de correo del profesor.
     * @param nombreDestinatario Nombre de pila del profesor.
     * @param enlaceActivacion URL completa con el token criptográfico para activar su cuenta.
     */
    void enviarInvitacionProfesor(String correoDestinatario, String nombreDestinatario, String enlaceActivacion);
}
