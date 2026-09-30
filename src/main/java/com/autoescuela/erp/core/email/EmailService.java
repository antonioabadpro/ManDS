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
     * @param duracionTokenMinutos Duración del token en minutos.
     */
    void enviarCorreoRecuperacion(String correoDestinatario, String nombreDestinatario, String enlaceRecuperacion, String duracionTokenMinutos);

    /**
     * Envía un correo electrónico de invitación formal a un nuevo profesor.
     * Incluye un enlace seguro y temporal para que establezca su nombre de usuario y contraseña iniciales.
     *
     * @param correoDestinatario Dirección de correo del profesor.
     * @param nombreDestinatario Nombre de pila del profesor.
     * @param enlaceActivacion URL completa con el token criptográfico para activar su cuenta.
     * @param duracionTokenMinutos Duración del token en minutos.
     */
    void enviarInvitacionProfesor(String correoDestinatario, String nombreDestinatario, String enlaceActivacion, String duracionTokenMinutos);

    /**
     * Envía un correo electrónico de notificación formal o pedagógica a un alumno tutelado.
     *
     * @param correoDestinatario Dirección de correo del alumno.
     * @param asunto Asunto del correo electrónico.
     * @param mensaje Cuerpo del mensaje en texto plano o contenido transaccional.
     */
    void enviarNotificacionAlumno(String correoDestinatario, String asunto, String mensaje);

    /**
     * Envía un correo electrónico de bienvenida y confirmación de matrícula a un nuevo alumno tras el pago.
     *
     * @param correoDestinatario Dirección de correo del alumno.
     * @param nombreDestinatario Nombre de pila del alumno.
     * @param descripcionCarnet Descripción legible del carnet matriculado.
     * @param importeAbonado Importe abonado de la matrícula en euros.
     */
    void enviarBienvenidaAlumno(String correoDestinatario, String nombreDestinatario, String descripcionCarnet, float importeAbonado);
}
