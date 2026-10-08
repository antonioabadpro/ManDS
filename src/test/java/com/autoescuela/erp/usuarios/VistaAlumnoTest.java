package com.autoescuela.erp.usuarios;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import com.autoescuela.erp.BaseIntegrationTest;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.pagos.dto.SesionPagoDTO;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Pruebas de integración visual y controladores para el Rol Alumno.
 * Valida la resolución de plantillas, flujos HTMX y pagos con Stripe heredando de BaseIntegrationTest.
 */
class VistaAlumnoTest extends BaseIntegrationTest
{
    @Test
    @WithMockUser(username = "alumno1", roles = "ALUMNO")
    @DisplayName("GET /alumno/dashboard renderiza correctamente el panel del alumno")
    void testDashboardRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/alumno/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("alumno/dashboard"));
    }

    @Test
    @WithMockUser(username = "alumno1", roles = "ALUMNO")
    @DisplayName("GET /alumno/calendario renderiza la vista interactiva con FullCalendar")
    void testCalendarioRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/alumno/calendario"))
                .andExpect(status().isOk())
                .andExpect(view().name("alumno/calendario"));
    }

    @Test
    @WithMockUser(username = "alumno1", roles = "ALUMNO")
    @DisplayName("GET /alumno/calendario/eventos devuelve JSON con eventos para FullCalendar")
    void testEventosCalendarioApi() throws Exception
    {
        this.mockMvc.perform(get("/alumno/calendario/eventos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "alumno1", roles = "ALUMNO")
    @DisplayName("GET /alumno/clases/reservar-modal devuelve el fragmento modal de reserva")
    void testModalReservarClase() throws Exception
    {
        this.mockMvc.perform(get("/alumno/clases/reservar-modal"))
                .andExpect(status().isOk())
                .andExpect(view().name("alumno/fragments/modal-reservar-clase :: modalReservarClase"));
    }

    @Test
    @WithMockUser(username = "alumno1", roles = "ALUMNO")
    @DisplayName("GET /alumno/clases/35/modal devuelve el fragmento con detalle de clase")
    void testModalDetalleClase() throws Exception
    {
        this.mockMvc.perform(get("/alumno/clases/35/modal"))
                .andExpect(status().isOk())
                .andExpect(view().name("alumno/fragments/modal-detalle-clase :: modalDetalleClase"));
    }

    @Test
    @WithMockUser(username = "alumno1", roles = "ALUMNO")
    @DisplayName("POST /alumno/clases/reservar registra una clase práctica con HTMX")
    void testReservarClasePracticaHtmx() throws Exception
    {
        this.mockMvc.perform(post("/alumno/clases/reservar")
                .with(csrf())
                .header("HX-Request", "true")
                .param("fechaHora", "2026-10-15T11:00")
                .param("duracion", "45")
                .param("puntoRecogida", "Calle Alcalá 45")
                .param("observaciones", "Práctica de estacionamiento"))
                .andExpect(status().isOk())
                .andExpect(header().string("HX-Trigger", "actualizarCalendario"));
    }

    @Test
    @WithMockUser(username = "alumno2", roles = "ALUMNO")
    @DisplayName("POST /alumno/clases/cancelar cancela una clase en PENDIENTE con HTMX")
    void testCancelarClasePracticaHtmx() throws Exception
    {
        this.mockMvc.perform(post("/alumno/clases/cancelar")
                .with(csrf())
                .header("HX-Request", "true")
                .param("claseId", "23"))
                .andExpect(status().isOk())
                .andExpect(header().string("HX-Trigger", "actualizarCalendario"));
    }

    @Test
    @WithMockUser(username = "alumno1", roles = "ALUMNO")
    @DisplayName("GET /alumno/clases renderiza el listado consolidado de clases prácticas")
    void testClasesRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/alumno/clases"))
                .andExpect(status().isOk())
                .andExpect(view().name("alumno/clases"));
    }

    @Test
    @WithMockUser(username = "alumno1", roles = "ALUMNO")
    @DisplayName("GET /alumno/examenes renderiza la bandeja de convocatorias y solicitudes DGT")
    void testExamenesRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/alumno/examenes"))
                .andExpect(status().isOk())
                .andExpect(view().name("alumno/examenes"));
    }

    @Test
    @WithMockUser(username = "alumno1", roles = "ALUMNO")
    @DisplayName("GET /alumno/notas renderiza la vista de calificaciones")
    void testNotasRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/alumno/notas"))
                .andExpect(status().isOk())
                .andExpect(view().name("alumno/notas"));
    }

    @Test
    @WithMockUser(username = "alumno1", roles = "ALUMNO")
    @DisplayName("GET /alumno/pagos renderiza la vista de compra de clases y bonos")
    void testPagosRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/alumno/pagos"))
                .andExpect(status().isOk())
                .andExpect(view().name("alumno/pagos"));
    }

    @Test
    @WithMockUser(username = "alumno1", roles = "ALUMNO")
    @DisplayName("POST /alumno/pagos/comprar-clases redirige a Stripe para clases individuales")
    void testComprarClasesIndividualesExito() throws Exception
    {
        when(this.pagoStripeService.crearSesionPagoClasesPracticas(any(), any(), eq(3), eq(false)))
                .thenReturn(new SesionPagoDTO("cs_clases_ind", "https://checkout.stripe.com/pay/cs_clases_ind", 9000L, "EUR"));

        this.mockMvc.perform(post("/alumno/pagos/comprar-clases")
                .with(csrf())
                .param("tipoProducto", "INDIVIDUAL")
                .param("cantidadClases", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("https://checkout.stripe.com/pay/cs_clases_ind"));
    }

    @Test
    @WithMockUser(username = "alumno1", roles = "ALUMNO")
    @DisplayName("POST /alumno/pagos/comprar-clases redirige a Stripe para Bono de 10 clases")
    void testComprarBono10Exito() throws Exception
    {
        when(this.pagoStripeService.crearSesionPagoClasesPracticas(any(), any(), eq(10), eq(true)))
                .thenReturn(new SesionPagoDTO("cs_bono_10", "https://checkout.stripe.com/pay/cs_bono_10", 27000L, "EUR"));

        this.mockMvc.perform(post("/alumno/pagos/comprar-clases")
                .with(csrf())
                .param("tipoProducto", "BONO_10")
                .param("cantidadClases", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("https://checkout.stripe.com/pay/cs_bono_10"));
    }

    @Test
    @WithMockUser(username = "alumno1", roles = "ALUMNO")
    @DisplayName("POST /alumno/pagos/comprar-clases redirige a Stripe para Bono de 15 clases")
    void testComprarBonoExito() throws Exception
    {
        when(this.pagoStripeService.crearSesionPagoClasesPracticas(any(), any(), eq(15), eq(true)))
                .thenReturn(new SesionPagoDTO("cs_bono_15", "https://checkout.stripe.com/pay/cs_bono_15", 40000L, "EUR"));

        this.mockMvc.perform(post("/alumno/pagos/comprar-clases")
                .with(csrf())
                .param("tipoProducto", "BONO_15")
                .param("cantidadClases", "15"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("https://checkout.stripe.com/pay/cs_bono_15"));
    }

    @Test
    @WithMockUser(username = "alumno8", roles = "ALUMNO")
    @DisplayName("GET /alumno/dashboard responde correctamente cuando el alumno ha agotado convocatorias")
    void testDashboardConvocatoriasAgotadas() throws Exception
    {
        this.mockMvc.perform(get("/alumno/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("alumno/dashboard"));
    }

    @Test
    @WithMockUser(username = "alumno8", roles = "ALUMNO")
    @DisplayName("GET /alumno/pagos responde correctamente para alumno con convocatorias agotadas")
    void testPagosConvocatoriasAgotadas() throws Exception
    {
        this.mockMvc.perform(get("/alumno/pagos"))
                .andExpect(status().isOk())
                .andExpect(view().name("alumno/pagos"));
    }

    @Test
    @WithMockUser(username = "alumno8", roles = "ALUMNO")
    @DisplayName("POST /alumno/pagos/comprar-clases bloquea la compra si el alumno tiene convocatorias agotadas")
    void testComprarClasesConvocatoriasAgotadasBloqueado() throws Exception
    {
        this.mockMvc.perform(post("/alumno/pagos/comprar-clases")
                .with(csrf())
                .param("tipoProducto", "INDIVIDUAL")
                .param("cantidadClases", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/alumno/pagos"))
                .andExpect(flash().attribute("error", containsString("Has agotado las convocatorias de examen de tu matrícula")));
    }

    @Test
    @WithMockUser(username = "alumno1", roles = "ALUMNO")
    @DisplayName("GET /alumno/estadisticas renderiza la vista de estadísticas de aprendizaje")
    void testEstadisticasRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/alumno/estadisticas"))
                .andExpect(status().isOk())
                .andExpect(view().name("alumno/estadisticas"));
    }

    @Test
    @WithMockUser(username = "alumno1", roles = "ALUMNO")
    @DisplayName("GET /alumno/perfil renderiza la vista de perfil de alumno")
    void testPerfilRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/alumno/perfil"))
                .andExpect(status().isOk())
                .andExpect(view().name("alumno/perfil"))
                .andExpect(model().attributeExists("perfilDTO"));
    }

    @Test
    @WithMockUser(username = "alumno1", roles = "ALUMNO")
    @DisplayName("POST /alumno/perfil actualiza los datos de contacto correctamente")
    void testActualizarPerfilExito() throws Exception
    {
        this.mockMvc.perform(post("/alumno/perfil")
                .with(csrf())
                .param("nombre", "Jose")
                .param("apellidos", "López Martínez")
                .param("telefono", "611223399")
                .param("direccion", "Calle Gran Vía 28, Madrid")
                .param("correo", "jose.actualizado@autoescuela.es")
                .param("fechaNacimiento", "2004-03-15"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/alumno/perfil"))
                .andExpect(flash().attributeExists("mensajeExito"));
    }

    @Test
    @WithMockUser(username = "alumno1", roles = "ALUMNO")
    @DisplayName("POST /alumno/perfil con datos inválidos permanece en la vista con errores de validación")
    void testActualizarPerfilErrorValidacion() throws Exception
    {
        this.mockMvc.perform(post("/alumno/perfil")
                .with(csrf())
                .param("nombre", "")
                .param("apellidos", "")
                .param("telefono", "123")
                .param("direccion", "")
                .param("fechaNacimiento", "2025-01-01"))
                .andExpect(status().isOk())
                .andExpect(view().name("alumno/perfil"))
                .andExpect(model().attributeHasFieldErrors("perfilDTO", "nombre", "apellidos", "telefono", "direccion", "mayorDeEdad"));
    }

    @Test
    @WithMockUser(username = "alumno1", roles = "ALUMNO")
    @DisplayName("POST /alumno/perfil con teléfono duplicado muestra error en el modelo")
    void testActualizarPerfilTelefonoDuplicado() throws Exception
    {
        this.mockMvc.perform(post("/alumno/perfil")
                .with(csrf())
                .param("nombre", "Jose")
                .param("apellidos", "López Martínez")
                .param("telefono", "600111222")
                .param("direccion", "Calle Gran Vía 28, Madrid")
                .param("fechaNacimiento", "2004-03-15"))
                .andExpect(status().isOk())
                .andExpect(view().name("alumno/perfil"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    @WithMockUser(username = "alumno22", roles = "ALUMNO")
    @DisplayName("GET /alumno/dashboard para alumno sin matrícula activa renderiza vista del dashboard")
    void testDashboardAlumnoSinMatriculaActivaRenderizaBloque() throws Exception
    {
        this.mockMvc.perform(get("/alumno/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("alumno/dashboard"));
    }

    @Test
    @WithMockUser(username = "alumno22", roles = "ALUMNO")
    @DisplayName("POST /alumno/matricular inicia sesión de pago en Stripe para alumno sin matrícula activa")
    void testMatricularNuevoCarnetExito() throws Exception
    {
        when(this.pagoStripeService.crearSesionPagoMatricula(eq(TipoCarnet.PERMISO_C), any()))
                .thenReturn(new SesionPagoDTO("cs_test_mock_matricula", "https://checkout.stripe.com/pay/cs_test_mock_matricula", 45000L, "EUR"));

        this.mockMvc.perform(post("/alumno/matricular")
                .with(csrf())
                .param("tipoCarnet", "PERMISO_C"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("https://checkout.stripe.com/pay/cs_test_mock_matricula"));
    }

    @Test
    @WithMockUser(username = "alumno1", roles = "ALUMNO")
    @DisplayName("POST /alumno/matricular rechaza la solicitud si el alumno ya tiene un permiso activo")
    void testMatricularNuevoCarnetRechazadoPorPermisoActivo() throws Exception
    {
        this.mockMvc.perform(post("/alumno/matricular")
                .with(csrf())
                .param("tipoCarnet", "PERMISO_A2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/alumno/dashboard"))
                .andExpect(flash().attribute("error", containsString("Ya tienes una matrícula activa")));
    }
}
