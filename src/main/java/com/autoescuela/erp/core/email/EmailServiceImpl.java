package com.autoescuela.erp.core.email;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import com.autoescuela.erp.core.enums.Rol;
import com.autoescuela.erp.core.util.UrlAplicacionUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del servicio de envío de correos electrónicos transaccionales.
 * Construye síncronamente las plantillas HTML corporativas unificadas y las URLs absolutas
 * efectivas antes de delegar el envío asíncrono de red en {@link EmailAsyncDispatcher}.
 */
@Slf4j
@Service
public class EmailServiceImpl implements EmailService
{
    private static final String FUENTE_CORPORATIVA = "'Helvetica Neue', Arial, sans-serif";
    private static final String ESTILO_SALUDO = "padding: 0 0 14px 0; font-family: " + FUENTE_CORPORATIVA + "; font-size: 20px; line-height: 1.3; color: #0f172a;";
    private static final String ESTILO_FILA_TEXTO = "padding: 0 0 16px 0; font-family: " + FUENTE_CORPORATIVA + "; font-size: 15px; line-height: 1.6; color: #475569;";
    private static final String ESTILO_FILA_SECUNDARIA = "padding: 0 0 16px 0; font-family: " + FUENTE_CORPORATIVA + "; font-size: 13px; line-height: 1.5; color: #64748b;";
    private static final String ESTILO_TARJETA_RESUMEN = "background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 10px;";
    private static final String ESTILO_CELDA_ETIQUETA = "padding: 7px 8px 7px 0; font-family: " + FUENTE_CORPORATIVA + "; font-size: 14px; line-height: 1.4; color: #64748b;";
    private static final String ESTILO_CELDA_VALOR = "padding: 7px 0 7px 8px; font-family: " + FUENTE_CORPORATIVA + "; font-size: 14px; line-height: 1.4; color: #1e293b;";
    private static final String ESTILO_CELDA_TOTAL_ETIQUETA = "padding: 12px 8px 2px 0; border-top: 1px dashed #cbd5e1; font-family: " + FUENTE_CORPORATIVA + "; font-size: 14px; line-height: 1.4; color: #0f172a;";
    private static final String ESTILO_CELDA_TOTAL_VALOR = "padding: 12px 0 2px 8px; border-top: 1px dashed #cbd5e1; font-family: " + FUENTE_CORPORATIVA + "; font-size: 15px; line-height: 1.4; color: #0f172a;";

    private final EmailAsyncDispatcher emailAsyncDispatcher;

    @Value("${app_base_url:http://localhost:8080}")
    private String appBaseUrl;

    public EmailServiceImpl(EmailAsyncDispatcher emailAsyncDispatcher)
    {
        this.emailAsyncDispatcher = emailAsyncDispatcher;
    }

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
        if (this.esCorreoInvalido(correoDestinatario))
        {
            return;
        }

        String enlaceSeguro = this.normalizarEnlaceDestino(enlaceRecuperacion, "/recuperar-password");
        String cuerpoHtml = this.construirHtmlRecuperacion(nombreDestinatario, enlaceSeguro, duracionTokenMinutos);

        this.emailAsyncDispatcher.despacharCorreoHtml(
                correoDestinatario,
                nombreDestinatario,
                "ManDS - Recuperación de contraseña",
                cuerpoHtml,
                "Correo de recuperación de contraseña"
        );
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
        if (this.esCorreoInvalido(correoDestinatario))
        {
            return;
        }

        String enlaceSeguro = this.normalizarEnlaceDestino(enlaceActivacion, "/activar-cuenta");
        String cuerpoHtml = this.construirHtmlInvitacionProfesor(nombreDestinatario, enlaceSeguro, duracionTokenMinutos);

        this.emailAsyncDispatcher.despacharCorreoHtml(
                correoDestinatario,
                nombreDestinatario,
                "ManDS - Bienvenida y activación de cuenta de Profesor",
                cuerpoHtml,
                "Invitación y activación de profesor"
        );
    }

    /**
     * Envía un correo electrónico de notificación formal unificada en HTML corporativo
     * para cualquier usuario (Alumno, Profesor o Administrador).
     */
    @Override
    public void enviarNotificacionCorreo(String correoDestinatario, String nombreDestinatario, String asunto, String mensaje, Rol rolDestinatario)
    {
        if (this.esCorreoInvalido(correoDestinatario))
        {
            return;
        }

        String asuntoLimpio = (asunto != null && !asunto.isBlank()) ? asunto.trim() : "Notificación oficial";
        String asuntoFinal = asuntoLimpio.startsWith("ManDS") ? asuntoLimpio : "ManDS - " + asuntoLimpio;
        String baseUrlEfectiva = UrlAplicacionUtil.resolverBaseUrlEfectiva(null, this.appBaseUrl);

        String rutaDestino = this.obtenerRutaPanelPorRol(rolDestinatario);
        String textoBoton = this.obtenerTextoBotonPorRol(rolDestinatario);
        String enlaceDestino = UrlAplicacionUtil.construirUrl(baseUrlEfectiva, rutaDestino);

        String cuerpoHtml = this.construirHtmlNotificacion(nombreDestinatario, asuntoLimpio, mensaje, textoBoton, enlaceDestino);
        String etiquetaLog = this.obtenerEtiquetaLogPorRol(rolDestinatario, asuntoLimpio);

        this.emailAsyncDispatcher.despacharCorreoHtml(
                correoDestinatario,
                nombreDestinatario,
                asuntoFinal,
                cuerpoHtml,
                etiquetaLog
        );
    }

    /**
     * Envía un correo electrónico de bienvenida y confirmación de matrícula tras un pago exitoso en Stripe.
     *
     * @param correoDestinatario Dirección de correo del alumno.
     * @param nombreDestinatario Nombre de pila del alumno.
     * @param descripcionCarnet Descripción legible del carnet matriculado.
     * @param importeAbonado Importe abonado de la matrícula en euros.
     */
    @Override
    public void enviarBienvenidaAlumno(String correoDestinatario, String nombreDestinatario, String descripcionCarnet, float importeAbonado)
    {
        if (this.esCorreoInvalido(correoDestinatario))
        {
            return;
        }

        String baseUrlEfectiva = UrlAplicacionUtil.resolverBaseUrlEfectiva(null, this.appBaseUrl);
        String enlaceLogin = UrlAplicacionUtil.construirUrl(baseUrlEfectiva, "/login");
        String cuerpoHtml = this.construirHtmlBienvenidaAlumno(nombreDestinatario, descripcionCarnet, importeAbonado, enlaceLogin);

        this.emailAsyncDispatcher.despacharCorreoHtml(
                correoDestinatario,
                nombreDestinatario,
                "ManDS - ¡Bienvenido/a a la autoescuela!",
                cuerpoHtml,
                "Bienvenida y confirmación de matrícula"
        );
    }

    /**
     * Envía un correo electrónico de notificación al Administrador cuando un profesor reporta una incidencia o avería mecánica.
     */
    @Override
    public void enviarNotificacionIncidenciaAdmin(String correoDestinatario, String nombreAdmin, String nombreProfesor, String vehiculoInfo, String matricula, String descripcionIncidencia, String urlIncidencias)
    {
        if (this.esCorreoInvalido(correoDestinatario))
        {
            return;
        }

        String enlaceDefinitivo = this.normalizarEnlaceDestino(urlIncidencias, "/admin/incidencias");
        String cuerpoHtml = this.construirHtmlIncidenciaAdmin(nombreAdmin, nombreProfesor, vehiculoInfo, matricula, descripcionIncidencia, enlaceDefinitivo);

        this.emailAsyncDispatcher.despacharCorreoHtml(
                correoDestinatario,
                nombreAdmin,
                "ManDS - Nueva Incidencia Reportada en Vehículo " + (matricula != null ? matricula : ""),
                cuerpoHtml,
                "Notificación de incidencia mecánica a administración"
        );
    }

    private boolean esCorreoInvalido(String correoDestinatario)
    {
        if (correoDestinatario == null || correoDestinatario.isBlank())
        {
            log.warn("[EmailService] Se omitió el envío de correo porque la dirección de destino es nula o vacía.");
            return true;
        }
        return false;
    }

    private String normalizarEnlaceDestino(String enlaceExplicito, String rutaRespaldo)
    {
        String baseUrlEfectiva = UrlAplicacionUtil.resolverBaseUrlEfectiva(null, this.appBaseUrl);
        if (enlaceExplicito == null || enlaceExplicito.isBlank())
        {
            return UrlAplicacionUtil.construirUrl(baseUrlEfectiva, rutaRespaldo);
        }

        String limpio = enlaceExplicito.trim();
        if (limpio.startsWith("/"))
        {
            return UrlAplicacionUtil.construirUrl(baseUrlEfectiva, limpio);
        }

        return limpio;
    }

    private String obtenerRutaPanelPorRol(Rol rol)
    {
        if (rol == null)
        {
            return "/login";
        }

        return switch (rol)
        {
            case ADMIN -> "/admin/dashboard";
            case PROFESOR -> "/profesor/dashboard";
            case ALUMNO -> "/alumno/dashboard";
        };
    }

    private String obtenerTextoBotonPorRol(Rol rol)
    {
        if (rol == null)
        {
            return "Acceder a ManDS";
        }

        return switch (rol)
        {
            case ADMIN -> "Acceder al Panel de Administración";
            case PROFESOR -> "Acceder a mi Portal de Profesor";
            case ALUMNO -> "Acceder a mi Portal de Alumno";
        };
    }

    private String obtenerEtiquetaLogPorRol(Rol rol, String asunto)
    {
        if (rol == null)
        {
            return "Notificación general (" + asunto + ")";
        }

        return switch (rol)
        {
            case ADMIN -> "Notificación a administrador (" + asunto + ")";
            case PROFESOR -> "Notificación a profesor (" + asunto + ")";
            case ALUMNO -> "Notificación a alumno (" + asunto + ")";
        };
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
        String nombreEscapado = this.escaparTexto(nombreDestinatario, "Usuario");
        String duracionEscapada = this.escaparTexto(duracionTokenMinutos, "15");

        String contenido = this.construirFilaSaludo("Hola, " + nombreEscapado + ":")
                + this.construirFilaTexto(
                "Hemos recibido una solicitud para restablecer la contraseña de tu cuenta en la autoescuela. "
                        + "Para crear una nueva clave, pulsa en el botón que verás a continuación:"
        );

        String alerta = "<strong>Importante:</strong> Este enlace caducará en <strong>" + duracionEscapada
                + " minutos</strong> y solo se puede utilizar una única vez por motivos de seguridad.";

        String notaInferior = this.construirFilaSecundaria(
                "Si no has solicitado este cambio, puedes ignorar este correo de forma segura. Tu contraseña actual no se modificará."
        );

        return this.envolverEnPlantillaCorporativa(
                "Recuperación de Contraseña",
                contenido,
                "Restablecer mi contraseña",
                enlace,
                alerta,
                notaInferior,
                "Si el botón no funciona, copia y pega este enlace en tu navegador:"
        );
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
        String nombreEscapado = this.escaparTexto(nombreDestinatario, "Profesor");
        String duracionEscapada = this.escaparTexto(duracionTokenMinutos, "60");

        String contenido = this.construirFilaSaludo("¡Hola, " + nombreEscapado + "!")
                + this.construirFilaTexto(
                "Te damos la bienvenida a la plataforma de gestión <strong>ManDS Autoescuela</strong>. "
                        + "La administración ha tramitado tu alta en la plataforma. Para poder acceder a tu panel de profesor, "
                        + "gestionar tus clases prácticas y consultar tus alumnos, pulsa en el botón inferior para configurar "
                        + "tu <strong>nombre de usuario</strong> y tu <strong>contraseña</strong> de acceso:"
        );

        String alerta = "<strong>Importante:</strong> Este enlace caducará en <strong>" + duracionEscapada
                + " minutos</strong> y solo se puede utilizar una única vez por motivos de seguridad.";

        String notaInferior = this.construirFilaSecundaria(
                "Si no reconoces este registro o ha sido un error, por favor contacta con el administrador de la autoescuela."
        );

        return this.envolverEnPlantillaCorporativa(
                "Activación de Cuenta de Profesor",
                contenido,
                "Activar cuenta y crear credenciales",
                enlace,
                alerta,
                notaInferior,
                "Si tienes problemas con el botón, copia y pega este enlace en tu navegador:"
        );
    }

    /**
     * Genera la plantilla HTML responsive para la bienvenida y confirmación de matrícula de un nuevo alumno.
     * @param nombreDestinatario Nombre del alumno.
     * @param descripcionCarnet Descripción del carnet.
     * @param importeAbonado Importe del pago en euros.
     * @param enlaceLogin Enlace directo a la pantalla de login.
     * @return Cadena con el contenido HTML formateado.
     */
    private String construirHtmlBienvenidaAlumno(String nombreDestinatario, String descripcionCarnet, float importeAbonado, String enlaceLogin)
    {
        String nombreEscapado = this.escaparTexto(nombreDestinatario, "Alumno/a");
        String carnetEscapado = this.escaparTexto(descripcionCarnet, "Permiso de Conducir");
        String importeFormateado = String.format(Locale.GERMAN, "%.2f", importeAbonado);

        String badgeEstado = this.construirBadgeAlineadoDerecha(
                "ACTIVA",
                "#dcfce7",
                "#15803d",
                "border-radius: 6px; padding: 3px 9px; font-size: 12px;"
        );

        String filasResumen = this.construirFilaResumenClaveValor("Permiso Matriculado:", "<strong>" + carnetEscapado + "</strong>", false)
                + this.construirFilaResumenClaveValor("Estado de la Cuenta:", badgeEstado, false)
                + this.construirFilaResumenClaveValor("Convocatorias examen:", "<strong>2 oficiales</strong>", false)
                + this.construirFilaResumenClaveValor("<strong>Total abonado:</strong>", "<strong>" + importeFormateado + " €</strong>", true);

        String contenido = this.construirFilaSaludo("¡Hola, " + nombreEscapado + "!")
                + this.construirFilaTexto(
                "¡Te damos la bienvenida a <strong>ManDS Autoescuela</strong>! Tu pago se ha procesado correctamente "
                        + "y tu matrícula oficial ha sido formalizada. Tu cuenta de alumno ya está 100% activa."
        )
                + this.construirFilaTarjetaResumen(filasResumen);

        String alerta = "<strong>¿Cómo acceder?</strong> Puedes iniciar sesión con tu <strong>nombre de usuario</strong> o <strong>correo electrónico</strong> junto con la contraseña que configuraste durante el registro.";

        return this.envolverEnPlantillaCorporativa(
                "¡Bienvenido/a a ManDS Autoescuela!",
                contenido,
                "Acceder a mi Portal de Alumno",
                enlaceLogin,
                alerta,
                "",
                "Si el botón no funciona, copia y pega este enlace en tu navegador:"
        );
    }

    private String construirHtmlIncidenciaAdmin(String nombreAdmin, String nombreProfesor, String vehiculoInfo, String matricula, String descripcion, String enlaceIncidencias)
    {
        String adminEscapado = this.escaparTexto(nombreAdmin, "Administrador");
        String profesorEscapado = this.escaparTexto(nombreProfesor, "Profesor");
        String vehiculoEscapado = this.escaparTexto(vehiculoInfo, "Vehículo de la flota");
        String matriculaEscapada = this.escaparTexto(matricula, "-");
        String descripcionEscapada = this.escaparTexto(descripcion, "Sin descripción facilitada.");

        String badgeMatricula = this.construirBadgeAlineadoDerecha(
                matriculaEscapada,
                "#e2e8f0",
                "#0f172a",
                "border-radius: 4px; padding: 2px 8px; font-size: 13px; letter-spacing: 0.5px;"
        );

        String bloqueDescripcion = """
            <tr>
                <td colspan="2" style="padding: 12px 0 0 0; border-top: 1px dashed #cbd5e1;">
                    <table role="presentation" width="100%%" border="0" cellpadding="0" cellspacing="0">
                        <tr>
                            <td align="left" style="padding: 0 0 8px 0; font-family: %1$s; font-size: 13px; line-height: 1.4; color: #64748b;">
                                Descripción de la avería:
                            </td>
                        </tr>
                        <tr>
                            <td align="left" bgcolor="#ffffff" style="background-color: #ffffff; border: 1px solid #cbd5e1; border-radius: 8px; padding: 12px 14px; font-family: %1$s; font-size: 14px; line-height: 1.5; color: #334155; font-style: italic;">
                                "%2$s"
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
            """.formatted(FUENTE_CORPORATIVA, descripcionEscapada);

        String filasResumen = this.construirFilaResumenClaveValor("Profesor emisor:", "<strong>" + profesorEscapado + "</strong>", false)
                + this.construirFilaResumenClaveValor("Vehículo afectado:", "<strong>" + vehiculoEscapado + "</strong>", false)
                + this.construirFilaResumenClaveValor("Matrícula:", badgeMatricula, false)
                + bloqueDescripcion;

        String contenido = this.construirFilaSaludo("Hola, " + adminEscapado + ":")
                + this.construirFilaTexto(
                "Te informamos de que el profesor <strong>" + profesorEscapado + "</strong> en el vehículo <strong>"
                        + vehiculoEscapado + "</strong> (matrícula <strong>" + matriculaEscapada + "</strong>) ha reportado una incidencia técnica."
        )
                + this.construirFilaTarjetaResumen(filasResumen);

        String alerta = "Consúltala en el Panel de Incidencias de Flota iniciando sesión en ManDS. Al pulsar en el enlace superior accederás directamente si ya tienes sesión activa.";

        return this.envolverEnPlantillaCorporativa(
                "Nueva Incidencia Mecánica Reportada",
                contenido,
                "Consultar en Panel de Incidencias",
                enlaceIncidencias,
                alerta,
                "",
                "Enlace directo al panel de incidencias:"
        );
    }

    private String construirHtmlNotificacion(String nombreDestinatario, String asunto, String mensaje, String textoBoton, String enlaceDestino)
    {
        String saludo = (nombreDestinatario != null && !nombreDestinatario.isBlank())
                ? "Hola, " + this.escaparTexto(nombreDestinatario, "Usuario") + ":"
                : "Estimado/a usuario/a:";

        String asuntoBase = (asunto != null && !asunto.isBlank()) ? asunto.trim() : "Notificación oficial";
        String etiquetaAvisoEscapada = this.escaparHtmlUtf8(asuntoBase.toUpperCase(Locale.forLanguageTag("es-ES")));
        String cuerpoFormateado = this.formatearMensajeHtml(mensaje);

        String filaPillAviso = """
            <tr>
                <td align="left" style="padding: 0 0 14px 0;">
                    <table role="presentation" border="0" cellpadding="0" cellspacing="0">
                        <tr>
                            <td align="left" bgcolor="#eff6ff" style="background-color: #eff6ff; border: 1px solid #bfdbfe; border-radius: 12px; padding: 4px 12px; font-family: %1$s; font-size: 11px; line-height: 1.3; color: #1d4ed8; letter-spacing: 0.6px;">
                                <strong>%2$s</strong>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
            """.formatted(FUENTE_CORPORATIVA, etiquetaAvisoEscapada);

        String contenido = filaPillAviso
                + this.construirFilaSaludo(saludo)
                + cuerpoFormateado;

        String alerta = "Puedes consultar el estado actualizado de tu expediente, horarios y avisos desde tu portal en ManDS Autoescuela.";

        return this.envolverEnPlantillaCorporativa(
                asuntoBase,
                contenido,
                textoBoton,
                enlaceDestino,
                alerta,
                "",
                "Si el botón no funciona, copia y pega este enlace en tu navegador:"
        );
    }

    /**
     * Envuelve cualquier bloque de contenido transaccional en la plantilla HTML corporativa unificada de ManDS.
     * Emplea maquetación híbrida por tablas con CSS inline para maximizar la compatibilidad entre clientes de correo y navegadores actuales.
     */
    private String envolverEnPlantillaCorporativa(
            String tituloHtml,
            String contenidoPrincipalHtml,
            String textoBoton,
            String enlaceBoton,
            String alertaInfoHtml,
            String notaInferiorHtml,
            String etiquetaEnlaceAlternativo)
    {
        String tituloEscapado = this.escaparTexto(tituloHtml, "ManDS Autoescuela");
        String enlaceHref = this.sanitizarHref(enlaceBoton);
        String enlaceVisible = this.formatearEnlaceVisibleConWbr(enlaceHref);
        String textoBotonEscapado = this.escaparTexto(textoBoton, "Acceder a ManDS");
        String etiquetaAltEscapada = this.escaparTexto(etiquetaEnlaceAlternativo, "Si el botón no funciona, copia y pega este enlace en tu navegador:");

        String bloqueAlerta = (alertaInfoHtml != null && !alertaInfoHtml.isBlank())
                ? """
                  <tr>
                      <td style="padding: 4px 0 18px 0;">
                          <table role="presentation" width="100%%" border="0" cellpadding="0" cellspacing="0" bgcolor="#eff6ff" style="background-color: #eff6ff; border-left: 4px solid #3b82f6; border-radius: 6px;">
                              <tr>
                                  <td align="left" style="padding: 14px 16px; font-family: %1$s; font-size: 13px; line-height: 1.5; color: #1e40af;">
                                      %2$s
                                  </td>
                              </tr>
                          </table>
                      </td>
                  </tr>
                  """.formatted(FUENTE_CORPORATIVA, alertaInfoHtml)
                : "";

        String bloqueNotaInferior = (notaInferiorHtml != null && !notaInferiorHtml.isBlank())
                ? notaInferiorHtml
                : "";

        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%1$s</title>
            </head>
            <body bgcolor="#f1f5f9">
                <table role="presentation" width="100%%" border="0" cellpadding="0" cellspacing="0" bgcolor="#f1f5f9" style="background-color: #f1f5f9; font-family: %2$s; color: #1e293b;">
                    <tr>
                        <td align="center" style="padding: 24px 12px;">
                            <!--[if mso]>
                            <table role="presentation" align="center" border="0" cellpadding="0" cellspacing="0" width="580">
                            <tr>
                            <td>
                            <![endif]-->
                            <table role="presentation" align="center" width="100%%" border="0" cellpadding="0" cellspacing="0" bgcolor="#ffffff" style="width: 100%%; max-width: 580px; background-color: #ffffff; border: 1px solid #e2e8f0; border-bottom: 2px solid #cbd5e1; border-radius: 14px;">
                                <tr>
                                    <td align="center" bgcolor="#0f172a" style="background-color: #0f172a; border-bottom: 4px solid #2563eb; border-radius: 13px 13px 0 0; padding: 28px 24px;">
                                        <span style="font-family: %2$s; font-size: 28px; line-height: 1.2; color: #ffffff; letter-spacing: -0.5px;"><strong>ManDS</strong></span>
                                        <br>
                                        <span style="font-family: %2$s; font-size: 11px; line-height: 1.8; color: #93c5fd; letter-spacing: 1.4px;"><strong>MANAGER DRIVING SCHOOL</strong></span>
                                    </td>
                                </tr>
                                <tr>
                                    <td style="padding: 32px 28px 28px 28px;">
                                        <table role="presentation" width="100%%" border="0" cellpadding="0" cellspacing="0">
                                            %3$s
                                            <tr>
                                                <td align="center" style="padding: 10px 0 22px 0;">
                                                    <table role="presentation" align="center" border="0" cellpadding="0" cellspacing="0">
                                                        <tr>
                                                            <td align="center" bgcolor="#2563eb" style="background-color: #2563eb; border-bottom: 3px solid #1d4ed8; border-radius: 10px; padding: 14px 0;">
                                                                <a href="%4$s" target="_blank" rel="noopener noreferrer" style="font-family: %2$s; font-size: 15px; line-height: 1.2; color: #ffffff; text-decoration: none; padding: 14px 30px;"><span style="color: #ffffff;"><strong>%5$s</strong></span></a>
                                                            </td>
                                                        </tr>
                                                    </table>
                                                </td>
                                            </tr>
                                            %6$s
                                            %7$s
                                            <tr>
                                                <td class="enlace-alternativo" align="left" bgcolor="#f1f5f9" style="background-color: #f1f5f9; border: 1px dashed #cbd5e1; border-radius: 8px; padding: 12px 14px; font-family: %2$s; font-size: 12px; line-height: 1.6; color: #64748b;">
                                                    %8$s<br>
                                                    <a href="%4$s" target="_blank" rel="noopener noreferrer" style="color: #2563eb;">%9$s</a>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                                <tr>
                                    <td align="center" bgcolor="#f8fafc" style="background-color: #f8fafc; border-top: 1px solid #e2e8f0; border-radius: 0 0 13px 13px; padding: 20px 24px; font-family: %2$s; font-size: 12px; line-height: 1.6; color: #64748b;">
                                        Equipo de Coordinación - ManDS Autoescuela<br>
                                        © 2026 ManDS Autoescuela. Todos los derechos reservados.
                                    </td>
                                </tr>
                            </table>
                            <!--[if mso]>
                            </td>
                            </tr>
                            </table>
                            <![endif]-->
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.formatted(
                tituloEscapado,
                FUENTE_CORPORATIVA,
                contenidoPrincipalHtml,
                enlaceHref,
                textoBotonEscapado,
                bloqueAlerta,
                bloqueNotaInferior,
                etiquetaAltEscapada,
                enlaceVisible
        );
    }

    /**
     * Convierte un texto plano multi-párrafo en filas HTML seguras de tabla,
     * maquetando automáticamente listas con viñetas dentro de tarjetas destacadas.
     */
    private String formatearMensajeHtml(String mensaje)
    {
        if (mensaje == null || mensaje.isBlank())
        {
            return this.construirFilaTexto("Tienes una nueva notificación disponible en tu cuenta de ManDS Autoescuela.");
        }

        String normalizado = mensaje.replace("\r\n", "\n").replace("\r", "\n").trim();
        String[] bloques = normalizado.split("\\n\\s*\\n+");
        StringBuilder html = new StringBuilder();

        for (String bloque : bloques)
        {
            String bloqueLimpio = bloque.trim();
            if (bloqueLimpio.isEmpty())
            {
                continue;
            }

            String[] lineas = bloqueLimpio.split("\\n");
            boolean contieneVinetas = false;
            for (String linea : lineas)
            {
                String l = linea.trim();
                if (l.startsWith("• ") || l.startsWith("- "))
                {
                    contieneVinetas = true;
                    break;
                }
            }

            if (contieneVinetas)
            {
                StringBuilder contenidoTarjeta = new StringBuilder();
                StringBuilder lista = new StringBuilder();
                for (String linea : lineas)
                {
                    String l = linea.trim();
                    if (l.startsWith("• ") || l.startsWith("- "))
                    {
                        String item = this.escaparHtmlUtf8(l.substring(2).trim());
                        lista.append("<li>").append(item).append("</li>");
                    }
                    else if (!l.isEmpty())
                    {
                        if (lista.length() > 0)
                        {
                            contenidoTarjeta.append("<ul>").append(lista).append("</ul>");
                            lista.setLength(0);
                        }
                        contenidoTarjeta.append("<div style=\"padding: 0 0 6px 0; color: #0f172a;\"><strong>")
                                .append(this.escaparHtmlUtf8(l))
                                .append("</strong></div>");
                    }
                }
                if (lista.length() > 0)
                {
                    contenidoTarjeta.append("<ul>").append(lista).append("</ul>");
                }

                html.append("""
                    <tr>
                        <td style="padding: 4px 0 18px 0;">
                            <table role="presentation" width="100%%" border="0" cellpadding="0" cellspacing="0" bgcolor="#f8fafc" style="%1$s">
                                <tr>
                                    <td align="left" style="padding: 16px 20px; font-family: %2$s; font-size: 14px; line-height: 1.6; color: #334155;">
                                        %3$s
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    """.formatted(ESTILO_TARJETA_RESUMEN, FUENTE_CORPORATIVA, contenidoTarjeta.toString()));
            }
            else
            {
                StringBuilder parrafo = new StringBuilder();
                for (int i = 0; i < lineas.length; i++)
                {
                    if (i > 0)
                    {
                        parrafo.append("<br>");
                    }
                    parrafo.append(this.escaparHtmlUtf8(lineas[i].trim()));
                }
                html.append(this.construirFilaTexto(parrafo.toString()));
            }
        }

        return html.toString();
    }

    private String construirFilaSaludo(String textoSaludo)
    {
        return """
            <tr>
                <td align="left" style="%1$s">
                    <strong>%2$s</strong>
                </td>
            </tr>
            """.formatted(ESTILO_SALUDO, textoSaludo);
    }

    private String construirFilaTexto(String contenidoHtml)
    {
        return """
            <tr>
                <td align="left" style="%1$s">
                    %2$s
                </td>
            </tr>
            """.formatted(ESTILO_FILA_TEXTO, contenidoHtml);
    }

    private String construirFilaSecundaria(String contenidoHtml)
    {
        return """
            <tr>
                <td align="left" style="%1$s">
                    %2$s
                </td>
            </tr>
            """.formatted(ESTILO_FILA_SECUNDARIA, contenidoHtml);
    }

    private String construirFilaTarjetaResumen(String filasInternasHtml)
    {
        return """
            <tr>
                <td style="padding: 6px 0 20px 0;">
                    <table role="presentation" width="100%%" border="0" cellpadding="0" cellspacing="0" bgcolor="#f8fafc" style="%1$s">
                        <tr>
                            <td style="padding: 16px 20px;">
                                <table role="presentation" width="100%%" border="0" cellpadding="0" cellspacing="0" style="border-collapse: collapse;">
                                    %2$s
                                </table>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
            """.formatted(ESTILO_TARJETA_RESUMEN, filasInternasHtml);
    }

    private String construirFilaResumenClaveValor(String etiquetaHtml, String valorHtml, boolean esFilaTotal)
    {
        String estiloEtiqueta = esFilaTotal ? ESTILO_CELDA_TOTAL_ETIQUETA : ESTILO_CELDA_ETIQUETA;
        String estiloValor = esFilaTotal ? ESTILO_CELDA_TOTAL_VALOR : ESTILO_CELDA_VALOR;

        return """
            <tr>
                <td align="left" valign="top" style="%1$s">%2$s</td>
                <td align="right" valign="top" style="%3$s">%4$s</td>
            </tr>
            """.formatted(estiloEtiqueta, etiquetaHtml, estiloValor, valorHtml);
    }

    private String construirBadgeAlineadoDerecha(String textoEscapado, String colorFondo, String colorTexto, String estiloExtra)
    {
        return """
            <table role="presentation" align="right" border="0" cellpadding="0" cellspacing="0">
                <tr>
                    <td align="center" bgcolor="%1$s" style="background-color: %1$s; font-family: %2$s; line-height: 1.2; color: %3$s; %4$s">
                        <strong>%5$s</strong>
                    </td>
                </tr>
            </table>
            """.formatted(colorFondo, FUENTE_CORPORATIVA, colorTexto, estiloExtra, textoEscapado);
    }

    /**
     * Inserta puntos de ruptura suave (&lt;wbr&gt;) en el texto visible de URLs largas tras delimitadores
     * o cada 24 caracteres sin corte, evitando desbordamientos horizontales en móviles sin usar CSS word-break.
     */
    private String formatearEnlaceVisibleConWbr(String enlaceHref)
    {
        StringBuilder resultado = new StringBuilder();
        int longitud = enlaceHref.length();
        int rachaSinCorte = 0;

        for (int i = 0; i < longitud; i++)
        {
            char c = enlaceHref.charAt(i);
            resultado.append(this.escaparCaracterHtml(c));

            boolean esBarraProtocolo = (c == '/')
                    && ((i > 0 && enlaceHref.charAt(i - 1) == ':')
                    || (i + 1 < longitud && enlaceHref.charAt(i + 1) == '/'));

            boolean esDelimitadorSeguro = (c == '/' && !esBarraProtocolo)
                    || c == '?'
                    || c == '&'
                    || c == '='
                    || c == '-'
                    || c == '_';

            if (esDelimitadorSeguro)
            {
                resultado.append("<wbr>");
                rachaSinCorte = 0;
            }
            else
            {
                rachaSinCorte++;
                if (rachaSinCorte >= 24 && i + 1 < longitud)
                {
                    resultado.append("<wbr>");
                    rachaSinCorte = 0;
                }
            }
        }

        return resultado.toString();
    }

    private String escaparCaracterHtml(char c)
    {
        return switch (c)
        {
            case '&' -> "&amp;";
            case '<' -> "&lt;";
            case '>' -> "&gt;";
            case '"' -> "&quot;";
            case '\'' -> "&#39;";
            default -> String.valueOf(c);
        };
    }

    private String escaparTexto(String texto, String respaldo)
    {
        if (texto == null || texto.isBlank())
        {
            return this.escaparHtmlUtf8(respaldo);
        }
        return this.escaparHtmlUtf8(texto.trim());
    }

    private String escaparHtmlUtf8(String texto)
    {
        return HtmlUtils.htmlEscape(texto, StandardCharsets.UTF_8.name());
    }

    private String sanitizarHref(String enlace)
    {
        if (enlace == null || enlace.isBlank())
        {
            return UrlAplicacionUtil.construirUrl(UrlAplicacionUtil.resolverBaseUrlEfectiva(null, this.appBaseUrl), "/login");
        }
        return enlace.trim().replace("\"", "%22");
    }
}
