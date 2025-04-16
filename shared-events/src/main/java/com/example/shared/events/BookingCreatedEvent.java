package com.example.shared.events;

import com.example.shared.records.TicketInfo;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record BookingCreatedEvent(
        UUID bookingId,
        UUID userId,
        UUID eventId,
        List<TicketInfo> tickets,
        UUID correlationId,
        BigDecimal totalPrice,
        Instant createdAt) {
}
