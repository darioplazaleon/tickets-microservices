package com.example.bookingservice.request;

public record ReserveTicketRequest(
        String ticketType,
        int quantity
) {
}
