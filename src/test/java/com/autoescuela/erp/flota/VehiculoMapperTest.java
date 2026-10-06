package com.autoescuela.erp.flota;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.autoescuela.erp.core.enums.EstadoVehiculo;
import com.autoescuela.erp.core.enums.TipoCambio;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.enums.TipoCombustible;
import com.autoescuela.erp.flota.dto.AltaVehiculoDTO;
import com.autoescuela.erp.flota.dto.EditarVehiculoDTO;
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
        vehiculo.setTipoCombustible(TipoCombustible.GASOLINA);
        vehiculo.setCajaCambios(TipoCambio.MANUAL);
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
        assertThat(dto.tipoCombustible()).isEqualTo(TipoCombustible.GASOLINA);
        assertThat(dto.combustibleDescripcion()).isEqualTo("Gasolina");
        assertThat(dto.cajaCambios()).isEqualTo(TipoCambio.MANUAL);
        assertThat(dto.cambioDescripcion()).isEqualTo("Manual");
        assertThat(dto.estado()).isEqualTo(EstadoVehiculo.DISPONIBLE);
        assertThat(dto.tipoPermiso()).isEqualTo(TipoCarnet.PERMISO_B);
        assertThat(dto.tipoDescripcion()).isEqualTo("Permiso B");
        assertThat(dto.isRevisionAlDia()).isTrue();
    }

    @Test
    @DisplayName("toEntity mapea AltaVehiculoDTO a una entidad Vehiculo con estado DISPONIBLE y matrícula normalizada")
    void testToEntityMapeoAltaVehiculo()
    {
        AltaVehiculoDTO dto = new AltaVehiculoDTO(
                "5678-bvc", "Renault", "Clio 1.5", "Rojo Pasión", 500L, 95, 2023,
                TipoCombustible.DIESEL,
                TipoCambio.MANUAL,
                TipoCarnet.PERMISO_B,
                LocalDate.now().minusMonths(1),
                LocalDate.now().plusMonths(11)
        );

        Vehiculo entidad = this.mapper.toEntity(dto);

        assertThat(entidad).isNotNull();
        assertThat(entidad.getMatricula()).isEqualTo("5678-BVC");
        assertThat(entidad.getMarca()).isEqualTo("Renault");
        assertThat(entidad.getModelo()).isEqualTo("Clio 1.5");
        assertThat(entidad.getColor()).isEqualTo("Rojo Pasión");
        assertThat(entidad.getKm()).isEqualTo(500L);
        assertThat(entidad.getCv()).isEqualTo(95);
        assertThat(entidad.getAnio()).isEqualTo(2023);
        assertThat(entidad.getTipoCombustible()).isEqualTo(TipoCombustible.DIESEL);
        assertThat(entidad.getCajaCambios()).isEqualTo(TipoCambio.MANUAL);
        assertThat(entidad.getTipoPermiso()).isEqualTo(TipoCarnet.PERMISO_B);
        assertThat(entidad.getEstado()).isEqualTo(EstadoVehiculo.DISPONIBLE);
    }

    @Test
    @DisplayName("toEditarVehiculoDTO mapea correctamente una entidad Vehiculo a EditarVehiculoDTO")
    void testToEditarVehiculoDTOMapeoCompleto()
    {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setMatricula("1234-LMN");
        vehiculo.setMarca("SEAT");
        vehiculo.setModelo("Ibiza 1.0 TSI");
        vehiculo.setColor("Blanco Nevada");
        vehiculo.setKm(45000L);
        vehiculo.setCv(110);
        vehiculo.setAnio(2022);
        vehiculo.setTipoCombustible(TipoCombustible.GASOLINA);
        vehiculo.setCajaCambios(TipoCambio.MANUAL);
        vehiculo.setFechaUltimaRevision(LocalDate.now().minusMonths(6));
        vehiculo.setFechaProximaRevision(LocalDate.now().plusMonths(6));
        vehiculo.setEstado(EstadoVehiculo.DISPONIBLE);
        vehiculo.setTipoPermiso(TipoCarnet.PERMISO_B);

        EditarVehiculoDTO dto = this.mapper.toEditarVehiculoDTO(vehiculo);

        assertThat(dto).isNotNull();
        assertThat(dto.matricula()).isEqualTo("1234-LMN");
        assertThat(dto.marca()).isEqualTo("SEAT");
        assertThat(dto.modelo()).isEqualTo("Ibiza 1.0 TSI");
        assertThat(dto.color()).isEqualTo("Blanco Nevada");
        assertThat(dto.km()).isEqualTo(45000L);
        assertThat(dto.cv()).isEqualTo(110);
        assertThat(dto.anio()).isEqualTo(2022);
        assertThat(dto.tipoCombustible()).isEqualTo(TipoCombustible.GASOLINA);
        assertThat(dto.cajaCambios()).isEqualTo(TipoCambio.MANUAL);
        assertThat(dto.tipoPermiso()).isEqualTo(TipoCarnet.PERMISO_B);
    }
}
