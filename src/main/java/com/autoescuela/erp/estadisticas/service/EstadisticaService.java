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
import com.autoescuela.erp.usuarios.model.Profesor;
import com.autoescuela.erp.usuarios.repository.AlumnoRepository;
import com.autoescuela.erp.usuarios.repository.ProfesorRepository;

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
     * Recopila, calcula y agrega todas las métricas operativas, pedagógicas y financieras
     * para el panel completo de Estadísticas del Administrador.
     *
     * @return DTO contenedor inmutable con todos los bloques de datos consolidados.
     */
    @Transactional(readOnly = true)
    public EstadisticasAdminDTO obtenerEstadisticasAdmin()
    {
        List<Matricula> todasMatriculas = this.matriculaRepository.findAll();
        List<Matricula> matriculasActivas = new ArrayList<>();
        long countTeoricoPractica = 0;
        long countSoloPractica = 0;
        long countNuevas = 0;
        long countRenovaciones = 0;
        double ingresosTeoricoPractica = 0.0;
        double ingresosSoloPractica = 0.0;
        double ingresosRenovaciones = 0.0;
        long totalBolsaSaldo = 0;

        // 1. Calculamos las estadísticas de la tarjeta de Distribución de Modalidades y Carnets, así como los ingresos por tipo de matrícula.
        for (Matricula m : todasMatriculas)
        {
            if (Boolean.TRUE.equals(m.getEstaActiva()))
            {
                matriculasActivas.add(m);
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

                if (m.getSaldoClases() != null && m.getSaldoClases() > 0)
                {
                    totalBolsaSaldo += m.getSaldoClases();
                }
            }

            // 2. Calculamos las estadísticas de la tarjeta financiera de todas las matrículas emitidas
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

        DistribucionModalidadDTO modalidadDTO = new DistribucionModalidadDTO(
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

        // 3. Calculamos las estadisticas de la tarjeta de Distribución de alumnos por carnet
        List<DistribucionCarnetDTO> carnets = new ArrayList<>();
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

        // 4. Calculamos las estadisticas de la tareta de Efectividad por Tipo de Examen
        List<Examen> todosExamenes = this.examenRepository.findAll();
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

        EfectividadExamenDTO efectividadExamenDTO = new EfectividadExamenDTO(
                teoricosAptos,
                teoricosNoAptos,
                teoricosTotal,
                pctTeoricos,
                practicosAptos,
                practicosNoAptos,
                practicosTotal,
                pctPracticos
        );

        // Calculamos el total de clases prácticas recibidas y el total de kilómetros recorridos por todos los alumnos, así como el total de horas prácticas impartidas.
        List<ClasePractica> todasClases = this.clasePracticaRepository.findAll();
        long minutosPracticas = 0;
        long kmTotales = 0;
        long clasesRecibidasTotal = 0;

        for (ClasePractica cp : todasClases)
        {
            if (cp.getEstadoClase() == EstadoClase.RECIBIDA)
            {
                clasesRecibidasTotal++;
                minutosPracticas += cp.getDuracion() != null ? cp.getDuracion() : 45;
                if (cp.getKmFin() != null && cp.getKmInicio() != null)
                {
                    kmTotales += Math.max(0, cp.getKmFin() - cp.getKmInicio());
                }
            }
        }
        double horasPracticas = Math.round((minutosPracticas / 60.0) * 10.0) / 10.0;

        // 5. Calculamos las estadisticas de la tajeta del Rendimiento de los Profesores
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

        // Ordenamos la lista de profesores por tasa de aprobados, luego por clases impartidas y finalmente por alumnos asignados para determinar los top 5 profesores.
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

        List<RendimientoProfesorResumenDTO> topProfesores = new ArrayList<>(
                todosProfesores.subList(0, Math.min(5, todosProfesores.size()))
        );

        // 6. Calculamos las estadisticas de la tarjeta inferior de Facturación y finanzas
        long totalClasesFacturadas = clasesRecibidasTotal + totalBolsaSaldo;
        double ingresosClases = totalClasesFacturadas * 30.0;
        double totalFacturado = ingresosTeoricoPractica + ingresosSoloPractica + ingresosRenovaciones + ingresosClases;

        DesgloseFinancieroAdminDTO financieroDTO = new DesgloseFinancieroAdminDTO(
                totalFacturado,
                ingresosTeoricoPractica,
                countTeoricoPractica,
                ingresosSoloPractica,
                countSoloPractica,
                ingresosClases,
                totalClasesFacturadas,
                ingresosRenovaciones,
                countRenovaciones
        );

        // 7. Calculamos las estadisticas de los KPIs principales
        long totalExamenesAptos = teoricosAptos + practicosAptos;
        long totalExamenesEvaluados = teoricosTotal + practicosTotal;
        double tasaAprobadosGlobal = totalExamenesEvaluados > 0
                ? Math.round(((double) totalExamenesAptos / totalExamenesEvaluados) * 1000.0) / 10.0
                : 0.0;

        KpisAdminDTO kpisDTO = new KpisAdminDTO(
                tasaAprobadosGlobal,
                totalExamenesAptos,
                totalExamenesEvaluados,
                horasPracticas,
                minutosPracticas,
                kmTotales,
                totalFacturado,
                todasMatriculas.size(),
                totalMatriculasActivas,
                totalBolsaSaldo
        );

        return new EstadisticasAdminDTO(
                kpisDTO,
                modalidadDTO,
                efectividadExamenDTO,
                carnets,
                topProfesores,
                todosProfesores,
                financieroDTO
        );
    }
}
