-- Deduplicación de eventos Kafka (at-least-once): cada consumidor registra la clave
-- del evento procesado dentro de la misma transacción que el cambio de negocio.
-- Los contadores reserved/sold no son idempotentes por sí solos, así que un duplicado
-- sin este registro los corrompería.
CREATE TABLE processed_events
(
    event_key    varchar(120) PRIMARY KEY,
    processed_at timestamptz NOT NULL DEFAULT now()
);
