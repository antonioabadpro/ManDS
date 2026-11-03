package com.autoescuela.erp.core.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TipoCombustible
{
    GASOLINA("Gasolina"),
    DIESEL("Diésel"),
    ELECTRICO("Eléctrico"),
    HIBRIDO("Híbrido");

    private final String descripcion;
}
