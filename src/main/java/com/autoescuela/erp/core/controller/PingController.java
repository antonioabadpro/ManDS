package com.autoescuela.erp.core.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST ultraligero para monitorización de salud y keep-alive (UptimeRobot).
 * Responde de forma inmediata sin consultar la base de datos ni crear sesiones HTTP,
 * evitando que la instancia en la nube se suspenda por inactividad.
 */
@RestController
public class PingController
{
    @GetMapping("/ping")
    public ResponseEntity<String> ping()
    {
        return ResponseEntity.ok("pong");
    }
}
