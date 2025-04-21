package com.example.ticketservice.messaging.listener;

import com.example.shared.events.PaymentSucceededEvent;
import com.example.ticketservice.service.TicketOwnershipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentSuccessEventListener {

    private final TicketOwnershipService ticketOwnershipService;

    @KafkaListener(topics = "tickets.payment.success", groupId = "ticket-service")
    public void handlePaymentSuccess(PaymentSucceededEvent event) {
        log.info(
                "[Ticket Service] PaymentSucceededEvent received: eventId={}, correlationId={}",
                event.eventId(),
                event.correlationId());
        ticketOwnershipService.processPayment(event);
        log.info(
                "[Ticket Service] PaymentSucceededEvent processed: eventId={}, correlationId={}",
                event.eventId(),
                event.correlationId());
    }
}
