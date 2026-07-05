-- Baseline del schema "event" (schema-per-service: cada servicio es dueño de su propio schema).
-- Nota: events.start_date/end_date son timestamp SIN zona horaria porque la entidad usa LocalDateTime.
-- El resto de los campos de auditoría (Instant) usan timestamptz.

CREATE TABLE venues
(
    id             uuid PRIMARY KEY,
    name           varchar(255) NOT NULL UNIQUE,
    total_capacity int          NOT NULL,
    address        varchar(255) NOT NULL,
    city           varchar(255) NOT NULL
);

CREATE TABLE categories
(
    id   uuid PRIMARY KEY,
    name varchar(255) NOT NULL UNIQUE
);

CREATE TABLE tags
(
    id   uuid PRIMARY KEY,
    name varchar(255) NOT NULL UNIQUE
);

CREATE TABLE events
(
    id                 uuid PRIMARY KEY,
    name               varchar(255) NOT NULL,
    start_date         timestamp    NOT NULL,
    end_date           timestamp    NOT NULL,
    status             varchar(255),
    venue_id           uuid REFERENCES venues (id),
    category_id        uuid REFERENCES categories (id),
    created_at         timestamptz,
    updated_at         timestamptz,
    created_by_user_id uuid         NOT NULL
);

CREATE INDEX idx_events_name ON events (name);

CREATE TABLE event_tags
(
    event_id uuid NOT NULL REFERENCES events (id) ON DELETE CASCADE,
    tag_id   uuid NOT NULL REFERENCES tags (id),
    PRIMARY KEY (event_id, tag_id)
);

-- La capacidad vive únicamente acá (events.left_capacity se eliminó: era una
-- segunda fuente de verdad que podía divergir de los contadores por tipo).
CREATE TABLE ticket_type
(
    id       uuid PRIMARY KEY,
    name     varchar(255)   NOT NULL,
    capacity int            NOT NULL,
    reserved int            NOT NULL DEFAULT 0,
    sold     int            NOT NULL DEFAULT 0,
    price    numeric(19, 2) NOT NULL,
    version  bigint         NOT NULL DEFAULT 0,
    event_id uuid           NOT NULL REFERENCES events (id) ON DELETE CASCADE,
    -- Red de seguridad contra oversell: la DB garantiza que nunca se reserva/vende
    -- por encima de la capacidad, sin importar bugs de concurrencia en la aplicación.
    CONSTRAINT chk_ticket_type_counts
        CHECK (reserved >= 0 AND sold >= 0 AND reserved + sold <= capacity)
);

-- Único por evento + nombre case-insensitive: el lookup es findByEventIdAndNameIgnoreCase.
CREATE UNIQUE INDEX uq_ticket_type_event_name ON ticket_type (event_id, lower(name));
