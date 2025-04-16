package com.example.paymentservice.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;


public record OrderResponse(
        UUID orderId,
        UUID bookingId,
        UUID customerId,
        UUID eventId,
        BigDecimal totalPrice,
        String status,
        Instant expiresAt,
        UUID correlationId,
        List<TicketItem> tickets
) {
    public record TicketItem(
            String ticketType,
            int quantity,
            BigDecimal unitPrice
    ) {}
}

