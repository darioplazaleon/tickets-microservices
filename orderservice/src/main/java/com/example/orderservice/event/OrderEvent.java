package com.example.orderservice.event;

import lombok.Builder;

@Builder
public record OrderEvent(
        String orderId,

) {
}
