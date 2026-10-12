---
name: stripe-flow-validator
description: >-
  Gestión integral y validación de flujos de pago con Stripe (Checkout Sessions, Webhooks y verificación criptográfica).
  Usar cuando se trabaje con pasarelas de pago, confirmación de matrículas, compra de clases/bonos, tasas de renovación
  o depuración de eventos webhook de Stripe en el paquete pagos.
metadata:
  author: ManDS
  version: "1.0.0"
---

# Stripe Flow Validator

Esta skill define las directrices arquitectónicas, protocolos de seguridad y validaciones transaccionales para la integración de la pasarela de pagos Stripe en el ERP de la autoescuela (`com.autoescuela.erp.pagos`).

---

## 1. Principios de Arquitectura del Módulo de Pagos

1. **Desacoplamiento Modular:** El módulo `pagos` reside de forma independiente en `com.autoescuela.erp.pagos`. Evita dependencias cíclicas sirviendo de pasarela agnóstica para:
   * Formalización y activación de matrículas de nuevos alumnos (`academico`).
   * Adquisición de clases prácticas sueltas y bonos de clases (`practicas`).
   * Tasas de renovación de matrícula tras 2 exámenes suspendidos (`examenes`).
2. **Fuente de Verdad en Webhooks:** El éxito de cualquier transacción financiera y la activación de derechos en el sistema **nunca se confía a la redirección del navegador (`success_url`)**, sino exclusivamente a la recepción, validación criptográfica y procesamiento asíncrono del webhook oficial de Stripe (`checkout.session.completed`).
3. **Idempotencia Transaccional:** Cada evento recibido debe procesarse de forma idempotente para proteger al sistema contra reintentos automáticos de la infraestructura de Stripe ante cortes transitorios de red.
4. **URLs de Retorno Criptográficas:** Las URLs de retorno (`success_url`, `cancel_url`) deben utilizar tokens efímeros o UUIDs, prohibiendo terminantemente IDs secuenciales predecibles.

---

## 2. Creación de Stripe Checkout Sessions

### 2.1. Estructura de Parámetros y Metadatos
Al construir una sesión mediante `SessionCreateParams`, es imperativo inyectar metadatos exhaustivos que identifiquen el contexto del dominio:

```java
package com.autoescuela.erp.pagos.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.autoescuela.erp.comun.exception.ReglaNegocioException;

@Service
public class PagoStripeService
{
    @Value("${app_base_url:http://localhost:8080}")
    private String appBaseUrl;

    public String crearSesionPagoMatricula(Long alumnoId, String concepto, BigDecimal importe, String tokenRegistro)
    {
        long importeEnCentimos = importe.multiply(BigDecimal.valueOf(100)).longValue();

        Map<String, String> metadatos = new HashMap<>();
        metadatos.put("tipoOperacion", "MATRICULA");
        metadatos.put("alumnoId", String.valueOf(alumnoId));
        metadatos.put("tokenRegistro", tokenRegistro);

        SessionCreateParams params = SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.PAYMENT)
            .setSuccessUrl(this.appBaseUrl + "/registro/pago-exito?token=" + tokenRegistro)
            .setCancelUrl(this.appBaseUrl + "/registro/pago-cancelado?token=" + tokenRegistro)
            .addLineItem(
                SessionCreateParams.LineItem.builder()
                    .setQuantity(1L)
                    .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency("eur")
                            .setUnitAmount(importeEnCentimos)
                            .setProductData(
                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName(concepto)
                                    .setDescription("Formalización de matrícula de autoescuela con 2 convocatorias")
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .putAllMetadata(metadatos)
            .build();

        try
        {
            Session session = Session.create(params);
            return session.getUrl();
        }
        catch (StripeException e)
        {
            throw new ReglaNegocioException("Error al comunicarse con la pasarela de pagos Stripe: " + e.getMessage());
        }
    }
}
```

---

## 3. Recepción y Verificación Criptográfica de Webhooks

### 3.1. Configuración de Seguridad en Spring Security
El endpoint del webhook debe estar estrictamente abierto al tráfico anónimo y **excluido de la validación CSRF**:
```java
// En ConfiguracionSeguridad.java
http.csrf(csrf -> csrf.ignoringRequestMatchers("/api/stripe/webhook", "/h2-console/**"))
    .authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/stripe/webhook").permitAll()
        ...
    );
```

### 3.2. Verificación de Firma y Despacho en Controlador
El controlador debe recibir el cuerpo de la petición como cadena de texto cruda (`@RequestBody String payload`) para preservar los bytes exactos firmados por Stripe:

```java
package com.autoescuela.erp.pagos.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.autoescuela.erp.pagos.service.ProcesadorPagosService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/stripe")
@RequiredArgsConstructor
public class StripeWebhookController
{
    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    private final ProcesadorPagosService procesadorPagosService;

    @PostMapping("/webhook")
    public ResponseEntity<String> manejarWebhook(
        @RequestBody String payload,
        @RequestHeader("Stripe-Signature") String sigHeader)
    {
        Event event;
        try
        {
            event = Webhook.constructEvent(payload, sigHeader, this.webhookSecret);
        }
        catch (SignatureVerificationException e)
        {
            log.error("Firma de webhook de Stripe no válida: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Firma inválida");
        }
        catch (Exception e)
        {
            log.error("Error al procesar payload de Stripe: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error en el cuerpo de la petición");
        }

        if ("checkout.session.completed".equals(event.getType()))
        {
            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
            if (session != null)
            {
                this.procesadorPagosService.procesarPagoCompletado(session);
            }
        }

        return ResponseEntity.ok("Evento recibido");
    }
}
```

---

## 4. Reglas de Negocio en la Liquidación de Pagos

### 4.1. Formalización de Matrícula (Alta de Alumno)
1. Al registrarse inicialmente, el alumno se persiste en estado `INACTIVO`.
2. Al recibir `checkout.session.completed` con `tipoOperacion = MATRICULA`:
   * Transición de estado a `ACTIVO`.
   * Creación formal de la entidad `Matricula` (`estaActiva = true`, `convocatoriasRestantes = 2`, `saldoClases = 0`).
   * Envío de correo electrónico transaccional de bienvenida y confirmación de pago.
3. Si la sesión se cancela o expira sin pago, el sistema purga periódicamente el registro huérfano.

### 4.2. Adquisición de Clases Prácticas Sueltas y Bonos
1. **Condición Infranqueable:** Un alumno **únicamente puede comprar clases sueltas o bonos cuando su saldo restante de clases sea exactamente cero** ($\text{saldoClases} = 0$).
2. **Tarifario Oficial:**
   * Pesados (C, D): Clase suelta (60 €), Bono 10 (540 €), Bono 15 (800 €), Bono 20 (1.000 €).
   * Estándar (B, A2, etc.): Clase suelta (30 €), Bono 10 (270 €), Bono 15 (400 €), Bono 20 (500 €).
3. **Incremento Atómico:** Al confirmarse el pago, se ejecuta una transacción `@Transactional`:
   $$\text{saldoClases} \mathrel{+}= \text{cantidadComprada}$$

### 4.3. Renovación de Matrícula (Tras 2 Suspensos)
1. Si el alumno acumula 2 exámenes con calificación `NO APTO`, su capacidad de reserva y compra de clases se bloquea automáticamente.
2. Al abonar la tasa de *Renovación de Matrícula* vía Stripe, se resetean las 2 convocatorias y se reactiva la operativa académica.

---

## 5. Simulación Local y Testing

### 5.1. Escucha de Webhooks con Stripe CLI
Para pruebas locales y desarrollo interactivo:
```bash
# 1. Iniciar sesión en Stripe CLI
stripe login

# 2. Reenviar eventos webhook al servidor Spring Boot local
stripe listen --forward-to localhost:8080/api/stripe/webhook

# 3. Disparar evento simulado de Checkout completado
stripe trigger checkout.session.completed
```

### 5.2. Mocking en Tests de Integración (`BaseIntegrationTest`)
Para evitar llamadas externas a la API de Stripe durante la ejecución del comando `mvn test`, `PagoStripeService` debe estar mockeado transversalmente en el contexto de pruebas:
```java
@MockitoBean
protected PagoStripeService pagoStripeService;
```
Al simular compras en tests de integración, mockear `pagoStripeService.crearSesionPago...` retornando una URL ficticia (`https://checkout.stripe.com/c/pay/cs_test_mock`).

---

## 6. Checklist de Seguridad

1. [ ] **Verificación de Firma Activa:** `Webhook.constructEvent` obligatorio en producción. Jamás procesar eventos sin validar la cabecera `Stripe-Signature`.
2. [ ] **Variables de Entorno Seguras:** Secretos `stripe.api.key` y `stripe.webhook.secret` gestionados vía variables de entorno en producción (Render/Docker), nunca en texto plano en el repositorio.
3. [ ] **Precios Calculados en Servidor:** El importe a cobrar se consulta siempre de las reglas de dominio del backend; jamás se confía en precios enviados por el cliente web.
4. [ ] **Operaciones Idempotentes:** Verificar si la transacción ya ha sido registrada en base de datos antes de incrementar saldo o activar matrículas duplicadas.
