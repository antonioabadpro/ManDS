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

    @Test
    @DisplayName("toVehiculoDetalleDTO mapea exhaustivamente todos los atributos y campos derivados del vehículo")
    void testToVehiculoDetalleDTOMapeoCompleto()
    {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setMatricula("1234-LMN");
        vehiculo.setMarca("SEAT");
        vehiculo.setModelo("Ibiza 1.0 TSI");
        vehiculo.setColor("Blanco Nevada");
        vehiculo.setKm(45000L);
        vehiculo.setCv(110);
        vehiculo.setAnio(2022);
        vehiculo.setTipoCombustible(com.autoescuela.erp.core.enums.TipoCombustible.GASOLINA);
        vehiculo.setCajaCambios(com.autoescuela.erp.core.enums.TipoCambio.MANUAL);
        vehiculo.setFechaUltimaRevision(LocalDate.now().minusMonths(6));
        vehiculo.setFechaProximaRevision(LocalDate.now().plusMonths(6));
        vehiculo.setEstado(EstadoVehiculo.DISPONIBLE);
        vehiculo.setTipoPermiso(TipoCarnet.PERMISO_B);

        var dto = this.mapper.toVehiculoDetalleDTO(vehiculo);

        assertThat(dto).isNotNull();
        assertThat(dto.matricula()).isEqualTo("1234-LMN");
        assertThat(dto.matriculaPrefijo()).isEqualTo("1234");
        assertThat(dto.marca()).isEqualTo("SEAT");
        assertThat(dto.modelo()).isEqualTo("Ibiza 1.0 TSI");
        assertThat(dto.color()).isEqualTo("Blanco Nevada");
        assertThat(dto.km()).isEqualTo(45000L);
        assertThat(dto.cv()).isEqualTo(110);
        assertThat(dto.anio()).isEqualTo(2022);
        assertThat(dto.tipoCombustible()).isEqualTo(com.autoescuela.erp.core.enums.TipoCombustible.GASOLINA);
        assertThat(dto.combustibleDescripcion()).isEqualTo("Gasolina");
        assertThat(dto.cajaCambios()).isEqualTo(com.autoescuela.erp.core.enums.TipoCambio.MANUAL);
        assertThat(dto.cambioDescripcion()).isEqualTo("Manual");
        assertThat(dto.estado()).isEqualTo(EstadoVehiculo.DISPONIBLE);
        assertThat(dto.tipoPermiso()).isEqualTo(TipoCarnet.PERMISO_B);
        assertThat(dto.tipoDescripcion()).isEqualTo("Permiso B");
        assertThat(dto.isRevisionAlDia()).isTrue();
    }
}
