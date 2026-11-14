package com.autoescuela.erp.core.config;

import java.time.Duration;
import java.util.List;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * Configuración del sistema de caché en memoria RAM (Spring Cache + Caffeine).
 * Almacena en memoria heap las agregaciones estadísticas, listados frecuentes y cuadrículas
 * semanales con límites estrictos de cardinalidad y TTL para operar con seguridad en Render (512 MB).
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
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(List.of(
                this.crearCache(CACHE_ESTADISTICAS_ADMIN, 5, Duration.ofMinutes(30)),
                this.crearCache(CACHE_ESTADISTICAS_DASHBOARD, 5, Duration.ofMinutes(30)),
                this.crearCache(CACHE_PROFESORES, 30, Duration.ofMinutes(15)),
                this.crearCache(CACHE_ALUMNOS, 30, Duration.ofMinutes(15)),
                this.crearCache(CACHE_FLOTA, 30, Duration.ofMinutes(15)),
                this.crearCache(CACHE_INCIDENCIAS, 30, Duration.ofMinutes(15)),
                this.crearCache(CACHE_EXAMENES, 30, Duration.ofMinutes(15)),
                this.crearCache(CACHE_ESTADISTICAS_PROFESOR, 100, Duration.ofMinutes(15)),
                this.crearCache(CACHE_ESTADISTICAS_ALUMNO, 100, Duration.ofMinutes(15)),
                this.crearCache(CACHE_CALENDARIO, 150, Duration.ofMinutes(10))
        ));
        return cacheManager;
    }

    /**
     * Construye una caché Caffeine acotada por número máximo de entradas y expiración tras escritura.
     */
    private CaffeineCache crearCache(String nombre, long maxEntradas, Duration ttlEscritura)
    {
        return new CaffeineCache(nombre, Caffeine.newBuilder()
                        .maximumSize(maxEntradas)
                        .expireAfterWrite(ttlEscritura)
                        .build()
        );
    }
}
