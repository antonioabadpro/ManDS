package com.autoescuela.erp.core.excepciones;

/**
 * Excepción lanzada cuando una operación vulnera una regla de negocio
 * (ej. saldo insuficiente, cupo de examen diario superado, cancelación fuera de plazo).
 */
public class ReglaNegocioException extends RuntimeException
{
    public ReglaNegocioException(String mensaje)
    {
        super(mensaje);
    }
}
