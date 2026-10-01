package com.autoescuela.erp.examenes.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autoescuela.erp.examenes.model.SolicitudExamen;
import com.autoescuela.erp.examenes.repository.SolicitudExamenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExamenService
{
    private final SolicitudExamenRepository solicitudExamenRepository;

    /**
     * Recupera las solicitudes de examen más recientes para el resumen rápido del Dashboard de Administración.
     */
    @Transactional(readOnly = true)
    public List<SolicitudExamen> obtenerSolicitudesExamenDashboard()
    {
        return this.solicitudExamenRepository.findTop5ByOrderByIdDesc();
    }
}
