package com.autoescuela.erp.core.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Inicializador de base de datos para asegurar restricciones e índices de concurrencia
 * en reservas de clases prácticas (First-Come, First-Served).
 * Compatible de forma nativa con PostgreSQL (Supabase/Docker) y H2 (Local/Tests).
 */
@Slf4j
@Component
@Order(10)
@RequiredArgsConstructor
public class DatabaseIndexInitializer implements ApplicationRunner
{
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args)
    {
        try
        {
            String databaseProductName = this.jdbcTemplate.execute((ConnectionCallback<String>) conn -> conn.getMetaData().getDatabaseProductName());

            log.info("Inicializando índice de concurrencia para base de datos: {}", databaseProductName);

            switch(databaseProductName)
            {
                case "PostgreSQL":
                    // En PostgreSQL creamos el índice único parcial nativo
                    this.jdbcTemplate.execute(
                            "CREATE UNIQUE INDEX IF NOT EXISTS uq_clase_profesor_fechahora_activa " +
                            "ON clase_practica (profesor_id, fecha_hora) " +
                            "WHERE estado_clase != 'CANCELADA'"
                    );
                    log.info("Índice único parcial de PostgreSQL asegurado en clase_practica.");
                    break;

                case "H2":
                    // En H2 emulamos el índice parcial mediante columna generada con valores NULL para CANCELADA
                    this.jdbcTemplate.execute(
                            "ALTER TABLE clase_practica ADD COLUMN IF NOT EXISTS profesor_activo_id BIGINT " +
                            "GENERATED ALWAYS AS (CASE WHEN estado_clase = 'CANCELADA' THEN NULL ELSE profesor_id END)"
                    );
                    this.jdbcTemplate.execute(
                            "CREATE UNIQUE INDEX IF NOT EXISTS uq_clase_profesor_activo " +
                            "ON clase_practica (profesor_activo_id, fecha_hora)"
                    );
                    log.info("Columna generada e índice único de concurrencia H2 asegurados en clase_practica.");
                    break;

                default:
                    log.warn("Base de datos no soportada para inicialización de índices de concurrencia: {}", databaseProductName);
            }
        }
        catch (Exception ex)
        {
            log.error("Error al inicializar índices de concurrencia en clase_practica: {}", ex.getMessage(), ex);
        }
    }
}
