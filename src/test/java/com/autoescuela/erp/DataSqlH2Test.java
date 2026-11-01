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
        Integer fechasExamen = this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM fecha_examen", Integer.class);
        Integer cuposExamen = this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM cupo_examen", Integer.class);

        assertEquals(34, vehiculos, "Debe haber 34 vehículos");
        assertEquals(84, personas, "Debe haber 84 personas registradas");
        assertEquals(1, admins, "Debe haber 1 administrador");
        assertEquals(30, profesores, "Debe haber 30 profesores");
        assertEquals(81, permisos, "Debe haber 81 permisos asignados a profesores");
        assertEquals(53, alumnos, "Debe haber 53 alumnos");
        assertEquals(10, incidencias, "Debe haber 10 incidencias registradas");
        assertEquals(55, matriculas, "Debe haber 55 matrículas");
        assertEquals(169, clases, "Debe haber 169 clases prácticas");
        assertEquals(50, solicitudes, "Debe haber 50 solicitudes de examen");
        assertEquals(39, examenes, "Debe haber 39 exámenes registrados");
        assertEquals(2, fechasExamen, "Debe haber 2 registros de fechas de examen fijadas");
        assertEquals(15, cuposExamen, "Debe haber 15 registros de cupos de examen fijados");
    }
}
