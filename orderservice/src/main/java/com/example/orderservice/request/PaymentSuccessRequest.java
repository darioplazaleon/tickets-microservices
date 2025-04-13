package com.example.orderservice.request;

import com.example.orderservice.entity.OrderStatus;

public record PaymentSuccessRequest(
        String paymentIntentId,
        OrderStatus status
) {
}
