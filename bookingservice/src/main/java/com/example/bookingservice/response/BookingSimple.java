package com.example.bookingservice.response;

import com.example.bookingservice.entity.BookingStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
public record BookingSimple(
        UUID id,
        UUID eventId,
        BookingStatus status,
        Instant createdAt
) {}
