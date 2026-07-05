-- Baseline del schema "booking" (schema-per-service).
-- event_id se guarda como uuid plano: pertenece al schema "event" de otro servicio
-- y no hay FKs entre schemas de distintos servicios.

-- Tabla slim de clientes: id = "sub" del JWT de Keycloak. Keycloak es la fuente de
-- verdad de identidad/credenciales; esto es la proyección local del perfil
-- (preparada para JIT provisioning). created_at/birthday son date porque la entidad usa LocalDate.
CREATE TABLE customers
(
    id           uuid PRIMARY KEY,
    username     varchar(255) NOT NULL UNIQUE,
    email        varchar(255) NOT NULL UNIQUE,
    full_name    varchar(255),
    phone_number varchar(255),
    created_at   date,
    birthday     date,
    country      varchar(255)
);

CREATE TABLE bookings
(
    id          uuid PRIMARY KEY,
    customer_id uuid           NOT NULL REFERENCES customers (id),
    event_id    uuid           NOT NULL,
    status      varchar(255)   NOT NULL,
    total_price numeric(19, 2) NOT NULL,
    created_at  timestamptz    NOT NULL,
    updated_at  timestamptz
);

CREATE INDEX idx_bookings_customer_id ON bookings (customer_id);

CREATE TABLE booking_tickets
(
    id          uuid PRIMARY KEY,
    ticket_type varchar(255),
    quantity    int,
    unit_price  numeric(19, 2),
    booking_id  uuid NOT NULL REFERENCES bookings (id) ON DELETE CASCADE
);

CREATE INDEX idx_booking_tickets_booking_id ON booking_tickets (booking_id);
