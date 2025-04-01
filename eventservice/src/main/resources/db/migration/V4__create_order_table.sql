CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    total DECIMAL(10, 2) NOT NULL,
    quantity BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    customer_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    CONSTRAINT fk_order_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE SET NULL,
    CONSTRAINT fk_order_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE SET NULL
)