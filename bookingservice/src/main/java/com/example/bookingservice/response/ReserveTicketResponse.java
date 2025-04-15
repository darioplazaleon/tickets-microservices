package com.example.bookingservice.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReserveTicketResponse(
        String ticketType,
        BigDecimal unitPrice,
        int reservedQuantity,
        LocalDateTime reservedUntil
) {}
