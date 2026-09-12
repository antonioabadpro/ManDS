package com.autoescuela.erp.usuarios.model;

import com.autoescuela.erp.core.enums.Rol;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.enums.TipoTurno;
import com.autoescuela.erp.examenes.model.SolicitudExamen;
import com.autoescuela.erp.flota.model.Vehiculo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "profesor")
@Getter
@Setter
@NoArgsConstructor
public class Profesor extends Persona
{
    @Column(nullable = false)
    private LocalDate fechaContratacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoTurno turno;

    @OneToMany(mappedBy = "profesor")
    private List<Alumno> listaAlumnos = new ArrayList<>();

    @OneToMany(mappedBy = "profesor")
    private List<SolicitudExamen> listaSolicitudesExamen = new ArrayList<>();

    @ElementCollection(targetClass = TipoCarnet.class)
    @CollectionTable(name = "profesor_permisos", joinColumns = @JoinColumn(name = "profesor_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_carnet", nullable = false)
    private List<TipoCarnet> listaTiposCarnet = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehiculo_id", unique = true)
    private Vehiculo vehiculo;

    @Override
    public Rol getRol()
    {
        return Rol.PROFESOR;
    }

}