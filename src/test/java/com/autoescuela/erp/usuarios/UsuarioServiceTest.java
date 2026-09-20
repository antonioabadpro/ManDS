package com.autoescuela.erp.usuarios;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.autoescuela.erp.core.excepciones.RecursoNoEncontradoException;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.usuarios.dto.EditarPerfilAdminDTO;
import com.autoescuela.erp.usuarios.mapper.UsuarioMapper;
import com.autoescuela.erp.usuarios.model.Administrador;
import com.autoescuela.erp.usuarios.repository.PersonaRepository;
import com.autoescuela.erp.usuarios.service.UsuarioService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest
{
    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private UsuarioMapper usuarioMapper;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    @DisplayName("obtenerPerfilAdmin recupera la persona y delega la transformación en UsuarioMapper")
    void testObtenerPerfilAdminExito()
    {
        Administrador admin = new Administrador();
        admin.setNombre("Carlos");

        EditarPerfilAdminDTO esperado = EditarPerfilAdminDTO.builder()
                .nombre("Carlos")
                .rol("ADMIN")
                .build();

        when(this.personaRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(this.usuarioMapper.toEditarPerfilAdminDTO(admin)).thenReturn(esperado);

        EditarPerfilAdminDTO resultado = this.usuarioService.obtenerPerfilAdmin(1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getNombre()).isEqualTo("Carlos");
        assertThat(resultado.getRol()).isEqualTo("ADMIN");
        verify(this.usuarioMapper).toEditarPerfilAdminDTO(admin);
    }

    @Test
    @DisplayName("obtenerPerfilAdmin lanza RecursoNoEncontradoException si el usuario no existe")
    void testObtenerPerfilAdminNoEncontrado()
    {
        when(this.personaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.usuarioService.obtenerPerfilAdmin(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("No se encontró el usuario con ID: 99");
    }

    @Test
    @DisplayName("actualizarPerfilAdmin lanza ReglaNegocioException si el teléfono ya está registrado por otro usuario")
    void testActualizarPerfilAdminTelefonoDuplicado()
    {
        Administrador admin = new Administrador();
        when(this.personaRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(this.personaRepository.existsByTelefonoAndIdNot("611223344", 1L)).thenReturn(true);

        EditarPerfilAdminDTO dto = EditarPerfilAdminDTO.builder()
                .telefono("611223344")
                .nombre("Nuevo Nombre")
                .apellidos("Nuevos Apellidos")
                .direccion("Nueva Direccion")
                .fechaNacimiento(LocalDate.of(1990, 1, 1))
                .build();

        assertThatThrownBy(() -> this.usuarioService.actualizarPerfilAdmin(1L, dto))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("El número de teléfono ya se encuentra registrado");
    }

    @Test
    @DisplayName("actualizarPerfilAdmin actualiza los datos y persiste la entidad correctamente")
    void testActualizarPerfilAdminExito()
    {
        Administrador admin = new Administrador();
        when(this.personaRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(this.personaRepository.existsByTelefonoAndIdNot("611223344", 1L)).thenReturn(false);

        EditarPerfilAdminDTO dto = EditarPerfilAdminDTO.builder()
                .telefono("611223344")
                .nombre("Carlos Modificado")
                .apellidos("García Moreno")
                .direccion("Calle Nueva 5")
                .fechaNacimiento(LocalDate.of(1985, 4, 12))
                .build();

        this.usuarioService.actualizarPerfilAdmin(1L, dto);

        verify(this.personaRepository).save(any(Administrador.class));
        assertThat(admin.getNombre()).isEqualTo("Carlos Modificado");
        assertThat(admin.getTelefono()).isEqualTo("611223344");
    }
}

