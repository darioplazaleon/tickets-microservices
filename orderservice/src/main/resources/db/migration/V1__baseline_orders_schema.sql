-- Baseline del schema "orders" (schema-per-service; "order" es palabra reservada en SQL).
-- customer_id / event_id / booking_id son uuid planos: pertenecen a schemas de otros
-- servicios y no hay FKs entre schemas.

CREATE TABLE orders
(
    id                uuid PRIMARY KEY,
    total             numeric(19, 2) NOT NULL,
    total_quantity    int            NOT NULL,
    status            varchar(255)   NOT NULL,
    correlation_id    uuid           NOT NULL,
    created_at        timestamptz    NOT NULL,
    customer_id       uuid           NOT NULL,
    event_id          uuid           NOT NULL,
    booking_id        uuid           NOT NULL,
    expires_at        timestamptz    NOT NULL,
    payment_intent_id varchar(255),
    paid_at           timestamptz
);

CREATE INDEX idx_orders_customer_id ON orders (customer_id);

-- Índice parcial para findExpiredUnpaidOrders (status = 'PENDING' AND expires_at < now):
-- más chico y selectivo que un índice compuesto (status, expires_at).
CREATE INDEX idx_orders_pending_expires ON orders (expires_at) WHERE status = 'PENDING';

CREATE TABLE order_tickets
(
    id          uuid PRIMARY KEY,
    ticket_type varchar(255)   NOT NULL,
    quantity    int            NOT NULL,
    unit_price  numeric(19, 2) NOT NULL,
    order_id    uuid           NOT NULL REFERENCES orders (id) ON DELETE CASCADE
);

CREATE INDEX idx_order_tickets_order_id ON order_tickets (order_id);
