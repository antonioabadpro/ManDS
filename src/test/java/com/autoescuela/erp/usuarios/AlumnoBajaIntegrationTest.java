package com.autoescuela.erp.usuarios;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import com.autoescuela.erp.core.enums.EstadoClase;
import com.autoescuela.erp.core.enums.EstadoUsuario;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.practicas.model.ClasePractica;
import com.autoescuela.erp.practicas.repository.ClasePracticaRepository;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.repository.AlumnoRepository;
import com.autoescuela.erp.usuarios.service.AlumnoService;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.autoescuela.erp.BaseIntegrationTest;

/**
 * Pruebas de integración para la baja lógica de alumnos (borrado suave),
 * desvinculación de profesor, cancelación de clases prácticas pendientes y notificaciones.
 */
class AlumnoBajaIntegrationTest extends BaseIntegrationTest
{
    @Autowired
    private AlumnoService alumnoService;

    @Autowired
    private AlumnoRepository alumnoRepository;

    @Autowired
    private ClasePracticaRepository clasePracticaRepository;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Cargar modal de baja de alumno como ADMIN devuelve fragmento con datos requeridos")
    void testCargarModalBajaAlumnoComoAdminExito() throws Exception
    {
        this.mockMvc.perform(get("/admin/alumnos/baja/201"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/modal-baja-alumno :: modal-baja-alumno"))
                .andExpect(model().attributeExists("alumno"))
                .andExpect(model().attributeExists("clasesPendientes"))
                .andExpect(model().attribute("abrirModalBajaAlumno", true))
                .andExpect(content().string(containsString("Baja Lógica del Alumno")))
                .andExpect(content().string(containsString("Confirmar Baja Definitiva")))
                .andExpect(content().string(containsString("modal-baja-alumno")))
                .andExpect(content().string(containsString("modal-confirmar-baja-alumno")));
    }

    @Test
    @DisplayName("Ejecutar baja lógica de alumno transiciona estado a INACTIVO, desvincula profesor y cancela clases")
    void testDarBajaAlumnoServicioExito()
    {
        // Alumno ID 201 tiene profesor asignado y clases prácticas
        Alumno alumno = this.alumnoRepository.findById(201L).orElseThrow();
        assertEquals(EstadoUsuario.ACTIVO, alumno.getEstado());
        assertNotNull(alumno.getProfesor());

        Alumno alumnoBaja = this.alumnoService.darBajaAlumno(201L);

        assertEquals(EstadoUsuario.INACTIVO, alumnoBaja.getEstado());
        assertNull(alumnoBaja.getProfesor());

        // Comprobamos en BD
        Alumno alumnoEnBD = this.alumnoRepository.findById(201L).orElseThrow();
        assertEquals(EstadoUsuario.INACTIVO, alumnoEnBD.getEstado());
        assertNull(alumnoEnBD.getProfesor());

        // Comprobamos que ninguna clase práctica queda en estado PENDIENTE
        List<ClasePractica> clases = this.clasePracticaRepository.findByAlumnoOrderByFechaHoraAsc(alumnoEnBD);
        for (ClasePractica clase : clases)
        {
            if (clase.getEstadoClase() != EstadoClase.RECIBIDA)
            {
                assertEquals(EstadoClase.CANCELADA, clase.getEstadoClase());
            }
        }
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Endpoint POST /admin/alumnos/baja/{id} redirige y registra flash attribute")
    void testDarBajaAlumnoPostControladorExito() throws Exception
    {
        this.mockMvc.perform(post("/admin/alumnos/baja/201")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/alumnos"))
                .andExpect(flash().attributeExists("mensajeExito"));

        Alumno alumnoActualizado = this.alumnoRepository.findById(201L).orElseThrow();
        assertEquals(EstadoUsuario.INACTIVO, alumnoActualizado.getEstado());
        assertNull(alumnoActualizado.getProfesor());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Petición HTMX POST /admin/alumnos/baja/{id} responde con cabecera HX-Redirect")
    void testDarBajaAlumnoHtmxRedireccion() throws Exception
    {
        this.mockMvc.perform(post("/admin/alumnos/baja/202")
                        .header("HX-Request", "true")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(header().string("HX-Redirect", "/admin/alumnos"));

        Alumno alumnoActualizado = this.alumnoRepository.findById(202L).orElseThrow();
        assertEquals(EstadoUsuario.INACTIVO, alumnoActualizado.getEstado());
        assertNull(alumnoActualizado.getProfesor());
    }

    @Test
    @DisplayName("Intentar dar de baja a un alumno ya INACTIVO lanza ReglaNegocioException")
    void testDarBajaAlumnoYaInactivoLanzaExcepcion()
    {
        Alumno alumno = this.alumnoRepository.findById(201L).orElseThrow();
        alumno.setEstado(EstadoUsuario.INACTIVO);
        this.alumnoRepository.save(alumno);

        assertThrows(ReglaNegocioException.class, () -> this.alumnoService.darBajaAlumno(201L));
    }

    @Test
    @WithMockUser(username = "alumno", roles = {"ALUMNO"})
    @DisplayName("Acceso no autorizado de rol no ADMIN a baja de alumno es denegado con 403")
    void testAccesoNoAutorizadoBajaAlumnoDenegado() throws Exception
    {
        this.mockMvc.perform(get("/admin/alumnos/baja/201"))
                .andExpect(status().isForbidden());

        this.mockMvc.perform(post("/admin/alumnos/baja/201").with(csrf()))
                .andExpect(status().isForbidden());
    }
}
