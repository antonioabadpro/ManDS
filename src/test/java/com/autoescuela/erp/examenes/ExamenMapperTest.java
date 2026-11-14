package com.autoescuela.erp.examenes;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.autoescuela.erp.academico.model.Matricula;
import com.autoescuela.erp.core.enums.EstadoSolicitud;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.enums.TipoExamen;
import com.autoescuela.erp.examenes.dto.ExamenResumenDTO;
import com.autoescuela.erp.examenes.dto.SolicitudExamenDashboardDTO;
import com.autoescuela.erp.examenes.mapper.ExamenMapper;
import com.autoescuela.erp.examenes.model.Examen;
import com.autoescuela.erp.examenes.model.SolicitudExamen;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.model.Profesor;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests Unitarios de ExamenMapper (MapStruct)")
class ExamenMapperTest
{
    private final ExamenMapper mapper = Mappers.getMapper(ExamenMapper.class);

    @Test
    @DisplayName("toSolicitudExamenDashboardDTO mapea alumno, profesor, permiso de matrícula, tipo y estado")
    void testToSolicitudExamenDashboardDTOCompleto()
    {
        Alumno alumno = new Alumno();
        alumno.setNombre("David");
        alumno.setApellidos("Ruiz");

        Profesor profesor = new Profesor();
        profesor.setNombre("Laura");
        profesor.setApellidos("Sánchez");

        Matricula matricula = new Matricula();
        matricula.setPermisoCarnet(TipoCarnet.PERMISO_B);

        SolicitudExamen solicitud = new SolicitudExamen();
        solicitud.setAlumno(alumno);
        solicitud.setProfesor(profesor);
        solicitud.setMatricula(matricula);
        solicitud.setTipo(TipoExamen.PRACTICO);
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);

        List<SolicitudExamenDashboardDTO> dtos = this.mapper.toSolicitudExamenDashboardDTOList(List.of(solicitud));

        assertThat(dtos).hasSize(1);
        SolicitudExamenDashboardDTO dto = dtos.get(0);
        assertThat(dto.alumnoNombreCompleto()).isEqualTo("David Ruiz");
        assertThat(dto.profesorNombreCompleto()).isEqualTo("Laura Sánchez");
        assertThat(dto.permisoCarnet()).isEqualTo(TipoCarnet.PERMISO_B);
        assertThat(dto.tipo()).isEqualTo(TipoExamen.PRACTICO);
        assertThat(dto.estado()).isEqualTo(EstadoSolicitud.PENDIENTE);
    }

    @Test
    @DisplayName("toSolicitudExamenDashboardDTO tolera profesor y matrícula nulos en solicitudes teóricas")
    void testToSolicitudExamenDashboardDTOSinProfesorNiMatricula()
    {
        Alumno alumno = new Alumno();
        alumno.setNombre("Lucía");
        alumno.setApellidos("Gómez");

        SolicitudExamen solicitud = new SolicitudExamen();
        solicitud.setAlumno(alumno);
        solicitud.setProfesor(null);
        solicitud.setMatricula(null);
        solicitud.setTipo(TipoExamen.TEORICO);
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);

        SolicitudExamenDashboardDTO dto = this.mapper.toSolicitudExamenDashboardDTO(solicitud);

        assertThat(dto.alumnoNombreCompleto()).isEqualTo("Lucía Gómez");
        assertThat(dto.profesorNombreCompleto()).isNull();
        assertThat(dto.permisoCarnet()).isNull();
        assertThat(dto.tipo()).isEqualTo(TipoExamen.TEORICO);
    }

    @Test
    @DisplayName("toExamenResumenDTO mapea acta oficial, alumno, DNI y permiso desde la solicitud")
    void testToExamenResumenDTOCompleto()
    {
        Alumno alumno = new Alumno();
        alumno.setNombre("Elena");
        alumno.setApellidos("Moreno");
        alumno.setDni("12345678Z");

        Matricula matricula = new Matricula();
        matricula.setPermisoCarnet(TipoCarnet.PERMISO_A2);

        SolicitudExamen solicitud = new SolicitudExamen();
        solicitud.setMatricula(matricula);

        Examen examen = new Examen();
        LocalDateTime fecha = LocalDateTime.of(2026, 11, 20, 10, 0);
        examen.setFechaHora(fecha);
        examen.setDuracion(45);
        examen.setTipo(TipoExamen.PRACTICO);
        examen.setEsApto(Boolean.TRUE);
        examen.setAlumno(alumno);
        examen.setSolicitudExamen(solicitud);

        List<ExamenResumenDTO> dtos = this.mapper.toExamenResumenDTOList(List.of(examen));

        assertThat(dtos).hasSize(1);
        ExamenResumenDTO dto = dtos.get(0);
        assertThat(dto.fechaHora()).isEqualTo(fecha);
        assertThat(dto.duracion()).isEqualTo(45);
        assertThat(dto.tipo()).isEqualTo(TipoExamen.PRACTICO);
        assertThat(dto.esApto()).isTrue();
        assertThat(dto.alumnoNombreCompleto()).isEqualTo("Elena Moreno");
        assertThat(dto.alumnoDni()).isEqualTo("12345678Z");
        assertThat(dto.permisoCarnet()).isEqualTo(TipoCarnet.PERMISO_A2);
    }

    @Test
    @DisplayName("toExamenResumenDTO devuelve valores seguros ante alumno o solicitud nulos")
    void testToExamenResumenDTOConNulos()
    {
        Examen examen = new Examen();
        examen.setTipo(TipoExamen.TEORICO);
        examen.setDuracion(60);

        ExamenResumenDTO dto = this.mapper.toExamenResumenDTO(examen);

        assertThat(dto.alumnoNombreCompleto()).isEmpty();
        assertThat(dto.alumnoDni()).isEmpty();
        assertThat(dto.permisoCarnet()).isNull();
    }
}
