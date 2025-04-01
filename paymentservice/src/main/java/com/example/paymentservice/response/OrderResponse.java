package com.example.paymentservice.response;

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
}
