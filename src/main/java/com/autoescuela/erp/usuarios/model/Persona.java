package com.autoescuela.erp.usuarios.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

@Entity
@Table(name = "persona")
@Inheritance(strategy = InheritanceType.JOINED) // Crea una tabla Persona y tablas hijas enlazadas por ID
public abstract class Persona
{

}
