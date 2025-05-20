package com.example.shared.data;

import java.time.LocalDateTime;
import java.util.UUID;

public record EventSimpleData(
        UUID id,
        String event,
        LocalDateTime startDate,
        String venueName,
        String venueAddress
) {
}
