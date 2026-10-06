package com.autoescuela.erp.usuarios;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.test.util.ReflectionTestUtils;

import com.autoescuela.erp.academico.model.Matricula;
import com.autoescuela.erp.core.enums.EstadoClase;
import com.autoescuela.erp.core.enums.EstadoUsuario;
import com.autoescuela.erp.core.enums.ModalidadMatricula;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.enums.TipoExamen;
import com.autoescuela.erp.core.enums.TipoTurno;
import com.autoescuela.erp.examenes.model.Examen;
import com.autoescuela.erp.flota.model.Vehiculo;
import com.autoescuela.erp.practicas.model.ClasePractica;
import com.autoescuela.erp.usuarios.dto.AlumnoDetalleDTO;
import com.autoescuela.erp.usuarios.dto.AlumnoExpedienteDTO;
import com.autoescuela.erp.usuarios.dto.AlumnoResumenDTO;
import com.autoescuela.erp.usuarios.dto.ClasePracticaExpedienteDTO;
import com.autoescuela.erp.usuarios.dto.EditarPerfilAlumnoDTO;
import com.autoescuela.erp.usuarios.dto.ExamenExpedienteDTO;
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

    @Test
    @DisplayName("toAlumnoDetalleDTO mapea correctamente alumno con todos sus datos, profesor, vehículo y matrícula activa")
    void testToAlumnoDetalleDTOCompleto()
    {
        Vehiculo vehiculo = new Vehiculo();
        ReflectionTestUtils.setField(vehiculo, "id", 50L);
        vehiculo.setMatricula("7890-XYZ");
        vehiculo.setMarca("Peugeot");
        vehiculo.setModelo("208 PureTech");
        vehiculo.setTipoPermiso(TipoCarnet.PERMISO_B);

        Profesor profesor = new Profesor();
        ReflectionTestUtils.setField(profesor, "id", 7L);
        profesor.setNombre("Carlos");
        profesor.setApellidos("Martínez León");
        profesor.setTurno(TipoTurno.MATINAL);
        profesor.setVehiculo(vehiculo);

        Alumno alumno = new Alumno();
        ReflectionTestUtils.setField(alumno, "id", 15L);
        alumno.setNombre("Elena");
        alumno.setApellidos("Martínez López");
        alumno.setNombreUsuario("elena.alumno");
        alumno.setDni("12345678Z");
        alumno.setCorreo("elena.alumno@autoescuela.es");
        alumno.setTelefono("600123456");
        alumno.setDireccion("Calle Mayor 1, Madrid");
        alumno.setFechaNacimiento(LocalDate.of(2000, 4, 15));
        alumno.setEstado(EstadoUsuario.ACTIVO);
        alumno.setProfesor(profesor);

        Matricula matricula = new Matricula();
        ReflectionTestUtils.setField(matricula, "id", 200L);
        matricula.setEstaActiva(true);
        matricula.setPermisoCarnet(TipoCarnet.PERMISO_B);
        matricula.setFechaMatriculacion(LocalDate.now().minusYears(1).minusMonths(2));

        AlumnoDetalleDTO dto = this.mapper.toAlumnoDetalleDTO(alumno, matricula);

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(15L);
        assertThat(dto.nombre()).isEqualTo("Elena");
        assertThat(dto.apellidos()).isEqualTo("Martínez López");
        assertThat(dto.nombreCompleto()).isEqualTo("Elena Martínez López");
        assertThat(dto.iniciales()).isEqualTo("EM");
        assertThat(dto.nombreUsuario()).isEqualTo("elena.alumno");
        assertThat(dto.estado()).isEqualTo(EstadoUsuario.ACTIVO);
        assertThat(dto.dni()).isEqualTo("12345678Z");
        assertThat(dto.fechaNacimiento()).isEqualTo(LocalDate.of(2000, 4, 15));
        assertThat(dto.edad()).isGreaterThanOrEqualTo(23);
        assertThat(dto.direccion()).isEqualTo("Calle Mayor 1, Madrid");
        assertThat(dto.correo()).isEqualTo("elena.alumno@autoescuela.es");
        assertThat(dto.telefono()).isEqualTo("600123456");
        assertThat(dto.tieneMatriculaActiva()).isTrue();
        assertThat(dto.fechaMatriculacion()).isEqualTo(matricula.getFechaMatriculacion());
        assertThat(dto.anioMatriculacion()).isEqualTo(matricula.getFechaMatriculacion().getYear());
        assertThat(dto.antiguedadAnios()).isEqualTo(1);
        assertThat(dto.antiguedadTexto()).isEqualTo("1 año");
        assertThat(dto.turno()).isEqualTo(TipoTurno.MATINAL);
        assertThat(dto.profesorId()).isEqualTo(7L);
        assertThat(dto.profesorNombre()).isEqualTo("Carlos Martínez León");
        assertThat(dto.vehiculoId()).isEqualTo(50L);
        assertThat(dto.vehiculoMatricula()).isEqualTo("7890-XYZ");
        assertThat(dto.vehiculoModelo()).isEqualTo("Peugeot 208 PureTech");
        assertThat(dto.vehiculoTipoDescripcion()).isEqualTo("Permiso B");
        assertThat(dto.tipoCarnet()).isEqualTo(TipoCarnet.PERMISO_B);
        assertThat(dto.tipoCarnetDescripcion()).isEqualTo("Permiso B");
    }

    @Test
    @DisplayName("toAlumnoDetalleDTO maneja alumno sin profesor ni matrícula activa de forma segura")
    void testToAlumnoDetalleDTOSinProfesorNiMatricula()
    {
        Alumno alumno = new Alumno();
        ReflectionTestUtils.setField(alumno, "id", 20L);
        alumno.setNombre("Silvia");
        alumno.setApellidos("Montesinos Ruiz");
        alumno.setNombreUsuario("silvia.alumno");
        alumno.setDni("23242526H");
        alumno.setCorreo("silvia.alumno@autoescuela.es");
        alumno.setTelefono("600123019");
        alumno.setDireccion("Calle Alcalá 20, Madrid");
        alumno.setFechaNacimiento(LocalDate.of(2004, 6, 16));
        alumno.setEstado(EstadoUsuario.ACTIVO);

        AlumnoDetalleDTO dto = this.mapper.toAlumnoDetalleDTO(alumno, null);

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(20L);
        assertThat(dto.nombreCompleto()).isEqualTo("Silvia Montesinos Ruiz");
        assertThat(dto.iniciales()).isEqualTo("SM");
        assertThat(dto.tieneMatriculaActiva()).isFalse();
        assertThat(dto.fechaMatriculacion()).isNull();
        assertThat(dto.anioMatriculacion()).isNull();
        assertThat(dto.antiguedadAnios()).isZero();
        assertThat(dto.antiguedadTexto()).isEqualTo("Sin antigüedad");
        assertThat(dto.turno()).isNull();
        assertThat(dto.profesorId()).isNull();
        assertThat(dto.profesorNombre()).isNull();
        assertThat(dto.vehiculoId()).isNull();
        assertThat(dto.vehiculoMatricula()).isNull();
        assertThat(dto.vehiculoModelo()).isNull();
        assertThat(dto.vehiculoTipoDescripcion()).isNull();
        assertThat(dto.tipoCarnet()).isNull();
        assertThat(dto.tipoCarnetDescripcion()).isNull();
    }

    @Test
    @DisplayName("toClasePracticaExpedienteDTO mapea correctamente clase con profesor y kilometraje formateado")
    void testToClasePracticaExpedienteDTO()
    {
        Profesor profesor = new Profesor();
        profesor.setNombre("Laura");
        profesor.setApellidos("Sánchez Romero");

        ClasePractica clase = new ClasePractica();
        ReflectionTestUtils.setField(clase, "id", 101L);
        clase.setFechaHora(LocalDateTime.of(2026, 9, 10, 10, 0));
        clase.setDuracion(45);
        clase.setPuntoRecogida("Calle Alcalá 45");
        clase.setKmInicio(44955);
        clase.setKmFin(45000);
        clase.setEstadoClase(EstadoClase.RECIBIDA);
        clase.setObservaciones("Excelente dominio del embrague");
        clase.setProfesor(profesor);

        ClasePracticaExpedienteDTO dto = this.mapper.toClasePracticaExpedienteDTO(clase);

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(101L);
        assertThat(dto.fechaHora()).isEqualTo(LocalDateTime.of(2026, 9, 10, 10, 0));
        assertThat(dto.duracion()).isEqualTo(45);
        assertThat(dto.puntoRecogida()).isEqualTo("Calle Alcalá 45");
        assertThat(dto.kmFormateado()).contains("44955").contains("45000");
        assertThat(dto.estadoClase()).isEqualTo(EstadoClase.RECIBIDA);
        assertThat(dto.profesorNombre()).isEqualTo("Laura Sánchez Romero");
        assertThat(dto.observaciones()).isEqualTo("Excelente dominio del embrague");
    }

    @Test
    @DisplayName("toExamenExpedienteDTO mapea correctamente examen DGT con resultado y centro oficial")
    void testToExamenExpedienteDTO()
    {
        Examen examen = new Examen();
        ReflectionTestUtils.setField(examen, "id", 201L);
        examen.setTipo(TipoExamen.TEORICO);
        examen.setFechaHora(LocalDateTime.of(2026, 7, 20, 9, 0));
        examen.setDuracion(30);
        examen.setEsApto(true);

        ExamenExpedienteDTO dto = this.mapper.toExamenExpedienteDTO(examen);

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(201L);
        assertThat(dto.tipo()).isEqualTo(TipoExamen.TEORICO);
        assertThat(dto.titulo()).isEqualTo("Examen Teórico Oficial");
        assertThat(dto.duracion()).isEqualTo(30);
        assertThat(dto.esApto()).isTrue();
        assertThat(dto.centroDgt()).isEqualTo("Centro DGT Móstoles");
    }

    @Test
    @DisplayName("toAlumnoExpedienteDTO agrupa métricas, clases y exámenes del alumno")
    void testToAlumnoExpedienteDTO()
    {
        Alumno alumno = new Alumno();
        ReflectionTestUtils.setField(alumno, "id", 5L);
        alumno.setNombre("Elena");
        alumno.setApellidos("Martínez López");

        Matricula matricula = new Matricula();
        matricula.setPermisoCarnet(TipoCarnet.PERMISO_B);
        matricula.setSaldoClases(5);
        matricula.setConvocatorias(2);

        ClasePracticaExpedienteDTO claseDto = new ClasePracticaExpedienteDTO(
                1L, LocalDateTime.now(), 45, "Calle Alcalá 45", 44955, 45000,
                "Km: 44.955 a 45.000", EstadoClase.RECIBIDA, "Laura Sánchez", "OK"
        );
        ExamenExpedienteDTO examenDto = new ExamenExpedienteDTO(
                1L, TipoExamen.TEORICO, "Examen Teórico Oficial", LocalDateTime.now(), 30, true, "Centro DGT Móstoles"
        );

        AlumnoExpedienteDTO expediente = this.mapper.toAlumnoExpedienteDTO(
                alumno, matricula, 1L, List.of(claseDto), List.of(examenDto)
        );

        assertThat(expediente).isNotNull();
        assertThat(expediente.id()).isEqualTo(5L);
        assertThat(expediente.nombreCompleto()).isEqualTo("Elena Martínez López");
        assertThat(expediente.tipoCarnet()).isEqualTo(TipoCarnet.PERMISO_B);
        assertThat(expediente.tipoCarnetDescripcion()).isEqualTo("Permiso B");
        assertThat(expediente.tieneMatriculaActiva()).isTrue();
        assertThat(expediente.saldoClases()).isEqualTo(5);
        assertThat(expediente.clasesRealizadas()).isEqualTo(1L);
        assertThat(expediente.convocatoriasRestantes()).isEqualTo(2);
        assertThat(expediente.ultimasClases()).hasSize(1);
        assertThat(expediente.ultimosExamenes()).hasSize(1);
    }
}
