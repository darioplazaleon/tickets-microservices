package com.example.notificationservice.response;

import java.math.BigDecimal;
import java.util.List;

public record OrderSummary(
        BigDecimal totalPrice,
        List<TicketSummary> tickets
) {
    public record TicketSummary(
            String ticketType,
            int quantity
    ){}
}
