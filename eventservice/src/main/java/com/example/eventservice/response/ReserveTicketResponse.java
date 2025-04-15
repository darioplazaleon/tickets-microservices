package com.example.eventservice.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record ReserveTicketResponse(
        String ticketType,
        BigDecimal unitPrice,
        int reservedQuantity,
        LocalDateTime reservedUntil
) {}
