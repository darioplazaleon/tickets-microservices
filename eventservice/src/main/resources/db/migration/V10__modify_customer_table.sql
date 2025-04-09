ALTER TABLE customers
    DROP COLUMN name,
    DROP COLUMN address;

ALTER TABLE customers
    ADD COLUMN keycloak_id VARCHAR(255) UNIQUE,
    ADD COLUMN username VARCHAR(255) UNIQUE,
    ADD COLUMN full_name VARCHAR(255),
    ADD COLUMN phone_number VARCHAR(50),
    ADD COLUMN created_at DATE,
    ADD COLUMN birthday DATE,
    ADD COLUMN country VARCHAR(255);