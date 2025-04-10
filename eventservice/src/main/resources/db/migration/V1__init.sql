CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE venues
(
    id UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    name           VARCHAR(255) NOT NULL,
    address        VARCHAR(255) NOT NULL,
    total_capacity INT          NOT NULL,
    city           VARCHAR(255) NOT NULL
);

CREATE TABLE events
(
    id UUID       PRIMARY KEY DEFAULT uuid_generate_v4(),
    name          VARCHAR(255) NOT NULL,
    start_date    TIMESTAMP    NOT NULL,
    end_date      TIMESTAMP    NOT NULL,
    left_capacity INT          NOT NULL,
    status        VARCHAR(50)  NOT NULL,
    venue_id      UUID         NOT NULL,
    FOREIGN KEY (venue_id) REFERENCES venues (id)
);