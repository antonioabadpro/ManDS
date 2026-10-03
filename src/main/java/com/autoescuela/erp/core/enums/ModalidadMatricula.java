package com.autoescuela.erp.core.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ModalidadMatricula
{
    TEORICO_PRACTICA("Teórico-Práctica"),
    PRACTICA("Práctica"),
    INDIVIDUAL("Individual");

    private final String descripcion;
}
