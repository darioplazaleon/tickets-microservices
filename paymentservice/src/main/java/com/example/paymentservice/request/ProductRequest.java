package com.example.paymentservice.request;

public record ProductRequest(
        Long amount,
        Long quantity,
        String name,
        String currency
) {
}
