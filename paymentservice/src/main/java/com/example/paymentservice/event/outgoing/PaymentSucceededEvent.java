package com.example.paymentservice.event.outgoing;

import com.example.paymentservice.response.OrderResponse;

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
    public record TicketInfo(String ticketType, int quantity, BigDecimal unitPrice) {}
}
