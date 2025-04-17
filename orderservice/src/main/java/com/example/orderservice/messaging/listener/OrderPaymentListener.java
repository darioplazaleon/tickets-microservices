package com.example.orderservice.messaging.listener;

import com.example.orderservice.service.OrderPaymentService;
import com.example.shared.events.PaymentSucceededEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderPaymentListener {

    private final OrderPaymentService orderPaymentService;

    @KafkaListener(topics = "tickets.payment.success", groupId = "order-service")
    public void handlePaymentSucceeded(PaymentSucceededEvent event) {
        log.info("[OrderService] PaymentSucceededEvent received for orderId: {} (correlationId={})", event.orderId(), event.correlationId());
        orderPaymentService.processPaymentSucceeded(event);
        log.info("[OrderService] Order updated successfully after payment succeeded for orderId: {} (correlationId={})", event.orderId(), event.correlationId());
    }
}
