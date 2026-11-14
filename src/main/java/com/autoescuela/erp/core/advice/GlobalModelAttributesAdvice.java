package com.autoescuela.erp.core.advice;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import jakarta.servlet.http.HttpServletRequest;

import com.autoescuela.erp.academico.model.Matricula;
import com.autoescuela.erp.academico.service.AcademicoService;
import com.autoescuela.erp.core.enums.ModalidadMatricula;
import com.autoescuela.erp.core.enums.Rol;
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
    public String obtenerNombreAlumno(@AuthenticationPrincipal Object principal, HttpServletRequest request)
    {
        if (principal instanceof UserDetailsImpl userDetails)
        {
            return userDetails.getNombreCompleto();
        }
        else if (principal != null)
        {
            Alumno alumno = this.obtenerAlumnoRequest(principal, request);
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
    public boolean esTeoricoPractica(@AuthenticationPrincipal Object principal, HttpServletRequest request)
    {
        Alumno alumno = this.obtenerAlumnoRequest(principal, request);
        if (alumno == null)
        {
            return false;
        }
        Matricula matricula = this.obtenerMatriculaActivaRequest(alumno, request);
        return matricula != null && matricula.getModalidad() == ModalidadMatricula.TEORICO_PRACTICA;

    }

    /**
     * Indica si el alumno ha agotado sus convocatorias de examen para alertas y restricciones.
     */
    @ModelAttribute("convocatoriasAgotadas")
    public boolean tieneConvocatoriasAgotadas(@AuthenticationPrincipal Object principal, HttpServletRequest request)
    {
        Alumno alumno = this.obtenerAlumnoRequest(principal, request);
        if (alumno == null)
        {
            return false;
        }
        Matricula matricula = this.obtenerMatriculaActivaRequest(alumno, request);
        return matricula != null && matricula.tieneConvocatoriasAgotadas();
    }

    /**
     * Indica si el alumno tiene aprobado el examen teórico.
     */
    @ModelAttribute("teoricoAprobado")
    public boolean teoricoAprobado(@AuthenticationPrincipal Object principal, HttpServletRequest request)
    {
        Alumno alumno = this.obtenerAlumnoRequest(principal, request);
        if (alumno == null)
        {
            return false;
        }
        return this.obtenerTeoricoAprobadoRequest(alumno, request);
    }

    private Alumno obtenerAlumnoSiAplica(Object principal)
    {
        if (principal instanceof UserDetailsImpl userDetails)
        {
            if (userDetails.getRol() != Rol.ALUMNO)
            {
                return null;
            }
            return this.alumnoService.obtenerAlumnoActual(principal);
        }
        else if (principal instanceof UserDetails)
        {
            return this.alumnoService.obtenerAlumnoActual(principal);
        }
        return null;
    }

    private Alumno obtenerAlumnoRequest(Object principal, HttpServletRequest request)
    {
        if (principal == null || this.esRutaSinVistasHtml(request))
        {
            return null;
        }
        if (request == null)
        {
            return this.obtenerAlumnoSiAplica(principal);
        }
        final String attr = "REQ_GLOBAL_ALUMNO";
        Object cached = request.getAttribute(attr);
        if (cached instanceof Alumno alumno)
        {
            return alumno;
        }
        Alumno alumno = this.obtenerAlumnoSiAplica(principal);
        if (alumno != null)
        {
            request.setAttribute(attr, alumno);
        }
        return alumno;
    }

    /**
     * Evita consultas de contexto de vistas Thymeleaf en endpoints técnicos, webhooks o eventos SSE/JSON.
     */
    private boolean esRutaSinVistasHtml(HttpServletRequest request)
    {
        if (request == null)
        {
            return false;
        }
        String uri = request.getRequestURI();
        if (uri == null || uri.isBlank())
        {
            return false;
        }
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath))
        {
            uri = uri.substring(contextPath.length());
        }
        return "/ping".equals(uri)
                || uri.startsWith("/pagos/webhook")
                || uri.startsWith("/eventos");
    }

    private Matricula obtenerMatriculaActivaRequest(Alumno alumno, HttpServletRequest request)
    {
        if (alumno == null)
        {
            return null;
        }
        if (request == null)
        {
            return this.academicoService.buscarMatriculaActiva(alumno).orElse(null);
        }
        final String attrChecked = "REQ_GLOBAL_MATRICULA_CHECKED";
        final String attrMatricula = "REQ_GLOBAL_MATRICULA";
        if (Boolean.TRUE.equals(request.getAttribute(attrChecked)))
        {
            return (Matricula) request.getAttribute(attrMatricula);
        }
        Matricula matricula = this.academicoService.buscarMatriculaActiva(alumno).orElse(null);
        request.setAttribute(attrMatricula, matricula);
        request.setAttribute(attrChecked, Boolean.TRUE);
        return matricula;
    }

    private boolean obtenerTeoricoAprobadoRequest(Alumno alumno, HttpServletRequest request)
    {
        if (alumno == null)
        {
            return false;
        }
        if (request == null)
        {
            return this.examenService.tieneTeoricoAprobado(alumno);
        }
        final String attr = "REQ_GLOBAL_TEORICO_APROBADO";
        Object cached = request.getAttribute(attr);
        if (cached instanceof Boolean b)
        {
            return b;
        }
        boolean aprobado = this.examenService.tieneTeoricoAprobado(alumno);
        request.setAttribute(attr, aprobado);
        return aprobado;
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
