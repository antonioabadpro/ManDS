package com.autoescuela.erp.core.excepciones;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
/**
 * Pruebas para la resolución de vistas de error globales (404, 403, 500 y Regla de Negocio).
 */
class GlobalExceptionHandlerTest
{
    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;
    private MockMvc standaloneMockMvc;

    @Controller
    static class ControladorPruebasError
    {
        @GetMapping("/test-error-recurso")
        public String dispararRecursoNoEncontrado()
        {
            throw new RecursoNoEncontradoException("Vehículo con matrícula 9999-ZZZ no existe.");
        }

        @GetMapping("/test-error-acceso")
        public String dispararAccesoDenegado()
        {
            throw new AccessDeniedException("No posees el rol requerido.");
        }

        @GetMapping("/test-error-negocio")
        public String dispararReglaNegocio()
        {
            throw new ReglaNegocioException("No se pueden cancelar prácticas con menos de 24 horas de antelación.");
        }

        @GetMapping("/test-error-500")
        public String dispararErrorInterno()
        {
            throw new RuntimeException("Fallo inesperado simulado.");
        }
    }

    @BeforeEach
    void setUp()
    {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(this.context)
                .apply(springSecurity())
                .build();

        this.standaloneMockMvc = MockMvcBuilders
                .standaloneSetup(new ControladorPruebasError())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("Petición a ruta inexistente -> Devuelve HTTP 404 y vista error/404 con estilos corporativos")
    void testRutaInexistenteDevuelve404() throws Exception
    {
        this.mockMvc.perform(get("/admin/ruta-totalmente-inexistente-xyz"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"))
                .andExpect(model().attributeExists("status"))
                .andExpect(model().attributeExists("error"))
                .andExpect(content().string(containsString("Error 404")))
                .andExpect(content().string(containsString("Página no encontrada")))
                .andExpect(content().string(containsString("Volver al Inicio")));
    }

    @Test
    @DisplayName("RecursoNoEncontradoException -> Devuelve HTTP 404 y vista error/404")
    void testRecursoNoEncontradoException() throws Exception
    {
        this.standaloneMockMvc.perform(get("/test-error-recurso"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"))
                .andExpect(model().attribute("status", 404))
                .andExpect(model().attribute("message", "Vehículo con matrícula 9999-ZZZ no existe."));
    }

    @Test
    @DisplayName("AccessDeniedException -> Devuelve HTTP 403 y vista error/403")
    void testAccessDeniedException() throws Exception
    {
        this.standaloneMockMvc.perform(get("/test-error-acceso"))
                .andExpect(status().isForbidden())
                .andExpect(view().name("error/403"))
                .andExpect(model().attribute("status", 403));
    }

    @Test
    @DisplayName("ReglaNegocioException -> Devuelve HTTP 422 y vista error/error-negocio")
    void testReglaNegocioException() throws Exception
    {
        this.standaloneMockMvc.perform(get("/test-error-negocio"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(view().name("error/error-negocio"))
                .andExpect(model().attribute("status", 422))
                .andExpect(model().attribute("message", "No se pueden cancelar prácticas con menos de 24 horas de antelación."));
    }

    @Test
    @DisplayName("Exception no controlada -> Devuelve HTTP 500 y vista error/500")
    void testExceptionGenericaDevuelve500() throws Exception
    {
        this.standaloneMockMvc.perform(get("/test-error-500"))
                .andExpect(status().isInternalServerError())
                .andExpect(view().name("error/500"))
                .andExpect(model().attribute("status", 500));
    }
}

