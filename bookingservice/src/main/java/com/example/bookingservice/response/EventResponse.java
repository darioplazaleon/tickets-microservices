package com.example.bookingservice.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record EventResponse (
        UUID id,
        String event,
        int capacity,
        VenueResponse venue,
        BigDecimal ticketPrice
) {
}
