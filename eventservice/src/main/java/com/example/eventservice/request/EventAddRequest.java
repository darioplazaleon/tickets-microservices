package com.example.eventservice.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record EventAddRequest(
    String name,
    UUID venueId,
    LocalDateTime startDate,
    LocalDateTime endDate,
    UUID categoryId,
    List<String> tagsNames,
    BigDecimal ticketPrice) {}
