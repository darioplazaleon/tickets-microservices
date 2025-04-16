package com.example.shared.events;

import com.example.shared.records.TicketInfo;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PaymentSucceededEvent(
        UUID orderId,
        UUID bookingId,
        UUID eventId,
        UUID userId,
        List<TicketInfo> tickets,
        BigDecimal totalPrice,
        UUID correlationId,
        Instant paidAt
) {
}