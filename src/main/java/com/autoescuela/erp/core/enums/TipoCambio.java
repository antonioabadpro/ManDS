package com.autoescuela.erp.core.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TipoCambio
{
    MANUAL("Manual"),
    AUTOMATICO("Automático");

    private final String descripcion;
}
