package com.example.eventservice.event.incoming;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderExpiredEvent(
        UUID orderId,
        UUID bookingId,
        UUID eventId,
        List<TicketInfo> tickets,
        UUID correlationId,
        Instant expiredAt
) {
    public record TicketInfo(
            String ticketType,
            int quantity
    ) {
    }
}

