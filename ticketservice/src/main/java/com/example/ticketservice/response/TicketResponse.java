package com.example.ticketservice.response;

import java.util.UUID;

public record TicketResponse(
        UUID ticketId,
        UUID eventId,
        String ticketType,
        boolean used,
        boolean transferable,
        String qrCodeBase64
) {}
