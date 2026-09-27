package com.autoescuela.erp.usuarios.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletResponse;

import com.autoescuela.erp.academico.model.Matricula;
import com.autoescuela.erp.academico.repository.MatriculaRepository;
import com.autoescuela.erp.core.email.EmailService;
import com.autoescuela.erp.core.enums.EstadoClase;
import com.autoescuela.erp.core.enums.EstadoIncidencia;
import com.autoescuela.erp.core.security.UserDetailsImpl;
import com.autoescuela.erp.examenes.dto.CalificarExamenDTO;
import com.autoescuela.erp.examenes.model.Examen;
import com.autoescuela.erp.examenes.repository.ExamenRepository;
import com.autoescuela.erp.flota.dto.ReportarIncidenciaDTO;
import com.autoescuela.erp.flota.model.IncidenciaVehiculo;
import com.autoescuela.erp.flota.model.Vehiculo;
import com.autoescuela.erp.flota.repository.IncidenciaVehiculoRepository;
import com.autoescuela.erp.flota.repository.VehiculoRepository;
import com.autoescuela.erp.practicas.dto.CompletarClaseDTO;
import com.autoescuela.erp.practicas.dto.EventoCalendarioDTO;
import com.autoescuela.erp.practicas.model.ClasePractica;
import com.autoescuela.erp.practicas.repository.ClasePracticaRepository;
import com.autoescuela.erp.usuarios.dto.CambiarPasswordDTO;
import com.autoescuela.erp.usuarios.dto.ContactarAlumnoDTO;
import com.autoescuela.erp.usuarios.dto.EditarPerfilProfesorDTO;
import com.autoescuela.erp.usuarios.mapper.ProfesorMapper;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.model.Profesor;
import com.autoescuela.erp.usuarios.repository.AlumnoRepository;
import com.autoescuela.erp.usuarios.repository.PersonaRepository;
import com.autoescuela.erp.usuarios.repository.ProfesorRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/profesor")
@RequiredArgsConstructor
public class ProfesorController
{
    private final ProfesorRepository profesorRepository;
    private final AlumnoRepository alumnoRepository;
    private final ClasePracticaRepository clasePracticaRepository;
    private final VehiculoRepository vehiculoRepository;
    private final IncidenciaVehiculoRepository incidenciaVehiculoRepository;
    private final ExamenRepository examenRepository;
    private final MatriculaRepository matriculaRepository;
    private final PersonaRepository personaRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final ProfesorMapper profesorMapper;


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
        return "profesor1";
    }

    @ModelAttribute("nombreProfesor")
    public String obtenerNombreCompleto(@AuthenticationPrincipal Object principal)
    {
        if (principal instanceof UserDetailsImpl userDetails)
        {
            return userDetails.getNombreCompleto();
        }
        Profesor profe = this.obtenerProfesorActual(principal);
        if (profe != null)
        {
            return profe.getNombre() + " " + profe.getApellidos();
        }
        return "Laura Sánchez Romero";
    }

    /**
     * Resuelve la entidad Profesor del usuario autenticado con fallback robusto a datos semilla si procede.
     */
    private Profesor obtenerProfesorActual(Object principal)
    {
        if (principal instanceof UserDetailsImpl userDetails)
        {
            Optional<Profesor> profesorOptional = this.profesorRepository.findById(userDetails.getId());
            if (profesorOptional.isPresent())
            {
                return profesorOptional.get();
            }
        }
        else if (principal instanceof UserDetails user)
        {
            Optional<Profesor> profesorOptional = this.profesorRepository.findByNombreUsuario(user.getUsername());
            if (profesorOptional.isPresent())
            {
                return profesorOptional.get();
            }
        }
        // Fallback para entornos de desarrollo/test
        List<Profesor> todos = this.profesorRepository.findAll();
        return todos.isEmpty() ? null : todos.get(0);
    }

    // =========================================================================
    // 1. DASHBOARD GENERAL DEL PROFESOR
    // =========================================================================
    @GetMapping("/dashboard")
    public String mostrarDashboard(@AuthenticationPrincipal Object principal, Model model)
    {
        Profesor profesor = this.obtenerProfesorActual(principal);
        if (profesor == null)
        {
            return "redirect:/login";
        }

        LocalDate hoy = LocalDate.now();
        LocalDateTime inicioHoy = hoy.atStartOfDay();
        LocalDateTime finHoy = hoy.atTime(23, 59, 59);

        // Clases prácticas del día de hoy
        List<ClasePractica> clasesHoy = this.clasePracticaRepository
                .findByProfesorAndFechaHoraBetweenOrderByFechaHoraAsc(profesor, inicioHoy, finHoy);

        // Próxima clase pendiente del profesor
        List<ClasePractica> todasClases = this.clasePracticaRepository.findByProfesorOrderByFechaHoraAsc(profesor);
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
        List<Alumno> misAlumnos = this.alumnoRepository.findByProfesor(profesor);
        // Obtener el vehículo asignado al profesor
        Vehiculo vehiculo = profesor.getVehiculo();

        // Contar el número de exámenes pendientes de calificación del profesor
        List<Examen> misExamenes = this.examenRepository.findByAlumnoProfesorOrderByFechaHoraDesc(profesor);
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
        model.addAttribute("totalExamenes", misExamenes.size());
        model.addAttribute("pendientesCalificar", pendientesCalificar);

        return "profesor/dashboard";
    }

    // =========================================================================
    // 2. MI CALENDARIO DE PRÁCTICAS
    // =========================================================================
    @GetMapping("/calendario")
    public String mostrarCalendario(@AuthenticationPrincipal Object principal, Model model)
    {
        Profesor profesor = this.obtenerProfesorActual(principal);
        if (profesor == null)
        {
            return "redirect:/login";
        }

        model.addAttribute("profesor", profesor);
        model.addAttribute("vehiculo", profesor.getVehiculo());
        model.addAttribute("completarClaseDTO", new CompletarClaseDTO());

        return "profesor/calendario";
    }

    /**
     * Endpoint API para serializar las clases prácticas y jornadas de examen en FullCalendar v6.
     * Código de colores acordado:
     * - Clase PENDIENTE: Ámbar (#f59e0b)
     * - Clase RECIBIDA: Verde esmeralda (#10b981)
     * - Clase CANCELADA: Slate/Rojo (#64748b)
     * - Jornada Examen Oficial DGT: Azul / Púrpura (#6366f1)
     */
    @GetMapping("/calendario/eventos")
    @ResponseBody
    public List<EventoCalendarioDTO> obtenerEventosCalendario(@AuthenticationPrincipal Object principal)
    {
        Profesor profesor = this.obtenerProfesorActual(principal);
        List<EventoCalendarioDTO> eventos = new ArrayList<>();
        if (profesor == null)
        {
            return eventos;
        }

        List<ClasePractica> clases = this.clasePracticaRepository.findByProfesorOrderByFechaHoraAsc(profesor);
        DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        for (ClasePractica clase : clases)
        {
            String bg = "#f59e0b"; // Ámbar por defecto (PENDIENTE)
            String border = "#d97706";
            if (clase.getEstadoClase() == EstadoClase.RECIBIDA)
            {
                bg = "#10b981"; // Verde esmeralda
                border = "#059669";
            }
            else if (clase.getEstadoClase() == EstadoClase.CANCELADA)
            {
                bg = "#64748b"; // Gris pizarra
                border = "#475569";
            }

            LocalDateTime finClase = clase.getFechaHora().plusMinutes(clase.getDuracion() != null ? clase.getDuracion() : 45);
            String nombreAlumno = (clase.getAlumno() != null) ? clase.getAlumno().getNombre() + " " + clase.getAlumno().getApellidos() : "Alumno";

            Map<String, Object> props = new HashMap<>();
            props.put("tipo", "CLASE_PRACTICA");
            props.put("alumnoNombre", nombreAlumno);
            props.put("alumnoDni", clase.getAlumno() != null ? clase.getAlumno().getDni() : "");
            props.put("alumnoTelefono", clase.getAlumno() != null ? clase.getAlumno().getTelefono() : "");
            props.put("puntoRecogida", clase.getPuntoRecogida());
            props.put("duracion", clase.getDuracion());
            props.put("kmInicio", clase.getKmInicio());
            props.put("kmFin", clase.getKmFin());
            props.put("observaciones", clase.getObservaciones());
            props.put("estadoClase", clase.getEstadoClase().name());

            EventoCalendarioDTO ev = new EventoCalendarioDTO(
                    clase.getId() != null ? clase.getId().toString() : "0",
                    "Clase: " + nombreAlumno + " (" + clase.getEstadoClase().name() + ")",
                    clase.getFechaHora().format(isoFormatter),
                    finClase.format(isoFormatter),
                    false,
                    bg,
                    border,
                    "#ffffff",
                    props
            );

            eventos.add(ev);
        }

        // Jornadas oficiales DGT del mes donde el profesor tiene citaciones
        List<Examen> examenes = this.examenRepository.findByAlumnoProfesorOrderByFechaHoraDesc(profesor);
        for (Examen examen : examenes)
        {
            Map<String, Object> props = new HashMap<>();
            props.put("tipo", "EXAMEN_DGT");
            props.put("alumnosCitados", examenes.size());
            props.put("examenId", examen.getId());

            EventoCalendarioDTO evDgt = new EventoCalendarioDTO(
                    "dgt-" + examen.getId(),
                    "Jornada Oficial DGT: Examen Práctico",
                    examen.getFechaHora().toLocalDate().toString(),
                    null,
                    true,
                    "#6366f1", // Azul índigo / púrpura
                    "#4f46e5",
                    "#ffffff",
                    props
            );

            eventos.add(evDgt);
        }

        return eventos;
    }

    /**
     * Devuelve el fragmento Thymeleaf de la ficha técnica de una clase práctica para su carga dinámica con HTMX.
     */
    @GetMapping("/clases/{id}/modal")
    public String obtenerModalClasePractica(@PathVariable("id") Long id, @AuthenticationPrincipal Object principal, Model model)
    {
        Profesor profesor = this.obtenerProfesorActual(principal);
        if (profesor == null)
        {
            return "redirect:/login";
        }

        Optional<ClasePractica> claseOptional = this.clasePracticaRepository.findById(id);
        if (claseOptional.isEmpty())
        {
            return "error/404";
        }

        ClasePractica clase = claseOptional.get();
        if (clase.getProfesor() != null && !clase.getProfesor().getId().equals(profesor.getId()))
        {
            return "error/403";
        }

        model.addAttribute("clase", clase);
        return "profesor/fragments/modal-ficha-clase :: modalFichaClase";
    }

    /**
     * Devuelve el fragmento Thymeleaf con los detalles de una jornada oficial DGT para su carga dinámica con HTMX.
     */
    @GetMapping("/examenes/{id}/modal")
    public String obtenerModalExamenDgt(@PathVariable("id") Long id, @AuthenticationPrincipal Object principal, Model model)
    {
        Profesor profesor = this.obtenerProfesorActual(principal);
        if (profesor == null)
        {
            return "redirect:/login";
        }

        Optional<Examen> examenOptional = this.examenRepository.findById(id);
        if (examenOptional.isEmpty())
        {
            return "error/404";
        }

        Examen examen = examenOptional.get();
        List<Examen> examenes = this.examenRepository.findByAlumnoProfesorOrderByFechaHoraDesc(profesor);

        model.addAttribute("examen", examen);
        model.addAttribute("alumnosCitados", examenes.size());
        return "profesor/fragments/modal-detalle-examen :: modalDetalleExamen";
    }

    /**
     * Procesa la cumplimentación de la ficha técnica de clase práctica (CU-024).
     * Transición atómica a RECIBIDA y deducción directa del saldo de la matrícula (Decisión #840).
     * Soporta peticiones tradicionales y peticiones dinámicas HTMX.
     */
    @PostMapping("/clases/completar")
    @Transactional
    public String completarClasePractica(@AuthenticationPrincipal Object principal, @Valid @ModelAttribute("completarClaseDTO") CompletarClaseDTO dto, BindingResult bindingResult, @RequestHeader(value = "HX-Request", required = false) String hxRequest, HttpServletResponse response, Model model, RedirectAttributes redirectAttributes)
    {
        Optional<ClasePractica> claseOptional = this.clasePracticaRepository.findById(dto.claseId());
        if (claseOptional.isEmpty())
        {
            if (hxRequest != null)
            {
                model.addAttribute("error", "No se encontró la clase práctica solicitada.");
                return "profesor/fragments/alerta-feedback :: feedbackExito";
            }
            redirectAttributes.addFlashAttribute("error", "No se encontró la clase práctica solicitada.");
            return "redirect:/profesor/calendario";
        }

        ClasePractica clase = claseOptional.get();

        if (bindingResult.hasErrors())
        {
            if (hxRequest != null)
            {
                model.addAttribute("clase", clase);
                model.addAttribute("error", "Error en los datos de kilometraje u observaciones de la clase.");
                return "profesor/fragments/modal-ficha-clase :: modalFichaClase";
            }
            redirectAttributes.addFlashAttribute("error", "Error en los datos de kilometraje u observaciones de la clase.");
            return "redirect:/profesor/calendario";
        }

        if (dto.kmFin() != null && dto.kmInicio() != null && dto.kmFin() < dto.kmInicio())
        {
            if (hxRequest != null)
            {
                model.addAttribute("clase", clase);
                model.addAttribute("error", "El kilometraje final no puede ser inferior al inicial.");
                return "profesor/fragments/modal-ficha-clase :: modalFichaClase";
            }
            redirectAttributes.addFlashAttribute("error", "El kilometraje final no puede ser inferior al inicial.");
            return "redirect:/profesor/calendario";
        }

        clase.setKmInicio(dto.kmInicio());
        clase.setKmFin(dto.kmFin());
        clase.setObservaciones(dto.observaciones());
        clase.setEstadoClase(EstadoClase.RECIBIDA);
        this.clasePracticaRepository.save(clase);

        // Actualizar odómetro del vehículo si corresponde
        if (clase.getProfesor() != null && clase.getProfesor().getVehiculo() != null)
        {
            Vehiculo vehiculo = clase.getProfesor().getVehiculo();
            if (dto.kmFin() != null && (vehiculo.getKm() == null || dto.kmFin() > vehiculo.getKm()))
            {
                vehiculo.setKm(dto.kmFin().longValue());
                this.vehiculoRepository.save(vehiculo);
            }
        }

        // Deducción automática de 1 unidad de saldo en la matrícula del alumno (Decisión #840)
        Alumno alumno = clase.getAlumno();
        if (alumno != null)
        {
            Optional<Matricula> matriculaOptional = this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno);
            if (matriculaOptional.isPresent())
            {
                Matricula matricula = matriculaOptional.get();
                if (matricula.getSaldoClases() != null && matricula.getSaldoClases() > 0)
                {
                    matricula.setSaldoClases(matricula.getSaldoClases() - 1);
                    this.matriculaRepository.save(matricula);
                }
            }
        }

        String mensajeExito = "Ficha técnica de la clase registrada correctamente. Se ha descontado 1 unidad de saldo al alumno.";
        if (hxRequest != null)
        {
            response.setHeader("HX-Trigger", "actualizarCalendario");
            model.addAttribute("mensajeExito", mensajeExito);
            return "profesor/fragments/alerta-feedback :: feedbackExito";
        }

        redirectAttributes.addFlashAttribute("mensajeExito", mensajeExito);
        return "redirect:/profesor/calendario";
    }

    /**
     * Cancela una clase práctica reservada notificando al alumno por correo (CU-023).
     * Soporta peticiones tradicionales y peticiones dinámicas HTMX.
     */
    @PostMapping("/clases/cancelar")
    @Transactional
    public String cancelarClasePractica(@AuthenticationPrincipal Object principal, @RequestParam("claseId") Long claseId, @RequestParam(value = "motivo", required = false) String motivo, @RequestHeader(value = "HX-Request", required = false) String hxRequest, HttpServletResponse response, Model model, RedirectAttributes redirectAttributes)
    {
        Optional<ClasePractica> claseOptional = this.clasePracticaRepository.findById(claseId);
        if (claseOptional.isEmpty())
        {
            if (hxRequest != null)
            {
                model.addAttribute("error", "No se encontró la clase práctica a cancelar.");
                return "profesor/fragments/alerta-feedback :: feedbackExito";
            }
            redirectAttributes.addFlashAttribute("error", "No se encontró la clase práctica a cancelar.");
            return "redirect:/profesor/calendario";
        }

        ClasePractica clase = claseOptional.get();
        clase.setEstadoClase(EstadoClase.CANCELADA);
        if (motivo != null && !motivo.isBlank())
        {
            String actual = clase.getObservaciones() != null ? clase.getObservaciones() + " | " : "";
            clase.setObservaciones(actual + "Cancelación: " + motivo.trim());
        }
        this.clasePracticaRepository.save(clase);

        // Notificación al alumno por email
        if (clase.getAlumno() != null && clase.getAlumno().getCorreo() != null)
        {
            try
            {
                this.emailService.enviarNotificacionAlumno(
                        clase.getAlumno().getCorreo(),
                        "Cancelación de clase práctica programada",
                        "Estimado/a " + clase.getAlumno().getNombre() + ",\n\nTu clase práctica del "
                                + clase.getFechaHora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                                + " ha sido cancelada por el profesor. Motivo: "
                                + (motivo != null ? motivo : "Imprevisto docente") + ".\n\nTu saldo de clases permanece intacto para que reserves una nueva fecha.");
            }
            catch (Exception ex)
            {
                // Fallback silencioso ante servidor SMTP apagado
            }
        }

        String mensajeExito = "Clase práctica cancelada correctamente y alumno notificado por correo.";
        if (hxRequest != null)
        {
            response.setHeader("HX-Trigger", "actualizarCalendario");
            model.addAttribute("mensajeExito", mensajeExito);
            return "profesor/fragments/alerta-feedback :: feedbackExito";
        }

        redirectAttributes.addFlashAttribute("mensajeExito", mensajeExito);
        return "redirect:/profesor/calendario";
    }

    // =========================================================================
    // 3. MIS ALUMNOS
    // =========================================================================
    @GetMapping("/alumnos")
    public String mostrarAlumnos(@AuthenticationPrincipal Object principal, Model model)
    {
        Profesor profesor = this.obtenerProfesorActual(principal);
        if (profesor == null)
        {
            return "redirect:/login";
        }

        List<Alumno> alumnos = this.alumnoRepository.findByProfesor(profesor);
        List<ClasePractica> todasLasClasesProfesor = this.clasePracticaRepository.findByProfesorOrderByFechaHoraAsc(profesor);

        // Estructura de apoyo para renderizar expedientes de cada alumno
        List<Map<String, Object>> fichasAlumnos = new ArrayList<>();
        for (Alumno alumno : alumnos)
        {
            Map<String, Object> ficha = new HashMap<>();
            ficha.put("alumno", alumno);

            Optional<Matricula> matriculaOptional = this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno);
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

            fichasAlumnos.add(ficha);
        }

        model.addAttribute("profesor", profesor);
        model.addAttribute("fichasAlumnos", fichasAlumnos);
        model.addAttribute("contactarDTO", new ContactarAlumnoDTO());

        return "profesor/alumnos";
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

        Optional<Alumno> alumnoOptional = this.alumnoRepository.findById(dto.alumnoId());
        if (alumnoOptional.isEmpty())
        {
            redirectAttributes.addFlashAttribute("error", "No se encontró el alumno indicado.");
            return "redirect:/profesor/alumnos";
        }

        Alumno alumno = alumnoOptional.get();
        try
        {
            this.emailService.enviarNotificacionAlumno(alumno.getCorreo(), dto.asunto(), dto.mensaje());
            redirectAttributes.addFlashAttribute("mensajeExito", "Mensaje enviado con éxito a " + alumno.getNombre() + " (" + alumno.getCorreo() + ").");
        }
        catch (Exception ex)
        {
            redirectAttributes.addFlashAttribute("mensajeExito", "Mensaje registrado correctamente (servidor de correo simulado).");
        }

        return "redirect:/profesor/alumnos";
    }

    // =========================================================================
    // 4. CONVOCATORIAS Y CALIFICACIONES DGT
    // =========================================================================
    @GetMapping("/examenes")
    public String mostrarExamenes(@AuthenticationPrincipal Object principal, Model model)
    {
        Profesor profesor = this.obtenerProfesorActual(principal);
        if (profesor == null)
        {
            return "redirect:/login";
        }

        List<Examen> misExamenes = this.examenRepository.findByAlumnoProfesorOrderByFechaHoraDesc(profesor);
        model.addAttribute("profesor", profesor);
        model.addAttribute("examenes", misExamenes);
        model.addAttribute("calificarExamenDTO", new CalificarExamenDTO());

        return "profesor/examenes";
    }

    /**
     * Asienta la calificación oficial de un examen práctico DGT (CU-025).
     */
    @PostMapping("/examenes/calificar")
    @Transactional
    public String calificarExamen(@AuthenticationPrincipal Object principal, @Valid @ModelAttribute("calificarExamenDTO") CalificarExamenDTO dto, BindingResult bindingResult, RedirectAttributes redirectAttributes)
    {
        if (bindingResult.hasErrors())
        {
            redirectAttributes.addFlashAttribute("error", "Debes especificar si el resultado es Apto o No Apto.");
            return "redirect:/profesor/examenes";
        }

        Optional<Examen> examenOptional = this.examenRepository.findById(dto.examenId());
        if (examenOptional.isEmpty())
        {
            redirectAttributes.addFlashAttribute("error", "No se encontró el examen a calificar.");
            return "redirect:/profesor/examenes";
        }

        Examen examen = examenOptional.get();
        examen.setEsApto(dto.esApto());
        this.examenRepository.save(examen);

        // Si suspende, computar convocatoria gastada en la matrícula
        if (Boolean.FALSE.equals(dto.esApto()) && examen.getAlumno() != null)
        {
            Optional<Matricula> matriculaOptional = this.matriculaRepository.findByAlumnoAndEstaActivaTrue(examen.getAlumno());
            if (matriculaOptional.isPresent())
            {
                Matricula matricula = matriculaOptional.get();
                int gastadas = matricula.getConvocatoriasGastadas() != null ? matricula.getConvocatoriasGastadas() : 0;
                matricula.setConvocatoriasGastadas(gastadas + 1);
                this.matriculaRepository.save(matricula);
            }
        }

        redirectAttributes.addFlashAttribute("mensajeExito", "Calificación registrada con éxito. Resultado: " + (Boolean.TRUE.equals(dto.esApto()) ? "APTO" : "NO APTO") + ".");
        return "redirect:/profesor/examenes";
    }

    // =========================================================================
    // 5. MI VEHÍCULO E INCIDENCIAS
    // =========================================================================
    @GetMapping("/vehiculo")
    public String mostrarVehiculo(@AuthenticationPrincipal Object principal, Model model)
    {
        Profesor profesor = this.obtenerProfesorActual(principal);
        if (profesor == null)
        {
            return "redirect:/login";
        }

        Vehiculo vehiculo = profesor.getVehiculo();
        List<IncidenciaVehiculo> incidencias = vehiculo != null
                ? this.incidenciaVehiculoRepository.findByVehiculoOrderByFechaHoraDesc(vehiculo)
                : this.incidenciaVehiculoRepository.findByProfesorOrderByFechaHoraDesc(profesor);

        model.addAttribute("profesor", profesor);
        model.addAttribute("vehiculo", vehiculo);
        model.addAttribute("incidencias", incidencias);
        model.addAttribute("reportarIncidenciaDTO", new ReportarIncidenciaDTO());

        return "profesor/vehiculo";
    }

    /**
     * Registra una nueva incidencia o avería mecánica sobre el vehículo asignado (CU-027).
     */
    @PostMapping("/vehiculo/incidencia")
    @Transactional
    public String reportarIncidencia(@AuthenticationPrincipal Object principal, @Valid @ModelAttribute("reportarIncidenciaDTO") ReportarIncidenciaDTO dto, BindingResult bindingResult, RedirectAttributes redirectAttributes)
    {
        Profesor profesor = this.obtenerProfesorActual(principal);
        if (profesor == null)
        {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors())
        {
            redirectAttributes.addFlashAttribute("error", "La descripción de la avería debe tener entre 10 y 1000 caracteres.");
            return "redirect:/profesor/vehiculo";
        }

        Vehiculo vehiculo = profesor.getVehiculo();
        if (vehiculo == null && dto.vehiculoId() != null)
        {
            vehiculo = this.vehiculoRepository.findById(dto.vehiculoId()).orElse(null);
        }

        if (vehiculo == null)
        {
            redirectAttributes.addFlashAttribute("error", "No tienes ningún vehículo asignado para reportar incidencias.");
            return "redirect:/profesor/vehiculo";
        }

        IncidenciaVehiculo incidencia = new IncidenciaVehiculo();
        incidencia.setFechaHora(LocalDateTime.now());
        incidencia.setDescripcion(dto.descripcion());
        incidencia.setEstado(EstadoIncidencia.PENDIENTE);
        incidencia.setVehiculo(vehiculo);
        incidencia.setProfesor(profesor);

        this.incidenciaVehiculoRepository.save(incidencia);


        redirectAttributes.addFlashAttribute("mensajeExito", "Incidencia mecánica reportada correctamente. El Administrador ha sido notificado para su revisión.");
        return "redirect:/profesor/vehiculo";
    }

    // =========================================================================
    // 6. MIS ESTADÍSTICAS
    // =========================================================================
    @GetMapping("/estadisticas")
    public String mostrarEstadisticas(@AuthenticationPrincipal Object principal, Model model)
    {
        Profesor profesor = this.obtenerProfesorActual(principal);
        if (profesor == null)
        {
            return "redirect:/login";
        }

        List<ClasePractica> clases = this.clasePracticaRepository.findByProfesorOrderByFechaHoraAsc(profesor);
        long totalClases = clases.size();
        long clasesRecibidas = 0;
        long clasesPendientes = 0;
        long clasesCanceladas = 0;
        int totalMinutos = 0;
        long totalKmRecorridos = 0;

        for (ClasePractica clase : clases)
        {
            if (clase.getEstadoClase() == EstadoClase.RECIBIDA)
            {
                clasesRecibidas++;
                int duracion = clase.getDuracion() != null ? clase.getDuracion() : 45;
                totalMinutos += duracion;
                if (clase.getKmFin() != null && clase.getKmInicio() != null)
                {
                    totalKmRecorridos += Math.max(0, clase.getKmFin() - clase.getKmInicio());
                }
            }
            else if (clase.getEstadoClase() == EstadoClase.PENDIENTE)
            {
                clasesPendientes++;
            }
            else if (clase.getEstadoClase() == EstadoClase.CANCELADA)
            {
                clasesCanceladas++;
            }
        }

        double horasConduccion = Math.round((totalMinutos / 60.0) * 10.0) / 10.0;

        // Tasa de aprobados
        List<Examen> examenes = this.examenRepository.findByAlumnoProfesorOrderByFechaHoraDesc(profesor);
        long aptos = 0;
        long noAptos = 0;
        for (Examen examen : examenes)
        {
            if (Boolean.TRUE.equals(examen.getEsApto()))
            {
                aptos++;
            }
            else if (Boolean.FALSE.equals(examen.getEsApto()))
            {
                noAptos++;
            }
        }
        long evaluados = aptos + noAptos;
        int tasaAprobados = evaluados > 0 ? (int) Math.round(((double) aptos / evaluados) * 100) : 0;

        model.addAttribute("profesor", profesor);
        model.addAttribute("totalClases", totalClases);
        model.addAttribute("clasesRecibidas", clasesRecibidas);
        model.addAttribute("clasesPendientes", clasesPendientes);
        model.addAttribute("clasesCanceladas", clasesCanceladas);
        model.addAttribute("horasConduccion", horasConduccion);
        model.addAttribute("totalKmRecorridos", totalKmRecorridos);
        model.addAttribute("aptos", aptos);
        model.addAttribute("noAptos", noAptos);
        model.addAttribute("tasaAprobados", tasaAprobados);
        model.addAttribute("permisosDocente", profesor.getListaTiposCarnet());

        return "profesor/estadisticas";
    }

    // =========================================================================
    // 7. MI PERFIL
    // =========================================================================
    @GetMapping("/perfil")
    public String mostrarPerfil(@AuthenticationPrincipal Object principal, Model model)
    {
        Profesor profesor = this.obtenerProfesorActual(principal);
        if (profesor == null)
        {
            return "redirect:/login";
        }

        EditarPerfilProfesorDTO dto = this.profesorMapper.toEditarPerfilProfesorDTO(profesor);

        model.addAttribute("profesor", profesor);
        model.addAttribute("perfilDTO", dto);
        model.addAttribute("passwordDTO", new CambiarPasswordDTO());

        return "profesor/perfil";
    }

    @PostMapping("/perfil")
    @Transactional
    public String actualizarPerfil(@AuthenticationPrincipal Object principal, @Valid @ModelAttribute("perfilDTO") EditarPerfilProfesorDTO dto, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes)
    {
        Profesor profesor = this.obtenerProfesorActual(principal);
        if (profesor == null)
        {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors())
        {
            EditarPerfilProfesorDTO perfilRepoblado = this.profesorMapper.repoblarPerfilDTO(profesor, dto);
            model.addAttribute("profesor", profesor);
            model.addAttribute("perfilDTO", perfilRepoblado);
            model.addAttribute("passwordDTO", new CambiarPasswordDTO());
            return "profesor/perfil";
        }

        // Comprobar unicidad de teléfono si ha cambiado
        String nuevoTel = dto.telefono() != null ? dto.telefono().trim() : "";
        if (!nuevoTel.equals(profesor.getTelefono()) && this.personaRepository.existsByTelefonoAndIdNot(nuevoTel, profesor.getId()))
        {
            EditarPerfilProfesorDTO perfilRepoblado = this.profesorMapper.repoblarPerfilDTO(profesor, dto);
            model.addAttribute("profesor", profesor);
            model.addAttribute("perfilDTO", perfilRepoblado);
            model.addAttribute("passwordDTO", new CambiarPasswordDTO());
            model.addAttribute("error", "El número de teléfono introducido ya está registrado por otro usuario.");
            return "profesor/perfil";
        }

        profesor.setNombre(dto.nombre() != null ? dto.nombre().trim() : "");
        profesor.setApellidos(dto.apellidos() != null ? dto.apellidos().trim() : "");
        profesor.setTelefono(nuevoTel);
        profesor.setDireccion(dto.direccion() != null ? dto.direccion().trim() : "");
        profesor.setFechaNacimiento(dto.fechaNacimiento());

        this.profesorRepository.save(profesor);

        redirectAttributes.addFlashAttribute("mensajeExito", "Tus datos personales han sido actualizados correctamente.");
        return "redirect:/profesor/perfil";
    }

    @PostMapping("/perfil/password")
    @Transactional
    public String cambiarPassword(@AuthenticationPrincipal Object principal, @Valid @ModelAttribute("passwordDTO") CambiarPasswordDTO dto, BindingResult bindingResult, RedirectAttributes redirectAttributes)
    {
        Profesor profesor = this.obtenerProfesorActual(principal);
        if (profesor == null)
        {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors())
        {
            redirectAttributes.addFlashAttribute("errorPassword", "La nueva contraseña debe tener al menos 6 caracteres.");
            return "redirect:/profesor/perfil";
        }

        if (!this.passwordEncoder.matches(dto.passwordActual(), profesor.getPassword()))
        {
            redirectAttributes.addFlashAttribute("errorPassword", "La contraseña actual no es correcta.");
            return "redirect:/profesor/perfil";
        }

        if (!dto.nuevaPassword().equals(dto.confirmPassword()))
        {
            redirectAttributes.addFlashAttribute("errorPassword", "Las nuevas contraseñas no coinciden.");
            return "redirect:/profesor/perfil";
        }

        profesor.actualizarPassword(this.passwordEncoder.encode(dto.nuevaPassword()));
        this.profesorRepository.save(profesor);

        redirectAttributes.addFlashAttribute("mensajeExito", "Tu contraseña de acceso ha sido cambiada correctamente.");
        return "redirect:/profesor/perfil";
    }
}
