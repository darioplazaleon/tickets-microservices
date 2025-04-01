package com.example.orderservice.response;

import com.example.orderservice.entity.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderResponse(
        Long orderId,
        BigDecimal totalPrice,
        Long quantity,
        LocalDateTime timestamp,
        String status,
        Long customerId,
        Long eventId
) {
    public OrderResponse(Order order) {
        this(
                order.getId(),
                order.getTotalPrice(),
                order.getTicketCount(),
                order.getCreatedAt(),
                order.getStatus().name(),
                order.getCustomerId(),
                order.getEventId()
        );
    }
}
