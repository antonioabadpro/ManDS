package com.autoescuela.erp.examenes.model;

import java.time.LocalDate;

import com.autoescuela.erp.core.enums.TipoCarnet;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad de dominio que representa los cupos máximos de examen fijados por tipo de carnet.
 * El cupo es fijo y aplica de forma independiente a ambas fechas de examen del mes especificado.
 */
@Entity
@Table(name = "cupo_examen", uniqueConstraints = {
    @UniqueConstraint(name = "uk_cupo_examen_mes_permiso", columnNames = {"mes", "tipo_permiso"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CupoExamen
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(nullable = false)
    private LocalDate mes;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_permiso", nullable = false)
    private TipoCarnet tipoPermiso;

    @Column(name = "cupo_teorico", nullable = false)
    private Integer cupoTeorico;

    @Column(name = "cupo_practico", nullable = false)
    private Integer cupoPractico;

    public CupoExamen(LocalDate mes, TipoCarnet tipoPermiso, Integer cupoTeorico, Integer cupoPractico)
    {
        this.mes = mes != null ? mes.withDayOfMonth(1) : null;
        this.tipoPermiso = tipoPermiso;
        this.cupoTeorico = cupoTeorico;
        this.cupoPractico = cupoPractico;
    }
}
