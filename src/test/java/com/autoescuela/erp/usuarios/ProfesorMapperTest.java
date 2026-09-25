package com.autoescuela.erp.usuarios;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.autoescuela.erp.core.enums.Rol;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.enums.TipoTurno;
import com.autoescuela.erp.usuarios.dto.AltaProfesorDTO;
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
}

