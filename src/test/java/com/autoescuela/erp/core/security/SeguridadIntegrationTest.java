package com.autoescuela.erp.core.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.mock.web.MockHttpSession;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Assertions;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.autoescuela.erp.BaseIntegrationTest;

class SeguridadIntegrationTest extends BaseIntegrationTest
{

    @Test
    @DisplayName("Login exitoso de Administrador con nombre de usuario -> Redirige a /admin/dashboard")
    void testLoginAdminConUsername() throws Exception
    {
        this.mockMvc.perform(post("/login")
                .param("username", "admin")
                .param("password", "admin123")
                .with(csrf())) // Inyectamos el token CSRF para simular un formulario legítimo
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"));
    }

    @Test
    @DisplayName("Login exitoso de Profesor con correo electrónico -> Redirige a /profesor/dashboard")
    void testLoginProfesorConCorreo() throws Exception
    {
        this.mockMvc.perform(post("/login")
                .param("username", "laura.profesor@autoescuela.es")
                .param("password", "profesor123")
                .with(csrf())) // Inyectamos el token CSRF para simular un formulario legítimo
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profesor/dashboard"));
    }

    @Test
    @DisplayName("Login exitoso de Alumno con correo electrónico -> Redirige a /alumno/dashboard")
    void testLoginAlumnoConCorreo() throws Exception
    {
        this.mockMvc.perform(post("/login")
                .param("username", "jose.alumno@autoescuela.es")
                .param("password", "alumno123")
                .with(csrf())) // Inyectamos el token CSRF para simular un formulario legítimo
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/alumno/dashboard"));
    }

    @Test
    @DisplayName("Login fallido con contraseña incorrecta -> Redirige a /login?error=true")
    void testLoginCredencialesIncorrectas() throws Exception
    {
        this.mockMvc.perform(post("/login")
                .param("username", "admin")
                .param("password", "password_incorrecto")
                .with(csrf())) // Inyectamos el token CSRF para simular un formulario legítimo
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"));
    }

    @Test
    @DisplayName("Acceso no autenticado a ruta protegida -> Redirige a /login (302)")
    void testAccesoNoAutenticadoRedirigeALogin() throws Exception
    {
        this.mockMvc.perform(get("/admin/usuarios"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("Petición HTMX no autenticada -> Responde con HTTP 401 y cabecera HX-Redirect")
    void testPeticionHtmxNoAutenticadaDevuelveHxRedirect() throws Exception
    {
        this.mockMvc.perform(get("/alumno/reservar")
                .header("HX-Request", "true"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("HX-Redirect", "/login?sesionExpirada=true"));
    }

    @Test
    @WithMockUser(roles = "ALUMNO")

    @DisplayName("Alumno intentando acceder a /admin/dashboard -> HTTP 403 Forbidden y no ve el panel de administración")
    void testAlumnoAccediendoAAdminForbiddenYNoVeContenido() throws Exception
    {
        this.mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isForbidden())
                .andExpect(content().string(not(containsString("Panel de Control del Administrador"))));
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    @DisplayName("Alumno intentando acceder a /profesor/dashboard -> HTTP 403 Forbidden y no ve el panel del profesor")
    void testAlumnoAccediendoAProfesorForbiddenYNoVeContenido() throws Exception
    {
        this.mockMvc.perform(get("/profesor/dashboard"))
                .andExpect(status().isForbidden())
                .andExpect(content().string(not(containsString("Panel de Control del Profesor"))));
    }

    @Test
    @WithMockUser(roles = "PROFESOR")

    @DisplayName("Profesor intentando acceder a /admin/dashboard -> HTTP 403 Forbidden y no ve el panel de administración")
    void testProfesorAccediendoAAdminForbiddenYNoVeContenido() throws Exception
    {
        this.mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isForbidden())
                .andExpect(content().string(not(containsString("Panel de Control del Administrador"))));
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    @DisplayName("Profesor intentando acceder a /alumno/dashboard -> HTTP 403 Forbidden y no ve el panel del alumno")
    void testProfesorAccediendoAAlumnoForbiddenYNoVeContenido() throws Exception
    {
        this.mockMvc.perform(get("/alumno/dashboard"))
                .andExpect(status().isForbidden())
                .andExpect(content().string(not(containsString("Panel del Alumno"))));
    }

    @Test
    @DisplayName("Usuario no autenticado intentando acceder a /admin/dashboard -> Redirige a /login (302) y no ve nada")
    void testNoAutenticadoAccediendoAAdminRedirigeYNoVeContenido() throws Exception
    {
        this.mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(content().string(not(containsString("Panel de Control del Administrador"))));
    }

    @Test
    @DisplayName("Usuario no autenticado intentando acceder a /profesor/dashboard -> Redirige a /login (302) y no ve nada")
    void testNoAutenticadoAccediendoAProfesorRedirigeYNoVeContenido() throws Exception
    {
        this.mockMvc.perform(get("/profesor/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(content().string(not(containsString("Panel de Control del Profesor"))));
    }

    @Test
    @DisplayName("Usuario no autenticado intentando acceder a /alumno/dashboard -> Redirige a /login (302) y no ve nada")
    void testNoAutenticadoAccediendoAAlumnoRedirigeYNoVeContenido() throws Exception
    {
        this.mockMvc.perform(get("/alumno/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(content().string(not(containsString("Panel del Alumno"))));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Administrador autorizado accede a su propio dashboard -> HTTP 200 OK y ve su panel")
    void testAdminAccedeASuDashboard() throws Exception
    {
        this.mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Panel de Control del Administrador")));
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    @DisplayName("Profesor autorizado accede a su propio dashboard -> HTTP 200 OK y ve su panel")
    void testProfesorAccedeASuDashboard() throws Exception
    {
        this.mockMvc.perform(get("/profesor/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Panel de Control del Profesor")));
    }

    @Test
    @DisplayName("Rutas públicas son accesibles sin autenticación")
    void testRutasPublicasAccesibles() throws Exception
    {
        this.mockMvc.perform(get("/login"))
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/registro"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Cierre de sesión -> Invalida y redirige a /login?logout=true")
    void testLogoutExitoso() throws Exception
    {
        this.mockMvc.perform(post("/logout")
                .with(csrf())) // Inyectamos el token CSRF para simular un formulario legítimo
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout=true"));
    }

    @Test
    @DisplayName("Control de sesiones concurrentes: Máximo 1 sesión activa por usuario (la nueva invalida a la previa)")
    void testControlSesionesConcurrentesMaximoUnaSesion() throws Exception
    {
        // Inicio de sesión del Administrador (Sesión 1)
        MvcResult primerLogin = this.mockMvc.perform(post("/login")
                .param("username", "admin")
                .param("password", "admin123")
                .with(csrf())) // Inyectamos el token CSRF para simular un formulario legítimo
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"))
                .andReturn(); // Devuelve el resultado de la petición para poder acceder a la sesión creada.

        // Obtenemos el objeto HttpSession de la primera sesión para simular el acceso desde otro navegador
        HttpSession primeraSesion = primerLogin.getRequest().getSession(false);
        Assertions.assertNotNull(primeraSesion, "La Sesión 1 se ha creado correctamente."); // Si 'primeraSesion' NO es null, se muestra este mensaje.

        // Inicio de sesión del Administrador (Sesión 2) desde otro navegador -> Se invalida la Sesión 1
        this.mockMvc.perform(post("/login")
                .param("username", "admin")
                .param("password", "admin123")
                .with(csrf())) // Inyectamos el token CSRF para simular un formulario legítimo
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"));

        // Acceso a la ruta protegida desde la Sesión 1 -> Es redirigido a /login?expirada=true
        this.mockMvc.perform(get("/admin/dashboard")
                .session((MockHttpSession) primeraSesion)) // Simulamos el acceso desde la Sesión 1 (que ha sido invalidada)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?expirada=true"));
    }

    @Test
    @DisplayName("Usuarios distintos pueden tener sesiones simultáneas sin interferir entre sí")
    void testUsuariosDistintosPuedenTenerSesionesSimultaneas() throws Exception
    {
        // 1. Login de Administrador (Sesión 1)
        MvcResult loginAdmin = this.mockMvc.perform(post("/login")
                .param("username", "admin")
                .param("password", "admin123")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"))
                .andReturn();

        HttpSession sesionAdmin = loginAdmin.getRequest().getSession(false);
        Assertions.assertNotNull(sesionAdmin);

        // 2. Login de Alumno (Sesión 2, simulando otro navegador)
        MvcResult loginAlumno = this.mockMvc.perform(post("/login")
                .param("username", "alumno1")
                .param("password", "alumno123")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/alumno/dashboard"))
                .andReturn();

        HttpSession sesionAlumno = loginAlumno.getRequest().getSession(false);
        Assertions.assertNotNull(sesionAlumno);

        // 3. Verificar que el Administrador sigue activo en su sesión
        this.mockMvc.perform(get("/admin/dashboard")
                .session((MockHttpSession) sesionAdmin))
                .andExpect(status().isOk());

        // 4. Verificar que el Alumno sigue activo en su sesión
        this.mockMvc.perform(get("/alumno/dashboard")
                .session((MockHttpSession) sesionAlumno))
                .andExpect(status().isOk());

        // 5. Iniciar una segunda sesión para Administrador (Sesión 3) -> Invalida Sesión 1, pero NO debe afectar a Alumno (Sesión 2)
        this.mockMvc.perform(post("/login")
                .param("username", "admin")
                .param("password", "admin123")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"));

        // Sesión 1 del Admin queda invalidada
        this.mockMvc.perform(get("/admin/dashboard")
                .session((MockHttpSession) sesionAdmin))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?expirada=true"));

        // Sesión 2 del Alumno DEBE SEGUIR TOTALMENTE ACTIVA
        this.mockMvc.perform(get("/alumno/dashboard")
                .session((MockHttpSession) sesionAlumno))
                .andExpect(status().isOk());
    }
}
