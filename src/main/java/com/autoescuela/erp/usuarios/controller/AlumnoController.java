package com.autoescuela.erp.usuarios.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.pagos.dto.SesionPagoDTO;
import com.autoescuela.erp.pagos.service.PagoStripeService;

import com.autoescuela.erp.academico.model.Matricula;
import com.autoescuela.erp.academico.service.AcademicoService;
import com.autoescuela.erp.core.enums.EstadoClase;
import com.autoescuela.erp.core.enums.EstadoVehiculo;
import com.autoescuela.erp.core.enums.ModalidadMatricula;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.enums.TipoExamen;
import com.autoescuela.erp.core.enums.TipoTurno;
import com.autoescuela.erp.core.security.UserDetailsImpl;
import com.autoescuela.erp.estadisticas.dto.EstadisticasAlumnoDTO;
import com.autoescuela.erp.estadisticas.service.EstadisticaService;
import com.autoescuela.erp.examenes.dto.SolicitudExamenDTO;
import com.autoescuela.erp.examenes.model.Examen;
import com.autoescuela.erp.examenes.model.SolicitudExamen;
import com.autoescuela.erp.examenes.service.ExamenService;
import com.autoescuela.erp.practicas.dto.CalendarioSemanalDTO;
import com.autoescuela.erp.practicas.dto.EventoCalendarioDTO;
import com.autoescuela.erp.practicas.dto.ReservaClasePracticaDTO;
import com.autoescuela.erp.practicas.model.ClasePractica;
import com.autoescuela.erp.practicas.service.CalendarioService;
import com.autoescuela.erp.practicas.service.ClasePracticaService;
import com.autoescuela.erp.practicas.util.FestivosCalendarioUtil;
import com.autoescuela.erp.usuarios.dto.EditarPerfilAlumnoDTO;
import com.autoescuela.erp.usuarios.mapper.AlumnoMapper;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.model.Profesor;
import com.autoescuela.erp.usuarios.service.AlumnoService;

import lombok.RequiredArgsConstructor;

/**
 * Controlador principal para el Portal del Alumno (ROLE_ALUMNO).
 * Gestiona el cuadro de mando, la reserva y consulta de clases prácticas (HTMX + Cuadrícula SSR),
 * el expediente de convocatorias y exámenes DGT, la adquisición de saldo y el perfil.
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
    private final CalendarioService calendarioService;
    private final EstadisticaService estadisticaService;
    private final PagoStripeService pagoStripeService;
    private final AlumnoMapper alumnoMapper;

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
        return "alumno1";
    }

    @ModelAttribute("nombreAlumno")
    public String obtenerNombreCompleto(@AuthenticationPrincipal Object principal)
    {
        if (principal instanceof UserDetailsImpl userDetails)
        {
            return userDetails.getNombreCompleto();
        }
        Alumno alumno = this.alumnoService.obtenerAlumnoActual(principal);
        if (alumno != null)
        {
            return alumno.getNombre() + " " + alumno.getApellidos();
        }
        return "Alumno Desconocido";
    }

    @ModelAttribute("esTeoricoPractica")
    public boolean esTeoricoPractica(@AuthenticationPrincipal Object principal)
    {
        Alumno alumno = this.alumnoService.obtenerAlumnoActual(principal);
        if (alumno == null)
        {
            return false;
        }
        return this.academicoService.buscarMatriculaActiva(alumno)
                .map(m -> m.getModalidad() == ModalidadMatricula.TEORICO_PRACTICA)
                .orElse(false);
    }

    @ModelAttribute("convocatoriasAgotadas")
    public boolean tieneConvocatoriasAgotadas(@AuthenticationPrincipal Object principal)
    {
        Alumno alumno = this.alumnoService.obtenerAlumnoActual(principal);
        if (alumno == null)
        {
            return false;
        }

        Matricula matricula = this.academicoService.buscarMatriculaActiva(alumno).orElse(null);
        boolean convocatoriasAgotadas = matricula != null && matricula.tieneConvocatoriasAgotadas();

        return convocatoriasAgotadas;
    }

    @ModelAttribute("teoricoAprobado")
    public boolean teoricoAprobado(@AuthenticationPrincipal Object principal)
    {
        Alumno alumno = this.alumnoService.obtenerAlumnoActual(principal);
        if (alumno == null)
        {
            return false;
        }

        return this.examenService.tieneTeoricoAprobado(alumno);
    }

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
        boolean sinMatriculaActiva = (matricula == null);

        model.addAttribute("alumno", alumno);
        model.addAttribute("matricula", matricula);
        model.addAttribute("convocatoriasAgotadas", convocatoriasAgotadas);
        model.addAttribute("sinMatriculaActiva", sinMatriculaActiva);
        model.addAttribute("tiposCarnet", TipoCarnet.values());
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
    // 2. CALENDARIO Y RESERVAS DE PRÁCTICAS (HTMX + FullCalendar v6)
    // =========================================================================
    @GetMapping("/calendario")
    public String mostrarCalendario(@RequestParam(value = "fechaRef", required = false) String fechaRefStr, @AuthenticationPrincipal Object principal, Model model, RedirectAttributes redirectAttributes)
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
    @GetMapping("/calendario/cuadricula")
    public String obtenerCuadriculaCalendario(@RequestParam(value = "fechaRef", required = false) String fechaRefStr, @AuthenticationPrincipal Object principal, Model model)
    {
        Alumno alumno = this.alumnoService.obtenerAlumnoActual(principal);
        if (alumno == null)
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
     * Endpoint API JSON que provee los eventos para FullCalendar v6 en la vista del alumno:
     * - Clases propias (Pendiente en ámbar, Recibida en verde, Cancelada en gris)
     * - Horarios ocupados del profesor asignado (bloqueados en gris)
     * - Jornadas oficiales de examen DGT (bloqueadas en índigo)
     */
    @GetMapping("/calendario/eventos")
    @ResponseBody
    public List<EventoCalendarioDTO> obtenerEventosCalendario(@AuthenticationPrincipal Object principal)
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
            String bg = "#f59e0b"; // Ámbar por defecto (PENDIENTE)
            String border = "#d97706";
            String title = "Tu Clase Práctica (Pendiente)";

            if (c.getEstadoClase() == EstadoClase.RECIBIDA)
            {
                bg = "#10b981"; // Verde esmeralda
                border = "#059669";
                title = "Tu Clase Práctica (Recibida)";
            }
            else if (c.getEstadoClase() == EstadoClase.CANCELADA)
            {
                bg = "#64748b"; // Gris pizarra
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

        // 2. Horarios ocupados del profesor por otros alumnos y jornadas DGT
        Profesor profesor = alumno.getProfesor();
        if (profesor != null)
        {
            List<ClasePractica> clasesProfesor = this.clasePracticaService.obtenerClasesPorProfesorAsc(profesor);
            for (ClasePractica cp : clasesProfesor)
            {
                // Si la clase no es de este alumno y no está cancelada, se visualiza como hueco ocupado (no disponible)
                if (cp.getAlumno() != null && !cp.getAlumno().getId().equals(alumno.getId()) && cp.getEstadoClase() != EstadoClase.CANCELADA)
                {
                    Map<String, Object> props = new HashMap<>();
                    props.put("esPropia", false);
                    props.put("tipo", "OCUPADO");

                    eventos.add(new EventoCalendarioDTO(
                            "ocupado-" + cp.getId(),
                            "Horario Ocupado",
                            cp.getFechaHora().format(isoFormatter),
                            cp.getFechaHora().plusMinutes(cp.getDuracion() != null ? cp.getDuracion() : 45).format(isoFormatter),
                            false,
                            "#94a3b8",
                            "#64748b",
                            "#ffffff",
                            props
                    ));
                }
            }

            // Exámenes oficiales programados en el profesor
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

    /**
     * Devuelve el fragmento modal Thymeleaf con el formulario HTMX para reservar clase.
     */
    @GetMapping("/clases/reservar-modal")
    public String obtenerModalReservarClase(@RequestParam(value = "fecha", required = false) String fecha, @AuthenticationPrincipal Object principal, Model model)
    {
        Alumno alumno = this.alumnoService.obtenerAlumnoActual(principal);
        if (alumno == null)
        {
            return "redirect:/login";
        }

        Matricula matricula = this.academicoService.buscarMatriculaActiva(alumno).orElse(null);
        if (matricula == null || matricula.tieneConvocatoriasAgotadas())
        {
            model.addAttribute("error", "Has agotado las convocatorias de tu matrícula. Debes tramitar la renovación antes de reservar clases prácticas.");
            return "alumno/fragments/alerta-feedback :: feedbackExito";
        }
        if (matricula.getModalidad() == ModalidadMatricula.TEORICO_PRACTICA
                && !this.examenService.tieneTeoricoAprobado(alumno))
        {
            model.addAttribute("error", "No puedes reservar clases prácticas hasta haber aprobado el examen teórico oficial ante la DGT.");
            return "alumno/fragments/alerta-feedback :: feedbackExito";
        }

        long clasesReservadas = this.clasePracticaService.contarClasesPendientesPorAlumno(alumno);
        int capacidadReserva = this.calcularCapacidadReserva(alumno, matricula);

        String fechaPreseleccionada = "";
        // Si se proporciona un parámetro de fecha, se intenta parsear y formatear para preseleccionar en el formulario
        if (fecha != null && !fecha.isBlank())
        {
            try
            {
                if (fecha.length() == 10)
                {
                    fechaPreseleccionada = fecha + "T10:00";
                }
                else
                {
                    fechaPreseleccionada = fecha.substring(0, 16);
                }
            }
            catch (Exception ignored)
            {
            }
        }

        boolean esNoLaborable = false;
        String motivoNoLaborable = null;
        String tipoNoLaborable = null;

        if (!fechaPreseleccionada.isBlank())
        {
            try
            {
                LocalDate fechaParsed = LocalDate.parse(fechaPreseleccionada.substring(0, 10));
                esNoLaborable = FestivosCalendarioUtil.esDiaNoLaborable(fechaParsed);
                if (esNoLaborable)
                {
                    motivoNoLaborable = FestivosCalendarioUtil.obtenerMotivoNoLaborable(fechaParsed);
                    tipoNoLaborable = FestivosCalendarioUtil.obtenerTipoNoLaborable(fechaParsed);
                }
            }
            catch (Exception ignored)
            {
            }
        }

        boolean vehiculoEnMantenimiento = alumno.getProfesor() != null
                                        && alumno.getProfesor().getVehiculo() != null
                                        && alumno.getProfesor().getVehiculo().getEstado() == EstadoVehiculo.MANTENIMIENTO;

        model.addAttribute("alumno", alumno);
        model.addAttribute("matricula", matricula);
        model.addAttribute("profesor", alumno.getProfesor());
        model.addAttribute("vehiculoEnMantenimiento", vehiculoEnMantenimiento);
        model.addAttribute("clasesReservadas", clasesReservadas);
        model.addAttribute("capacidadReserva", capacidadReserva);
        model.addAttribute("fechaPreseleccionada", fechaPreseleccionada);
        model.addAttribute("esNoLaborable", esNoLaborable);
        model.addAttribute("motivoNoLaborable", motivoNoLaborable);
        model.addAttribute("tipoNoLaborable", tipoNoLaborable);

        return "alumno/fragments/modal-reservar-clase :: modalReservarClase";
    }

    /**
     * Devuelve el fragmento modal Thymeleaf con los detalles de una clase práctica del alumno.
     */
    @GetMapping("/clases/{id}/modal")
    public String obtenerModalDetalleClase(@PathVariable("id") Long id, @AuthenticationPrincipal Object principal, Model model)
    {
        Alumno alumno = this.alumnoService.obtenerAlumnoActual(principal);
        if (alumno == null)
        {
            return "redirect:/login";
        }

        Optional<ClasePractica> claseOptional = this.clasePracticaService.buscarPorId(id);
        if (claseOptional.isEmpty())
        {
            return "error/404";
        }

        ClasePractica clase = claseOptional.get();
        if (clase.getAlumno() != null && !clase.getAlumno().getId().equals(alumno.getId()))
        {
            return "error/403";
        }

        model.addAttribute("clase", clase);
        return "alumno/fragments/modal-detalle-clase :: modalDetalleClase";
    }

    /**
     * Devuelve el fragmento modal Thymeleaf con la doble confirmación para cancelar una clase práctica.
     */
    @GetMapping("/clases/{id}/cancelar-modal")
    public String obtenerModalConfirmarCancelarClase(@PathVariable("id") Long id, @AuthenticationPrincipal Object principal, Model model)
    {
        Alumno alumno = this.alumnoService.obtenerAlumnoActual(principal);
        if (alumno == null)
        {
            return "redirect:/login";
        }

        Optional<ClasePractica> claseOptional = this.clasePracticaService.buscarPorId(id);
        if (claseOptional.isEmpty())
        {
            return "error/404";
        }

        ClasePractica clase = claseOptional.get();
        if (clase.getAlumno() != null && !clase.getAlumno().getId().equals(alumno.getId()))
        {
            return "error/403";
        }

        model.addAttribute("clase", clase);
        return "alumno/fragments/modal-confirmar-cancelar-clase :: modalConfirmarCancelarClase";
    }

    /**
     * Procesa la reserva transaccional de una clase práctica (CU-034).
     * Aplica la fórmula CapacidadReserva = saldoClases - clasesReservadas > 0 y control de solapamiento.
     */
    @PostMapping("/clases/reservar")
    @Transactional
    public String reservarClasePractica(@AuthenticationPrincipal Object principal, @Valid @ModelAttribute("reservaDTO") ReservaClasePracticaDTO dto, BindingResult bindingResult, @RequestHeader(value = "HX-Request", required = false) String hxRequest, HttpServletResponse response, Model model, RedirectAttributes redirectAttributes)
    {
        Alumno alumno = this.alumnoService.obtenerAlumnoActual(principal);
        if (alumno == null)
        {
            return "redirect:/login";
        }

        Matricula matricula = this.academicoService.buscarMatriculaActiva(alumno).orElse(null);
        if (matricula == null || matricula.tieneConvocatoriasAgotadas())
        {
            String error = "Has agotado las convocatorias de tu matrícula. Debes tramitar la renovación antes de reservar clases prácticas.";
            if (hxRequest != null)
            {
                model.addAttribute("error", error);
                return "alumno/fragments/alerta-feedback :: feedbackExito";
            }
            redirectAttributes.addFlashAttribute("error", error);
            return "redirect:/alumno/calendario";
        }
        if (matricula.getModalidad() == ModalidadMatricula.TEORICO_PRACTICA
                && !this.examenService.tieneTeoricoAprobado(alumno))
        {
            String error = "No puedes reservar clases prácticas hasta haber aprobado el examen teórico oficial ante la DGT.";
            if (hxRequest != null)
            {
                model.addAttribute("error", error);
                return "alumno/fragments/alerta-feedback :: feedbackExito";
            }
            redirectAttributes.addFlashAttribute("error", error);
            return "redirect:/alumno/calendario";
        }

        int capacidad = this.calcularCapacidadReserva(alumno, matricula);

        // Validación de Capacidad de Reserva
        if (capacidad <= 0)
        {
            String error = "No tienes capacidad de reserva disponible. Has agotado tu saldo de clases o tienes reservas pendientes de cursar.";
            if (hxRequest != null)
            {
                model.addAttribute("error", error);
                return "alumno/fragments/alerta-feedback :: feedbackExito";
            }
            redirectAttributes.addFlashAttribute("error", error);
            return "redirect:/alumno/calendario";
        }

        // Validación de Profesor Asignado
        Profesor profesor = alumno.getProfesor();
        if (profesor == null)
        {
            String error = "Aún no tienes un profesor asignado por la autoescuela para reservar clases.";
            if (hxRequest != null)
            {
                model.addAttribute("error", error);
                return "alumno/fragments/alerta-feedback :: feedbackExito";
            }
            redirectAttributes.addFlashAttribute("error", error);
            return "redirect:/alumno/calendario";
        }

        // Validación de Vehículo en Mantenimiento
        if (profesor.getVehiculo() != null && profesor.getVehiculo().getEstado() == EstadoVehiculo.MANTENIMIENTO)
        {
            String error = "El vehículo de tu profesor se encuentra actualmente en mantenimiento técnico. No es posible reservar clases prácticas hasta que finalice su reparación.";
            if (hxRequest != null)
            {
                model.addAttribute("error", error);
                return "alumno/fragments/alerta-feedback :: feedbackExito";
            }
            redirectAttributes.addFlashAttribute("error", error);
            return "redirect:/alumno/calendario";
        }

        if (bindingResult.hasErrors() || dto.fechaHora() == null)
        {
            String error = "Los datos de fecha, hora o punto de recogida no son válidos.";
            if (hxRequest != null)
            {
                model.addAttribute("error", error);
                return "alumno/fragments/alerta-feedback :: feedbackExito";
            }
            redirectAttributes.addFlashAttribute("error", error);
            return "redirect:/alumno/calendario";
        }

        // Validación de Festivos Nacionales y Periodos Vacacionales
        if (FestivosCalendarioUtil.esDiaNoLaborable(dto.fechaHora().toLocalDate()))
        {
            String motivo = FestivosCalendarioUtil.obtenerMotivoNoLaborable(dto.fechaHora().toLocalDate());
            String error = "No es posible reservar clases prácticas en días festivos nacionales o periodos vacacionales (" + motivo + ").";
            if (hxRequest != null)
            {
                model.addAttribute("error", error);
                return "alumno/fragments/alerta-feedback :: feedbackExito";
            }
            redirectAttributes.addFlashAttribute("error", error);
            return "redirect:/alumno/calendario";
        }

        // Validación de Turno del Profesor
        LocalTime hora = dto.fechaHora().toLocalTime();
        if (profesor.getTurno() == TipoTurno.MATINAL && (hora.isBefore(LocalTime.of(8, 0)) || hora.isAfter(LocalTime.of(15, 0))))
        {
            String error = "El profesor imparte turno MATINAL (08:00 a 15:00). Selecciona un tramo dentro de ese intervalo.";
            if (hxRequest != null)
            {
                model.addAttribute("error", error);
                return "alumno/fragments/alerta-feedback :: feedbackExito";
            }
            redirectAttributes.addFlashAttribute("error", error);
            return "redirect:/alumno/calendario";
        }
        else
        {
            if (profesor.getTurno() == TipoTurno.TARDE && (hora.isBefore(LocalTime.of(15, 0)) || hora.isAfter(LocalTime.of(22, 0))))
            {
                String error = "El profesor imparte turno de TARDE (15:00 a 22:00). Selecciona un tramo dentro de ese intervalo.";
                if (hxRequest != null)
                {
                    model.addAttribute("error", error);
                    return "alumno/fragments/alerta-feedback :: feedbackExito";
                }
                redirectAttributes.addFlashAttribute("error", error);
                return "redirect:/alumno/calendario";
            }
        }
        // Delegación transaccional con control de concurrencia First-Come, First-Served
        try
        {
            this.clasePracticaService.reservarClase(alumno, profesor, dto);
        }
        catch (ReglaNegocioException ex)
        {
            String error = ex.getMessage();
            if (hxRequest != null)
            {
                response.setHeader("HX-Trigger", "actualizarCalendario");
                model.addAttribute("error", error);
                return "alumno/fragments/alerta-feedback :: feedbackExito";
            }
            redirectAttributes.addFlashAttribute("error", error);
            return "redirect:/alumno/calendario";
        }

        String mensajeExito = "¡Clase práctica reservada correctamente para el " +
                dto.fechaHora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "! Consulta tu agenda.";

        if (hxRequest != null)
        {
            int clasesReservadas = this.clasePracticaService.contarClasesPendientesPorAlumno(alumno);
            int capacidadReserva = this.calcularCapacidadReserva(alumno, matricula);
            model.addAttribute("matricula", matricula);
            model.addAttribute("clasesReservadas", clasesReservadas);
            model.addAttribute("capacidadReserva", capacidadReserva);
            response.setHeader("HX-Trigger", "actualizarCalendario");
            model.addAttribute("mensajeExito", mensajeExito);
            return "alumno/fragments/alerta-feedback :: feedbackExito";
        }

        redirectAttributes.addFlashAttribute("mensajeExito", mensajeExito);
        return "redirect:/alumno/calendario";
    }

    /**
     * Procesa la cancelación de una clase práctica en estado PENDIENTE por parte del alumno.
     */
    @PostMapping("/clases/cancelar")
    @Transactional
    public String cancelarClasePractica(@RequestParam("claseId") Long claseId, @AuthenticationPrincipal Object principal, @RequestHeader(value = "HX-Request", required = false) String hxRequest, HttpServletResponse response, Model model, RedirectAttributes redirectAttributes)
    {
        Alumno alumno = this.alumnoService.obtenerAlumnoActual(principal);
        if (alumno == null)
        {
            return "redirect:/login";
        }

        Optional<ClasePractica> claseOptional = this.clasePracticaService.buscarPorId(claseId);
        if (claseOptional.isEmpty())
        {
            String error = "No se ha encontrado la clase práctica solicitada.";
            if (hxRequest != null)
            {
                model.addAttribute("error", error);
                return "alumno/fragments/alerta-feedback :: feedbackExito";
            }
            redirectAttributes.addFlashAttribute("error", error);
            return "redirect:/alumno/calendario";
        }

        ClasePractica clase = claseOptional.get();
        if (clase.getAlumno() != null && !clase.getAlumno().getId().equals(alumno.getId()))
        {
            return "error/403";
        }

        try
        {
            this.clasePracticaService.cancelarClasePorAlumno(clase, alumno);
        }
        catch (ReglaNegocioException ex)
        {
            String error = ex.getMessage();
            if (hxRequest != null)
            {
                response.setHeader("HX-Trigger", "actualizarCalendario");
                model.addAttribute("error", error);
                return "alumno/fragments/alerta-feedback :: feedbackExito";
            }
            redirectAttributes.addFlashAttribute("error", error);
            return "redirect:/alumno/calendario";
        }

        String mensajeExito = "Reserva de clase práctica cancelada con éxito. Has liberado tu capacidad de reserva.";

        if (hxRequest != null)
        {
            Matricula matricula = this.academicoService.buscarMatriculaActiva(alumno).orElse(null);
            int clasesReservadas = this.clasePracticaService.contarClasesPendientesPorAlumno(alumno);
            int capacidadReserva = this.calcularCapacidadReserva(alumno, matricula);
            model.addAttribute("matricula", matricula);
            model.addAttribute("clasesReservadas", clasesReservadas);
            model.addAttribute("capacidadReserva", capacidadReserva);
            response.setHeader("HX-Trigger", "actualizarCalendario");
            model.addAttribute("mensajeExito", mensajeExito);
            return "alumno/fragments/alerta-feedback :: feedbackExito";
        }

        redirectAttributes.addFlashAttribute("mensajeExito", mensajeExito);
        return "redirect:/alumno/calendario";
    }

    // =========================================================================
    // 3. HISTORIAL DE CLASES PRÁCTICAS
    // =========================================================================
    @GetMapping("/clases")
    public String mostrarHistorialClases(@AuthenticationPrincipal Object principal, @RequestHeader(value = "HX-Request", required = false) String hxRequest, Model model)
    {
        Alumno alumno = this.alumnoService.obtenerAlumnoActual(principal);
        if (alumno == null)
        {
            return "redirect:/login";
        }

        List<ClasePractica> clases = this.clasePracticaService.obtenerClasesPorAlumnoDesc(alumno);
        Matricula matricula = this.academicoService.buscarMatriculaActiva(alumno).orElse(null);
        int clasesReservadas = this.clasePracticaService.contarClasesPendientesPorAlumno(alumno);
        int capacidadReserva = this.calcularCapacidadReserva(alumno, matricula);

        model.addAttribute("clases", clases);
        model.addAttribute("totalClases", clases.size());
        model.addAttribute("matricula", matricula);
        model.addAttribute("clasesReservadas", clasesReservadas);
        model.addAttribute("capacidadReserva", capacidadReserva);

        if (hxRequest != null)
        {
            return "alumno/clases :: tablaHistorialClases";
        }

        return "alumno/clases";
    }

    // =========================================================================
    // 4. SOLICITUD Y CONVOCATORIAS DGT
    // =========================================================================
    @GetMapping("/examenes")
    public String mostrarExamenes(@AuthenticationPrincipal Object principal, Model model)
    {
        Alumno alumno = this.alumnoService.obtenerAlumnoActual(principal);
        if (alumno == null)
        {
            return "redirect:/login";
        }

        Matricula matricula = this.academicoService.buscarMatriculaActiva(alumno)
                .orElseGet(() -> this.academicoService.obtenerUltimaMatricula(alumno).orElse(null));
        List<SolicitudExamen> solicitudes = matricula != null
                ? this.examenService.obtenerSolicitudesPorMatriculaDesc(matricula)
                : this.examenService.obtenerSolicitudesPorAlumnoDesc(alumno);

        model.addAttribute("alumno", alumno);
        model.addAttribute("matricula", matricula);
        model.addAttribute("solicitudes", solicitudes);

        return "alumno/examenes";
    }

    /**
     * Modal HTMX para formalizar solicitud de examen DGT.
     */
    @GetMapping("/examenes/solicitar-modal")
    public String obtenerModalSolicitarExamen(@AuthenticationPrincipal Object principal, Model model)
    {
        Alumno alumno = this.alumnoService.obtenerAlumnoActual(principal);
        if (alumno == null)
        {
            return "redirect:/login";
        }

        Matricula matricula = this.academicoService.buscarMatriculaActiva(alumno).orElse(null);
        boolean teoricoAprobado = this.examenService.tieneTeoricoAprobado(alumno);
        boolean practicoAprobado = this.examenService.tienePracticoAprobado(alumno);

        boolean expedienteCerrado = (matricula == null || !matricula.getEstaActiva()) && practicoAprobado;
        boolean esMatriculaActiva = matricula == null || matricula.getEstaActiva();
        boolean convocatoriasAgotadas = matricula != null && (matricula.getConvocatorias() == null || matricula.getConvocatorias() <= 0);
        boolean tieneSolicitudPendiente = this.examenService.tieneSolicitudPendiente(alumno);
        boolean tieneSolicitudAceptada = this.examenService.tieneSolicitudAceptadaConExamenPendiente(alumno);

        boolean puedeSolicitarTeorico = matricula != null
                && matricula.getModalidad() == ModalidadMatricula.TEORICO_PRACTICA
                && !teoricoAprobado;

        boolean puedeSolicitarPractico = matricula != null
                && (matricula.getModalidad() == ModalidadMatricula.PRACTICA || teoricoAprobado);

        model.addAttribute("matricula", matricula);
        model.addAttribute("expedienteCerrado", expedienteCerrado);
        model.addAttribute("sinMatriculaActiva", esMatriculaActiva);
        model.addAttribute("convocatoriasAgotadas", convocatoriasAgotadas);
        model.addAttribute("tienePendiente", tieneSolicitudPendiente);
        model.addAttribute("tieneExamenProgramado", tieneSolicitudAceptada);
        model.addAttribute("teoricoAprobado", teoricoAprobado);
        model.addAttribute("practicoAprobado", practicoAprobado);
        model.addAttribute("puedeSolicitarTeorico", puedeSolicitarTeorico);
        model.addAttribute("puedeSolicitarPractico", puedeSolicitarPractico);

        return "alumno/fragments/modal-solicitar-examen :: modalSolicitarExamen";
    }

    /**
     * Procesa la presentación de una nueva solicitud de examen DGT (CU-038).
     */
    @PostMapping("/examenes/solicitar")
    @Transactional
    public String solicitarExamenDgt(@AuthenticationPrincipal Object principal, @Valid @ModelAttribute("solicitudDTO") SolicitudExamenDTO dto, BindingResult bindingResult, RedirectAttributes redirectAttributes)
    {
        Alumno alumno = this.alumnoService.obtenerAlumnoActual(principal);
        if (alumno == null)
        {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors())
        {
            redirectAttributes.addFlashAttribute("error", "Debe seleccionar un tipo de prueba oficial válido.");
            return "redirect:/alumno/examenes";
        }

        Matricula matricula = this.academicoService.buscarMatriculaActiva(alumno).orElse(null);
        if (matricula == null || !matricula.getEstaActiva())
        {
            redirectAttributes.addFlashAttribute("error", "Debes tener una matrícula activa para solicitar fecha de examen.");
            return "redirect:/alumno/examenes";
        }

        boolean teoricoAprobado = this.examenService.tieneTeoricoAprobado(alumno);
        boolean practicoAprobado = this.examenService.tienePracticoAprobado(alumno);

        if (practicoAprobado)
        {
            redirectAttributes.addFlashAttribute("error", "Has superado con éxito las pruebas oficiales de tu permiso. Tu expediente se encuentra cerrado.");
            return "redirect:/alumno/examenes";
        }

        if (matricula.getConvocatorias() == null || matricula.getConvocatorias() <= 0)
        {
            redirectAttributes.addFlashAttribute("error", "Has agotado las convocatorias de tu matrícula. Debes tramitar la renovación previamente.");
            return "redirect:/alumno/examenes";
        }

        // Comprobar si ya existe una solicitud PENDIENTE
        boolean tieneSolicitudPendiente = this.examenService.tieneSolicitudPendiente(alumno);
        if (tieneSolicitudPendiente)
        {
            redirectAttributes.addFlashAttribute("error", "Ya tienes una solicitud de examen en trámite pendiente de resolución por la Administración.");
            return "redirect:/alumno/examenes";
        }

        // Comprobar si tiene una solicitud ACEPTADA con examen futuro no celebrado
        boolean tieneSolicitudAceptada = this.examenService.tieneSolicitudAceptadaConExamenPendiente(alumno);
        if (tieneSolicitudAceptada)
        {
            redirectAttributes.addFlashAttribute("error", "Ya dispones de una convocatoria oficial asignada y pendiente de realización. No puedes realizar otra solicitud hasta haber celebrado dicho examen.");
            return "redirect:/alumno/examenes";
        }

        // Validaciones de tipo de examen según modalidad y aprobados previos
        if (dto.tipoExamen() == TipoExamen.TEORICO)
        {
            if (matricula.getModalidad() == ModalidadMatricula.PRACTICA)
            {
                redirectAttributes.addFlashAttribute("error", "No puedes solicitar examen teórico con una matrícula de modalidad práctica.");
                return "redirect:/alumno/examenes";
            }
            if (teoricoAprobado)
            {
                redirectAttributes.addFlashAttribute("error", "Ya has aprobado el examen teórico, no puedes volver a solicitarlo.");
                return "redirect:/alumno/examenes";
            }
        }
        else
        {
            if (dto.tipoExamen() == TipoExamen.PRACTICO)
            {
                if (matricula.getModalidad() == ModalidadMatricula.TEORICO_PRACTICA && !teoricoAprobado)
                {
                    redirectAttributes.addFlashAttribute("error", "Debes haber aprobado el examen teórico previamente para solicitar el examen práctico.");
                    return "redirect:/alumno/examenes";
                }
            }
        }

        this.examenService.registrarSolicitudExamen(alumno, dto.tipoExamen(), LocalDateTime.now(), matricula);

        redirectAttributes.addFlashAttribute("mensajeExito", "Solicitud de examen registrada con éxito. Ha ingresado en la cola oficial para su asignación.");
        return "redirect:/alumno/examenes";
    }

    // =========================================================================

    // 5. CALIFICACIONES Y NOTAS DGT
    // =========================================================================
    @GetMapping("/notas")
    public String mostrarNotas(@AuthenticationPrincipal Object principal, Model model)
    {
        Alumno alumno = this.alumnoService.obtenerAlumnoActual(principal);
        if (alumno == null)
        {
            return "redirect:/login";
        }

        Matricula matricula = this.academicoService.buscarMatriculaActiva(alumno).orElse(null);
        List<Examen> examenes = this.examenService.obtenerExamenesPorAlumno(alumno);

        long aptos = this.examenService.contarAptosPorAlumno(alumno);
        long noAptos = this.examenService.contarNoAptosPorAlumno(alumno);

        model.addAttribute("matricula", matricula);
        model.addAttribute("examenes", examenes);
        model.addAttribute("totalExamenes", examenes.size());
        model.addAttribute("examenesAprobados", aptos);
        model.addAttribute("examenesSuspensos", noAptos);

        return "alumno/notas";
    }

    // =========================================================================
    // 6. ADQUISICIÓN DE SALDO, BONOS Y RENOVACIÓN DE MATRÍCULA
    // =========================================================================
    @GetMapping("/pagos")
    public String mostrarPagos(@AuthenticationPrincipal Object principal, Model model)
    {
        Alumno alumno = this.alumnoService.obtenerAlumnoActual(principal);
        if (alumno == null)
        {
            return "redirect:/login";
        }

        Matricula matricula = this.academicoService.buscarMatriculaActiva(alumno).orElse(null);
        boolean esVehiculoPesado = matricula != null && matricula.getPermisoCarnet() != null &&
                (matricula.getPermisoCarnet().name().startsWith("PERMISO_C") || matricula.getPermisoCarnet().name().startsWith("PERMISO_D"));
        boolean convocatoriasAgotadas = matricula != null && matricula.tieneConvocatoriasAgotadas();

        model.addAttribute("matricula", matricula);
        model.addAttribute("esVehiculoPesado", esVehiculoPesado);
        model.addAttribute("convocatoriasAgotadas", convocatoriasAgotadas);

        return "alumno/pagos";
    }

    /**
     * Procesa la adquisición de clases sueltas o bonos oficiales conectando con Stripe Checkout.
     * Bloquea la compra si el alumno ha agotado sus convocatorias oficiales y debe renovar su matrícula.
     */
    @PostMapping("/pagos/comprar-clases")
    public String comprarClasesPracticas(@AuthenticationPrincipal Object principal, @RequestParam("tipoProducto") String tipoProducto, @RequestParam(value = "cantidadClases", defaultValue = "1") Integer cantidadClases, RedirectAttributes redirectAttributes, HttpSession sesion)
    {
        int clasesCompradas = 1;
        boolean esOferta = false;

        Alumno alumno = this.alumnoService.obtenerAlumnoActual(principal);
        if (alumno == null)
        {
            return "redirect:/login";
        }

        Matricula matricula = this.academicoService.buscarMatriculaActiva(alumno).orElse(null);
        if (matricula == null)
        {
            redirectAttributes.addFlashAttribute("error", "No se encontró un expediente de matrícula activo para recargar saldo.");
            return "redirect:/alumno/pagos";
        }

        // Regla: Bloqueo de compra de clases si ha agotado sus 2 convocatorias y debe renovar
        if (matricula.tieneConvocatoriasAgotadas())
        {
            redirectAttributes.addFlashAttribute("error", "Has agotado las convocatorias de examen de tu matrícula. No puedes adquirir clases prácticas ni bonos hasta renovar tu matrícula.");
            return "redirect:/alumno/pagos";
        }

        switch(tipoProducto != null ? tipoProducto.toUpperCase() : "")
        {
            case "BONO_10":
            {
                clasesCompradas = 10;
                esOferta = true;
            }; break;
            case "BONO_15":
            {
                clasesCompradas = 15;
                esOferta = true;
            }; break;
            case "BONO_20":
            {
                clasesCompradas = 20;
                esOferta = true;

            }; break;
            default:
            {
                if (cantidadClases != null && cantidadClases > 0)
                {
                    clasesCompradas = cantidadClases;
                    esOferta = false;
                }
            }; break;
        }

        try
        {
            SesionPagoDTO sesionPago = this.pagoStripeService.crearSesionPagoClasesPracticas( matricula.getPermisoCarnet(), alumno.getDni(), clasesCompradas, esOferta);

            // Almacenamos datos en sesión para enriquecer la experiencia tras el retorno
            sesion.setAttribute("pagoClases_dni", alumno.getDni());
            sesion.setAttribute("pagoClases_numero", clasesCompradas);
            sesion.setAttribute("pagoClases_importe", sesionPago.importeTotal() / 100.0f);
            sesion.setAttribute("pagoClases_tipoCarnet", matricula.getPermisoCarnet().getDescripcion());

            return "redirect:" + sesionPago.urlStripe();
        }
        catch (ReglaNegocioException ex)
        {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/alumno/pagos";
        }
    }

    /**
     * Inicia el proceso de pago para la renovación de matrícula al agotar convocatorias oficiales conectando con Stripe Checkout (Regla 7.2).
     */
    @PostMapping("/pagos/renovar-matricula")
    public String renovarMatricula(@AuthenticationPrincipal Object principal, RedirectAttributes redirectAttributes, HttpSession sesion)
    {
        Alumno alumno = this.alumnoService.obtenerAlumnoActual(principal);
        if (alumno == null)
        {
            return "redirect:/login";
        }

        Matricula matricula = this.academicoService.buscarMatriculaActiva(alumno).orElse(null);
        if (matricula == null)
        {
            redirectAttributes.addFlashAttribute("error", "No se encontró un expediente de matrícula activo para tramitar la renovación.");
            return "redirect:/alumno/pagos";
        }

        if (matricula.getConvocatorias() != null && matricula.getConvocatorias() > 0)
        {
            redirectAttributes.addFlashAttribute("error", "Aún dispones de convocatorias de examen vigentes (" + matricula.getConvocatorias() + " restantes). No es necesario renovar tu matrícula.");
            return "redirect:/alumno/pagos";
        }

        try
        {
            SesionPagoDTO sesionPago = this.pagoStripeService.crearSesionPagoRenovacionMatricula(matricula.getPermisoCarnet(), alumno.getDni());

            sesion.setAttribute("pagoRenovacion_dni", alumno.getDni());
            sesion.setAttribute("pagoRenovacion_importe", sesionPago.importeTotal() / 100.0f);
            sesion.setAttribute("pagoRenovacion_tipoCarnet", matricula.getPermisoCarnet().getDescripcion());

            return "redirect:" + sesionPago.urlStripe();
        }
        catch (ReglaNegocioException ex)
        {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/alumno/pagos";
        }
    }

    /**
     * Inicia el proceso de pago para matricularse en un nuevo permiso de conducir
     * para un alumno que ya no tiene una matrícula activa en vigor (Regla de negocio: 1 carnet activo simultáneo).
     */
    @PostMapping("/matricular")
    public String matricularNuevoCarnet(@RequestParam("tipoCarnet") TipoCarnet tipoCarnet, @AuthenticationPrincipal Object principal, RedirectAttributes redirectAttributes, HttpSession sesion)
    {
        Alumno alumno = this.alumnoService.obtenerAlumnoActual(principal);
        if (alumno == null)
        {
            return "redirect:/login";
        }

        if (tipoCarnet == null)
        {
            redirectAttributes.addFlashAttribute("error", "Debes seleccionar un tipo de permiso para formalizar la matrícula.");
            return "redirect:/alumno/dashboard";
        }

        Optional<Matricula> matriculaActiva = this.academicoService.buscarMatriculaActiva(alumno);
        if (matriculaActiva.isPresent())
        {
            redirectAttributes.addFlashAttribute("error", "Ya tienes una matrícula activa para el " +
                    matriculaActiva.get().getPermisoCarnet().getDescripcion() + ". No puedes cursar más de un permiso simultáneamente.");
            return "redirect:/alumno/dashboard";
        }

        try
        {
            SesionPagoDTO sesionPago = this.pagoStripeService.crearSesionPagoMatricula(tipoCarnet, alumno.getDni());

            // Almacenamos datos en sesión para la confirmación tras el retorno de Stripe
            sesion.setAttribute("dniAlumno", alumno.getDni());
            sesion.setAttribute("tipoCarnet", tipoCarnet.name());
            sesion.setAttribute("importeTotal", sesionPago.importeTotal() / 100.0f);

            return "redirect:" + sesionPago.urlStripe();
        }
        catch (ReglaNegocioException ex)
        {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/alumno/dashboard";
        }
    }

    // =========================================================================
    // 7. ESTADÍSTICAS Y PROGRESO DEL ALUMNO
    // =========================================================================
    @GetMapping("/estadisticas")
    public String mostrarEstadisticas(@AuthenticationPrincipal Object principal, @RequestHeader(value = "HX-Request", required = false) String hxRequest, Model model)
    {
        Alumno alumno = this.alumnoService.obtenerAlumnoActual(principal);
        if (alumno == null)
        {
            return "redirect:/login";
        }

        Matricula matricula = this.academicoService.buscarMatriculaActiva(alumno).orElse(null);
        EstadisticasAlumnoDTO stats = this.estadisticaService.obtenerEstadisticasAlumno(alumno);

        model.addAttribute("alumno", alumno);
        model.addAttribute("matricula", matricula);
        model.addAttribute("estadisticas", stats);
        model.addAttribute("esVehiculoPesado", stats.esVehiculoPesado());
        model.addAttribute("tarifaClase", stats.tarifaClase());
        model.addAttribute("totalMatriculas", stats.totalMatriculas());
        model.addAttribute("saldoClasesActual", stats.saldoClasesActual());
        model.addAttribute("totalClasesContratadas", stats.totalClasesContratadas());
        model.addAttribute("gastoClases", stats.gastoClases());

        if (hxRequest != null)
        {
            return "alumno/estadisticas :: contenedorEstadisticas";
        }

        return "alumno/estadisticas";
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
