package com.autoescuela.erp.usuarios.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import com.autoescuela.erp.academico.model.Matricula;
import com.autoescuela.erp.core.enums.Rol;
import com.autoescuela.erp.examenes.model.Examen;
import com.autoescuela.erp.examenes.model.SolicitudExamen;
import com.autoescuela.erp.practicas.model.ClasePractica;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "alumno")
@Getter
@Setter
@NoArgsConstructor
public class Alumno extends Persona
{
    @OneToMany(mappedBy = "alumno")
    private List<ClasePractica> historialClasesPracticas = new ArrayList<>();

    @OneToMany(mappedBy = "alumno")
    private List<Examen> historialExamenes = new ArrayList<>();

    @OneToMany(mappedBy = "alumno")
    private List<Matricula> historialMatriculas = new ArrayList<>();

    @OneToMany(mappedBy = "alumno")
    private List<SolicitudExamen> listaSolicitudesExamen = new ArrayList<>();

    // Relaciones con otras entidades
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profesor_id")
    private Profesor profesor;

    @Override
    public Rol getRol()
    {
        return Rol.ALUMNO;
    }
}