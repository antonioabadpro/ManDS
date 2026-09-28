package com.autoescuela.erp.pagos.controller;

import java.util.Locale;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.autoescuela.erp.core.enums.TipoCarnet;

import lombok.extern.slf4j.Slf4j;

/**
 * Controlador para la gestión de pagos y pasarela de matriculación.
 * Alberga temporalmente el flujo de checkout simulado previo a la integración
 * oficial con Stripe API, así como los futuros webhooks de pago.
 */
@Slf4j
@Controller
@RequestMapping("/pagos")
public class StripeWebhookController
{
    /**
     * Muestra la interfaz de pasarela de pago para el abono de la tasa de matrícula.
     * Aplica la regla de negocio de precios según el tipo de permiso:
     * - Permisos C y D (vehículos grandes y pesados): 450,00 €
     * - Resto de permisos: 250,00 €
     *
     * @param tipoCarnetStr Nombre del enum {@link TipoCarnet} recibido como parámetro.
     * @param model Modelo para abastecer a la plantilla Thymeleaf.
     * @return Nombre de la vista de checkout ("pagos/checkout").
     */
    @GetMapping("/checkout")
    public String mostrarCheckoutMatricula(
            @RequestParam(name = "tipoCarnet", required = false) String tipoCarnetStr,
            Model model)
    {
        TipoCarnet tipoCarnet = TipoCarnet.PERMISO_B;
        if (tipoCarnetStr != null && !tipoCarnetStr.isBlank())
        {
            try
            {
                tipoCarnet = TipoCarnet.valueOf(tipoCarnetStr.trim().toUpperCase());
            }
            catch (IllegalArgumentException ex)
            {
                log.warn("Tipo de carnet no reconocido en pasarela: '{}'. Se aplica PERMISO_B por defecto.", tipoCarnetStr);
            }
        }

        // Regla de Negocio: Permisos C y D (450€), resto (250€)
        boolean esPesado = tipoCarnet.name().startsWith("PERMISO_C") || tipoCarnet.name().startsWith("PERMISO_D");
        double importeTotal = esPesado ? 450.00 : 250.00;
        double tasaDgt = 94.05;
        double cuotaAutoescuela = importeTotal - tasaDgt;

        model.addAttribute("tipoCarnet", tipoCarnet);
        model.addAttribute("descripcionCarnet", tipoCarnet.getDescripcion());
        model.addAttribute("esPesado", esPesado);
        model.addAttribute("importeTotal", String.format(Locale.GERMAN, "%.2f", importeTotal));
        model.addAttribute("importeTotalNum", (int) importeTotal);
        model.addAttribute("cuotaAutoescuela", String.format(Locale.GERMAN, "%.2f", cuotaAutoescuela));
        model.addAttribute("tasaDgt", String.format(Locale.GERMAN, "%.2f", tasaDgt));

        return "pagos/checkout";
    }
}
