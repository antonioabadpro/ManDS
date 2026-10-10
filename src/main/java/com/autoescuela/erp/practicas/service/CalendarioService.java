package com.autoescuela.erp.practicas.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autoescuela.erp.academico.model.Matricula;
import com.autoescuela.erp.academico.repository.MatriculaRepository;
import com.autoescuela.erp.core.enums.EstadoClase;
import com.autoescuela.erp.core.enums.EstadoVehiculo;
import com.autoescuela.erp.core.enums.TipoTurno;
import com.autoescuela.erp.examenes.model.Examen;
import com.autoescuela.erp.examenes.repository.ExamenRepository;
import com.autoescuela.erp.flota.model.Vehiculo;
import com.autoescuela.erp.practicas.dto.CalendarioSemanalDTO;
import com.autoescuela.erp.practicas.dto.CeldaCalendarioDTO;
import com.autoescuela.erp.practicas.dto.DiaCalendarioDTO;
import com.autoescuela.erp.practicas.dto.FilaCalendarioDTO;
import com.autoescuela.erp.practicas.dto.TramoHorarioDTO;
import com.autoescuela.erp.practicas.model.ClasePractica;
import com.autoescuela.erp.practicas.repository.ClasePracticaRepository;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.model.Profesor;

import lombok.AllArgsConstructor;

/**
 * Servicio encargado de ensamblar la cuadrícula semanal Server-Side Rendering (SSR)
 * para los calendarios de alumno y profesor, conforme a las directrices de AGENTS.md.
 */
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class CalendarioService
{
    private final ClasePracticaRepository clasePracticaRepository;
    private final ExamenRepository examenRepository;
    private final MatriculaRepository matriculaRepository;

    /**
     * Ensambla la estructura semanal completa para la agenda del profesor autenticado.
     */
    public CalendarioSemanalDTO obtenerCalendarioProfesor(Profesor profesor, LocalDate fechaReferencia)
    {
        LocalDate fechaBase = (fechaReferencia != null) ? fechaReferencia : LocalDate.now();
        LocalDate lunes = fechaBase.with(DayOfWeek.MONDAY);
        LocalDate viernes = lunes.plusDays(4);
        LocalDate hoy = LocalDate.now();

        TipoTurno turno = (profesor != null && profesor.getTurno() != null) ? profesor.getTurno() : TipoTurno.MATINAL;
        List<TramoHorarioDTO> tramos = this.generarTramos(turno);

        Set<LocalDate> diasConExamenDgt = this.obtenerDiasConExamenDgt(profesor);

        List<ClasePractica> clases = (profesor != null)
                ? this.clasePracticaRepository.findByProfesorOrderByFechaHoraAsc(profesor)
                : new ArrayList<>();

        List<DiaCalendarioDTO> dias = this.construirDiasSemana(lunes, diasConExamenDgt, hoy);

        List<FilaCalendarioDTO> filas = new ArrayList<>();
        for (TramoHorarioDTO tramo : tramos)
        {
            List<CeldaCalendarioDTO> celdas = new ArrayList<>();
            for (DiaCalendarioDTO dia : dias)
            {
                boolean esExamenDgt = diasConExamenDgt.contains(dia.fecha());
                celdas.add(this.construirCeldaProfesor(dia, tramo, esExamenDgt, clases));
            }
            filas.add(new FilaCalendarioDTO(tramo, celdas));
        }

        String rangoFechasTexto = this.formatearRangoFechas(lunes, viernes);
        LocalDate semanaAnterior = lunes.minusWeeks(1);
        LocalDate semanaSiguiente = lunes.plusWeeks(1);

        return new CalendarioSemanalDTO(
                lunes,
                viernes,
                rangoFechasTexto,
                semanaAnterior,
                semanaSiguiente,
                hoy,
                dias,
                filas,
                turno,
                profesor != null,
                true,
                null
        );
    }

    /**
     * Ensambla la estructura semanal completa para la agenda del alumno autenticado,
     * aplicando el filtro de privacidad de datos RGPD ("Clase Práctica") y control de reservas.
     */
    public CalendarioSemanalDTO obtenerCalendarioAlumno(Alumno alumno, LocalDate fechaReferencia)
    {
        LocalDate fechaBase = (fechaReferencia != null) ? fechaReferencia : LocalDate.now();
        LocalDate lunes = fechaBase.with(DayOfWeek.MONDAY);
        LocalDate viernes = lunes.plusDays(4);
        LocalDate hoy = LocalDate.now();

        Profesor profesor = (alumno != null) ? alumno.getProfesor() : null;
        boolean tieneProfesorAsignado = (profesor != null);
        TipoTurno turno = (profesor != null && profesor.getTurno() != null) ? profesor.getTurno() : TipoTurno.MATINAL;

        List<TramoHorarioDTO> tramos = this.generarTramos(turno);

        // Control de capacidad de reserva y estado del vehículo
        Matricula matricula = (alumno != null)
                ? this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno).orElse(null)
                : null;
        int saldoClases = (matricula != null && matricula.getSaldoClases() != null) ? matricula.getSaldoClases() : 0;
        int clasesPendientes = (alumno != null)
                ? this.clasePracticaRepository.countByAlumnoAndEstadoClase(alumno, EstadoClase.PENDIENTE)
                : 0;
        int capacidadReserva = saldoClases - clasesPendientes;

        Vehiculo vehiculo = (profesor != null) ? profesor.getVehiculo() : null;
        boolean vehiculoEnMantenimiento = (vehiculo != null && vehiculo.getEstado() == EstadoVehiculo.MANTENIMIENTO);

        boolean puedeReservar = tieneProfesorAsignado && !vehiculoEnMantenimiento && (capacidadReserva > 0);
        String motivoBloqueoReserva = this.obtenerMotivoBloqueoReserva(tieneProfesorAsignado, vehiculoEnMantenimiento, capacidadReserva);

        Set<LocalDate> diasConExamenDgt = this.obtenerDiasConExamenDgt(profesor);

        List<ClasePractica> clasesProfesor = (profesor != null)
                ? this.clasePracticaRepository.findByProfesorOrderByFechaHoraAsc(profesor)
                : new ArrayList<>();

        List<DiaCalendarioDTO> dias = this.construirDiasSemana(lunes, diasConExamenDgt, hoy);
        LocalDateTime ahora = LocalDateTime.now();

        List<FilaCalendarioDTO> filas = new ArrayList<>();
        for (TramoHorarioDTO tramo : tramos)
        {
            List<CeldaCalendarioDTO> celdas = new ArrayList<>();
            for (DiaCalendarioDTO dia : dias)
            {
                boolean esExamenDgt = diasConExamenDgt.contains(dia.fecha());
                celdas.add(this.construirCeldaAlumno(dia, tramo, esExamenDgt, clasesProfesor, alumno, puedeReservar, ahora));
            }
            filas.add(new FilaCalendarioDTO(tramo, celdas));
        }

        String rangoFechasTexto = this.formatearRangoFechas(lunes, viernes);
        LocalDate semanaAnterior = lunes.minusWeeks(1);
        LocalDate semanaSiguiente = lunes.plusWeeks(1);

        return new CalendarioSemanalDTO(
                lunes,
                viernes,
                rangoFechasTexto,
                semanaAnterior,
                semanaSiguiente,
                hoy,
                dias,
                filas,
                turno,
                tieneProfesorAsignado,
                puedeReservar,
                motivoBloqueoReserva
        );
    }

    /**
     * Resuelve y retorna los días que contienen convocatorias oficiales DGT para el profesor.
     */
    private Set<LocalDate> obtenerDiasConExamenDgt(Profesor profesor)
    {
        if (profesor == null)
        {
            return new HashSet<>();
        }

        List<Examen> examenesDgt = this.examenRepository.findByAlumnoProfesorOrderByFechaHoraDesc(profesor);
        Set<LocalDate> diasConExamenDgt = new HashSet<>();
        for (Examen examen : examenesDgt)
        {
            if (examen.getFechaHora() != null)
            {
                diasConExamenDgt.add(examen.getFechaHora().toLocalDate());
            }
        }
        return diasConExamenDgt;
    }

    /**
     * Construye la celda individual para la cuadrícula del profesor.
     */
    private CeldaCalendarioDTO construirCeldaProfesor(DiaCalendarioDTO dia, TramoHorarioDTO tramo, boolean esExamenDgt, List<ClasePractica> clases)
    {
        LocalDateTime fechaHora = dia.fecha().atTime(tramo.horaInicio());

        if (esExamenDgt)
        {
            return CeldaCalendarioDTO.deExamenDgt(dia.fecha(), tramo.horaInicio(), tramo.horaFin(), fechaHora, "Convocatoria Oficial DGT");
        }

        ClasePractica claseEnSlot = this.buscarClaseEnSlot(clases, dia.fecha(), tramo.horaInicio());
        if (claseEnSlot != null)
        {
            String nombreAlumno = (claseEnSlot.getAlumno() != null)
                    ? claseEnSlot.getAlumno().getNombre() + " " + claseEnSlot.getAlumno().getApellidos()
                    : "Alumno";
            String subtitulo = this.obtenerSubtituloEstado(claseEnSlot.getEstadoClase());

            return CeldaCalendarioDTO.deOcupadaProfesor(
                    dia.fecha(),
                    tramo.horaInicio(),
                    tramo.horaFin(),
                    fechaHora,
                    claseEnSlot.getId(),
                    claseEnSlot.getEstadoClase().name(),
                    nombreAlumno,
                    subtitulo
            );
        }

        return CeldaCalendarioDTO.deLibreProfesor(dia.fecha(), tramo.horaInicio(), tramo.horaFin(), fechaHora);
    }

    /**
     * Construye la celda individual para la cuadrícula del alumno aplicando RGPD y validación de reserva.
     */
    private CeldaCalendarioDTO construirCeldaAlumno(DiaCalendarioDTO dia, TramoHorarioDTO tramo, boolean esExamenDgt, List<ClasePractica> clasesProfesor, Alumno alumno, boolean puedeReservar, LocalDateTime ahora)
    {
        LocalDateTime fechaHora = dia.fecha().atTime(tramo.horaInicio());

        if (esExamenDgt)
        {
            return CeldaCalendarioDTO.deExamenDgt(dia.fecha(), tramo.horaInicio(), tramo.horaFin(), fechaHora, "Convocatoria Oficial DGT (Bloqueado)");
        }

        ClasePractica claseEnSlot = this.buscarClaseEnSlot(clasesProfesor, dia.fecha(), tramo.horaInicio());
        if (claseEnSlot != null)
        {
            boolean esPropia = (alumno != null && claseEnSlot.getAlumno() != null
                    && claseEnSlot.getAlumno().getId().equals(alumno.getId()));
            String subtitulo = this.obtenerSubtituloEstado(claseEnSlot.getEstadoClase());

            if (esPropia)
            {
                return CeldaCalendarioDTO.dePropiaAlumno(
                        dia.fecha(),
                        tramo.horaInicio(),
                        tramo.horaFin(),
                        fechaHora,
                        claseEnSlot.getId(),
                        claseEnSlot.getEstadoClase().name(),
                        subtitulo
                );
            }

            return CeldaCalendarioDTO.deOcupadaAjenaAlumno(dia.fecha(), tramo.horaInicio(), tramo.horaFin(), fechaHora);
        }

        return CeldaCalendarioDTO.deLibreAlumno(dia.fecha(), tramo.horaInicio(), tramo.horaFin(), fechaHora, puedeReservar, ahora);
    }

    /**
     * Determina el mensaje explicativo en caso de bloqueo para realizar reservas.
     */
    private String obtenerMotivoBloqueoReserva(boolean tieneProfesorAsignado, boolean vehiculoEnMantenimiento, int capacidadReserva)
    {
        if (!tieneProfesorAsignado)
        {
            return "No tienes profesor asignado actualmente.";
        }
        if (vehiculoEnMantenimiento)
        {
            return "El vehículo de tu profesor está en mantenimiento.";
        }
        if (capacidadReserva <= 0)
        {
            return "No dispones de capacidad de reserva (saldo de clases agotado o reservado).";
        }
        return null;
    }

    /**
     * Genera los 9 tramos fijos de 45 minutos delimitados por el turno del profesor.
     */
    private List<TramoHorarioDTO> generarTramos(TipoTurno turno)
    {
        List<TramoHorarioDTO> tramos = new ArrayList<>();
        LocalTime horaBase = (turno == TipoTurno.TARDE) ? LocalTime.of(15, 0) : LocalTime.of(8, 0);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        for (int i = 0; i < 9; i++)
        {
            LocalTime inicio = horaBase.plusMinutes(i * 45L);
            LocalTime fin = inicio.plusMinutes(45L);
            String etiqueta = inicio.format(timeFormatter);
            String etiquetaCompleta = etiqueta + " - " + fin.format(timeFormatter);
            tramos.add(new TramoHorarioDTO(inicio, fin, etiqueta, etiquetaCompleta));
        }
        return tramos;
    }

    /**
     * Construye la lista de los 5 días hábiles de la semana (Lunes a Viernes).
     */
    private List<DiaCalendarioDTO> construirDiasSemana(LocalDate lunes, Set<LocalDate> diasConExamenDgt, LocalDate hoy)
    {
        List<DiaCalendarioDTO> dias = new ArrayList<>();
        String[] nombresDias = {"lun", "mar", "mié", "jue", "vie"};

        for (int i = 0; i < 5; i++)
        {
            LocalDate fecha = lunes.plusDays(i);
            String nombre = nombresDias[i];
            String fechaFormateada = fecha.getDayOfMonth() + "/" + fecha.getMonthValue();
            String encabezadoCompleto = nombre + " " + fechaFormateada;
            boolean esHoy = fecha.equals(hoy);
            boolean esExamenDgt = diasConExamenDgt.contains(fecha);
            String examenDgtTexto = esExamenDgt ? "Jornada Examen Oficial DGT" : null;

            dias.add(new DiaCalendarioDTO(
                    fecha,
                    nombre,
                    fechaFormateada,
                    encabezadoCompleto,
                    esHoy,
                    esExamenDgt,
                    examenDgtTexto
            ));
        }
        return dias;
    }

    /**
     * Localiza la clase práctica asignada a un tramo concreto de fecha y hora.
     */
    private ClasePractica buscarClaseEnSlot(List<ClasePractica> clases, LocalDate fecha, LocalTime horaInicio)
    {
        for (ClasePractica clase : clases)
        {
            if (clase.getFechaHora() != null)
            {
                LocalDate fechaClase = clase.getFechaHora().toLocalDate();
                LocalTime horaClase = clase.getFechaHora().toLocalTime();

                if (fechaClase.equals(fecha) && horaClase.getHour() == horaInicio.getHour()
                        && horaClase.getMinute() == horaInicio.getMinute())
                {
                    return clase;
                }
            }
        }
        return null;
    }

    /**
     * Traduce el estado de la clase a un subtítulo legible en español.
     */
    private String obtenerSubtituloEstado(EstadoClase estado)
    {
        if (estado == EstadoClase.RECIBIDA)
        {
            return "Clase Recibida";
        }
        else
        {
            if (estado == EstadoClase.CANCELADA)
            {
                return "Clase Cancelada";
            }
        }
        return "Clase Pendiente";
    }

    /**
     * Formatea el rango visible central de la semana (ej. "5 – 9 oct 2026").
     * Tiene en cuenta la posibilidad de que en la misma semana (lunes - viernes) incluyan fechas de meses o años distintos.
     */
    private String formatearRangoFechas(LocalDate lunes, LocalDate viernes)
    {
        Locale esLocale = Locale.of("es", "ES");
        DateTimeFormatter mesFormat = DateTimeFormatter.ofPattern("MMM", esLocale);
        String mesLunes = lunes.format(mesFormat).replace(".", "");
        String mesViernes = viernes.format(mesFormat).replace(".", "");

        // Si el lunes y viernes son del mismo año, se muestra "5 – 9 oct 2026"
        if (lunes.getYear() == viernes.getYear())
        {
            // Si el lunes y viernes son del mismo mes, se muestra "5 – 9 oct 2026"
            if (lunes.getMonth() == viernes.getMonth())
            {
                return lunes.getDayOfMonth() + " – " + viernes.getDayOfMonth() + " " + mesViernes + " " + lunes.getYear();
            }
            else // Si el lunes y viernes son de meses distintos, se muestra "30 sep – 4 oct 2026"
            {
                return lunes.getDayOfMonth() + " " + mesLunes + " – " + viernes.getDayOfMonth() + " " + mesViernes + " " + lunes.getYear();
            }
        }
        else // Si el lunes y viernes son de años distintos, se muestra "30 dic 2026 – 4 ene 2027"
        {
            return lunes.getDayOfMonth() + " " + mesLunes + " " + lunes.getYear() + " – " + viernes.getDayOfMonth() + " " + mesViernes + " " + viernes.getYear();
        }
    }
}
