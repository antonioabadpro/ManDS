package com.autoescuela.erp.usuarios;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.autoescuela.erp.core.enums.EstadoUsuario;
import com.autoescuela.erp.core.excepciones.RecursoNoEncontradoException;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.core.util.FechaHoraUtil;
import com.autoescuela.erp.usuarios.dto.EditarPerfilAdminDTO;
import com.autoescuela.erp.usuarios.mapper.UsuarioMapper;
import com.autoescuela.erp.usuarios.model.Administrador;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.repository.AlumnoRepository;
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
    private AlumnoRepository alumnoRepository;

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
                .dni("12345678Z")
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
                .dni("12345678Z")
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

    @Test
    @DisplayName("existeNombreUsuario comprueba existencia y maneja nulos o blancos")
    void testExisteNombreUsuario()
    {
        when(this.personaRepository.existsByNombreUsuario("carlos")).thenReturn(true);
        when(this.personaRepository.existsByNombreUsuario("pedro")).thenReturn(false);

        assertThat(this.usuarioService.existeNombreUsuario("carlos")).isTrue();
        assertThat(this.usuarioService.existeNombreUsuario("pedro")).isFalse();
        assertThat(this.usuarioService.existeNombreUsuario(null)).isFalse();
        assertThat(this.usuarioService.existeNombreUsuario("   ")).isFalse();
    }

    @Test
    @DisplayName("existeCorreo comprueba existencia y maneja nulos o blancos")
    void testExisteCorreo()
    {
        when(this.personaRepository.existsByCorreo("carlos@correo.es")).thenReturn(true);
        when(this.personaRepository.existsByCorreo("nuevo@correo.es")).thenReturn(false);

        assertThat(this.usuarioService.existeCorreo("carlos@correo.es")).isTrue();
        assertThat(this.usuarioService.existeCorreo("nuevo@correo.es")).isFalse();
        assertThat(this.usuarioService.existeCorreo(null)).isFalse();
        assertThat(this.usuarioService.existeCorreo("   ")).isFalse();
    }

    @Test
    @DisplayName("existeDni comprueba existencia y maneja nulos o blancos")
    void testExisteDni()
    {
        when(this.personaRepository.existsByDni("12345678Z")).thenReturn(true);
        when(this.personaRepository.existsByDni("87654321A")).thenReturn(false);

        assertThat(this.usuarioService.existeDni("12345678Z")).isTrue();
        assertThat(this.usuarioService.existeDni("87654321A")).isFalse();
        assertThat(this.usuarioService.existeDni(null)).isFalse();
        assertThat(this.usuarioService.existeDni("   ")).isFalse();
    }

    @Test
    @DisplayName("existeTelefono comprueba existencia y maneja nulos o blancos")
    void testExisteTelefono()
    {
        when(this.personaRepository.existsByTelefono("600111222")).thenReturn(true);
        when(this.personaRepository.existsByTelefono("699888777")).thenReturn(false);

        assertThat(this.usuarioService.existeTelefono("600111222")).isTrue();
        assertThat(this.usuarioService.existeTelefono("699888777")).isFalse();
        assertThat(this.usuarioService.existeTelefono(null)).isFalse();
        assertThat(this.usuarioService.existeTelefono("   ")).isFalse();
    }

    @Test
    @DisplayName("esDniValido valida correctamente DNIs y NIEs mediante algoritmo módulo 23")
    void testEsDniValido()
    {
        // DNIs válidos
        assertThat(this.usuarioService.esLetraDniValida("12345678Z")).isTrue();
        assertThat(this.usuarioService.esLetraDniValida("00000000T")).isTrue();
        assertThat(this.usuarioService.esLetraDniValida("87654321X")).isTrue();
        assertThat(this.usuarioService.esLetraDniValida(" 12345678z ")).isTrue(); // Soporta minúsculas y espacios

        // NIEs válidos (X=0, Y=1, Z=2)
        assertThat(this.usuarioService.esLetraDniValida("X1234567L")).isTrue();
        assertThat(this.usuarioService.esLetraDniValida("Y1234567X")).isTrue();
        assertThat(this.usuarioService.esLetraDniValida("Z1234567R")).isTrue();

        // DNIs con letra errónea
        assertThat(this.usuarioService.esLetraDniValida("12345678A")).isFalse();
        assertThat(this.usuarioService.esLetraDniValida("00000000A")).isFalse();
        assertThat(this.usuarioService.esLetraDniValida("X1234567A")).isFalse();

        // Formatos inválidos o nulos
        assertThat(this.usuarioService.esLetraDniValida(null)).isFalse();
        assertThat(this.usuarioService.esLetraDniValida("")).isFalse();
        assertThat(this.usuarioService.esLetraDniValida("   ")).isFalse();
        assertThat(this.usuarioService.esLetraDniValida("12345678")).isFalse();
        assertThat(this.usuarioService.esLetraDniValida("123456789Z")).isFalse();
        assertThat(this.usuarioService.esLetraDniValida("A1234567Z")).isFalse();
    }

    @Test
    @DisplayName("existe* purga alumnos huérfanos INACTIVOS sin matrículas y métodos OtroUsuario delegan con o sin ID")
    void testPurgaHuerfanosYValidacionesOtroUsuario()
    {
        Alumno huerfano = new Alumno();
        huerfano.setEstado(EstadoUsuario.INACTIVO);
        huerfano.setHistorialMatriculas(List.of());

        when(this.alumnoRepository.findByNombreUsuario("huerfano")).thenReturn(Optional.of(huerfano));
        when(this.alumnoRepository.findByCorreo("h@m.es")).thenReturn(Optional.of(huerfano));
        when(this.alumnoRepository.findByDni("12345678Z")).thenReturn(Optional.of(huerfano));
        when(this.alumnoRepository.findByTelefono("600111222")).thenReturn(Optional.of(huerfano));

        assertThat(this.usuarioService.existeNombreUsuario("huerfano")).isFalse();
        assertThat(this.usuarioService.existeCorreo("h@m.es")).isFalse();
        assertThat(this.usuarioService.existeDni("12345678Z")).isFalse();
        assertThat(this.usuarioService.existeTelefono("600111222")).isFalse();
        verify(this.alumnoRepository, org.mockito.Mockito.times(4)).delete(huerfano);
        verify(this.alumnoRepository, org.mockito.Mockito.times(4)).flush();

        // Métodos OtroUsuario con null/blank, id nulo e id presente
        assertThat(this.usuarioService.existeCorreoOtroUsuario(null, 1L)).isFalse();
        assertThat(this.usuarioService.existeDniOtroUsuario(" ", 1L)).isFalse();
        assertThat(this.usuarioService.existeTelefonoOtroUsuario(null, 1L)).isFalse();

        assertThat(this.usuarioService.existeCorreoOtroUsuario("h@m.es", null)).isFalse();
        assertThat(this.usuarioService.existeDniOtroUsuario("12345678Z", null)).isFalse();
        assertThat(this.usuarioService.existeTelefonoOtroUsuario("600111222", null)).isFalse();

        when(this.personaRepository.existsByCorreoAndIdNot("c@m.es", 5L)).thenReturn(true);
        when(this.personaRepository.existsByDniAndIdNot("12345678Z", 5L)).thenReturn(true);
        when(this.personaRepository.existsByTelefonoAndIdNot("600111222", 5L)).thenReturn(true);

        assertThat(this.usuarioService.existeCorreoOtroUsuario("C@M.ES", 5L)).isTrue();
        assertThat(this.usuarioService.existeDniOtroUsuario("12345678z", 5L)).isTrue();
        assertThat(this.usuarioService.existeTelefonoOtroUsuario("600111222", 5L)).isTrue();
    }

    @Test
    @DisplayName("obtenerPerfilAdminPorUsername, validaciones DNI/edad en actualizarPerfilAdmin y obtenerAdministradoresActivos")
    void testPerfilAdminValidacionesYListado()
    {
        Administrador admin = new Administrador();
        admin.setDni("12345678Z");
        EditarPerfilAdminDTO dtoAdmin = EditarPerfilAdminDTO.builder().nombre("Admin").build();

        when(this.personaRepository.findByNombreUsuarioOrCorreo("admin", "admin")).thenReturn(Optional.of(admin));
        when(this.usuarioMapper.toEditarPerfilAdminDTO(admin)).thenReturn(dtoAdmin);
        assertThat(this.usuarioService.obtenerPerfilAdminPorUsername("admin")).isSameAs(dtoAdmin);

        when(this.personaRepository.findByNombreUsuarioOrCorreo("noexiste", "noexiste")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> this.usuarioService.obtenerPerfilAdminPorUsername("noexiste"))
                .isInstanceOf(RecursoNoEncontradoException.class);

        when(this.personaRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> this.usuarioService.actualizarPerfilAdmin(1L, EditarPerfilAdminDTO.builder().dni(" ").build()))
                .isInstanceOf(ReglaNegocioException.class);

        assertThatThrownBy(() -> this.usuarioService.actualizarPerfilAdmin(1L, EditarPerfilAdminDTO.builder().dni("99999999A").build()))
                .isInstanceOf(ReglaNegocioException.class);

        when(this.personaRepository.existsByDniAndIdNot("87654321X", 1L)).thenReturn(true);
        assertThatThrownBy(() -> this.usuarioService.actualizarPerfilAdmin(1L, EditarPerfilAdminDTO.builder().dni("87654321X").build()))
                .isInstanceOf(ReglaNegocioException.class);

        LocalDate hoy = FechaHoraUtil.hoy();
        when(this.personaRepository.existsByDniAndIdNot("12345678Z", 1L)).thenReturn(false);
        when(this.personaRepository.existsByTelefonoAndIdNot("600111222", 1L)).thenReturn(false);

        EditarPerfilAdminDTO dtoMenor = EditarPerfilAdminDTO.builder()
                .dni("12345678Z").telefono("600111222").nombre("A").apellidos("B").direccion("C")
                .fechaNacimiento(hoy.minusYears(16)).build();
        assertThatThrownBy(() -> this.usuarioService.actualizarPerfilAdmin(1L, dtoMenor))
                .isInstanceOf(ReglaNegocioException.class);

        EditarPerfilAdminDTO dtoCentenario = EditarPerfilAdminDTO.builder()
                .dni("12345678Z").telefono("600111222").nombre("A").apellidos("B").direccion("C")
                .fechaNacimiento(hoy.minusYears(105)).build();
        assertThatThrownBy(() -> this.usuarioService.actualizarPerfilAdmin(1L, dtoCentenario))
                .isInstanceOf(ReglaNegocioException.class);

        when(this.personaRepository.findAdministradoresActivos()).thenReturn(List.of(admin));
        assertThat(this.usuarioService.obtenerAdministradoresActivos()).containsExactly(admin);
    }
}

