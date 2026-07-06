-- Respaldo a nivel BD del guard de idempotencia en OrderService.createOrder:
-- una orden por booking, incluso si dos consumidores procesaran el mismo evento.
ALTER TABLE orders
    ADD CONSTRAINT uq_orders_booking_id UNIQUE (booking_id);
