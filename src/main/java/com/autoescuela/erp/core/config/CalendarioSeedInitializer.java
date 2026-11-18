package com.autoescuela.erp.core.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.autoescuela.erp.core.enums.EstadoClase;
import com.autoescuela.erp.core.enums.TipoExamen;
import com.autoescuela.erp.core.util.FechaHoraUtil;
import com.autoescuela.erp.examenes.model.Examen;
import com.autoescuela.erp.examenes.repository.ExamenRepository;
import com.autoescuela.erp.practicas.model.ClasePractica;
import com.autoescuela.erp.practicas.repository.ClasePracticaRepository;
import com.autoescuela.erp.usuarios.model.Profesor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Inicializador y post-procesador de datos de arranque encargado de garantizar que las fechas
 * de clases prácticas pendientes y exámenes oficiales no coincidan en días inhábiles
 * (fines de semana, festivos nacionales, periodos vacacionales o jornadas reservadas para examen práctico DGT).
 */
@Slf4j
@Component
@Order(20)
@RequiredArgsConstructor
public class CalendarioSeedInitializer implements ApplicationRunner
{
    private final ClasePracticaRepository clasePracticaRepository;
    private final ExamenRepository examenRepository;

    @Value("${app.seed.adjust-calendar-dates:true}")
    private boolean activado;

    @Override
    @Transactional
    public void run(ApplicationArguments args)
    {
        if (!this.activado)
        {
            log.debug("Ajuste automático de fechas lectivas desactivado por configuración.");
            return;
        }

        log.info("Iniciando auditoría y ajuste dinámico de fechas lectivas en datos iniciales...");
        this.ajustarExamenesOficiales();
        this.ajustarClasesPracticasPendientes();
        log.info("Ajuste dinámico de fechas lectivas completado exitosamente.");
    }

    /**
     * Reubica los exámenes oficiales futuros que hayan caído en días inhábiles hacia el siguiente día hábil disponible.
     */
    public void ajustarExamenesOficiales()
    {
        List<Examen> todosExamenes = this.examenRepository.findAll();
        for (Examen examen : todosExamenes)
        {
            if (examen.getFechaHora() != null)
            {
                LocalDate fechaExamen = examen.getFechaHora().toLocalDate();
                LocalTime horaExamen = examen.getFechaHora().toLocalTime();

                if (this.esDiaInhabil(fechaExamen))
                {
                    LocalDate nuevaFecha = this.obtenerSiguienteDiaLectivo(fechaExamen.plusDays(1), Collections.emptySet());
                    LocalDateTime nuevaFechaHora = nuevaFecha.atTime(horaExamen);
                    examen.setFechaHora(nuevaFechaHora);
                    if (examen.getSolicitudExamen() != null)
                    {
                        examen.getSolicitudExamen().setFechaHora(nuevaFechaHora);
                    }
                    this.examenRepository.save(examen);
                    log.info("Examen oficial ID {} reubicado de {} al día lectivo {}", examen.getId(), fechaExamen, nuevaFecha);
                }
            }
        }
    }

    /**
     * Ajusta las clases prácticas en estado PENDIENTE:
     * - Si la fecha es anterior a la actual, se ubica como máximo en el último día lectivo anterior.
     * - Si la fecha cae en día inhábil o con examen práctico oficial del profesor, se reubica a partir del día siguiente.
     */
    public void ajustarClasesPracticasPendientes()
    {
        Map<Long, Set<LocalDate>> diasExamenPorProfesor = this.obtenerDiasExamenPorProfesor();
        List<ClasePractica> clasesPendientes = this.clasePracticaRepository.findByEstadoClaseOrderByFechaHoraAsc(EstadoClase.PENDIENTE);
        LocalDate hoy = FechaHoraUtil.hoy();
        LocalDateTime ahora = FechaHoraUtil.ahora();

        for (ClasePractica clase : clasesPendientes)
        {
            if (clase.getFechaHora() != null)
            {
                Profesor profesor = clase.getProfesor();
                Long profesorId = (profesor != null) ? profesor.getId() : null;
                Set<LocalDate> diasExamen = (profesorId != null)
                        ? diasExamenPorProfesor.getOrDefault(profesorId, Collections.emptySet())
                        : Collections.emptySet();

                LocalDate fechaClase = clase.getFechaHora().toLocalDate();
                LocalTime horaClase = clase.getFechaHora().toLocalTime();

                if (clase.getFechaHora().isBefore(ahora))
                {
                    // Caso clase pendiente con fecha pasada: reubicar como máximo en el último día lectivo anterior
                    LocalDate ultimoDiaLectivo = this.obtenerUltimoDiaLectivoAnterior(hoy, diasExamen);
                    if (fechaClase.isBefore(ultimoDiaLectivo) || this.esDiaInhabilOConExamen(fechaClase, diasExamen))
                    {
                        LocalDate candidata = ultimoDiaLectivo;
                        while (this.existeColision(profesor, candidata, horaClase, clase.getId()))
                        {
                            candidata = this.obtenerUltimoDiaLectivoAnterior(candidata, diasExamen);
                        }
                        LocalDateTime nuevaFechaHora = candidata.atTime(horaClase);
                        clase.setFechaHora(nuevaFechaHora);
                        this.clasePracticaRepository.save(clase);
                        log.info("Clase pendiente pasada ID {} ajustada al último día lectivo hábil: {}", clase.getId(), nuevaFechaHora);
                    }
                }
                else
                {
                    // Caso clase pendiente futura o presente: si cae en día inhábil o con examen práctico, reubicar al día siguiente
                    if (this.esDiaInhabilOConExamen(fechaClase, diasExamen))
                    {
                        LocalDate inicioBusqueda = fechaClase.plusDays(1);
                        LocalDate candidata = this.obtenerSiguienteDiaLectivo(inicioBusqueda, diasExamen);
                        while (this.existeColision(profesor, candidata, horaClase, clase.getId()))
                        {
                            candidata = this.obtenerSiguienteDiaLectivo(candidata.plusDays(1), diasExamen);
                        }
                        LocalDateTime nuevaFechaHora = candidata.atTime(horaClase);
                        clase.setFechaHora(nuevaFechaHora);
                        this.clasePracticaRepository.save(clase);
                        log.info("Clase pendiente ID {} reubicada al día lectivo hábil: {}", clase.getId(), nuevaFechaHora);
                    }
                }
            }
        }
    }

    /**
     * Calcula el siguiente día lectivo hábil a partir de una fecha de inicio, delegando en {@link FechaHoraUtil}.
     */
    public LocalDate obtenerSiguienteDiaLectivo(LocalDate inicio, Set<LocalDate> diasExamenProfesor)
    {
        return FechaHoraUtil.obtenerSiguienteDiaLectivo(inicio, diasExamenProfesor);
    }

    /**
     * Calcula el último día lectivo hábil inmediatamente anterior a la fecha de referencia, delegando en {@link FechaHoraUtil}.
     */
    public LocalDate obtenerUltimoDiaLectivoAnterior(LocalDate inicio, Set<LocalDate> diasExamenProfesor)
    {
        return FechaHoraUtil.obtenerUltimoDiaLectivoAnterior(inicio, diasExamenProfesor);
    }

    /**
     * Evalúa si una fecha corresponde a un día inhábil (fin de semana, festivo o periodo vacacional), delegando en {@link FechaHoraUtil}.
     */
    public boolean esDiaInhabil(LocalDate fecha)
    {
        return FechaHoraUtil.esDiaInhabil(fecha);
    }

    /**
     * Evalúa si una fecha es inhábil o coincide con un examen práctico DGT reservado para el profesor, delegando en {@link FechaHoraUtil}.
     */
    public boolean esDiaInhabilOConExamen(LocalDate fecha, Set<LocalDate> diasExamenProfesor)
    {
        return FechaHoraUtil.esDiaInhabilOConExamen(fecha, diasExamenProfesor);
    }

    /**
     * Verifica si el profesor ya tiene otra clase activa asignada en la misma fecha y hora.
     */
    private boolean existeColision(Profesor profesor, LocalDate fecha, LocalTime hora, Long excludeClaseId)
    {
        if (profesor == null)
        {
            return false;
        }
        LocalDateTime fechaHora = fecha.atTime(hora);
        List<ClasePractica> clasesProfesor = this.clasePracticaRepository.findByProfesorOrderByFechaHoraAsc(profesor);
        for (ClasePractica c : clasesProfesor)
        {
            if (c.getId() != null && !c.getId().equals(excludeClaseId))
            {
                if (c.getEstadoClase() != EstadoClase.CANCELADA && fechaHora.equals(c.getFechaHora()))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Ensambla el mapa de días ocupados por exámenes prácticos oficiales DGT por identificador de profesor.
     */
    private Map<Long, Set<LocalDate>> obtenerDiasExamenPorProfesor()
    {
        Map<Long, Set<LocalDate>> diasExamenPorProfesor = new HashMap<>();
        List<Examen> examenesPracticos = this.examenRepository.findByTipoOrderByFechaHoraDesc(TipoExamen.PRACTICO);
        for (Examen examen : examenesPracticos)
        {
            if (examen.getFechaHora() != null)
            {
                Long profesorId = null;
                if (examen.getSolicitudExamen() != null && examen.getSolicitudExamen().getProfesor() != null)
                {
                    profesorId = examen.getSolicitudExamen().getProfesor().getId();
                }
                else if (examen.getAlumno() != null && examen.getAlumno().getProfesor() != null)
                {
                    profesorId = examen.getAlumno().getProfesor().getId();
                }

                if (profesorId != null)
                {
                    diasExamenPorProfesor.computeIfAbsent(profesorId, k -> new HashSet<>()).add(examen.getFechaHora().toLocalDate());
                }
            }
        }
        return diasExamenPorProfesor;
    }
}
