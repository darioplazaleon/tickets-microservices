package com.example.paymentservice.service;

import com.example.paymentservice.client.OrderServiceClient;
import com.example.paymentservice.event.outgoing.PaymentSucceededEvent;
import com.example.paymentservice.messaging.publisher.PaymentEventPublisher;
import com.example.paymentservice.response.OrderResponse;
import com.example.paymentservice.response.StripeResponse;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    @Value("${stripe.secretKey}")
    private String secretKey;

    private final OrderServiceClient orderClient;
    private final PaymentEventPublisher paymentEventPublisher;

    public StripeResponse checkoutProducts(UUID orderId) {
        Stripe.apiKey = secretKey;

        OrderResponse order = orderClient.getOrder(orderId);

        if (order == null) {
            log.error("Order with ID: {} not found", orderId);
            throw new IllegalArgumentException("Order not found");
        }

        if (!order.status().equalsIgnoreCase("PENDING")) {
            throw new IllegalStateException("Order is not available for payment");
        }

        List<SessionCreateParams.LineItem> lineItems = order.tickets().stream()
                .map(ticket -> {
                    SessionCreateParams.LineItem.PriceData.ProductData productData =
                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName(ticket.ticketType())
                                    .build();

                    SessionCreateParams.LineItem.PriceData priceData =
                            SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency("usd")
                                    .setUnitAmount(ticket.unitPrice().multiply(BigDecimal.valueOf(100)).longValue())
                                    .setProductData(productData)
                                    .build();

                    return SessionCreateParams.LineItem.builder()
                            .setQuantity((long) ticket.quantity())
                            .setPriceData(priceData)
                            .build();
                }).toList();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:8080/success")
                .setCancelUrl("http://localhost:8080/cancel")
                .addAllLineItem(lineItems)
                .putMetadata("order_id", order.orderId().toString())
                .build();

        try {
            Session session = Session.create(params);
            log.info("Session created for orderId {}", orderId);
            return new StripeResponse("success", "Session created successfully", session);
        } catch (StripeException e) {
            log.error("Error creating Stripe session: {}", e.getMessage(), e);
            return new StripeResponse("error", "Failed to create session", null);
        }
    }

    public void handlePaymentSuccess(String orderIdRaw) {
        UUID orderId = UUID.fromString(orderIdRaw);
        OrderResponse order = orderClient.getOrder(orderId);
        paymentEventPublisher.emitPaymentSucceededEvent(order);
    }

}
