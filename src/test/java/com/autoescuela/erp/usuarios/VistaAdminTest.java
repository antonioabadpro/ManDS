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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
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
 * Pruebas de integración visual y autorización para el conjunto de vistas del Rol Administrador.
 */
@Transactional
class VistaAdminTest
{
    @Autowired
    private WebApplicationContext contexto;

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
    @DisplayName("Usuario anónimo que intenta acceder a /admin/dashboard es redirigido al login")
    void testAccesoAnonimoRedirigeALogin() throws Exception
    {
        this.mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /admin/dashboard renderiza correctamente con KPIs y menú de administración")
    void testDashboardRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(content().string(containsString("Panel de Control del Administrador")))
                .andExpect(content().string(containsString("Resumen de la Autoescuela")))
                .andExpect(content().string(containsString("Alumnos Activos")))
                .andExpect(content().string(containsString("Profesores en Plantilla")));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /admin/profesores renderiza correctamente la gestión de profesores")
    void testProfesoresRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/admin/profesores"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/profesores"))
                .andExpect(content().string(containsString("Gestión de Profesores")))
                .andExpect(content().string(containsString("Equipo Docente")))
                .andExpect(content().string(containsString("Laura Sánchez Romero")));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /admin/alumnos renderiza correctamente la gestión de alumnos")
    void testAlumnosRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/admin/alumnos"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/alumnos"))
                .andExpect(content().string(containsString("Gestión de Alumnos")))
                .andExpect(content().string(containsString("Expedientes de Alumnado")))
                .andExpect(content().string(containsString("Elena Martínez López")));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /admin/flota renderiza correctamente la gestión de flota")
    void testFlotaRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/admin/flota"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/flota"))
                .andExpect(content().string(containsString("Gestión de Flota de Vehículos")))
                .andExpect(content().string(containsString("Parque Móvil de la Autoescuela")))
                .andExpect(content().string(containsString("1234-LMN")));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /admin/incidencias renderiza correctamente la bandeja de incidencias")
    void testIncidenciasRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/admin/incidencias"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/incidencias"))
                .andExpect(content().string(containsString("Incidencias de Flota")))
                .andExpect(content().string(containsString("Bandeja de Incidencias Mecánicas")))
                .andExpect(content().string(containsString("3456-FGH")));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /admin/examenes renderiza correctamente la cola FIFO y convocatorias DGT")
    void testExamenesRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/admin/examenes"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/examenes"))
                .andExpect(content().string(containsString("Convocatorias y Solicitudes DGT")))
                .andExpect(content().string(containsString("Cola de Solicitudes FIFO")))
                .andExpect(content().string(containsString("Convocatorias Oficiales del Mes")));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /admin/practicas renderiza correctamente la agenda global de prácticas")
    void testPracticasRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/admin/practicas"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/practicas"))
                .andExpect(content().string(containsString("Agenda Global de Prácticas")))
                .andExpect(content().string(containsString("Control de Clases Prácticas")))
                .andExpect(content().string(containsString("Calle Alcalá 45")));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /admin/estadisticas renderiza correctamente el panel de métricas globales")
    void testEstadisticasRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/admin/estadisticas"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/estadisticas"))
                .andExpect(content().string(containsString("Estadísticas y Rendimiento Global")))
                .andExpect(content().string(containsString("Tasa de Aprobados")))
                .andExpect(content().string(containsString("Facturación Bruta")));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /admin/dashboard incluye los nuevos componentes de la top bar: reloj, modo oscuro y avatar de usuario")
    void testTopBarComponentesRenderizados() throws Exception
    {
        this.mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"reloj-sistema-texto\"")))
                .andExpect(content().string(containsString("id=\"btn-theme-toggle\"")))
                .andExpect(content().string(containsString("id=\"btn-user-menu\"")))
                .andExpect(content().string(containsString("action=\"/logout\"")));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /admin/perfil renderiza correctamente el formulario de Mi Perfil y botón de cambio de contraseña")
    void testPerfilAdminRenderizado() throws Exception
    {
        this.mockMvc.perform(get("/admin/perfil"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/perfil"))
                .andExpect(content().string(containsString("Mi Perfil")))
                .andExpect(content().string(containsString("Información Personal")))
                .andExpect(content().string(containsString("Seguridad y Acceso")))
                .andExpect(content().string(containsString("Cambiar Contraseña")))
                .andExpect(content().string(containsString("action=\"/recuperar-password\"")));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /perfil redirige a /admin/perfil para un usuario autenticado con Rol ADMIN")
    void testRedireccionPerfilAdmin() throws Exception
    {
        this.mockMvc.perform(get("/perfil"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/perfil"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("POST /admin/perfil actualiza los datos personales y redirige con mensaje flash de éxito")
    void testActualizarPerfilAdminExito() throws Exception
    {
        this.mockMvc.perform(post("/admin/perfil")
                .with(csrf())
                .param("nombre", "Carlos Modificado")
                .param("apellidos", "García Moreno")
                .param("telefono", "611223344")
                .param("direccion", "Calle Gran Vía 100, Madrid")
                .param("fechaNacimiento", "1985-04-12"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/perfil"))
                .andExpect(flash().attributeExists("mensajeExito"));
    }
}
