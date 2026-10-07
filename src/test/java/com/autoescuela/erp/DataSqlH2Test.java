package com.autoescuela.erp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DataSqlH2Test extends BaseIntegrationTest
{
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Verifica que el fichero 'data.sql' se ejecuta correctamente e inserta datos en todas las entidades")
    void testDataSqlExecutedSuccessfully()
    {
        Integer vehiculos = this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM vehiculo", Integer.class);
        Integer personas = this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM persona", Integer.class);
        Integer admins = this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM administrador", Integer.class);
        Integer profesores = this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM profesor", Integer.class);
        Integer permisos = this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM profesor_permisos", Integer.class);
        Integer alumnos = this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM alumno", Integer.class);
        Integer incidencias = this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM incidencia_vehiculo", Integer.class);
        Integer matriculas = this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM matricula", Integer.class);
        Integer clases = this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM clase_practica", Integer.class);
        Integer solicitudes = this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM solicitud_examen", Integer.class);
        Integer examenes = this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM examen", Integer.class);

        assertEquals(19, vehiculos, "Debe haber 19 vehículos");
        assertEquals(35, personas, "Debe haber 35 personas registradas");
        assertEquals(1, admins, "Debe haber 1 administrador");
        assertEquals(6, profesores, "Debe haber 6 profesores");
        assertEquals(12, permisos, "Debe haber 12 permisos asignados a profesores");
        assertEquals(28, alumnos, "Debe haber 28 alumnos");
        assertEquals(9, incidencias, "Debe haber 9 incidencias registradas");
        assertEquals(29, matriculas, "Debe haber 29 matrículas");
        assertEquals(248, clases, "Debe haber 248 clases prácticas");
        assertEquals(45, solicitudes, "Debe haber 45 solicitudes de examen");
        assertEquals(38, examenes, "Debe haber 38 exámenes registrados");
    }
}
