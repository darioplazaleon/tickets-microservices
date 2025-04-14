CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE ticket_type (
                             id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                             name VARCHAR(255) NOT NULL,
                             capacity INT NOT NULL,
                             reserved INT NOT NULL DEFAULT 0,
                             sold INT NOT NULL DEFAULT 0,
                             price NUMERIC(19, 2) NOT NULL,
                             event_id UUID,
                             CONSTRAINT fk_event FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE
);