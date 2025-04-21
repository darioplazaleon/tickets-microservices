package com.example.shared.events;

import java.time.Instant;
import java.util.UUID;

public record TicketMasterQrEvent(
        UUID orderId,
        UUID eventId,
        UUID ownerId,
        String qrBase64,
        Instant createdAt
) {
}
