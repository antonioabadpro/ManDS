---
name: feature-scaffolder
description: >-
  Andamiaje estructurado de nuevas funcionalidades bajo el patrón Package by Feature en Spring Boot 4.
  Usar cuando se requiera crear o estructurar un nuevo módulo, entidad, DTOs con records, mappers MapStruct,
  repositorios JPA, servicios transaccionales o controladores MVC/HTMX dentro del ERP.
metadata:
  author: ManDS
  version: "1.0.0"
---

# Feature Scaffolder (Package by Feature)

Esta skill define la guía maestra y el estándar técnico obligatorio para diseñar, estructurar e implementar nuevas funcionalidades en el ERP de la autoescuela bajo una arquitectura modular **Package by Feature** en Spring Boot 4 y Java 21.

---

## 1. Principios Arquitectónicos Fundamentales

1. **Alta Cohesión y Bajo Acoplamiento:** Cada funcionalidad (rebanada vertical o *Vertical Slice*) agrupa todos sus componentes relacionados en un único paquete raíz (`com.autoescuela.erp.<feature>`), facilitando la navegación y delimitando responsabilidades.
2. **Inmutabilidad y DTOs Planos:** Los datos que cruzan la frontera web viajan en `record` inmutables de Java 21 con validaciones declarativas Jakarta Bean Validation. Prohibido exponer entidades JPA directamente a las plantillas o vistas.
3. **Mapeo Tipado con MapStruct:** La conversión entre DTOs y entidades se realiza mediante mappers generados en tiempo de compilación con `@Mapper(componentModel = "spring")`.
4. **Uso Obligatorio de `this.`:** En todo el código Java del backend, es obligatorio anteponer `this.` a cualquier lectura o asignación de campos de instancia para evitar *shadowing*.
5. **Transaccionalidad Defensiva:** Servicios anotados con `@Transactional(readOnly = true)` a nivel de clase y `@Transactional` explícito en métodos de mutación.

---

## 2. Estructura Canónica de Directorios

Cada módulo o feature debe respetar la siguiente estructura de subpaquetes:

```text
com.autoescuela.erp.<feature>/
├── controller/         # Controladores Spring MVC y endpoints HTMX (@Controller)
├── dto/                # Records Java 21 inmutables con Bean Validation
├── mapper/             # Interfaces MapStruct (componentModel = "spring")
├── model/              # Entidades JPA (@Entity) y enums de dominio
├── repository/         # Interfaces Spring Data JPA (@Repository)
├── service/            # Lógica de negocio y transacciones (@Service)
└── util/               # (Opcional) Clases de utilidad puras sin estado
```

En los recursos web (`src/main/resources/`):
```text
templates/<feature>/              # Vistas maestras de la funcionalidad (ej. flota.html)
templates/<feature>/fragments/    # Fragmentos modales y tablas parciales (ej. modal-alta.html)
static/js/<feature>.js            # Lógica JS cliente no intrusiva y manejadores de eventos
```

---

## 3. Anatomía Detallada de Componentes

### 3.1. Entidades de Dominio (`model/`)
* Anotar con `@Entity` y `@Table(name = "nombre_tabla")`.
* Si participa en la jerarquía polimórfica de usuarios/personas, respetar la estrategia `@Inheritance(strategy = InheritanceType.JOINED)`.
* Emplear tipos envoltorio (`Long`, `Integer`, `Boolean`, `BigDecimal`) para permitir nulabilidad en base de datos.
* Definir constructores explícitos o constructores sin argumentos `@NoArgsConstructor` requeridos por JPA.

```java
package com.autoescuela.erp.ejemplo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ejemplo_modulo")
@Getter
@Setter
@NoArgsConstructor
public class EjemploModulo
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoEjemplo estado;

    public EjemploModulo(String nombre, EstadoEjemplo estado)
    {
        this.nombre = nombre;
        this.estado = estado;
    }
}
```

### 3.2. DTOs con Records de Java 21 (`dto/`)
* Diseñar DTOs planos orientados al caso de uso: `Alta<Feature>DTO`, `Editar<Feature>DTO`, `Detalle<Feature>DTO`, `<Feature>ResumenDTO`.
* Incluir validaciones declarativas Jakarta Bean Validation (`@NotNull`, `@NotBlank`, `@Size`, `@Pattern`, `@PastOrPresent`, etc.).
* Para validaciones cruzadas complejas entre campos, utilizar métodos auxiliares con `@AssertTrue`.

```java
package com.autoescuela.erp.ejemplo.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AltaEjemploDTO(
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    String nombre,

    @NotNull(message = "Debe indicar el límite de plazas")
    Integer limitePlazas,

    Boolean esEspecial
) {
    @AssertTrue(message = "Las plazas para eventos especiales deben ser superiores a 10")
    public boolean isPlazasCoherentes()
    {
        if (Boolean.TRUE.equals(this.esEspecial) && this.limitePlazas != null)
        {
            return this.limitePlazas > 10;
        }
        return true;
    }
}
```

### 3.3. Mappers con MapStruct (`mapper/`)
* Configurar `@Mapper(componentModel = "spring")`.
* Inyectar o mapear enums y dependencias si fuera preciso.
* Si el formulario requiere recomponer datos de sólo lectura tras un error de enlace (*binding*), incorporar un método `repoblarDTO`.

```java
package com.autoescuela.erp.ejemplo.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.autoescuela.erp.ejemplo.dto.AltaEjemploDTO;
import com.autoescuela.erp.ejemplo.dto.EjemploDetalleDTO;
import com.autoescuela.erp.ejemplo.model.EjemploModulo;

@Mapper(componentModel = "spring")
public interface EjemploMapper
{
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estado", constant = "ACTIVO")
    EjemploModulo toEntity(AltaEjemploDTO dto);

    EjemploDetalleDTO toDetalleDTO(EjemploModulo entity);
}
```

### 3.4. Repositorios Spring Data JPA (`repository/`)
* Heredar de `JpaRepository<Entidad, ID>`.
* Emplear consultas JPQL explícitas con `@Query` para uniones eficientes (`JOIN FETCH`) y evitar el problema $N+1$.
* Definir métodos de existencia eficientes: `boolean existsByNombreIgnoreCase(String nombre)`.

```java
package com.autoescuela.erp.ejemplo.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.autoescuela.erp.ejemplo.model.EjemploModulo;

@Repository
public interface EjemploModuloRepository extends JpaRepository<EjemploModulo, Long>
{
    boolean existsByNombreIgnoreCase(String nombre);

    @Query("SELECT e FROM EjemploModulo e WHERE e.estado = 'ACTIVO' ORDER BY e.id ASC")
    List<EjemploModulo> findAllActivos();

    @Query("SELECT e FROM EjemploModulo e WHERE e.id = :id")
    Optional<EjemploModulo> findByIdConRelaciones(@Param("id") Long id);
}
```

### 3.5. Servicios de Dominio (`service/`)
* Declarar `@Service` y `@RequiredArgsConstructor`.
* Inyectar dependencias finales en atributos privados.
* Control estricto de reglas de negocio arrojando `ReglaNegocioException` o `RecursoNoEncontradoException`.
* Obligatoriedad absoluta de `this.` en invocaciones y atributos de instancia.

```java
package com.autoescuela.erp.ejemplo.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.autoescuela.erp.comun.exception.RecursoNoEncontradoException;
import com.autoescuela.erp.comun.exception.ReglaNegocioException;
import com.autoescuela.erp.ejemplo.dto.AltaEjemploDTO;
import com.autoescuela.erp.ejemplo.dto.EjemploDetalleDTO;
import com.autoescuela.erp.ejemplo.mapper.EjemploMapper;
import com.autoescuela.erp.ejemplo.model.EjemploModulo;
import com.autoescuela.erp.ejemplo.repository.EjemploModuloRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EjemploModuloService
{
    private final EjemploModuloRepository ejemploRepository;
    private final EjemploMapper ejemploMapper;

    @Transactional
    public Long crearEjemplo(AltaEjemploDTO dto)
    {
        if (dto == null)
        {
            throw new IllegalArgumentException("El DTO no puede ser nulo.");
        }
        if (this.ejemploRepository.existsByNombreIgnoreCase(dto.nombre()))
        {
            throw new ReglaNegocioException("Ya existe un registro con el nombre indicado: " + dto.nombre());
        }

        EjemploModulo entidad = this.ejemploMapper.toEntity(dto);
        EjemploModulo persistido = this.ejemploRepository.save(entidad);
        return persistido.getId();
    }

    public EjemploDetalleDTO obtenerPorId(Long id)
    {
        EjemploModulo entidad = this.ejemploRepository.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el elemento con ID: " + id));
        return this.ejemploMapper.toDetalleDTO(entidad);
    }
}
```

### 3.6. Controladores Web y HTMX (`controller/`)
* Emplear `@Controller` con `@RequestMapping("/<feature>")`.
* Soportar peticiones ordinarias y asíncronas con HTMX:
  - Inspeccionar cabecera `@RequestHeader(value = "HX-Request", required = false) boolean esHtmx`.
  - En peticiones HTMX que requieran recargar la vista padre o navegar, responder con cabecera `HX-Redirect`.
  - En validaciones erróneas de formulario bajo HTMX, retornar quirúrgicamente el fragmento del modal con sus mensajes de error.

```java
package com.autoescuela.erp.ejemplo.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.autoescuela.erp.ejemplo.dto.AltaEjemploDTO;
import com.autoescuela.erp.ejemplo.service.EjemploModuloService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/ejemplos")
@RequiredArgsConstructor
public class EjemploModuloController
{
    private final EjemploModuloService ejemploService;

    @GetMapping
    public String listar(Model model)
    {
        model.addAttribute("nuevoEjemploDTO", new AltaEjemploDTO("", 15, false));
        return "admin/ejemplos";
    }

    @PostMapping("/alta")
    public String darAlta(
        @Valid @ModelAttribute("nuevoEjemploDTO") AltaEjemploDTO dto,
        BindingResult bindingResult,
        @RequestHeader(value = "HX-Request", required = false) boolean esHtmx,
        HttpServletResponse response,
        RedirectAttributes redirectAttributes,
        Model model)
    {
        if (bindingResult.hasErrors())
        {
            if (esHtmx)
            {
                return "admin/fragments/modal-alta-ejemplo :: modalAltaEjemplo";
            }
            return "admin/ejemplos";
        }

        this.ejemploService.crearEjemplo(dto);

        if (esHtmx)
        {
            response.setHeader("HX-Redirect", "/admin/ejemplos");
            return "";
        }

        redirectAttributes.addFlashAttribute("mensajeExito", "Registro creado correctamente.");
        return "redirect:/admin/ejemplos";
    }
}
```

---

## 4. Checklist de Ejecución Paso a Paso

Al crear una nueva funcionalidad, sigue estrictamente este orden:
1. [ ] **Definición del Modelo:** Crear entidad JPA en `model/` y enum de estados si aplica.
2. [ ] **Repositorio Spring Data:** Crear interfaz en `repository/` con consultas derivadas y JPQL optimizadas.
3. [ ] **Diseño de DTOs:** Crear records inmutables con Bean Validation en `dto/`.
4. [ ] **Mapper MapStruct:** Crear interfaz en `mapper/` y verificar compilación de clases MapStruct (`mvn compile`).
5. [ ] **Servicio Transaccional:** Implementar lógica de negocio, validaciones e invariantes en `service/`.
6. [ ] **Pruebas Unitarias del Servicio:** Crear suite con Mockito (`@ExtendWith(MockitoExtension.class)`) validando casos felices y excepciones de negocio (`ReglaNegocioException`).
7. [ ] **Controlador MVC:** Implementar endpoints con soporte síncrono y HTMX en `controller/`.
8. [ ] **Vistas y Fragmentos:** Crear plantillas Thymeleaf con maquetación Tailwind CSS en `templates/<feature>/`.
9. [ ] **JavaScript No Intrusivo:** Crear archivo cliente en `static/js/<feature>.js` bajo el patrón *Named Event Handlers*.
10. [ ] **Pruebas de Controlador:** Validar peticiones HTTP, redirecciones y atributos del modelo con MockMvc.

---

## 5. Antipatrones Prohibidos

* **NO devolver entidades JPA en controladores:** Siempre retornar DTOs mapeados.
* **NO usar `@SpringBootTest` para pruebas de servicio o utilidades:** Usar pruebas unitarias puras con Mockito.
* **NO omitir el operador `this.`:** Cualquier variable de instancia debe accederse con `this.campo`.
* **NO incluir lógica de negocio en controladores:** Los controladores solo orquestan HTTP, validan enlace y delegan al servicio.
* **NO usar identificadores secuenciales en enlaces públicos:** Usar UUIDs o tokens criptográficos temporales.
