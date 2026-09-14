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
        Integer vehiculos = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM vehiculo", Integer.class);
        Integer personas = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM persona", Integer.class);
        Integer admins = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM administrador", Integer.class);
        Integer profesores = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM profesor", Integer.class);
        Integer permisos = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM profesor_permisos", Integer.class);
        Integer alumnos = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM alumno", Integer.class);
        Integer incidencias = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM incidencia_vehiculo", Integer.class);
        Integer matriculas = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM matricula", Integer.class);
        Integer clases = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM clase_practica", Integer.class);
        Integer solicitudes = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM solicitud_examen", Integer.class);
        Integer examenes = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM examen", Integer.class);

        assertEquals(4, vehiculos, "Debe haber 4 vehículos");
        assertEquals(6, personas, "Debe haber 6 personas registradas");
        assertEquals(1, admins, "Debe haber 1 administrador");
        assertEquals(2, profesores, "Debe haber 2 profesores");
        assertEquals(4, permisos, "Debe haber 4 permisos asignados a profesores");
        assertEquals(3, alumnos, "Debe haber 3 alumnos");
        assertEquals(2, incidencias, "Debe haber 2 incidencias registradas");
        assertEquals(3, matriculas, "Debe haber 3 matrículas");
        assertEquals(4, clases, "Debe haber 4 clases prácticas");
        assertEquals(3, solicitudes, "Debe haber 3 solicitudes de examen");
        assertEquals(3, examenes, "Debe haber 3 exámenes registrados");
    }
}
