package com.autoescuela.erp.examenes.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import com.autoescuela.erp.BaseIntegrationTest;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Pruebas de integración para el panel DGT, convocatorias, cupos, resolución de solicitudes y calificaciones en ExamenController.
 */
class ExamenControllerTest extends BaseIntegrationTest
{
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /admin/examenes, /buscar, /jornadas y modales HTMX de detalle/edición cubren todos sus flujos")
    void testVistasYModalesAdminExamenes() throws Exception
    {
        // Acceso con parámetro ?mes= redirige con flash manteniendo URL limpia
        this.mockMvc.perform(get("/admin/examenes").param("mes", "2026-10-01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/examenes"))
                .andExpect(flash().attributeExists("mes"));

        this.mockMvc.perform(get("/admin/examenes"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/examenes"))
                .andExpect(model().attributeExists("solicitudes", "totalSolicitudes", "tiposCarnet", "tiposExamen"));

        this.mockMvc.perform(get("/admin/examenes/buscar")
                        .param("terminoBusqueda", "a")
                        .param("estado", "PENDIENTE"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/fragments/tabla-solicitudes-admin :: tablaSolicitudesFragment"))
                .andExpect(model().attribute("esBusquedaHtmx", true));

        this.mockMvc.perform(get("/admin/examenes/jornadas").param("mes", "2026-10-01"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/fragments/modal-fechas-cupos-dgt :: modal-fechas-cupos-dgt"))
                .andExpect(model().attributeExists("fechasMes", "cuposMes"));

        // Modales detalle y edición: 404 y carga válida (teórica 46 y práctica 47)
        this.mockMvc.perform(get("/admin/examenes/99999/detalle-modal"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"));

        this.mockMvc.perform(get("/admin/examenes/46/detalle-modal"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/fragments/modal-detalle-solicitud-admin :: modalDetalleSolicitudAdmin"))
                .andExpect(model().attributeExists("solicitud"));

        this.mockMvc.perform(get("/admin/examenes/47/detalle-modal"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("solicitud", "vehiculo"));

        this.mockMvc.perform(get("/admin/examenes/99999/editar-modal"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"))
                .andExpect(model().attributeExists("error"));

        this.mockMvc.perform(get("/admin/examenes/47/editar-modal").param("mes", "2027-02-01"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/fragments/modal-editar-solicitud-examen :: modalEditarSolicitud"))
                .andExpect(model().attributeExists("diagnostico", "solicitud", "mesSeleccionado"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("POST fijar fechas y fijar cupos DGT validan errores de formato, inmutabilidad y registro exitoso")
    void testFijarFechasYCuposDgt() throws Exception
    {
        // Fijar fechas con error de validación (sin fechaHora2)
        this.mockMvc.perform(post("/admin/examenes/fijar-fechas-examen")
                        .with(csrf())
                        .param("mes", "2027-02-01")
                        .param("fechaHora1", "2027-02-04T09:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/examenes"))
                .andExpect(flash().attributeExists("error"));

        // Mes ya fijado en data.sql (2026-10) -> ReglaNegocioException
        this.mockMvc.perform(post("/admin/examenes/fijar-fechas-examen")
                        .with(csrf())
                        .param("mes", "2026-10-01")
                        .param("fechaHora1", "2026-10-15T09:00")
                        .param("fechaHora2", "2026-10-29T09:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/examenes"))
                .andExpect(flash().attributeExists("error"));

        // Mes futuro limpio (febrero 2027, miércoles 3 y jueves 18) -> éxito
        LocalDate mesLimpio = LocalDate.of(2027, 2, 1);
        LocalDate fecha1 = mesLimpio.withDayOfMonth(3);
        while (fecha1.getDayOfWeek() == DayOfWeek.SATURDAY || fecha1.getDayOfWeek() == DayOfWeek.SUNDAY)
        {
            fecha1 = fecha1.plusDays(1);
        }
        LocalDate fecha2 = fecha1.plusDays(14);
        while (fecha2.getDayOfWeek() == DayOfWeek.SATURDAY || fecha2.getDayOfWeek() == DayOfWeek.SUNDAY)
        {
            fecha2 = fecha2.plusDays(1);
        }

        this.mockMvc.perform(post("/admin/examenes/fijar-fechas-examen")
                        .with(csrf())
                        .param("mes", mesLimpio.toString())
                        .param("fechaHora1", LocalDateTime.of(fecha1.getYear(), fecha1.getMonth(), fecha1.getDayOfMonth(), 9, 0).toString())
                        .param("fechaHora2", LocalDateTime.of(fecha2.getYear(), fecha2.getMonth(), fecha2.getDayOfMonth(), 9, 0).toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/examenes"))
                .andExpect(flash().attributeExists("mensajeExito"));

        // Cupos con formato numérico inválido -> captura Exception genérica
        this.mockMvc.perform(post("/admin/examenes/fijar-cupos-examen")
                        .with(csrf())
                        .param("mesCupos", "2027-02")
                        .param("cupoTeorico_PERMISO_B", "no-numero"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/examenes"))
                .andExpect(flash().attributeExists("error"));

        // Cupos en formato YYYY-MM válido
        this.mockMvc.perform(post("/admin/examenes/fijar-cupos-examen")
                        .with(csrf())
                        .param("mesCupos", "2027-02")
                        .param("cupoTeorico_PERMISO_B", "10")
                        .param("cupoPractico_PERMISO_B", "8"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/examenes"))
                .andExpect(flash().attributeExists("mensajeExito"));

        // Repetir cupos en mes ya fijado -> ReglaNegocioException
        this.mockMvc.perform(post("/admin/examenes/fijar-cupos-examen")
                        .with(csrf())
                        .param("mesCupos", "2027-02-01")
                        .param("cupoTeorico_PERMISO_B", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/examenes"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("POST aceptar/rechazar solicitudes y calificar teóricos admin cubren errores y éxitos")
    void testAceptarRechazarYCalificarAdmin() throws Exception
    {
        // Aceptar inexistente -> error
        this.mockMvc.perform(post("/admin/examenes/99999/aceptar")
                        .with(csrf())
                        .param("numeroConvocatoria", "1")
                        .param("mes", "2026-11-01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/examenes"))
                .andExpect(flash().attributeExists("error"));

        // Fijar mes futuro 2027-03 con fechas y cupos para aceptar solicitud 46 (TEORICO PERMISO_B)
        LocalDate marzo = LocalDate.of(2027, 3, 1);
        this.mockMvc.perform(post("/admin/examenes/fijar-fechas-examen")
                        .with(csrf())
                        .param("mes", marzo.toString())
                        .param("fechaHora1", "2027-03-04T09:00")
                        .param("fechaHora2", "2027-03-18T09:00"))
                .andExpect(status().is3xxRedirection());

        this.mockMvc.perform(post("/admin/examenes/fijar-cupos-examen")
                        .with(csrf())
                        .param("mesCupos", "2027-03")
                        .param("cupoTeorico_PERMISO_B", "10")
                        .param("cupoPractico_PERMISO_B", "10"))
                .andExpect(status().is3xxRedirection());

        this.mockMvc.perform(post("/admin/examenes/46/aceptar")
                        .with(csrf())
                        .param("numeroConvocatoria", "1")
                        .param("mes", "2027-03-01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/examenes"))
                .andExpect(flash().attributeExists("mensajeExito"));

        // Rechazar sin justificación -> error; con justificación (solicitud 48) -> mensajeExito
        this.mockMvc.perform(post("/admin/examenes/48/rechazar")
                        .with(csrf())
                        .param("motivoRechazo", " "))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/examenes"))
                .andExpect(flash().attributeExists("error"));

        this.mockMvc.perform(post("/admin/examenes/48/rechazar")
                        .with(csrf())
                        .param("motivoRechazo", "Falta superar simulacros teóricos previos."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/examenes"))
                .andExpect(flash().attributeExists("mensajeExito"));

        // Calificaciones teóricas Admin
        this.mockMvc.perform(get("/admin/calificaciones"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/calificaciones"))
                .andExpect(model().attributeExists("examenes", "calificarExamenDTO"));

        // Error de validación (sin esApto)
        this.mockMvc.perform(post("/admin/calificaciones/calificar")
                        .with(csrf())
                        .param("examenId", "15"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/calificaciones"))
                .andExpect(flash().attributeExists("error"));

        // Intento de calificar un examen PRACTICO (id=3) desde /admin/calificaciones
        this.mockMvc.perform(post("/admin/calificaciones/calificar")
                        .with(csrf())
                        .param("examenId", "3")
                        .param("esApto", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/calificaciones"))
                .andExpect(flash().attributeExists("error"));

        // Calificación válida del examen teórico 15 (esApto=NULL en fecha pasada)
        this.mockMvc.perform(post("/admin/calificaciones/calificar")
                        .with(csrf())
                        .param("examenId", "15")
                        .param("esApto", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/calificaciones"))
                .andExpect(flash().attributeExists("mensajeExito"));

        // Recalificar examen ya calificado (id=1) -> ReglaNegocioException
        this.mockMvc.perform(post("/admin/calificaciones/calificar")
                        .with(csrf())
                        .param("examenId", "1")
                        .param("esApto", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/calificaciones"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @WithMockUser(username = "profesor1", roles = "PROFESOR")
    @DisplayName("GET y POST de exámenes prácticos para profesor cubren modales 404/válido, restricciones y calificación")
    void testExamenesYCalificacionProfesor() throws Exception
    {
        this.mockMvc.perform(get("/profesor/examenes"))
                .andExpect(status().isOk())
                .andExpect(view().name("profesor/examenes"))
                .andExpect(model().attributeExists("profesor", "examenes", "calificarExamenDTO"));

        this.mockMvc.perform(get("/profesor/examenes/99999/modal"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"));

        this.mockMvc.perform(get("/profesor/examenes/3/modal"))
                .andExpect(status().isOk())
                .andExpect(view().name("profesor/fragments/modal-detalle-examen :: modalDetalleExamen"))
                .andExpect(model().attributeExists("examen", "alumnosCitados"));

        // Error de validación
        this.mockMvc.perform(post("/profesor/examenes/calificar")
                        .with(csrf())
                        .param("examenId", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profesor/examenes"))
                .andExpect(flash().attributeExists("error"));

        // Intento de calificar examen TEORICO (id=15) por profesor
        this.mockMvc.perform(post("/profesor/examenes/calificar")
                        .with(csrf())
                        .param("examenId", "15")
                        .param("esApto", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profesor/examenes"))
                .andExpect(flash().attributeExists("error"));

        // Calificación válida de práctico (id=3)
        this.mockMvc.perform(post("/profesor/examenes/calificar")
                        .with(csrf())
                        .param("examenId", "3")
                        .param("esApto", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profesor/examenes"))
                .andExpect(flash().attributeExists("mensajeExito"));

        // Recalificar examen ya calificado -> ReglaNegocioException
        this.mockMvc.perform(post("/profesor/examenes/calificar")
                        .with(csrf())
                        .param("examenId", "3")
                        .param("esApto", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profesor/examenes"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @WithMockUser(username = "alumno9", roles = "ALUMNO")
    @DisplayName("GET /alumno/examenes y /alumno/notas renderizan expedientes históricos y notas")
    void testVistasAlumnoExamenesYNotas() throws Exception
    {
        this.mockMvc.perform(get("/alumno/examenes"))
                .andExpect(status().isOk())
                .andExpect(view().name("alumno/examenes"))
                .andExpect(model().attributeExists("alumno", "solicitudes"));

        this.mockMvc.perform(get("/alumno/notas"))
                .andExpect(status().isOk())
                .andExpect(view().name("alumno/notas"))
                .andExpect(model().attributeExists("examenes", "totalExamenes", "examenesAprobados", "examenesSuspensos"));
    }
}
