-- Baseline del schema "ticket" (schema-per-service).
-- Todas las referencias (order, event, buyers) son uuid planos de otros servicios: sin FKs entre schemas.

CREATE TABLE ticket_ownerships
(
    id                uuid PRIMARY KEY,
    order_id          uuid         NOT NULL,
    event_id          uuid         NOT NULL,
    original_buyer_id uuid         NOT NULL,
    current_owner_id  uuid         NOT NULL,
    ticket_type       varchar(255) NOT NULL,
    used              boolean      NOT NULL DEFAULT false,
    transferable      boolean      NOT NULL DEFAULT false,
    transferred_at    timestamptz,
    created_at        timestamptz  NOT NULL
);

CREATE INDEX idx_ticket_ownerships_current_owner ON ticket_ownerships (current_owner_id);
CREATE INDEX idx_ticket_ownerships_order_id ON ticket_ownerships (order_id);
