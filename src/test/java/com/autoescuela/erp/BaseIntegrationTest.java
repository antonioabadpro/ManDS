package com.autoescuela.erp;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.autoescuela.erp.core.email.EmailAsyncDispatcher;
import com.autoescuela.erp.pagos.service.PagoStripeService;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

/**
 * Clase base unificada para pruebas de integración con contexto completo de Spring Boot.
 * Centraliza la configuración de base de datos H2, data.sql, Spring Security y los mocks
 * transversales de Stripe y despacho de correo para maximizar el Context Caching de Spring Test,
 * evitar reinicios del contenedor y aislar conexiones externas durante la suite.
 */
@SpringBootTest
@Transactional
public abstract class BaseIntegrationTest
{
    @Autowired
    protected WebApplicationContext contexto;

    @MockitoBean
    protected PagoStripeService pagoStripeService;

    @MockitoBean
    protected EmailAsyncDispatcher emailAsyncDispatcher;

    protected MockMvc mockMvc;

    @BeforeEach
    void setUpBaseIntegration()
    {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(this.contexto)
                .apply(springSecurity())
                .build();
    }
}
