package com.autoescuela.erp.practicas.model;

import java.time.LocalDateTime;

import com.autoescuela.erp.core.enums.EstadoClase;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.model.Profesor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "clase_practica")
@Getter
@Setter
@NoArgsConstructor
public class ClasePractica
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    @Column(nullable = false)
    private Integer duracion;

    @Column(nullable = false, length = 100)
    private String puntoRecogida;

    @Column(nullable = false)
    private Long kmInicio;

    @Column(nullable = false)
    private Long kmFin;

    @Column(length = 1000)
    private String observaciones;

    @Column(length = 500)
    private String motivoCancelacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoClase estadoClase = EstadoClase.PENDIENTE;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private Boolean isCanceladaPorProfesor = false;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private Boolean isCancelacionJornada = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alumno_id")
    private Alumno alumno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profesor_id")
    private Profesor profesor;

    /**
     * Obtiene el motivo de cancelación de la clase práctica con fallback descriptivo.
     *
     * @return Motivo descriptivo de la cancelación para Alumno y Profesor.
     */
    public String getMotivoCancelacion()
    {
        if (this.motivoCancelacion != null && !this.motivoCancelacion.isBlank())
        {
            return this.motivoCancelacion;
        }
        if (Boolean.TRUE.equals(this.isCanceladaPorProfesor))
        {
            return "Cancelada por el profesor debido a imprevistos en la jornada docente.";
        }
        return "Cancelada por el alumno para liberar la franja horaria.";
    }
}
