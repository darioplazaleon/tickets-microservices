package com.example.paymentservice.service;

import com.example.paymentservice.client.OrderServiceClient;
import com.example.paymentservice.event.PaymentEvent;
import com.example.paymentservice.request.ProductRequest;
import com.example.paymentservice.response.OrderResponse;
import com.example.paymentservice.response.StripeResponse;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.model.climate.Order;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.service.climate.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    @Value("${stripe.secretKey}")
    private String secretKey;

    private final OrderServiceClient orderServiceClient;
    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    public StripeResponse checkoutProducts(Long orderId) {
        Stripe.apiKey = secretKey;

        OrderResponse orderResponse = orderServiceClient.getOrder(orderId);

        System.out.println(orderResponse);

        SessionCreateParams.LineItem.PriceData.ProductData productData = SessionCreateParams.LineItem.PriceData.ProductData.builder()
                .setName(orderResponse.eventId().toString())
                .build();

        SessionCreateParams.LineItem.PriceData priceData = SessionCreateParams.LineItem.PriceData.builder()
                .setCurrency("usd")
                .setUnitAmount(orderResponse.totalPrice().multiply(BigDecimal.valueOf(100)).longValue())
                .setProductData(productData)
                .build();

        SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                .setQuantity(1L)
                .setPriceData(priceData)
                .build();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:8080/success")
                .setCancelUrl("http://localhost:8080/cancel")
                .addLineItem(lineItem)
                .putMetadata("order_id", orderResponse.eventId().toString())
                .build();

        Session session = null;

        try {
            session = Session.create(params);
        } catch (StripeException e) {
            System.out.println(e.getMessage());
        }

        return new StripeResponse(
                "success",
                "Payment session created successfully",
                session
        );
    }

    public void updateOrderStatus(Long orderId) {
        orderServiceClient.updateOrderStatus(orderId, "COMPLETED");
        log.info("Order status with id: {} updated to COMPLETED", orderId);

        OrderResponse orderResponse = orderServiceClient.getOrder(orderId);
        var paymentEvent = new PaymentEvent(orderResponse);

        kafkaTemplate.send("payment-success", paymentEvent);
        log.info("Payment event sent to kafka topic: {}", paymentEvent);
    }
}
