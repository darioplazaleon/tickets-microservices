package com.example.orderservice.response;

import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID orderId,
        UUID customerId,
        UUID eventId,
        BigDecimal totalPrice,
        OrderStatus status,
        Instant expiresAt,
        List<TicketItem> tickets
) {
    public record TicketItem(
            String ticketType,
            int quantity,
            BigDecimal unitPrice
    ) {}

}
