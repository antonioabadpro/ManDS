package com.autoescuela.erp.usuarios;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.autoescuela.erp.core.enums.Rol;
import com.autoescuela.erp.usuarios.dto.EditarPerfilAdminDTO;
import com.autoescuela.erp.usuarios.mapper.UsuarioMapper;
import com.autoescuela.erp.usuarios.model.Administrador;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioMapperTest
{
    private final UsuarioMapper mapper = Mappers.getMapper(UsuarioMapper.class);

    @Test
    @DisplayName("toEditarPerfilAdminDTO mapea correctamente todos los campos y el rol polimórfico")
    void testToEditarPerfilAdminDTO()
    {
        Administrador admin = new Administrador();
        admin.setNombre("Carlos");
        admin.setApellidos("García Moreno");
        admin.setDni("12345678Z");
        admin.setCorreo("admin@mands.com");
        admin.setNombreUsuario("admin");
        admin.setTelefono("600112233");
        admin.setDireccion("Calle Mayor 1");
        admin.setFechaNacimiento(LocalDate.of(1985, 4, 12));

        EditarPerfilAdminDTO dto = this.mapper.toEditarPerfilAdminDTO(admin);

        assertThat(dto).isNotNull();
        assertThat(dto.getNombre()).isEqualTo("Carlos");
        assertThat(dto.getApellidos()).isEqualTo("García Moreno");
        assertThat(dto.getDni()).isEqualTo("12345678Z");
        assertThat(dto.getCorreo()).isEqualTo("admin@mands.com");
        assertThat(dto.getNombreUsuario()).isEqualTo("admin");
        assertThat(dto.getTelefono()).isEqualTo("600112233");
        assertThat(dto.getDireccion()).isEqualTo("Calle Mayor 1");
        assertThat(dto.getFechaNacimiento()).isEqualTo(LocalDate.of(1985, 4, 12));
        assertThat(dto.getRol()).isEqualTo(Rol.ADMIN.name());
    }

    @Test
    @DisplayName("toEditarPerfilAdminDTO retorna null cuando la entidad es null")
    void testToEditarPerfilAdminDTONull()
    {
        assertThat(this.mapper.toEditarPerfilAdminDTO(null)).isNull();
    }
}

