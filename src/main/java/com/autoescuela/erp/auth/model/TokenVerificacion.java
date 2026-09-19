package com.autoescuela.erp.auth.model;

import java.time.LocalDateTime;

import com.autoescuela.erp.usuarios.model.Persona;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa un token criptográfico temporal para procesos de verificación
 * (recuperación de contraseña, invitación a profesores o verificación de cuenta).
 */
@Entity
@Table(name = "token_verificacion")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TokenVerificacion
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(nullable = false, unique = true)
    @EqualsAndHashCode.Include
    private String token;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

    @Column(nullable = false)
    private LocalDateTime fechaExpiracion;

    @Column(name = "usado", nullable = false)
    private boolean usado;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    public TokenVerificacion(String token, Persona persona, LocalDateTime fechaExpiracion)
    {
        if (token == null || token.isBlank())
        {
            throw new IllegalArgumentException("El token no puede ser nulo ni estar vacío.");
        }
        if (persona == null)
        {
            throw new IllegalArgumentException("La persona asociada al token no puede ser nula.");
        }
        if (fechaExpiracion == null)
        {
            throw new IllegalArgumentException("La fecha de expiración no puede ser nula.");
        }

        this.token = token;
        this.persona = persona;
        this.fechaExpiracion = fechaExpiracion;
        this.fechaCreacion = LocalDateTime.now();
        this.usado = false;
    }

    /**
     * Comprueba si el token ha superado su tiempo límite de validez.
     * @return true si ha expirado, false en caso contrario.
     */
    public boolean isExpirado()
    {
        return LocalDateTime.now().isAfter(this.fechaExpiracion);
    }

    /**
     * Comprueba si el token es utilizable (no consumido y dentro del tiempo límite).
     * @return true si es válido, false en caso contrario.
     */
    public boolean isValido()
    {
        return !this.usado && !isExpirado();
    }

    /**
     * Indica si el token ha sido consumido para evitar reutilizaciones.
     * @return true si ya fue usado, false en caso contrario.
     */
    public boolean isUsado()
    {
        return this.usado;
    }

    /**
     * Marca el token como consumido para evitar reutilizaciones.
     */
    public void marcarComoUsado()
    {
        this.usado = true;
    }
}
