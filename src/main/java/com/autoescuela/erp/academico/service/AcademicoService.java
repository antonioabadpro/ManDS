package com.autoescuela.erp.academico.service;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autoescuela.erp.academico.model.Matricula;
import com.autoescuela.erp.academico.repository.MatriculaRepository;
import com.autoescuela.erp.core.email.EmailService;
import com.autoescuela.erp.core.enums.EstadoUsuario;
import com.autoescuela.erp.core.enums.ModalidadMatricula;
import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.core.enums.TipoMatricula;
import com.autoescuela.erp.core.excepciones.ReglaNegocioException;
import com.autoescuela.erp.usuarios.model.Alumno;
import com.autoescuela.erp.usuarios.repository.AlumnoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AcademicoService
{
    private final AlumnoRepository alumnoRepository;
    private final MatriculaRepository matriculaRepository;
    private final EmailService emailService;

    /**
     * Formaliza la matrícula de un alumno tras la confirmación del pago en Stripe.
     * @param dniAlumno DNI del alumno.
     * @param tipoCarnet_string Tipo de carnet del alumno (permiso de conducir) en formato String.
     * @param importeTotal Importe total de la matrícula.
     * @return La matrícula formalizada.
     * @throws ReglaNegocioException Si el DNI del alumno o el tipo de carnet son inválidos o si el alumno no existe.
     */
    @Transactional
    public Matricula matricularTrasPago(String dniAlumno, String tipoCarnet_string, float importeTotal)
    {
        TipoCarnet tipoCarnet = null;
        if (dniAlumno == null || dniAlumno.isBlank())
        {
            throw new ReglaNegocioException("El DNI del alumno es obligatorio para formalizar la matrícula.");
        }

        Alumno alumno = this.alumnoRepository.findByDni(dniAlumno.trim().toUpperCase())
                .orElseThrow(() -> new ReglaNegocioException("NO se ha encontrado a ningún Alumno con el DNI: " + dniAlumno));

        // Activamos al alumno si su cuenta se encontraba en estado INACTIVO pendiente de formalización de pago
        if (alumno.getEstado() == EstadoUsuario.INACTIVO)
        {
            alumno.setEstado(EstadoUsuario.ACTIVO);
            this.alumnoRepository.save(alumno);
            log.info("Alumno con DNI {} activado formalmente tras confirmación de pago.", dniAlumno);
        }

        try
        {
            if (tipoCarnet_string == null || tipoCarnet_string.isBlank())
            {
                throw new ReglaNegocioException("El tipo de carnet es obligatorio para formalizar la matrícula.");
            }
            tipoCarnet = TipoCarnet.valueOf(tipoCarnet_string.trim().toUpperCase());
        }
        catch (IllegalArgumentException ex)
        {
            log.warn("Tipo de carnet no reconocido: '{}'. Se asigna PERMISO_B por defecto.", tipoCarnet_string);
        }

        // Idempotencia ante reintentos de Stripe o concurrencia: si el alumno ya tiene matrícula activa para este permiso, la retornamos
        Optional<Matricula> matriculaActivaOpt = this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno);
        if (matriculaActivaOpt.isPresent() && tipoCarnet != null)
        {
            Matricula matriculaActiva = matriculaActivaOpt.get();
            if (matriculaActiva.getPermisoCarnet() == tipoCarnet)
            {
                log.info("El alumno con DNI {} ya tiene matrícula activa para el carnet {}. Reutilizando matrícula existente (ID: {}).",
                        dniAlumno, tipoCarnet, matriculaActiva.getId());
                return matriculaActiva;
            }
        }

        Matricula matriculaGuardada = null;
        if(tipoCarnet != null)
        {
            Matricula matricula = new Matricula();
            matricula.setEstaActiva(true);
            matricula.setPermisoCarnet(tipoCarnet);
            matricula.setConvocatorias(2);
            matricula.setSaldoClases(0);
            matricula.setConvocatoriasGastadas(0);
            matricula.setPrecio(importeTotal);
            matricula.setFechaMatriculacion(LocalDate.now());
            matricula.setTipo(TipoMatricula.NUEVA);
            matricula.setModalidad(ModalidadMatricula.TEORICO_PRACTICA);
            matricula.setAlumno(alumno);

            matriculaGuardada = this.matriculaRepository.save(matricula);
            log.info("Matrícula formalizada exitosamente tras pago Stripe para Alumno DNI: {} y carnet: {}", dniAlumno, tipoCarnet);

            // Envío de correo de bienvenida y confirmación de matrícula oficial
            this.emailService.enviarBienvenidaAlumno(alumno.getCorreo(), alumno.getNombre(), tipoCarnet.getDescripcion(), importeTotal);
        }
        return matriculaGuardada;
    }
}
