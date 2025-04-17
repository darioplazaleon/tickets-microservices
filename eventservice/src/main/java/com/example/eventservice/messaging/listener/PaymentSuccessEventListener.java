package com.example.eventservice.messaging.listener;

import com.example.eventservice.service.events.EventPaymentService;
import com.example.shared.events.PaymentSucceededEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentSuccessEventListener {

  private final EventPaymentService eventPaymentService;

  @KafkaListener(topics = "tickets.payment.success", groupId = "event-service")
  public void handlePaymentSuccess(PaymentSucceededEvent event) {
    log.info(
        "[Event Service] PaymentSucceededEvent received: eventId={}, correlationId={}",
        event.eventId(),
        event.correlationId());
    eventPaymentService.processPaymentSuccess(event);
    log.info(
        "[Event Service] PaymentSucceededEvent processed: eventId={}, correlationId={}",
        event.eventId(),
        event.correlationId());
  }
}
