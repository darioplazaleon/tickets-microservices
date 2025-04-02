package com.example.paymentservice.event;

public record PaymentEvent(
        Long eventId,
        Long orderId,
        Long customerId,
        Long ticketCount
) {
}
