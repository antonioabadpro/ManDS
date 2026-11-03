package com.autoescuela.erp.usuarios.model;

import com.autoescuela.erp.core.enums.Rol;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "administrador")
@Getter
@Setter
@NoArgsConstructor
public class Administrador extends Persona
{

    @Override
    public Rol getRol()
    {
        return Rol.ADMIN;
    }

}