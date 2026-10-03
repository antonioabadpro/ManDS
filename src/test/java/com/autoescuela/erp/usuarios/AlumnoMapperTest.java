package com.autoescuela.erp.usuarios;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.test.util.ReflectionTestUtils;

import com.autoescuela.erp.academico.model.Matricula;
import com.autoescuela.erp.core.enums.EstadoUsuario;
import com.autoescuela.erp.core.enums.ModalidadMatricula;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.enums.TipoTurno;
import com.autoescuela.erp.usuarios.dto.AlumnoResumenDTO;
import com.autoescuela.erp.usuarios.dto.EditarPerfilAlumnoDTO;
import com.autoescuela.erp.usuarios.mapper.AlumnoMapper;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.model.Profesor;

import static org.assertj.core.api.Assertions.assertThat;

class AlumnoMapperTest
{
    private final AlumnoMapper mapper = Mappers.getMapper(AlumnoMapper.class);

    @Test
    @DisplayName("toAlumnoResumenDTO mapea correctamente alumno con profesor y matrícula activa")
    void testToAlumnoResumenDTOCompleto()
    {
        Profesor profesor = new Profesor();
        ReflectionTestUtils.setField(profesor, "id", 10L);
        profesor.setNombre("Carlos");
        profesor.setApellidos("Martínez León");
        profesor.setTurno(TipoTurno.MATINAL);

        Alumno alumno = new Alumno();
        ReflectionTestUtils.setField(alumno, "id", 1L);
        alumno.setNombre("Alejandro");
        alumno.setApellidos("Blanco Gil");
        alumno.setDni("12345678A");
        alumno.setCorreo("alejandro.alumno@autoescuela.es");
        alumno.setFechaNacimiento(LocalDate.now().minusYears(22));
        alumno.setEstado(EstadoUsuario.ACTIVO);
        alumno.setProfesor(profesor);

        Matricula matricula = new Matricula();
        ReflectionTestUtils.setField(matricula, "id", 100L);
        matricula.setPermisoCarnet(TipoCarnet.PERMISO_B);
        matricula.setModalidad(ModalidadMatricula.PRACTICA);
        matricula.setSaldoClases(5);
        matricula.setConvocatorias(2);

        AlumnoResumenDTO dto = this.mapper.toAlumnoResumenDTO(alumno, matricula, 1);

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.nombre()).isEqualTo("Alejandro");
        assertThat(dto.apellidos()).isEqualTo("Blanco Gil");
        assertThat(dto.nombreCompleto()).isEqualTo("Alejandro Blanco Gil");
        assertThat(dto.iniciales()).isEqualTo("AB");
        assertThat(dto.edad()).isEqualTo(22);
        assertThat(dto.dni()).isEqualTo("12345678A");
        assertThat(dto.correo()).isEqualTo("alejandro.alumno@autoescuela.es");
        assertThat(dto.estado()).isEqualTo(EstadoUsuario.ACTIVO);
        assertThat(dto.profesorId()).isEqualTo(10L);
        assertThat(dto.profesorNombre()).isEqualTo("Carlos Martínez León");
        assertThat(dto.profesorTurno()).isEqualTo(TipoTurno.MATINAL);
        assertThat(dto.tieneMatriculaActiva()).isTrue();
        assertThat(dto.tipoCarnet()).isEqualTo(TipoCarnet.PERMISO_B);
        assertThat(dto.modalidadDescripcion()).isEqualTo(ModalidadMatricula.PRACTICA.getDescripcion());
        assertThat(dto.saldoClases()).isEqualTo(5);
        assertThat(dto.clasesPendientes()).isEqualTo(1);
        assertThat(dto.convocatoriasRestantes()).isEqualTo(2);
    }

    @Test
    @DisplayName("toAlumnoResumenDTO maneja alumno sin profesor ni matrícula")
    void testToAlumnoResumenDTOSinProfesorNiMatricula()
    {
        Alumno alumno = new Alumno();
        ReflectionTestUtils.setField(alumno, "id", 2L);
        alumno.setNombre("Carlos");
        alumno.setApellidos("Ibáñez Méndez");
        alumno.setDni("87654321B");
        alumno.setFechaNacimiento(LocalDate.now().minusYears(25));
        alumno.setEstado(EstadoUsuario.ACTIVO);

        AlumnoResumenDTO dto = this.mapper.toAlumnoResumenDTO(alumno, null, 0);

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(2L);
        assertThat(dto.nombreCompleto()).isEqualTo("Carlos Ibáñez Méndez");
        assertThat(dto.iniciales()).isEqualTo("CI");
        assertThat(dto.edad()).isEqualTo(25);
        assertThat(dto.profesorId()).isNull();
        assertThat(dto.profesorNombre()).isNull();
        assertThat(dto.profesorTurno()).isNull();
        assertThat(dto.tieneMatriculaActiva()).isFalse();
        assertThat(dto.tipoCarnet()).isNull();
        assertThat(dto.modalidadDescripcion()).isNull();
        assertThat(dto.saldoClases()).isNull();
        assertThat(dto.clasesPendientes()).isZero();
        assertThat(dto.convocatoriasRestantes()).isNull();
    }

    @Test
    @DisplayName("repoblarPerfilDTO combina datos editados con datos contractuales de solo lectura")
    void testRepoblarPerfilDTO()
    {
        Profesor profesor = new Profesor();
        profesor.setNombre("Elena");
        profesor.setApellidos("Gómez Varela");
        profesor.setTelefono("622334455");
        profesor.setCorreo("elena.profesor@autoescuela.es");

        Alumno alumno = new Alumno();
        ReflectionTestUtils.setField(alumno, "id", 5L);
        alumno.setDni("44556677C");
        alumno.setNombreUsuario("alumno_test");
        alumno.setProfesor(profesor);

        Matricula matricula = new Matricula();
        matricula.setPermisoCarnet(TipoCarnet.PERMISO_B);
        matricula.setFechaMatriculacion(LocalDate.of(2026, 1, 10));
        matricula.setSaldoClases(8);
        matricula.setConvocatorias(2);

        EditarPerfilAlumnoDTO formDTO = new EditarPerfilAlumnoDTO(
                null,
                "NombreEditado",
                "ApellidoEditado",
                "611000111",
                "Calle Nueva 12",
                "nuevo@correo.com",
                LocalDate.of(2000, 5, 20),
                null, null, null, null, null, null, null, null, null, null, null
        );

        EditarPerfilAlumnoDTO repoblado = this.mapper.repoblarPerfilDTO(alumno, matricula, formDTO);

        assertThat(repoblado).isNotNull();
        // Campos editables procedentes del formulario
        assertThat(repoblado.nombre()).isEqualTo("NombreEditado");
        assertThat(repoblado.apellidos()).isEqualTo("ApellidoEditado");
        assertThat(repoblado.telefono()).isEqualTo("611000111");
        assertThat(repoblado.direccion()).isEqualTo("Calle Nueva 12");
        assertThat(repoblado.correo()).isEqualTo("nuevo@correo.com");
        assertThat(repoblado.fechaNacimiento()).isEqualTo(LocalDate.of(2000, 5, 20));

        // Campos inmutables procedentes de la entidad
        assertThat(repoblado.id()).isEqualTo(5L);
        assertThat(repoblado.dni()).isEqualTo("44556677C");
        assertThat(repoblado.nombreUsuario()).isEqualTo("alumno_test");
        assertThat(repoblado.profesorNombre()).isEqualTo("Elena Gómez Varela");
        assertThat(repoblado.profesorTelefono()).isEqualTo("622334455");
        assertThat(repoblado.profesorEmail()).isEqualTo("elena.profesor@autoescuela.es");
        assertThat(repoblado.permisoActual()).isEqualTo(TipoCarnet.PERMISO_B);
        assertThat(repoblado.fechaMatriculacion()).isEqualTo(LocalDate.of(2026, 1, 10));
        assertThat(repoblado.saldoClases()).isEqualTo(8);
        assertThat(repoblado.convocatoriasRestantes()).isEqualTo(2);
    }
}
