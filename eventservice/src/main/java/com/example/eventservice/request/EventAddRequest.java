package com.example.eventservice.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record EventAddRequest(
    String name,
    Long venueId,
    LocalDateTime startDate,
    LocalDateTime endDate,
    Long categoryId,
    List<String> tagsNames,
    BigDecimal ticketPrice) {}
