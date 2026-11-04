package com.autoescuela.erp.core.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Configuración centralizada de ejecución asíncrona para tareas en segundo plano.
 * Proporciona un pool de hilos dedicado para el despacho de correos electrónicos
 * transaccionales sin bloquear los hilos de atención HTTP del servidor web.
 */
@Configuration
@EnableAsync
public class AsyncConfig
{
    /**
     * Pool de hilos dedicado para el envío de correos electrónicos transaccionales.
     * Configurado con parámetros óptimos para contenedores de recursos acotados.
     *
     * @return Ejecutor de tareas asíncronas para correo electrónico.
     */
    @Bean(name = "emailTaskExecutor")
    public Executor emailTaskExecutor()
    {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("email-async-");
        executor.initialize();
        return executor;
    }
}
