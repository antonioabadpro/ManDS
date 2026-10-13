package com.autoescuela.erp.estadisticas.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autoescuela.erp.academico.model.Matricula;
import com.autoescuela.erp.academico.repository.MatriculaRepository;
import com.autoescuela.erp.core.enums.EstadoClase;
import com.autoescuela.erp.core.enums.EstadoSolicitud;
import com.autoescuela.erp.core.enums.EstadoUsuario;
import com.autoescuela.erp.core.enums.EstadoVehiculo;
import com.autoescuela.erp.core.enums.ModalidadMatricula;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.enums.TipoExamen;
import com.autoescuela.erp.core.enums.TipoMatricula;
import com.autoescuela.erp.estadisticas.dto.DesgloseFinancieroAdminDTO;
import com.autoescuela.erp.estadisticas.dto.DistribucionCarnetDTO;
import com.autoescuela.erp.estadisticas.dto.DistribucionModalidadDTO;
import com.autoescuela.erp.estadisticas.dto.EfectividadExamenDTO;
import com.autoescuela.erp.estadisticas.dto.EstadisticasAdminDTO;
import com.autoescuela.erp.estadisticas.dto.EstadisticasAutoescuelaDTO;
import com.autoescuela.erp.estadisticas.dto.KpisAdminDTO;
import com.autoescuela.erp.estadisticas.dto.RendimientoProfesorResumenDTO;
import com.autoescuela.erp.examenes.model.Examen;
import com.autoescuela.erp.examenes.repository.ExamenRepository;
import com.autoescuela.erp.examenes.repository.SolicitudExamenRepository;
import com.autoescuela.erp.flota.repository.VehiculoRepository;
import com.autoescuela.erp.practicas.model.ClasePractica;
import com.autoescuela.erp.practicas.repository.ClasePracticaRepository;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.model.Profesor;
import com.autoescuela.erp.usuarios.repository.ProfesorRepository;
import com.autoescuela.erp.usuarios.repository.AlumnoRepository;
import com.autoescuela.erp.estadisticas.dto.BalanceEconomicoAlumnoDTO;
import com.autoescuela.erp.estadisticas.dto.DistribucionClasesAlumnoDTO;
import com.autoescuela.erp.estadisticas.dto.EstadisticasAlumnoDTO;
import com.autoescuela.erp.estadisticas.dto.RendimientoExamenesAlumnoDTO;
import com.autoescuela.erp.estadisticas.dto.TiempoVolanteDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EstadisticaService
{
    private final AlumnoRepository alumnoRepository;
    private final ProfesorRepository profesorRepository;
    private final VehiculoRepository vehiculoRepository;
    private final SolicitudExamenRepository solicitudExamenRepository;
    private final MatriculaRepository matriculaRepository;
    private final ClasePracticaRepository clasePracticaRepository;
    private final ExamenRepository examenRepository;

    /**
     * Recopila y calcula las métricas globales clave de la autoescuela para el Dashboard de Administración.
     *
     * @return DTO con los totales agregados en tiempo real.
     */
    @Transactional(readOnly = true)
    public EstadisticasAutoescuelaDTO obtenerEstadisticasDashboard()
    {
        long totalAlumnos = this.alumnoRepository.countByEstado(EstadoUsuario.ACTIVO);
        long totalProfesores = this.profesorRepository.countByEstado(EstadoUsuario.ACTIVO);
        long totalVehiculosActivos = this.vehiculoRepository.countByEstadoNot(EstadoVehiculo.INACTIVO);
        long totalVehiculosOperativos = this.vehiculoRepository.countByEstado(EstadoVehiculo.DISPONIBLE)
                + this.vehiculoRepository.countByEstado(EstadoVehiculo.OCUPADO);
        long totalVehiculosEnMantenimiento = this.vehiculoRepository.countByEstado(EstadoVehiculo.MANTENIMIENTO);
        long totalSolicitudesExamenPendientes = this.solicitudExamenRepository.countByEstado(EstadoSolicitud.PENDIENTE);

        return EstadisticasAutoescuelaDTO.builder()
                .totalAlumnos(totalAlumnos)
                .totalProfesores(totalProfesores)
                .totalVehiculosActivos(totalVehiculosActivos)
                .totalVehiculosOperativos(totalVehiculosOperativos)
                .totalVehiculosEnMantenimiento(totalVehiculosEnMantenimiento)
                .totalSolicitudesExamenPendientes(totalSolicitudesExamenPendientes)
                .build();
    }

    /**
     * Recopila y calcula las métricas para el panel de Estadísticas del Administrador.
     *
     * @return DTO contenedor inmutable con todos los bloques de datos consolidados.
     */
    @Transactional(readOnly = true)
    public EstadisticasAdminDTO obtenerEstadisticasAdmin()
    {
        List<Matricula> todasMatriculas = this.matriculaRepository.findAll();
        List<Matricula> matriculasActivas = this.filtrarMatriculasActivas(todasMatriculas);
        long totalBolsaSaldo = this.calcularTotalBolsaSaldo(matriculasActivas);

        DistribucionModalidadDTO modalidadDTO = this.calcularTarjetaDistribucionModalidades(matriculasActivas);
        List<DistribucionCarnetDTO> todosCarnets = this.calcularTarjetaDistribucionCarnets(matriculasActivas);
        List<DistribucionCarnetDTO> topCarnets = new ArrayList<>(todosCarnets.subList(0, Math.min(4, todosCarnets.size()))
        );

        List<Examen> todosExamenes = this.examenRepository.findAll();
        EfectividadExamenDTO efectividadExamenDTO = this.calcularTarjetaEfectividadExamenes(todosExamenes);

        List<ClasePractica> todasClases = this.clasePracticaRepository.findAll();

        List<RendimientoProfesorResumenDTO> todosProfesores = this.calcularTarjetaRendimientoProfesores();
        List<RendimientoProfesorResumenDTO> topProfesores = new ArrayList<>(todosProfesores.subList(0, Math.min(3, todosProfesores.size()))
        );

        DesgloseFinancieroAdminDTO financieroDTO = this.calcularTarjetaDesgloseFinanciero(
                todasMatriculas,
                modalidadDTO,
                todasClases,
                totalBolsaSaldo
        );

        KpisAdminDTO kpisDTO = this.calcularTarjetaKpisGlobales(
                efectividadExamenDTO,
                todasClases,
                financieroDTO.totalFacturado(),
                todasMatriculas.size(),
                matriculasActivas.size(),
                totalBolsaSaldo
        );

        return new EstadisticasAdminDTO(
                kpisDTO,
                modalidadDTO,
                efectividadExamenDTO,
                topCarnets,
                todosCarnets,
                topProfesores,
                todosProfesores,
                financieroDTO
        );
    }

    /**
     * Filtra la lista de matrículas para conservar únicamente aquellas en estado activo.
     */
    private List<Matricula> filtrarMatriculasActivas(List<Matricula> todasMatriculas)
    {
        List<Matricula> activas = new ArrayList<>();
        for (Matricula m : todasMatriculas)
        {
            if (Boolean.TRUE.equals(m.getEstaActiva()))
            {
                activas.add(m);
            }
        }
        return activas;
    }

    /**
     * Tarjeta Bloque 1: Saldo de Clases Pendientes.
     * Calcula la suma acumulada de saldo de clases prácticas pendientes entre los alumnos con matrícula activa.
     * Utilizado en las Estadísticas del Administrador
     */
    private long calcularTotalBolsaSaldo(List<Matricula> matriculasActivas)
    {
        long totalBolsa = 0;
        for (Matricula m : matriculasActivas)
        {
            if (m.getSaldoClases() != null && m.getSaldoClases() > 0)
            {
                totalBolsa += m.getSaldoClases();
            }
        }
        return totalBolsa;
    }

    /**
     * Tarjeta Bloque 1: Cuadrícula de KPIs Clave Globales.
     * Consolida los 4 indicadores superiores del panel (Tasa de Aprobados, Horas/Km, Facturación y Alumnos en formación).
     * Utilizado en las Estadísticas del Administrador
     */
    private KpisAdminDTO calcularTarjetaKpisGlobales(
            EfectividadExamenDTO examenes,
            List<ClasePractica> todasClases,
            double facturacionBruta,
            long totalMatriculasAbonadas,
            long alumnosFormandose,
            long bolsaClasesPendientes)
    {
        long totalExamenesAptos = examenes.teoricosAptos() + examenes.practicosAptos();
        long totalExamenesEvaluados = examenes.teoricosTotal() + examenes.practicosTotal();
        double tasaAprobadosGlobal = totalExamenesEvaluados > 0
                ? Math.round(((double) totalExamenesAptos / totalExamenesEvaluados) * 1000.0) / 10.0
                : 0.0;

        long minutosPracticas = 0;
        long kmTotales = 0;
        for (ClasePractica cp : todasClases)
        {
            if (cp.getEstadoClase() == EstadoClase.RECIBIDA)
            {
                minutosPracticas += cp.getDuracion() != null ? cp.getDuracion() : 45;
                if (cp.getKmFin() != null && cp.getKmInicio() != null)
                {
                    kmTotales += Math.max(0, cp.getKmFin() - cp.getKmInicio());
                }
            }
        }
        double horasPracticas = Math.round((minutosPracticas / 60.0) * 10.0) / 10.0;

        return new KpisAdminDTO(
                tasaAprobadosGlobal,
                totalExamenesAptos,
                totalExamenesEvaluados,
                horasPracticas,
                minutosPracticas,
                kmTotales,
                facturacionBruta,
                totalMatriculasAbonadas,
                alumnosFormandose,
                bolsaClasesPendientes
        );
    }

    /**
     * Tarjeta Bloque 2 (Izquierda): Distribución por Modalidad de Matrícula.
     * Computa los desgloses pedagógicos (Teórico + Prácticas vs Sólo Prácticas) y de expediente (Nuevas vs Renovaciones).
     * Utilizado en las Estadísticas del Administrador
     */
    private DistribucionModalidadDTO calcularTarjetaDistribucionModalidades(List<Matricula> matriculasActivas)
    {
        long countTeoricoPractica = 0;
        long countSoloPractica = 0;
        long countNuevas = 0;
        long countRenovaciones = 0;

        for (Matricula m : matriculasActivas)
        {
            if (m.getModalidad() == ModalidadMatricula.TEORICO_PRACTICA)
            {
                countTeoricoPractica++;
            }
            else if (m.getModalidad() == ModalidadMatricula.PRACTICA)
            {
                countSoloPractica++;
            }

            if (m.getTipo() == TipoMatricula.RENOVACION)
            {
                countRenovaciones++;
            }
            else
            {
                countNuevas++;
            }
        }

        long totalMatriculasActivas = matriculasActivas.size();
        double pctTeoricoPractica = totalMatriculasActivas > 0
                ? Math.round(((double) countTeoricoPractica / totalMatriculasActivas) * 100.0)
                : 0.0;
        double pctSoloPractica = totalMatriculasActivas > 0
                ? Math.round(((double) countSoloPractica / totalMatriculasActivas) * 100.0)
                : 0.0;
        double pctNuevas = totalMatriculasActivas > 0
                ? Math.round(((double) countNuevas / totalMatriculasActivas) * 100.0)
                : 0.0;
        double pctRenovaciones = totalMatriculasActivas > 0
                ? Math.round(((double) countRenovaciones / totalMatriculasActivas) * 100.0)
                : 0.0;

        return new DistribucionModalidadDTO(
                countTeoricoPractica,
                countSoloPractica,
                pctTeoricoPractica,
                pctSoloPractica,
                countNuevas,
                countRenovaciones,
                pctNuevas,
                pctRenovaciones,
                totalMatriculasActivas
        );
    }

    /**
     * Tarjeta Bloque 2 (Derecha): Efectividad por Tipo de Examen (DGT).
     * Computa las tasas de aptos y no aptos oficiales para pruebas teóricas y prácticas en vía abierta.
     * Utilizado en las Estadísticas del Administrador
     */
    private EfectividadExamenDTO calcularTarjetaEfectividadExamenes(List<Examen> todosExamenes)
    {
        long teoricosAptos = 0;
        long teoricosNoAptos = 0;
        long practicosAptos = 0;
        long practicosNoAptos = 0;

        for (Examen ex : todosExamenes)
        {
            if (ex.getTipo() == TipoExamen.TEORICO)
            {
                if (Boolean.TRUE.equals(ex.getEsApto()))
                {
                    teoricosAptos++;
                }
                else if (Boolean.FALSE.equals(ex.getEsApto()))
                {
                    teoricosNoAptos++;
                }
            }
            else if (ex.getTipo() == TipoExamen.PRACTICO)
            {
                if (Boolean.TRUE.equals(ex.getEsApto()))
                {
                    practicosAptos++;
                }
                else if (Boolean.FALSE.equals(ex.getEsApto()))
                {
                    practicosNoAptos++;
                }
            }
        }

        long teoricosTotal = teoricosAptos + teoricosNoAptos;
        double pctTeoricos = teoricosTotal > 0
                ? Math.round(((double) teoricosAptos / teoricosTotal) * 100.0)
                : 0.0;
        long practicosTotal = practicosAptos + practicosNoAptos;
        double pctPracticos = practicosTotal > 0
                ? Math.round(((double) practicosAptos / practicosTotal) * 100.0)
                : 0.0;

        return new EfectividadExamenDTO(
                teoricosAptos,
                teoricosNoAptos,
                teoricosTotal,
                pctTeoricos,
                practicosAptos,
                practicosNoAptos,
                practicosTotal,
                pctPracticos
        );
    }

    /**
     * Tarjeta Bloque 3 (Izquierda): Carnets de Conducir.
     * Computa el desglose y porcentaje de demanda de cada permiso oficial entre los alumnos con matrícula activa.
     * Utilizado en las Estadísticas del Administrador
     */
    private List<DistribucionCarnetDTO> calcularTarjetaDistribucionCarnets(List<Matricula> matriculasActivas)
    {
        List<DistribucionCarnetDTO> carnets = new ArrayList<>();
        long totalMatriculasActivas = matriculasActivas.size();

        for (TipoCarnet carnet : TipoCarnet.values())
        {
            long totalCarnet = 0;
            for (Matricula m : matriculasActivas)
            {
                if (m.getPermisoCarnet() == carnet)
                {
                    totalCarnet++;
                }
            }
            double pctCarnet = totalMatriculasActivas > 0
                    ? Math.round(((double) totalCarnet / totalMatriculasActivas) * 100.0)
                    : 0.0;
            carnets.add(new DistribucionCarnetDTO(carnet, carnet.getDescripcion(), totalCarnet, pctCarnet));
        }

        carnets.sort((c1, c2) -> Long.compare(c2.totalAlumnos(), c1.totalAlumnos()));
        return carnets;
    }

    /**
     * Tarjeta Bloque 3 (Derecha): TOP 3 Profesores y Modal Completo.
     * Evalúa el rendimiento individual de cada profesor según clases impartidas, horas de conducción y tasa de aptos DGT.
     * Utilizado en las Estadísticas del Administrador
     */
    private List<RendimientoProfesorResumenDTO> calcularTarjetaRendimientoProfesores()
    {
        List<Profesor> listaProfesores = this.profesorRepository.findAll();
        List<RendimientoProfesorResumenDTO> todosProfesores = new ArrayList<>();

        for (Profesor prof : listaProfesores)
        {
            if (prof.getEstado() == EstadoUsuario.ACTIVO)
            {
                List<ClasePractica> clasesProf = this.clasePracticaRepository.findByProfesorAndEstadoClase(prof, EstadoClase.RECIBIDA);
                long clasesImpartidas = clasesProf.size();
                int minutosProf = 0;
                for (ClasePractica cp : clasesProf)
                {
                    minutosProf += cp.getDuracion() != null ? cp.getDuracion() : 45;
                }
                double horasConduccionProf = Math.round((minutosProf / 60.0) * 10.0) / 10.0;
                long alumnosAsignados = prof.getListaAlumnos() != null ? prof.getListaAlumnos().size() : 0;

                List<Examen> examenesProf = this.examenRepository.findByAlumnoProfesorOrderByFechaHoraDesc(prof);
                long aptosProf = 0;
                long noAptosProf = 0;
                for (Examen ex : examenesProf)
                {
                    if (Boolean.TRUE.equals(ex.getEsApto()))
                    {
                        aptosProf++;
                    }
                    else if (Boolean.FALSE.equals(ex.getEsApto()))
                    {
                        noAptosProf++;
                    }
                }
                long evaluadosProf = aptosProf + noAptosProf;
                double tasaAprobadosProf = evaluadosProf > 0
                        ? Math.round(((double) aptosProf / evaluadosProf) * 100.0)
                        : 0.0;

                String vehiculoStr = prof.getVehiculo() != null
                        ? prof.getVehiculo().getMarca() + " " + prof.getVehiculo().getModelo() + " (" + prof.getVehiculo().getMatricula() + ")"
                        : "Sin vehículo asignado";

                String nombreCompleto = prof.getNombre() + " " + prof.getApellidos();

                todosProfesores.add(new RendimientoProfesorResumenDTO(
                        prof.getId(),
                        nombreCompleto,
                        prof.getTurno(),
                        vehiculoStr,
                        clasesImpartidas,
                        horasConduccionProf,
                        alumnosAsignados,
                        aptosProf,
                        noAptosProf,
                        tasaAprobadosProf
                ));
            }
        }

        todosProfesores.sort((p1, p2) -> {
            int cmpTasa = Double.compare(p2.tasaAprobados(), p1.tasaAprobados());
            if (cmpTasa != 0)
            {
                return cmpTasa;
            }
            int cmpClases = Long.compare(p2.clasesImpartidas(), p1.clasesImpartidas());
            if (cmpClases != 0)
            {
                return cmpClases;
            }
            return Long.compare(p2.alumnosAsignados(), p1.alumnosAsignados());
        });

        return todosProfesores;
    }

    /**
     * Tarjeta Bloque 4: Desglose de Ingresos Estimados.
     * Computa los ingresos brutos consolidados por modalidades de matrícula, renovaciones de expediente y clases prácticas.
     * Utilizado en las Estadísticas del Administrador
     */
    private DesgloseFinancieroAdminDTO calcularTarjetaDesgloseFinanciero(
            List<Matricula> todasMatriculas,
            DistribucionModalidadDTO modalidades,
            List<ClasePractica> todasClases,
            long totalBolsaSaldo)
    {
        double ingresosTeoricoPractica = 0.0;
        double ingresosSoloPractica = 0.0;
        double ingresosRenovaciones = 0.0;

        for (Matricula m : todasMatriculas)
        {
            boolean esPesado = m.getPermisoCarnet() != null &&
                    (m.getPermisoCarnet().name().startsWith("PERMISO_C") || m.getPermisoCarnet().name().startsWith("PERMISO_D"));
            double tarifaBase = esPesado ? 450.0 : 250.0;
            double precioM = m.getPrecio() != null ? m.getPrecio() : tarifaBase;

            if (m.getTipo() == TipoMatricula.RENOVACION)
            {
                if (precioM <= tarifaBase)
                {
                    precioM += tarifaBase;
                }
                ingresosRenovaciones += precioM;
            }
            else
            {
                if (m.getModalidad() == ModalidadMatricula.TEORICO_PRACTICA)
                {
                    ingresosTeoricoPractica += precioM;
                }
                else
                {
                    ingresosSoloPractica += precioM;
                }
            }
        }

        long clasesRecibidasTotal = 0;
        for (ClasePractica cp : todasClases)
        {
            if (cp.getEstadoClase() == EstadoClase.RECIBIDA)
            {
                clasesRecibidasTotal++;
            }
        }

        long totalClasesFacturadas = clasesRecibidasTotal + totalBolsaSaldo;
        double ingresosClases = totalClasesFacturadas * 30.0;
        double totalFacturado = ingresosTeoricoPractica + ingresosSoloPractica + ingresosRenovaciones + ingresosClases;

        return new DesgloseFinancieroAdminDTO(
                totalFacturado,
                ingresosTeoricoPractica,
                modalidades.teoricoPractica(),
                ingresosSoloPractica,
                modalidades.soloPractica(),
                ingresosClases,
                totalClasesFacturadas,
                ingresosRenovaciones,
                modalidades.matriculasRenovacion()
        );
    }

    /**
     * Recopila y calcula las métricas y evolución pedagógica para el panel de Estadísticas del Alumno.
     *
     * @param alumno Entidad del alumno autenticado.
     * @return DTO inmutable consolidado con los datos de todas las tarjetas del panel.
     */
    @Transactional(readOnly = true)
    public EstadisticasAlumnoDTO obtenerEstadisticasAlumno(Alumno alumno)
    {
        if (alumno == null)
        {
            return new EstadisticasAlumnoDTO();
        }

        Matricula matricula = this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno).orElse(null);
        List<ClasePractica> clasesRecibidas = this.clasePracticaRepository.findByAlumnoAndEstadoClase(alumno, EstadoClase.RECIBIDA);

        // Tarjeta Bloque 1 (KPI 1): Tiempo al Volante
        TiempoVolanteDTO tiempoVolanteDTO = this.calcularTarjetaTiempoVolante(clasesRecibidas.size());

        // Tarjeta Bloque 1 (KPI 2): Distancia Recorrida
        long kilometrosRecorridos = this.calcularTarjetaDistanciaRecorrida(clasesRecibidas);

        // Tarjeta Bloque 1 (KPI 3): Rendimiento en Exámenes DGT y Convocatorias
        RendimientoExamenesAlumnoDTO examenesDTO = this.calcularTarjetaExamenesDgt(alumno, matricula);

        // Tarjeta Bloque 2 (Izquierda): Distribución de Clases Prácticas
        DistribucionClasesAlumnoDTO clasesDTO = this.calcularTarjetaDistribucionClases(alumno);

        // Tarjeta Bloque 3: Balance Económico Formativo e Inversión
        List<Matricula> historialMatriculas = this.matriculaRepository.findByAlumnoOrderByFechaMatriculacionDesc(alumno);
        BalanceEconomicoAlumnoDTO balanceDTO = this.calcularTarjetaBalanceEconomico(matricula, historialMatriculas, clasesDTO.clasesRecibidas());

        return new EstadisticasAlumnoDTO(
                tiempoVolanteDTO,
                kilometrosRecorridos,
                examenesDTO,
                clasesDTO,
                balanceDTO
        );
    }

    /**
     * Tarjeta Bloque 1 (KPI 1): Tiempo al Volante.
     * Computa los minutos y horas acumuladas al volante a razón de 45 minutos por clase práctica completada.
     */
    private TiempoVolanteDTO calcularTarjetaTiempoVolante(long clasesRecibidas)
    {
        long minutosConduccion = clasesRecibidas * 45;
        double horasConduccion = (clasesRecibidas * 45) / 60.0;
        return new TiempoVolanteDTO(minutosConduccion, horasConduccion);
    }

    /**
     * Tarjeta Bloque 1 (KPI 2): Distancia Recorrida.
     * Calcula la suma de kilómetros recorridos a partir de las fichas técnicas registradas por el profesor.
     */
    private long calcularTarjetaDistanciaRecorrida(List<ClasePractica> clasesRecibidas)
    {
        long kilometrosRecorridos = 0;
        if (clasesRecibidas != null)
        {
            for (ClasePractica c : clasesRecibidas)
            {
                if (c.getKmFin() != null && c.getKmInicio() != null && c.getKmFin() >= c.getKmInicio())
                {
                    kilometrosRecorridos += (c.getKmFin() - c.getKmInicio());
                }
            }
        }
        return kilometrosRecorridos;
    }

    /**
     * Tarjeta Bloque 1 (KPI 3): Exámenes DGT y Convocatorias.
     * Computa el balance de exámenes oficiales aptos y suspensos, así como las convocatorias restantes y consumidas.
     */
    private RendimientoExamenesAlumnoDTO calcularTarjetaExamenesDgt(Alumno alumno, Matricula matricula)
    {
        long aptos = this.examenRepository.countByAlumnoAndEsAptoTrue(alumno);
        long suspensos = this.examenRepository.countByAlumnoAndEsAptoFalse(alumno);

        int convocatoriasGastadas = (matricula != null && matricula.getConvocatoriasGastadas() != null)
                ? matricula.getConvocatoriasGastadas()
                : 0;
        int convocatoriasRestantes = (matricula != null && matricula.getConvocatorias() != null)
                ? matricula.getConvocatorias()
                : 2;

        return new RendimientoExamenesAlumnoDTO(aptos, suspensos, convocatoriasGastadas, convocatoriasRestantes);
    }

    /**
     * Tarjeta Bloque 2 (Izquierda): Distribución de Clases Prácticas.
     * Computa el desglose de sesiones prácticas según su estado en agenda (recibidas, pendientes y canceladas).
     */
    private DistribucionClasesAlumnoDTO calcularTarjetaDistribucionClases(Alumno alumno)
    {
        long clasesRecibidas = this.clasePracticaRepository.countByAlumnoAndEstadoClase(alumno, EstadoClase.RECIBIDA);
        long clasesPendientes = this.clasePracticaRepository.countByAlumnoAndEstadoClase(alumno, EstadoClase.PENDIENTE);
        long clasesCanceladas = this.clasePracticaRepository.countByAlumnoAndEstadoClase(alumno, EstadoClase.CANCELADA);
        long totalClases = this.clasePracticaRepository.countByAlumno(alumno);

        double pctRecibidas = totalClases > 0 ? (clasesRecibidas * 100.0 / totalClases) : 0.0;
        double pctPendientes = totalClases > 0 ? (clasesPendientes * 100.0 / totalClases) : 0.0;
        double pctCanceladas = totalClases > 0 ? (clasesCanceladas * 100.0 / totalClases) : 0.0;

        return new DistribucionClasesAlumnoDTO(
                clasesRecibidas,
                clasesPendientes,
                clasesCanceladas,
                totalClases,
                pctRecibidas,
                pctPendientes,
                pctCanceladas
        );
    }

    /**
     * Tarjeta Bloque 3: Balance Económico Formativo.
     * Computa la inversión acumulada en cuotas de matrícula (incluidas renovaciones por suspensos),
     * el gasto en clases prácticas contratadas según tarifas oficiales y el gasto global formativo.
     */
    private BalanceEconomicoAlumnoDTO calcularTarjetaBalanceEconomico(
            Matricula matricula,
            List<Matricula> historialMatriculas,
            long clasesRecibidas)
    {
        boolean esVehiculoPesado = matricula != null && matricula.getPermisoCarnet() != null &&
                (matricula.getPermisoCarnet().name().startsWith("PERMISO_C") || matricula.getPermisoCarnet().name().startsWith("PERMISO_D"));
        double tarifaClase = esVehiculoPesado ? 60.0 : 30.0;
        double tarifaMatriculaDefecto = esVehiculoPesado ? 450.0 : 250.0;

        double totalMatriculas = 0.0;
        if (historialMatriculas != null && !historialMatriculas.isEmpty())
        {
            for (Matricula m : historialMatriculas)
            {
                boolean esPesadoM = m.getPermisoCarnet() != null &&
                        (m.getPermisoCarnet().name().startsWith("PERMISO_C") || m.getPermisoCarnet().name().startsWith("PERMISO_D"));
                double tarifaBaseM = esPesadoM ? 450.0 : 250.0;
                double precioM = m.getPrecio() != null ? m.getPrecio() : tarifaBaseM;
                if (m.getTipo() == TipoMatricula.RENOVACION && precioM <= tarifaBaseM)
                {
                    precioM += tarifaBaseM;
                }
                totalMatriculas += precioM;
            }
        }
        else if (matricula != null)
        {
            double precioM = matricula.getPrecio() != null ? matricula.getPrecio() : tarifaMatriculaDefecto;
            if (matricula.getTipo() == TipoMatricula.RENOVACION && precioM <= tarifaMatriculaDefecto)
            {
                precioM += tarifaMatriculaDefecto;
            }
            totalMatriculas = precioM;
        }

        int saldoClasesActual = (matricula != null && matricula.getSaldoClases() != null)
                ? matricula.getSaldoClases()
                : 0;
        long totalClasesContratadas = clasesRecibidas + saldoClasesActual;
        double gastoClases = totalClasesContratadas * tarifaClase;
        double gastoTotal = totalMatriculas + gastoClases;

        return new BalanceEconomicoAlumnoDTO(
                totalMatriculas,
                gastoClases,
                gastoTotal,
                tarifaClase,
                saldoClasesActual,
                totalClasesContratadas,
                esVehiculoPesado
        );
    }
}
