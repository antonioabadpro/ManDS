package com.autoescuela.erp.academico.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.autoescuela.erp.academico.model.Matricula;
import com.autoescuela.erp.academico.service.AcademicoService;
import com.autoescuela.erp.core.enums.ModalidadMatricula;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.examenes.service.ExamenService;
import com.autoescuela.erp.pagos.dto.SesionPagoDTO;
import com.autoescuela.erp.pagos.service.PagoStripeService;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.service.AlumnoService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

/**
 * Controlador de dominio para la consulta de tarifas, estado del saldo formativo
 * y adquisición de clases prácticas y bonos de ahorro para el alumno.
 */
@Controller
@RequestMapping("/alumno/pagos")
@RequiredArgsConstructor
public class PagoPracticasController
{
    private final AcademicoService academicoService;
    private final AlumnoService alumnoService;
    private final PagoStripeService pagoStripeService;
    private final ExamenService examenService;

    /**
     * Muestra el panel de adquisición de clases prácticas, bonos y renovación de matrícula.
     */
    @GetMapping
    public String mostrarPagos(@AuthenticationPrincipal Object principal, Model model, RedirectAttributes redirectAttributes)
    {
        Alumno alumno = this.alumnoService.obtenerAlumnoActual(principal);
        if (alumno == null)
        {
            return "redirect:/login";
        }

        Matricula matricula = this.academicoService.buscarMatriculaActiva(alumno).orElse(null);
        if (matricula == null)
        {
            redirectAttributes.addFlashAttribute("error", "Debes disponer de una matrícula activa para acceder a la compra de clases prácticas.");
            return "redirect:/alumno/dashboard";
        }

        if (!matricula.tieneConvocatoriasAgotadas() && matricula.getModalidad() == ModalidadMatricula.TEORICO_PRACTICA)
        {
            boolean teoricoAprobado = this.examenService.tieneTeoricoAprobado(alumno);
            if (!teoricoAprobado)
            {
                redirectAttributes.addFlashAttribute("error", "No puedes acceder a la compra de clases prácticas hasta haber aprobado el examen teórico oficial ante la DGT.");
                return "redirect:/alumno/dashboard";
            }
        }

        boolean esVehiculoPesado = matricula.getPermisoCarnet() != null &&
                (matricula.getPermisoCarnet().name().startsWith("PERMISO_C") || matricula.getPermisoCarnet().name().startsWith("PERMISO_D"));
        boolean convocatoriasAgotadas = matricula.tieneConvocatoriasAgotadas();

        model.addAttribute("matricula", matricula);
        model.addAttribute("esVehiculoPesado", esVehiculoPesado);
        model.addAttribute("convocatoriasAgotadas", convocatoriasAgotadas);

        return "alumno/pagos";
    }

    /**
     * Inicia el proceso de pago para la compra de saldo de clases prácticas o bonos a través de Stripe Checkout.
     */
    @PostMapping("/comprar-clases")
    public String comprarClasesPracticas(@AuthenticationPrincipal Object principal,
            @RequestParam("tipoProducto") String tipoProducto,
            @RequestParam(value = "cantidadClases", defaultValue = "1") Integer cantidadClases,
            RedirectAttributes redirectAttributes,
            HttpSession sesion)
    {
        Alumno alumno = this.alumnoService.obtenerAlumnoActual(principal);
        if (alumno == null)
        {
            return "redirect:/login";
        }

        int clasesCompradas = 1;
        boolean esOferta = false;

        Matricula matricula = this.academicoService.buscarMatriculaActiva(alumno).orElse(null);
        if (matricula == null)
        {
            redirectAttributes.addFlashAttribute("error", "No se encontró un expediente de matrícula activo para recargar saldo.");
            return "redirect:/alumno/pagos";
        }

        // Bloqueo de compra de clases si ha agotado sus convocatorias
        if (matricula.tieneConvocatoriasAgotadas())
        {
            redirectAttributes.addFlashAttribute("error", "Has agotado las convocatorias de examen de tu matrícula. No puedes adquirir clases prácticas ni bonos hasta renovar tu matrícula.");
            return "redirect:/alumno/pagos";
        }

        // Examen teórico aprobado en modalidad TEORICO_PRACTICA
        if (matricula.getModalidad() == ModalidadMatricula.TEORICO_PRACTICA)
        {
            boolean teoricoAprobado = this.examenService.tieneTeoricoAprobado(alumno);
            if (!teoricoAprobado)
            {
                redirectAttributes.addFlashAttribute("error", "No puedes adquirir clases prácticas hasta haber aprobado el examen teórico oficial ante la DGT.");
                return "redirect:/alumno/dashboard";
            }
        }

        switch (tipoProducto != null ? tipoProducto.toUpperCase() : "")
        {
            case "BONO_10":
                clasesCompradas = 10;
                esOferta = true;
                break;
            case "BONO_15":
                clasesCompradas = 15;
                esOferta = true;
                break;
            case "BONO_20":
                clasesCompradas = 20;
                esOferta = true;
                break;
            default:
                if (cantidadClases != null && cantidadClases > 0)
                {
                    clasesCompradas = cantidadClases;
                    esOferta = false;
                }
                break;
        }

        try
        {
            SesionPagoDTO sesionPago = this.pagoStripeService.crearSesionPagoClasesPracticas(alumno.getId(), matricula.getPermisoCarnet(), clasesCompradas, esOferta);

            sesion.setAttribute("pagoClases_alumnoId", alumno.getId());
            sesion.setAttribute("pagoClases_numero", clasesCompradas);
            sesion.setAttribute("pagoClases_importe", sesionPago.importeTotal() / 100.0f);
            sesion.setAttribute("pagoClases_tipoCarnet", matricula.getPermisoCarnet().getDescripcion());

            return "redirect:" + sesionPago.urlStripe();
        }
        catch (ReglaNegocioException ex)
        {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/alumno/pagos";
        }
    }
}
