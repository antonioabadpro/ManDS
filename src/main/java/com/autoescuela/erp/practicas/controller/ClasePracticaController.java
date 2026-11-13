package com.autoescuela.erp.practicas.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.autoescuela.erp.academico.model.Matricula;
import com.autoescuela.erp.academico.service.AcademicoService;
import com.autoescuela.erp.core.enums.EstadoClase;
import com.autoescuela.erp.core.enums.EstadoVehiculo;
import com.autoescuela.erp.core.enums.ModalidadMatricula;
import com.autoescuela.erp.core.enums.TipoTurno;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.examenes.service.ExamenService;
import com.autoescuela.erp.practicas.dto.ClasePracticaAdminDTO;
import com.autoescuela.erp.practicas.dto.CompletarClaseDTO;
import com.autoescuela.erp.practicas.dto.ReservaClasePracticaDTO;
import com.autoescuela.erp.practicas.model.ClasePractica;
import com.autoescuela.erp.practicas.service.ClasePracticaService;
import com.autoescuela.erp.practicas.util.FestivosCalendarioUtil;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.model.Profesor;
import com.autoescuela.erp.usuarios.service.AlumnoService;
import com.autoescuela.erp.usuarios.service.ProfesorService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador de dominio unificado para la gestión y ejecución de clases prácticas.
 * Atiende las peticiones de Administrador, Alumno y Profesor bajo Package-by-Features.
 */
@Controller
@RequiredArgsConstructor
public class ClasePracticaController
{
    private final ClasePracticaService clasePracticaService;
    private final ProfesorService profesorService;
    private final AlumnoService alumnoService;
    private final AcademicoService academicoService;
    private final ExamenService examenService;

    // =========================================================================
    // 1. ENDPOINTS DE ADMINISTRADOR (/admin/practicas)
    // =========================================================================

    /**
     * Muestra la agenda global de prácticas de profesores y vehículos.
     */
    @GetMapping("/admin/practicas")
    public String mostrarPracticas()
    {
        return "admin/practicas";
    }

    /**
     * Endpoint HTMX para buscar y filtrar clases prácticas por profesor y/o estado.
     */
    @GetMapping("/admin/practicas/buscar")
    public String buscarPracticas(
            @RequestParam(name = "profesorId", required = false) Long profesorId,
            @RequestParam(name = "estadoClase", required = false) EstadoClase estadoClase,
            Model model)
    {
        List<ClasePracticaAdminDTO> clases = this.clasePracticaService.obtenerClasesParaAdmin(profesorId, estadoClase);
        model.addAttribute("clases", clases);
        model.addAttribute("totalClases", clases.size());
        model.addAttribute("busquedaRealizada", true);
        return "admin/fragments/tabla-practicas :: tabla-practicas-fragment";
    }

    /**
     * Endpoint HTMX para cargar el modal con el resumen informativo detallado de una clase práctica.
     */
    @GetMapping("/admin/practicas/detalle/{id}")
    public String cargarModalDetalleClaseAdmin(@PathVariable("id") Long id, Model model)
    {
        ClasePracticaAdminDTO clase = this.clasePracticaService.obtenerClaseParaDetalleAdmin(id);
        model.addAttribute("clase", clase);
        model.addAttribute("abrirModalDetalleClase", true);
        return "admin/fragments/modal-detalle-clase-admin :: modal-detalle-clase-admin";
    }

    // =========================================================================
    // 2. ENDPOINTS DE ALUMNO (/alumno/clases)
    // =========================================================================

    /**
     * Devuelve el fragmento modal Thymeleaf con el formulario HTMX para reservar clase práctica.
     */
    @GetMapping("/alumno/clases/reservar-modal")
    public String obtenerModalReservarClase(
            @RequestParam(value = "fecha", required = false) String fecha,
            @AuthenticationPrincipal Object principal,
            Model model)
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
    @GetMapping("/alumno/clases/{id}/modal")
    public String obtenerModalDetalleClase(@PathVariable("id") Long id, @AuthenticationPrincipal Object principal, Model model)
    {
        return this.cargarModalClaseAlumno(id, principal, model, "alumno/fragments/modal-detalle-clase :: modalDetalleClase");
    }

    /**
     * Devuelve el fragmento modal Thymeleaf con la doble confirmación para cancelar una clase práctica.
     */
    @GetMapping("/alumno/clases/{id}/cancelar-modal")
    public String obtenerModalConfirmarCancelarClase(@PathVariable("id") Long id, @AuthenticationPrincipal Object principal, Model model)
    {
        return this.cargarModalClaseAlumno(id, principal, model, "alumno/fragments/modal-confirmar-cancelar-clase :: modalConfirmarCancelarClase");
    }

    /**
     * Procesa la reserva transaccional de una clase práctica (CU-034).
     */
    @PostMapping("/alumno/clases/reservar")
    @Transactional
    public String reservarClasePractica(
            @AuthenticationPrincipal Object principal,
            @Valid @ModelAttribute("reservaDTO") ReservaClasePracticaDTO dto,
            BindingResult bindingResult,
            @RequestHeader(value = "HX-Request", required = false) String hxRequest,
            HttpServletResponse response,
            Model model,
            RedirectAttributes redirectAttributes)
    {
        String msjError = null;

        Alumno alumno = this.alumnoService.obtenerAlumnoActual(principal);
        if (alumno == null)
        {
            return "redirect:/login";
        }

        Matricula matricula = this.academicoService.buscarMatriculaActiva(alumno).orElse(null);
        if (matricula == null || matricula.tieneConvocatoriasAgotadas())
        {
            msjError = "Has agotado las convocatorias de tu matrícula. Debes tramitar la renovación antes de reservar clases prácticas.";
            return this.responderErrorAlumnoCalendario(msjError, hxRequest, false, response, model, redirectAttributes);
        }
        if (matricula.getModalidad() == ModalidadMatricula.TEORICO_PRACTICA
                && !this.examenService.tieneTeoricoAprobado(alumno))
        {
            msjError = "No puedes reservar clases prácticas hasta haber aprobado el examen teórico oficial ante la DGT.";
            return this.responderErrorAlumnoCalendario(msjError, hxRequest, false, response, model, redirectAttributes);
        }

        int capacidad = this.calcularCapacidadReserva(alumno, matricula);
        if (capacidad <= 0)
        {
            msjError = "No tienes capacidad de reserva disponible. Has agotado tu saldo de clases o tienes reservas pendientes de cursar.";
            return this.responderErrorAlumnoCalendario(msjError, hxRequest, false, response, model, redirectAttributes);
        }

        Profesor profesor = alumno.getProfesor();
        if (profesor == null)
        {
            msjError = "Aún no tienes un profesor asignado por la autoescuela para reservar clases.";
            return this.responderErrorAlumnoCalendario(msjError, hxRequest, false, response, model, redirectAttributes);
        }

        if (profesor.getVehiculo() != null && profesor.getVehiculo().getEstado() == EstadoVehiculo.MANTENIMIENTO)
        {
            msjError = "El vehículo de tu profesor se encuentra actualmente en mantenimiento técnico. No es posible reservar clases prácticas hasta que finalice su reparación.";
            return this.responderErrorAlumnoCalendario(msjError, hxRequest, false, response, model, redirectAttributes);
        }

        if (bindingResult.hasErrors() || dto.fechaHora() == null)
        {
            msjError = "Los datos de fecha, hora o punto de recogida no son válidos.";
            return this.responderErrorAlumnoCalendario(msjError, hxRequest, false, response, model, redirectAttributes);
        }

        if (FestivosCalendarioUtil.esDiaNoLaborable(dto.fechaHora().toLocalDate()))
        {
            String motivo = FestivosCalendarioUtil.obtenerMotivoNoLaborable(dto.fechaHora().toLocalDate());
            msjError = "No es posible reservar clases prácticas en días festivos nacionales o periodos vacacionales (" + motivo + ").";
            return this.responderErrorAlumnoCalendario(msjError, hxRequest, false, response, model, redirectAttributes);
        }

        LocalTime hora = dto.fechaHora().toLocalTime();
        if (profesor.getTurno() == TipoTurno.MATINAL && (hora.isBefore(LocalTime.of(8, 0)) || hora.isAfter(LocalTime.of(15, 0))))
        {
            msjError = "El profesor imparte turno MATINAL (08:00 a 15:00). Selecciona un tramo dentro de ese intervalo.";
            return this.responderErrorAlumnoCalendario(msjError, hxRequest, false, response, model, redirectAttributes);
        }
        else if (profesor.getTurno() == TipoTurno.TARDE && (hora.isBefore(LocalTime.of(15, 0)) || hora.isAfter(LocalTime.of(22, 0))))
        {
            msjError = "El profesor imparte turno TARDE (15:00 a 22:00). Selecciona un tramo dentro de ese intervalo.";
            return this.responderErrorAlumnoCalendario(msjError, hxRequest, false, response, model, redirectAttributes);
        }

        try
        {
            this.clasePracticaService.reservarClase(alumno, profesor, dto);
        }
        catch (ReglaNegocioException ex)
        {
            msjError = ex.getMessage();
            return this.responderErrorAlumnoCalendario(msjError, hxRequest, true, response, model, redirectAttributes);
        }

        String mensajeExito = "¡Clase práctica reservada correctamente para el " +
                dto.fechaHora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "! Consulta tu agenda.";
        return this.responderExitoAlumnoCalendario(alumno, matricula, mensajeExito, hxRequest, response, model, redirectAttributes);
    }

    /**
     * Procesa la cancelación de una clase práctica en estado PENDIENTE por parte del alumno.
     */
    @PostMapping("/alumno/clases/cancelar")
    @Transactional
    public String cancelarClasePractica(
            @RequestParam("claseId") Long claseId,
            @AuthenticationPrincipal Object principal,
            @RequestHeader(value = "HX-Request", required = false) String hxRequest,
            HttpServletResponse response,
            Model model,
            RedirectAttributes redirectAttributes)
    {
        Alumno alumno = this.alumnoService.obtenerAlumnoActual(principal);
        if (alumno == null)
        {
            return "redirect:/login";
        }

        Optional<ClasePractica> claseOptional = this.clasePracticaService.buscarPorId(claseId);
        if (claseOptional.isEmpty())
        {
            return this.responderErrorAlumnoCalendario(
                    "No se ha encontrado la clase práctica solicitada.",
                    hxRequest, false, response, model, redirectAttributes);
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
            return this.responderErrorAlumnoCalendario(ex.getMessage(), hxRequest, true, response, model, redirectAttributes);
        }

        Matricula matricula = this.academicoService.buscarMatriculaActiva(alumno).orElse(null);
        String mensajeExito = "Reserva de clase práctica cancelada con éxito. Has liberado tu capacidad de reserva.";
        return this.responderExitoAlumnoCalendario(alumno, matricula, mensajeExito, hxRequest, response, model, redirectAttributes);
    }

    /**
     * Muestra el historial completo de clases prácticas del alumno.
     */
    @GetMapping("/alumno/clases")
    public String mostrarHistorialClases(
            @AuthenticationPrincipal Object principal,
            @RequestHeader(value = "HX-Request", required = false) String hxRequest,
            Model model)
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
    // 3. ENDPOINTS DE PROFESOR (/profesor/clases)
    // =========================================================================

    /**
     * Endpoint HTMX para cargar los datos de una clase práctica en el modal del profesor.
     */
    @GetMapping("/profesor/clases/{id}/modal")
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
        Long kmActualVehiculo = (clase.getProfesor() != null && clase.getProfesor().getVehiculo() != null && clase.getProfesor().getVehiculo().getKm() != null)
                ? clase.getProfesor().getVehiculo().getKm()
                : 0L;

        model.addAttribute("kmActualVehiculo", kmActualVehiculo);
        model.addAttribute("clase", clase);

        return "profesor/fragments/modal-ficha-clase :: modalFichaClase";
    }

    /**
     * Procesa la cumplimentación de la ficha técnica de clase práctica (CU-024).
     */
    @PostMapping("/profesor/clases/completar")
    public String completarClasePractica(
            @AuthenticationPrincipal Object principal,
            @Valid @ModelAttribute("completarClaseDTO") CompletarClaseDTO dto,
            BindingResult bindingResult,
            @RequestHeader(value = "HX-Request", required = false) String hxRequest,
            HttpServletResponse response,
            Model model,
            RedirectAttributes redirectAttributes)
    {
        String mensaje = null;
        Optional<ClasePractica> claseOptional = this.clasePracticaService.buscarPorId(dto.claseId());

        if (claseOptional.isEmpty())
        {
            mensaje = "No se encontró la clase práctica solicitada.";
            return this.responderFeedbackProfesorCalendario(
                    "error", mensaje, false, hxRequest, response, model, redirectAttributes);
        }

        ClasePractica clase = claseOptional.get();

        if (bindingResult.hasErrors())
        {
            mensaje = "Error en los datos de kilometraje u observaciones de la clase.";
            return this.responderErrorFichaClase(clase, dto, mensaje, hxRequest, model, redirectAttributes);
        }

        try
        {
            this.clasePracticaService.completarClasePractica(dto);
        }
        catch (ReglaNegocioException ex)
        {
            return this.responderErrorFichaClase(clase, dto, ex.getMessage(), hxRequest, model, redirectAttributes);
        }

        mensaje = "Ficha técnica de la clase registrada correctamente. Se ha descontado 1 unidad de saldo al alumno.";
        return this.responderFeedbackProfesorCalendario("mensajeExito", mensaje, true, hxRequest, response, model, redirectAttributes);
    }

    /**
     * Cancela una clase práctica reservada notificando al alumno por correo (CU-023).
     */
    @PostMapping("/profesor/clases/cancelar")
    public String cancelarClasePracticaProfesor(
            @AuthenticationPrincipal Object principal,
            @RequestParam("claseId") Long claseId,
            @RequestParam(value = "motivo", required = false) String motivo,
            @RequestHeader(value = "HX-Request", required = false) String hxRequest,
            HttpServletResponse response,
            Model model,
            RedirectAttributes redirectAttributes)
    {
        try
        {
            this.clasePracticaService.cancelarClasePorProfesor(claseId, motivo);
        }
        catch (ReglaNegocioException ex)
        {
            return this.responderFeedbackProfesorCalendario("error", ex.getMessage(), false, hxRequest, response, model, redirectAttributes);
        }

        String mensajeExito = "Clase práctica cancelada correctamente y alumno notificado por correo.";
        return this.responderFeedbackProfesorCalendario("mensajeExito", mensajeExito, true, hxRequest, response, model, redirectAttributes);
    }

    // =========================================================================
    // MÉTODOS AUXILIARES
    // =========================================================================

    private String cargarModalClaseAlumno(Long id, Object principal, Model model, String vistaDestino)
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
        return vistaDestino;
    }

    private String responderErrorAlumnoCalendario(String error, String hxRequest,
            boolean dispararActualizacion, HttpServletResponse response, Model model, RedirectAttributes redirectAttributes)
    {
        if (hxRequest != null)
        {
            if (dispararActualizacion)
            {
                response.setHeader("HX-Trigger", "actualizarCalendario");
            }
            model.addAttribute("error", error);
            return "alumno/fragments/alerta-feedback :: feedbackExito";
        }
        redirectAttributes.addFlashAttribute("error", error);
        return "redirect:/alumno/calendario";
    }

    private String responderExitoAlumnoCalendario(Alumno alumno, Matricula matricula, String mensajeExito, String hxRequest, HttpServletResponse response, Model model, RedirectAttributes redirectAttributes)
    {
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

    private String responderErrorFichaClase(ClasePractica clase, CompletarClaseDTO dto, String error, String hxRequest, Model model, RedirectAttributes redirectAttributes)
    {
        if (hxRequest != null)
        {
            Long kmActualVehiculo = (clase.getProfesor() != null && clase.getProfesor().getVehiculo() != null && clase.getProfesor().getVehiculo().getKm() != null)
                    ? clase.getProfesor().getVehiculo().getKm()
                    : (dto.kmInicio() != null ? dto.kmInicio() : 0L);
            model.addAttribute("kmActualVehiculo", kmActualVehiculo);
            model.addAttribute("clase", clase);
            model.addAttribute("error", error);
            return "profesor/fragments/modal-ficha-clase :: modalFichaClase";
        }
        redirectAttributes.addFlashAttribute("error", error);
        return "redirect:/profesor/calendario";
    }

    private String responderFeedbackProfesorCalendario(String claveModelo, String mensaje, boolean dispararActualizacion, String hxRequest, HttpServletResponse response, Model model, RedirectAttributes redirectAttributes)
    {
        if (hxRequest != null)
        {
            if (dispararActualizacion)
            {
                response.setHeader("HX-Trigger", "actualizarCalendario");
            }
            model.addAttribute(claveModelo, mensaje);
            return "profesor/fragments/alerta-feedback :: feedbackExito";
        }

        redirectAttributes.addFlashAttribute(claveModelo, mensaje);
        return "redirect:/profesor/calendario";
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
}
