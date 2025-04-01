package com.example.paymentservice.response;

import com.stripe.model.checkout.Session;

public record StripeResponse(
        String status,
        String message,
        String sessionId,
        String sessionUrl
) {
    public StripeResponse(String status, String message, Session session) {
        this(status, message, session.getId(), session.getUrl());
    }
}
