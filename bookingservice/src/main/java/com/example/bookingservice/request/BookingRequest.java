package com.example.bookingservice.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public record BookingRequest (
        Long userId,
        Long eventId,
        Long ticketCount
) {

}
