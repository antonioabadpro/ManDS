package com.autoescuela.erp.core.email;

import com.autoescuela.erp.core.enums.Rol;

/**
 * Servicio transversal para el envío de correos electrónicos transaccionales.
 * Gestiona notificaciones automáticas con formato HTML corporativo unificado.
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
     * Envía un correo electrónico de notificación formal en HTML corporativo a cualquier usuario del sistema
     * (Alumno, Profesor o Administrador), dirigiendo el acceso hacia su panel correspondiente según su rol.
     *
     * @param correoDestinatario Dirección de correo del destinatario.
     * @param nombreDestinatario Nombre de pila del destinatario para personalizar el saludo (opcional).
     * @param asunto Asunto del correo electrónico.
     * @param mensaje Cuerpo formal del aviso (soporta saltos de línea para párrafos o listas).
     * @param rolDestinatario Rol del destinatario para dirigir el enlace a su panel, o {@code null} para acceso general.
     */
    void enviarNotificacionCorreo(String correoDestinatario, String nombreDestinatario, String asunto, String mensaje, Rol rolDestinatario);

    /**
     * Envía un correo electrónico de bienvenida y confirmación de matrícula a un nuevo alumno tras el pago.
     *
     * @param correoDestinatario Dirección de correo del alumno.
     * @param nombreDestinatario Nombre de pila del alumno.
     * @param descripcionCarnet Descripción legible del carnet matriculado.
     * @param importeAbonado Importe abonado de la matrícula en euros.
     */
    void enviarBienvenidaAlumno(String correoDestinatario, String nombreDestinatario, String descripcionCarnet, float importeAbonado);

    /**
     * Envía un correo electrónico de notificación al Administrador cuando un profesor reporta una incidencia o avería.
     *
     * @param correoDestinatario Dirección de correo electrónico del Administrador.
     * @param nombreAdmin Nombre de pila del Administrador.
     * @param nombreProfesor Nombre completo del profesor emisor.
     * @param vehiculoInfo Marca y modelo del vehículo afectado.
     * @param matricula Matrícula del vehículo afectado.
     * @param descripcionIncidencia Descripción o anomalía mecánica detallada por el profesor.
     * @param urlIncidencias URL directa hacia el Panel de Incidencias de Flota.
     */
    void enviarNotificacionIncidenciaAdmin(String correoDestinatario, String nombreAdmin, String nombreProfesor, String vehiculoInfo, String matricula, String descripcionIncidencia, String urlIncidencias);
}
