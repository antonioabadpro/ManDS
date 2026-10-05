package com.autoescuela.erp.usuarios;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import com.autoescuela.erp.core.enums.EstadoClase;
import com.autoescuela.erp.core.enums.EstadoUsuario;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.practicas.model.ClasePractica;
import com.autoescuela.erp.practicas.repository.ClasePracticaRepository;
import com.autoescuela.erp.usuarios.dto.ProfesorResumenDTO;
import com.autoescuela.erp.usuarios.dto.ReasignarAlumnoDTO;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.repository.AlumnoRepository;
import com.autoescuela.erp.usuarios.service.AlumnoService;
import com.autoescuela.erp.usuarios.service.ProfesorService;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
 * Pruebas de integración para la reasignación de profesores a alumnos,
 * cancelación de clases prácticas pendientes y notificaciones por correo.
 */
class AlumnoReasignacionIntegrationTest extends BaseIntegrationTest
{
    @Autowired
    private AlumnoService alumnoService;

    @Autowired
    private AlumnoRepository alumnoRepository;

    @Autowired
    private ClasePracticaRepository clasePracticaRepository;

    @Autowired
    private ProfesorService profesorService;

    @Test
    @DisplayName("Reasignar alumno a otro profesor activo actualiza el profesor y cancela clases pendientes")
    void testReasignarProfesorConNuevoProfesorExito()
    {
        // Alumno ID 7 (Jose López) tiene asignado al profesor ID 2 (Laura Sánchez)
        Alumno alumno = this.alumnoRepository.findById(7L).orElseThrow();
        assertEquals(2L, alumno.getProfesor().getId());

        ReasignarAlumnoDTO dto = new ReasignarAlumnoDTO(7L, "REASIGNAR", 3L);
        this.alumnoService.reasignarProfesor(dto);

        // Verificamos que el profesor del alumno ahora es el profesor ID 3 (Manuel Navarro)
        Alumno alumnoActualizado = this.alumnoRepository.findById(7L).orElseThrow();
        assertNotNull(alumnoActualizado.getProfesor());
        assertEquals(3L, alumnoActualizado.getProfesor().getId());

        // Verificamos que todas las clases del alumno pasan a CANCELADA si estaban pendientes
        List<ClasePractica> clases = this.clasePracticaRepository.findByAlumnoOrderByFechaHoraAsc(alumnoActualizado);
        for (ClasePractica clase : clases)
        {
            if (clase.getEstadoClase() != EstadoClase.RECIBIDA)
            {
                assertEquals(EstadoClase.CANCELADA, clase.getEstadoClase());
            }
        }
    }

    @Test
    @DisplayName("Dejar alumno temporalmente sin profesor desasigna el docente y cancela clases pendientes")
    void testReasignarProfesorSinProfesorExito()
    {
        Alumno alumno = this.alumnoRepository.findById(7L).orElseThrow();
        assertNotNull(alumno.getProfesor());

        ReasignarAlumnoDTO dto = new ReasignarAlumnoDTO(7L, "SIN_PROFESOR", null);
        this.alumnoService.reasignarProfesor(dto);

        Alumno alumnoActualizado = this.alumnoRepository.findById(7L).orElseThrow();
        assertNull(alumnoActualizado.getProfesor());

        List<ClasePractica> clases = this.clasePracticaRepository.findByAlumnoOrderByFechaHoraAsc(alumnoActualizado);
        for (ClasePractica clase : clases)
        {
            if (clase.getEstadoClase() != EstadoClase.RECIBIDA)
            {
                assertEquals(EstadoClase.CANCELADA, clase.getEstadoClase());
            }
        }
    }

    @Test
    @DisplayName("Reasignar al mismo profesor lanza ReglaNegocioException")
    void testReasignarMismoProfesorLanzaExcepcion()
    {
        Alumno alumno = this.alumnoRepository.findById(7L).orElseThrow();
        Long profesorActualId = alumno.getProfesor().getId();

        ReasignarAlumnoDTO dto = new ReasignarAlumnoDTO(7L, "REASIGNAR", profesorActualId);
        assertThrows(ReglaNegocioException.class, () -> this.alumnoService.reasignarProfesor(dto));
    }

    @Test
    @DisplayName("Dejar sin profesor a un alumno que ya no tiene profesor lanza ReglaNegocioException")
    void testDejarSinProfesorYaSinProfesorLanzaExcepcion()
    {
        // Alumno ID 31 (Silvia Montesinos) ya se encuentra sin profesor asignado
        Alumno alumno = this.alumnoRepository.findById(31L).orElseThrow();
        assertNull(alumno.getProfesor());

        ReasignarAlumnoDTO dto = new ReasignarAlumnoDTO(31L, "SIN_PROFESOR", null);
        assertThrows(ReglaNegocioException.class, () -> this.alumnoService.reasignarProfesor(dto));
    }

    @Test
    @DisplayName("Reasignar profesor a un alumno inactivo lanza ReglaNegocioException")
    void testReasignarAlumnoInactivoLanzaExcepcion()
    {
        // Alumno ID 30 (Roberto Aguilar) está en estado INACTIVO
        Alumno alumno = this.alumnoRepository.findById(30L).orElseThrow();
        assertEquals(EstadoUsuario.INACTIVO, alumno.getEstado());

        ReasignarAlumnoDTO dto = new ReasignarAlumnoDTO(30L, "REASIGNAR", 3L);
        assertThrows(ReglaNegocioException.class, () -> this.alumnoService.reasignarProfesor(dto));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /admin/alumnos/reasignar/{id} renderiza modal HTMX con datos del alumno, permiso y aviso de clases")
    void testCargarModalReasignarAlumnoHtmx() throws Exception
    {
        this.mockMvc.perform(get("/admin/alumnos/reasignar/7"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/modal-reasignar-alumno :: modal-reasignar-alumno"))
                .andExpect(model().attributeExists("alumno"))
                .andExpect(model().attributeExists("clasesPendientes"))
                .andExpect(model().attributeExists("profesoresDisponibles"))
                .andExpect(content().string(containsString("Reasignar Profesor al Alumno")))
                .andExpect(content().string(containsString("Permiso matriculado:")))
                .andExpect(content().string(containsString("Consecuencias operativas de la reasignación")))
                .andExpect(content().string(containsString("modal-confirmar-reasignacion-alumno")));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /admin/alumnos/reasignar/{id} solo muestra profesores activos con el mismo carnet que el alumno")
    void testCargarModalReasignarAlumnoFiltraPorCarnetDelAlumno() throws Exception
    {
        // Alumno ID 12 está matriculado en PERMISO_A2 y asignado al profesor ID 5.
        // Entre los profesores activos solo ID 2 e ID 5 tienen PERMISO_A2, por lo que solo debe listarse la ID 2 (Laura Sánchez).
        this.mockMvc.perform(get("/admin/alumnos/reasignar/12"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/modal-reasignar-alumno :: modal-reasignar-alumno"))
                .andExpect(model().attribute("profesoresDisponibles", hasSize(1)))
                .andExpect(content().string(containsString("Laura Sánchez")))
                .andExpect(content().string(containsString("Permiso A2")));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /admin/alumnos/reasignar/{id} muestra aviso si no hay otros profesores disponibles con ese carnet")
    void testCargarModalReasignarAlumnoSinProfesoresDisponiblesMuestraAvisoContextual() throws Exception
    {
        // Alumno ID 16 está matriculado en PERMISO_C y asignado al profesor ID 3 (el único con PERMISO_C).
        // Al excluir al profesor actual, la lista de disponibles debe ser vacía y mostrar el mensaje contextual.
        this.mockMvc.perform(get("/admin/alumnos/reasignar/16"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/modal-reasignar-alumno :: modal-reasignar-alumno"))
                .andExpect(model().attribute("profesoresDisponibles", hasSize(0)))
                .andExpect(content().string(containsString("No hay otros profesores activos habilitados para Permiso C")));
    }

    @Test
    @DisplayName("Reasignar alumno a profesor sin el carnet de su matrícula lanza ReglaNegocioException")
    void testReasignarProfesorSinCarnetCompatibleLanzaExcepcion()
    {
        // Alumno ID 16 está matriculada en PERMISO_C. El profesor ID 4 (Carlos Martínez) solo tiene PERMISO_B y PERMISO_B_E
        ReasignarAlumnoDTO dto = new ReasignarAlumnoDTO(16L, "REASIGNAR", 4L);
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> this.alumnoService.reasignarProfesor(dto));
        assertTrue(ex.getMessage().contains("no dispone del carnet"));
    }

    @Test
    @DisplayName("Los profesores disponibles para reasignación se ordenan de menor a mayor número de alumnos")
    void testProfesoresOrdenadosDeMenorAMayorNumeroDeAlumnos()
    {
        List<ProfesorResumenDTO> profesores = this.profesorService.obtenerProfesoresActivosPorCarnetExcluyendo(TipoCarnet.PERMISO_B, 2L);
        assertTrue(profesores.size() > 1);

        for (int i = 0; i < profesores.size() - 1; i++)
        {
            assertTrue(profesores.get(i).totalAlumnos() <= profesores.get(i + 1).totalAlumnos(),
                    "El profesor en la posición " + i + " tiene más alumnos que el siguiente");
        }
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("POST /admin/alumnos/reasignar redirige con éxito y mensaje flash")
    void testPostReasignarProfesorRedireccionExito() throws Exception
    {
        this.mockMvc.perform(post("/admin/alumnos/reasignar")
                .with(csrf())
                .param("alumnoId", "7")
                .param("opcion", "REASIGNAR")
                .param("nuevoProfesorId", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/alumnos"))
                .andExpect(flash().attributeExists("mensajeExito"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("POST /admin/alumnos/reasignar con HTMX devuelve cabecera HX-Redirect")
    void testPostReasignarProfesorHtmx() throws Exception
    {
        this.mockMvc.perform(post("/admin/alumnos/reasignar")
                .with(csrf())
                .header("HX-Request", "true")
                .param("alumnoId", "7")
                .param("opcion", "SIN_PROFESOR"))
                .andExpect(status().isOk())
                .andExpect(header().string("HX-Redirect", "/admin/alumnos"));
    }
}
