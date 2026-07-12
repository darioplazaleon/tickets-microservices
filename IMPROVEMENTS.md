# Plan de mejoras — tickets-microservices

Hallazgos de la revisión del 2026-07-05, ordenados por impacto. Vamos a ir corrigiéndolos punto por punto: marcar cada ítem al completarlo.

**Estado general al momento de la revisión:** lo que ya está bien encaminado — Flyway con `ddl-auto: validate`, locking optimista con retry en `TicketTypeService.reserveTickets`, tracing con correlation-id propagado por HTTP y Kafka (trabajo reciente, aún sin commitear).

---

## 1. Idempotencia en los consumidores de Kafka 🔴 (crítico)

Kafka garantiza *at-least-once*: si un consumidor falla después de procesar pero antes de commitear el offset, el evento se re-entrega. Ninguno de los 9 listeners se protege contra eso.

**Caso más grave:** `TicketOwnershipService.processPayment` (`ticketservice`). Si `tickets.payment.success` llega dos veces, se crean tickets duplicados para la misma orden.

- [x] Chequeo de duplicados en `processPayment` (`existsByOrderId` antes de crear tickets)
- [x] Tabla `processed_events` en eventservice (los contadores `reserved`/`sold` no tienen clave natural de dedupe) — guards en `EventPaymentService` y `EventExpirationService`
- [x] Revisar los 9 listeners. Resultado:
  - ticketservice `processPayment`: guard `existsByOrderId` ✓
  - eventservice payment/expiración: `processed_events` ✓
  - orderservice `createOrder`: guard `existsByBookingId` + unique constraint en BD (V2) ✓
  - orderservice `processPaymentSucceeded`: guard por estado `PAID` ✓
  - bookingservice pago: guard por estado `CONFIRMED` ✓
  - bookingservice expiración: ya era idempotente (guard `PENDING`) ✓
  - notificationservice (x2): un duplicado solo re-envía un email — riesgo aceptado hasta tener DLT (punto 4)

## 2. Transacciones en operaciones multi-paso 🔴 (crítico)

Solo hay 3 `@Transactional` en todo el proyecto (todos en eventservice). `processPayment` guarda N tickets en un loop sin transacción: si falla a la mitad, quedan tickets huérfanos y el evento se reprocesa (agravando el punto 1).

- [x] `@Transactional` en `TicketOwnershipService.processPayment`
- [x] Auditar el resto de los servicios: agregado también en `TicketOwnershipService.transferTicket` y `OrderService.createOrder`; los demás handlers hacen un solo write (el guard de idempotencia + write quedan en la misma transacción donde importa)

## 3. Dual-write: BD + Kafka sin outbox 🟠

Los publishers llaman `kafkaTemplate.send()` directamente después de escribir en la BD. Si la BD commitea y Kafka falla (o al revés), los servicios quedan inconsistentes (ej.: tickets creados pero la notificación del QR nunca sale).

- [x] Implementar patrón **Transactional Outbox**: tabla `outbox_events` + `OutboxEventWriter` (escribe en la TX de negocio) en ticketservice y orderservice
- [x] Publicador: `OutboxRelay` (@Scheduled cada 2s) lee pendientes en orden, publica con el tipo original (header `__TypeId__` intacto) y marca `published_at`; ante error corta el batch y reintenta
- [x] ticketservice (`tickets.qr.ready`, `tickets.qr.transfer`) y orderservice (`tickets.order.expired`)
- [x] bookingservice (`tickets.booking.created`): outbox vía shared-infra, `createBooking` transaccional, migración V2
- [x] paymentservice: N/A — no tiene base de datos propia; sigue publicando directo (documentado en el publisher)

> Bug encontrado y corregido de paso: `OrderExpirationScheduler` guardaba `bookingId` en el ZSet de Redis pero lo buscaba como `orderId` (`findById`) — las órdenes nunca expiraban por esa vía. Ahora busca por `bookingId` y la expiración corre en transacción (`OrderExpireService.expireByBookingId`), removiendo del ZSet recién después de procesar.

## 4. Manejo de errores en los listeners (DLT) 🟠

No hay `DefaultErrorHandler`, `@RetryableTopic` ni dead-letter topics. Un mensaje "venenoso" (que siempre lanza excepción) bloquea la partición reintentando infinitamente.

- [x] `KafkaErrorHandlingConfig` en los 5 servicios consumidores: `DefaultErrorHandler` con backoff exponencial (3 reintentos, 1s→10s); `IllegalArgumentException` marcada como no-reintentable
- [x] `DeadLetterPublishingRecoverer` → topics `<topic>.DLT`; los fallos de deserialización van con producer de bytes (payload crudo), el resto con JsonSerializer
- [x] `ErrorHandlingDeserializer` en los YAML: un payload corrupto ya no bloquea la partición, va directo al DLT sin reintentos
- [ ] (Opcional) Alerta/log visible cuando algo cae al DLT (hoy solo queda el ERROR log del handler)

## 5. Tests 🟠

175 clases de producción, solo los 7 stubs `contextLoads()`. Orden sugerido por valor:

- [x] Unit tests de `TicketTypeService.reserveTickets` (capacidad, cantidad inválida, tipo inexistente)
- [x] Test de integración con **Testcontainers** (Postgres real): `TicketReservationConcurrencyTest` — 20 hilos concurrentes nunca sobrevenden la capacidad (valida el locking optimista + retry)
- [x] Tests de idempotencia de los handlers: event (payment/expiración), ticket (processPayment), order (createOrder/pago), booking (pago) — 28 tests en total
- [x] Tests del outbox: `OutboxRelayTest` (publica con tipo original + header de correlación, corta el batch ante fallo para preservar orden)
- [x] Eliminados los 7 stubs `contextLoads()` (requerían la infraestructura real levantada y no verificaban nada)
- [ ] Pendiente: test de integración del flujo completo booking → order → payment → ticket con Kafka (Testcontainers Kafka), idealmente tras el módulo compartido (punto 6)

> Nota: los daemons Docker recientes rechazan la API vieja que usa docker-java; quedó fijado `api.version=1.44` vía surefire en eventservice y `testcontainers.version=1.21.3`.

## 6. Código duplicado entre servicios 🟡

`RequestTracingFilter` y `KafkaTracingInterceptor` están copiados idénticos en 5–6 servicios. `GlobalExceptionHandler` solo existe en eventservice — los demás devuelven stack traces crudos. Los nombres de topics (`"tickets.payment.success"`, etc.) están hardcodeados como strings en cada servicio.

- [x] Módulo **`shared-infra`** con auto-configuraciones Spring Boot (se activan por classpath, sin código por servicio):
  - `SharedWebAutoConfiguration`: `RequestTracingFilter` + `GlobalExceptionHandler` genérico (solo apps servlet)
  - `SharedKafkaAutoConfiguration`: `KafkaTracingInterceptor` + `DefaultErrorHandler` con DLT (Boot los aplica solo al container factory)
  - `SharedOutboxAutoConfiguration`: `OutboxEventWriter`/`OutboxRelay` reescritos con **JDBC plano** (sin acoplar JPA), opt-in por servicio con `shared.outbox.enabled=true`
- [x] `GlobalExceptionHandler` genérico en todos los servicios servlet; eventservice conserva `EventExceptionHandler` solo para sus excepciones de dominio
- [x] Constantes `Topics` y `Tracing` en shared-events; reemplazados los strings hardcodeados en los 9 listeners y todos los publishers
- [x] Eliminadas ~20 clases duplicadas entre servicios

## 7. Propagación inconsistente del correlation-id 🟡

`TicketEventPublisher` lee el correlation-id del MDC; `OrderEventPublisher` lo recibe como parámetro `UUID`. La traza puede cortarse en algún salto.

- [x] Unificado vía `OutboxEventWriter`: por defecto lee el MDC; acepta correlation-id explícito solo donde no hay contexto de request (scheduler de expiración, que lo toma de la orden persistida)

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

- [x] Commitear el trabajo de tracing actual (commit `ac3b271`)
