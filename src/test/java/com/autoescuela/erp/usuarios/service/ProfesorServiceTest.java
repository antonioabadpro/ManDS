package com.autoescuela.erp.usuarios.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.autoescuela.erp.auth.model.TokenVerificacion;
import com.autoescuela.erp.auth.service.TokenVerificacionService;
import com.autoescuela.erp.core.email.EmailService;
import com.autoescuela.erp.core.enums.EstadoClase;
import com.autoescuela.erp.core.enums.EstadoUsuario;
import com.autoescuela.erp.core.enums.EstadoVehiculo;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.enums.TipoTurno;
import com.autoescuela.erp.core.util.FechaHoraUtil;
import com.autoescuela.erp.flota.model.Vehiculo;
import com.autoescuela.erp.flota.repository.VehiculoRepository;
import com.autoescuela.erp.practicas.repository.ClasePracticaRepository;
import com.autoescuela.erp.usuarios.dto.AltaProfesorDTO;
import com.autoescuela.erp.usuarios.dto.BajaProfesorDTO;
import com.autoescuela.erp.usuarios.dto.EditarProfesorDTO;
import com.autoescuela.erp.usuarios.mapper.ProfesorMapper;
import com.autoescuela.erp.usuarios.model.Profesor;
import com.autoescuela.erp.usuarios.repository.AlumnoRepository;
import com.autoescuela.erp.usuarios.repository.PersonaRepository;
import com.autoescuela.erp.usuarios.repository.ProfesorRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios de ProfesorService (Reglas de Flota y Mantenimiento)")
class ProfesorServiceTest
{
    @Mock
    private UsuarioService usuarioService;

    @Mock
    private ProfesorRepository profesorRepository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private VehiculoRepository vehiculoRepository;

    @Mock
    private AlumnoRepository alumnoRepository;

    @Mock
    private ClasePracticaRepository clasePracticaRepository;

    @Mock
    private TokenVerificacionService tokenVerificacionService;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ProfesorMapper profesorMapper;

    @InjectMocks
    private ProfesorService profesorService;

    @Test
    @DisplayName("darBajaProfesor desvincula el vehículo pero conserva su estado MANTENIMIENTO si está en taller")
    void testDarBajaProfesorConVehiculoEnMantenimientoPreservaEstadoMantenimiento()
    {
        Long profesorId = 100L;
        Profesor profesor = this.crearProfesorBase(profesorId);

        Vehiculo vehiculo = new Vehiculo();
        ReflectionTestUtils.setField(vehiculo, "id", 10L);
        vehiculo.setEstado(EstadoVehiculo.MANTENIMIENTO);
        vehiculo.setProfesor(profesor);
        profesor.setVehiculo(vehiculo);

        when(this.profesorRepository.findById(profesorId)).thenReturn(Optional.of(profesor));
        when(this.clasePracticaRepository.findByProfesorAndEstadoClase(profesor, EstadoClase.PENDIENTE)).thenReturn(List.of());
        when(this.alumnoRepository.findByProfesor(profesor)).thenReturn(List.of());
        when(this.profesorRepository.save(any(Profesor.class))).thenAnswer(inv -> inv.getArgument(0));

        Profesor resultado = this.profesorService.darBajaProfesor(new BajaProfesorDTO(profesorId, "SIN_PROFESOR", null));

        assertEquals(EstadoUsuario.INACTIVO, resultado.getEstado());
        assertNull(resultado.getVehiculo());
        assertNull(vehiculo.getProfesor());
        assertEquals(EstadoVehiculo.MANTENIMIENTO, vehiculo.getEstado());
        verify(this.vehiculoRepository).save(vehiculo);
    }

    @Test
    @DisplayName("darBajaProfesor pasa el vehículo a DISPONIBLE si estaba OCUPADO")
    void testDarBajaProfesorConVehiculoOcupadoLoLiberaADisponible()
    {
        Long profesorId = 101L;
        Profesor profesor = this.crearProfesorBase(profesorId);

        Vehiculo vehiculo = new Vehiculo();
        ReflectionTestUtils.setField(vehiculo, "id", 11L);
        vehiculo.setEstado(EstadoVehiculo.OCUPADO);
        vehiculo.setProfesor(profesor);
        profesor.setVehiculo(vehiculo);

        when(this.profesorRepository.findById(profesorId)).thenReturn(Optional.of(profesor));
        when(this.clasePracticaRepository.findByProfesorAndEstadoClase(profesor, EstadoClase.PENDIENTE)).thenReturn(List.of());
        when(this.alumnoRepository.findByProfesor(profesor)).thenReturn(List.of());
        when(this.profesorRepository.save(any(Profesor.class))).thenAnswer(inv -> inv.getArgument(0));

        Profesor resultado = this.profesorService.darBajaProfesor(new BajaProfesorDTO(profesorId, "SIN_PROFESOR", null));

        assertEquals(EstadoUsuario.INACTIVO, resultado.getEstado());
        assertNull(resultado.getVehiculo());
        assertNull(vehiculo.getProfesor());
        assertEquals(EstadoVehiculo.DISPONIBLE, vehiculo.getEstado());
        verify(this.vehiculoRepository).save(vehiculo);
    }

    @Test
    @DisplayName("modificarProfesor al desvincular vehículo en MANTENIMIENTO preserva su estado MANTENIMIENTO")
    void testModificarProfesorDesvinculaVehiculoEnMantenimientoConservandoEstado()
    {
        Long profesorId = 102L;
        Profesor profesor = this.crearProfesorBase(profesorId);

        Vehiculo vehiculoActual = new Vehiculo();
        ReflectionTestUtils.setField(vehiculoActual, "id", 10L);
        vehiculoActual.setEstado(EstadoVehiculo.MANTENIMIENTO);
        vehiculoActual.setTipoPermiso(TipoCarnet.PERMISO_B);
        vehiculoActual.setProfesor(profesor);
        profesor.setVehiculo(vehiculoActual);

        EditarProfesorDTO dto = this.crearEditarProfesorDTO(profesor, null);

        when(this.profesorRepository.findById(profesorId)).thenReturn(Optional.of(profesor));
        when(this.personaRepository.existsByDniAndIdNot("23456789D", profesorId)).thenReturn(false);
        when(this.personaRepository.existsByCorreoAndIdNot("profe@autoescuela.es", profesorId)).thenReturn(false);
        when(this.personaRepository.existsByTelefonoAndIdNot("600111222", profesorId)).thenReturn(false);
        when(this.clasePracticaRepository.countByProfesorAndEstadoClase(profesor, EstadoClase.PENDIENTE)).thenReturn(0);
        when(this.profesorRepository.save(any(Profesor.class))).thenAnswer(inv -> inv.getArgument(0));

        Profesor resultado = this.profesorService.modificarProfesor(dto);

        assertNull(resultado.getVehiculo());
        assertNull(vehiculoActual.getProfesor());
        assertEquals(EstadoVehiculo.MANTENIMIENTO, vehiculoActual.getEstado());
        verify(this.vehiculoRepository).save(vehiculoActual);
    }

    @Test
    @DisplayName("modificarProfesor al cambiar de vehículo preserva MANTENIMIENTO en el vehículo anterior y asigna el nuevo como OCUPADO")
    void testModificarProfesorSustituyeVehiculoEnMantenimientoConservandoEstadoAnterior()
    {
        Long profesorId = 103L;
        Profesor profesor = this.crearProfesorBase(profesorId);

        Vehiculo vehiculoAnterior = new Vehiculo();
        ReflectionTestUtils.setField(vehiculoAnterior, "id", 10L);
        vehiculoAnterior.setMatricula("1111-BBB");
        vehiculoAnterior.setEstado(EstadoVehiculo.MANTENIMIENTO);
        vehiculoAnterior.setTipoPermiso(TipoCarnet.PERMISO_B);
        vehiculoAnterior.setProfesor(profesor);
        profesor.setVehiculo(vehiculoAnterior);

        Vehiculo vehiculoNuevo = new Vehiculo();
        ReflectionTestUtils.setField(vehiculoNuevo, "id", 20L);
        vehiculoNuevo.setMatricula("2222-CCC");
        vehiculoNuevo.setEstado(EstadoVehiculo.DISPONIBLE);
        vehiculoNuevo.setTipoPermiso(TipoCarnet.PERMISO_B);

        EditarProfesorDTO dto = this.crearEditarProfesorDTO(profesor, 20L);

        when(this.profesorRepository.findById(profesorId)).thenReturn(Optional.of(profesor));
        when(this.personaRepository.existsByDniAndIdNot("23456789D", profesorId)).thenReturn(false);
        when(this.personaRepository.existsByCorreoAndIdNot("profe@autoescuela.es", profesorId)).thenReturn(false);
        when(this.personaRepository.existsByTelefonoAndIdNot("600111222", profesorId)).thenReturn(false);
        when(this.clasePracticaRepository.countByProfesorAndEstadoClase(profesor, EstadoClase.PENDIENTE)).thenReturn(0);
        when(this.vehiculoRepository.findById(20L)).thenReturn(Optional.of(vehiculoNuevo));
        when(this.profesorRepository.save(any(Profesor.class))).thenAnswer(inv -> inv.getArgument(0));

        Profesor resultado = this.profesorService.modificarProfesor(dto);

        assertEquals(vehiculoNuevo, resultado.getVehiculo());
        assertNull(vehiculoAnterior.getProfesor());
        assertEquals(EstadoVehiculo.MANTENIMIENTO, vehiculoAnterior.getEstado());
        assertEquals(profesor, vehiculoNuevo.getProfesor());
        assertEquals(EstadoVehiculo.OCUPADO, vehiculoNuevo.getEstado());
        verify(this.vehiculoRepository).save(vehiculoAnterior);
        verify(this.vehiculoRepository).save(vehiculoNuevo);
    }

    @Test
    @DisplayName("darAltaProfesor normaliza barras finales de baseUrl y usa appBaseUrl de respaldo cuando baseUrl viene vacía")
    void darAltaProfesor_normalizaBarrasFinalesYRespaldoEnEnlaceActivacion()
    {
        ReflectionTestUtils.setField(this.profesorService, "appBaseUrl", "https://fallback.mands.es///");

        AltaProfesorDTO dto = new AltaProfesorDTO(
                "Carlos",
                "Ruiz",
                "23456789D",
                "600111222",
                "profe@autoescuela.es",
                LocalDate.of(1985, 5, 10),
                "Calle Mayor 1",
                FechaHoraUtil.hoy(),
                TipoTurno.MATINAL,
                null,
                List.of(TipoCarnet.PERMISO_B)
        );

        Profesor profesorMapeado = this.crearProfesorBase(10L);
        TokenVerificacion token = new TokenVerificacion("tok-abc", profesorMapeado, FechaHoraUtil.ahora().plusMinutes(60));

        when(this.personaRepository.existsByDni("23456789D")).thenReturn(false);
        when(this.usuarioService.esLetraDniValida("23456789D")).thenReturn(true);
        when(this.personaRepository.existsByCorreo("profe@autoescuela.es")).thenReturn(false);
        when(this.personaRepository.existsByTelefono("600111222")).thenReturn(false);
        when(this.profesorMapper.toProfesor(dto)).thenReturn(profesorMapeado);
        when(this.passwordEncoder.encode(anyString())).thenReturn("hash-temporal");
        when(this.profesorRepository.save(any(Profesor.class))).thenAnswer(inv -> inv.getArgument(0));
        when(this.tokenVerificacionService.generarTokenRecuperacion(profesorMapeado, 60)).thenReturn(token);

        this.profesorService.darAltaProfesor(dto, "https://mands.onrender.com///");
        this.profesorService.darAltaProfesor(dto, null);

        verify(this.emailService).enviarInvitacionProfesor(
                eq("profe@autoescuela.es"),
                eq("Carlos"),
                eq("https://mands.onrender.com/activar-cuenta?token=tok-abc"),
                eq("60")
        );
        verify(this.emailService).enviarInvitacionProfesor(
                eq("profe@autoescuela.es"),
                eq("Carlos"),
                eq("https://fallback.mands.es/activar-cuenta?token=tok-abc"),
                eq("60")
        );
    }

    private Profesor crearProfesorBase(Long id)
    {
        Profesor profesor = new Profesor();
        ReflectionTestUtils.setField(profesor, "id", id);
        profesor.setNombre("Carlos");
        profesor.setApellidos("Ruiz");
        profesor.setDni("23456789D");
        profesor.setCorreo("profe@autoescuela.es");
        profesor.setTelefono("600111222");
        profesor.setDireccion("Calle Mayor 1");
        profesor.setFechaNacimiento(LocalDate.of(1985, 5, 10));
        profesor.setFechaContratacion(LocalDate.of(2025, 1, 15));
        profesor.setTurno(TipoTurno.MATINAL);
        profesor.setEstado(EstadoUsuario.ACTIVO);
        profesor.setListaTiposCarnet(List.of(TipoCarnet.PERMISO_B));
        return profesor;
    }

    private EditarProfesorDTO crearEditarProfesorDTO(Profesor profesor, Long vehiculoId)
    {
        return new EditarProfesorDTO(
                profesor.getId(),
                profesor.getNombre(),
                profesor.getApellidos(),
                profesor.getDni(),
                profesor.getTelefono(),
                profesor.getCorreo(),
                profesor.getFechaNacimiento(),
                profesor.getDireccion(),
                profesor.getFechaContratacion(),
                profesor.getTurno(),
                vehiculoId,
                List.of(TipoCarnet.PERMISO_B)
        );
    }
}
