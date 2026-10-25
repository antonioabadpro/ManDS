package com.autoescuela.erp.core.advice;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.autoescuela.erp.academico.model.Matricula;
import com.autoescuela.erp.academico.service.AcademicoService;
import com.autoescuela.erp.core.enums.ModalidadMatricula;
import com.autoescuela.erp.core.enums.TipoCambio;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.enums.TipoCombustible;
import com.autoescuela.erp.core.enums.TipoTurno;
import com.autoescuela.erp.core.security.UserDetailsImpl;
import com.autoescuela.erp.examenes.service.ExamenService;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.service.AlumnoService;

import lombok.RequiredArgsConstructor;

/**
 * Componente transversal para suministrar atributos globales de sesion y usuario
 * a todas las plantillas Thymeleaf de la aplicacion (barras de navegacion, cabeceras y menus laterales).
 */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributesAdvice
{
    private final AlumnoService alumnoService;
    private final AcademicoService academicoService;
    private final ExamenService examenService;

    /**
     * Proporciona el nombre de usuario autenticado para los pies de menu y cabeceras.
     */
    @ModelAttribute("nomUsuario")
    public String obtenerNombreUsuario(@AuthenticationPrincipal Object principal)
    {
        if (principal instanceof UserDetailsImpl userDetails)
        {
            return userDetails.getNombreUsuario();
        }
        else if (principal instanceof UserDetails user)
        {
            return user.getUsername();
        }
        return "usuario";
    }

    /**
     * Proporciona el nombre completo del Administrador para su portal.
     */
    @ModelAttribute("nombreAdmin")
    public String obtenerNombre(@AuthenticationPrincipal Object principal)
    {
        if (principal instanceof UserDetailsImpl userDetails)
        {
            return userDetails.getNombreCompleto();
        }
        else if (principal instanceof UserDetails user)
        {
            return user.getUsername();
        }
        return "Admin";
    }

    /**
     * Proporciona el nombre completo del Alumno para su portal.
     */
    @ModelAttribute("nombreAlumno")
    public String obtenerNombreAlumno(@AuthenticationPrincipal Object principal)
    {
        if (principal instanceof UserDetailsImpl userDetails)
        {
            return userDetails.getNombreCompleto();
        }
        else if (principal != null)
        {
            Alumno alumno = this.alumnoService.obtenerAlumnoActual(principal);
            if (alumno != null)
            {
                return alumno.getNombre() + " " + alumno.getApellidos();
            }
        }
        return "Alumno";
    }

    /**
     * Proporciona el nombre completo del Profesor para su portal.
     */
    @ModelAttribute("nombreProfesor")
    public String obtenerNombreProfesor(@AuthenticationPrincipal Object principal)
    {
        if (principal instanceof UserDetailsImpl userDetails)
        {
            return userDetails.getNombreCompleto();
        }
        else if (principal instanceof UserDetails user)
        {
            return user.getUsername();
        }
        return "Profesor";
    }

    /**
     * Indica si el alumno conectado esta en modalidad Teórico + Práctica para el menu lateral.
     */
    @ModelAttribute("esTeoricoPractica")
    public boolean esTeoricoPractica(@AuthenticationPrincipal Object principal)
    {
        if (principal == null)
        {
            return false;
        }
        Alumno alumno = this.alumnoService.obtenerAlumnoActual(principal);
        if (alumno == null)
        {
            return false;
        }
        return this.academicoService.buscarMatriculaActiva(alumno)
                .map(m -> m.getModalidad() == ModalidadMatricula.TEORICO_PRACTICA)
                .orElse(false);
    }

    /**
     * Indica si el alumno ha agotado sus convocatorias de examen para alertas y restricciones.
     */
    @ModelAttribute("convocatoriasAgotadas")
    public boolean tieneConvocatoriasAgotadas(@AuthenticationPrincipal Object principal)
    {
        if (principal == null)
        {
            return false;
        }
        Alumno alumno = this.alumnoService.obtenerAlumnoActual(principal);
        if (alumno == null)
        {
            return false;
        }
        Matricula matricula = this.academicoService.buscarMatriculaActiva(alumno).orElse(null);
        return matricula != null && matricula.tieneConvocatoriasAgotadas();
    }

    /**
     * Indica si el alumno tiene aprobado el examen teórico.
     */
    @ModelAttribute("teoricoAprobado")
    public boolean teoricoAprobado(@AuthenticationPrincipal Object principal)
    {
        if (principal == null)
        {
            return false;
        }
        Alumno alumno = this.alumnoService.obtenerAlumnoActual(principal);
        if (alumno == null)
        {
            return false;
        }
        return this.examenService.tieneTeoricoAprobado(alumno);
    }

    /**
     * Catálogo global de permisos de conducir para modales y selectores.
     */
    @ModelAttribute("tiposCarnet")
    public TipoCarnet[] tiposCarnet()
    {
        return TipoCarnet.values();
    }

    /**
     * Catálogo global de turnos de trabajo para modales y selectores.
     */
    @ModelAttribute("turnos")
    public TipoTurno[] turnos()
    {
        return TipoTurno.values();
    }

    /**
     * Catálogo global de tipos de combustible para la flota de vehículos.
     */
    @ModelAttribute("tiposCombustible")
    public TipoCombustible[] tiposCombustible()
    {
        return TipoCombustible.values();
    }

    /**
     * Catálogo global de tipos de transmisión/caja de cambios para la flota de vehículos.
     */
    @ModelAttribute("tiposCambio")
    public TipoCambio[] tiposCambio()
    {
        return TipoCambio.values();
    }

    /**
     * Catálogo global de modalidades de matrícula para selectores y filtros.
     */
    @ModelAttribute("modalidadesMatricula")
    public ModalidadMatricula[] modalidadesMatricula()
    {
        return ModalidadMatricula.values();
    }
}
