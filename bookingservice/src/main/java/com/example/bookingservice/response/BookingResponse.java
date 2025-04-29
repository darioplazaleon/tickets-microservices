package com.example.bookingservice.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.example.bookingservice.entity.Booking;
import com.example.bookingservice.request.TicketRequest;
import lombok.Builder;

@Builder
public record BookingResponse(
        UUID bookingId, UUID customerId, UUID eventId, BigDecimal totalPrice, List<TicketRequest> tickets) {
}
