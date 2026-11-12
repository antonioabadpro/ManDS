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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
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
import com.autoescuela.erp.core.excepciones.RecursoNoEncontradoException;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.core.security.UserDetailsImpl;
import com.autoescuela.erp.core.util.FechaHoraUtil;
import com.autoescuela.erp.flota.model.Vehiculo;
import com.autoescuela.erp.flota.repository.VehiculoRepository;
import com.autoescuela.erp.practicas.model.ClasePractica;
import com.autoescuela.erp.practicas.repository.ClasePracticaRepository;
import com.autoescuela.erp.usuarios.dto.AltaProfesorDTO;
import com.autoescuela.erp.usuarios.dto.BajaProfesorDTO;
import com.autoescuela.erp.usuarios.dto.EditarPerfilProfesorDTO;
import com.autoescuela.erp.usuarios.dto.EditarProfesorDTO;
import com.autoescuela.erp.usuarios.dto.ProfesorDetalleDTO;
import com.autoescuela.erp.usuarios.dto.ProfesorResumenDTO;
import com.autoescuela.erp.usuarios.mapper.ProfesorMapper;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.model.Profesor;
import com.autoescuela.erp.usuarios.repository.AlumnoRepository;
import com.autoescuela.erp.usuarios.repository.PersonaRepository;
import com.autoescuela.erp.usuarios.repository.ProfesorRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    @Test
    @DisplayName("darAltaProfesor valida edad, contratación, permisos, unicidad DNI/correo/teléfono y compatibilidad de vehículo")
    void testDarAltaProfesorValidacionesYVehiculo()
    {
        assertThrows(ReglaNegocioException.class, () -> this.profesorService.darAltaProfesor(null, "https://mands.es"));

        LocalDate hoy = FechaHoraUtil.hoy();
        AltaProfesorDTO menorEdad = new AltaProfesorDTO("Ana", "Gil", "12345678Z", "600000001", "a@mands.es",
                hoy.minusYears(17), "Calle 1", hoy, TipoTurno.MATINAL, null, List.of(TipoCarnet.PERMISO_B));
        assertThrows(ReglaNegocioException.class, () -> this.profesorService.darAltaProfesor(menorEdad, "https://mands.es"));

        AltaProfesorDTO muyMayor = new AltaProfesorDTO("Ana", "Gil", "12345678Z", "600000001", "a@mands.es",
                hoy.minusYears(101), "Calle 1", hoy, TipoTurno.MATINAL, null, List.of(TipoCarnet.PERMISO_B));
        assertThrows(ReglaNegocioException.class, () -> this.profesorService.darAltaProfesor(muyMayor, "https://mands.es"));

        AltaProfesorDTO fechaContratoInvalida = new AltaProfesorDTO("Ana", "Gil", "12345678Z", "600000001", "a@mands.es",
                LocalDate.of(1990, 1, 1), "Calle 1", hoy.minusMonths(2), TipoTurno.MATINAL, null, List.of(TipoCarnet.PERMISO_B));
        assertThrows(ReglaNegocioException.class, () -> this.profesorService.darAltaProfesor(fechaContratoInvalida, "https://mands.es"));

        AltaProfesorDTO sinPermisos = new AltaProfesorDTO("Ana", "Gil", "12345678Z", "600000001", "a@mands.es",
                LocalDate.of(1990, 1, 1), "Calle 1", hoy, TipoTurno.MATINAL, null, List.of());
        assertThrows(ReglaNegocioException.class, () -> this.profesorService.darAltaProfesor(sinPermisos, "https://mands.es"));

        AltaProfesorDTO valido = new AltaProfesorDTO("Ana", "Gil", "12345678Z", "600000001", "a@mands.es",
                LocalDate.of(1990, 1, 1), "Calle 1", hoy, TipoTurno.MATINAL, 55L, List.of(TipoCarnet.PERMISO_B));

        when(this.personaRepository.existsByDni("12345678Z")).thenReturn(true);
        assertThrows(ReglaNegocioException.class, () -> this.profesorService.darAltaProfesor(valido, "https://mands.es"));

        when(this.personaRepository.existsByDni("12345678Z")).thenReturn(false);
        when(this.usuarioService.esLetraDniValida("12345678Z")).thenReturn(false);
        assertThrows(ReglaNegocioException.class, () -> this.profesorService.darAltaProfesor(valido, "https://mands.es"));

        when(this.usuarioService.esLetraDniValida("12345678Z")).thenReturn(true);
        when(this.personaRepository.existsByCorreo("a@mands.es")).thenReturn(true);
        assertThrows(ReglaNegocioException.class, () -> this.profesorService.darAltaProfesor(valido, "https://mands.es"));

        when(this.personaRepository.existsByCorreo("a@mands.es")).thenReturn(false);
        when(this.personaRepository.existsByTelefono("600000001")).thenReturn(true);
        assertThrows(ReglaNegocioException.class, () -> this.profesorService.darAltaProfesor(valido, "https://mands.es"));

        when(this.personaRepository.existsByTelefono("600000001")).thenReturn(false);
        Vehiculo ocupado = new Vehiculo();
        ocupado.setMatricula("1111BBB");
        ocupado.setProfesor(new Profesor());
        when(this.vehiculoRepository.findById(55L)).thenReturn(Optional.of(ocupado));
        assertThrows(ReglaNegocioException.class, () -> this.profesorService.darAltaProfesor(valido, "https://mands.es"));

        Vehiculo incompatible = new Vehiculo();
        incompatible.setMatricula("2222CCC");
        incompatible.setTipoPermiso(TipoCarnet.PERMISO_C);
        when(this.vehiculoRepository.findById(55L)).thenReturn(Optional.of(incompatible));
        assertThrows(ReglaNegocioException.class, () -> this.profesorService.darAltaProfesor(valido, "https://mands.es"));

        Vehiculo disponible = new Vehiculo();
        disponible.setMatricula("3333DDD");
        disponible.setTipoPermiso(TipoCarnet.PERMISO_B);
        disponible.setEstado(EstadoVehiculo.DISPONIBLE);
        Profesor mapeado = this.crearProfesorBase(200L);
        mapeado.setListaTiposCarnet(null);

        when(this.vehiculoRepository.findById(55L)).thenReturn(Optional.of(disponible));
        when(this.profesorMapper.toProfesor(valido)).thenReturn(mapeado);
        when(this.passwordEncoder.encode(anyString())).thenReturn("hash");
        when(this.profesorRepository.save(any(Profesor.class))).thenAnswer(i -> i.getArgument(0));
        TokenVerificacion tok = new TokenVerificacion("tok-55", mapeado, FechaHoraUtil.ahora().plusMinutes(60));
        when(this.tokenVerificacionService.generarTokenRecuperacion(any(), eq(60))).thenReturn(tok);

        Profesor resultado = this.profesorService.darAltaProfesor(valido, "https://mands.es");
        assertEquals(EstadoVehiculo.OCUPADO, disponible.getEstado());
        assertNotNull(resultado.getListaTiposCarnet());
        assertSame(disponible, resultado.getVehiculo());
    }

    @Test
    @DisplayName("obtenerProfesorParaEdicion, obtenerProfesorParaDetalle y tieneClasesPracticasPendientes cubren validaciones y nulos")
    void testConsultasEdicionYDetalle()
    {
        assertThrows(ReglaNegocioException.class, () -> this.profesorService.obtenerProfesorParaEdicion(null));
        assertThrows(ReglaNegocioException.class, () -> this.profesorService.obtenerProfesorParaDetalle(null));
        assertFalse(this.profesorService.tieneClasesPracticasPendientes(null));

        Profesor inactivo = this.crearProfesorBase(10L);
        inactivo.setEstado(EstadoUsuario.INACTIVO);
        when(this.profesorRepository.findById(10L)).thenReturn(Optional.of(inactivo));
        assertThrows(ReglaNegocioException.class, () -> this.profesorService.obtenerProfesorParaEdicion(10L));

        Profesor activo = this.crearProfesorBase(11L);
        EditarProfesorDTO editarDTO = this.crearEditarProfesorDTO(activo, null);
        when(this.profesorRepository.findById(11L)).thenReturn(Optional.of(activo));
        when(this.profesorMapper.toEditarProfesorDTO(activo)).thenReturn(editarDTO);
        when(this.profesorMapper.toProfesorDetalleDTO(activo)).thenReturn(
                new ProfesorDetalleDTO(11L, "Carlos", "Ruiz", "Carlos Ruiz", "CR", "cruiz", "23456789D", "c@m.es", "600111222",
                        LocalDate.of(1985, 5, 10), 40, "Calle 1", LocalDate.of(2025, 1, 15), 2025, 1,
                        TipoTurno.MATINAL, 1L, "1234ABC", "Polo", TipoCarnet.PERMISO_B, "Permiso B",
                        List.of(TipoCarnet.PERMISO_B), 2, 1, EstadoUsuario.ACTIVO));
        when(this.clasePracticaRepository.countByProfesorAndEstadoClase(activo, EstadoClase.PENDIENTE)).thenReturn(1);
        when(this.profesorRepository.findById(99L)).thenReturn(Optional.empty());

        assertEquals(editarDTO, this.profesorService.obtenerProfesorParaEdicion(11L));
        assertNotNull(this.profesorService.obtenerProfesorParaDetalle(11L));
        assertTrue(this.profesorService.tieneClasesPracticasPendientes(11L));
        assertFalse(this.profesorService.tieneClasesPracticasPendientes(99L));
    }

    @Test
    @DisplayName("modificarProfesor valida reglas de negocio y gestiona asignación/desvinculación de vehículos")
    void testModificarProfesorValidacionesYFlota()
    {
        assertThrows(ReglaNegocioException.class, () -> this.profesorService.modificarProfesor(null));
        assertThrows(ReglaNegocioException.class, () -> this.profesorService.modificarProfesor(
                new EditarProfesorDTO(null, "A", "B", "12345678Z", "600000000", "a@b.es", LocalDate.of(1990, 1, 1), "Dir", LocalDate.now(), TipoTurno.MATINAL, null, List.of(TipoCarnet.PERMISO_B))));

        Profesor profesor = this.crearProfesorBase(105L);
        profesor.setEstado(EstadoUsuario.INACTIVO);
        when(this.profesorRepository.findById(105L)).thenReturn(Optional.of(profesor));
        assertThrows(ReglaNegocioException.class, () -> this.profesorService.modificarProfesor(this.crearEditarProfesorDTO(profesor, null)));

        profesor.setEstado(EstadoUsuario.ACTIVO);
        EditarProfesorDTO menor = new EditarProfesorDTO(105L, "Carlos", "Ruiz", "23456789D", "600111222",
                "profe@autoescuela.es", FechaHoraUtil.hoy().minusYears(16), "Calle", profesor.getFechaContratacion(),
                TipoTurno.MATINAL, null, List.of(TipoCarnet.PERMISO_B));
        assertThrows(ReglaNegocioException.class, () -> this.profesorService.modificarProfesor(menor));

        EditarProfesorDTO centenario = new EditarProfesorDTO(105L, "Carlos", "Ruiz", "23456789D", "600111222",
                "profe@autoescuela.es", FechaHoraUtil.hoy().minusYears(105), "Calle", profesor.getFechaContratacion(),
                TipoTurno.MATINAL, null, List.of(TipoCarnet.PERMISO_B));
        assertThrows(ReglaNegocioException.class, () -> this.profesorService.modificarProfesor(centenario));

        EditarProfesorDTO contratoDistinto = new EditarProfesorDTO(105L, "Carlos", "Ruiz", "23456789D", "600111222",
                "profe@autoescuela.es", profesor.getFechaNacimiento(), "Calle", profesor.getFechaContratacion().plusDays(1),
                TipoTurno.MATINAL, null, List.of(TipoCarnet.PERMISO_B));
        assertThrows(ReglaNegocioException.class, () -> this.profesorService.modificarProfesor(contratoDistinto));

        EditarProfesorDTO sinPermisos = new EditarProfesorDTO(105L, "Carlos", "Ruiz", "23456789D", "600111222",
                "profe@autoescuela.es", profesor.getFechaNacimiento(), "Calle", profesor.getFechaContratacion(),
                TipoTurno.MATINAL, null, List.of());
        assertThrows(ReglaNegocioException.class, () -> this.profesorService.modificarProfesor(sinPermisos));

        EditarProfesorDTO baseDto = this.crearEditarProfesorDTO(profesor, null);
        when(this.personaRepository.existsByDniAndIdNot("23456789D", 105L)).thenReturn(true);
        assertThrows(ReglaNegocioException.class, () -> this.profesorService.modificarProfesor(baseDto));

        when(this.personaRepository.existsByDniAndIdNot("23456789D", 105L)).thenReturn(false);
        when(this.personaRepository.existsByCorreoAndIdNot("profe@autoescuela.es", 105L)).thenReturn(true);
        assertThrows(ReglaNegocioException.class, () -> this.profesorService.modificarProfesor(baseDto));

        when(this.personaRepository.existsByCorreoAndIdNot("profe@autoescuela.es", 105L)).thenReturn(false);
        when(this.personaRepository.existsByTelefonoAndIdNot("600111222", 105L)).thenReturn(true);
        assertThrows(ReglaNegocioException.class, () -> this.profesorService.modificarProfesor(baseDto));

        when(this.personaRepository.existsByTelefonoAndIdNot("600111222", 105L)).thenReturn(false);

        // Asignar vehículo distinto ocupado o incompatible
        Vehiculo vehiculoActual = new Vehiculo();
        ReflectionTestUtils.setField(vehiculoActual, "id", 1L);
        vehiculoActual.setTipoPermiso(TipoCarnet.PERMISO_B);
        vehiculoActual.setEstado(EstadoVehiculo.OCUPADO);
        profesor.setVehiculo(vehiculoActual);

        Profesor otroProfesor = this.crearProfesorBase(999L);
        Vehiculo vehiculoDeOtro = new Vehiculo();
        ReflectionTestUtils.setField(vehiculoDeOtro, "id", 2L);
        vehiculoDeOtro.setMatricula("9999ZZZ");
        vehiculoDeOtro.setProfesor(otroProfesor);
        vehiculoDeOtro.setTipoPermiso(TipoCarnet.PERMISO_B);
        when(this.vehiculoRepository.findById(2L)).thenReturn(Optional.of(vehiculoDeOtro));
        assertThrows(ReglaNegocioException.class, () -> this.profesorService.modificarProfesor(this.crearEditarProfesorDTO(profesor, 2L)));

        // Mismo vehículo incompatible con nuevos permisos
        vehiculoActual.setTipoPermiso(TipoCarnet.PERMISO_C);
        assertThrows(ReglaNegocioException.class, () -> this.profesorService.modificarProfesor(this.crearEditarProfesorDTO(profesor, 1L)));

        // Cambio exitoso de vehículo 1 -> vehículo 3
        vehiculoActual.setTipoPermiso(TipoCarnet.PERMISO_B);
        Vehiculo vehiculoNuevo = new Vehiculo();
        ReflectionTestUtils.setField(vehiculoNuevo, "id", 3L);
        vehiculoNuevo.setTipoPermiso(TipoCarnet.PERMISO_B);
        vehiculoNuevo.setEstado(EstadoVehiculo.DISPONIBLE);
        when(this.vehiculoRepository.findById(3L)).thenReturn(Optional.of(vehiculoNuevo));
        when(this.profesorRepository.save(any(Profesor.class))).thenAnswer(i -> i.getArgument(0));

        Profesor actualizado = this.profesorService.modificarProfesor(this.crearEditarProfesorDTO(profesor, 3L));
        assertSame(vehiculoNuevo, actualizado.getVehiculo());
        assertEquals(EstadoVehiculo.DISPONIBLE, vehiculoActual.getEstado());
        assertEquals(EstadoVehiculo.OCUPADO, vehiculoNuevo.getEstado());
    }

    @Test
    @DisplayName("darBajaProfesor valida entradas, reasignación y modo SIN_PROFESOR notificando alumnos")
    void testDarBajaProfesorRamasYNotificaciones()
    {
        assertThrows(ReglaNegocioException.class, () -> this.profesorService.darBajaProfesor(null));
        assertThrows(ReglaNegocioException.class, () -> this.profesorService.darBajaProfesor(new BajaProfesorDTO(null, "SIN_PROFESOR", null)));

        Profesor profesor = this.crearProfesorBase(108L);
        profesor.setEstado(EstadoUsuario.INACTIVO);
        when(this.profesorRepository.findById(108L)).thenReturn(Optional.of(profesor));
        assertThrows(ReglaNegocioException.class, () -> this.profesorService.darBajaProfesor(new BajaProfesorDTO(108L, "SIN_PROFESOR", null)));

        profesor.setEstado(EstadoUsuario.ACTIVO);
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setTipoPermiso(TipoCarnet.PERMISO_C);
        vehiculo.setEstado(EstadoVehiculo.OCUPADO);
        profesor.setVehiculo(vehiculo);

        Alumno alumno = new Alumno();
        alumno.setNombre("Laura");
        alumno.setCorreo("laura@mands.es");
        ClasePractica clasePend = new ClasePractica();
        clasePend.setEstadoClase(EstadoClase.PENDIENTE);

        when(this.clasePracticaRepository.findByProfesorAndEstadoClase(profesor, EstadoClase.PENDIENTE)).thenReturn(List.of(clasePend));
        when(this.alumnoRepository.findByProfesor(profesor)).thenReturn(List.of(alumno));

        assertThrows(ReglaNegocioException.class, () -> this.profesorService.darBajaProfesor(new BajaProfesorDTO(108L, "REASIGNAR", null)));
        assertThrows(ReglaNegocioException.class, () -> this.profesorService.darBajaProfesor(new BajaProfesorDTO(108L, "REASIGNAR", 108L)));

        Profesor sustitutoInactivo = this.crearProfesorBase(109L);
        sustitutoInactivo.setEstado(EstadoUsuario.INACTIVO);
        when(this.profesorRepository.findById(109L)).thenReturn(Optional.of(sustitutoInactivo));
        assertThrows(ReglaNegocioException.class, () -> this.profesorService.darBajaProfesor(new BajaProfesorDTO(108L, "REASIGNAR", 109L)));

        Profesor sustitutoSinCarnet = this.crearProfesorBase(110L);
        sustitutoSinCarnet.setListaTiposCarnet(List.of(TipoCarnet.PERMISO_B));
        profesor.setVehiculo(vehiculo);
        when(this.profesorRepository.findById(110L)).thenReturn(Optional.of(sustitutoSinCarnet));
        assertThrows(ReglaNegocioException.class, () -> this.profesorService.darBajaProfesor(new BajaProfesorDTO(108L, "REASIGNAR", 110L)));

        assertThrows(ReglaNegocioException.class, () -> this.profesorService.darBajaProfesor(new BajaProfesorDTO(108L, "OTRA", null)));

        // Baja con SIN_PROFESOR
        when(this.profesorRepository.save(any(Profesor.class))).thenAnswer(i -> i.getArgument(0));
        Profesor baja = this.profesorService.darBajaProfesor(new BajaProfesorDTO(108L, "SIN_PROFESOR", null));
        assertEquals(EstadoUsuario.INACTIVO, baja.getEstado());
        assertNull(alumno.getProfesor());
        assertEquals(EstadoClase.CANCELADA, clasePend.getEstadoClase());
    }

    @Test
    @DisplayName("reactivarProfesor, consultas auxiliares, obtenerProfesorActual, actualizarPerfil y cambiarPassword")
    void testReactivarConsultasPerfilYPassword()
    {
        assertThrows(ReglaNegocioException.class, () -> this.profesorService.reactivarProfesor(null));
        Profesor activo = this.crearProfesorBase(120L);
        when(this.profesorRepository.findById(120L)).thenReturn(Optional.of(activo));
        assertThrows(ReglaNegocioException.class, () -> this.profesorService.reactivarProfesor(120L));

        assertThrows(RecursoNoEncontradoException.class, () -> this.profesorService.reactivarProfesor(999L));

        // Consultas auxiliares
        assertTrue(this.profesorService.obtenerProfesoresActivosConVehiculoPorCarnet(null, null).isEmpty());
        assertEquals(0, this.profesorService.contarClasesPendientes(null));
        assertEquals(0, this.profesorService.contarClasesPendientes(999L));
        assertTrue(this.profesorService.buscarPorId(null).isEmpty());
        assertTrue(this.profesorService.buscarPorNombreUsuario("  ").isEmpty());

        ProfesorResumenDTO resumen = new ProfesorResumenDTO(120L, "Carlos", "Ruiz", "Carlos Ruiz", "CR",
                "23456789D", "c@m.es", "600111222", LocalDate.of(2025, 1, 15), 2025, TipoTurno.MATINAL,
                1L, "1234", "Polo", TipoCarnet.PERMISO_B, "Permiso B", "Polo (1234)",
                List.of(TipoCarnet.PERMISO_B), 2, 1, EstadoUsuario.ACTIVO);
        when(this.profesorRepository.findActivosConVehiculoPorCarnetExcluyendoIdOrderByAlumnosAsc(TipoCarnet.PERMISO_B, 1L))
                .thenReturn(List.of(activo));
        when(this.profesorRepository.findActivosConVehiculoPorCarnetOrderByAlumnosAsc(TipoCarnet.PERMISO_B))
                .thenReturn(List.of(activo));
        when(this.profesorMapper.toProfesorResumenDTO(activo)).thenReturn(resumen);
        when(this.profesorRepository.count()).thenReturn(4L);
        when(this.profesorRepository.countByTurno(TipoTurno.MATINAL)).thenReturn(2L);
        when(this.clasePracticaRepository.countByProfesorAndEstadoClase(activo, EstadoClase.PENDIENTE)).thenReturn(3);
        when(this.profesorRepository.findByNombreUsuario("cruiz")).thenReturn(Optional.of(activo));
        when(this.profesorRepository.findAll()).thenReturn(List.of(activo));
        when(this.profesorRepository.save(activo)).thenReturn(activo);

        assertEquals(1, this.profesorService.obtenerProfesoresActivosConVehiculoPorCarnet(TipoCarnet.PERMISO_B, 1L).size());
        assertEquals(1, this.profesorService.obtenerProfesoresActivosConVehiculoPorCarnet(TipoCarnet.PERMISO_B, null).size());
        assertEquals(4L, this.profesorService.contarTotalProfesores());
        assertEquals(2L, this.profesorService.contarProfesoresPorTurno(TipoTurno.MATINAL));
        assertEquals(3, this.profesorService.contarClasesPendientes(120L));
        assertTrue(this.profesorService.buscarPorId(120L).isPresent());
        assertTrue(this.profesorService.buscarPorNombreUsuario(" cruiz ").isPresent());
        assertEquals(1, this.profesorService.obtenerTodos().size());
        assertSame(activo, this.profesorService.guardar(activo));

        // obtenerProfesorActual con UserDetailsImpl, UserDetails y objeto no reconocido
        Alumno alumnoPrincipal = new Alumno();
        ReflectionTestUtils.setField(alumnoPrincipal, "id", 201L);
        alumnoPrincipal.setNombre("Ana");
        alumnoPrincipal.setApellidos("Gómez");
        alumnoPrincipal.setNombreUsuario("ana");
        alumnoPrincipal.setEstado(EstadoUsuario.ACTIVO);
        assertNull(this.profesorService.obtenerProfesorActual(new UserDetailsImpl(alumnoPrincipal)));

        assertSame(activo, this.profesorService.obtenerProfesorActual(new UserDetailsImpl(activo)));

        User adminUser = new User("admin", "pw", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        assertNull(this.profesorService.obtenerProfesorActual(adminUser));

        User profUser = new User("desconocido", "pw", List.of(new SimpleGrantedAuthority("ROLE_PROFESOR")));
        when(this.profesorRepository.findByNombreUsuario("desconocido")).thenReturn(Optional.empty());
        assertSame(activo, this.profesorService.obtenerProfesorActual(profUser));
        assertNull(this.profesorService.obtenerProfesorActual("otro"));

        // actualizarPerfil
        assertThrows(ReglaNegocioException.class, () -> this.profesorService.actualizarPerfil(null, new EditarPerfilProfesorDTO()));
        EditarPerfilProfesorDTO perfilDto = new EditarPerfilProfesorDTO(120L, "Carlos", "Ruiz", "699888777", "Nueva Dir",
                LocalDate.of(1985, 5, 10), "23456789D", "cruiz", "c@m.es", TipoTurno.MATINAL, LocalDate.now(), null, null, null);
        when(this.personaRepository.existsByTelefonoAndIdNot("699888777", 120L)).thenReturn(true);
        assertThrows(ReglaNegocioException.class, () -> this.profesorService.actualizarPerfil(120L, perfilDto));

        when(this.personaRepository.existsByTelefonoAndIdNot("699888777", 120L)).thenReturn(false);
        this.profesorService.actualizarPerfil(120L, perfilDto);
        assertEquals("699888777", activo.getTelefono());

        // cambiarPassword
        assertThrows(ReglaNegocioException.class, () -> this.profesorService.cambiarPassword(null, "a", "123456", "123456"));
        when(this.passwordEncoder.matches("mal", activo.getPassword())).thenReturn(false);
        assertThrows(ReglaNegocioException.class, () -> this.profesorService.cambiarPassword(120L, "mal", "123456", "123456"));

        when(this.passwordEncoder.matches("ok", activo.getPassword())).thenReturn(true);
        assertThrows(ReglaNegocioException.class, () -> this.profesorService.cambiarPassword(120L, "ok", "123", "123"));
        assertThrows(ReglaNegocioException.class, () -> this.profesorService.cambiarPassword(120L, "ok", "123456", "654321"));

        when(this.passwordEncoder.encode("nueva123")).thenReturn("encodedNueva");
        this.profesorService.cambiarPassword(120L, "ok", "nueva123", "nueva123");
        assertEquals("encodedNueva", activo.getPassword());
    }
}
