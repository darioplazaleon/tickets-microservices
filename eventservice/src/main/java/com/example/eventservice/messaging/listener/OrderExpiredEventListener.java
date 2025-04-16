package com.example.eventservice.messaging.listener;

import com.example.eventservice.service.EventExpirationService;
import com.example.shared.events.OrderExpiredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderExpiredEventListener {

  private final EventExpirationService eventExpirationService;

  @KafkaListener(topics = "tickets.order.expired", groupId = "event-service")
  public void handleOrderExpired(OrderExpiredEvent event) {
    log.info(
        "[EventService] OrderExpiredEvent received: orderId={}, bookingId={}, correlationId={}",
        event.orderId(),
        event.bookingId(),
        event.correlationId());

    eventExpirationService.processOrderExpiration(event);
  }
}
