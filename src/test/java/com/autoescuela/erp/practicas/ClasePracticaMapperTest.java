package com.autoescuela.erp.practicas;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.autoescuela.erp.core.enums.EstadoClase;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.flota.model.Vehiculo;
import com.autoescuela.erp.practicas.dto.ClasePracticaAdminDTO;
import com.autoescuela.erp.practicas.mapper.ClasePracticaMapper;
import com.autoescuela.erp.practicas.model.ClasePractica;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.model.Profesor;

@DisplayName("Tests Unitarios de ClasePracticaMapper (MapStruct)")
class ClasePracticaMapperTest
{
    private final ClasePracticaMapper mapper = Mappers.getMapper(ClasePracticaMapper.class);

    @Test
    @DisplayName("toClasePracticaAdminDTO mapea exhaustivamente relaciones y odómetro en clase RECIBIDA")
    void testToClasePracticaAdminDTOCompleto()
    {
        Alumno alumno = new Alumno();
        alumno.setNombre("Carlos");
        alumno.setApellidos("Gómez Ruiz");
        alumno.setNombreUsuario("carlos.gomez");

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setMatricula("1234-BBB");
        vehiculo.setMarca("SEAT");
        vehiculo.setModelo("Ibiza 1.0");
        vehiculo.setTipoPermiso(TipoCarnet.PERMISO_B);

        Profesor profesor = new Profesor();
        profesor.setNombre("Laura");
        profesor.setApellidos("Sánchez Peña");
        profesor.setNombreUsuario("laura.profe");
        profesor.setVehiculo(vehiculo);

        LocalDateTime fechaHora = LocalDateTime.of(2026, 10, 15, 10, 0);

        ClasePractica clase = new ClasePractica();
        clase.setFechaHora(fechaHora);
        clase.setDuracion(45);
        clase.setPuntoRecogida("Estación de Autobuses");
        clase.setKmInicio(10000L);
        clase.setKmFin(10045L);
        clase.setEstadoClase(EstadoClase.RECIBIDA);
        clase.setObservaciones("Práctica de circulación urbana y rotondas");
        clase.setAlumno(alumno);
        clase.setProfesor(profesor);

        ClasePracticaAdminDTO dto = this.mapper.toClasePracticaAdminDTO(clase);

        assertThat(dto).isNotNull();
        assertThat(dto.fechaHora()).isEqualTo(fechaHora);
        assertThat(dto.duracion()).isEqualTo(45);
        assertThat(dto.puntoRecogida()).isEqualTo("Estación de Autobuses");
        assertThat(dto.kmInicio()).isEqualTo(10000L);
        assertThat(dto.kmFin()).isEqualTo(10045L);
        assertThat(dto.kmFormateado()).isEqualTo("10.000 a 10.045 km (+45 km)");
        assertThat(dto.estadoClase()).isEqualTo(EstadoClase.RECIBIDA);
        assertThat(dto.alumnoNombreCompleto()).isEqualTo("Carlos Gómez Ruiz");
        assertThat(dto.alumnoUsername()).isEqualTo("carlos.gomez");
        assertThat(dto.profesorNombreCompleto()).isEqualTo("Laura Sánchez Peña");
        assertThat(dto.profesorUsername()).isEqualTo("laura.profe");
        assertThat(dto.vehiculoMatricula()).isEqualTo("1234-BBB");
        assertThat(dto.vehiculoModelo()).isEqualTo("SEAT Ibiza 1.0");
        assertThat(dto.vehiculoDescripcion()).isEqualTo("SEAT Ibiza 1.0 (1234-BBB)");
        assertThat(dto.tipoPermiso()).isEqualTo(TipoCarnet.PERMISO_B);
        assertThat(dto.tipoPermisoDescripcion()).isEqualTo(TipoCarnet.PERMISO_B.getDescripcion());
        assertThat(dto.observaciones()).isEqualTo("Práctica de circulación urbana y rotondas");
    }

    @Test
    @DisplayName("toClasePracticaAdminDTO maneja correctamente clase PENDIENTE sin vehículo asignado")
    void testToClasePracticaAdminDTOSinVehiculoYPendiente()
    {
        Alumno alumno = new Alumno();
        alumno.setNombre("Marta");
        alumno.setApellidos("Navarro");
        alumno.setNombreUsuario("marta.n");

        Profesor profesor = new Profesor();
        profesor.setNombre("Pedro");
        profesor.setApellidos("Ruiz");
        profesor.setNombreUsuario("pedro.r");
        profesor.setVehiculo(null);

        ClasePractica clase = new ClasePractica();
        clase.setFechaHora(LocalDateTime.of(2026, 10, 20, 16, 0));
        clase.setDuracion(45);
        clase.setPuntoRecogida("Sede Central");
        clase.setKmInicio(0L);
        clase.setKmFin(0L);
        clase.setEstadoClase(EstadoClase.PENDIENTE);
        clase.setAlumno(alumno);
        clase.setProfesor(profesor);

        ClasePracticaAdminDTO dto = this.mapper.toClasePracticaAdminDTO(clase);

        assertThat(dto).isNotNull();
        assertThat(dto.kmFormateado()).isEqualTo("--");
        assertThat(dto.estadoClase()).isEqualTo(EstadoClase.PENDIENTE);
        assertThat(dto.vehiculoMatricula()).isNull();
        assertThat(dto.vehiculoModelo()).isNull();
        assertThat(dto.vehiculoDescripcion()).isNull();
        assertThat(dto.tipoPermiso()).isNull();
        assertThat(dto.tipoPermisoDescripcion()).isNull();
    }

    @Test
    @DisplayName("toClasePracticaAdminDTO retorna null cuando la entidad recibida es null")
    void testToClasePracticaAdminDTONulo()
    {
        ClasePracticaAdminDTO dto = this.mapper.toClasePracticaAdminDTO(null);
        assertThat(dto).isNull();
    }

    @Test
    @DisplayName("toClasePracticaAdminDTOList transforma colecciones completas")
    void testToClasePracticaAdminDTOList()
    {
        ClasePractica c1 = new ClasePractica();
        c1.setDuracion(45);
        c1.setEstadoClase(EstadoClase.PENDIENTE);

        ClasePractica c2 = new ClasePractica();
        c2.setDuracion(90);
        c2.setEstadoClase(EstadoClase.CANCELADA);

        List<ClasePracticaAdminDTO> dtos = this.mapper.toClasePracticaAdminDTOList(List.of(c1, c2));

        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).duracion()).isEqualTo(45);
        assertThat(dtos.get(1).duracion()).isEqualTo(90);
    }
}
