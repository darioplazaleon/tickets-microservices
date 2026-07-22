# Architecture

This document zooms into the two flows that define the system: the **purchase happy path** and the **order lifecycle** (including what happens when the user never pays). For the high-level component view, see the diagram in the [README](../README.md#-architecture).

## Purchase flow (happy path)

The services are choreographed through Kafka events — there is no central orchestrator. Each arrow annotated with a topic name is an asynchronous event; everything else is synchronous REST.

```mermaid
sequenceDiagram
    autonumber
    actor U as Client
    participant GW as API Gateway
    participant BK as booking-service
    participant EV as event-service
    participant K as Kafka
    participant OR as order-service
    participant PAY as payment-service
    participant ST as Stripe
    participant TK as ticket-service
    participant NT as notification-service

    U->>GW: POST /api/v1/bookings/create (JWT)
    GW->>BK: forward + X-User-Id, X-Correlation-Id
    BK->>EV: POST /api/v1/ticket-types/reserve/{eventId}
    Note over EV: optimistic locking + retry<br/>capacity: reserved += qty
    EV-->>BK: unit price
    BK->>BK: save booking PENDING<br/>+ outbox row (same TX)
    BK--)K: tickets.booking.created (outbox relay)
    K--)OR: BookingCreatedEvent
    OR->>OR: create order PENDING<br/>(idempotent by bookingId)
    Note over OR: expiry scheduled in Redis ZSET<br/>(createdAt + 15 min)

    U->>PAY: POST /api/v1/payments/checkout/{orderId}
    PAY->>OR: GET order (REST)
    PAY->>ST: create Checkout Session<br/>(metadata: order_id)
    ST-->>U: hosted payment page
    U->>ST: pays
    ST->>PAY: webhook: checkout.session.completed<br/>(signature verified)
    PAY--)K: tickets.payment.success

    par order-service
        K--)OR: PaymentSucceededEvent
        OR->>OR: order → PAID
    and booking-service
        K--)BK: PaymentSucceededEvent
        BK->>BK: booking → CONFIRMED
    and event-service
        K--)EV: PaymentSucceededEvent
        EV->>EV: capacity: reserved → sold<br/>(processed_events dedupe)
    and ticket-service
        K--)TK: PaymentSucceededEvent
        TK->>TK: create N tickets + signed master QR<br/>(skipped if orderId already has tickets)
        TK--)K: tickets.qr.ready (outbox relay)
    end

    K--)NT: TicketQrReadyEvent
    NT->>EV: REST: event / order / customer data
    NT->>U: email with QR attachment (SMTP)
```

Notes on the numbered steps:

- **6–7 (outbox):** services that own a database never call `kafkaTemplate.send()` inside business logic. The event is written to an `outbox_events` table in the same transaction as the state change, and a scheduled relay publishes it in order. If the broker is down, the business transaction still commits and the event goes out later. `payment-service` is the exception — it has no database, so it publishes directly.
- **18 (fan-out):** `tickets.payment.success` has four consumer groups, each processing independently. Kafka delivers *at-least-once*, so every handler is idempotent: `order`/`booking` guard by current state, `ticket` checks `existsByOrderId`, and `event` deduplicates through a `processed_events` table (its capacity counters have no natural idempotency key).
- **QR signing:** each QR payload is signed with HMAC-SHA256 (`QR_SECRET_KEY`), so `ticket-service` can validate scans offline without a database lookup of the signature.
- **Failure handling:** every listener sits behind a `DefaultErrorHandler` with exponential backoff (3 retries, 1s→10s) and a `<topic>.DLT` dead-letter topic. Corrupt payloads skip retries and go straight to the DLT via `ErrorHandlingDeserializer`.

## Order lifecycle

An order is born when `booking.created` is consumed and dies either paid or expired. Expiration is not event-driven from the outside — `order-service` schedules it itself in a Redis sorted set and sweeps it with a scheduler.

```mermaid
stateDiagram-v2
    [*] --> PENDING : BookingCreatedEvent consumed
    PENDING --> PAID : PaymentSucceededEvent (webhook confirmed)
    PENDING --> EXPIRED : 15 min without payment
    PAID --> [*]
    EXPIRED --> [*]

    note right of PENDING
        On creation, the bookingId is added to a
        Redis ZSET scored by expiry timestamp.
        A scheduler polls it every 60 s.
    end note

    note right of EXPIRED
        Emits tickets.order.expired (outbox).
        Consumers: booking-service (booking → EXPIRED)
        and event-service (reserved capacity released).
    end note
```

The booking mirrors these transitions on its side: `PENDING → CONFIRMED` on payment success, `PENDING → EXPIRED` on order expiry. The ZSET entry is removed only *after* the expiration is processed — if the service dies mid-sweep, the next tick retries it (`expireByBookingId` is idempotent).

## Where to look in the code

| Concern | Entry point |
|---|---|
| Capacity reservation (optimistic locking + retry) | `eventservice · TicketTypeService.reserveTickets` |
| Transactional outbox (writer + relay) | `shared-infra · OutboxEventWriter`, `OutboxRelay` |
| Idempotent payment fan-out | `ticketservice · TicketOwnershipService.processPayment` |
| Order expiration sweep | `orderservice · OrderExpirationScheduler` |
| Retry / DLT policy | `shared-infra · SharedKafkaAutoConfiguration` |
| Stripe webhook verification | `paymentservice · WebHookController` |
