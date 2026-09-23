package com.autoescuela.erp.core.excepciones;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Capturador global de excepciones para la capa de presentación MVC y HTMX.
 * Encamina los errores controlados y no controlados a las vistas dedicadas en templates/error/.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler
{
    /**
     * Gestiona las excepciones de recursos no encontrados (404).
     */
    @ExceptionHandler({RecursoNoEncontradoException.class, NoResourceFoundException.class, NoHandlerFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String manejarRecursoNoEncontrado(Exception ex, HttpServletRequest request, Model model)
    {
        log.warn("Recurso no encontrado en {}: {}", request.getRequestURI(), ex.getMessage());
        model.addAttribute("status", 404);
        model.addAttribute("error", "Recurso no encontrado");
        model.addAttribute("message", ex.getMessage() != null && !ex.getMessage().isBlank()
                ? ex.getMessage()
                : "La página o el recurso solicitado no existe o ha sido movido.");
        model.addAttribute("path", request.getRequestURI());
        model.addAttribute("timestamp", LocalDateTime.now());
        return "error/404";
    }

    /**
     * Gestiona accesos denegados y permisos insuficientes (403).
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String manejarAccesoDenegado(AccessDeniedException ex, HttpServletRequest request, Model model)
    {
        log.warn("Acceso denegado a {}: {}", request.getRequestURI(), ex.getMessage());
        model.addAttribute("status", 403);
        model.addAttribute("error", "Acceso denegado");
        model.addAttribute("message", "No dispones de los permisos necesarios para acceder a esta sección del sistema.");
        model.addAttribute("path", request.getRequestURI());
        model.addAttribute("timestamp", LocalDateTime.now());
        return "error/403";
    }

    /**
     * Gestiona infracciones de reglas de negocio globales no interceptadas en formularios locales (422).
     */
    @ExceptionHandler(ReglaNegocioException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public String manejarReglaNegocio(ReglaNegocioException ex, HttpServletRequest request, Model model)
    {
        log.warn("Infracción de regla de negocio en {}: {}", request.getRequestURI(), ex.getMessage());
        model.addAttribute("status", 422);
        model.addAttribute("error", "Regla de Negocio");
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("path", request.getRequestURI());
        model.addAttribute("timestamp", LocalDateTime.now());
        return "error/error-negocio";
    }

    /**
     * Captura cualquier error o fallo interno inesperado del servidor (500).
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String manejarExcepcionGenerica(Exception ex, HttpServletRequest request, Model model)
    {
        log.error("Error no controlado en el servidor al procesar {}: ", request.getRequestURI(), ex);
        model.addAttribute("status", 500);
        model.addAttribute("error", "Error Interno del Servidor");
        model.addAttribute("message", "Se ha producido un error inesperado al procesar la solicitud. El equipo técnico ha sido notificado.");
        model.addAttribute("path", request.getRequestURI());
        model.addAttribute("timestamp", LocalDateTime.now());
        return "error/500";
    }
}

