package com.autoescuela.erp.usuarios;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.autoescuela.erp.pagos.dto.SesionPagoDTO;
import com.autoescuela.erp.pagos.service.PagoStripeService;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@TestPropertySource(properties =
{
        "spring.sql.init.mode=always",
        "spring.sql.init.data-locations=classpath:data.sql",
        "spring.jpa.defer-datasource-initialization=true"
})
/**
 * Pruebas de integración visual y autorización para el conjunto de vistas del Rol Alumno.
 */
@Transactional
class VistaAlumnoTest
{
    @Autowired
    private WebApplicationContext contexto;

    @MockitoBean
    private PagoStripeService pagoStripeService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp()
    {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(this.contexto)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("Usuario anónimo que intenta acceder a /alumno/dashboard es redirigido al login")
    void testAccesoAnonimoRedirigeALogin() throws Exception
    {
        this.mockMvc.perform(get("/alumno/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(username = "profesor1", roles = "PROFESOR")
    @DisplayName("Usuario con Rol PROFESOR que intenta acceder a /alumno/dashboard recibe 403 Forbidden")
    void testAccesoProfesorEsDenegado() throws Exception
    {
        this.mockMvc.perform(get("/alumno/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "alumno1", roles = "ALUMNO")
    @DisplayName("GET /alumno/dashboard renderiza correctamente con los 4 KPIs principales")
    void testDashboardRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/alumno/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("alumno/dashboard"))
                .andExpect(content().string(containsString("Panel de Control del Alumno")))
                .andExpect(content().string(containsString("Saldo de Clases")))
                .andExpect(content().string(containsString("Convocatorias DGT")))
                .andExpect(content().string(containsString("Profesor Tutor")));
    }

    @Test
    @WithMockUser(username = "alumno1", roles = "ALUMNO")
    @DisplayName("GET /alumno/calendario renderiza la vista interactiva con contenedor de FullCalendar")
    void testCalendarioRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/alumno/calendario"))
                .andExpect(status().isOk())
                .andExpect(view().name("alumno/calendario"))
                .andExpect(content().string(containsString("Reservar Clases Prácticas")))
                .andExpect(content().string(containsString("id=\"calendario-alumno\"")))
                .andExpect(content().string(containsString("Capacidad de Reserva")));
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
                .andExpect(view().name("alumno/fragments/modal-reservar-clase :: modalReservarClase"))
                .andExpect(content().string(containsString("Reservar Clase Práctica")));
    }

    @Test
    @WithMockUser(username = "alumno1", roles = "ALUMNO")
    @DisplayName("GET /alumno/clases/1/modal devuelve el fragmento con detalle de clase")
    void testModalDetalleClase() throws Exception
    {
        this.mockMvc.perform(get("/alumno/clases/1/modal"))
                .andExpect(status().isOk())
                .andExpect(view().name("alumno/fragments/modal-detalle-clase :: modalDetalleClase"))
                .andExpect(content().string(containsString("Ficha de Clase Práctica")));
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
                .andExpect(header().string("HX-Trigger", "actualizarCalendario"))
                .andExpect(content().string(containsString("¡Clase práctica reservada correctamente")))
                .andExpect(content().string(containsString("id=\"indicador-capacidad-reserva\"")))
                .andExpect(content().string(containsString("hx-swap-oob=\"outerHTML\"")));
    }

    @Test
    @WithMockUser(username = "alumno1", roles = "ALUMNO")
    @DisplayName("POST /alumno/clases/cancelar cancela una clase en PENDIENTE con HTMX")
    void testCancelarClasePracticaHtmx() throws Exception
    {
        this.mockMvc.perform(post("/alumno/clases/cancelar")
                .with(csrf())
                .header("HX-Request", "true")
                .param("claseId", "8"))
                .andExpect(status().isOk())
                .andExpect(header().string("HX-Trigger", "actualizarCalendario"))
                .andExpect(content().string(containsString("cancelada con éxito")))
                .andExpect(content().string(containsString("id=\"indicador-capacidad-reserva\"")))
                .andExpect(content().string(containsString("hx-swap-oob=\"outerHTML\"")));
    }

    @Test
    @WithMockUser(username = "alumno1", roles = "ALUMNO")
    @DisplayName("GET /alumno/clases renderiza el listado consolidado de clases prácticas")
    void testClasesRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/alumno/clases"))
                .andExpect(status().isOk())
                .andExpect(view().name("alumno/clases"))
                .andExpect(content().string(containsString("Historial de Clases Prácticas")))
                .andExpect(content().string(containsString("id=\"tabla-historial-clases\"")));
    }

    @Test
    @WithMockUser(username = "alumno1", roles = "ALUMNO")
    @DisplayName("GET /alumno/examenes renderiza la bandeja de convocatorias y solicitudes DGT")
    void testExamenesRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/alumno/examenes"))
                .andExpect(status().isOk())
                .andExpect(view().name("alumno/examenes"))
                .andExpect(content().string(containsString("Solicitud y Convocatorias DGT")))
                .andExpect(content().string(containsString("Circuito Oficial DGT")));
    }

    @Test
    @WithMockUser(username = "alumno1", roles = "ALUMNO")
    @DisplayName("GET /alumno/notas renderiza las calificaciones y el enlace a la Sede DGT")
    void testNotasRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/alumno/notas"))
                .andExpect(status().isOk())
                .andExpect(view().name("alumno/notas"))
                .andExpect(content().string(containsString("Mis Calificaciones DGT")))
                .andExpect(content().string(containsString("Sede Electrónica DGT")));
    }

    @Test
    @WithMockUser(username = "alumno1", roles = "ALUMNO")
    @DisplayName("GET /alumno/pagos renderiza la tarjeta individual a 30€ y las 3 tarjetas de bonos oficiales")
    void testPagosRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/alumno/pagos"))
                .andExpect(status().isOk())
                .andExpect(view().name("alumno/pagos"))
                .andExpect(content().string(containsString("30,00 €")))
                .andExpect(content().string(containsString("Bono 10 Clases")))
                .andExpect(content().string(containsString("270,00 €")))
                .andExpect(content().string(containsString("Bono 15 Clases")))
                .andExpect(content().string(containsString("Bono 20 Clases")))
                .andExpect(content().string(containsString("Conectar con Pasarela de Pago")));
    }

    @Test
    @WithMockUser(username = "alumno1", roles = "ALUMNO")
    @DisplayName("POST /alumno/pagos/comprar-clases redirige a la pasarela de Stripe para clases individuales")
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
    @DisplayName("POST /alumno/pagos/comprar-clases redirige a la pasarela de Stripe para Bono de 10 clases (1 clase gratis)")
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
    @DisplayName("POST /alumno/pagos/comprar-clases redirige a la pasarela de Stripe para Bono de 15 clases")
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
    @DisplayName("GET /alumno/dashboard muestra el banner de aviso cuando el alumno ha agotado sus convocatorias")
    void testDashboardConvocatoriasAgotadas() throws Exception
    {
        this.mockMvc.perform(get("/alumno/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("alumno/dashboard"))
                .andExpect(content().string(containsString("Convocatorias Agotadas")))
                .andExpect(content().string(containsString("Has consumido todas las convocatorias de tu matrícula")))
                .andExpect(content().string(containsString("Renovar Matrícula Ahora")));
    }

    @Test
    @WithMockUser(username = "alumno8", roles = "ALUMNO")
    @DisplayName("GET /alumno/pagos muestra banner de bloqueo, deshabilita botones de compra y muestra tarjeta de renovación")
    void testPagosConvocatoriasAgotadas() throws Exception
    {
        this.mockMvc.perform(get("/alumno/pagos"))
                .andExpect(status().isOk())
                .andExpect(view().name("alumno/pagos"))
                .andExpect(content().string(containsString("Bloqueo de Compra Activo")))
                .andExpect(content().string(containsString("No puedes adquirir más clases prácticas hasta renovar tu matrícula")))
                .andExpect(content().string(containsString("Compra Bloqueada (Requiere Renovación)")))
                .andExpect(content().string(containsString("id=\"seccion-renovacion\"")))
                .andExpect(content().string(containsString("Renovar Matrícula")));
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
    @DisplayName("GET /alumno/estadisticas renderiza horas al volante y progreso pedagógico")
    void testEstadisticasRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/alumno/estadisticas"))
                .andExpect(status().isOk())
                .andExpect(view().name("alumno/estadisticas"))
                .andExpect(content().string(containsString("Mis Estadísticas de Aprendizaje")))
                .andExpect(content().string(containsString("Horas al Volante")));
    }

    @Test
    @WithMockUser(username = "alumno1", roles = "ALUMNO")
    @DisplayName("GET /alumno/perfil renderiza datos de expediente y formularios")
    void testPerfilRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/alumno/perfil"))
                .andExpect(status().isOk())
                .andExpect(view().name("alumno/perfil"))
                .andExpect(content().string(containsString("Mi Perfil de Alumno")))
                .andExpect(content().string(containsString("Datos de Contacto y Personales")))
                .andExpect(content().string(containsString("Seguridad y Credenciales")));
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
}
