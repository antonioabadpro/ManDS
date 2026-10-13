package com.autoescuela.erp.estadisticas.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import com.autoescuela.erp.estadisticas.dto.ClasesPreviasAprobadoProfesorDTO;
import com.autoescuela.erp.estadisticas.dto.ConvocatoriasAprobadosProfesorDTO;
import com.autoescuela.erp.estadisticas.dto.DistribucionClasesAlumnoDTO;
import com.autoescuela.erp.estadisticas.dto.DistribucionClasesProfesorDTO;
import com.autoescuela.erp.estadisticas.dto.EstadisticasAlumnoDTO;
import com.autoescuela.erp.estadisticas.dto.EstadisticasProfesorDTO;
import com.autoescuela.erp.estadisticas.dto.RendimientoExamenesAlumnoDTO;
import com.autoescuela.erp.estadisticas.dto.RendimientoExamenesProfesorDTO;
import com.autoescuela.erp.estadisticas.dto.TiempoVolanteDTO;
import com.autoescuela.erp.estadisticas.dto.TiempoVolanteProfesorDTO;

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

    /**************************************************************************************/
    /*************************** Estadísticas del Administrador ***************************/
    /**************************************************************************************/

    /**
     * Recopila y calcula las métricas globales clave de la autoescuela para el Dashboard de Administración.
     *
     * @return DTO con los totales agregados en tiempo real.
     */
    @Transactional(readOnly = true)
    public EstadisticasAutoescuelaDTO obtenerEstadisticasDashboardAdmin()
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
                ? Math.round(((double) countTeoricoPractica / totalMatriculasActivas) * 1000.0) / 10.0
                : 0.0;
        double pctSoloPractica = totalMatriculasActivas > 0
                ? Math.round(((double) countSoloPractica / totalMatriculasActivas) * 1000.0) / 10.0
                : 0.0;
        double pctNuevas = totalMatriculasActivas > 0
                ? Math.round(((double) countNuevas / totalMatriculasActivas) * 1000.0) / 10.0
                : 0.0;
        double pctRenovaciones = totalMatriculasActivas > 0
                ? Math.round(((double) countRenovaciones / totalMatriculasActivas) * 1000.0) / 10.0
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
                ? Math.round(((double) teoricosAptos / teoricosTotal) * 1000.0) / 10.0
                : 0.0;
        long practicosTotal = practicosAptos + practicosNoAptos;
        double pctPracticos = practicosTotal > 0
                ? Math.round(((double) practicosAptos / practicosTotal) * 1000.0) / 10.0
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
                    ? Math.round(((double) totalCarnet / totalMatriculasActivas) * 1000.0) / 10.0
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
                        ? Math.round(((double) aptosProf / evaluadosProf) * 1000.0) / 10.0
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


    /**************************************************************************************/
    /********************************** Estadísticas del Alumno ***************************/
    /**************************************************************************************/


    /**
     * Recopila y calcula las métricas y evolución pedagógica para el panel de Estadísticas del Alumno.
     * Utilizado en las Estadísticas del Alumno
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
     * Utilizado en las Estadísticas del Alumno
     */
    private TiempoVolanteDTO calcularTarjetaTiempoVolante(long clasesRecibidas)
    {
        long minutosConduccion = clasesRecibidas * 45;
        double horasConduccion = (clasesRecibidas * 45) / 60.0;
        return new TiempoVolanteDTO(minutosConduccion, horasConduccion);
    }

    /**
     * Tarjeta Bloque 1 (KPI 2): Distancia Recorrida.
     * Calcula la suma de kilómetros recorridos a partir de las fichas técnicas registradas por el profesor.ç
     * Utilizado en las Estadísticas del Alumno
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
     * Utilizado en las Estadísticas del Alumno
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
     * Utilizado en las Estadísticas del Alumno
     */
    private DistribucionClasesAlumnoDTO calcularTarjetaDistribucionClases(Alumno alumno)
    {
        long clasesRecibidas = this.clasePracticaRepository.countByAlumnoAndEstadoClase(alumno, EstadoClase.RECIBIDA);
        long clasesPendientes = this.clasePracticaRepository.countByAlumnoAndEstadoClase(alumno, EstadoClase.PENDIENTE);
        long clasesCanceladas = this.clasePracticaRepository.countByAlumnoAndEstadoClase(alumno, EstadoClase.CANCELADA);
        long totalClases = this.clasePracticaRepository.countByAlumno(alumno);

        double pctRecibidas = totalClases > 0 ? Math.round((clasesRecibidas * 100.0 / totalClases) * 10.0) / 10.0 : 0.0;
        double pctPendientes = totalClases > 0 ? Math.round((clasesPendientes * 100.0 / totalClases) * 10.0) / 10.0 : 0.0;
        double pctCanceladas = totalClases > 0 ? Math.round((clasesCanceladas * 100.0 / totalClases) * 10.0) / 10.0 : 0.0;

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
     * Utilizado en las Estadísticas del Alumno
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

    /**************************************************************************************/
    /******************************* Estadísticas del Profesor ****************************/
    /**************************************************************************************/

    /**
     * Recopila y calcula las métricas formativas y de rendimiento para el panel de Estadísticas del Profesor.
     * Utilizado en las Estadísticas del Profesor
     *
     * @param profesor Entidad del profesor autenticado.
     * @return DTO inmutable consolidado con los datos de todas las tarjetas del panel.
     */
    @Transactional(readOnly = true)
    public EstadisticasProfesorDTO obtenerEstadisticasProfesor(Profesor profesor)
    {
        if (profesor == null)
        {
            return new EstadisticasProfesorDTO();
        }

        List<ClasePractica> clasesProfesor = this.clasePracticaRepository.findByProfesorOrderByFechaHoraAsc(profesor);
        List<ClasePractica> clasesRecibidas = new ArrayList<>();
        if (clasesProfesor != null)
        {
            for (ClasePractica cp : clasesProfesor)
            {
                if (cp.getEstadoClase() == EstadoClase.RECIBIDA)
                {
                    clasesRecibidas.add(cp);
                }
            }
        }

        // Exámenes oficiales de los alumnos del profesor
        List<Examen> examenesProfesor = this.examenRepository.findByAlumnoProfesorOrderByFechaHoraDesc(profesor);

        // Tarjeta 1 y Bloque Desglose 1: Rendimiento en Exámenes DGT
        RendimientoExamenesProfesorDTO examenesDTO = this.calcularTarjetaExamenesProfesor(examenesProfesor);

        // Tarjeta 2 y Bloque Desglose 2: Distribución de Clases Prácticas
        DistribucionClasesProfesorDTO clasesDTO = this.calcularTarjetaDistribucionClasesProfesor(clasesProfesor);

        // Tarjeta 3: Horas al Volante
        TiempoVolanteProfesorDTO tiempoVolanteDTO = this.calcularTarjetaTiempoVolanteProfesor(clasesRecibidas);

        // Tarjeta 4: Kilómetros en Prácticas
        long distanciaRecorrida = this.calcularTarjetaDistanciaRecorridaProfesor(clasesRecibidas);

        // Permisos habilitados que imparte el profesor
        List<TipoCarnet> permisosHabilitados = this.obtenerPermisosHabilitadosProfesor(profesor);

        // Tarjeta Desglose 3: Aprobados por Convocatoria DGT
        ConvocatoriasAprobadosProfesorDTO convocatoriasDTO = this.calcularTarjetaConvocatoriasAprobadosProfesor(examenesProfesor);

        // Tarjeta Desglose 4: Clases prácticas previas al aprobado
        ClasesPreviasAprobadoProfesorDTO clasesPreviasDTO = this.calcularTarjetaClasesPreviasAprobadoProfesor(examenesProfesor);

        return new EstadisticasProfesorDTO(
                examenesDTO,
                clasesDTO,
                tiempoVolanteDTO,
                distanciaRecorrida,
                permisosHabilitados,
                convocatoriasDTO,
                clasesPreviasDTO
        );
    }

    /**
     * Tarjeta 1 y Bloque Desglose 1: Rendimiento en Exámenes DGT.
     * Computa los aptos, no aptos y la tasa de aprobados del profesor sobre las pruebas de sus alumnos.
     * Utilizado en las Estadísticas del Profesor
     */
    private RendimientoExamenesProfesorDTO calcularTarjetaExamenesProfesor(List<Examen> examenes)
    {
        long aptos = 0;
        long noAptos = 0;
        if (examenes != null)
        {
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
        }
        long totalEvaluados = aptos + noAptos;
        double tasaAprobados = totalEvaluados > 0
                ? Math.round(((double) aptos / totalEvaluados) * 1000.0) / 10.0
                : 0.0;
        double porcentajeNoAptos = totalEvaluados > 0
                ? Math.round(((double) noAptos / totalEvaluados) * 1000.0) / 10.0
                : 0.0;

        return new RendimientoExamenesProfesorDTO(aptos, noAptos, totalEvaluados, tasaAprobados, porcentajeNoAptos);
    }

    /**
     * Tarjeta 2 y Bloque Desglose 2: Distribución de Clases Prácticas.
     * Computa el desglose de sesiones prácticas según su estado en agenda (recibidas, pendientes y canceladas).
     * Utilizado en las Estadísticas del Profesor
     */
    private DistribucionClasesProfesorDTO calcularTarjetaDistribucionClasesProfesor(List<ClasePractica> clasesProfesor)
    {
        long clasesRecibidas = 0;
        long clasesPendientes = 0;
        long clasesCanceladas = 0;
        long totalClases = clasesProfesor != null ? clasesProfesor.size() : 0;

        if (clasesProfesor != null)
        {
            for (ClasePractica c : clasesProfesor)
            {
                if (c.getEstadoClase() == EstadoClase.RECIBIDA)
                {
                    clasesRecibidas++;
                }
                else if (c.getEstadoClase() == EstadoClase.PENDIENTE)
                {
                    clasesPendientes++;
                }
                else if (c.getEstadoClase() == EstadoClase.CANCELADA)
                {
                    clasesCanceladas++;
                }
            }
        }

        double pctRecibidas = totalClases > 0 ? Math.round((clasesRecibidas * 100.0 / totalClases) * 10.0) / 10.0 : 0.0;
        double pctPendientes = totalClases > 0 ? Math.round((clasesPendientes * 100.0 / totalClases) * 10.0) / 10.0 : 0.0;
        double pctCanceladas = totalClases > 0 ? Math.round((clasesCanceladas * 100.0 / totalClases) * 10.0) / 10.0 : 0.0;

        return new DistribucionClasesProfesorDTO(
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
     * Tarjeta 3: Horas al Volante.
     * Computa los minutos y horas acumuladas al volante en clases prácticas recibidas por el profesor.
     * Utilizado en las Estadísticas del Profesor
     */
    private TiempoVolanteProfesorDTO calcularTarjetaTiempoVolanteProfesor(List<ClasePractica> clasesRecibidas)
    {
        long totalMinutos = 0;
        if (clasesRecibidas != null)
        {
            for (ClasePractica c : clasesRecibidas)
            {
                int duracion = c.getDuracion() != null ? c.getDuracion() : 45;
                totalMinutos += duracion;
            }
        }
        double horasConduccion = Math.round((totalMinutos / 60.0) * 10.0) / 10.0;
        return new TiempoVolanteProfesorDTO(totalMinutos, horasConduccion);
    }

    /**
     * Tarjeta 4: Kilómetros en Prácticas.
     * Calcula la suma de kilómetros recorridos a partir de las fichas técnicas registradas por el profesor.
     * Utilizado en las Estadísticas del Profesor
     */
    private long calcularTarjetaDistanciaRecorridaProfesor(List<ClasePractica> clasesRecibidas)
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
     * Permisos Habilitados.
     * Recupera la lista inmutable de permisos de conducir que imparte el profesor.
     * Utilizado en las Estadísticas del Profesor
     */
    private List<TipoCarnet> obtenerPermisosHabilitadosProfesor(Profesor profesor)
    {
        return profesor != null && profesor.getListaTiposCarnet() != null
                ? new ArrayList<>(profesor.getListaTiposCarnet())
                : List.of();
    }

    /**
     * Tarjeta Desglose 3: Rendimiento por Convocatoria DGT.
     * Computa cuántos alumnos han aprobado en 1ª convocatoria, 2ª convocatoria y +3 convocatorias.
     * Utilizado en las Estadísticas del Profesor
     */
    private ConvocatoriasAprobadosProfesorDTO calcularTarjetaConvocatoriasAprobadosProfesor(List<Examen> examenesProfesor)
    {
        long primeraConvocatoria = 0;
        long segundaConvocatoria = 0;
        long masDeDosConvocatorias = 0;

        if (examenesProfesor != null)
        {
            Set<Object> alumnosAprobadosProcesados = new HashSet<>();

            for (Examen examen : examenesProfesor)
            {
                if (Boolean.TRUE.equals(examen.getEsApto()))
                {
                    Alumno alumno = examen.getAlumno();
                    if (alumno == null)
                    {
                        primeraConvocatoria++;
                        continue;
                    }

                    Object claveAlumno = alumno.getId() != null ? alumno.getId() : (alumno.getDni() != null ? alumno.getDni() : System.identityHashCode(alumno));
                    if (alumnosAprobadosProcesados.contains(claveAlumno))
                    {
                        continue;
                    }
                    alumnosAprobadosProcesados.add(claveAlumno);

                    List<Examen> examenesAlumno = this.examenRepository.findByAlumnoOrderByFechaHoraDesc(alumno);
                    int convocatoriasNecesarias = 0;

                    if (examenesAlumno != null && !examenesAlumno.isEmpty())
                    {
                        for (Examen exAl : examenesAlumno)
                        {
                            if (examen.getTipo() == null || exAl.getTipo() == examen.getTipo())
                            {
                                if (examen.getFechaHora() == null || exAl.getFechaHora() == null || !exAl.getFechaHora().isAfter(examen.getFechaHora()))
                                {
                                    convocatoriasNecesarias++;
                                }
                            }
                        }
                    }

                    if (convocatoriasNecesarias <= 1)
                    {
                        primeraConvocatoria++;
                    }
                    else if (convocatoriasNecesarias == 2)
                    {
                        segundaConvocatoria++;
                    }
                    else
                    {
                        masDeDosConvocatorias++;
                    }
                }
            }
        }

        long totalAprobados = primeraConvocatoria + segundaConvocatoria + masDeDosConvocatorias;
        double pctPrimera = totalAprobados > 0
                ? Math.round((primeraConvocatoria * 100.0 / totalAprobados) * 10.0) / 10.0
                : 0.0;
        double pctSegunda = totalAprobados > 0
                ? Math.round((segundaConvocatoria * 100.0 / totalAprobados) * 10.0) / 10.0
                : 0.0;
        double pctMasDeDos = totalAprobados > 0
                ? Math.round((masDeDosConvocatorias * 100.0 / totalAprobados) * 10.0) / 10.0
                : 0.0;

        return new ConvocatoriasAprobadosProfesorDTO(
                primeraConvocatoria,
                segundaConvocatoria,
                masDeDosConvocatorias,
                totalAprobados,
                pctPrimera,
                pctSegunda,
                pctMasDeDos
        );
    }

    /**
     * Tarjeta Desglose 4: Clases prácticas previas al aprobado.
     * Computa la media, el mínimo y el máximo de clases prácticas recibidas por los alumnos antes de aprobar.
     * Utilizado en las Estadísticas del Profesor
     */
    private ClasesPreviasAprobadoProfesorDTO calcularTarjetaClasesPreviasAprobadoProfesor(List<Examen> examenesProfesor)
    {
        List<Integer> clasesPorAlumno = new ArrayList<>();

        if (examenesProfesor != null)
        {
            Set<Object> alumnosAprobadosProcesados = new HashSet<>();

            for (Examen examen : examenesProfesor)
            {
                if (Boolean.TRUE.equals(examen.getEsApto()))
                {
                    Alumno alumno = examen.getAlumno();
                    if (alumno == null)
                    {
                        continue;
                    }

                    Object claveAlumno = alumno.getId() != null ? alumno.getId() : (alumno.getDni() != null ? alumno.getDni() : System.identityHashCode(alumno));
                    if (alumnosAprobadosProcesados.contains(claveAlumno))
                    {
                        continue;
                    }
                    alumnosAprobadosProcesados.add(claveAlumno);

                    List<ClasePractica> clasesRecibidas = this.clasePracticaRepository.findByAlumnoAndEstadoClase(alumno, EstadoClase.RECIBIDA);
                    int clasesAntesDeAprobar = 0;

                    if (clasesRecibidas != null)
                    {
                        for (ClasePractica cp : clasesRecibidas)
                        {
                            if (examen.getFechaHora() == null || cp.getFechaHora() == null || !cp.getFechaHora().isAfter(examen.getFechaHora()))
                            {
                                clasesAntesDeAprobar++;
                            }
                        }
                    }

                    clasesPorAlumno.add(clasesAntesDeAprobar);
                }
            }
        }

        if (clasesPorAlumno.isEmpty())
        {
            return new ClasesPreviasAprobadoProfesorDTO(0.0, 0, 0, 0);
        }

        int minClases = Integer.MAX_VALUE;
        int maxClases = Integer.MIN_VALUE;
        long sumaClases = 0;

        for (int cantidad : clasesPorAlumno)
        {
            if (cantidad < minClases)
            {
                minClases = cantidad;
            }
            if (cantidad > maxClases)
            {
                maxClases = cantidad;
            }
            sumaClases += cantidad;
        }

        double mediaClases = Math.round(((double) sumaClases / clasesPorAlumno.size()) * 10.0) / 10.0;
        long totalAlumnosAprobados = clasesPorAlumno.size();

        return new ClasesPreviasAprobadoProfesorDTO(mediaClases, minClases, maxClases, totalAlumnosAprobados);
    }
}
