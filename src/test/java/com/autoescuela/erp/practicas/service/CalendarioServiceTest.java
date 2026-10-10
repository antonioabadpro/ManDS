package com.autoescuela.erp.practicas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.autoescuela.erp.academico.model.Matricula;
import com.autoescuela.erp.academico.repository.MatriculaRepository;
import com.autoescuela.erp.core.enums.EstadoClase;
import com.autoescuela.erp.core.enums.EstadoVehiculo;
import com.autoescuela.erp.core.enums.TipoExamen;
import com.autoescuela.erp.core.enums.TipoTurno;
import com.autoescuela.erp.examenes.model.Examen;
import com.autoescuela.erp.examenes.repository.ExamenRepository;
import com.autoescuela.erp.flota.model.Vehiculo;
import com.autoescuela.erp.practicas.dto.CalendarioSemanalDTO;
import com.autoescuela.erp.practicas.dto.CeldaCalendarioDTO;
import com.autoescuela.erp.practicas.dto.TramoHorarioDTO;
import com.autoescuela.erp.practicas.model.ClasePractica;
import com.autoescuela.erp.practicas.repository.ClasePracticaRepository;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.model.Profesor;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios de CalendarioService (Cuadrícula Semanal SSR)")
class CalendarioServiceTest
{
    @Mock
    private ClasePracticaRepository clasePracticaRepository;

    @Mock
    private ExamenRepository examenRepository;

    @Mock
    private MatriculaRepository matriculaRepository;

    private CalendarioService calendarioService;

    @BeforeEach
    void setUp()
    {
        this.calendarioService = new CalendarioService(
                this.clasePracticaRepository,
                this.examenRepository,
                this.matriculaRepository
        );
    }

    @Test
    @DisplayName("generarTramos genera 9 tramos de 45 minutos para turno MATINAL (08:00 a 14:45)")
    void testGenerarTramosMatinal()
    {
        List<TramoHorarioDTO> tramos = this.calendarioService.generarTramos(TipoTurno.MATINAL);

        assertThat(tramos).hasSize(9);
        assertThat(tramos.get(0).horaInicio()).isEqualTo(LocalTime.of(8, 0));
        assertThat(tramos.get(0).horaFin()).isEqualTo(LocalTime.of(8, 45));
        assertThat(tramos.get(8).horaInicio()).isEqualTo(LocalTime.of(14, 0));
        assertThat(tramos.get(8).horaFin()).isEqualTo(LocalTime.of(14, 45));
    }

    @Test
    @DisplayName("generarTramos genera 9 tramos de 45 minutos para turno TARDE (15:00 a 21:45)")
    void testGenerarTramosTarde()
    {
        List<TramoHorarioDTO> tramos = this.calendarioService.generarTramos(TipoTurno.TARDE);

        assertThat(tramos).hasSize(9);
        assertThat(tramos.get(0).horaInicio()).isEqualTo(LocalTime.of(15, 0));
        assertThat(tramos.get(0).horaFin()).isEqualTo(LocalTime.of(15, 45));
        assertThat(tramos.get(8).horaInicio()).isEqualTo(LocalTime.of(21, 0));
        assertThat(tramos.get(8).horaFin()).isEqualTo(LocalTime.of(21, 45));
    }

    @Test
    @DisplayName("obtenerCalendarioProfesor construye la semana con 5 días hábiles y bloquea día entero por Examen DGT")
    void testObtenerCalendarioProfesorConExamenDgt()
    {
        Profesor profesor = new Profesor();
        profesor.setTurno(TipoTurno.MATINAL);

        LocalDate lunes = LocalDate.of(2026, 10, 5); // Lunes 5 de octubre de 2026
        LocalDate miercoles = LocalDate.of(2026, 10, 7);

        Examen examenDgt = new Examen();
        examenDgt.setFechaHora(miercoles.atTime(9, 0));
        examenDgt.setDuracion(45);
        examenDgt.setTipo(TipoExamen.PRACTICO);

        when(this.examenRepository.findByAlumnoProfesorOrderByFechaHoraDesc(profesor))
                .thenReturn(List.of(examenDgt));
        when(this.clasePracticaRepository.findByProfesorOrderByFechaHoraAsc(profesor))
                .thenReturn(new ArrayList<>());

        CalendarioSemanalDTO resultado = this.calendarioService.obtenerCalendarioProfesor(profesor, lunes);

        assertThat(resultado.dias()).hasSize(5);
        assertThat(resultado.dias().get(0).encabezadoCompleto()).isEqualTo("lun 5/10");
        assertThat(resultado.dias().get(2).esExamenDgt()).isTrue();
        assertThat(resultado.filas()).hasSize(9);

        // Verificar que en el miércoles todas las celdas están bloqueadas por examen DGT
        for (int i = 0; i < 9; i++)
        {
            CeldaCalendarioDTO celdaMiercoles = resultado.filas().get(i).celdas().get(2);
            assertThat(celdaMiercoles.esExamenDgt()).isTrue();
            assertThat(celdaMiercoles.estado()).isEqualTo("EXAMEN_DGT");
            assertThat(celdaMiercoles.esClicable()).isFalse();
        }
    }

    @Test
    @DisplayName("obtenerCalendarioAlumno anonimiza clases ajenas como 'Clase Práctica' y permite clic solo en clases propias")
    void testObtenerCalendarioAlumnoAnonimizacionRgpd()
    {
        Profesor profesor = new Profesor();
        profesor.setTurno(TipoTurno.MATINAL);
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setEstado(EstadoVehiculo.DISPONIBLE);
        profesor.setVehiculo(vehiculo);

        Alumno alumnoActual = new Alumno();
        ReflectionTestUtils.setField(alumnoActual, "id", 201L);
        alumnoActual.setNombre("Carlos");
        alumnoActual.setProfesor(profesor);

        Alumno otroAlumno = new Alumno();
        ReflectionTestUtils.setField(otroAlumno, "id", 202L);
        otroAlumno.setNombre("Marta");
        otroAlumno.setApellidos("Pérez");

        LocalDate lunes = LocalDate.of(2026, 10, 5);

        // Clase propia el lunes a las 08:00
        ClasePractica clasePropia = new ClasePractica();
        ReflectionTestUtils.setField(clasePropia, "id", 10L);
        clasePropia.setAlumno(alumnoActual);
        clasePropia.setProfesor(profesor);
        clasePropia.setFechaHora(lunes.atTime(8, 0));
        clasePropia.setEstadoClase(EstadoClase.PENDIENTE);

        // Clase de otro alumno el lunes a las 08:45
        ClasePractica claseAjena = new ClasePractica();
        ReflectionTestUtils.setField(claseAjena, "id", 11L);
        claseAjena.setAlumno(otroAlumno);
        claseAjena.setProfesor(profesor);
        claseAjena.setFechaHora(lunes.atTime(8, 45));
        claseAjena.setEstadoClase(EstadoClase.PENDIENTE);

        Matricula matricula = new Matricula();
        matricula.setSaldoClases(5);

        when(this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumnoActual))
                .thenReturn(Optional.of(matricula));
        when(this.clasePracticaRepository.countByAlumnoAndEstadoClase(alumnoActual, EstadoClase.PENDIENTE))
                .thenReturn(1);
        when(this.examenRepository.findByAlumnoProfesorOrderByFechaHoraDesc(profesor))
                .thenReturn(new ArrayList<>());
        when(this.clasePracticaRepository.findByProfesorOrderByFechaHoraAsc(profesor))
                .thenReturn(List.of(clasePropia, claseAjena));

        CalendarioSemanalDTO resultado = this.calendarioService.obtenerCalendarioAlumno(alumnoActual, lunes);

        // Celda propia (Fila 0, Día 0)
        CeldaCalendarioDTO celdaPropia = resultado.filas().get(0).celdas().get(0);
        assertThat(celdaPropia.titulo()).isEqualTo("Clase Práctica");
        assertThat(celdaPropia.esPropia()).isTrue();
        assertThat(celdaPropia.esClicable()).isTrue();
        assertThat(celdaPropia.modalUrl()).isEqualTo("/alumno/clases/10/modal");

        // Celda ajena (Fila 1, Día 0) -> RGPD anonimizado
        CeldaCalendarioDTO celdaAjena = resultado.filas().get(1).celdas().get(0);
        assertThat(celdaAjena.titulo()).isEqualTo("Clase Práctica");
        assertThat(celdaAjena.subtitulo()).isEqualTo("Horario Ocupado");
        assertThat(celdaAjena.esPropia()).isFalse();
        assertThat(celdaAjena.esClicable()).isFalse();
        assertThat(celdaAjena.modalUrl()).isNull();
    }
}
