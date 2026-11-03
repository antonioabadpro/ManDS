package com.autoescuela.erp.examenes.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autoescuela.erp.core.enums.TipoCarnet;
import com.autoescuela.erp.examenes.model.CupoExamen;

public interface CupoExamenRepository extends JpaRepository<CupoExamen, Long>
{
    /**
     * Recupera todos los cupos fijados para un mes determinado (normalizado a día 1).
     */
    List<CupoExamen> findByMes(LocalDate mes);

    /**
     * Recupera el cupo fijado para un permiso específico en un mes determinado.
     */
    Optional<CupoExamen> findByMesAndTipoPermiso(LocalDate mes, TipoCarnet tipoPermiso);

    /**
     * Determina si ya existen cupos fijados para un mes determinado.
     */
    boolean existsByMes(LocalDate mes);
}
