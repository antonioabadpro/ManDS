package com.autoescuela.erp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@TestPropertySource(properties =
{
        "spring.sql.init.mode=always",
        "spring.sql.init.data-locations=classpath:data.sql",
        "spring.jpa.defer-datasource-initialization=true"
})
class DataSqlH2Test
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

        assertEquals(11, vehiculos, "Debe haber 11 vehículos");
        assertEquals(32, personas, "Debe haber 32 personas registradas");
        assertEquals(1, admins, "Debe haber 1 administrador");
        assertEquals(5, profesores, "Debe haber 5 profesores");
        assertEquals(10, permisos, "Debe haber 10 permisos asignados a profesores");
        assertEquals(26, alumnos, "Debe haber 26 alumnos");
        assertEquals(8, incidencias, "Debe haber 8 incidencias registradas");
        assertEquals(19, matriculas, "Debe haber 19 matrículas");
        assertEquals(24, clases, "Debe haber 24 clases prácticas");
        assertEquals(9, solicitudes, "Debe haber 9 solicitudes de examen");
        assertEquals(8, examenes, "Debe haber 8 exámenes registrados");
    }
}
