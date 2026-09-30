package com.autoescuela.erp.pagos.service;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.pagos.dto.SesionPagoDTO;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PagoStripeService
{
    private final StripeClient stripeClient;

    @Value("${app_base_url:http://localhost:8080}")
    private String appBaseUrl;

    /**
     * Crea una sesión de pago en Stripe.
     * @param parametros Parámetros para la creación de la sesión de pago.
     * @param importeTotalCents Importe total en céntimos.
     * @return DTO con la información de la sesión de pago.
     */
    private SesionPagoDTO crearSesionPago(SessionCreateParams parametros, long importeTotalCents)
    {
        try
        {
            Session session = this.stripeClient.v1().checkout().sessions().create(parametros);
            log.info("Sesión de Stripe creada satisfactoriamente. ID: {}", session.getId());
            return new SesionPagoDTO(session.getId(), session.getUrl(), importeTotalCents, "EUR");
        }
        catch (StripeException e)
        {
            log.error("Error al crear la sesión de pago en Stripe: {}", e.getMessage(), e);
            throw new ReglaNegocioException("No se pudo crear la sesión de pago. Por favor, inténtelo de nuevo más tarde.");
        }
    }

    /**
     * Crea una sesión de pago para la matrícula de un alumno según el tipo de carnet y su DNI.
     * @param tipoCarnet Tipo de carnet del alumno (permiso de conducir).
     * @param dniAlumno DNI del alumno.
     * @return DTO con la información de la sesión de pago.
     */
    public SesionPagoDTO crearSesionPagoMatricula(TipoCarnet tipoCarnet, String dniAlumno)
    {
        if(tipoCarnet == null || dniAlumno == null || dniAlumno.isBlank())
        {
            throw new ReglaNegocioException("El DNI del alumno y el tipo de carnet son obligatorios para formalizar la matrícula.");
        }
        // Regla de Negocio: Permisos C y D (450€), resto (250€)
        boolean esVehiculoPesado = tipoCarnet.name().startsWith("PERMISO_C") || tipoCarnet.name().startsWith("PERMISO_D");
        long importeTotalCents = esVehiculoPesado ? 45000L : 25000L; // Convertir a céntimos

        long tasaDgtCents = 9405L; // Tasa oficial DGT fijada en 94,05 €
        long cuotaAutoescuelaCents = importeTotalCents - tasaDgtCents;

        double tasaDgt = tasaDgtCents / 100.0;
        double cuotaAutoescuela = cuotaAutoescuelaCents / 100.0;

        // Creamos la sesión de pago en Stripe con los parámetros necesarios
        SessionCreateParams parametros = SessionCreateParams.builder()
                .setLocale(SessionCreateParams.Locale.ES)
                .setMode(SessionCreateParams.Mode.PAYMENT) // Modo de pago único
                .setSuccessUrl(this.appBaseUrl + "/pagos/checkout/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(this.appBaseUrl + "/pagos/checkout/cancel")
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD) // Solo permito pagos con tarjeta para evitar los pagos asíncronos de Bizum y PayPal o transferencias
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("EUR")
                                                .setUnitAmount(tasaDgtCents)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Tasa Oficial DGT y Apertura de Expediente")
                                                                .setDescription("Tramitación y gestión de apertura de expediente ante la Jefatura Provincial de Tráfico.")
                                                                .build())
                                                .build())
                                .build())
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("EUR")
                                                .setUnitAmount(cuotaAutoescuelaCents)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Matrícula Autoescuela - " + tipoCarnet.getDescripcion())
                                                                .setDescription("Incluye 2 convocatorias oficiales a examen, seguro escolar y seguro de responsabilidad civil (Alumno DNI: " + dniAlumno + ").")
                                                                .build())
                                                .build())
                                .build())
                // Metadata que Stripe devolverá en los webhooks para identificar y conciliar la operación
                .putMetadata("tipoOperacion", "MATRICULA")
                .putMetadata("tipoCarnet", tipoCarnet.name())
                .putMetadata("dniAlumno", dniAlumno)
                .putMetadata("tasaDgt", String.format(Locale.US, "%.2f", tasaDgt))
                .putMetadata("cuotaAutoescuela", String.format(Locale.US, "%.2f", cuotaAutoescuela))
                .build();

        return this.crearSesionPago(parametros, importeTotalCents);
    }

    /**
     * Crea una sesión de pago para las clases prácticas de un alumno según el tipo de carnet y su DNI.
     * @param tipoCarnet Tipo de carnet del alumno (permiso de conducir).
     * @param dniAlumno DNI del alumno.
     * @param numeroClases Número de clases prácticas.
     * @param esOferta Indica si se trata de una oferta especial.
     * @return DTO con la información de la sesión de pago.
     */
    public SesionPagoDTO crearSesionPagoClasesPracticas(TipoCarnet tipoCarnet, String dniAlumno, int numeroClases, boolean esOferta)
    {
        long precioTotalCents = 0L;

        if (tipoCarnet == null || dniAlumno == null || dniAlumno.isBlank())
        {
            throw new ReglaNegocioException("El DNI del alumno y el tipo de carnet son obligatorios para la compra de clases prácticas.");
        }
        if (numeroClases <= 0)
        {
            throw new ReglaNegocioException("El número de clases prácticas debe ser un valor positivo.");
        }

        // Regla de Negocio: Permisos C y D (vehículos pesados), resto (vehículos estándar)
        boolean esVehiculoPesado = tipoCarnet.name().startsWith("PERMISO_C") || tipoCarnet.name().startsWith("PERMISO_D");

        if (esOferta) // Bono de formación intensiva con descuento
        {
            switch (numeroClases)
            {
                case 10:
                    {
                        precioTotalCents = esVehiculoPesado ? 54000L : 27000L; // 540 € (pesado) vs 270 € (estándar)
                    }; break;
                case 15:
                    {
                        precioTotalCents = esVehiculoPesado ? 80000L : 40000L; // 800 € (pesado) vs 400 € (estándar)
                    }; break;
                case 20:
                    {
                        precioTotalCents = esVehiculoPesado ? 100000L : 50000L; // 1000 € (pesado) vs 500 € (estándar)
                    }; break;
                default:
                    throw new ReglaNegocioException("El número de clases no es válido para una oferta.");
            }
        }
        else // Clases sueltas a tarifa estándar
        {
            long precioPorClaseCents = esVehiculoPesado ? 6000L : 3000L; // 60 € (pesado) vs 30 € (estándar)
            precioTotalCents = precioPorClaseCents * numeroClases;
        }

        double precioTotal = precioTotalCents / 100.0;

        String nombreProducto;
        String descripcionProducto;

        if (esOferta)
        {
            if (numeroClases == 10)
            {
                nombreProducto = "Bono 10 Clases Prácticas (1 Clase Gratis) - " + tipoCarnet.getDescripcion();
                descripcionProducto = "Pack de 10 clases prácticas oficiales de 45 min con 1 clase gratis de regalo incluida para " + tipoCarnet.getDescripcion() + " (Alumno DNI: " + dniAlumno + ").";
            }
            else
            {
                nombreProducto = "Bono " + numeroClases + " Clases Prácticas - " + tipoCarnet.getDescripcion();
                descripcionProducto = "Pack de formación intensiva (" + numeroClases + " clases) con tarifa bonificada para " + tipoCarnet.getDescripcion() + " (Alumno DNI: " + dniAlumno + ").";
            }
        }
        else
        {
            nombreProducto = numeroClases + (numeroClases == 1 ? " Clase Práctica - " : " Clases Prácticas - ") + tipoCarnet.getDescripcion();
            descripcionProducto = "Sesión(es) oficial(es) de conducción de 45 minutos para " + tipoCarnet.getDescripcion() + " (Alumno DNI: " + dniAlumno + ").";
        }

        // Creamos la sesión de pago en Stripe Checkout
        SessionCreateParams parametros = SessionCreateParams.builder()
                .setLocale(SessionCreateParams.Locale.ES)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(this.appBaseUrl + "/pagos/clases/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(this.appBaseUrl + "/pagos/clases/cancel")
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD) // Solo permito pagos con tarjeta para evitar los pagos asíncronos de Bizum y PayPal o transferencias
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("EUR")
                                                .setUnitAmount(precioTotalCents)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName(nombreProducto)
                                                                .setDescription(descripcionProducto)
                                                                .build())
                                                .build())
                                .build())
                // Metadata devuelta en webhooks para reconciliación y abono
                .putMetadata("tipoOperacion", "CLASES_PRACTICAS")
                .putMetadata("tipoCarnet", tipoCarnet.name())
                .putMetadata("dniAlumno", dniAlumno)
                .putMetadata("numeroClases", String.valueOf(numeroClases))
                .putMetadata("esOferta", String.valueOf(esOferta))
                .putMetadata("importeTotal", String.format(Locale.US, "%.2f", precioTotal))
                .build();

        return this.crearSesionPago(parametros, precioTotalCents);
    }

}
