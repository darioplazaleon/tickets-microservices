package com.example.shared.data;

import java.util.UUID;

public record TicketData(
        UUID id,
        UUID eventId,
        String ticketType
) {
}
