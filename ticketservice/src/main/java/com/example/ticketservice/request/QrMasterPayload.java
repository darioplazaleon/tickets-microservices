package com.example.ticketservice.request;

import java.util.List;
import java.util.UUID;

public record QrMasterPayload(
        UUID orderId,
        UUID ownerId,
        List<UUID> ticketsIds,
        String timestamp,
        String signature
) {
}
