package com.autoescuela.erp.core.config;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatabaseIndexInitializerTest
{
    private static final List<String> TABLAS_SECUENCIA = List.of(
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

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private DatabaseIndexInitializer databaseIndexInitializer;

    @BeforeEach
    void setUp()
    {
        ReflectionTestUtils.setField(this.databaseIndexInitializer, "sincronizarSecuencias", true);
        ReflectionTestUtils.setField(this.databaseIndexInitializer, "inicializarIndices", true);
        ReflectionTestUtils.setField(this.databaseIndexInitializer, "ddlAuto", "create-drop");
    }

    private static ConnectionCallback<String> cualquierCallbackConexion()
    {
        return ArgumentMatchers.<ConnectionCallback<String>>any();
    }

    private void simularMotor(String nombreProducto) throws SQLException
    {
        Connection conexion = mock(Connection.class);
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        when(conexion.getMetaData()).thenReturn(metaData);
        when(metaData.getDatabaseProductName()).thenReturn(nombreProducto);
        when(this.jdbcTemplate.execute(cualquierCallbackConexion())).thenAnswer(invocacion -> {
            ConnectionCallback<String> callback = invocacion.getArgument(0);
            return callback.doInConnection(conexion);
        });
    }

    @Test
    @DisplayName("PostgreSQL con sincronización activa crea índices y ejecuta el bloque dinámico sin fallback")
    void testRun_PostgreSQL_SincronizacionDinamicaExitosa() throws SQLException
    {
        simularMotor("PostgreSQL");

        this.databaseIndexInitializer.run(null);

        verify(this.jdbcTemplate).execute(argThat((String sql) -> sql != null && sql.contains("uq_clase_profesor_fechahora_activa")));
        verify(this.jdbcTemplate).execute(argThat((String sql) -> sql != null && sql.contains("idx_solicitud_examen_alumno")));
        verify(this.jdbcTemplate).execute(argThat((String sql) -> sql != null && sql.contains("DO $$")));
        verify(this.jdbcTemplate, never()).execute(argThat((String sql) -> sql != null && sql.contains("pg_get_serial_sequence('persona'")));
    }

    @Test
    @DisplayName("PostgreSQL aplica fallback estático por tabla si falla la sincronización dinámica")
    void testRun_PostgreSQL_FallbackEstaticoSiFallaBloqueDinamico() throws SQLException
    {
        simularMotor("PostgreSQL");
        doAnswer(invocacion -> {
            String sql = invocacion.getArgument(0);
            if (sql != null && sql.contains("DO $$"))
            {
                throw new DataAccessResourceFailureException("Fallo DO");
            }
            return null;
        }).when(this.jdbcTemplate).execute(anyString());

        this.databaseIndexInitializer.run(null);

        for (String tabla : TABLAS_SECUENCIA)
        {
            verify(this.jdbcTemplate, times(1)).execute(
                    argThat((String sql) -> sql != null && sql.contains("pg_get_serial_sequence('" + tabla + "'")));
        }
    }

    @Test
    @DisplayName("Fallback estático continúa sincronizando el resto de tablas aunque una lance excepción")
    void testRun_PostgreSQL_FallbackEstaticoContinuaSiFallaUnaTabla() throws SQLException
    {
        simularMotor("PostgreSQL");
        doAnswer(invocacion -> {
            String sql = invocacion.getArgument(0);
            if (sql != null && (sql.contains("DO $$") || sql.contains("pg_get_serial_sequence('persona'")))
            {
                throw new DataAccessResourceFailureException("Fallo simulado");
            }
            return null;
        }).when(this.jdbcTemplate).execute(anyString());

        assertDoesNotThrow(() -> this.databaseIndexInitializer.run(null));

        verify(this.jdbcTemplate, times(1)).execute(
                argThat((String sql) -> sql != null && sql.contains("pg_get_serial_sequence('cupo_examen'")));
    }

    @Test
    @DisplayName("PostgreSQL con sincronización desactivada crea índices y omite secuencias")
    void testRun_PostgreSQL_SinSincronizarSecuencias() throws SQLException
    {
        ReflectionTestUtils.setField(this.databaseIndexInitializer, "sincronizarSecuencias", false);
        simularMotor("PostgreSQL");

        this.databaseIndexInitializer.run(null);

        verify(this.jdbcTemplate).execute(argThat((String sql) -> sql != null && sql.contains("uq_clase_profesor_fechahora_activa")));
        verify(this.jdbcTemplate, never()).execute(argThat((String sql) -> sql != null && sql.contains("DO $$")));
    }

    @Test
    @DisplayName("H2 crea la columna generada y el índice único de concurrencia")
    void testRun_H2_AseguraColumnaEIndice() throws SQLException
    {
        simularMotor("H2");

        this.databaseIndexInitializer.run(null);

        verify(this.jdbcTemplate).execute(argThat((String sql) -> sql != null && sql.contains("ADD COLUMN IF NOT EXISTS profesor_activo_id")));
        verify(this.jdbcTemplate).execute(argThat((String sql) -> sql != null && sql.contains("uq_clase_profesor_activo")));
    }

    @Test
    @DisplayName("Motor no soportado no ejecuta sentencias DDL")
    void testRun_MotorNoSoportado_NoEjecutaSentencias() throws SQLException
    {
        simularMotor("Oracle");

        this.databaseIndexInitializer.run(null);

        verify(this.jdbcTemplate, never()).execute(anyString());
    }

    @Test
    @DisplayName("Error al obtener metadatos de conexión queda controlado sin propagar excepción")
    void testRun_ErrorConexion_NoPropagaExcepcion()
    {
        when(this.jdbcTemplate.execute(cualquierCallbackConexion()))
                .thenThrow(new DataAccessResourceFailureException("Conexión rechazada"));

        assertDoesNotThrow(() -> this.databaseIndexInitializer.run(null));
    }

    @Test
    @DisplayName("En perfil deploy (ddl-auto=none e init-indexes=false) no ejecuta ninguna sentencia DDL ni consulta de metadatos")
    void testRun_ModoDeploy_NoEjecutaDdlNiMetadatos()
    {
        ReflectionTestUtils.setField(this.databaseIndexInitializer, "ddlAuto", "none");
        ReflectionTestUtils.setField(this.databaseIndexInitializer, "inicializarIndices", false);
        ReflectionTestUtils.setField(this.databaseIndexInitializer, "sincronizarSecuencias", false);

        this.databaseIndexInitializer.run(null);

        verify(this.jdbcTemplate, never()).execute(cualquierCallbackConexion());
        verify(this.jdbcTemplate, never()).execute(anyString());
    }

    @Test
    @DisplayName("Con ddl-auto=none aunque init-indexes sea true omite creación de índices en PostgreSQL")
    void testRun_DdlAutoNone_OmiteIndices() throws SQLException
    {
        ReflectionTestUtils.setField(this.databaseIndexInitializer, "ddlAuto", "none");
        ReflectionTestUtils.setField(this.databaseIndexInitializer, "inicializarIndices", true);
        ReflectionTestUtils.setField(this.databaseIndexInitializer, "sincronizarSecuencias", true);
        simularMotor("PostgreSQL");

        this.databaseIndexInitializer.run(null);

        verify(this.jdbcTemplate, never()).execute(argThat((String sql) -> sql != null && sql.contains("CREATE UNIQUE INDEX")));
        verify(this.jdbcTemplate).execute(argThat((String sql) -> sql != null && sql.contains("DO $$")));
    }
}
