package com.example.paymentservice.controller;

import com.example.paymentservice.service.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WebHookController {

    private final PaymentService paymentService;

    @PostMapping("/webhook")
    public void handleWebHook(@RequestHeader("Stripe-Signature") String signature, @RequestBody String payload) {
        System.out.println("Webhook received");
//        System.out.println("Signature: " + signature);
//        System.out.println("Payload: " + payload);
        Event event = null;

        try {
            event = Webhook.constructEvent(payload, signature, "whsec_420c181d02d264c1467742bdad20e274f008e98ad1f310979a9853c2faa1b535");
//            System.out.println("Event parsed" + event);
        } catch ( SignatureVerificationException e) {
            System.out.println("Invalid webhook signature: " + e.getMessage());
        }

        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        StripeObject object = null;

        if (deserializer.getObject().isPresent()) {
            object = deserializer.getObject().get();
        } else {
            System.out.println("Webhook error: no valid object found");
        }

        switch (event.getType()) {
            case "checkout.session.completed":
                if (object instanceof Session) {
                    Session checkoutSession = (Session) object;
                    String orderId = checkoutSession.getMetadata().get("order_id");
                    System.out.println("Received order id from Stripe webhook: " + orderId);
                    paymentService.updateSuccessOrderStatus(Long.parseLong(orderId));
                }
                break;

            case "payment_intent.payment_failed":
                System.out.println("Payment failed");
                break;

            default:
                System.out.println("Unhandled event type: " + event.getType());
        }
    }
}
