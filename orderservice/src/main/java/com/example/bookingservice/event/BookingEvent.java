package com.example.bookingservice.event;


import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
public record BookingEvent(UUID bookingId, UUID customerId, UUID eventId, int ticketCount, UUID correlationId, String userId,
                           BigDecimal totalPrice, Instant createdAt) {
}
