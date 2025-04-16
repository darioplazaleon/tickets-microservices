package com.example.shared.events;

import com.example.shared.records.TicketInfoSimple;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderExpiredEvent(
        UUID orderId,
        UUID bookingId,
        UUID eventId,
        List<TicketInfoSimple> tickets,
        UUID correlationId,
        Instant expiredAt
) {
}

