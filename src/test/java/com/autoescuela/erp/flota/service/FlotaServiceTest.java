package com.autoescuela.erp.flota.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.autoescuela.erp.core.enums.EstadoVehiculo;
import com.autoescuela.erp.core.enums.TipoCambio;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.enums.TipoCombustible;
import com.autoescuela.erp.core.excepciones.RecursoNoEncontradoException;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.flota.dto.AltaVehiculoDTO;
import com.autoescuela.erp.flota.dto.EditarVehiculoDTO;
import com.autoescuela.erp.flota.dto.VehiculoDetalleDTO;
import com.autoescuela.erp.flota.dto.VehiculoResumenDTO;
import com.autoescuela.erp.flota.mapper.VehiculoMapper;
import com.autoescuela.erp.flota.model.Vehiculo;
import com.autoescuela.erp.flota.repository.VehiculoRepository;
import com.autoescuela.erp.usuarios.model.Profesor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlotaServiceTest
{
    @Mock
    private VehiculoRepository vehiculoRepository;

    @Mock
    private VehiculoMapper vehiculoMapper;

    @InjectMocks
    private FlotaService flotaService;

    @Test
    @DisplayName("obtenerTodosLosVehiculos recupera la flota ordenada por ID y delega en VehiculoMapper")
    void testObtenerTodosLosVehiculos()
    {
        Vehiculo vehiculo1 = new Vehiculo();
        vehiculo1.setMatricula("1234-LMN");
        vehiculo1.setMarca("SEAT");
        vehiculo1.setModelo("Ibiza");

        VehiculoResumenDTO dto1 = new VehiculoResumenDTO(
                1L, "1234-LMN", "1234", "SEAT", "Ibiza", "Blanco",
                45000L, LocalDate.now().minusMonths(6), LocalDate.now().plusMonths(6),
                EstadoVehiculo.DISPONIBLE, TipoCarnet.PERMISO_B, "Permiso B",
                null, null, null
        );

        when(this.vehiculoRepository.findAllByOrderByTipoPermisoAsc()).thenReturn(List.of(vehiculo1));
        when(this.vehiculoMapper.toVehiculoResumenDTO(vehiculo1)).thenReturn(dto1);

        List<VehiculoResumenDTO> resultado = this.flotaService.obtenerTodosLosVehiculos();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).matricula()).isEqualTo("1234-LMN");
        assertThat(resultado.get(0).matriculaPrefijo()).isEqualTo("1234");
        verify(this.vehiculoRepository).findAllByOrderByTipoPermisoAsc();
        verify(this.vehiculoMapper).toVehiculoResumenDTO(vehiculo1);
    }

    @Test
    @DisplayName("VehiculoResumenDTO evalúa correctamente los estados de vencimiento de ITV")
    void testVehiculoResumenDTOEstadosRevision()
    {
        VehiculoResumenDTO revisionVencida = new VehiculoResumenDTO(
                1L, "1111-AAA", "1111", "SEAT", "Ibiza", "Blanco", 50000L,
                LocalDate.now().minusYears(1), LocalDate.now().minusDays(5),
                EstadoVehiculo.DISPONIBLE, TipoCarnet.PERMISO_B, "Permiso B",
                null, null, null
        );
        assertThat(revisionVencida.isRevisionVencida()).isTrue();
        assertThat(revisionVencida.isRevisionProxima()).isFalse();
        assertThat(revisionVencida.isRevisionAlDia()).isFalse();

        VehiculoResumenDTO revisionProxima = new VehiculoResumenDTO(
                2L, "2222-BBB", "2222", "Renault", "Clio", "Azul", 30000L,
                LocalDate.now().minusMonths(11), LocalDate.now().plusDays(20),
                EstadoVehiculo.OCUPADO, TipoCarnet.PERMISO_B, "Permiso B",
                null, null, null
        );
        assertThat(revisionProxima.isRevisionVencida()).isFalse();
        assertThat(revisionProxima.isRevisionProxima()).isTrue();
        assertThat(revisionProxima.isRevisionAlDia()).isFalse();

        VehiculoResumenDTO revisionAlDia = new VehiculoResumenDTO(
                3L, "3333-CCC", "3333", "Yamaha", "MT-07", "Negro", 10000L,
                LocalDate.now().minusMonths(2), LocalDate.now().plusMonths(10),
                EstadoVehiculo.DISPONIBLE, TipoCarnet.PERMISO_A2, "Permiso A2",
                null, null, null
        );
        assertThat(revisionAlDia.isRevisionVencida()).isFalse();
        assertThat(revisionAlDia.isRevisionProxima()).isFalse();
        assertThat(revisionAlDia.isRevisionAlDia()).isTrue();
    }

    @Test
    @DisplayName("obtenerVehiculoParaDetalle recupera el vehículo existente y delega en el mapper")
    void testObtenerVehiculoParaDetalleExitoso()
    {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setMatricula("1234-LMN");
        vehiculo.setMarca("SEAT");
        vehiculo.setModelo("Ibiza");

        VehiculoDetalleDTO dto = new VehiculoDetalleDTO(
                1L, "1234-LMN", "1234", "SEAT", "Ibiza", "Blanco",
                45000L, 110, 2022, null, "Gasolina", null, "Manual",
                LocalDate.now().minusMonths(6), LocalDate.now().plusMonths(6),
                EstadoVehiculo.DISPONIBLE, TipoCarnet.PERMISO_B, "Permiso B",
                null, null, null, null, 0, 0
        );

        when(this.vehiculoRepository.findById(1L)).thenReturn(Optional.of(vehiculo));
        when(this.vehiculoMapper.toVehiculoDetalleDTO(vehiculo)).thenReturn(dto);

        VehiculoDetalleDTO resultado = this.flotaService.obtenerVehiculoParaDetalle(1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.matricula()).isEqualTo("1234-LMN");
        verify(this.vehiculoRepository).findById(1L);
        verify(this.vehiculoMapper).toVehiculoDetalleDTO(vehiculo);
    }

    @Test
    @DisplayName("obtenerVehiculoParaDetalle lanza RecursoNoEncontradoException si el ID no existe")
    void testObtenerVehiculoParaDetalleNoExiste()
    {
        when(this.vehiculoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.flotaService.obtenerVehiculoParaDetalle(999L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("No se encontró el vehículo con ID: 999");

        verify(this.vehiculoRepository).findById(999L);
    }

    @Test
    @DisplayName("darAltaVehiculo persiste correctamente un nuevo vehículo en estado DISPONIBLE")
    void testDarAltaVehiculoExitoso()
    {
        AltaVehiculoDTO dto = new AltaVehiculoDTO(
                "9876-XYZ", "Toyota", "Yaris", "Gris", 0L, 90, 2024,
                TipoCombustible.HIBRIDO, TipoCambio.AUTOMATICO, TipoCarnet.PERMISO_B,
                null, LocalDate.now().plusYears(1)
        );

        Vehiculo entidadMapeada = new Vehiculo();
        entidadMapeada.setMatricula("9876-XYZ");
        entidadMapeada.setMarca("Toyota");
        entidadMapeada.setModelo("Yaris");
        entidadMapeada.setColor("Gris");
        entidadMapeada.setEstado(EstadoVehiculo.DISPONIBLE);

        when(this.vehiculoRepository.findByMatricula("9876-XYZ")).thenReturn(Optional.empty());
        when(this.vehiculoMapper.toEntity(dto)).thenReturn(entidadMapeada);
        when(this.vehiculoRepository.save(entidadMapeada)).thenReturn(entidadMapeada);

        Vehiculo resultado = this.flotaService.darAltaVehiculo(dto);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getMatricula()).isEqualTo("9876-XYZ");
        assertThat(resultado.getEstado()).isEqualTo(EstadoVehiculo.DISPONIBLE);
        verify(this.vehiculoRepository).findByMatricula("9876-XYZ");
        verify(this.vehiculoMapper).toEntity(dto);
        verify(this.vehiculoRepository).save(entidadMapeada);
    }

    @Test
    @DisplayName("darAltaVehiculo lanza ReglaNegocioException si la matrícula ya se encuentra registrada")
    void testDarAltaVehiculoMatriculaDuplicada()
    {
        AltaVehiculoDTO dto = new AltaVehiculoDTO(
                "1234-LMN", "SEAT", "Ibiza", "Blanco", 1000L, 110, 2022,
                TipoCombustible.GASOLINA, TipoCambio.MANUAL, TipoCarnet.PERMISO_B,
                null, LocalDate.now().plusYears(1)
        );

        Vehiculo existente = new Vehiculo();
        existente.setMatricula("1234-LMN");

        when(this.vehiculoRepository.findByMatricula("1234-LMN")).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> this.flotaService.darAltaVehiculo(dto))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Ya existe un vehículo registrado en la flota con la matrícula 1234-LMN.");

        verify(this.vehiculoRepository).findByMatricula("1234-LMN");
    }

    @Test
    @DisplayName("darAltaVehiculo lanza ReglaNegocioException si el DTO proporcionado es nulo")
    void testDarAltaVehiculoDtoNulo()
    {
        assertThatThrownBy(() -> this.flotaService.darAltaVehiculo(null))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Los datos del vehículo no pueden ser nulos.");
    }

    @Test
    @DisplayName("existeMatricula devuelve true cuando la matrícula normalizada existe en el repositorio")
    void testExisteMatriculaDevuelveTrueSiExiste()
    {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setMatricula("1234-LMN");

        when(this.vehiculoRepository.findByMatricula("1234-LMN")).thenReturn(Optional.of(vehiculo));

        boolean resultadoConGuion = this.flotaService.existeMatricula("1234-LMN");
        boolean resultadoSinGuion = this.flotaService.existeMatricula("1234lmn");

        assertThat(resultadoConGuion).isTrue();
        assertThat(resultadoSinGuion).isTrue();
        verify(this.vehiculoRepository, org.mockito.Mockito.times(2)).findByMatricula("1234-LMN");
    }

    @Test
    @DisplayName("existeMatricula devuelve false cuando la matrícula no existe o es nula/vacía")
    void testExisteMatriculaDevuelveFalseSiNoExisteOEsInvalida()
    {
        when(this.vehiculoRepository.findByMatricula("9999-ZZZ")).thenReturn(Optional.empty());

        assertThat(this.flotaService.existeMatricula("9999-ZZZ")).isFalse();
        assertThat(this.flotaService.existeMatricula(null)).isFalse();
        assertThat(this.flotaService.existeMatricula("")).isFalse();
        assertThat(this.flotaService.existeMatricula("   ")).isFalse();

        verify(this.vehiculoRepository).findByMatricula("9999-ZZZ");
    }

    @Test
    @DisplayName("darAltaVehiculo lanza ReglaNegocioException si el año de matriculación es inválido")
    void testDarAltaVehiculoAnioInvalido()
    {
        AltaVehiculoDTO dtoAnioPasado = new AltaVehiculoDTO(
                "1234-LMN", "SEAT", "Ibiza", "Blanco", 1000L, 110, 1980,
                TipoCombustible.GASOLINA, TipoCambio.MANUAL, TipoCarnet.PERMISO_B,
                null, LocalDate.now().plusYears(1)
        );

        assertThatThrownBy(() -> this.flotaService.darAltaVehiculo(dtoAnioPasado))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("El año de matriculación debe estar comprendido entre 1990 y el año actual.");

        AltaVehiculoDTO dtoAnioFuturo = new AltaVehiculoDTO(
                "1234-LMN", "SEAT", "Ibiza", "Blanco", 1000L, 110, LocalDate.now().getYear() + 2,
                TipoCombustible.GASOLINA, TipoCambio.MANUAL, TipoCarnet.PERMISO_B,
                null, LocalDate.now().plusYears(1)
        );

        assertThatThrownBy(() -> this.flotaService.darAltaVehiculo(dtoAnioFuturo))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("El año de matriculación debe estar comprendido entre 1990 y el año actual.");
    }

    @Test
    @DisplayName("darAltaVehiculo lanza ReglaNegocioException si la fecha de última revisión es inválida")
    void testDarAltaVehiculoFechaUltimaRevisionInvalida()
    {
        AltaVehiculoDTO dtoMuyAntigua = new AltaVehiculoDTO(
                "1234-LMN", "SEAT", "Ibiza", "Blanco", 1000L, 110, 2022,
                TipoCombustible.GASOLINA, TipoCambio.MANUAL, TipoCarnet.PERMISO_B,
                LocalDate.now().minusYears(5), LocalDate.now().plusYears(1)
        );

        assertThatThrownBy(() -> this.flotaService.darAltaVehiculo(dtoMuyAntigua))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("La fecha de la última revisión no puede ser anterior a hace 4 años ni posterior a hoy.");

        AltaVehiculoDTO dtoFutura = new AltaVehiculoDTO(
                "1234-LMN", "SEAT", "Ibiza", "Blanco", 1000L, 110, 2022,
                TipoCombustible.GASOLINA, TipoCambio.MANUAL, TipoCarnet.PERMISO_B,
                LocalDate.now().plusDays(2), LocalDate.now().plusYears(1)
        );

        assertThatThrownBy(() -> this.flotaService.darAltaVehiculo(dtoFutura))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("La fecha de la última revisión no puede ser anterior a hace 4 años ni posterior a hoy.");
    }

    @Test
    @DisplayName("darAltaVehiculo lanza ReglaNegocioException si la fecha de próxima revisión no es posterior a la última")
    void testDarAltaVehiculoRevisionesIncoherentes()
    {
        AltaVehiculoDTO dto = new AltaVehiculoDTO(
                "1234-LMN", "SEAT", "Ibiza", "Blanco", 1000L, 110, 2022,
                TipoCombustible.GASOLINA, TipoCambio.MANUAL, TipoCarnet.PERMISO_B,
                LocalDate.now().minusMonths(6), LocalDate.now().minusMonths(7)
        );

        assertThatThrownBy(() -> this.flotaService.darAltaVehiculo(dto))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("La fecha de la próxima revisión debe ser estrictamente posterior a la fecha de la última revisión.");
    }

    @Test
    @DisplayName("darAltaVehiculo lanza ReglaNegocioException si la fecha de próxima revisión es superior a 10 años")
    void testDarAltaVehiculoFechaProximaRevisionInvalida()
    {
        AltaVehiculoDTO dto = new AltaVehiculoDTO(
                "1234-LMN", "SEAT", "Ibiza", "Blanco", 1000L, 110, 2022,
                TipoCombustible.GASOLINA, TipoCambio.MANUAL, TipoCarnet.PERMISO_B,
                null, LocalDate.now().plusYears(11)
        );

        assertThatThrownBy(() -> this.flotaService.darAltaVehiculo(dto))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("La fecha de la próxima revisión no puede superar los 10 años en el futuro.");
    }

    @Test
    @DisplayName("existeMatriculaOtroVehiculo devuelve true si existe la matrícula en otro vehículo")
    void testExisteMatriculaOtroVehiculoDevuelveTrueSiExiste()
    {
        when(this.vehiculoRepository.existsByMatriculaAndIdNot("1234-LMN", 1L)).thenReturn(true);


        boolean resultado = this.flotaService.existeMatriculaOtroVehiculo("1234-LMN", 1L);
        boolean resultadoSinGuion = this.flotaService.existeMatriculaOtroVehiculo("1234lmn", 1L);

        assertThat(resultado).isTrue();
        assertThat(resultadoSinGuion).isTrue();
        verify(this.vehiculoRepository, org.mockito.Mockito.times(2)).existsByMatriculaAndIdNot("1234-LMN", 1L);
    }

    @Test
    @DisplayName("existeMatriculaOtroVehiculo devuelve false si no existe o es nula")
    void testExisteMatriculaOtroVehiculoDevuelveFalseSiNoExiste()
    {
        when(this.vehiculoRepository.existsByMatriculaAndIdNot("9999-ZZZ", 1L)).thenReturn(false);

        assertThat(this.flotaService.existeMatriculaOtroVehiculo("9999-ZZZ", 1L)).isFalse();
        assertThat(this.flotaService.existeMatriculaOtroVehiculo(null, 1L)).isFalse();
        assertThat(this.flotaService.existeMatriculaOtroVehiculo("", 1L)).isFalse();
    }

    @Test
    @DisplayName("obtenerVehiculoParaEdicion recupera el DTO mapeado correctamente si el vehículo está activo")
    void testObtenerVehiculoParaEdicionExito()
    {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setMatricula("1234-LMN");
        vehiculo.setMarca("SEAT");
        vehiculo.setModelo("Ibiza");
        vehiculo.setEstado(EstadoVehiculo.DISPONIBLE);

        EditarVehiculoDTO dto = new EditarVehiculoDTO(
                1L, "1234-LMN", "SEAT", "Ibiza", "Blanco", 50000L, 110, 2022,
                TipoCombustible.GASOLINA, TipoCambio.MANUAL, TipoCarnet.PERMISO_B,
                null, LocalDate.now().plusMonths(6)
        );

        when(this.vehiculoRepository.findById(1L)).thenReturn(Optional.of(vehiculo));
        when(this.vehiculoMapper.toEditarVehiculoDTO(vehiculo)).thenReturn(dto);

        EditarVehiculoDTO resultado = this.flotaService.obtenerVehiculoParaEdicion(1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.matricula()).isEqualTo("1234-LMN");
        verify(this.vehiculoRepository).findById(1L);
        verify(this.vehiculoMapper).toEditarVehiculoDTO(vehiculo);
    }

    @Test
    @DisplayName("obtenerVehiculoParaEdicion lanza excepción si el id es nulo o no existe")
    void testObtenerVehiculoParaEdicionIdInvalido()
    {
        assertThatThrownBy(() -> this.flotaService.obtenerVehiculoParaEdicion(null))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("El identificador del vehículo no puede ser nulo.");

        when(this.vehiculoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.flotaService.obtenerVehiculoParaEdicion(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("No se encontró el vehículo con ID: 99");
    }

    @Test
    @DisplayName("obtenerVehiculoParaEdicion lanza excepción si el vehículo está INACTIVO")
    void testObtenerVehiculoParaEdicionVehiculoInactivo()
    {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setEstado(EstadoVehiculo.INACTIVO);

        when(this.vehiculoRepository.findById(1L)).thenReturn(Optional.of(vehiculo));

        assertThatThrownBy(() -> this.flotaService.obtenerVehiculoParaEdicion(1L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("No se puede modificar un vehículo en estado INACTIVO.");
    }

    @Test
    @DisplayName("tieneProfesorAsignado verifica correctamente la presencia de profesor vinculado")
    void testTieneProfesorAsignado()
    {
        Vehiculo conProfesor = new Vehiculo();
        conProfesor.setProfesor(new Profesor());

        Vehiculo sinProfesor = new Vehiculo();
        sinProfesor.setProfesor(null);

        when(this.vehiculoRepository.findById(1L)).thenReturn(Optional.of(conProfesor));
        when(this.vehiculoRepository.findById(2L)).thenReturn(Optional.of(sinProfesor));
        when(this.vehiculoRepository.findById(3L)).thenReturn(Optional.empty());

        assertThat(this.flotaService.tieneProfesorAsignado(1L)).isTrue();
        assertThat(this.flotaService.tieneProfesorAsignado(2L)).isFalse();
        assertThat(this.flotaService.tieneProfesorAsignado(3L)).isFalse();
        assertThat(this.flotaService.tieneProfesorAsignado(null)).isFalse();
    }

    @Test
    @DisplayName("modificarVehiculo actualiza satisfactoriamente matrícula y permiso cuando NO tiene profesor asignado")
    void testModificarVehiculoSinProfesorPermiteCambiarPermisoYMatricula()
    {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setMatricula("1234-LMN");
        vehiculo.setTipoPermiso(TipoCarnet.PERMISO_B);
        vehiculo.setProfesor(null);
        vehiculo.setEstado(EstadoVehiculo.DISPONIBLE);

        EditarVehiculoDTO dto = new EditarVehiculoDTO(
                1L, "5678-XYZ", "Toyota", "Yaris", "Rojo", 35000L, 90, 2023,
                TipoCombustible.HIBRIDO, TipoCambio.AUTOMATICO, TipoCarnet.PERMISO_A2,
                LocalDate.now().minusMonths(6), LocalDate.now().plusMonths(6)
        );

        when(this.vehiculoRepository.findById(1L)).thenReturn(Optional.of(vehiculo));
        when(this.vehiculoRepository.existsByMatriculaAndIdNot("5678-XYZ", 1L)).thenReturn(false);
        when(this.vehiculoRepository.save(vehiculo)).thenReturn(vehiculo);

        Vehiculo resultado = this.flotaService.modificarVehiculo(dto);

        assertThat(resultado.getMatricula()).isEqualTo("5678-XYZ");
        assertThat(resultado.getTipoPermiso()).isEqualTo(TipoCarnet.PERMISO_A2);
        assertThat(resultado.getMarca()).isEqualTo("Toyota");
        assertThat(resultado.getModelo()).isEqualTo("Yaris");
        assertThat(resultado.getKm()).isEqualTo(35000L);
        verify(this.vehiculoRepository).save(vehiculo);
    }

    @Test
    @DisplayName("modificarVehiculo actualiza ficha técnica pero mantiene intactos permiso y matrícula cuando TIENE profesor asignado")
    void testModificarVehiculoConProfesorMantienePermisoYMatricula()
    {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setMatricula("1234-LMN");
        vehiculo.setTipoPermiso(TipoCarnet.PERMISO_B);
        vehiculo.setProfesor(new Profesor());
        vehiculo.setEstado(EstadoVehiculo.OCUPADO);

        EditarVehiculoDTO dto = new EditarVehiculoDTO(
                1L, "1234-LMN", "SEAT", "Ibiza Nuevo", "Gris", 60000L, 115, 2022,
                TipoCombustible.DIESEL, TipoCambio.MANUAL, TipoCarnet.PERMISO_B,
                null, LocalDate.now().plusMonths(12)
        );

        when(this.vehiculoRepository.findById(1L)).thenReturn(Optional.of(vehiculo));
        when(this.vehiculoRepository.save(vehiculo)).thenReturn(vehiculo);

        Vehiculo resultado = this.flotaService.modificarVehiculo(dto);

        assertThat(resultado.getMatricula()).isEqualTo("1234-LMN");
        assertThat(resultado.getTipoPermiso()).isEqualTo(TipoCarnet.PERMISO_B);
        assertThat(resultado.getModelo()).isEqualTo("Ibiza Nuevo");
        assertThat(resultado.getKm()).isEqualTo(60000L);
        verify(this.vehiculoRepository).save(vehiculo);
    }

    @Test
    @DisplayName("modificarVehiculo lanza ReglaNegocioException si tiene profesor e intenta modificar el permiso")
    void testModificarVehiculoConProfesorErrorCambiarPermiso()
    {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setMatricula("1234-LMN");
        vehiculo.setTipoPermiso(TipoCarnet.PERMISO_B);
        vehiculo.setProfesor(new Profesor());
        vehiculo.setEstado(EstadoVehiculo.OCUPADO);

        EditarVehiculoDTO dto = new EditarVehiculoDTO(
                1L, "1234-LMN", "SEAT", "Ibiza", "Blanco", 50000L, 110, 2022,
                TipoCombustible.GASOLINA, TipoCambio.MANUAL, TipoCarnet.PERMISO_A2,
                null, LocalDate.now().plusMonths(6)
        );

        when(this.vehiculoRepository.findById(1L)).thenReturn(Optional.of(vehiculo));

        assertThatThrownBy(() -> this.flotaService.modificarVehiculo(dto))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("No se puede modificar el tipo de carnet de un vehículo que tiene un profesor asignado.");
    }

    @Test
    @DisplayName("modificarVehiculo lanza ReglaNegocioException si tiene profesor e intenta modificar la matrícula")
    void testModificarVehiculoConProfesorErrorCambiarMatricula()
    {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setMatricula("1234-LMN");
        vehiculo.setTipoPermiso(TipoCarnet.PERMISO_B);
        vehiculo.setProfesor(new Profesor());
        vehiculo.setEstado(EstadoVehiculo.OCUPADO);

        EditarVehiculoDTO dto = new EditarVehiculoDTO(
                1L, "9999-ZZZ", "SEAT", "Ibiza", "Blanco", 50000L, 110, 2022,
                TipoCombustible.GASOLINA, TipoCambio.MANUAL, TipoCarnet.PERMISO_B,
                null, LocalDate.now().plusMonths(6)
        );

        when(this.vehiculoRepository.findById(1L)).thenReturn(Optional.of(vehiculo));

        assertThatThrownBy(() -> this.flotaService.modificarVehiculo(dto))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("No se puede modificar la matrícula de un vehículo que tiene un profesor asignado.");
    }

    @Test
    @DisplayName("modificarVehiculo lanza ReglaNegocioException si la matrícula ya pertenece a otro vehículo")
    void testModificarVehiculoErrorMatriculaDuplicada()
    {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setMatricula("1234-LMN");
        vehiculo.setTipoPermiso(TipoCarnet.PERMISO_B);
        vehiculo.setProfesor(null);
        vehiculo.setEstado(EstadoVehiculo.DISPONIBLE);

        EditarVehiculoDTO dto = new EditarVehiculoDTO(
                1L, "9999-ZZZ", "SEAT", "Ibiza", "Blanco", 50000L, 110, 2022,
                TipoCombustible.GASOLINA, TipoCambio.MANUAL, TipoCarnet.PERMISO_B,
                null, LocalDate.now().plusMonths(6)
        );

        when(this.vehiculoRepository.findById(1L)).thenReturn(Optional.of(vehiculo));
        when(this.vehiculoRepository.existsByMatriculaAndIdNot("9999-ZZZ", 1L)).thenReturn(true);

        assertThatThrownBy(() -> this.flotaService.modificarVehiculo(dto))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Ya existe un vehículo registrado en la flota con la matrícula 9999-ZZZ.");
    }

    @Test
    @DisplayName("modificarVehiculo lanza ReglaNegocioException si km o cv son negativos")
    void testModificarVehiculoValoresNegativos()
    {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setEstado(EstadoVehiculo.DISPONIBLE);

        when(this.vehiculoRepository.findById(1L)).thenReturn(Optional.of(vehiculo));

        EditarVehiculoDTO dtoKmNegativo = new EditarVehiculoDTO(
                1L, "1234-LMN", "SEAT", "Ibiza", "Blanco", -10L, 110, 2022,
                TipoCombustible.GASOLINA, TipoCambio.MANUAL, TipoCarnet.PERMISO_B,
                null, LocalDate.now().plusMonths(6)
        );

        assertThatThrownBy(() -> this.flotaService.modificarVehiculo(dtoKmNegativo))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("El kilometraje no puede ser negativo.");

        EditarVehiculoDTO dtoCvNegativo = new EditarVehiculoDTO(
                1L, "1234-LMN", "SEAT", "Ibiza", "Blanco", 1000L, -5, 2022,
                TipoCombustible.GASOLINA, TipoCambio.MANUAL, TipoCarnet.PERMISO_B,
                null, LocalDate.now().plusMonths(6)
        );

        assertThatThrownBy(() -> this.flotaService.modificarVehiculo(dtoCvNegativo))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("La potencia no puede ser negativa ni superar los 1000 CV.");
    }

    @Test
    @DisplayName("modificarVehiculo lanza ReglaNegocioException si el vehículo está INACTIVO")
    void testModificarVehiculoInactivoLanzaExcepcion()
    {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setEstado(EstadoVehiculo.INACTIVO);

        when(this.vehiculoRepository.findById(1L)).thenReturn(Optional.of(vehiculo));

        EditarVehiculoDTO dto = new EditarVehiculoDTO(
                1L, "1234-LMN", "SEAT", "Ibiza", "Blanco", 1000L, 110, 2022,
                TipoCombustible.GASOLINA, TipoCambio.MANUAL, TipoCarnet.PERMISO_B,
                null, LocalDate.now().plusMonths(6)
        );

        assertThatThrownBy(() -> this.flotaService.modificarVehiculo(dto))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("No se puede modificar un vehículo en estado INACTIVO.");
    }
}
