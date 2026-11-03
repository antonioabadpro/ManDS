package com.autoescuela.erp.examenes.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autoescuela.erp.examenes.model.FechaExamen;

public interface FechaExamenRepository extends JpaRepository<FechaExamen, Long>
{
    /**
     * Busca las fechas oficiales fijadas para un mes determinado (normalizado a día 1).
     */
    Optional<FechaExamen> findByMes(LocalDate mes);

    /**
     * Determina si ya existen fechas fijadas e inmutables para un mes determinado.
     */
    boolean existsByMes(LocalDate mes);
}
