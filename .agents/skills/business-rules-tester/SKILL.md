---
name: business-rules-tester
description: >-
  Diseño y ejecución de baterías de pruebas automatizadas enfocadas en reglas de negocio estrictas (JUnit 5 + Mockito).
  Usar al escribir o adaptar pruebas unitarias de servicios, validación de invariantes de dominio, control de excepciones
  o verificación de cobertura tras cambios en reglas de negocio.
metadata:
  author: ManDS
  version: "1.0.0"
---

# Business Rules Tester (JUnit 5 + Mockito)

Esta skill establece las directrices normativas, patrones de prueba y protocolos de aserción para la verificación exhaustiva de la lógica de negocio y las invariantes de dominio del ERP de la autoescuela.

---

## 1. Pirámide de Testing y Foco Unitario

1. **Pruebas Unitarias de Servicios y Utilidades (Base de la Pirámide):**
   * **Prohibido `@SpringBootTest`** en tests de servicios, mappers o clases utilitarias. Levantar el contexto Spring para probar lógica de negocio introduce una sobrecarga inadmisible de tiempo de ejecución.
   * Emplear exclusivamente **JUnit 5 + Mockito** con `@ExtendWith(MockitoExtension.class)`, `@Mock` para dependencias y `@InjectMocks` para la clase bajo prueba.
2. **Pruebas Web sin Acoplamiento a Plantillas HTML:**
   * En controladores web con MockMvc, las aserciones se limitan estrictamente a:
     1. Código de estado HTTP (`status().isOk()`, `status().is3xxRedirection()`).
     2. Nombre de vista o redirección (`view().name(...)`, `redirectedUrl(...)`).
     3. Presencia de atributos en el modelo (`model().attributeExists(...)`).
   * **Prohibido validar texto plano o etiquetas HTML estáticas** en Thymeleaf (`content().string(containsString(...))`). Los cambios estéticos no deben romper la suite de pruebas.
3. **Reutilización del Contexto en Pruebas de Integración:**
   * Todo test de integración que requiera base de datos o contexto completo debe extender obligatoriamente de `com.autoescuela.erp.BaseIntegrationTest`.
   * Prohibido declarar `@TestPropertySource` o `@MockitoBean` en clases hijas para evitar invalidar el *Spring Test Context Caching*.

---

## 2. Doctrina Anti-Test-Lock-In (Regla Suprema)

Los cambios de negocio o arquitectura introducidos por el desarrollador representan la nueva intención oficial del sistema. Queda **TERMINANTEMENTE PROHIBIDO** revertir o alterar su lógica para "poner los tests en verde".

### Protocolo de Actuación ante Fallo de Tests:
1. **Analizar la Causa Raíz:** Comprobar si el fallo se debe a que el test esperaba el comportamiento antiguo que el desarrollador ha decidido reemplazar o refactorizar.
2. **Adaptar las Expectativas del Test:** Si el cambio es deliberado, actualizar las aserciones (`assertEquals`, `assertThrows`), los stubs de mocks (`when(...)`) y el dataset de prueba para reflejar la nueva especificación.
3. **Corrección de Bugs Colaterales:** Si el fallo es un error técnico imprevisto (ej. `NullPointerException`, índice fuera de límites, error de sintaxis), corregir el detalle técnico respetando al 100% el nuevo diseño del desarrollador.

---

## 3. Catálogo de Invariantes Críticas a Verificar

Cualquier funcionalidad o servicio que toque las siguientes áreas debe contar con pruebas unitarias que cubran tanto el camino feliz como los límites defensivos:

| Invariante de Dominio | Condición / Regla | Excepción o Comportamiento Esperado |
| :--- | :--- | :--- |
| **Capacidad de Reserva** | $\text{saldoClases} - \text{clasesReservadasPendientes} \le 0$ | Lanza `ReglaNegocioException("Capacidad insuficiente")`. |
| **Compra de Clases / Bonos** | Alumno con saldo remanente ($\text{saldoClases} > 0$) | Lanza `ReglaNegocioException`. Solo se permite comprar con saldo 0. |
| **Festivos Nacionales** | Reserva en 1 ene, 6 ene, 1 may, 15 ago, 12 oct, 1 nov, 6 dic, 8 dic, 25 dic o Semana Santa | Lanza `ReglaNegocioException`. Doble barrera con `FestivosCalendarioUtil`. |
| **Vacaciones de Centro** | Reserva del 1 al 14 de agosto o del 23 de dic al 7 de enero | Lanza `ReglaNegocioException`. Franjas no hábiles. |
| **Vehículo en Mantenimiento** | Reserva en vehículo con incidencia `EN_PROCESO` | Lanza `ReglaNegocioException`. Bloqueo absoluto. |
| **Límite Horario Cancelación** | Alumno intenta cancelar clase con `fechaHora <= now()` | Lanza `ReglaNegocioException`. Bloquea fraude de última hora. |
| **Baja de Vehículo** | Vehículo en estado distinto de `DISPONIBLE` | Lanza `ReglaNegocioException`. Solo se da de baja si está disponible. |
| **Aprobado Oficial Práctico** | Calificación `APTO` en examen oficial | Desvincula profesor (`profesor = null`), cancela clases futuras y cierra matrícula. |
| **Concurrencia en Reservas** | Mismo profesor y tramo horario | Rechazo atómico con HTTP 409 o feedback reactivo HTMX. |

---

## 4. Plantilla Canónica de Prueba Unitaria con Mockito

```java
package com.autoescuela.erp.practicas.service;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.autoescuela.erp.comun.exception.ReglaNegocioException;
import com.autoescuela.erp.practicas.model.ClasePractica;
import com.autoescuela.erp.practicas.model.EstadoClase;
import com.autoescuela.erp.practicas.repository.ClasePracticaRepository;
import com.autoescuela.erp.usuarios.model.Alumno;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClasePracticaServiceTest
{
    @Mock
    private ClasePracticaRepository clasePracticaRepository;

    @InjectMocks
    private ClasePracticaService clasePracticaService;

    @Test
    @DisplayName("Debe lanzar ReglaNegocioException si el alumno no dispone de capacidad de reserva")
    void testReservarClase_SinCapacidadReserva_LanzaExcepcion()
    {
        // GIVEN: Alumno con saldo 2 pero ya tiene 2 clases pendientes reservadas
        Alumno alumno = new Alumno();
        alumno.setId(201L);
        alumno.setSaldoClases(2);
        alumno.setClasesReservadasPendientes(2);

        // WHEN & THEN: Intento de reserva debe arrojar ReglaNegocioException
        ReglaNegocioException excepcion = assertThrows(
            ReglaNegocioException.class,
            () -> this.clasePracticaService.reservarClase(alumno, LocalDateTime.now().plusDays(2))
        );

        assertTrue(excepcion.getMessage().contains("capacidad"));
        verify(this.clasePracticaRepository, never()).save(any(ClasePractica.class));
    }

    @Test
    @DisplayName("Debe completar la clase y descontar saldo atómicamente al cumplimentar la ficha")
    void testCumplimentarFicha_Exito()
    {
        // GIVEN: Clase en estado PENDIENTE
        Alumno alumno = new Alumno();
        alumno.setSaldoClases(5);
        alumno.setClasesReservadasPendientes(2);

        ClasePractica clase = new ClasePractica();
        clase.setId(10L);
        clase.setEstadoClase(EstadoClase.PENDIENTE);
        clase.setAlumno(alumno);

        when(this.clasePracticaRepository.findById(10L)).thenReturn(Optional.of(clase));
        when(this.clasePracticaRepository.save(any(ClasePractica.class))).thenAnswer(i -> i.getArgument(0));

        // WHEN: Se registra la ficha técnica
        this.clasePracticaService.registrarFichaTecnica(10L, 100, 125, "Buen dominio del embrague");

        // THEN: Transición a RECIBIDA y decremento atómico
        assertEquals(EstadoClase.RECIBIDA, clase.getEstadoClase());
        assertEquals(4, alumno.getSaldoClases());
        assertEquals(1, alumno.getClasesReservadasPendientes());
        verify(this.clasePracticaRepository, times(1)).save(clase);
    }
}
```

---

## 5. Ejecución Selectiva de Pruebas

Durante el ciclo de desarrollo activo, está terminantemente prohibido ejecutar toda la suite de pruebas del proyecto (`mvn test`), reservándola exclusivamente para la fase de pre-commit.

```bash
# Ejecutar únicamente la suite de la clase afectada
mvn test -Dtest=ClasePracticaServiceTest

# Ejecutar un método de prueba concreto
mvn test -Dtest=ClasePracticaServiceTest#testReservarClase_SinCapacidadReserva_LanzaExcepcion

# Ejecutar pruebas de un paquete específico
mvn test -Dtest="com.autoescuela.erp.practicas.**"
```
