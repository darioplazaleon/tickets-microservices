CREATE TABLE orders
(
    id                UUID PRIMARY KEY        DEFAULT uuid_generate_v4(),
    total             DECIMAL(10, 2) NOT NULL,
    quantity          INT            NOT NULL,
    correlation_id    UUID,
    created_at        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    customer_id       UUID           NOT NULL,
    event_id          UUID           NOT NULL,
    payment_intent_id TEXT,
    paid_at           TIMESTAMP,
    CONSTRAINT fk_order_customer FOREIGN KEY (customer_id)
        REFERENCES customers (id) ON DELETE SET NULL,
    CONSTRAINT fk_order_event FOREIGN KEY (event_id)
        REFERENCES events (id) ON DELETE SET NULL
);