package com.autoescuela.erp.core.config;

import java.util.List;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración del sistema de caché en memoria RAM (Spring Cache).
 * Almacena en memoria heap las agregaciones estadísticas y dashboards
 * para proporcionar tiempos de respuesta de 0 ms en accesos sucesivos.
 */
@Configuration
@EnableCaching
public class CacheConfig
{
    public static final String CACHE_ESTADISTICAS_ADMIN = "estadisticasAdmin";
    public static final String CACHE_ESTADISTICAS_DASHBOARD = "estadisticasDashboard";
    public static final String CACHE_PROFESORES = "profesores";
    public static final String CACHE_ALUMNOS = "alumnos";
    public static final String CACHE_FLOTA = "flota";
    public static final String CACHE_INCIDENCIAS = "incidencias";
    public static final String CACHE_EXAMENES = "examenes";
    public static final String CACHE_CALENDARIO = "calendario";
    public static final String CACHE_ESTADISTICAS_PROFESOR = "estadisticasProfesor";
    public static final String CACHE_ESTADISTICAS_ALUMNO = "estadisticasAlumno";

    @Bean
    public CacheManager cacheManager()
    {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setCacheNames(List.of(
                CACHE_ESTADISTICAS_ADMIN,
                CACHE_ESTADISTICAS_DASHBOARD,
                CACHE_PROFESORES,
                CACHE_ALUMNOS,
                CACHE_FLOTA,
                CACHE_INCIDENCIAS,
                CACHE_EXAMENES,
                CACHE_CALENDARIO,
                CACHE_ESTADISTICAS_PROFESOR,
                CACHE_ESTADISTICAS_ALUMNO
        ));
        return cacheManager;
    }
}
