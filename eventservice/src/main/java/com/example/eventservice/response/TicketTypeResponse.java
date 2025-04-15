package com.example.eventservice.response;

import java.math.BigDecimal;

public record TicketTypeResponse(
        String ticketType,
        int quantity,
        BigDecimal unitPrice
) {}
