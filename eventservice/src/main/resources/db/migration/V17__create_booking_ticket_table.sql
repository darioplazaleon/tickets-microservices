CREATE TABLE booking_tickets (
                                 id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                 ticket_type VARCHAR(255) NOT NULL,
                                 quantity INT NOT NULL,
                                 unit_price NUMERIC(19, 2) NOT NULL,
                                 booking_id UUID,
                                 CONSTRAINT fk_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
);