-- Transactional Outbox: el evento se persiste en la misma transacción que el
-- cambio de negocio y un scheduler (OutboxRelay de shared-infra) lo publica a
-- Kafka después. Evita el dual-write: si Kafka está caído, el evento queda
-- pendiente y se reintenta; si la transacción rollbackea, nunca se publica.
CREATE TABLE outbox_events
(
    id             uuid PRIMARY KEY,
    topic          varchar(100) NOT NULL,
    message_key    varchar(100),
    event_type     varchar(255) NOT NULL,
    payload        text         NOT NULL,
    correlation_id varchar(100),
    created_at     timestamptz  NOT NULL DEFAULT now(),
    published_at   timestamptz
);

CREATE INDEX idx_outbox_events_pending ON outbox_events (created_at) WHERE published_at IS NULL;
