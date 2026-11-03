package com.autoescuela.erp.estadisticas.dto;

/**
 * DTO para la tarjeta de clases prácticas previas al aprobado del profesor (Opción D).
 * Calcula la media, el mínimo y el máximo de clases prácticas que necesitaron los alumnos para aprobar.
 */
public record ClasesPreviasAprobadoProfesorDTO(
    double mediaClases,
    int minClases,
    int maxClases,
    long totalAlumnosAprobados
)
{
    public ClasesPreviasAprobadoProfesorDTO()
    {
        this(0.0, 0, 0, 0);
    }
}
