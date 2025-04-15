package com.example.bookingservice.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record BookingEvent(
    UUID bookingId,
    UUID userId,
    UUID eventId,
    List<TicketInfo> tickets,
    UUID correlationId,
    BigDecimal totalPrice,
    Instant createdAt) {
  public record TicketInfo(String ticketType, int quantity, BigDecimal unitPrice) {}
}
