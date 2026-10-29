package com.autoescuela.erp.usuarios.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.autoescuela.erp.academico.model.Matricula;
import com.autoescuela.erp.academico.service.AcademicoService;
import com.autoescuela.erp.core.enums.EstadoClase;
import com.autoescuela.erp.core.enums.ModalidadMatricula;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.examenes.model.SolicitudExamen;
import com.autoescuela.erp.examenes.service.ExamenService;
import com.autoescuela.erp.practicas.model.ClasePractica;
import com.autoescuela.erp.practicas.service.ClasePracticaService;
import com.autoescuela.erp.usuarios.dto.EditarPerfilAlumnoDTO;
import com.autoescuela.erp.usuarios.mapper.AlumnoMapper;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.model.Profesor;
import com.autoescuela.erp.usuarios.service.AlumnoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador principal para el Portal del Alumno (ROLE_ALUMNO).
 * Gestiona el cuadro de mando, el acceso a test teóricos y el perfil personal.
 */
@Controller
@RequestMapping("/alumno")
@RequiredArgsConstructor
public class AlumnoController
{
    private final AlumnoService alumnoService;
    private final AcademicoService academicoService;
    private final ExamenService examenService;
    private final ClasePracticaService clasePracticaService;
    private final AlumnoMapper alumnoMapper;


    /**
     * Calcula la capacidad de reserva del alumno según la fórmula oficial:
     * CapacidadReserva = saldoClases - clasesReservadasPendientes
     */
    private int calcularCapacidadReserva(Alumno alumno, Matricula matricula)
    {
        int capacidadReserva = 0;
        int saldoClases = (matricula != null && matricula.getSaldoClases() != null) ? matricula.getSaldoClases() : 0;
        int clasesReservadas = this.clasePracticaService.contarClasesPendientesPorAlumno(alumno);
        capacidadReserva = saldoClases - clasesReservadas;
        return capacidadReserva;
    }

    // =========================================================================
    // 1. DASHBOARD GENERAL DEL ALUMNO
    // =========================================================================
    @GetMapping("/dashboard")
    public String mostrarDashboard(@AuthenticationPrincipal Object principal, Model model)
    {
        Alumno alumno = this.alumnoService.obtenerAlumnoActual(principal);
        if (alumno == null)
        {
            return "redirect:/login";
        }

        Matricula matricula = this.academicoService.buscarMatriculaActiva(alumno).orElse(null);
        Profesor profesor = alumno.getProfesor();

        int capacidadReserva = this.calcularCapacidadReserva(alumno, matricula);

        // Próxima clase pendiente futura
        LocalDateTime ahora = LocalDateTime.now();
        List<ClasePractica> todasClases = this.clasePracticaService.obtenerClasesPorAlumnoAsc(alumno);
        ClasePractica proximaClase = null;
        for (ClasePractica c : todasClases)
        {
            if (c.getEstadoClase() == EstadoClase.PENDIENTE && c.getFechaHora().isAfter(ahora))
            {
                proximaClase = c;
                break;
            }
        }

        // Últimas clases prácticas para el resumen
        List<ClasePractica> clasesRecientes = this.clasePracticaService.obtenerClasesPorAlumnoDesc(alumno);
        if (clasesRecientes.size() > 5)
        {
            clasesRecientes = clasesRecientes.subList(0, 5);
        }

        // Solicitudes de examen recientes
        List<SolicitudExamen> solicitudesRecientes = this.examenService.obtenerSolicitudesRecientesPorAlumno(alumno);
        if (solicitudesRecientes.size() > 3)
        {
            solicitudesRecientes = solicitudesRecientes.subList(0, 3);
        }

        boolean convocatoriasAgotadas = matricula != null && matricula.tieneConvocatoriasAgotadas();
        boolean esMatriculaActiva = matricula != null && Boolean.TRUE.equals(matricula.getEstaActiva());

        model.addAttribute("alumno", alumno);
        model.addAttribute("matricula", matricula);
        model.addAttribute("convocatoriasAgotadas", convocatoriasAgotadas);
        model.addAttribute("esMatriculaActiva", esMatriculaActiva);
        model.addAttribute("profesor", profesor);
        model.addAttribute("capacidadReserva", capacidadReserva);
        model.addAttribute("proximaClase", proximaClase);
        model.addAttribute("clasesRecientes", clasesRecientes);
        model.addAttribute("solicitudesRecientes", solicitudesRecientes);

        return "alumno/dashboard";
    }

    // =========================================================================
    // 1.1. TEST TEÓRICOS (Solo modalidad TEORICO_PRACTICA)
    // =========================================================================
    @GetMapping("/tests")
    public String mostrarTestsTeoricos(@AuthenticationPrincipal Object principal, Model model, RedirectAttributes redirectAttributes)
    {
        Alumno alumno = this.alumnoService.obtenerAlumnoActual(principal);
        if (alumno == null)
        {
            return "redirect:/login";
        }

        Optional<Matricula> matriculaOptional = this.academicoService.buscarMatriculaActiva(alumno);
        if (matriculaOptional.isEmpty() || matriculaOptional.get().getModalidad() != ModalidadMatricula.TEORICO_PRACTICA)
        {
            return "redirect:/alumno/dashboard";
        }

        Matricula matricula = matriculaOptional.get();
        if (matricula.tieneConvocatoriasAgotadas())
        {
            redirectAttributes.addFlashAttribute("error", "Has agotado las convocatorias de tu matrícula. Debes tramitar la renovación de matrícula para poder seguir accediendo al apartado de test teóricos.");
            return "redirect:/alumno/dashboard";
        }

        model.addAttribute("alumno", alumno);
        model.addAttribute("matricula", matricula);
        return "alumno/tests";
    }

    // =========================================================================
    // 8. PERFIL DEL ALUMNO (CONSULTA Y EDICIÓN)
    // =========================================================================
    @GetMapping("/perfil")
    public String mostrarPerfil(@AuthenticationPrincipal Object principal, Model model)
    {
        Alumno alumno = this.alumnoService.obtenerAlumnoActual(principal);
        if (alumno == null)
        {
            return "redirect:/login";
        }

        Matricula matricula = this.academicoService.buscarMatriculaActiva(alumno).orElse(null);
        Profesor profesor = alumno.getProfesor();

        EditarPerfilAlumnoDTO dto = new EditarPerfilAlumnoDTO(
                alumno.getId(),
                alumno.getNombre(),
                alumno.getApellidos(),
                alumno.getTelefono(),
                alumno.getDireccion(),
                alumno.getCorreo(),
                alumno.getFechaNacimiento(),
                alumno.getDni(),
                alumno.getNombreUsuario(),
                profesor != null ? profesor.getNombre() + " " + profesor.getApellidos() : null,
                profesor != null ? profesor.getTelefono() : null,
                profesor != null ? profesor.getCorreo() : null,
                profesor != null ? profesor.getTurno() : null,
                (profesor != null && profesor.getVehiculo() != null) ? profesor.getVehiculo().getMarca() : null,
                (profesor != null && profesor.getVehiculo() != null) ? profesor.getVehiculo().getModelo() : null,
                (profesor != null && profesor.getVehiculo() != null) ? profesor.getVehiculo().getMatricula() : null,
                matricula != null ? matricula.getPermisoCarnet() : null,
                matricula != null ? matricula.getFechaMatriculacion() : null,
                matricula != null ? matricula.getSaldoClases() : 0,
                matricula != null ? matricula.getConvocatorias() : 2
        );

        model.addAttribute("perfilDTO", dto);
        return "alumno/perfil";
    }

    @PostMapping("/perfil")
    @Transactional
    public String actualizarPerfil(@AuthenticationPrincipal Object principal, @Valid @ModelAttribute("perfilDTO") EditarPerfilAlumnoDTO dto, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes)
    {
        Alumno alumno = this.alumnoService.obtenerAlumnoActual(principal);
        if (alumno == null)
        {
            return "redirect:/login";
        }

        Matricula matricula = this.academicoService.buscarMatriculaActiva(alumno).orElse(null);

        if (bindingResult.hasErrors())
        {
            EditarPerfilAlumnoDTO perfilRepoblado = this.alumnoMapper.repoblarPerfilDTO(alumno, matricula, dto);
            model.addAttribute("perfilDTO", perfilRepoblado);
            model.addAttribute(BindingResult.MODEL_KEY_PREFIX + "perfilDTO", bindingResult);
            return "alumno/perfil";
        }

        try
        {
            this.alumnoService.actualizarPerfil(alumno.getId(), dto);
            redirectAttributes.addFlashAttribute("mensajeExito", "Tus datos personales se han actualizado correctamente.");
            return "redirect:/alumno/perfil";
        }
        catch (ReglaNegocioException ex)
        {
            EditarPerfilAlumnoDTO perfilRepoblado = this.alumnoMapper.repoblarPerfilDTO(alumno, matricula, dto);
            model.addAttribute("perfilDTO", perfilRepoblado);
            model.addAttribute("error", ex.getMessage());
            return "alumno/perfil";
        }
    }

    @PostMapping("/perfil/cambiar-password")
    @Transactional
    public String cambiarPassword(@AuthenticationPrincipal Object principal, @RequestParam("passwordActual") String passwordActual, @RequestParam("nuevaPassword") String nuevaPassword, @RequestParam("confirmarPassword") String confirmarPassword, RedirectAttributes redirectAttributes)
    {
        Alumno alumno = this.alumnoService.obtenerAlumnoActual(principal);
        if (alumno == null)
        {
            return "redirect:/login";
        }

        try
        {
            this.alumnoService.cambiarPassword(alumno.getId(), passwordActual, nuevaPassword, confirmarPassword);
            redirectAttributes.addFlashAttribute("mensajeExito", "Contraseña actualizada satisfactoriamente.");
        }
        catch (ReglaNegocioException ex)
        {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/alumno/perfil";
    }
}

