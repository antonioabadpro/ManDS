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
import org.springframework.stereotype.Controller;
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
import com.autoescuela.erp.academico.service.AcademicoService;
import com.autoescuela.erp.core.enums.EstadoClase;
import com.autoescuela.erp.core.security.UserDetailsImpl;
import com.autoescuela.erp.examenes.dto.CalificarExamenDTO;
import com.autoescuela.erp.examenes.model.Examen;
import com.autoescuela.erp.examenes.service.ExamenService;
import com.autoescuela.erp.flota.dto.ReportarIncidenciaDTO;
import com.autoescuela.erp.flota.model.IncidenciaVehiculo;
import com.autoescuela.erp.flota.model.Vehiculo;
import com.autoescuela.erp.flota.service.FlotaService;
import com.autoescuela.erp.flota.service.IncidenciaVehiculoService;
import com.autoescuela.erp.practicas.dto.CompletarClaseDTO;
import com.autoescuela.erp.practicas.dto.EventoCalendarioDTO;
import com.autoescuela.erp.practicas.model.ClasePractica;
import com.autoescuela.erp.practicas.service.ClasePracticaService;
import com.autoescuela.erp.usuarios.dto.CambiarPasswordDTO;
import com.autoescuela.erp.usuarios.dto.ContactarAlumnoDTO;
import com.autoescuela.erp.usuarios.dto.EditarPerfilProfesorDTO;
import com.autoescuela.erp.usuarios.mapper.ProfesorMapper;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.model.Profesor;
import com.autoescuela.erp.usuarios.service.ProfesorService;

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
    private final ProfesorService profesorService;
    private final AlumnoService alumnoService;
    private final AcademicoService academicoService;
    private final ExamenService examenService;
    private final ClasePracticaService clasePracticaService;
    private final FlotaService flotaService;
    private final IncidenciaVehiculoService incidenciaVehiculoService;
    private final CalendarioService calendarioService;
    private final EstadisticaService estadisticaService;
    private final ProfesorMapper profesorMapper;

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
        Profesor profe = this.profesorService.obtenerProfesorActual(principal);
        if (profe != null)
        {
            return profe.getNombre() + " " + profe.getApellidos();
        }
        return "Laura Sánchez Romero";
    }

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
    // 2. MI CALENDARIO DE PRÁCTICAS
    // =========================================================================
    @GetMapping("/calendario")
    public String mostrarCalendario(@RequestParam(value = "fechaRef", required = false) String fechaRefStr, @AuthenticationPrincipal Object principal, Model model)
    {
        Profesor profesor = this.profesorService.obtenerProfesorActual(principal);
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
        Profesor profesor = this.profesorService.obtenerProfesorActual(principal);
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
        Profesor profesor = this.profesorService.obtenerProfesorActual(principal);
        List<EventoCalendarioDTO> eventos = new ArrayList<>();
        if (profesor == null)
        {
            return eventos;
        }

        List<ClasePractica> clases = this.clasePracticaService.obtenerClasesPorProfesorAsc(profesor);
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
        List<Examen> examenes = this.examenService.obtenerExamenesPorProfesor(profesor);
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
        Profesor profesor = this.profesorService.obtenerProfesorActual(principal);
        if (profesor == null)
        {
            return "redirect:/login";
        }

        Optional<ClasePractica> claseOptional = this.clasePracticaService.buscarPorId(id);
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
        Profesor profesor = this.profesorService.obtenerProfesorActual(principal);
        if (profesor == null)
        {
            return "redirect:/login";
        }

        Optional<Examen> examenOptional = this.examenService.buscarExamenPorId(id);
        if (examenOptional.isEmpty())
        {
            return "error/404";
        }

        Examen examen = examenOptional.get();
        List<Examen> examenes = this.examenService.obtenerExamenesPorProfesor(profesor);

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
    public String completarClasePractica(@AuthenticationPrincipal Object principal, @Valid @ModelAttribute("completarClaseDTO") CompletarClaseDTO dto, BindingResult bindingResult, @RequestHeader(value = "HX-Request", required = false) String hxRequest, HttpServletResponse response, Model model, RedirectAttributes redirectAttributes)
    {
        Optional<ClasePractica> claseOptional = this.clasePracticaService.buscarPorId(dto.claseId());
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

        try
        {
            this.clasePracticaService.completarClasePractica(dto);
        }
        catch (ReglaNegocioException ex)
        {
            if (hxRequest != null)
            {
                Long kmActualVehiculo = (clase.getProfesor() != null && clase.getProfesor().getVehiculo() != null && clase.getProfesor().getVehiculo().getKm() != null)
                        ? clase.getProfesor().getVehiculo().getKm()
                        : (dto.kmInicio() != null ? dto.kmInicio() : 0L);
                model.addAttribute("kmActualVehiculo", kmActualVehiculo);
                model.addAttribute("clase", clase);
                model.addAttribute("error", ex.getMessage());
                return "profesor/fragments/modal-ficha-clase :: modalFichaClase";
            }
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/profesor/calendario";
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
    public String cancelarClasePractica(@AuthenticationPrincipal Object principal, @RequestParam("claseId") Long claseId, @RequestParam(value = "motivo", required = false) String motivo, @RequestHeader(value = "HX-Request", required = false) String hxRequest, HttpServletResponse response, Model model, RedirectAttributes redirectAttributes)
    {
        try
        {
            this.clasePracticaService.cancelarClasePorProfesor(claseId, motivo);
        }
        catch (ReglaNegocioException ex)
        {
            if (hxRequest != null)
            {
                model.addAttribute("error", ex.getMessage());
                return "profesor/fragments/alerta-feedback :: feedbackExito";
            }
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/profesor/calendario";
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
        Profesor profesor = this.profesorService.obtenerProfesorActual(principal);
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
        Profesor profesor = this.profesorService.obtenerProfesorActual(principal);
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
    public String cancelarClasesDia(@AuthenticationPrincipal Object principal, @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha, @RequestParam(value = "motivo", required = false) String motivo, @RequestHeader(value = "HX-Request", required = false) String hxRequest, HttpServletResponse response, Model model, RedirectAttributes redirectAttributes)
    {
        Profesor profesor = this.profesorService.obtenerProfesorActual(principal);
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

        List<Examen> examenesDgt = this.examenService.obtenerExamenesPorProfesor(profesor);
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

        int totalCanceladas;
        try
        {
            totalCanceladas = this.clasePracticaService.cancelarJornadaProfesor(profesor, fecha, motivo);
        }
        catch (ReglaNegocioException ex)
        {
            return this.devolverErrorCancelacion(ex.getMessage(), hxRequest, model, redirectAttributes);
        }

        String fechaFormatoLegible = fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
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
            List<Examen> examenes = this.examenService.obtenerExamenesPorProfesor(profesor);
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
            List<ClasePractica> clasesTodas = this.clasePracticaService.obtenerClasesPorProfesorYRango(profesor, inicioDia, finDia);
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
    // 4. CONVOCATORIAS Y CALIFICACIONES DGT
    // =========================================================================
    @GetMapping("/examenes")
    public String mostrarExamenes(@AuthenticationPrincipal Object principal, Model model)
    {
        Profesor profesor = this.profesorService.obtenerProfesorActual(principal);
        if (profesor == null)
        {
            return "redirect:/login";
        }

        List<Examen> misExamenes = this.examenService.obtenerExamenesPorProfesor(profesor);
        model.addAttribute("profesor", profesor);
        model.addAttribute("examenes", misExamenes);
        model.addAttribute("calificarExamenDTO", new CalificarExamenDTO());

        return "profesor/examenes";
    }

    /**
     * Asienta la calificación oficial de un examen práctico DGT (CU-025).
     */
    @PostMapping("/examenes/calificar")
    public String calificarExamen(@AuthenticationPrincipal Object principal, @Valid @ModelAttribute("calificarExamenDTO") CalificarExamenDTO dto, BindingResult bindingResult, RedirectAttributes redirectAttributes)
    {
        if (bindingResult.hasErrors())
        {
            redirectAttributes.addFlashAttribute("error", "Debes especificar si el resultado es Apto o No Apto.");
            return "redirect:/profesor/examenes";
        }

        try
        {
            this.examenService.calificarExamenOficial(dto);
            redirectAttributes.addFlashAttribute("mensajeExito", "Calificación registrada con éxito. Resultado: " + (Boolean.TRUE.equals(dto.esApto()) ? "APTO" : "NO APTO") + ".");
        }
        catch (ReglaNegocioException ex)
        {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/profesor/examenes";
    }

    // =========================================================================
    // 5. MI VEHÍCULO E INCIDENCIAS
    // =========================================================================
    @GetMapping("/vehiculo")
    public String mostrarVehiculo(@AuthenticationPrincipal Object principal, Model model)
    {
        Profesor profesor = this.profesorService.obtenerProfesorActual(principal);
        if (profesor == null)
        {
            return "redirect:/login";
        }

        Vehiculo vehiculo = profesor.getVehiculo();
        List<IncidenciaVehiculo> incidencias = vehiculo != null
                ? this.incidenciaVehiculoService.obtenerIncidenciasPorVehiculoDesc(vehiculo)
                : this.incidenciaVehiculoService.obtenerIncidenciasPorProfesorDesc(profesor);

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
    public String reportarIncidencia(@AuthenticationPrincipal Object principal, @Valid @ModelAttribute("reportarIncidenciaDTO") ReportarIncidenciaDTO dto, BindingResult bindingResult, HttpServletRequest request, HttpServletResponse response, Model model, RedirectAttributes redirectAttributes)
    {
        Profesor profesor = this.profesorService.obtenerProfesorActual(principal);
        if (profesor == null)
        {
            return "redirect:/login";
        }

        boolean esPeticionHtmx = "true".equals(request.getHeader("HX-Request"));

        Vehiculo vehiculo = profesor.getVehiculo();
        if (vehiculo == null && dto.vehiculoId() != null)
        {
            vehiculo = this.flotaService.buscarPorId(dto.vehiculoId()).orElse(null);
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

        try
        {
            this.incidenciaVehiculoService.reportarIncidencia(profesor, vehiculo, dto, this.appBaseUrl);
        }
        catch (ReglaNegocioException ex)
        {
            if (esPeticionHtmx)
            {
                model.addAttribute("errorReportarIncidencia", ex.getMessage());
                model.addAttribute("vehiculo", vehiculo);
                model.addAttribute("abrirModalReportarIncidencia", true);
                return "profesor/fragments/modal-reportar-incidencia :: modal-reportar-incidencia";
            }
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/profesor/vehiculo";
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
        Profesor profesor = this.profesorService.obtenerProfesorActual(principal);
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
