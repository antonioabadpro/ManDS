package com.autoescuela.erp.flota.service;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.autoescuela.erp.core.enums.EstadoVehiculo;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.flota.dto.VehiculoResumenDTO;
import com.autoescuela.erp.flota.mapper.VehiculoMapper;
import com.autoescuela.erp.flota.model.Vehiculo;
import com.autoescuela.erp.flota.repository.VehiculoRepository;

import static org.assertj.core.api.Assertions.assertThat;
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
}
