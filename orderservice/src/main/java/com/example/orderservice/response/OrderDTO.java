package com.example.orderservice.response;

import com.example.orderservice.entity.OrderStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderDTO(
        UUID id,
        UUID customerId,
        UUID eventId,
        UUID bookingId,
        OrderStatus status,
        BigDecimal totalPrice,
        List<TicketItem> tickets
) {
    public record TicketItem(
            String ticketType,
            int quantity,
            BigDecimal unitPrice
    ) {
    }
}
