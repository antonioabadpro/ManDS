package com.autoescuela.erp.usuarios.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.autoescuela.erp.usuarios.model.Persona;

public interface PersonaRepository extends JpaRepository<Persona, Long>
{
    /**
     * Busca una persona por su nombre de usuario o por su correo electrónico.
     * Permite la autenticación dual requerida por la regla de negocio 7.1.
     * @param nombreUsuario Nombre de usuario de la persona.
     * @param correo Correo electrónico de la persona.
     * @return Un Optional con la Persona encontrada o vacío si no existe.
     */
    Optional<Persona> findByNombreUsuarioOrCorreo(String nombreUsuario, String correo);

    /**
     * Comprueba si ya existe un usuario registrado con el nombre de usuario indicado.
     */
    boolean existsByNombreUsuario(String nombreUsuario);

    /**
     * Comprueba si ya existe un usuario registrado con el correo electrónico indicado.
     */
    boolean existsByCorreo(String correo);

    /**
     * Comprueba si ya existe un usuario registrado con el DNI indicado.
     */
    boolean existsByDni(String dni);
}
