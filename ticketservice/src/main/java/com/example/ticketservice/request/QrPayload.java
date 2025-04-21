package com.example.ticketservice.request;

import java.util.UUID;

public record QrPayload(
        UUID ticketId,
        UUID ownerId,
        UUID eventId,
        String timestamp,
        String signature
) {
}
