package com.autoescuela.erp.usuarios.model;

import java.time.LocalDate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

import com.autoescuela.erp.core.enums.EstadoUsuario;
import com.autoescuela.erp.core.enums.Rol;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "persona")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // Solo se incluirán los campos anotados con @EqualsAndHashCode.Include
public abstract class Persona
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Genera automáticamente el valor del ID al insertar un nuevo registro en la base de datos
    @Setter(AccessLevel.NONE) // Evita que se genere el metodo setId() automáticamente
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellidos;

    @Column(nullable = false, unique = true)
    @EqualsAndHashCode.Include
    private String dni;

    @Column(nullable = false)
    private LocalDate fechaNacimiento;

    @Column(nullable = false, unique = true)
    private String nombreUsuario;

    @Column(nullable = false, unique = true)
    private String correo;

    @Column(nullable = false)
    @Setter(AccessLevel.NONE) // Evita que Lombok genere setPassword() público, protegiendo la integridad y previniendo modificaciones inseguras
    private String password;

    @Column(nullable = false)
    private String telefono;

    @Column(nullable = false)
    private String direccion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoUsuario estado;

    /**
     * Actualiza la contraseña de la persona de forma controlada.
     * La contraseña debe haber sido codificada previamente mediante PasswordEncoder (BCrypt).
     *
     * @param passwordCodificada Hash de la nueva contraseña (no puede ser nulo ni estar en blanco).
     */
    public void actualizarPassword(String passwordCodificada)
    {
        if (passwordCodificada == null || passwordCodificada.isBlank())
        {
            throw new IllegalArgumentException("La contraseña codificada no puede ser nula ni estar en blanco.");
        }
        this.password = passwordCodificada;
    }

    /**
     * Metodo abstracto y polimofrico que cada subclase debe implementar este método para devolver su rol correspondiente.
     * @return Devuelve el rol de la persona (Alumno, Profesor o Administrador) sin que el JPA lo persista en la base de datos.
     */
    public abstract Rol getRol(); // Método abstracto para obtener el rol de la persona

}
