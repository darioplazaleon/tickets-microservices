package com.example.notificationservice.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record EventDetailsResponse (
    UUID id,
    String name,
    LocalDateTime startDate,
    String venueName,
    String venueAddress
) {
}


