package com.example.notificationservice.entity;

import java.time.LocalDateTime;

public record QrData (
        Long orderId,
        Long eventId,
        Long customerId,
        Long ticketCount,
        LocalDateTime timestamp,
        String signature
) {
}
