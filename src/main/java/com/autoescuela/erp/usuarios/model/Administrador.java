package com.autoescuela.erp.usuarios.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "administrador")
public class Administrador extends Persona
{
    public Administrador()
    {
    }
}