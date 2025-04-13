CREATE TABLE bookings (
                          id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                          customer_id UUID NOT NULL,
                          event_id UUID NOT NULL,
                          quantity INT NOT NULL,
                          status  VARCHAR(255) NOT NULL,
                          created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP WITH TIME ZONE
);


ALTER TABLE bookings
    ADD CONSTRAINT fk_booking_event FOREIGN KEY (event_id)
        REFERENCES events(id) ON DELETE CASCADE;

ALTER TABLE bookings
    ADD CONSTRAINT fk_booking_customer FOREIGN KEY (customer_id)
        REFERENCES customers(id) ON DELETE CASCADE;