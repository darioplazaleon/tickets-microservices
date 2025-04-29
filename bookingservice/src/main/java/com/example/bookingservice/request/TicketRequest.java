package com.example.bookingservice.request;

import lombok.Builder;

@Builder
public record TicketRequest(
        String ticketType,
        int quantity
) {}
