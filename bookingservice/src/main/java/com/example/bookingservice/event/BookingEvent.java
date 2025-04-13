package com.example.bookingservice.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record BookingEvent(
    UUID bookingId,
    UUID customerId,
    UUID eventId,
    int ticketCount,
    UUID correlationId,
    String userId,
    BigDecimal totalPrice,
    Instant createdAt) {}
