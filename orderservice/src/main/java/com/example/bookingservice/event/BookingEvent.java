package com.example.bookingservice.event;


import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
public record BookingEvent(Long customerId, Long eventId, Long ticketCount, String correlationId, String userId, BigDecimal totalPrice, Instant createdAt) {}
