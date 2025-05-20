package com.example.bookingservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
public class TicketHold {
    private UUID eventId;
    private UUID userId;
    private Map<String, Integer> ticketTypeCounts;
    private Instant createdAt;
}
