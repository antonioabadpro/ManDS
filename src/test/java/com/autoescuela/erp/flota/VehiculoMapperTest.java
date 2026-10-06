package com.autoescuela.erp.flota;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.autoescuela.erp.core.enums.EstadoVehiculo;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.flota.dto.VehiculoResumenDTO;
import com.autoescuela.erp.flota.mapper.VehiculoMapper;
import com.autoescuela.erp.flota.model.Vehiculo;

import static org.assertj.core.api.Assertions.assertThat;

class VehiculoMapperTest
{
    private final VehiculoMapper mapper = Mappers.getMapper(VehiculoMapper.class);

    @Test
    @DisplayName("toVehiculoResumenDTO mapea correctamente tipoPermiso al campo tipo del DTO")
    void testToVehiculoResumenDTOMapeaTipo()
    {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setMatricula("1234-LMN");
        vehiculo.setMarca("SEAT");
        vehiculo.setModelo("Ibiza 1.0 TSI");
        vehiculo.setColor("Blanco");
        vehiculo.setKm(45000L);
        vehiculo.setEstado(EstadoVehiculo.DISPONIBLE);
        vehiculo.setTipoPermiso(TipoCarnet.PERMISO_B);
        vehiculo.setFechaProximaRevision(LocalDate.now().plusMonths(6));

        VehiculoResumenDTO dto = this.mapper.toVehiculoResumenDTO(vehiculo);

        assertThat(dto).isNotNull();
        assertThat(dto.tipoPermiso()).isEqualTo(TipoCarnet.PERMISO_B);
        assertThat(dto.tipoDescripcion()).isEqualTo("Permiso B");
        assertThat(dto.matriculaPrefijo()).isEqualTo("1234");
    }
}
