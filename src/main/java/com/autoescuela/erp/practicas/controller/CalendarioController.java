package com.autoescuela.erp.practicas.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.autoescuela.erp.academico.model.Matricula;
import com.autoescuela.erp.academico.service.AcademicoService;
import com.autoescuela.erp.core.enums.EstadoClase;
import com.autoescuela.erp.core.enums.ModalidadMatricula;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.examenes.model.Examen;
import com.autoescuela.erp.examenes.service.ExamenService;
import com.autoescuela.erp.practicas.dto.CalendarioSemanalDTO;
import com.autoescuela.erp.practicas.dto.CompletarClaseDTO;
import com.autoescuela.erp.practicas.dto.DiaCalendarioDTO;
import com.autoescuela.erp.practicas.dto.EventoCalendarioDTO;
import com.autoescuela.erp.practicas.model.ClasePractica;
import com.autoescuela.erp.practicas.service.CalendarioService;
import com.autoescuela.erp.practicas.service.ClasePracticaService;
import com.autoescuela.erp.practicas.util.FestivosCalendarioUtil;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.model.Profesor;
import com.autoescuela.erp.usuarios.service.AlumnoService;
import com.autoescuela.erp.usuarios.service.ProfesorService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Controlador de dominio unificado para la cuadrícula semanal de calendario SSR (HTMX).
 * Atiende las vistas y eventos de calendario tanto para Alumno como para Profesor.
 */
@Controller
@RequiredArgsConstructor
public class CalendarioController
{
    private final CalendarioService calendarioService;
    private final ClasePracticaService clasePracticaService;
    private final ProfesorService profesorService;
    private final AlumnoService alumnoService;
    private final AcademicoService academicoService;
    private final ExamenService examenService;

    // =========================================================================
    // 1. ENDPOINTS DE ALUMNO (/alumno/calendario)
    // =========================================================================

    /**
     * Muestra la vista interactiva del calendario del alumno con cuadrícula semanal SSR.
     */
    @GetMapping("/alumno/calendario")
    public String mostrarCalendarioAlumno(
            @RequestParam(value = "fechaRef", required = false) String fechaRefStr,
            @AuthenticationPrincipal Object principal,
            Model model,
            RedirectAttributes redirectAttributes)
    {
        Alumno alumno = this.alumnoService.obtenerAlumnoActual(principal);
        if (alumno == null)
        {
            return "redirect:/login";
        }

        Matricula matricula = this.academicoService.buscarMatriculaActiva(alumno).orElse(null);
        if (matricula == null)
        {
            redirectAttributes.addFlashAttribute("error", "Debes disponer de una matrícula activa para acceder al calendario de reservas.");
            return "redirect:/alumno/dashboard";
        }

        if (matricula.tieneConvocatoriasAgotadas())
        {
            redirectAttributes.addFlashAttribute("error", "Has agotado las convocatorias de examen de tu matrícula. Debes tramitar la renovación de matrícula para acceder al calendario y reservar clases prácticas.");
            return "redirect:/alumno/dashboard";
        }

        if (matricula.getModalidad() == ModalidadMatricula.TEORICO_PRACTICA)
        {
            boolean teoricoAprobado = this.examenService.tieneTeoricoAprobado(alumno);
            if (!teoricoAprobado)
            {
                redirectAttributes.addFlashAttribute("error", "No puedes acceder al panel para reservar clases prácticas hasta haber aprobado el examen teórico oficial ante la DGT.");
                return "redirect:/alumno/dashboard";
            }
        }

        LocalDate fechaRef = this.parsearFechaRef(fechaRefStr);
        int clasesReservadas = this.clasePracticaService.contarClasesPendientesPorAlumno(alumno);
        int capacidadReserva = this.calcularCapacidadReserva(alumno, matricula);
        CalendarioSemanalDTO calendarioSemanal = this.calendarioService.obtenerCalendarioAlumno(alumno, fechaRef);

        model.addAttribute("alumno", alumno);
        model.addAttribute("matricula", matricula);
        model.addAttribute("profesor", alumno.getProfesor());
        model.addAttribute("clasesReservadas", clasesReservadas);
        model.addAttribute("capacidadReserva", capacidadReserva);
        model.addAttribute("calendarioSemanal", calendarioSemanal);

        return "alumno/calendario";
    }

    /**
     * Endpoint HTMX que devuelve exclusivamente el fragmento de la cuadrícula semanal del alumno.
     */
    @GetMapping("/alumno/calendario/cuadricula")
    public String obtenerCuadriculaCalendarioAlumno(
            @RequestParam(value = "fechaRef", required = false) String fechaRefStr,
            @AuthenticationPrincipal Object principal,
            Model model)
    {
        Alumno alumno = this.alumnoService.obtenerAlumnoActual(principal);
        if (alumno == null)
        {
            return "redirect:/login";
        }

        LocalDate fechaRef = this.parsearFechaRef(fechaRefStr);
        Matricula matricula = this.academicoService.buscarMatriculaActiva(alumno).orElse(null);
        if (matricula == null || matricula.tieneConvocatoriasAgotadas())
        {
            return "redirect:/alumno/dashboard";
        }
        if (matricula.getModalidad() == ModalidadMatricula.TEORICO_PRACTICA
                && !this.examenService.tieneTeoricoAprobado(alumno))
        {
            return "redirect:/alumno/dashboard";
        }

        int clasesReservadas = this.clasePracticaService.contarClasesPendientesPorAlumno(alumno);
        int capacidadReserva = this.calcularCapacidadReserva(alumno, matricula);
        CalendarioSemanalDTO calendarioSemanal = this.calendarioService.obtenerCalendarioAlumno(alumno, fechaRef);

        model.addAttribute("alumno", alumno);
        model.addAttribute("matricula", matricula);
        model.addAttribute("profesor", alumno.getProfesor());
        model.addAttribute("clasesReservadas", clasesReservadas);
        model.addAttribute("capacidadReserva", capacidadReserva);
        model.addAttribute("calendarioSemanal", calendarioSemanal);

        return "alumno/fragments/cuadricula-calendario :: cuadriculaSemanal";
    }

    /**
     * Endpoint API JSON que provee los eventos para FullCalendar v6 en la vista del alumno.
     */
    @GetMapping("/alumno/calendario/eventos")
    @ResponseBody
    public List<EventoCalendarioDTO> obtenerEventosCalendarioAlumno(@AuthenticationPrincipal Object principal)
    {
        Alumno alumno = this.alumnoService.obtenerAlumnoActual(principal);
        List<EventoCalendarioDTO> eventos = new ArrayList<>();
        if (alumno == null)
        {
            return eventos;
        }

        DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        // 1. Clases propias del alumno
        List<ClasePractica> misClases = this.clasePracticaService.obtenerClasesPorAlumnoExcluyendoEstado(alumno, EstadoClase.CANCELADA);
        for (ClasePractica c : misClases)
        {
            String bg = "#f59e0b";
            String border = "#d97706";
            String title = "Tu Clase Práctica (Pendiente)";

            if (c.getEstadoClase() == EstadoClase.RECIBIDA)
            {
                bg = "#10b981";
                border = "#059669";
                title = "Tu Clase Práctica (Recibida)";
            }
            else if (c.getEstadoClase() == EstadoClase.CANCELADA)
            {
                bg = "#64748b";
                border = "#475569";
                title = "Tu Clase (Cancelada)";
            }

            Map<String, Object> props = new HashMap<>();
            props.put("esPropia", true);
            props.put("claseId", c.getId());
            props.put("estado", c.getEstadoClase().name());
            props.put("recogida", c.getPuntoRecogida());

            eventos.add(new EventoCalendarioDTO(
                    String.valueOf(c.getId()),
                    title,
                    c.getFechaHora().format(isoFormatter),
                    c.getFechaHora().plusMinutes(c.getDuracion() != null ? c.getDuracion() : 45).format(isoFormatter),
                    false,
                    bg,
                    border,
                    "#ffffff",
                    props
            ));
        }

        // 2. Clases ocupadas del profesor y jornadas de examen DGT
        Profesor profesor = alumno.getProfesor();
        if (profesor != null)
        {
            List<ClasePractica> clasesProfesor = this.clasePracticaService.obtenerClasesPorProfesorAsc(profesor);
            for (ClasePractica c : clasesProfesor)
            {
                if (c.getAlumno() != null && !c.getAlumno().getId().equals(alumno.getId()) && c.getEstadoClase() != EstadoClase.CANCELADA)
                {
                    Map<String, Object> props = new HashMap<>();
                    props.put("esPropia", false);
                    props.put("tipo", "OCUPADO");

                    eventos.add(new EventoCalendarioDTO(
                            "ocupado-" + c.getId(),
                            "Horario Ocupado",
                            c.getFechaHora().format(isoFormatter),
                            c.getFechaHora().plusMinutes(c.getDuracion() != null ? c.getDuracion() : 45).format(isoFormatter),
                            false,
                            "#94a3b8",
                            "#64748b",
                            "#ffffff",
                            props
                    ));
                }
            }

            List<Examen> examenesDgt = this.examenService.obtenerExamenesPorProfesor(profesor);
            for (Examen ex : examenesDgt)
            {
                Map<String, Object> props = new HashMap<>();
                props.put("esPropia", false);
                props.put("tipo", "EXAMEN_DGT");

                eventos.add(new EventoCalendarioDTO(
                        "examen-" + ex.getId(),
                        "Jornada Examen Oficial DGT (Bloqueado)",
                        ex.getFechaHora().format(isoFormatter),
                        ex.getFechaHora().plusMinutes(ex.getDuracion() != null ? ex.getDuracion() : 45).format(isoFormatter),
                        false,
                        "#6366f1",
                        "#4f46e5",
                        "#ffffff",
                        props
                ));
            }
        }

        return eventos;
    }

    // =========================================================================
    // 2. ENDPOINTS DE PROFESOR (/profesor/calendario)
    // =========================================================================

    /**
     * Muestra la vista interactiva del calendario del profesor con cuadrícula semanal SSR.
     */
    @GetMapping("/profesor/calendario")
    public String mostrarCalendarioProfesor(
            @RequestParam(value = "fechaRef", required = false) String fechaRefStr,
            @AuthenticationPrincipal Object principal,
            Model model)
    {
        Profesor profesor = this.profesorService.obtenerProfesorActual(principal);
        if (profesor == null)
        {
            return "redirect:/login";
        }

        LocalDate fechaRef = this.parsearFechaRef(fechaRefStr);
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
    @GetMapping("/profesor/calendario/cuadricula")
    public String obtenerCuadriculaCalendarioProfesor(
            @RequestParam(value = "fechaRef", required = false) String fechaRefStr,
            @AuthenticationPrincipal Object principal,
            Model model)
    {
        Profesor profesor = this.profesorService.obtenerProfesorActual(principal);
        if (profesor == null)
        {
            return "redirect:/login";
        }

        LocalDate fechaRef = this.parsearFechaRef(fechaRefStr);
        CalendarioSemanalDTO calendarioSemanal = this.calendarioService.obtenerCalendarioProfesor(profesor, fechaRef);

        model.addAttribute("profesor", profesor);
        model.addAttribute("vehiculo", profesor.getVehiculo());
        model.addAttribute("completarClaseDTO", new CompletarClaseDTO());
        model.addAttribute("calendarioSemanal", calendarioSemanal);

        return "profesor/fragments/cuadricula-calendario :: cuadriculaSemanal";
    }

    /**
     * Endpoint API para serializar las clases prácticas y jornadas de examen en FullCalendar v6.
     */
    @GetMapping("/profesor/calendario/eventos")
    @ResponseBody
    public List<EventoCalendarioDTO> obtenerEventosCalendarioProfesor(@AuthenticationPrincipal Object principal)
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
            String bg = "#f59e0b";
            String border = "#d97706";
            if (clase.getEstadoClase() == EstadoClase.RECIBIDA)
            {
                bg = "#10b981";
                border = "#059669";
            }
            else if (clase.getEstadoClase() == EstadoClase.CANCELADA)
            {
                bg = "#64748b";
                border = "#475569";
            }

            String nombreAlumno = clase.getAlumno() != null
                    ? (clase.getAlumno().getNombre() + " " + clase.getAlumno().getApellidos())
                    : "Alumno sin asignar";
            String title = nombreAlumno + " (" + clase.getEstadoClase().name() + ")";

            Map<String, Object> props = new HashMap<>();
            props.put("tipo", "CLASE_PRACTICA");
            props.put("claseId", clase.getId());
            props.put("alumnoNombre", nombreAlumno);
            props.put("estado", clase.getEstadoClase().name());
            props.put("recogida", clase.getPuntoRecogida());

            eventos.add(new EventoCalendarioDTO(
                    "clase-" + clase.getId(),
                    title,
                    clase.getFechaHora().format(isoFormatter),
                    clase.getFechaHora().plusMinutes(clase.getDuracion() != null ? clase.getDuracion() : 45).format(isoFormatter),
                    false,
                    bg,
                    border,
                    "#ffffff",
                    props
            ));
        }

        List<Examen> examenes = this.examenService.obtenerExamenesPorProfesor(profesor);
        for (Examen examen : examenes)
        {
            Map<String, Object> props = new HashMap<>();
            props.put("tipo", "EXAMEN_DGT");
            props.put("examenId", examen.getId());

            eventos.add(new EventoCalendarioDTO(
                    "examen-" + examen.getId(),
                    "Jornada Oficial Examen DGT",
                    examen.getFechaHora().format(isoFormatter),
                    examen.getFechaHora().plusMinutes(examen.getDuracion() != null ? examen.getDuracion() : 45).format(isoFormatter),
                    false,
                    "#6366f1",
                    "#4f46e5",
                    "#ffffff",
                    props
            ));
        }

        return eventos;
    }

    /**
     * Devuelve el fragmento Thymeleaf del modal para la cancelación masiva de clases prácticas de una jornada.
     */
    @GetMapping("/profesor/calendario/cancelar-dia/modal")
    public String obtenerModalCancelarDia(
            @RequestParam(value = "fecha", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(value = "fechaRef", required = false) String fechaRefStr,
            @AuthenticationPrincipal Object principal,
            Model model)
    {
        Profesor profesor = this.profesorService.obtenerProfesorActual(principal);
        if (profesor == null)
        {
            return "redirect:/login";
        }

        LocalDate hoy = LocalDate.now();
        LocalDate fechaRef = this.parsearFechaRef(fechaRefStr);
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
    @GetMapping("/profesor/calendario/cancelar-dia/calcular-clases")
    public String calcularClasesCancelarDia(
            @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @AuthenticationPrincipal Object principal,
            Model model)
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
     * Cancela de forma atómica todas las clases prácticas en estado PENDIENTE de una fecha concreta.
     */
    @PostMapping("/profesor/calendario/cancelar-dia")
    public String cancelarClasesDia(
            @AuthenticationPrincipal Object principal,
            @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(value = "motivo", required = false) String motivo,
            @RequestHeader(value = "HX-Request", required = false) String hxRequest,
            HttpServletResponse response,
            Model model,
            RedirectAttributes redirectAttributes)
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
                return this.devolverErrorCancelacion("Esta fecha coincide con una convocatoria oficial de examen DGT y no admite cancelación del profesor.", hxRequest, model, redirectAttributes);
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

    // =========================================================================
    // MÉTODOS AUXILIARES
    // =========================================================================

    private LocalDate parsearFechaRef(String fechaRefStr)
    {
        if (fechaRefStr != null && !fechaRefStr.isBlank())
        {
            try
            {
                return LocalDate.parse(fechaRefStr);
            }
            catch (Exception ignored)
            {
            }
        }
        return LocalDate.now();
    }

    private int calcularCapacidadReserva(Alumno alumno, Matricula matricula)
    {
        if (matricula == null || matricula.getSaldoClases() == null)
        {
            return 0;
        }
        int clasesPendientes = this.clasePracticaService.contarClasesPendientesPorAlumno(alumno);
        return Math.max(0, matricula.getSaldoClases() - clasesPendientes);
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
            Locale esLocale = Locale.of("es", "ES");
            fechaFormateada = fecha.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", esLocale));
            fechaFormateada = fechaFormateada.substring(0, 1).toUpperCase(esLocale) + fechaFormateada.substring(1);
        }

        model.addAttribute("fechaSeleccionada", fecha);
        model.addAttribute("fechaFormateada", fechaFormateada);
        model.addAttribute("noCancelable", noCancelable);
        model.addAttribute("motivoNoCancelable", motivoNoCancelable);
        model.addAttribute("clasesPendientes", clasesPendientes);
    }
}
