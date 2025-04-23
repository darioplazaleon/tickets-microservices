package com.example.eventservice.response;


import java.time.LocalDateTime;
import java.util.UUID;

public record EventNotificationResponse (
    UUID id,
    String event,
    LocalDateTime startDate,
    String venueName,
    String venueAddress
) {
}
