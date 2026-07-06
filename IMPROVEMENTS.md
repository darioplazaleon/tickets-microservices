# Plan de mejoras — tickets-microservices

Hallazgos de la revisión del 2026-07-05, ordenados por impacto. Vamos a ir corrigiéndolos punto por punto: marcar cada ítem al completarlo.

**Estado general al momento de la revisión:** lo que ya está bien encaminado — Flyway con `ddl-auto: validate`, locking optimista con retry en `TicketTypeService.reserveTickets`, tracing con correlation-id propagado por HTTP y Kafka (trabajo reciente, aún sin commitear).

---

## 1. Idempotencia en los consumidores de Kafka 🔴 (crítico)

Kafka garantiza *at-least-once*: si un consumidor falla después de procesar pero antes de commitear el offset, el evento se re-entrega. Ninguno de los 9 listeners se protege contra eso.

**Caso más grave:** `TicketOwnershipService.processPayment` (`ticketservice`). Si `tickets.payment.success` llega dos veces, se crean tickets duplicados para la misma orden.

- [ ] Chequeo de duplicados en `processPayment` (ej. `existsByOrderId(orderId)` antes de crear tickets)
- [ ] Estrategia general: tabla `processed_events` con el eventId como clave única, por servicio consumidor
- [ ] Revisar los 9 listeners: `BookingExpiredEventListener`, `BookingPaymentEventListener`, `OrderExpiredEventListener` (event), `PaymentSuccessEventListener` (event), `NotificationEventListener`, `TicketTransferEventListener`, `OrderEventListener`, `OrderPaymentListener`, `PaymentSuccessEventListener` (ticket)

## 2. Transacciones en operaciones multi-paso 🔴 (crítico)

Solo hay 3 `@Transactional` en todo el proyecto (todos en eventservice). `processPayment` guarda N tickets en un loop sin transacción: si falla a la mitad, quedan tickets huérfanos y el evento se reprocesa (agravando el punto 1).

- [ ] `@Transactional` en `TicketOwnershipService.processPayment`
- [ ] Auditar el resto de los servicios: envolver en transacción todo método con más de un write a la BD

## 3. Dual-write: BD + Kafka sin outbox 🟠

Los publishers llaman `kafkaTemplate.send()` directamente después de escribir en la BD. Si la BD commitea y Kafka falla (o al revés), los servicios quedan inconsistentes (ej.: tickets creados pero la notificación del QR nunca sale).

- [ ] Implementar patrón **Transactional Outbox**: guardar el evento en una tabla `outbox` dentro de la misma transacción del write de negocio
- [ ] Publicador: scheduler que lee la tabla outbox y publica a Kafka (alternativa avanzada: Debezium/CDC)
- [ ] Empezar por ticketservice (flujo de pago) y orderservice

## 4. Manejo de errores en los listeners (DLT) 🟠

No hay `DefaultErrorHandler`, `@RetryableTopic` ni dead-letter topics. Un mensaje "venenoso" (que siempre lanza excepción) bloquea la partición reintentando infinitamente.

- [ ] Configurar `DefaultErrorHandler` con backoff exponencial en cada servicio consumidor
- [ ] `DeadLetterPublishingRecoverer` para mandar los mensajes fallidos a topics `.DLT`
- [ ] (Opcional) Alerta/log visible cuando algo cae al DLT

## 5. Tests 🟠

175 clases de producción, solo los 7 stubs `contextLoads()`. Orden sugerido por valor:

- [ ] Unit tests de `TicketTypeService.reserveTickets` (lógica de capacidad y concurrencia — corazón del negocio)
- [ ] Tests de integración con **Testcontainers** (Postgres + Kafka) para el flujo booking → order → payment → ticket
- [ ] Tests de los listeners verificando idempotencia (después del punto 1)

## 6. Código duplicado entre servicios 🟡

`RequestTracingFilter` y `KafkaTracingInterceptor` están copiados idénticos en 5–6 servicios. `GlobalExceptionHandler` solo existe en eventservice — los demás devuelven stack traces crudos. Los nombres de topics (`"tickets.payment.success"`, etc.) están hardcodeados como strings en cada servicio.

- [ ] Crear módulo `shared-web` (junto a `shared-events` en el POM padre) con `RequestTracingFilter` y `KafkaTracingInterceptor`
- [ ] Mover/replicar `GlobalExceptionHandler` a todos los servicios vía el módulo compartido
- [ ] Constantes compartidas para nombres de topics y headers

## 7. Propagación inconsistente del correlation-id 🟡

`TicketEventPublisher` lee el correlation-id del MDC; `OrderEventPublisher` lo recibe como parámetro `UUID`. La traza puede cortarse en algún salto.

- [ ] Unificar: todos los publishers leen del MDC (no ensucia las firmas de los métodos)

## 8. Seguridad solo en el gateway 🟡

`SecurityFilterChain` existe solo en apigateway; los servicios internos confían ciegamente en el header `X-User-Id`. Cualquiera con acceso a la red interna puede impersonar usuarios.

- [ ] Cada servicio valida el JWT como resource server OAuth2 (Keycloak ya está en el stack)
- [ ] Como mínimo: documentar la decisión de confiar en la red interna si se elige no hacerlo

## 9. Infraestructura y DX 🟢

- [ ] Dockerfiles para cada servicio + entradas en `docker-compose.yml` (hoy el compose solo levanta infraestructura)
- [ ] CI con GitHub Actions: `mvn verify` en cada push
- [ ] Migrar Kafka a modo **KRaft** (elimina el contenedor de Zookeeper; `cp-kafka` 7.5+ lo soporta)
- [ ] Tracing distribuido real: Micrometer Tracing + Tempo/Zipkin, o al menos Loki para buscar logs por correlation-id (Prometheus/Grafana ya están)

---

## Pendiente inmediato

- [ ] Commitear el trabajo de tracing actual (todo el diff sin commitear) antes de arrancar con los puntos de arriba
