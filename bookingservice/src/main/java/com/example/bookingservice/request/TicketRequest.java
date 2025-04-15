package com.example.bookingservice.request;

public record TicketRequest(
        String ticketType,
        int quantity
) {}
