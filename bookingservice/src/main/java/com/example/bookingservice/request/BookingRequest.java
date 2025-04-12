package com.example.bookingservice.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

public record BookingRequest (
        UUID userId,
        UUID eventId,
        int ticketCount
) {
}
