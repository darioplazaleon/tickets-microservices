package com.example.bookingservice.messaging.listener;

import com.example.bookingservice.service.BookingPaymentService;
import com.example.shared.events.PaymentSucceededEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingPaymentEventListener {

    private final BookingPaymentService bookingPaymentService;

    @KafkaListener(topics = "tickets.payment.success", groupId = "booking-service")
    public void handlePaymentSuccess(PaymentSucceededEvent event) {
        log.info("[BookingService] Payment success event received: {}", event);
        bookingPaymentService.processPaymentSuccess(event);
    }
}
