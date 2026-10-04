package com.autoescuela.erp.academico.service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
    // Registro de sesiones de Stripe ya procesadas para garantizar idempotencia entre retorno y webhook
    private final Set<String> sesionesProcesadas = Collections.newSetFromMap(new ConcurrentHashMap<>());

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
            tipoCarnet = TipoCarnet.PERMISO_B;
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
            throw new ReglaNegocioException("El alumno ya dispone de una matrícula activa para el "
                    + matriculaActiva.getPermisoCarnet().getDescripcion()
                    + ". No es posible cursar dos permisos de conducir simultáneamente.");
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

    /**
     * Recarga el saldo de clases prácticas del expediente de matrícula activo de un alumno tras confirmación de pago en Stripe.
     * Garantiza idempotencia ante reintentos concurrentes del webhook y redirección del navegador mediante sessionId.
     *
     * @param dniAlumno DNI del alumno titular.
     * @param numeroClases Número de clases prácticas adquiridas.
     * @param sessionId Identificador de la sesión de Stripe (opcional, para idempotencia).
     * @return Matrícula actualizada con el nuevo saldo de clases prácticas.
     * @throws ReglaNegocioException Si el alumno o su matrícula activa no existen.
     */
    @Transactional
    public Matricula recargarSaldoClasesTrasPago(String dniAlumno, int numeroClases, String sessionId)
    {
        if (dniAlumno == null || dniAlumno.isBlank())
        {
            throw new ReglaNegocioException("El DNI del alumno es obligatorio para recargar saldo de clases.");
        }
        if (numeroClases <= 0)
        {
            throw new ReglaNegocioException("El número de clases a recargar debe ser superior a cero.");
        }

        Alumno alumno = this.alumnoRepository.findByDni(dniAlumno.trim().toUpperCase())
                .orElseThrow(() -> new ReglaNegocioException("NO se ha encontrado a ningún Alumno con el DNI: " + dniAlumno));

        Matricula matricula = this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno)
                .orElseThrow(() -> new ReglaNegocioException("No se encontró ningún expediente de matrícula activo para el alumno con DNI: " + dniAlumno));

        if (matricula.tieneConvocatoriasAgotadas())
        {
            throw new ReglaNegocioException("Has agotado las convocatorias oficiales de examen de tu matrícula. Debes tramitar la renovación de tu matrícula antes de adquirir más clases prácticas.");
        }

        // Idempotencia: si la sesión de Stripe ya fue procesada (por webhook o retorno), retornamos el estado actual sin duplicar
        if (sessionId != null && !sessionId.isBlank())
        {
            if (!this.sesionesProcesadas.add(sessionId))
            {
                log.info("La sesión de Stripe {} ya fue procesada previamente para el alumno DNI {}. Evitando recarga duplicada.", sessionId, dniAlumno);
                return matricula;
            }
        }

        int saldoAnterior = matricula.getSaldoClases() != null ? matricula.getSaldoClases() : 0;
        matricula.setSaldoClases(saldoAnterior + numeroClases);
        Matricula guardada = this.matriculaRepository.save(matricula);

        log.info("Saldo de clases recargado exitosamente para Alumno DNI: {}. Clases añadidas: {}. Nuevo saldo total: {}",
                dniAlumno, numeroClases, guardada.getSaldoClases());

        return guardada;
    }

    /**
     * Renueva la matrícula de un alumno tras la confirmación del pago en Stripe.
     * Restablece las 2 convocatorias oficiales a examen, actualiza el tipo a RENOVACION
     * y garantiza idempotencia ante webhooks y retornos concurrentes.
     *
     * @param dniAlumno DNI del alumno titular.
     * @param sessionId Identificador de la sesión de Stripe (para idempotencia).
     * @param importeTotal Importe total abonado en la renovación.
     * @return Matrícula renovada.
     */
    @Transactional
    public Matricula renovarMatriculaTrasPago(String dniAlumno, String sessionId, float importeTotal)
    {
        if (dniAlumno == null || dniAlumno.isBlank())
        {
            throw new ReglaNegocioException("El DNI del alumno es obligatorio para renovar la matrícula.");
        }

        Alumno alumno = this.alumnoRepository.findByDni(dniAlumno.trim().toUpperCase())
                .orElseThrow(() -> new ReglaNegocioException("NO se ha encontrado a ningún Alumno con el DNI: " + dniAlumno));

        Matricula matricula = this.matriculaRepository.findByAlumnoAndEstaActivaTrue(alumno)
                .orElseThrow(() -> new ReglaNegocioException("No se encontró ningún expediente de matrícula activo para el alumno con DNI: " + dniAlumno));

        // Idempotencia: evitamos duplicar la renovación si webhook y retorno coinciden
        if (sessionId != null && !sessionId.isBlank())
        {
            if (!this.sesionesProcesadas.add(sessionId))
            {
                log.info("La sesión de renovación de Stripe {} ya fue procesada previamente para el alumno DNI {}. Evitando duplicación.", sessionId, dniAlumno);
                return matricula;
            }
        }

        matricula.setConvocatorias(2);
        matricula.setTipo(TipoMatricula.RENOVACION);
        matricula.setPrecio(importeTotal);

        Matricula matriculaGuardada = this.matriculaRepository.save(matricula);
        log.info("Matrícula renovada exitosamente tras pago Stripe para Alumno DNI: {}. Convocatorias disponibles: 2, Tipo: RENOVACION, Importe: {} €",
                dniAlumno, importeTotal);

        return matriculaGuardada;
    }
}
