package com.autoescuela.erp.core.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Catálogo oficial de tipos de permisos y licencias de conducción reconocidos por la DGT.
 */
@Getter
@RequiredArgsConstructor
public enum TipoCarnet
{
    PERMISO_AM("Permiso AM"),
    PERMISO_A1("Permiso A1"),
    PERMISO_A2("Permiso A2"),
    PERMISO_A("Permiso A"),
    PERMISO_B("Permiso B"),
    PERMISO_B_E("Permiso B+E"),
    PERMISO_B1("Permiso B1"),
    PERMISO_C("Permiso C"),
    PERMISO_C_E("Permiso C+E"),
    PERMISO_D("Permiso D"),
    PERMISO_D_E("Permiso D+E"),
    PERMISO_LVA("Permiso LVA");

    private final String descripcion;
}
