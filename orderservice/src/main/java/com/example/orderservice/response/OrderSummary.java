package com.example.orderservice.response;

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
