package com.autoescuela.erp.core.security;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.autoescuela.erp.core.enums.EstadoUsuario;
import com.autoescuela.erp.core.enums.Rol;
import com.autoescuela.erp.usuarios.model.Persona;

import lombok.Getter;

/**
 * Adaptador de seguridad que implementa {@link UserDetails} para Spring Security.
 * Encapsula la entidad {@link Persona} y expone sus credenciales, autoridades y datos de perfil en el ambito de la sesión.
 */
@Getter
public class UserDetailsImpl implements UserDetails
{
    private final Long id;
    private final String nombre;
    private final String nombreCompleto;
    private final String nombreUsuario;
    private final String correo;
    private final String password;
    private final Rol rol;
    private final boolean estaActivo;
    private final Collection<? extends GrantedAuthority> roles;

    public UserDetailsImpl(Persona persona)
    {
        this.id = persona.getId();
        this.nombre = persona.getNombre();
        this.nombreCompleto = persona.getNombre() + " " + persona.getApellidos();
        this.nombreUsuario = persona.getNombreUsuario();
        this.correo = persona.getCorreo();
        this.password = persona.getPassword();
        this.rol = persona.getRol();
        this.estaActivo = persona.getEstado() == EstadoUsuario.ACTIVO;
        this.roles = List.of(new SimpleGrantedAuthority("ROLE_" + persona.getRol().name()));
    }

    /**
     * @return Collection<? extends GrantedAuthority> - Devuelve la colección de autoridades (roles) del usuario.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities()
    {
        return this.roles;
    }

    @Override
    public String getPassword()
    {
        return this.password;
    }

    @Override
    public String getUsername()
    {
        return this.nombreUsuario;
    }

    @Override
    public boolean isAccountNonExpired()
    {
        return true;
    }

    @Override
    public boolean isAccountNonLocked()
    {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired()
    {
        return true;
    }

    @Override
    public boolean isEnabled()
    {
        return this.estaActivo;
    }

    /**
     * Sobrescritura obligatoria de equals y hashCode para permitir que SessionRegistry
     * gestione de forma precisa el control de concurrencia de sesiones (1 sesión activa por usuario).
     */
    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof UserDetailsImpl that))
        {
            return false;
        }
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.id);
    }
}
