package com.autoescuela.erp.core.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.mock.web.MockHttpSession;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Assertions;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

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
    @DisplayName("Login fallido con contraseña incorrecta -> Redirige a /login")
    void testLoginCredencialesIncorrectas() throws Exception
    {
        this.mockMvc.perform(post("/login")
                .param("username", "admin")
                .param("password", "password_incorrecto")
                .with(csrf())) // Inyectamos el token CSRF para simular un formulario legítimo
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
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
    @DisplayName("Acceso no autenticado a ruta protegida con sesión caducada -> Redirige a /login con flash attribute sesionExpirada")
    void testAccesoNoAutenticadoConSesionCaducadaRedirigeALoginConFlashAttribute() throws Exception
    {
        MvcResult resultado = this.mockMvc.perform(get("/admin/usuarios")
                .cookie(new Cookie("JSESSIONID", "sesion-invalida-antigua"))
                .with(request -> {
                    request.setRequestedSessionId("sesion-invalida-antigua");
                    request.setRequestedSessionIdFromCookie(true);
                    request.setRequestedSessionIdValid(false);
                    return request;
                }))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andReturn();

        MockHttpSession sesionTrasRedirect = (MockHttpSession) resultado.getRequest().getSession(false);
        Assertions.assertNotNull(sesionTrasRedirect, "Debe existir sesión para transportar el FlashMap tras caducidad.");

        this.mockMvc.perform(get("/login").session(sesionTrasRedirect))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attribute("sesionExpirada", true));
    }

    @Test
    @DisplayName("Acceso no autenticado a ruta protegida sin sesión previa -> Redirige a /login sin flash attribute sesionExpirada")
    void testAccesoNoAutenticadoSinSesionPreviaRedirigeALoginSinFlashAttribute() throws Exception
    {
        MvcResult resultado = this.mockMvc.perform(get("/admin/usuarios"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andReturn();

        MockHttpSession sesionTrasRedirect = (MockHttpSession) resultado.getRequest().getSession(false);
        MockHttpServletRequestBuilder peticionLogin = get("/login");
        if (sesionTrasRedirect != null)
        {
            peticionLogin.session(sesionTrasRedirect);
        }

        this.mockMvc.perform(peticionLogin)
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attributeDoesNotExist("sesionExpirada"));
    }

    @Test
    @DisplayName("Acceso no autenticado con cookie JSESSIONID válida -> Redirige a /login sin flash attribute sesionExpirada")
    void testAccesoNoAutenticadoConCookieYSessionIdValidoNoMarcaSesionExpirada() throws Exception
    {
        MvcResult resultado = this.mockMvc.perform(get("/admin/usuarios")
                .cookie(new Cookie("JSESSIONID", "sesion-valida"))
                .with(request -> {
                    request.setRequestedSessionIdValid(true);
                    return request;
                }))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andReturn();

        MockHttpSession sesionTrasRedirect = (MockHttpSession) resultado.getRequest().getSession(false);
        MockHttpServletRequestBuilder peticionLogin = get("/login");
        if (sesionTrasRedirect != null)
        {
            peticionLogin.session(sesionTrasRedirect);
        }

        this.mockMvc.perform(peticionLogin)
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attributeDoesNotExist("sesionExpirada"));
    }

    @Test
    @DisplayName("Petición HTMX no autenticada -> Responde con HTTP 401 y cabecera HX-Redirect")
    void testPeticionHtmxNoAutenticadaDevuelveHxRedirect() throws Exception
    {
        this.mockMvc.perform(get("/alumno/reservar")
                .header("HX-Request", "true"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("HX-Redirect", "/login"));
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    @DisplayName("Alumno intentando acceder a /admin/dashboard -> HTTP 403 Forbidden")
    void testAlumnoAccediendoAAdminForbiddenYNoVeContenido() throws Exception
    {
        this.mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    @DisplayName("Alumno intentando acceder a /profesor/dashboard -> HTTP 403 Forbidden")
    void testAlumnoAccediendoAProfesorForbiddenYNoVeContenido() throws Exception
    {
        this.mockMvc.perform(get("/profesor/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    @DisplayName("Profesor intentando acceder a /admin/dashboard -> HTTP 403 Forbidden")
    void testProfesorAccediendoAAdminForbiddenYNoVeContenido() throws Exception
    {
        this.mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    @DisplayName("Profesor intentando acceder a /alumno/dashboard -> HTTP 403 Forbidden")
    void testProfesorAccediendoAAlumnoForbiddenYNoVeContenido() throws Exception
    {
        this.mockMvc.perform(get("/alumno/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Usuario no autenticado intentando acceder a /admin/dashboard -> Redirige a /login (302)")
    void testNoAutenticadoAccediendoAAdminRedirigeYNoVeContenido() throws Exception
    {
        this.mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("Usuario no autenticado intentando acceder a /profesor/dashboard -> Redirige a /login (302)")
    void testNoAutenticadoAccediendoAProfesorRedirigeYNoVeContenido() throws Exception
    {
        this.mockMvc.perform(get("/profesor/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("Usuario no autenticado intentando acceder a /alumno/dashboard -> Redirige a /login (302)")
    void testNoAutenticadoAccediendoAAlumnoRedirigeYNoVeContenido() throws Exception
    {
        this.mockMvc.perform(get("/alumno/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Administrador autorizado accede a su propio dashboard -> HTTP 200 OK")
    void testAdminAccedeASuDashboard() throws Exception
    {
        this.mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"));
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    @DisplayName("Profesor autorizado accede a su propio dashboard -> HTTP 200 OK")
    void testProfesorAccedeASuDashboard() throws Exception
    {
        this.mockMvc.perform(get("/profesor/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("profesor/dashboard"));
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
    @DisplayName("Cierre de sesión -> Invalida y redirige a /login")
    void testLogoutExitoso() throws Exception
    {
        this.mockMvc.perform(post("/logout")
                .with(csrf())) // Inyectamos el token CSRF para simular un formulario legítimo
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
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

        // Acceso a la ruta protegida desde la Sesión 1 -> Es redirigido a /login
        this.mockMvc.perform(get("/admin/dashboard")
                .session((MockHttpSession) primeraSesion)) // Simulamos el acceso desde la Sesión 1 (que ha sido invalidada)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
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
                .andExpect(redirectedUrl("/login"));

        // Sesión 2 del Alumno DEBE SEGUIR TOTALMENTE ACTIVA
        this.mockMvc.perform(get("/alumno/dashboard")
                .session((MockHttpSession) sesionAlumno))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /login sin token CSRF es rechazado con 403 Forbidden (protección CSRF global activa)")
    void testPostLoginSinCsrfEsRechazado() throws Exception
    {
        this.mockMvc.perform(post("/login")
                .param("username", "admin")
                .param("password", "admin123"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /pagos/webhook está exento de CSRF y alcanza el controlador de Stripe")
    void testWebhookStripeExentoDeCsrf() throws Exception
    {
        this.mockMvc.perform(post("/pagos/webhook")
                .content("{}"))
                .andExpect(status().isBadRequest());
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
