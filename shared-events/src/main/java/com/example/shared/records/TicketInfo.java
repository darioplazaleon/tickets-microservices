package com.example.shared.records;

import java.math.BigDecimal;

public record TicketInfo(
        String ticketType,
        int quantity,
        BigDecimal unitPrice
) {
}
