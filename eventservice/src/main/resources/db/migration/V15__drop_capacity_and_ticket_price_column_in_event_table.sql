ALTER TABLE events
    DROP COLUMN IF EXISTS left_capacity;

ALTER TABLE events
    DROP COLUMN IF EXISTS ticket_price;

ALTER TABLE events
    DROP COLUMN IF EXISTS capacity;

ALTER TABLE events
    ADD COLUMN created_by_user_id UUID NOT NULL default uuid_generate_v4();