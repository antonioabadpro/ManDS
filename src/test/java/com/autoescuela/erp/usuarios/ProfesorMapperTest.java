package com.autoescuela.erp.usuarios;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.autoescuela.erp.core.enums.Rol;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.enums.TipoTurno;
import com.autoescuela.erp.flota.model.Vehiculo;
import com.autoescuela.erp.usuarios.dto.AltaProfesorDTO;
import com.autoescuela.erp.usuarios.dto.EditarPerfilProfesorDTO;
import com.autoescuela.erp.usuarios.dto.ProfesorResumenDTO;
import com.autoescuela.erp.usuarios.mapper.ProfesorMapper;
import com.autoescuela.erp.usuarios.model.Profesor;

import static org.assertj.core.api.Assertions.assertThat;

class ProfesorMapperTest
{
    private final ProfesorMapper mapper = Mappers.getMapper(ProfesorMapper.class);

    @Test
    @DisplayName("toProfesor mapea correctamente todos los campos desde AltaProfesorDTO")
    void testToProfesor()
    {
        AltaProfesorDTO dto = new AltaProfesorDTO(
                "Lucía",
                "Pérez Gómez",
                "98765432W",
                "655443322",
                "lucia.profesor@autoescuela.es",
                LocalDate.of(1987, 6, 15),
                "Calle Toledo 45, Madrid",
                LocalDate.of(2026, 9, 1),
                TipoTurno.TARDE,
                null,
                List.of(TipoCarnet.PERMISO_B, TipoCarnet.PERMISO_A2)
        );

        Profesor profesor = this.mapper.toProfesor(dto);

        assertThat(profesor).isNotNull();
        assertThat(profesor.getNombre()).isEqualTo("Lucía");
        assertThat(profesor.getApellidos()).isEqualTo("Pérez Gómez");
        assertThat(profesor.getDni()).isEqualTo("98765432W");
        assertThat(profesor.getCorreo()).isEqualTo("lucia.profesor@autoescuela.es");
        assertThat(profesor.getTelefono()).isEqualTo("655443322");
        assertThat(profesor.getDireccion()).isEqualTo("Calle Toledo 45, Madrid");
        assertThat(profesor.getFechaNacimiento()).isEqualTo(LocalDate.of(1987, 6, 15));
        assertThat(profesor.getFechaContratacion()).isEqualTo(LocalDate.of(2026, 9, 1));
        assertThat(profesor.getTurno()).isEqualTo(TipoTurno.TARDE);
        assertThat(profesor.getListaTiposCarnet()).containsExactly(TipoCarnet.PERMISO_B, TipoCarnet.PERMISO_A2);
        assertThat(profesor.getRol()).isEqualTo(Rol.PROFESOR);

        // Los campos de negocio/seguridad deben quedar sin inicializar para que los gestione el servicio
        assertThat(profesor.getId()).isNull();
        assertThat(profesor.getNombreUsuario()).isNull();
        assertThat(profesor.getPassword()).isNull();
        assertThat(profesor.getVehiculo()).isNull();
    }

    @Test
    @DisplayName("toProfesor retorna null cuando el DTO es null")
    void testToProfesorNull()
    {
        assertThat(this.mapper.toProfesor(null)).isNull();
    }

    @Test
    @DisplayName("antiguedadTexto calcula correctamente la antigüedad del profesor")
    void testAntiguedadTexto()
    {
        EditarPerfilProfesorDTO dtoSinFecha = new EditarPerfilProfesorDTO();
        assertThat(dtoSinFecha.antiguedadTexto()).isNull();

        EditarPerfilProfesorDTO dtoMenosDeUnAnio = new EditarPerfilProfesorDTO(
                1L, "Laura", "Sánchez", "600111222", "Calle Mayor 1", LocalDate.of(1990, 1, 1),
                "12345678Z", "laura.profesor", "laura@autoescuela.es", TipoTurno.MATINAL,
                LocalDate.now().minusMonths(6), "1234-BBB", "SEAT Ibiza", List.of(TipoCarnet.PERMISO_B)
        );
        assertThat(dtoMenosDeUnAnio.antiguedadTexto()).isEqualTo("menos de 1 año");

        EditarPerfilProfesorDTO dtoUnAnio = new EditarPerfilProfesorDTO(
                1L, "Laura", "Sánchez", "600111222", "Calle Mayor 1", LocalDate.of(1990, 1, 1),
                "12345678Z", "laura.profesor", "laura@autoescuela.es", TipoTurno.MATINAL,
                LocalDate.now().minusYears(1).minusMonths(1), "1234-BBB", "SEAT Ibiza", List.of(TipoCarnet.PERMISO_B)
        );
        assertThat(dtoUnAnio.antiguedadTexto()).isEqualTo("1 año");

        EditarPerfilProfesorDTO dtoVariosAnios = new EditarPerfilProfesorDTO(
                1L, "Laura", "Sánchez", "600111222", "Calle Mayor 1", LocalDate.of(1990, 1, 1),
                "12345678Z", "laura.profesor", "laura@autoescuela.es", TipoTurno.MATINAL,
                LocalDate.now().minusYears(3), "1234-BBB", "SEAT Ibiza", List.of(TipoCarnet.PERMISO_B)
        );
        assertThat(dtoVariosAnios.antiguedadTexto()).isEqualTo("3 años");
    }

    @Test
    @DisplayName("toProfesorResumenDTO mapea correctamente el tipo de permiso y descripción del vehículo asignado")
    void testToProfesorResumenDTOConVehiculo()
    {
        Profesor profesor = new Profesor();
        profesor.setNombre("Carlos");
        profesor.setApellidos("Gómez");
        profesor.setFechaContratacion(LocalDate.of(2024, 1, 15));
        profesor.setTurno(TipoTurno.MATINAL);

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setMarca("SEAT");
        vehiculo.setModelo("Ibiza");
        vehiculo.setMatricula("1234-LMN");
        vehiculo.setTipoPermiso(TipoCarnet.PERMISO_B);
        profesor.setVehiculo(vehiculo);

        ProfesorResumenDTO dto = this.mapper.toProfesorResumenDTO(profesor);

        assertThat(dto).isNotNull();
        assertThat(dto.nombreCompleto()).isEqualTo("Carlos Gómez");
        assertThat(dto.vehiculoMatricula()).isEqualTo("1234-LMN");
        assertThat(dto.vehiculoModelo()).isEqualTo("SEAT Ibiza");
        assertThat(dto.vehiculoTipo()).isEqualTo(TipoCarnet.PERMISO_B);
        assertThat(dto.vehiculoTipoDescripcion()).isEqualTo("Permiso B");
    }

    @Test
    @DisplayName("toProfesorResumenDTO asigna campos nulos de vehículo cuando el profesor no tiene vehículo asignado")
    void testToProfesorResumenDTOSinVehiculo()
    {
        Profesor profesor = new Profesor();
        profesor.setNombre("Ana");
        profesor.setApellidos("López");

        ProfesorResumenDTO dto = this.mapper.toProfesorResumenDTO(profesor);

        assertThat(dto).isNotNull();
        assertThat(dto.vehiculoId()).isNull();
        assertThat(dto.vehiculoMatricula()).isNull();
        assertThat(dto.vehiculoModelo()).isNull();
        assertThat(dto.vehiculoTipo()).isNull();
        assertThat(dto.vehiculoTipoDescripcion()).isNull();
        assertThat(dto.vehiculoDescripcion()).isEqualTo("Sin vehículo asignado");
    }
}

