package com.example.orderservice.response;

import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderResponse(
        UUID orderId,
        BigDecimal totalPrice,
        int quantity,
        LocalDateTime timestamp,
        OrderStatus status,
        UUID customerId,
        UUID eventId
) {
    public OrderResponse(Order order) {
        this(
                order.getId(),
                order.getTotalPrice(),
                order.getTicketCount(),
                order.getCreatedAt(),
                order.getStatus(),
                order.getCustomerId(),
                order.getEventId()
        );
    }
}
