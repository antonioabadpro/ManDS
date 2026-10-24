package com.autoescuela.erp.usuarios.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.autoescuela.erp.academico.model.Matricula;
import com.autoescuela.erp.academico.service.AcademicoService;
import com.autoescuela.erp.core.enums.EstadoClase;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.examenes.model.Examen;
import com.autoescuela.erp.examenes.service.ExamenService;
import com.autoescuela.erp.flota.model.Vehiculo;
import com.autoescuela.erp.practicas.model.ClasePractica;
import com.autoescuela.erp.practicas.service.ClasePracticaService;
import com.autoescuela.erp.usuarios.dto.AlumnoDetalleDTO;
import com.autoescuela.erp.usuarios.dto.CambiarPasswordDTO;
import com.autoescuela.erp.usuarios.dto.ContactarAlumnoDTO;
import com.autoescuela.erp.usuarios.dto.EditarPerfilProfesorDTO;
import com.autoescuela.erp.usuarios.mapper.ProfesorMapper;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.model.Profesor;
import com.autoescuela.erp.usuarios.service.AlumnoService;
import com.autoescuela.erp.usuarios.service.ProfesorService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador principal para el Portal del Profesor (ROLE_PROFESOR).
 * Gestiona el cuadro de mando, los alumnos asignados y el perfil personal.
 */
@Controller
@RequestMapping("/profesor")
@RequiredArgsConstructor
public class ProfesorController
{
    private final ProfesorService profesorService;
    private final AlumnoService alumnoService;
    private final AcademicoService academicoService;
    private final ExamenService examenService;
    private final ClasePracticaService clasePracticaService;
    private final ProfesorMapper profesorMapper;


    // =========================================================================
    // 1. DASHBOARD GENERAL DEL PROFESOR
    // =========================================================================
    @GetMapping("/dashboard")
    public String mostrarDashboard(@AuthenticationPrincipal Object principal, Model model)
    {
        Profesor profesor = this.profesorService.obtenerProfesorActual(principal);
        if (profesor == null)
        {
            return "redirect:/login";
        }

        LocalDate hoy = LocalDate.now();
        LocalDateTime inicioHoy = hoy.atStartOfDay();
        LocalDateTime finHoy = hoy.atTime(23, 59, 59);

        // Clases prácticas del día de hoy
        List<ClasePractica> clasesHoy = this.clasePracticaService
                .obtenerClasesPorProfesorYRango(profesor, inicioHoy, finHoy);

        // Próxima clase pendiente del profesor
        List<ClasePractica> todasClases = this.clasePracticaService.obtenerClasesPorProfesorAsc(profesor);
        ClasePractica proximaClase = null;
        LocalDateTime ahora = LocalDateTime.now();

        // Buscar la próxima clase pendiente que esté programada para después de la fecha y hora actual
        for (ClasePractica clase : todasClases)
        {
            if (clase.getEstadoClase() == EstadoClase.PENDIENTE && clase.getFechaHora().isAfter(ahora))
            {
                proximaClase = clase;
                break;
            }
        }

        // Contar el número de alumnos asignados al profesor
        List<Alumno> misAlumnos = this.alumnoService.buscarPorProfesor(profesor);
        // Obtener el vehículo asignado al profesor
        Vehiculo vehiculo = profesor.getVehiculo();

        // Contar el número de exámenes pendientes de calificación del profesor
        List<Examen> misExamenes = this.examenService.obtenerExamenesPorProfesor(profesor);
        long pendientesCalificar = 0;
        for (Examen examen : misExamenes)
        {
            if (examen.getEsApto() == null)
            {
                pendientesCalificar++;
            }
        }

        model.addAttribute("profesor", profesor);
        model.addAttribute("clasesHoy", clasesHoy);
        model.addAttribute("proximaClase", proximaClase);
        model.addAttribute("totalAlumnos", misAlumnos.size());
        model.addAttribute("vehiculo", vehiculo);
        model.addAttribute("pendientesCalificar", pendientesCalificar);

        return "profesor/dashboard";
    }

    // =========================================================================
    // 3. MIS ALUMNOS
    // =========================================================================
    @GetMapping("/alumnos")
    public String mostrarAlumnos(@AuthenticationPrincipal Object principal, Model model)
    {
        Profesor profesor = this.profesorService.obtenerProfesorActual(principal);
        if (profesor == null)
        {
            return "redirect:/login";
        }

        List<Alumno> alumnos = this.alumnoService.buscarPorProfesor(profesor);
        List<ClasePractica> todasLasClasesProfesor = this.clasePracticaService.obtenerClasesPorProfesorAsc(profesor);

        // Estructura de apoyo para renderizar expedientes de cada alumno
        List<Map<String, Object>> fichasAlumnos = new ArrayList<>();
        for (Alumno alumno : alumnos)
        {
            Map<String, Object> ficha = new HashMap<>();
            ficha.put("alumno", alumno);

            Optional<Matricula> matriculaOptional = this.academicoService.buscarMatriculaActiva(alumno);
            if (matriculaOptional.isPresent())
            {
                Matricula matricula = matriculaOptional.get();
                ficha.put("matricula", matricula);
                ficha.put("saldoClases", matricula.getSaldoClases() != null ? matricula.getSaldoClases() : 0);
                ficha.put("convocatoriasGastadas", matricula.getConvocatoriasGastadas() != null ? matricula.getConvocatoriasGastadas() : 0);
                ficha.put("permiso", (matricula.getPermisoCarnet() != null && matricula.getPermisoCarnet().getDescripcion() != null)
                                        ? matricula.getPermisoCarnet().getDescripcion(): "Permiso B");
            }
            else
            {
                ficha.put("matricula", null);
                ficha.put("saldoClases", 0);
                ficha.put("convocatoriasGastadas", 0);
                ficha.put("permiso", "Permiso B");
            }

            List<ClasePractica> clasesAlumno = new ArrayList<>();
            long clasesRecibidas = 0;
            for (ClasePractica clase : todasLasClasesProfesor)
            {
                if (clase.getAlumno() != null && clase.getAlumno().getId().equals(alumno.getId()))
                {
                    clasesAlumno.add(clase);
                    if (clase.getEstadoClase() == EstadoClase.RECIBIDA)
                    {
                        clasesRecibidas++;
                    }
                }
            }

            ficha.put("clasesRecibidas", clasesRecibidas);
            ficha.put("historialClases", clasesAlumno);

            String iniciales = (alumno.getNombre() != null && !alumno.getNombre().isBlank() ? alumno.getNombre().trim().substring(0, 1).toUpperCase() : "")
                             + (alumno.getApellidos() != null && !alumno.getApellidos().isBlank() ? alumno.getApellidos().trim().substring(0, 1).toUpperCase() : "");
            ficha.put("iniciales", iniciales.isBlank() ? "AL" : iniciales);

            fichasAlumnos.add(ficha);
        }

        model.addAttribute("profesor", profesor);
        model.addAttribute("fichasAlumnos", fichasAlumnos);
        model.addAttribute("contactarDTO", new ContactarAlumnoDTO());

        return "profesor/alumnos";
    }

    /**
     * Endpoint HTMX para cargar y presentar los datos informativos del alumno en el modal de detalle para el profesor.
     */
    @GetMapping("/alumnos/detalle/{id}")
    public String cargarModalDetalleAlumno(@PathVariable("id") Long id, @AuthenticationPrincipal Object principal, Model model)
    {
        Profesor profesor = this.profesorService.obtenerProfesorActual(principal);
        if (profesor == null)
        {
            return "redirect:/login";
        }

        AlumnoDetalleDTO dto = this.alumnoService.obtenerAlumnoParaDetalle(id);
        if (dto.profesorId() == null || !dto.profesorId().equals(profesor.getId()))
        {
            throw new ReglaNegocioException("No tiene permisos para consultar el expediente de un alumno que no tiene asignado.");
        }

        model.addAttribute("alumno", dto);
        model.addAttribute("abrirModalDetalleAlumno", true);

        return "profesor/fragments/modal-detalle-alumno :: modal-detalle-alumno";
    }

    /**
     * Envía un mensaje directo por correo a un alumno tutelado.
     */
    @PostMapping("/alumnos/contactar")
    public String contactarAlumno(@AuthenticationPrincipal Object principal, @Valid @ModelAttribute("contactarDTO") ContactarAlumnoDTO dto, BindingResult bindingResult, RedirectAttributes redirectAttributes)
    {
        if (bindingResult.hasErrors())
        {
            redirectAttributes.addFlashAttribute("error", "El asunto y el cuerpo del mensaje no pueden estar vacíos.");
            return "redirect:/profesor/alumnos";
        }

        try
        {
            Alumno alumno = this.alumnoService.contactarAlumno(dto.alumnoId(), dto.asunto(), dto.mensaje());
            redirectAttributes.addFlashAttribute("mensajeExito", "Mensaje enviado con éxito a " + alumno.getNombre() + " (" + alumno.getCorreo() + ").");
        }
        catch (ReglaNegocioException ex)
        {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        catch (Exception ex)
        {
            redirectAttributes.addFlashAttribute("mensajeExito", "Mensaje registrado correctamente (servidor de correo simulado).");
        }

        return "redirect:/profesor/alumnos";
    }

    // =========================================================================
    // 7. MI PERFIL
    // =========================================================================
    @GetMapping("/perfil")
    public String mostrarPerfil(@AuthenticationPrincipal Object principal, Model model)
    {
        Profesor profesor = this.profesorService.obtenerProfesorActual(principal);
        if (profesor == null)
        {
            return "redirect:/login";
        }

        EditarPerfilProfesorDTO dto = this.profesorMapper.toEditarPerfilProfesorDTO(profesor);

        model.addAttribute("perfilDTO", dto);
        model.addAttribute("vehiculo", profesor.getVehiculo());

        return "profesor/perfil";
    }

    @PostMapping("/perfil")
    public String actualizarPerfil(@AuthenticationPrincipal Object principal, @Valid @ModelAttribute("perfilDTO") EditarPerfilProfesorDTO dto, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes)
    {
        Profesor profesor = this.profesorService.obtenerProfesorActual(principal);
        if (profesor == null)
        {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors())
        {
            EditarPerfilProfesorDTO perfilRepoblado = this.profesorMapper.repoblarPerfilDTO(profesor, dto);
            model.addAttribute("profesor", profesor);
            model.addAttribute("perfilDTO", perfilRepoblado);
            model.addAttribute("vehiculo", profesor.getVehiculo());
            return "profesor/perfil";
        }

        try
        {
            this.profesorService.actualizarPerfil(profesor.getId(), dto);
        }
        catch (ReglaNegocioException ex)
        {
            EditarPerfilProfesorDTO perfilRepoblado = this.profesorMapper.repoblarPerfilDTO(profesor, dto);
            model.addAttribute("profesor", profesor);
            model.addAttribute("perfilDTO", perfilRepoblado);
            model.addAttribute("vehiculo", profesor.getVehiculo());
            model.addAttribute("error", ex.getMessage());
            return "profesor/perfil";
        }

        redirectAttributes.addFlashAttribute("mensajeExito", "Tus datos personales han sido actualizados correctamente.");
        return "redirect:/profesor/perfil";
    }

    @PostMapping("/perfil/password")
    public String cambiarPassword(@AuthenticationPrincipal Object principal, @Valid @ModelAttribute("passwordDTO") CambiarPasswordDTO dto, BindingResult bindingResult, RedirectAttributes redirectAttributes)
    {
        Profesor profesor = this.profesorService.obtenerProfesorActual(principal);
        if (profesor == null)
        {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors())
        {
            redirectAttributes.addFlashAttribute("errorPassword", "La nueva contraseña debe tener al menos 6 caracteres.");
            return "redirect:/profesor/perfil";
        }

        try
        {
            this.profesorService.cambiarPassword(profesor.getId(), dto.passwordActual(), dto.nuevaPassword(), dto.confirmPassword());
            redirectAttributes.addFlashAttribute("mensajeExito", "Tu contraseña de acceso ha sido cambiada correctamente.");
        }
        catch (ReglaNegocioException ex)
        {
            redirectAttributes.addFlashAttribute("errorPassword", ex.getMessage());
        }

        return "redirect:/profesor/perfil";
    }
}

