#!/bin/bash
# Se ejecuta una única vez por el entrypoint de la imagen oficial de Postgres
# (solo cuando el volumen de datos está vacío). Crea un rol y un schema por
# servicio: cada rol es OWNER únicamente de su schema, así el aislamiento
# entre servicios queda garantizado por permisos y no por convención.
set -euo pipefail

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE ROLE event_svc   LOGIN PASSWORD '${EVENT_DB_PASSWORD}';
    CREATE ROLE booking_svc LOGIN PASSWORD '${BOOKING_DB_PASSWORD}';
    CREATE ROLE order_svc   LOGIN PASSWORD '${ORDER_DB_PASSWORD}';
    CREATE ROLE ticket_svc  LOGIN PASSWORD '${TICKET_DB_PASSWORD}';

    -- Cerrar el acceso por defecto antes de repartir permisos
    REVOKE ALL    ON DATABASE ${POSTGRES_DB} FROM PUBLIC;
    REVOKE CREATE ON SCHEMA public FROM PUBLIC;

    GRANT CONNECT ON DATABASE ${POSTGRES_DB}
        TO event_svc, booking_svc, order_svc, ticket_svc;

    -- Cada rol es owner de su schema: Flyway puede crear/alterar tablas ahí,
    -- y nadie tiene USAGE sobre los schemas ajenos.
    CREATE SCHEMA event   AUTHORIZATION event_svc;
    CREATE SCHEMA booking AUTHORIZATION booking_svc;
    CREATE SCHEMA orders  AUTHORIZATION order_svc;
    CREATE SCHEMA ticket  AUTHORIZATION ticket_svc;

    -- Refuerzo del currentSchema de la JDBC URL para conexiones psql manuales
    ALTER ROLE event_svc   SET search_path = event;
    ALTER ROLE booking_svc SET search_path = booking;
    ALTER ROLE order_svc   SET search_path = orders;
    ALTER ROLE ticket_svc  SET search_path = ticket;
EOSQL
