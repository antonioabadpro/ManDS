package com.autoescuela.erp.academico.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.autoescuela.erp.core.enums.ModalidadMatricula;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.enums.TipoMatricula;
import com.autoescuela.erp.examenes.model.SolicitudExamen;
import com.autoescuela.erp.usuarios.model.Alumno;

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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "matricula")
@Getter
@Setter
@NoArgsConstructor
public class Matricula
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(nullable = false)
    private Boolean estaActiva = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoCarnet permisoCarnet;

    private Integer convocatorias = 2;

    private Integer saldoClases = 0;

    private Integer numClasesPendientesConfirmar = 0;

    private Integer convocatoriasGastadas = 0;

    @Column(nullable = false)
    private Float precio;

    @Column(nullable = false)
    private LocalDate fechaMatriculacion;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoMatricula tipo;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ModalidadMatricula modalidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alumno_id", nullable = false)
    private Alumno alumno;

    @OneToMany(mappedBy = "matricula")
    private List<SolicitudExamen> listaSolicitudesExamen = new ArrayList<>();
}