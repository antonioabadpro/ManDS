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

        // Consultar exámenes oficiales del profesor para detectar jornadas DGT completas
        List<Examen> examenesDgt = (profesor != null)
                ? this.examenRepository.findByAlumnoProfesorOrderByFechaHoraDesc(profesor)
                : new ArrayList<>();

        Set<LocalDate> diasConExamenDgt = new HashSet<>();
        for (Examen examen : examenesDgt)
        {
            if (examen.getFechaHora() != null)
            {
                diasConExamenDgt.add(examen.getFechaHora().toLocalDate());
            }
        }

        // Consultar clases prácticas del profesor
        List<ClasePractica> clases = (profesor != null)
                ? this.clasePracticaRepository.findByProfesorOrderByFechaHoraAsc(profesor)
                : new ArrayList<>();

        // Construir los 5 días de lunes a viernes
        List<DiaCalendarioDTO> dias = this.construirDiasSemana(lunes, diasConExamenDgt, hoy);

        // Construir las 9 filas de tramos de 45 min
        List<FilaCalendarioDTO> filas = new ArrayList<>();
        for (TramoHorarioDTO tramo : tramos)
        {
            List<CeldaCalendarioDTO> celdas = new ArrayList<>();
            for (DiaCalendarioDTO dia : dias)
            {
                LocalDateTime fechaHora = dia.fecha().atTime(tramo.horaInicio());
                boolean esExamenDgt = diasConExamenDgt.contains(dia.fecha());

                if (esExamenDgt)
                {
                    celdas.add(new CeldaCalendarioDTO(
                            dia.fecha(),
                            tramo.horaInicio(),
                            tramo.horaFin(),
                            fechaHora,
                            true,
                            true,
                            "EXAMEN_DGT",
                            null,
                            "Jornada Examen Oficial DGT",
                            "Convocatoria Oficial DGT",
                            false,
                            false,
                            null
                    ));
                    continue;
                }

                ClasePractica claseEnSlot = this.buscarClaseEnSlot(clases, dia.fecha(), tramo.horaInicio());
                if (claseEnSlot != null)
                {
                    String nombreAlumno = (claseEnSlot.getAlumno() != null)
                            ? claseEnSlot.getAlumno().getNombre() + " " + claseEnSlot.getAlumno().getApellidos()
                            : "Alumno";
                    String subtitulo = this.obtenerSubtituloEstado(claseEnSlot.getEstadoClase());

                    celdas.add(new CeldaCalendarioDTO(
                            dia.fecha(),
                            tramo.horaInicio(),
                            tramo.horaFin(),
                            fechaHora,
                            true,
                            false,
                            claseEnSlot.getEstadoClase().name(),
                            claseEnSlot.getId(),
                            nombreAlumno,
                            subtitulo,
                            true,
                            true,
                            "/profesor/clases/" + claseEnSlot.getId() + "/modal"
                    ));
                }
                else
                {
                    celdas.add(new CeldaCalendarioDTO(
                            dia.fecha(),
                            tramo.horaInicio(),
                            tramo.horaFin(),
                            fechaHora,
                            false,
                            false,
                            "LIBRE",
                            null,
                            "Libre",
                            "Disponible",
                            false,
                            false,
                            null
                    ));
                }
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

        // Control de capacidad de reserva y vehículo
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
        String motivoBloqueoReserva = null;
        if (!tieneProfesorAsignado)
        {
            motivoBloqueoReserva = "No tienes profesor asignado actualmente.";
        }
        else if (vehiculoEnMantenimiento)
        {
            motivoBloqueoReserva = "El vehículo de tu profesor está en mantenimiento.";
        }
        else if (capacidadReserva <= 0)
        {
            motivoBloqueoReserva = "No dispones de capacidad de reserva (saldo de clases agotado o reservado).";
        }

        // Consultar exámenes DGT del profesor tutor
        List<Examen> examenesDgt = (profesor != null)
                ? this.examenRepository.findByAlumnoProfesorOrderByFechaHoraDesc(profesor)
                : new ArrayList<>();

        Set<LocalDate> diasConExamenDgt = new HashSet<>();
        for (Examen examen : examenesDgt)
        {
            if (examen.getFechaHora() != null)
            {
                diasConExamenDgt.add(examen.getFechaHora().toLocalDate());
            }
        }

        // Consultar clases prácticas del profesor
        List<ClasePractica> clasesProfesor = (profesor != null)
                ? this.clasePracticaRepository.findByProfesorOrderByFechaHoraAsc(profesor)
                : new ArrayList<>();

        // Construir los 5 días de lunes a viernes
        List<DiaCalendarioDTO> dias = this.construirDiasSemana(lunes, diasConExamenDgt, hoy);

        LocalDateTime ahora = LocalDateTime.now();

        // Construir las 9 filas
        List<FilaCalendarioDTO> filas = new ArrayList<>();
        for (TramoHorarioDTO tramo : tramos)
        {
            List<CeldaCalendarioDTO> celdas = new ArrayList<>();
            for (DiaCalendarioDTO dia : dias)
            {
                LocalDateTime fechaHora = dia.fecha().atTime(tramo.horaInicio());
                boolean esExamenDgt = diasConExamenDgt.contains(dia.fecha());

                if (esExamenDgt)
                {
                    celdas.add(new CeldaCalendarioDTO(
                            dia.fecha(),
                            tramo.horaInicio(),
                            tramo.horaFin(),
                            fechaHora,
                            true,
                            true,
                            "EXAMEN_DGT",
                            null,
                            "Jornada Examen Oficial DGT",
                            "Convocatoria Oficial DGT (Bloqueado)",
                            false,
                            false,
                            null
                    ));
                    continue;
                }

                ClasePractica claseEnSlot = this.buscarClaseEnSlot(clasesProfesor, dia.fecha(), tramo.horaInicio());
                if (claseEnSlot != null)
                {
                    boolean esPropia = (alumno != null && claseEnSlot.getAlumno() != null
                            && claseEnSlot.getAlumno().getId().equals(alumno.getId()));
                    String subtitulo = this.obtenerSubtituloEstado(claseEnSlot.getEstadoClase());

                    if (esPropia)
                    {
                        celdas.add(new CeldaCalendarioDTO(
                                dia.fecha(),
                                tramo.horaInicio(),
                                tramo.horaFin(),
                                fechaHora,
                                true,
                                false,
                                claseEnSlot.getEstadoClase().name(),
                                claseEnSlot.getId(),
                                "Clase Práctica",
                                subtitulo,
                                true,
                                true,
                                "/alumno/clases/" + claseEnSlot.getId() + "/modal"
                        ));
                    }
                    else
                    {
                        // Si pertenece a otro alumno: no interactivo, anonimizado
                        celdas.add(new CeldaCalendarioDTO(
                                dia.fecha(),
                                tramo.horaInicio(),
                                tramo.horaFin(),
                                fechaHora,
                                true,
                                false,
                                "OCUPADO",
                                null,
                                "Clase Práctica",
                                "Horario Ocupado",
                                false,
                                false,
                                null
                        ));
                    }
                }
                else
                {
                    boolean esFuturo = fechaHora.isAfter(ahora);
                    boolean slotHabilitadoReserva = puedeReservar && esFuturo;
                    String modalUrl = slotHabilitadoReserva
                            ? "/alumno/clases/reservar-modal?fecha=" + fechaHora.toString()
                            : null;

                    celdas.add(new CeldaCalendarioDTO(
                            dia.fecha(),
                            tramo.horaInicio(),
                            tramo.horaFin(),
                            fechaHora,
                            false,
                            false,
                            "LIBRE",
                            null,
                            slotHabilitadoReserva ? "Reservar" : "Libre",
                            slotHabilitadoReserva ? "Disponible para reserva" : "No disponible",
                            false,
                            slotHabilitadoReserva,
                            modalUrl
                    ));
                }
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
     * Genera los 9 tramos fijos de 45 minutos delimitados por el turno del profesor.
     */
    public List<TramoHorarioDTO> generarTramos(TipoTurno turno)
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
        else if (estado == EstadoClase.CANCELADA)
        {
            return "Clase Cancelada";
        }
        return "Clase Pendiente";
    }

    /**
     * Formatea el rango visible central de la semana (ej. "5 – 9 oct 2026").
     */
    private String formatearRangoFechas(LocalDate lunes, LocalDate viernes)
    {
        Locale esLocale = Locale.of("es", "ES");
        DateTimeFormatter mesFormat = DateTimeFormatter.ofPattern("MMM", esLocale);
        String mesLunes = lunes.format(mesFormat).replace(".", "");
        String mesViernes = viernes.format(mesFormat).replace(".", "");

        if (lunes.getYear() == viernes.getYear())
        {
            if (lunes.getMonth() == viernes.getMonth())
            {
                return lunes.getDayOfMonth() + " – " + viernes.getDayOfMonth() + " " + mesViernes + " " + lunes.getYear();
            }
            else
            {
                return lunes.getDayOfMonth() + " " + mesLunes + " – " + viernes.getDayOfMonth() + " " + mesViernes + " " + lunes.getYear();
            }
        }
        else
        {
            return lunes.getDayOfMonth() + " " + mesLunes + " " + lunes.getYear() + " – " + viernes.getDayOfMonth() + " " + mesViernes + " " + viernes.getYear();
        }
    }
}
