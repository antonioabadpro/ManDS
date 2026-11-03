package com.autoescuela.erp.core.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.db.sync-sequences:true}")
    private boolean sincronizarSecuencias;

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

                    // Sincronización de secuencias IDENTITY tras la carga de data.sql
                    // Índices de claves foráneas para acelerar JOINs y evitar Seq Scans
                    this.jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_clase_practica_alumno ON clase_practica (alumno_id)");
                    this.jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_clase_practica_profesor ON clase_practica (profesor_id)");
                    this.jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_clase_practica_vehiculo ON clase_practica (vehiculo_id)");
                    this.jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_matricula_alumno ON matricula (alumno_id)");
                    this.jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_examen_alumno ON examen (alumno_id)");
                    this.jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_solicitud_examen_alumno ON solicitud_examen (alumno_id)");
                    log.info("Índices de claves foráneas PostgreSQL asegurados.");

                    if (this.sincronizarSecuencias)
                    {
                        sincronizarSecuenciasPostgreSQL();
                    }
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

    /**
     * Sincroniza las secuencias de las columnas IDENTITY en PostgreSQL tras la carga inicial de data.sql.
     * En PostgreSQL, la inserción de registros con IDs explícitos no avanza automáticamente las secuencias
     * asociadas (a diferencia de H2), provocando colisiones de clave primaria (ej. duplicate key en persona_pkey)
     * al registrar nuevas entidades (como dar de alta a un profesor).
     */
    private void sincronizarSecuenciasPostgreSQL()
    {
        try
        {
            String sqlBloqueDinamico = """
                DO $$
                DECLARE
                    r RECORD;
                    secuencia TEXT;
                    tiene_filas BOOLEAN;
                    max_id BIGINT;
                BEGIN
                    FOR r IN (
                        SELECT c.table_name, c.column_name
                        FROM information_schema.columns c
                        JOIN information_schema.tables t ON c.table_name = t.table_name AND c.table_schema = t.table_schema
                        WHERE c.table_schema = 'public'
                          AND t.table_type = 'BASE TABLE'
                          AND (c.column_default LIKE 'nextval(%' OR c.is_identity = 'YES')
                    ) LOOP
                        secuencia := pg_get_serial_sequence(quote_ident(r.table_name), quote_ident(r.column_name));
                        IF secuencia IS NOT NULL THEN
                            EXECUTE format('SELECT MAX(%I) IS NOT NULL, COALESCE(MAX(%I), 1) FROM %I', r.column_name, r.column_name, r.table_name) INTO tiene_filas, max_id;
                            PERFORM setval(secuencia, max_id, tiene_filas);
                        END IF;
                    END LOOP;
                END $$;
                """;
            this.jdbcTemplate.execute(sqlBloqueDinamico);
            log.info("Secuencias de PostgreSQL sincronizadas dinámicamente con éxito.");
        }
        catch (Exception ex)
        {
            log.warn("Fallo en sincronización dinámica de secuencias en PostgreSQL: {}. Aplicando sincronización estática...", ex.getMessage());
            sincronizarSecuenciasEstaticasPostgreSQL();
        }
    }

    private void sincronizarSecuenciasEstaticasPostgreSQL()
    {
        List<String> tablas = List.of(
                "persona",
                "vehiculo",
                "matricula",
                "incidencia_vehiculo",
                "clase_practica",
                "solicitud_examen",
                "examen",
                "fecha_examen",
                "cupo_examen"
        );

        for (String tabla : tablas)
        {
            try
            {
                String sql = "SELECT setval(pg_get_serial_sequence('" + tabla + "', 'id'), "
                        + "COALESCE((SELECT MAX(id) FROM " + tabla + "), 1), "
                        + "(SELECT MAX(id) IS NOT NULL FROM " + tabla + "))";
                this.jdbcTemplate.execute(sql);
                log.info("Secuencia sincronizada en PostgreSQL para tabla: {}", tabla);
            }
            catch (Exception ex)
            {
                log.warn("No se pudo sincronizar la secuencia para la tabla {}: {}", tabla, ex.getMessage());
            }
        }
    }
}
