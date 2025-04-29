package com.example.paymentservice.controller;

import com.example.paymentservice.service.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class WebHookController {

    private final PaymentService paymentService;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebHook(
            @RequestHeader("Stripe-Signature") String signature,
            @RequestBody String payload) {

        log.info("Received webhook request");

        Event event;
        try {
            event = Webhook.constructEvent(payload, signature, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.warn("Signature verification failed in webhook: {}", e.getMessage());
            return ResponseEntity.status(400).body("Invalid signature");
        }

        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        StripeObject object = deserializer.getObject().orElse(null);

        if (object == null) {
            log.warn("Stripe object is null");
            return ResponseEntity.badRequest().body("Invalid object");
        }

        switch (event.getType()) {
            case "checkout.session.completed":
                if (object instanceof Session session) {
                    String orderId = session.getMetadata().get("order_id");
                    log.info("checkout.session.completed -> orderId={}", orderId);
                    paymentService.handlePaymentSuccess(orderId);
                }
                break;

            case "payment_intent.payment_failed":
                log.warn("Payment failed");
                break;

            default:
                log.info("Unhandled event type: {}", event.getType());
        }

        return ResponseEntity.ok("Webhook handled");
    }
}
