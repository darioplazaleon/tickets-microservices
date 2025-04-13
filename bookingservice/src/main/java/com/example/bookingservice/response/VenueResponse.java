package com.example.bookingservice.response;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
public record VenueResponse (
    UUID id,
    String name,
    int totalCapacity,
    String address,
    String city
) {
}
