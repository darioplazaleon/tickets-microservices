package com.example.bookingservice.event;

import java.math.BigDecimal;

public record TicketEvent(
        String ticketType,
        int quantity,
        BigDecimal unitPrice
) {}
