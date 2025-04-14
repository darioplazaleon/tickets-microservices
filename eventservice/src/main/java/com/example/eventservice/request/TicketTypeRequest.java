package com.example.eventservice.request;

import java.math.BigDecimal;

public record TicketTypeRequest(
        String name,
        int capacity,
        BigDecimal price
) {}
