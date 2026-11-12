package com.autoescuela.erp.flota.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import com.autoescuela.erp.BaseIntegrationTest;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Pruebas de integración para {@link IncidenciaVehiculoController}.
 * Valida la bandeja de incidencias, transiciones de taller y reporte de averías por el profesor.
 */
class IncidenciaVehiculoControllerTest extends BaseIntegrationTest
{
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("Endpoints de administrador gestionan listado, modales, transición a EN_PROCESO y errores")
    void testAdminGestionIncidencias() throws Exception
    {
        this.mockMvc.perform(get("/admin/incidencias"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/incidencias"))
                .andExpect(model().attributeExists("incidencias", "totalPendientes", "totalEnProceso", "totalResueltas"));

        this.mockMvc.perform(get("/admin/incidencias/gestionar/2"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/fragments/modal-gestionar-incidencia :: modal-gestionar-incidencia"))
                .andExpect(model().attributeExists("incidencia"));

        this.mockMvc.perform(get("/admin/incidencias/detalle/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/fragments/modal-detalle-incidencia :: modal-detalle-incidencia"))
                .andExpect(model().attributeExists("incidencia"));

        // ReglaNegocioException: transición inválida (incidencia 3 ya RESUELTA -> EN_PROCESO) en HTMX y no-HTMX
        this.mockMvc.perform(post("/admin/incidencias/gestionar")
                        .with(csrf())
                        .header("HX-Request", "true")
                        .param("id", "3")
                        .param("estado", "EN_PROCESO"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/fragments/modal-gestionar-incidencia :: modal-gestionar-incidencia"))
                .andExpect(model().attributeExists("errorGestionarIncidencia"));

        this.mockMvc.perform(post("/admin/incidencias/gestionar")
                        .with(csrf())
                        .param("id", "3")
                        .param("estado", "EN_PROCESO"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/incidencias"))
                .andExpect(model().attributeExists("errorGestionarIncidencia"));

        // Resolver incidencia 1 (EN_PROCESO -> RESUELTA)
        this.mockMvc.perform(post("/admin/incidencias/gestionar")
                        .with(csrf())
                        .header("HX-Request", "true")
                        .param("id", "1")
                        .param("estado", "RESUELTA"))
                .andExpect(status().isOk())
                .andExpect(header().string("HX-Redirect", "/admin/incidencias"));

        // Pasar incidencia 9 (PENDIENTE -> EN_PROCESO)
        this.mockMvc.perform(post("/admin/incidencias/gestionar")
                        .with(csrf())
                        .param("id", "9")
                        .param("estado", "EN_PROCESO"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/incidencias"))
                .andExpect(flash().attributeExists("mensajeExito"));
    }

    @Test
    @WithMockUser(username = "profesor1", roles = "PROFESOR")
    @DisplayName("POST /profesor/vehiculo/incidencia reporta avería y valida descripción en HTMX y estándar")
    void testReportarIncidenciaProfesor() throws Exception
    {
        // Descripción inválida (<10 chars) en HTMX y no-HTMX
        this.mockMvc.perform(post("/profesor/vehiculo/incidencia")
                        .with(csrf())
                        .header("HX-Request", "true")
                        .param("descripcion", "corta"))
                .andExpect(status().isOk())
                .andExpect(view().name("profesor/fragments/modal-reportar-incidencia :: modal-reportar-incidencia"))
                .andExpect(model().attributeExists("errorReportarIncidencia"));

        this.mockMvc.perform(post("/profesor/vehiculo/incidencia")
                        .with(csrf())
                        .param("descripcion", "corta"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profesor/vehiculo"))
                .andExpect(flash().attributeExists("error"));

        // Vehículo 1 ya tiene incidencia PENDIENTE -> ReglaNegocioException en HTMX y no-HTMX
        this.mockMvc.perform(post("/profesor/vehiculo/incidencia")
                        .with(csrf())
                        .header("HX-Request", "true")
                        .param("descripcion", "Ruido metálico pronunciado al girar la dirección a la izquierda."))
                .andExpect(status().isOk())
                .andExpect(view().name("profesor/fragments/modal-reportar-incidencia :: modal-reportar-incidencia"))
                .andExpect(model().attributeExists("errorReportarIncidencia"));

        this.mockMvc.perform(post("/profesor/vehiculo/incidencia")
                        .with(csrf())
                        .param("descripcion", "Ruido metálico pronunciado al girar la dirección a la izquierda."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profesor/vehiculo"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @WithMockUser(username = "profesor7", roles = "PROFESOR")
    @DisplayName("POST /profesor/vehiculo/incidencia registra avería válida vía HTMX")
    void testReportarIncidenciaValidaProfesor7() throws Exception
    {
        this.mockMvc.perform(post("/profesor/vehiculo/incidencia")
                        .with(csrf())
                        .header("HX-Request", "true")
                        .param("vehiculoId", "16")
                        .param("descripcion", "Ruido metálico pronunciado al girar la dirección a la izquierda."))
                .andExpect(status().isOk())
                .andExpect(header().string("HX-Redirect", "/profesor/vehiculo"));
    }

    @Test
    @WithMockUser(username = "profesor17", roles = "PROFESOR")
    @DisplayName("POST /profesor/vehiculo/incidencia sin vehículo asignado devuelve error controlado")
    void testReportarIncidenciaProfesorSinVehiculo() throws Exception
    {
        // profesor17 (id=116 -> vehiculo_id=NULL) en HTMX y no-HTMX
        this.mockMvc.perform(post("/profesor/vehiculo/incidencia")
                        .with(csrf())
                        .header("HX-Request", "true")
                        .param("descripcion", "Descripción válida de prueba sin vehículo asignado"))
                .andExpect(status().isOk())
                .andExpect(view().name("profesor/fragments/modal-reportar-incidencia :: modal-reportar-incidencia"))
                .andExpect(model().attributeExists("errorReportarIncidencia"));

        this.mockMvc.perform(post("/profesor/vehiculo/incidencia")
                        .with(csrf())
                        .param("descripcion", "Descripción válida de prueba sin vehículo asignado"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profesor/vehiculo"))
                .andExpect(flash().attributeExists("error"));
    }
}
