ALTER TABLE events
    ADD COLUMN category_id UUID;

ALTER TABLE events
    ADD CONSTRAINT fk_category
        FOREIGN KEY (category_id)
            REFERENCES categories (id);