package com.autoescuela.erp.pagos.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO para representar la información de una sesión de pago.
 * @param idSesion Identificador único de la sesión en Stripe.
 * @param urlStripe URL de redirección segura alojada en Stripe a la que se redirige al usuario para completar el pago.
 * @param importeTotal Importe total de la sesión de pago en céntimos (1 euro = 100 céntimos).
 * @param moneda Moneda de la sesión de pago (por ejemplo, "EUR" para euros).
 */
public record SesionPagoDTO(
    @NotNull(message = "El identificador de sesión no puede ser nulo")
    String idSesion,
    @NotNull(message = "La URL de Stripe no puede ser nula")
    String urlStripe,
    @NotNull(message = "El importe total no puede ser nulo")
    Long importeTotal,
    @NotNull(message = "La moneda no puede ser nula")
    String moneda
)
{
    public SesionPagoDTO(String idSesion, String urlStripe, Long importeTotal, String moneda)
    {
        this.idSesion = idSesion;
        this.urlStripe = urlStripe;
        this.importeTotal = importeTotal;
        this.moneda = moneda;
    }
}
