package com.example.paymentservice.controller;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebHookController {

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
            case "payment_intent.succeeded":
                System.out.println("Payment succeeded");
                break;

            case "payment_intent.payment_failed":
                System.out.println("Payment failed");
                break;

            case "checkout.session.completed":
                System.out.println("Checkout session completed");
                break;

            default:
                System.out.println("Unhandled event type: " + event.getType());
        }
    }
}
