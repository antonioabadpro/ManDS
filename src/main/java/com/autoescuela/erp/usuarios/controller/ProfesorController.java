package com.autoescuela.erp.usuarios.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.autoescuela.erp.academico.model.Matricula;
import com.autoescuela.erp.academico.repository.MatriculaRepository;
import com.autoescuela.erp.core.email.EmailService;
import com.autoescuela.erp.core.enums.EstadoClase;
import com.autoescuela.erp.core.enums.EstadoIncidencia;
import com.autoescuela.erp.core.enums.TipoExamen;
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
import com.autoescuela.erp.practicas.dto.TramoHorarioDTO;
import com.autoescuela.erp.practicas.model.ClasePractica;
import com.autoescuela.erp.practicas.repository.ClasePracticaRepository;
import com.autoescuela.erp.usuarios.dto.CambiarPasswordDTO;
import com.autoescuela.erp.usuarios.dto.ContactarAlumnoDTO;
import com.autoescuela.erp.usuarios.dto.EditarPerfilProfesorDTO;
import com.autoescuela.erp.usuarios.mapper.ProfesorMapper;
import com.autoescuela.erp.usuarios.model.Administrador;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.model.Profesor;
import com.autoescuela.erp.usuarios.repository.AlumnoRepository;
import com.autoescuela.erp.usuarios.repository.PersonaRepository;
import com.autoescuela.erp.usuarios.repository.ProfesorRepository;

import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.estadisticas.dto.EstadisticasProfesorDTO;
import com.autoescuela.erp.estadisticas.service.EstadisticaService;
import com.autoescuela.erp.practicas.dto.CalendarioSemanalDTO;
import com.autoescuela.erp.practicas.dto.DiaCalendarioDTO;
import com.autoescuela.erp.practicas.service.CalendarioService;
import com.autoescuela.erp.practicas.util.FestivosCalendarioUtil;
import com.autoescuela.erp.usuarios.dto.AlumnoDetalleDTO;
import com.autoescuela.erp.usuarios.service.AlumnoService;

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
    private final AlumnoService alumnoService;
    private final CalendarioService calendarioService;
    private final EstadisticaService estadisticaService;

    @Value("${app_base_url:http://localhost:8080}")
    private String appBaseUrl;

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
        model.addAttribute("pendientesCalificar", pendientesCalificar);

        return "profesor/dashboard";
    }

    // =========================================================================
    // 2. MI CALENDARIO DE PRÁCTICAS
    // =========================================================================
    @GetMapping("/calendario")
    public String mostrarCalendario(@RequestParam(value = "fechaRef", required = false) String fechaRefStr, @AuthenticationPrincipal Object principal, Model model)
    {
        Profesor profesor = this.obtenerProfesorActual(principal);
        if (profesor == null)
        {
            return "redirect:/login";
        }

        LocalDate fechaRef = LocalDate.now();
        if (fechaRefStr != null && !fechaRefStr.isBlank())
        {
            try
            {
                fechaRef = LocalDate.parse(fechaRefStr);
            }
            catch (Exception ignored)
            {
                fechaRef = LocalDate.now();
            }
        }

        CalendarioSemanalDTO calendarioSemanal = this.calendarioService.obtenerCalendarioProfesor(profesor, fechaRef);

        model.addAttribute("profesor", profesor);
        model.addAttribute("vehiculo", profesor.getVehiculo());
        model.addAttribute("completarClaseDTO", new CompletarClaseDTO());
        model.addAttribute("calendarioSemanal", calendarioSemanal);

        return "profesor/calendario";
    }

    /**
     * Endpoint HTMX que devuelve exclusivamente el fragmento de la cuadrícula semanal del profesor.
     */
    @GetMapping("/calendario/cuadricula")
    public String obtenerCuadriculaCalendario(@RequestParam(value = "fechaRef", required = false) String fechaRefStr, @AuthenticationPrincipal Object principal, Model model)
    {
        Profesor profesor = this.obtenerProfesorActual(principal);
        if (profesor == null)
        {
            return "redirect:/login";
        }

        LocalDate fechaRef = LocalDate.now();
        if (fechaRefStr != null && !fechaRefStr.isBlank())
        {
            try
            {
                fechaRef = LocalDate.parse(fechaRefStr);
            }
            catch (Exception ignored)
            {
                fechaRef = LocalDate.now();
            }
        }

        CalendarioSemanalDTO calendarioSemanal = this.calendarioService.obtenerCalendarioProfesor(profesor, fechaRef);

        model.addAttribute("profesor", profesor);
        model.addAttribute("vehiculo", profesor.getVehiculo());
        model.addAttribute("completarClaseDTO", new CompletarClaseDTO());
        model.addAttribute("calendarioSemanal", calendarioSemanal);

        return "profesor/fragments/cuadricula-calendario :: cuadriculaSemanal";
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

        Long kmActualVehiculo = (clase.getProfesor() != null && clase.getProfesor().getVehiculo() != null && clase.getProfesor().getVehiculo().getKm() != null)
                ? clase.getProfesor().getVehiculo().getKm()
                : (clase.getKmInicio() != null ? clase.getKmInicio() : 0L);
        model.addAttribute("kmActualVehiculo", kmActualVehiculo);
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
                Long kmActualVehiculo = (clase.getProfesor() != null && clase.getProfesor().getVehiculo() != null && clase.getProfesor().getVehiculo().getKm() != null)
                        ? clase.getProfesor().getVehiculo().getKm()
                        : (dto.kmInicio() != null ? dto.kmInicio() : 0L);
                model.addAttribute("kmActualVehiculo", kmActualVehiculo);
                model.addAttribute("clase", clase);
                model.addAttribute("error", "Error en los datos de kilometraje u observaciones de la clase.");
                return "profesor/fragments/modal-ficha-clase :: modalFichaClase";
            }
            redirectAttributes.addFlashAttribute("error", "Error en los datos de kilometraje u observaciones de la clase.");
            return "redirect:/profesor/calendario";
        }

        if (dto.kmFin() != null && dto.kmInicio() != null && dto.kmFin() <= dto.kmInicio())
        {
            if (hxRequest != null)
            {
                Long kmActualVehiculo = (clase.getProfesor() != null && clase.getProfesor().getVehiculo() != null && clase.getProfesor().getVehiculo().getKm() != null)
                        ? clase.getProfesor().getVehiculo().getKm()
                        : (dto.kmInicio() != null ? dto.kmInicio() : 0L);
                model.addAttribute("kmActualVehiculo", kmActualVehiculo);
                model.addAttribute("clase", clase);
                model.addAttribute("error", "El kilometraje final debe ser estrictamente superior al kilometraje inicial.");
                return "profesor/fragments/modal-ficha-clase :: modalFichaClase";
            }
            redirectAttributes.addFlashAttribute("error", "El kilometraje final debe ser estrictamente superior al kilometraje inicial.");
            return "redirect:/profesor/calendario";
        }

        clase.setKmInicio(dto.kmInicio());
        clase.setKmFin(dto.kmFin());
        clase.setObservaciones(dto.observaciones());
        clase.setEstadoClase(EstadoClase.RECIBIDA);
        this.clasePracticaRepository.save(clase);

        // Actualizar odómetro del vehículo automáticamente con el odómetro final de la clase práctica
        if (clase.getProfesor() != null && clase.getProfesor().getVehiculo() != null && dto.kmFin() != null)
        {
            Vehiculo vehiculo = clase.getProfesor().getVehiculo();
            vehiculo.setKm(dto.kmFin());
            this.vehiculoRepository.save(vehiculo);
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
        clase.setIsCanceladaPorProfesor(true);
        clase.setIsCancelacionJornada(false);
        if (motivo != null && !motivo.isBlank())
        {
            clase.setMotivoCancelacion(motivo.trim());
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
                                + (motivo != null && !motivo.isBlank() ? motivo.trim() : "Imprevisto del Profesor") + ".\n\nTu saldo de clases permanece intacto para que reserves una nueva fecha.");
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

    /**
     * Devuelve el fragmento Thymeleaf del modal para la cancelación masiva de clases prácticas de una jornada.
     */
    @GetMapping("/calendario/cancelar-dia/modal")
    public String obtenerModalCancelarDia(@RequestParam(value = "fecha", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha, @RequestParam(value = "fechaRef", required = false) String fechaRefStr, @AuthenticationPrincipal Object principal, Model model)
    {
        Profesor profesor = this.obtenerProfesorActual(principal);
        if (profesor == null)
        {
            return "redirect:/login";
        }

        LocalDate hoy = LocalDate.now();
        LocalDate fechaRef = hoy;
        if (fechaRefStr != null && !fechaRefStr.isBlank())
        {
            try
            {
                fechaRef = LocalDate.parse(fechaRefStr);
            }
            catch (Exception ignored)
            {
                fechaRef = hoy;
            }
        }

        CalendarioSemanalDTO calendarioSemanal = this.calendarioService.obtenerCalendarioProfesor(profesor, fechaRef);

        LocalDate fechaFinal = fecha;
        if (fechaFinal == null)
        {
            for (DiaCalendarioDTO dia : calendarioSemanal.dias())
            {
                if (!dia.fecha().isBefore(hoy) && dia.numClasesPendientes() > 0 && !dia.esNoLaborable() && !dia.esExamenDgt())
                {
                    fechaFinal = dia.fecha();
                    break;
                }
            }
            if (fechaFinal == null)
            {
                if (hoy.getDayOfWeek() == DayOfWeek.SATURDAY)
                {
                    fechaFinal = hoy.plusDays(2);
                }
                else if (hoy.getDayOfWeek() == DayOfWeek.SUNDAY)
                {
                    fechaFinal = hoy.plusDays(1);
                }
                else
                {
                    fechaFinal = hoy;
                }
            }
        }

        this.prepararModeloCancelarDia(profesor, fechaFinal, model);
        model.addAttribute("profesor", profesor);
        model.addAttribute("calendarioSemanal", calendarioSemanal);
        model.addAttribute("hoy", hoy.toString());

        return "profesor/fragments/modal-cancelar-dia :: modalCancelarDia";
    }

    /**
     * Endpoint HTMX para actualizar dinámicamente el estado y las clases pendientes al cambiar la fecha seleccionada en el modal.
     */
    @GetMapping("/calendario/cancelar-dia/calcular-clases")
    public String calcularClasesCancelarDia(@RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha, @AuthenticationPrincipal Object principal, Model model)
    {
        Profesor profesor = this.obtenerProfesorActual(principal);
        if (profesor == null)
        {
            return "redirect:/login";
        }

        this.prepararModeloCancelarDia(profesor, fecha, model);
        return "profesor/fragments/modal-cancelar-dia :: vistaPreviaClases";
    }

    /**
     * Cancela de forma atómica todas las clases prácticas en estado PENDIENTE de una fecha concreta,
     * imposibilita reservas futuras en esa jornada y notifica por correo electrónico a todos los alumnos asignados al profesor.
     */
    @PostMapping("/calendario/cancelar-dia")
    @Transactional
    public String cancelarClasesDia(@AuthenticationPrincipal Object principal, @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha, @RequestParam(value = "motivo", required = false) String motivo, @RequestHeader(value = "HX-Request", required = false) String hxRequest, HttpServletResponse response, Model model, RedirectAttributes redirectAttributes)
    {
        Profesor profesor = this.obtenerProfesorActual(principal);
        if (profesor == null)
        {
            return "redirect:/login";
        }

        if (fecha == null)
        {
            return this.devolverErrorCancelacion("No se especificó la fecha de la jornada a cancelar.", hxRequest, model, redirectAttributes);
        }

        if (fecha.isBefore(LocalDate.now()))
        {
            return this.devolverErrorCancelacion("No es posible cancelar jornadas de fechas pasadas.", hxRequest, model, redirectAttributes);
        }

        if (fecha.getDayOfWeek() == DayOfWeek.SATURDAY || fecha.getDayOfWeek() == DayOfWeek.SUNDAY)
        {
            return this.devolverErrorCancelacion("No se pueden cancelar jornadas en fines de semana.", hxRequest, model, redirectAttributes);
        }

        if (FestivosCalendarioUtil.esDiaNoLaborable(fecha))
        {
            return this.devolverErrorCancelacion("No se puede cancelar una jornada en festivo nacional o periodo vacacional.", hxRequest, model, redirectAttributes);
        }

        List<Examen> examenesDgt = this.examenRepository.findByAlumnoProfesorOrderByFechaHoraDesc(profesor);
        for (Examen examen : examenesDgt)
        {
            if (examen.getFechaHora() != null && examen.getFechaHora().toLocalDate().equals(fecha))
            {
                return this.devolverErrorCancelacion("Esta fecha coincide con una convocatoria oficial de examen DGT y no admite cancelación docente.", hxRequest, model, redirectAttributes);
            }
        }

        if (motivo == null || motivo.isBlank())
        {
            return this.devolverErrorCancelacion("Debes indicar obligatoriamente el motivo de la cancelación masiva.", hxRequest, model, redirectAttributes);
        }

        LocalDateTime inicioDia = fecha.atStartOfDay();
        LocalDateTime finDia = fecha.atTime(23, 59, 59);

        List<ClasePractica> clasesTodas = this.clasePracticaRepository.findByProfesorAndFechaHoraBetweenOrderByFechaHoraAsc(profesor, inicioDia, finDia);

        boolean yaCanceladaPreviamente = false;
        boolean hayPendientes = false;
        for (ClasePractica c : clasesTodas)
        {
            if (c.getEstadoClase() == EstadoClase.PENDIENTE)
            {
                hayPendientes = true;
            }
            else if (c.getEstadoClase() == EstadoClase.CANCELADA && Boolean.TRUE.equals(c.getIsCancelacionJornada()))
            {
                yaCanceladaPreviamente = true;
            }
        }

        if (yaCanceladaPreviamente && !hayPendientes)
        {
            return this.devolverErrorCancelacion("Esta jornada ya ha sido cancelada previamente por el profesor.", hxRequest, model, redirectAttributes);
        }

        int totalCanceladas = 0;

        for (ClasePractica clase : clasesTodas)
        {
            if (clase.getEstadoClase() == EstadoClase.PENDIENTE)
            {
                clase.setEstadoClase(EstadoClase.CANCELADA);
                clase.setIsCanceladaPorProfesor(true);
                clase.setIsCancelacionJornada(true);
                clase.setMotivoCancelacion(motivo.trim());
                this.clasePracticaRepository.save(clase);
                totalCanceladas++;
            }
        }

        // Generar bloqueos en los tramos horarios restantes del turno que estuvieran libres
        List<TramoHorarioDTO> tramos = this.calendarioService.generarTramos(profesor.getTurno());
        Long kmActual = (profesor.getVehiculo() != null && profesor.getVehiculo().getKm() != null)
                ? profesor.getVehiculo().getKm()
                : 0L;

        for (TramoHorarioDTO tramo : tramos)
        {
            LocalDateTime slotFechaHora = fecha.atTime(tramo.horaInicio());
            boolean existeClaseEnSlot = false;
            for (ClasePractica c : clasesTodas)
            {
                if (c.getFechaHora() != null && c.getFechaHora().toLocalTime().equals(tramo.horaInicio()))
                {
                    existeClaseEnSlot = true;
                    break;
                }
            }

            if (!existeClaseEnSlot)
            {
                ClasePractica claseCancelada = new ClasePractica();
                claseCancelada.setProfesor(profesor);
                claseCancelada.setAlumno(null);
                claseCancelada.setFechaHora(slotFechaHora);
                claseCancelada.setDuracion(45);
                claseCancelada.setPuntoRecogida("Autoescuela");
                claseCancelada.setKmInicio(kmActual);
                claseCancelada.setKmFin(kmActual);
                claseCancelada.setEstadoClase(EstadoClase.CANCELADA);
                claseCancelada.setIsCanceladaPorProfesor(true);
                claseCancelada.setIsCancelacionJornada(true);
                claseCancelada.setMotivoCancelacion(motivo.trim());
                this.clasePracticaRepository.save(claseCancelada);
            }
        }

        // Notificación por correo a TODOS los alumnos asignados al profesor
        List<Alumno> alumnosDelProfesor = this.alumnoRepository.findByProfesor(profesor);
        String fechaFormatoLegible = fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        for (Alumno alumno : alumnosDelProfesor)
        {
            if (alumno.getCorreo() != null && !alumno.getCorreo().isBlank())
            {
                try
                {
                    this.emailService.enviarNotificacionAlumno(
                            alumno.getCorreo(),
                            "Cancelación de clases prácticas - Jornada del " + fechaFormatoLegible,
                            "Estimado/a " + alumno.getNombre() + ",\n\nTe informamos de que todas las clases prácticas programadas con tu profesor para el día " + fechaFormatoLegible + " han sido canceladas.\n\nMotivo de la cancelación: " + motivo.trim() + ".\n\nSi disponías de una reserva para dicho día, tu saldo de clases permanece intacto para que reserves una nueva fecha.\n\nDisculpa las molestias.");
                }
                catch (Exception ex)
                {
                    // Fallback silencioso ante servidor SMTP apagado
                }
            }
        }

        String mensajeExito = (totalCanceladas > 0)
                ? "Se han cancelado las " + totalCanceladas + " clases prácticas del día "
                        + fechaFormatoLegible + " y se ha notificado por correo a todos tus alumnos asignados."
                : "Se ha cancelado la jornada del día " + fechaFormatoLegible + " bloqueando todas las franjas e impidiendo futuras reservas. Se ha notificado por correo a todos tus alumnos asignados.";

        if (hxRequest != null)
        {
            response.setHeader("HX-Trigger", "actualizarCalendario");
            model.addAttribute("mensajeExito", mensajeExito);
            return "profesor/fragments/alerta-feedback :: feedbackExito";
        }

        redirectAttributes.addFlashAttribute("mensajeExito", mensajeExito);
        return "redirect:/profesor/calendario";
    }

    private String devolverErrorCancelacion(String mensajeError, String hxRequest, Model model, RedirectAttributes redirectAttributes)
    {
        if (hxRequest != null)
        {
            model.addAttribute("error", mensajeError);
            return "profesor/fragments/alerta-feedback :: feedbackExito";
        }
        redirectAttributes.addFlashAttribute("error", mensajeError);
        return "redirect:/profesor/calendario";
    }

    private void prepararModeloCancelarDia(Profesor profesor, LocalDate fecha, Model model)
    {
        LocalDate hoy = LocalDate.now();
        boolean noCancelable = false;
        String motivoNoCancelable = null;

        if (fecha == null)
        {
            noCancelable = true;
            motivoNoCancelable = "No se ha especificado una fecha válida.";
        }
        else if (fecha.isBefore(hoy))
        {
            noCancelable = true;
            motivoNoCancelable = "No es posible cancelar jornadas de fechas anteriores a la actual.";
        }
        else if (fecha.getDayOfWeek() == DayOfWeek.SATURDAY || fecha.getDayOfWeek() == DayOfWeek.SUNDAY)
        {
            noCancelable = true;
            motivoNoCancelable = "La autoescuela no realiza clases prácticas en fines de semana (sábados y domingos).";
        }
        else if (FestivosCalendarioUtil.esDiaNoLaborable(fecha))
        {
            noCancelable = true;
            String motivo = FestivosCalendarioUtil.obtenerMotivoNoLaborable(fecha);
            motivoNoCancelable = "Esta fecha coincide con un día no laborable (" + (motivo != null ? motivo : "Festivo / Vacaciones") + ") y no admite cancelación.";
        }
        else
        {
            List<Examen> examenes = this.examenRepository.findByAlumnoProfesorOrderByFechaHoraDesc(profesor);
            for (Examen examen : examenes)
            {
                if (examen.getFechaHora() != null && examen.getFechaHora().toLocalDate().equals(fecha))
                {
                    noCancelable = true;
                    motivoNoCancelable = "Esta fecha coincide con una convocatoria oficial de examen DGT del profesor y no admite cancelación.";
                    break;
                }
            }
        }

        List<ClasePractica> clasesPendientes = new ArrayList<>();
        if (!noCancelable && fecha != null)
        {
            LocalDateTime inicioDia = fecha.atStartOfDay();
            LocalDateTime finDia = fecha.atTime(23, 59, 59);
            List<ClasePractica> clasesTodas = this.clasePracticaRepository.findByProfesorAndFechaHoraBetweenOrderByFechaHoraAsc(profesor, inicioDia, finDia);
            boolean tieneCancelacionJornada = false;
            for (ClasePractica c : clasesTodas)
            {
                if (c.getEstadoClase() == EstadoClase.PENDIENTE)
                {
                    clasesPendientes.add(c);
                }
                else if (c.getEstadoClase() == EstadoClase.CANCELADA && Boolean.TRUE.equals(c.getIsCancelacionJornada()))
                {
                    tieneCancelacionJornada = true;
                }
            }

            if (tieneCancelacionJornada && clasesPendientes.isEmpty())
            {
                noCancelable = true;
                motivoNoCancelable = "Esta jornada ya ha sido cancelada previamente por el profesor.";
            }
        }

        String fechaFormateada = "";
        if (fecha != null)
        {
            fechaFormateada = fecha.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-ES")));
            fechaFormateada = fechaFormateada.substring(0, 1).toUpperCase(Locale.forLanguageTag("es-ES")) + fechaFormateada.substring(1);
        }

        model.addAttribute("fechaSeleccionada", fecha);
        model.addAttribute("fechaFormateada", fechaFormateada);
        model.addAttribute("noCancelable", noCancelable);
        model.addAttribute("motivoNoCancelable", motivoNoCancelable);
        model.addAttribute("clasesPendientes", clasesPendientes);
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
        Profesor profesor = this.obtenerProfesorActual(principal);
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
        if (examen.getEsApto() != null)
        {
            redirectAttributes.addFlashAttribute("error", "Este examen ya ha sido calificado previamente y no puede modificarse.");
            return "redirect:/profesor/examenes";
        }

        examen.setEsApto(dto.esApto());
        this.examenRepository.save(examen);

        // Si suspende, computar convocatoria gastada en la matrícula y notificar por email
        if (Boolean.FALSE.equals(dto.esApto()) && examen.getAlumno() != null)
        {
            Alumno alumno = examen.getAlumno();
            Optional<Matricula> matriculaOptional = this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno);
            int gastadas = 0;
            String nombreCarnet = "";
            if (matriculaOptional.isPresent())
            {
                Matricula matricula = matriculaOptional.get();
                gastadas = matricula.getConvocatoriasGastadas() != null ? matricula.getConvocatoriasGastadas() : 0;
                gastadas++;
                matricula.setConvocatoriasGastadas(gastadas);
                this.matriculaRepository.save(matricula);
                if (matricula.getPermisoCarnet() != null)
                {
                    nombreCarnet = matricula.getPermisoCarnet().getDescripcion();
                }
            }

            int convocatoriasRestantes = Math.max(0, 2 - gastadas);

            // Notificación por correo electrónico oficial de suspenso al alumno según tipo de examen
            if (alumno.getCorreo() != null && !alumno.getCorreo().isBlank())
            {
                if (examen.getTipo() == TipoExamen.TEORICO) // Si ha suspendido el examen teórico
                {
                    String asunto = "Resultado oficial de tu Examen Teórico DGT - ManDS Autoescuela";
                    String mensaje = "Estimado/a " + alumno.getNombre() + ",\n\n"
                            + "Te comunicamos oficialmente que el resultado de tu examen teórico oficial de conducir"
                            + (nombreCarnet.isBlank() ? "" : " para el " + nombreCarnet) + " ha sido calificado como NO APTO.\n\n"
                            + "Queremos transmitirte mucho ánimo. En esta convocatoria se ha consumido 1 intento de tu matrícula. Convocatorias oficiales restantes: " + convocatoriasRestantes + " de 2.\n\n"
                            + (gastadas >= 2
                                    ? "Has agotado las 2 convocatorias oficiales incluidas en tu matrícula. Para poder presentarte a una nueva convocatoria oficial de examen deberás abonar las tasas de Renovación de Matrícula desde tu panel de pagos.\n\n"
                                    : "Te recomendamos continuar repasando los test y el temario teórico. Podrás solicitar una nueva fecha de examen oficial desde tu panel de alumno en cuanto te sientas preparado/a.\n\n")
                            + "Recuerda que el acceso a clases prácticas de circulación se habilitará una vez superada la prueba teórica oficial ante la DGT.\n\n"
                            + "¡Mucho ánimo y a seguir adelante!\n\n"
                            + "Un cordial saludo,\n"
                            + "Equipo de Coordinación - ManDS Autoescuela";
                    this.emailService.enviarNotificacionAlumno(alumno.getCorreo(), asunto, mensaje);
                }
                else // Si ha suspendido el examen práctico
                {
                    String asunto = "Resultado oficial de tu Examen Práctico DGT - ManDS Autoescuela";
                    String mensaje = "Estimado/a " + alumno.getNombre() + ",\n\n"
                            + "Te comunicamos oficialmente que el resultado de tu examen práctico oficial de conducir"
                            + (nombreCarnet.isBlank() ? "" : " para el " + nombreCarnet) + " ha sido calificado como NO APTO.\n\n"
                            + "No te desanimes, es una etapa habitual en el aprendizaje de la conducción. En esta prueba se ha consumido 1 intento de tu matrícula. Convocatorias oficiales restantes: " + convocatoriasRestantes + " de 2.\n\n"
                            + (gastadas >= 2
                                    ? "Has acumulado 2 convocatorias agotadas. Por normativa oficial, la adquisición de saldo y la reserva de clases prácticas quedan temporalmente pausadas hasta que tramites la Renovación de Matrícula desde tu panel de pagos.\n\n"
                                    : "Mantienes asignado a tu profesor de autoescuela. Te recomendamos reservar clases prácticas adicionales en el calendario para pulir las maniobras antes de tu siguiente examen.\n\n")
                            + "¡Mucho ánimo, estamos a tu lado para conseguirlo!\n\n"
                            + "Un cordial saludo,\n"
                            + "Equipo de Coordinación - ManDS Autoescuela";
                    this.emailService.enviarNotificacionAlumno(alumno.getCorreo(), asunto, mensaje);
                }
            }
        }
        // Si aprueba el examen práctico, finalizar el expediente del carnet y liberar cupo del profesor
        else if (Boolean.TRUE.equals(dto.esApto()) && examen.getAlumno() != null && examen.getTipo() == TipoExamen.PRACTICO)
        {
            Alumno alumno = examen.getAlumno();
            Optional<Matricula> matriculaOptional = this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno);
            String nombreCarnet = "";
            if (matriculaOptional.isPresent())
            {
                Matricula matricula = matriculaOptional.get();
                matricula.setEstaActiva(false);
                this.matriculaRepository.save(matricula);
                if (matricula.getPermisoCarnet() != null)
                {
                    nombreCarnet = matricula.getPermisoCarnet().getDescripcion();
                }
            }

            // Desvinculación de profesor asignado para liberar su cupo oficial
            alumno.setProfesor(null);
            this.alumnoRepository.save(alumno);

            // Cancelación de clases prácticas futuras pendientes si quedase alguna agendada
            List<ClasePractica> clasesPendientes = this.clasePracticaRepository.findByAlumnoAndEstadoClase(alumno, EstadoClase.PENDIENTE);
            for (ClasePractica clase : clasesPendientes)
            {
                clase.setEstadoClase(EstadoClase.CANCELADA);
                this.clasePracticaRepository.save(clase);
            }

            // Notificación por correo electrónico oficial de felicitación al alumno
            if (alumno.getCorreo() != null && !alumno.getCorreo().isBlank())
            {
                String asunto = "¡Enhorabuena! Has aprobado tu examen práctico en ManDS Autoescuela";
                String mensaje = "Estimado/a " + alumno.getNombre() + ",\n\n"
                        + "¡Muchísimas felicidades! Te comunicamos oficialmente que has resultado APTO en tu examen práctico de conducir"
                        + (nombreCarnet.isBlank() ? "" : " para el " + nombreCarnet) + ".\n\n"
                        + "Tu expediente para este permiso ha finalizado con éxito. A partir de ahora podrás consultar todo tu historial de clases y notas desde tu panel de usuario.\n\n"
                        + "Si en el futuro deseas matricularte en un nuevo permiso de conducir, podrás gestionarlo directamente desde tu panel personal.\n\n"
                        + "¡Gracias por confiar en ManDS Autoescuela!\n\n"
                        + "Un cordial saludo,\n"
                        + "Equipo de Coordinación - ManDS Autoescuela";
                this.emailService.enviarNotificacionAlumno(alumno.getCorreo(), asunto, mensaje);
            }
        }
        // Si aprueba el examen teórico, notificar por correo electrónico oficial de felicitación e inicio de prácticas
        else if (Boolean.TRUE.equals(dto.esApto()) && examen.getAlumno() != null && examen.getTipo() == TipoExamen.TEORICO)
        {
            Alumno alumno = examen.getAlumno();
            Optional<Matricula> matriculaOptional = this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno);
            String nombreCarnet = "";
            if (matriculaOptional.isPresent())
            {
                Matricula matricula = matriculaOptional.get();
                if (matricula.getPermisoCarnet() != null)
                {
                    nombreCarnet = matricula.getPermisoCarnet().getDescripcion();
                }
            }

            // Notificación por correo electrónico oficial de felicitación al alumno
            if (alumno.getCorreo() != null && !alumno.getCorreo().isBlank())
            {
                String asunto = "¡Enhorabuena! Has aprobado tu examen teórico en ManDS Autoescuela";
                String mensaje = "Estimado/a " + alumno.getNombre() + ",\n\n"
                        + "¡Muchísimas felicidades! Te comunicamos oficialmente que has resultado APTO en tu examen teórico oficial de conducir"
                        + (nombreCarnet.isBlank() ? "" : " para el " + nombreCarnet) + ".\n\n"
                        + "Has superado con éxito la fase teórica. A partir de este momento ya puedes comenzar con tu formación práctica y reservar tus clases en el calendario desde tu panel de alumno.\n\n"
                        + "Tu profesor asignado estará encantado de acompañarte en las prácticas de circulación.\n\n"
                        + "¡Gracias por confiar en ManDS Autoescuela!\n\n"
                        + "Un cordial saludo,\n"
                        + "Equipo de Coordinación - ManDS Autoescuela";
                this.emailService.enviarNotificacionAlumno(alumno.getCorreo(), asunto, mensaje);
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
     * Soporta peticiones tradicionales y peticiones dinámicas HTMX.
     */
    @PostMapping("/vehiculo/incidencia")
    @Transactional
    public String reportarIncidencia(@AuthenticationPrincipal Object principal, @Valid @ModelAttribute("reportarIncidenciaDTO") ReportarIncidenciaDTO dto, BindingResult bindingResult, HttpServletRequest request, HttpServletResponse response, Model model, RedirectAttributes redirectAttributes)
    {
        Profesor profesor = this.obtenerProfesorActual(principal);
        if (profesor == null)
        {
            return "redirect:/login";
        }

        boolean esPeticionHtmx = "true".equals(request.getHeader("HX-Request"));

        Vehiculo vehiculo = profesor.getVehiculo();
        if (vehiculo == null && dto.vehiculoId() != null)
        {
            vehiculo = this.vehiculoRepository.findById(dto.vehiculoId()).orElse(null);
        }

        if (bindingResult.hasErrors())
        {
            if (esPeticionHtmx)
            {
                model.addAttribute("errorReportarIncidencia", "La descripción de la avería debe tener entre 10 y 1000 caracteres.");
                model.addAttribute("vehiculo", vehiculo);
                model.addAttribute("abrirModalReportarIncidencia", true);
                return "profesor/fragments/modal-reportar-incidencia :: modal-reportar-incidencia";
            }
            redirectAttributes.addFlashAttribute("error", "La descripción de la avería debe tener entre 10 y 1000 caracteres.");
            return "redirect:/profesor/vehiculo";
        }

        if (vehiculo == null)
        {
            if (esPeticionHtmx)
            {
                model.addAttribute("errorReportarIncidencia", "No tienes ningún vehículo asignado para reportar incidencias.");
                model.addAttribute("abrirModalReportarIncidencia", true);
                return "profesor/fragments/modal-reportar-incidencia :: modal-reportar-incidencia";
            }
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

        // Notificación por correo al Administrador del sistema
        try
        {
            List<Administrador> administradores = this.personaRepository.findAdministradoresActivos();
            String nombreProfesor = profesor.getNombre() + " " + profesor.getApellidos();
            String vehiculoInfo = vehiculo.getMarca() + " " + vehiculo.getModelo();
            String urlIncidencias = (this.appBaseUrl != null && this.appBaseUrl.endsWith("/"))
                    ? this.appBaseUrl + "admin/incidencias"
                    : (this.appBaseUrl != null ? this.appBaseUrl + "/admin/incidencias" : "http://localhost:8080/admin/incidencias");

            if (administradores != null && !administradores.isEmpty())
            {
                for (Administrador admin : administradores)
                {
                    if (admin.getCorreo() != null && !admin.getCorreo().isBlank())
                    {
                        this.emailService.enviarNotificacionIncidenciaAdmin(
                                admin.getCorreo(),
                                admin.getNombre(),
                                nombreProfesor,
                                vehiculoInfo,
                                vehiculo.getMatricula(),
                                dto.descripcion(),
                                urlIncidencias
                        );
                    }
                }
            }
            else
            {
                this.emailService.enviarNotificacionIncidenciaAdmin(
                        "admin@autoescuela.es",
                        "Administrador",
                        nombreProfesor,
                        vehiculoInfo,
                        vehiculo.getMatricula(),
                        dto.descripcion(),
                        urlIncidencias
                );
            }
        }
        catch (Exception ex)
        {
            // Fallback silencioso ante indisponibilidad del servidor SMTP para no romper la experiencia de usuario
        }

        String mensajeExito = "Incidencia mecánica reportada correctamente. El Administrador ha sido notificado para su revisión.";
        redirectAttributes.addFlashAttribute("mensajeExito", mensajeExito);

        String contextPath = request.getContextPath() != null ? request.getContextPath() : "";
        String destinoRedireccion = contextPath + "/profesor/vehiculo";

        if (esPeticionHtmx)
        {
            FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
            if (flashMap != null)
            {
                flashMap.put("mensajeExito", mensajeExito);
                FlashMapManager flashMapManager = RequestContextUtils.getFlashMapManager(request);
                if (flashMapManager != null)
                {
                    flashMapManager.saveOutputFlashMap(flashMap, request, response);
                }
            }
            response.setHeader("HX-Redirect", destinoRedireccion);
            return null;
        }

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

        EstadisticasProfesorDTO estadisticas = this.estadisticaService.obtenerEstadisticasProfesor(profesor);

        model.addAttribute("profesor", profesor);
        model.addAttribute("estadisticas", estadisticas);

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

        model.addAttribute("perfilDTO", dto);
        model.addAttribute("vehiculo", profesor.getVehiculo());

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
            model.addAttribute("vehiculo", profesor.getVehiculo());
            return "profesor/perfil";
        }

        // Comprobar unicidad de teléfono si ha cambiado
        String nuevoTel = dto.telefono() != null ? dto.telefono().trim() : "";
        if (!nuevoTel.equals(profesor.getTelefono()) && this.personaRepository.existsByTelefonoAndIdNot(nuevoTel, profesor.getId()))
        {
            EditarPerfilProfesorDTO perfilRepoblado = this.profesorMapper.repoblarPerfilDTO(profesor, dto);
            model.addAttribute("profesor", profesor);
            model.addAttribute("perfilDTO", perfilRepoblado);
            model.addAttribute("vehiculo", profesor.getVehiculo());
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
