package com.autoescuela.erp.usuarios.dto;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.autoescuela.erp.auth.dto.RegistroAlumnoDTO;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias de las validaciones de rango y mayoría de edad en los DTOs de usuario.
 * Sigue el principio de la pirámide de testing: validación unitaria rápida sin sobrecarga de contexto Spring.
 */
class ValidacionFechaNacimientoDTOTest
{
    @Test
    @DisplayName("RegistroAlumnoDTO: isMayorDeEdad valida correctamente límites y fronteras")
    void testRegistroAlumnoDTO_IsMayorDeEdad()
    {
        LocalDate hoy = LocalDate.now();

        // Exactamente 18 años hoy -> válido
        RegistroAlumnoDTO dtoExacto18 = new RegistroAlumnoDTO(
                "usuario", "correo@autoescuela.es", "pass123", "pass123", "Nombre", "Apellidos",
                "12345678Z", hoy.minusYears(18), "600112233", "Calle 1", true, null
        );
        assertTrue(dtoExacto18.isMayorDeEdad());

        // Más de 18 años -> válido
        RegistroAlumnoDTO dtoMayor = new RegistroAlumnoDTO(
                "usuario", "correo@autoescuela.es", "pass123", "pass123", "Nombre", "Apellidos",
                "12345678Z", hoy.minusYears(20), "600112233", "Calle 1", true, null
        );
        assertTrue(dtoMayor.isMayorDeEdad());

        // 17 años y 364 días (cumple mañana) -> inválido
        RegistroAlumnoDTO dtoMenor = new RegistroAlumnoDTO(
                "usuario", "correo@autoescuela.es", "pass123", "pass123", "Nombre", "Apellidos",
                "12345678Z", hoy.minusYears(18).plusDays(1), "600112233", "Calle 1", true, null
        );
        assertFalse(dtoMenor.isMayorDeEdad());

        // Null se delega a @NotNull -> devuelve true en isMayorDeEdad
        RegistroAlumnoDTO dtoNull = new RegistroAlumnoDTO(
                "usuario", "correo@autoescuela.es", "pass123", "pass123", "Nombre", "Apellidos",
                "12345678Z", null, "600112233", "Calle 1", true, null
        );
        assertTrue(dtoNull.isMayorDeEdad());
    }

    @Test
    @DisplayName("RegistroAlumnoDTO: isEdadRazonable valida límite de 100 años")
    void testRegistroAlumnoDTO_IsEdadRazonable()
    {
        LocalDate hoy = LocalDate.now();

        // Exactamente 100 años hoy -> válido
        RegistroAlumnoDTO dtoExacto100 = new RegistroAlumnoDTO(
                "usuario", "correo@autoescuela.es", "pass123", "pass123", "Nombre", "Apellidos",
                "12345678Z", hoy.minusYears(100), "600112233", "Calle 1", true, null
        );
        assertTrue(dtoExacto100.isEdadRazonable());

        // 100 años y 1 día -> inválido (más de 100 años)
        RegistroAlumnoDTO dtoMasDe100 = new RegistroAlumnoDTO(
                "usuario", "correo@autoescuela.es", "pass123", "pass123", "Nombre", "Apellidos",
                "12345678Z", hoy.minusYears(100).minusDays(1), "600112233", "Calle 1", true, null
        );
        assertFalse(dtoMasDe100.isEdadRazonable());

        // Null -> válido (delegado a @NotNull)
        RegistroAlumnoDTO dtoNull = new RegistroAlumnoDTO(
                "usuario", "correo@autoescuela.es", "pass123", "pass123", "Nombre", "Apellidos",
                "12345678Z", null, "600112233", "Calle 1", true, null
        );
        assertTrue(dtoNull.isEdadRazonable());
    }

    @Test
    @DisplayName("EditarPerfilProfesorDTO: isMayorDeEdad e isEdadRazonable validan rangos")
    void testEditarPerfilProfesorDTO_ValidacionesFechaNacimiento()
    {
        LocalDate hoy = LocalDate.now();

        // Profesor válido (30 años)
        EditarPerfilProfesorDTO dtoValido = new EditarPerfilProfesorDTO(
                1L, "Carlos", "Sainz", "611223344", "Calle Paddock 1", hoy.minusYears(30),
                "12345678Z", "carlos.profesor", "carlos@autoescuela.es", null, null, null, null, null
        );
        assertTrue(dtoValido.isMayorDeEdad());
        assertTrue(dtoValido.isEdadRazonable());

        // Profesor menor de edad (17 años)
        EditarPerfilProfesorDTO dtoMenor = new EditarPerfilProfesorDTO(
                1L, "Carlos", "Sainz", "611223344", "Calle Paddock 1", hoy.minusYears(17),
                "12345678Z", "carlos.profesor", "carlos@autoescuela.es", null, null, null, null, null
        );
        assertFalse(dtoMenor.isMayorDeEdad());
        assertTrue(dtoMenor.isEdadRazonable());

        // Profesor de más de 100 años
        EditarPerfilProfesorDTO dtoCentenario = new EditarPerfilProfesorDTO(
                1L, "Carlos", "Sainz", "611223344", "Calle Paddock 1", hoy.minusYears(101),
                "12345678Z", "carlos.profesor", "carlos@autoescuela.es", null, null, null, null, null
        );
        assertTrue(dtoCentenario.isMayorDeEdad());
        assertFalse(dtoCentenario.isEdadRazonable());
    }
}
