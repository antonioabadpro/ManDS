package com.autoescuela.erp.usuarios;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.autoescuela.erp.core.enums.EstadoClase;
import com.autoescuela.erp.core.enums.EstadoUsuario;
import com.autoescuela.erp.core.enums.EstadoVehiculo;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.flota.model.Vehiculo;
import com.autoescuela.erp.flota.repository.VehiculoRepository;
import com.autoescuela.erp.practicas.model.ClasePractica;
import com.autoescuela.erp.practicas.repository.ClasePracticaRepository;
import com.autoescuela.erp.usuarios.dto.BajaProfesorDTO;
import com.autoescuela.erp.usuarios.dto.EditarProfesorDTO;
import com.autoescuela.erp.usuarios.dto.ProfesorResumenDTO;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.model.Profesor;
import com.autoescuela.erp.usuarios.repository.AlumnoRepository;
import com.autoescuela.erp.usuarios.repository.ProfesorRepository;
import com.autoescuela.erp.usuarios.service.ProfesorService;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@TestPropertySource(properties =
{
        "spring.sql.init.mode=always",
        "spring.sql.init.data-locations=classpath:data.sql",
        "spring.jpa.defer-datasource-initialization=true",
        "spring.mail.host=localhost",
        "spring.mail.port=1025"
})
@Transactional
/**
 * Pruebas de integración para la baja lógica y reactivación de profesores (Regla 7.3).
 */
class ProfesorBajaReactivacionIntegrationTest
{
    @Autowired
    private WebApplicationContext contexto;

    @Autowired
    private ProfesorService profesorService;

    @Autowired
    private ProfesorRepository profesorRepository;

    @Autowired
    private AlumnoRepository alumnoRepository;

    @Autowired
    private VehiculoRepository vehiculoRepository;

    @Autowired
    private ClasePracticaRepository clasePracticaRepository;

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
    @DisplayName("Baja lógica con reasignación de alumnos libera el vehículo, cancela clases pendientes y reasigna los alumnos")
    void testBajaProfesorConReasignacionAlumnosExito()
    {
        // Profesor 1 (Laura Sánchez, ID: 2) tiene vehículo 1 ('1234-LMN'), 6 alumnos y clases pendientes
        Profesor profesorOriginal = this.profesorRepository.findById(2L).orElseThrow();
        assertEquals(EstadoUsuario.ACTIVO, profesorOriginal.getEstado());
        assertEquals(1L, profesorOriginal.getVehiculo().getId());

        List<Alumno> alumnosPrevios = this.alumnoRepository.findByProfesor(profesorOriginal);
        assertTrue(alumnosPrevios.size() > 0);

        BajaProfesorDTO dto = new BajaProfesorDTO(2L, "REASIGNAR", 3L);
        Profesor profesorBaja = this.profesorService.darBajaProfesor(dto);

        // Verificaciones del profesor dado de baja
        assertEquals(EstadoUsuario.INACTIVO, profesorBaja.getEstado());
        assertNull(profesorBaja.getVehiculo());

        // Verificaciones del vehículo liberado
        Vehiculo vehiculoLiberado = this.vehiculoRepository.findById(1L).orElseThrow();
        assertEquals(EstadoVehiculo.DISPONIBLE, vehiculoLiberado.getEstado());
        assertNull(vehiculoLiberado.getProfesor());

        // Verificaciones de clases pendientes canceladas
        List<ClasePractica> clasesProfesor = this.clasePracticaRepository.findByProfesorIdOrderByFechaHoraAsc(2L);
        for (ClasePractica clase : clasesProfesor)
        {
            if (clase.getEstadoClase() != EstadoClase.RECIBIDA)
            {
                assertEquals(EstadoClase.CANCELADA, clase.getEstadoClase());
            }
        }

        // Verificaciones de alumnos reasignados al nuevo docente (ID: 3)
        Profesor profesorDestino = this.profesorRepository.findById(3L).orElseThrow();
        for (Alumno alumno : alumnosPrevios)
        {
            Alumno alumnoActualizado = this.alumnoRepository.findById(alumno.getId()).orElseThrow();
            assertEquals(profesorDestino.getId(), alumnoActualizado.getProfesor().getId());
        }
    }

    @Test
    @DisplayName("Baja lógica con opción SIN_PROFESOR desvincula a los alumnos")
    void testBajaProfesorSinProfesorParaAlumnosExito()
    {
        Profesor profesorOriginal = this.profesorRepository.findById(2L).orElseThrow();
        List<Alumno> alumnosPrevios = this.alumnoRepository.findByProfesor(profesorOriginal);

        BajaProfesorDTO dto = new BajaProfesorDTO(2L, "SIN_PROFESOR", null);
        Profesor profesorBaja = this.profesorService.darBajaProfesor(dto);

        assertEquals(EstadoUsuario.INACTIVO, profesorBaja.getEstado());
        assertNull(profesorBaja.getVehiculo());

        for (Alumno alumno : alumnosPrevios)
        {
            Alumno alumnoActualizado = this.alumnoRepository.findById(alumno.getId()).orElseThrow();
            assertNull(alumnoActualizado.getProfesor());
        }
    }

    @Test
    @DisplayName("Reactivar profesor inactivo lo pasa a ACTIVO y garantiza vehiculo = null")
    void testReactivarProfesorInactivoExito()
    {
        // Primero damos de baja al profesor 2
        this.profesorService.darBajaProfesor(new BajaProfesorDTO(2L, "SIN_PROFESOR", null));
        Profesor profesorInactivo = this.profesorRepository.findById(2L).orElseThrow();
        assertEquals(EstadoUsuario.INACTIVO, profesorInactivo.getEstado());

        // Reactivación
        Profesor profesorReactivado = this.profesorService.reactivarProfesor(2L);
        assertEquals(EstadoUsuario.ACTIVO, profesorReactivado.getEstado());
        assertNull(profesorReactivado.getVehiculo());
    }

    @Test
    @DisplayName("Intentar editar a un profesor inactivo lanza ReglaNegocioException")
    void testEditarProfesorInactivoFalla()
    {
        this.profesorService.darBajaProfesor(new BajaProfesorDTO(2L, "SIN_PROFESOR", null));

        // Intento de consulta para edición
        assertThrows(ReglaNegocioException.class, () -> this.profesorService.obtenerProfesorParaEdicion(2L));

        // Intento de modificación
        EditarProfesorDTO dtoMod = new EditarProfesorDTO(
                2L, "Laura", "Sánchez Romero", "23456789B",
                "600222333", "laura.profesor@autoescuela.es",
                java.time.LocalDate.of(1988, 9, 23), "Calle Test",
                java.time.LocalDate.now(),
                com.autoescuela.erp.core.enums.TipoTurno.MATINAL, null,
                List.of(com.autoescuela.erp.core.enums.TipoCarnet.PERMISO_B)
        );
        assertThrows(ReglaNegocioException.class, () -> this.profesorService.modificarProfesor(dtoMod));
    }

    @Test
    @DisplayName("Intentar reactivar a un profesor ya activo lanza ReglaNegocioException")
    void testReactivarProfesorActivoFalla()
    {
        assertThrows(ReglaNegocioException.class, () -> this.profesorService.reactivarProfesor(2L));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /admin/profesores/baja/{id} renderiza modal HTMX con datos del profesor y lista de docentes")
    void testCargarModalBajaProfesorHtmx() throws Exception
    {
        this.mockMvc.perform(get("/admin/profesores/baja/2"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/modal-baja-profesor :: modal-baja-profesor"))
                .andExpect(model().attributeExists("profesor"))
                .andExpect(model().attributeExists("profesoresDisponibles"))
                .andExpect(content().string(containsString("Baja Lógica del Profesor")))
                .andExpect(content().string(containsString("Laura Sánchez Romero")))
                .andExpect(content().string(containsString("1234-LMN")))
                .andExpect(content().string(containsString("id=\"modal-baja-profesor\"")))
                .andExpect(content().string(containsString("id=\"modal-confirmar-baja-profesor\"")))
                .andExpect(content().string(containsString("Confirmar Baja Definitiva")))
                .andExpect(content().string(containsString("id=\"form-baja-profesor\"")))
                .andExpect(content().string(containsString("id=\"btn-volver-modal-baja\"")));
    }

    @Test
    @DisplayName("Los profesores disponibles para reasignación en baja lógica se ordenan de menor a mayor número de alumnos")
    void testProfesoresParaBajaOrdenadosDeMenorAMayorNumeroDeAlumnos()
    {
        List<ProfesorResumenDTO> profesores = this.profesorService.obtenerProfesoresActivosExcluyendo(2L);
        assertTrue(profesores.size() > 1);

        for (int i = 0; i < profesores.size() - 1; i++)
        {
            assertTrue(profesores.get(i).totalAlumnos() <= profesores.get(i + 1).totalAlumnos(),
                    "El profesor en la posición " + i + " tiene más alumnos que el siguiente");
        }
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /admin/profesores/reactivar/{id} renderiza modal HTMX advirtiendo alta sin vehículo")
    void testCargarModalReactivarProfesorHtmx() throws Exception
    {
        this.mockMvc.perform(get("/admin/profesores/reactivar/2"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/modal-reactivar-profesor :: modal-reactivar-profesor"))
                .andExpect(model().attributeExists("profesor"))
                .andExpect(content().string(containsString("Reactivar Profesor")))
                .andExpect(content().string(containsString("sin ningún vehículo asignado")));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("POST /admin/profesores/baja redirige con éxito y mensaje flash")
    void testPostBajaProfesorRedireccionExito() throws Exception
    {
        this.mockMvc.perform(post("/admin/profesores/baja")
                .with(csrf())
                .param("profesorId", "2")
                .param("opcionAlumnos", "REASIGNAR")
                .param("nuevoProfesorId", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/profesores"))
                .andExpect(flash().attributeExists("mensajeExito"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("POST /admin/profesores/reactivar/{id} con HTMX responde cabecera HX-Redirect")
    void testPostReactivarProfesorHtmx() throws Exception
    {
        // Dejamos inactivo previamente al docente 2
        this.profesorService.darBajaProfesor(new BajaProfesorDTO(2L, "SIN_PROFESOR", null));

        this.mockMvc.perform(post("/admin/profesores/reactivar/2")
                .with(csrf())
                .header("HX-Request", "true"))
                .andExpect(status().isOk())
                .andExpect(header().string("HX-Redirect", "/admin/profesores"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /admin/profesores/oculta botón editar y papelera en inactivos, mostrando botón de reactivar")
    void testRenderizadoCondicionalBotonesSegunEstado() throws Exception
    {
        // Dejamos inactivo al profesor 2
        this.profesorService.darBajaProfesor(new BajaProfesorDTO(2L, "SIN_PROFESOR", null));

        this.mockMvc.perform(get("/admin/profesores"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("INACTIVO")))
                .andExpect(content().string(containsString("Reactivar profesor (Dar de alta sin vehículo)")));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /admin/profesores/baja/{id} solo muestra profesores que disponen del permiso del vehículo asignado")
    void testModalBajaProfesorFiltraPorPermisoVehiculo() throws Exception
    {
        // Asignamos al profesor 2 un vehículo de PERMISO_C (vehículo 4 en data.sql)
        Profesor profesor = this.profesorRepository.findById(2L).orElseThrow();
        Vehiculo vehiculoC = this.vehiculoRepository.findById(4L).orElseThrow();
        vehiculoC.setProfesor(profesor);
        profesor.setVehiculo(vehiculoC);
        this.profesorRepository.save(profesor);

        MvcResult result = this.mockMvc.perform(get("/admin/profesores/baja/2"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("profesoresDisponibles"))
                .andReturn();

        @SuppressWarnings("unchecked")
        List<ProfesorResumenDTO> disponibles = (List<ProfesorResumenDTO>) result.getModelAndView().getModel().get("profesoresDisponibles");

        // En data.sql, de los profesores activos excluyendo al 2 (3, 7, 8, 12), únicamente el 3 tiene PERMISO_C
        assertEquals(1, disponibles.size());
        assertEquals(3L, disponibles.get(0).id());
    }

    @Test
    @DisplayName("Baja lógica con reasignación a un profesor que no tiene el permiso del vehículo lanza ReglaNegocioException")
    void testBajaProfesorReasignacionSinPermisoFalla()
    {
        // Asignamos al profesor 2 un vehículo de PERMISO_C
        Profesor profesor = this.profesorRepository.findById(2L).orElseThrow();
        Vehiculo vehiculoC = this.vehiculoRepository.findById(4L).orElseThrow();
        vehiculoC.setProfesor(profesor);
        profesor.setVehiculo(vehiculoC);
        this.profesorRepository.save(profesor);

        // Profesor 7 (Carlos Martínez) solo tiene PERMISO_B y PERMISO_B_E, NO tiene PERMISO_C
        BajaProfesorDTO dto = new BajaProfesorDTO(2L, "REASIGNAR", 7L);
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> this.profesorService.darBajaProfesor(dto));
        assertTrue(ex.getMessage().contains("no dispone del carnet"));
    }

    @Test
    @DisplayName("Modificar profesor intentando alterar fechaContratacion lanza ReglaNegocioException")
    void testModificarProfesorAlterandoFechaContratacionFalla()
    {
        Profesor profesor = this.profesorRepository.findById(2L).orElseThrow();
        EditarProfesorDTO dtoMod = new EditarProfesorDTO(
                2L, profesor.getNombre(), profesor.getApellidos(), profesor.getDni(),
                profesor.getTelefono(), profesor.getCorreo(),
                profesor.getFechaNacimiento(), profesor.getDireccion(),
                profesor.getFechaContratacion().plusDays(5),
                profesor.getTurno(), null,
                profesor.getListaTiposCarnet()
        );

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> this.profesorService.modificarProfesor(dtoMod));
        assertEquals("La fecha de contratación no puede ser modificada.", ex.getMessage());
    }

    @Test
    @DisplayName("Modificar profesor con la misma fechaContratacion actualiza con éxito los demás campos")
    void testModificarProfesorConFechaContratacionInmutableExito()
    {
        Profesor profesor = this.profesorRepository.findById(2L).orElseThrow();
        EditarProfesorDTO dtoMod = new EditarProfesorDTO(
                2L, "Laura Modificada", profesor.getApellidos(), profesor.getDni(),
                profesor.getTelefono(), profesor.getCorreo(),
                profesor.getFechaNacimiento(), "Calle Inmutable 42",
                profesor.getFechaContratacion(),
                profesor.getTurno(), null,
                profesor.getListaTiposCarnet()
        );

        Profesor modificado = this.profesorService.modificarProfesor(dtoMod);
        assertEquals("Laura Modificada", modificado.getNombre());
        assertEquals("Calle Inmutable 42", modificado.getDireccion());
        assertEquals(profesor.getFechaContratacion(), modificado.getFechaContratacion());
    }
}
