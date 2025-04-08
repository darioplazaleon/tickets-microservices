package com.example.eventservice.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EventAddRequest(
    String name,
    Long venueId,
    LocalDateTime startDate,
    LocalDateTime endDate,
    Long categoryId,
    BigDecimal ticketPrice) {}
