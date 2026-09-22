package com.autoescuela.erp.usuarios.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.autoescuela.erp.usuarios.model.Profesor;

public interface ProfesorRepository extends JpaRepository<Profesor, Long>
{
    /**
     * Busca un profesor a partir de su Documento Nacional de Identidad o NIE.
     *
     * @param dni DNI o NIE del docente.
     * @return Optional con el Profesor encontrado o vacío si no existe.
     */
    Optional<Profesor> findByDni(String dni);

    /**
     * Busca un profesor por su dirección de correo electrónico.
     *
     * @param correo Dirección de correo electrónico.
     * @return Optional con el Profesor encontrado o vacío si no existe.
     */
    Optional<Profesor> findByCorreo(String correo);

    /**
     * Busca un profesor por su nombre de usuario.
     *
     * @param nombreUsuario Nombre de usuario.
     * @return Optional con el Profesor encontrado o vacío si no existe.
     */
    Optional<Profesor> findByNombreUsuario(String nombreUsuario);
}
