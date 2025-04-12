package com.example.bookingservice.response;

import java.math.BigDecimal;
import java.util.UUID;

import com.example.bookingservice.entity.Booking;
import lombok.Builder;

@Builder
public record BookingResponse(
        UUID bookingId, UUID customerId, UUID eventId, int ticketCount, BigDecimal totalPrice) {

}
