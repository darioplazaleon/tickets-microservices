package com.example.eventservice.request;

public record ReserveTicketRequest(
        String ticketType,
        int quantity
) {}
