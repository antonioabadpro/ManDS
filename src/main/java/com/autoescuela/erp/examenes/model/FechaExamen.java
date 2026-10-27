package com.autoescuela.erp.examenes.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * Entidad de dominio que representa las dos convocatorias oficiales fijadas para un mes.
 * Una vez consolidadas para un mes concreto, adquieren carácter inmutable.
 */
@Entity
@Table(name = "fecha_examen", uniqueConstraints = {
    @UniqueConstraint(name = "uk_fecha_examen_mes", columnNames = {"mes"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FechaExamen
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate mes;

    @Column(name = "fecha_hora_1", nullable = false)
    private LocalDateTime fechaHora1;

    @Column(name = "fecha_hora_2", nullable = false)
    private LocalDateTime fechaHora2;

    public FechaExamen(LocalDate mes, LocalDateTime fechaHora1, LocalDateTime fechaHora2)
    {
        this.mes = mes != null ? mes.withDayOfMonth(1) : null;
        this.fechaHora1 = fechaHora1;
        this.fechaHora2 = fechaHora2;
    }
}
