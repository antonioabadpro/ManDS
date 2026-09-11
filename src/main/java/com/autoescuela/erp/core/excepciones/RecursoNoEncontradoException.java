package com.autoescuela.erp.core.excepciones;

/**
 * Excepción lanzada cuando no se localiza una entidad o recurso solicitado en el sistema.
 */
public class RecursoNoEncontradoException extends RuntimeException
{
    public RecursoNoEncontradoException(String mensaje)
    {
        super(mensaje);
    }
}
