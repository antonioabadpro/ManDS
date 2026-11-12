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
 * Pruebas de integración para {@link VehiculoController}.
 * Verifica alta, edición, baja lógica, validaciones HTMX y vista de vehículo del profesor.
 */
class VehiculoControllerTest extends BaseIntegrationTest
{
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("POST /admin/flota/alta cubre error de validación y matrícula duplicada en HTMX y flujo tradicional")
    void testAltaVehiculoErroresHtmxYTradicional() throws Exception
    {
        this.mockMvc.perform(post("/admin/flota/alta")
                        .with(csrf())
                        .header("HX-Request", "true")
                        .param("matricula", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/fragments/modal-alta-vehiculo :: #modal-alta-vehiculo"))
                .andExpect(model().attributeExists("abrirModalAltaVehiculo"));

        // Matrícula ya existente en data.sql (1234-LMN) -> ReglaNegocioException en HTMX y no-HTMX
        this.mockMvc.perform(post("/admin/flota/alta")
                        .with(csrf())
                        .header("HX-Request", "true")
                        .param("matricula", "1234-LMN")
                        .param("marca", "SEAT")
                        .param("modelo", "Ibiza")
                        .param("color", "Blanco")
                        .param("km", "1000")
                        .param("cv", "95")
                        .param("anio", "2023")
                        .param("tipoCombustible", "GASOLINA")
                        .param("cajaCambios", "MANUAL")
                        .param("fechaUltimaRevision", "2026-01-10")
                        .param("fechaProximaRevision", "2027-01-10")
                        .param("tipoPermiso", "PERMISO_B"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/fragments/modal-alta-vehiculo :: #modal-alta-vehiculo"))
                .andExpect(model().attributeExists("errorAltaVehiculo"));

        this.mockMvc.perform(post("/admin/flota/alta")
                        .with(csrf())
                        .param("matricula", "1234-LMN")
                        .param("marca", "SEAT")
                        .param("modelo", "Ibiza")
                        .param("color", "Blanco")
                        .param("km", "1000")
                        .param("cv", "95")
                        .param("anio", "2023")
                        .param("tipoCombustible", "GASOLINA")
                        .param("cajaCambios", "MANUAL")
                        .param("fechaUltimaRevision", "2026-01-10")
                        .param("fechaProximaRevision", "2027-01-10")
                        .param("tipoPermiso", "PERMISO_B"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/flota"))
                .andExpect(model().attributeExists("errorAltaVehiculo"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("POST /admin/flota/validar-matricula valida obligatoria, formato, duplicidad y libre")
    void testValidarMatriculaHtmx() throws Exception
    {
        this.mockMvc.perform(post("/admin/flota/validar-matricula").with(csrf()).param("matricula", " "))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/registro :: mensaje-error"));

        this.mockMvc.perform(post("/admin/flota/validar-matricula").with(csrf()).param("matricula", "INVALIDA"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/registro :: mensaje-error"));

        this.mockMvc.perform(post("/admin/flota/validar-matricula").with(csrf()).param("matricula", "1234-LMN"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/registro :: mensaje-error"));

        this.mockMvc.perform(post("/admin/flota/validar-matricula").with(csrf()).param("matricula", "9876-XYZ"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/registro :: fragmento-vacio"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("Endpoints de edición, detalle y baja de vehículos cubren modales, HTMX y errores")
    void testEdicionDetalleYBajaVehiculo() throws Exception
    {
        // Modal editar y modal detalle
        this.mockMvc.perform(get("/admin/flota/editar/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/fragments/modal-editar-vehiculo :: modal-editar-vehiculo"))
                .andExpect(model().attributeExists("editarVehiculoDTO", "tieneProfesorAsignado"));

        this.mockMvc.perform(get("/admin/flota/detalle/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/fragments/modal-detalle-vehiculo :: modal-detalle-vehiculo"))
                .andExpect(model().attributeExists("vehiculo"));

        // Editar con error de validación (HTMX y no-HTMX)
        this.mockMvc.perform(post("/admin/flota/editar")
                        .with(csrf())
                        .header("HX-Request", "true")
                        .param("id", "5")
                        .param("matricula", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/fragments/modal-editar-vehiculo :: modal-editar-vehiculo"));

        this.mockMvc.perform(post("/admin/flota/editar")
                        .with(csrf())
                        .param("id", "5")
                        .param("matricula", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/flota"));

        // Editar con éxito vía HTMX (vehículo 5 DISPONIBLE sin profesor)
        this.mockMvc.perform(post("/admin/flota/editar")
                        .with(csrf())
                        .header("HX-Request", "true")
                        .param("id", "5")
                        .param("matricula", "5678-MNP")
                        .param("marca", "Volkswagen")
                        .param("modelo", "Polo 1.0 TSI")
                        .param("color", "Plata")
                        .param("km", "19000")
                        .param("cv", "95")
                        .param("anio", "2023")
                        .param("tipoCombustible", "GASOLINA")
                        .param("cajaCambios", "MANUAL")
                        .param("fechaUltimaRevision", "2026-03-12")
                        .param("fechaProximaRevision", "2027-03-12")
                        .param("tipoPermiso", "PERMISO_B"))
                .andExpect(status().isOk())
                .andExpect(header().string("HX-Redirect", "/admin/flota"));

        // Editar con ReglaNegocioException (matrícula duplicada de otro vehículo 1234-LMN sobre id 5)
        this.mockMvc.perform(post("/admin/flota/editar")
                        .with(csrf())
                        .header("HX-Request", "true")
                        .param("id", "5")
                        .param("matricula", "1234-LMN")
                        .param("marca", "Volkswagen")
                        .param("modelo", "Polo")
                        .param("color", "Plata")
                        .param("km", "19000")
                        .param("cv", "95")
                        .param("anio", "2023")
                        .param("tipoCombustible", "GASOLINA")
                        .param("cajaCambios", "MANUAL")
                        .param("fechaUltimaRevision", "2026-03-12")
                        .param("fechaProximaRevision", "2027-03-12")
                        .param("tipoPermiso", "PERMISO_B"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/fragments/modal-editar-vehiculo :: modal-editar-vehiculo"))
                .andExpect(model().attributeExists("errorEditarVehiculo"));

        this.mockMvc.perform(post("/admin/flota/editar")
                        .with(csrf())
                        .param("id", "5")
                        .param("matricula", "1234-LMN")
                        .param("marca", "Volkswagen")
                        .param("modelo", "Polo")
                        .param("color", "Plata")
                        .param("km", "19000")
                        .param("cv", "95")
                        .param("anio", "2023")
                        .param("tipoCombustible", "GASOLINA")
                        .param("cajaCambios", "MANUAL")
                        .param("fechaUltimaRevision", "2026-03-12")
                        .param("fechaProximaRevision", "2027-03-12")
                        .param("tipoPermiso", "PERMISO_B"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/flota"))
                .andExpect(model().attributeExists("errorEditarVehiculo"));

        // Baja vehículo OCUPADO (id=1) -> ReglaNegocioException en HTMX y no-HTMX
        this.mockMvc.perform(post("/admin/flota/baja/1")
                        .with(csrf())
                        .header("HX-Request", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/fragments/modal-baja-vehiculo :: modal-baja-vehiculo"))
                .andExpect(model().attributeExists("errorBajaVehiculo"));

        this.mockMvc.perform(post("/admin/flota/baja/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/flota"))
                .andExpect(model().attributeExists("errorBajaVehiculo"));

        // Baja vehículo DISPONIBLE (id=5) -> Éxito con redirección
        this.mockMvc.perform(post("/admin/flota/baja/5")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/flota"))
                .andExpect(flash().attributeExists("mensajeExito"));
    }

    @Test
    @WithMockUser(username = "profesor1", roles = "PROFESOR")
    @DisplayName("GET /profesor/vehiculo muestra la ficha del vehículo asignado al profesor")
    void testMostrarVehiculoProfesor() throws Exception
    {
        this.mockMvc.perform(get("/profesor/vehiculo"))
                .andExpect(status().isOk())
                .andExpect(view().name("profesor/vehiculo"))
                .andExpect(model().attributeExists("profesor", "vehiculo", "incidencias", "reportarIncidenciaDTO"));
    }
}
