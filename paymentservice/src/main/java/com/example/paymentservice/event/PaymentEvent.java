package com.example.paymentservice.event;

import com.example.paymentservice.response.OrderResponse;

public record PaymentEvent(
        Long eventId,
        Long orderId,
        Long customerId,
        Long ticketCount
) {
    public PaymentEvent(OrderResponse response) {
        this(
                response.eventId(),
                response.orderId(),
                response.customerId(),
                response.quantity()
        );
    }
}
