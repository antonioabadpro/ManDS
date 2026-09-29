package com.autoescuela.erp.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.stripe.StripeClient;

import org.springframework.beans.factory.annotation.Value;

/**
 * Clase de configuración para la integración con Stripe.
 * Configura el cliente de Stripe con la clave API proporcionada en las propiedades de la aplicación.
 */
@Configuration
public class StripeConfig
{
    @Value("${stripe.api.key}")
    private String stripeApiKey;

    /**
     * Crea y configura un bean de StripeClient utilizando la clave API de Stripe.
     * @return Un objeto StripeClient configurado con la clave API.
     */
    @Bean
    public StripeClient stripeClient()
    {
        return new StripeClient(this.stripeApiKey);
    }

}
