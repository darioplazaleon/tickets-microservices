package com.example.orderservice.response;

import com.example.orderservice.entity.OrderStatus;

import java.time.Instant;
import java.util.UUID;

public record OrderSimple(
        UUID id,
        String orderStatus,
        OrderStatus status,
        Instant createdAt
) {
}
