package com.example.eventservice.service.events;

import com.example.eventservice.entity.ProcessedEvent;
import com.example.eventservice.entity.TicketType;
import com.example.eventservice.repository.ProcessedEventRepository;
import com.example.eventservice.repository.TicketTypeRepository;
import com.example.shared.events.OrderExpiredEvent;
import com.example.shared.records.TicketInfoSimple;
import jakarta.transaction.Transactional;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventExpirationService {

  private final TicketTypeRepository ticketTypeRepository;
  private final ProcessedEventRepository processedEventRepository;

  // Transaccional para que un retry por choque de @Version re-ejecute el lote
  // completo desde cero en vez de re-aplicar tickets ya guardados.
  @Retryable(
      retryFor = ObjectOptimisticLockingFailureException.class,
      maxAttempts = 3,
      backoff = @Backoff(delay = 50))
  @Transactional
  public void processOrderExpiration(OrderExpiredEvent event) {
    // Kafka entrega at-least-once: un duplicado volvería a restar reserved,
    // robándole capacidad a holds de otras órdenes.
    String eventKey = "order-expired:" + event.orderId();
    if (processedEventRepository.existsById(eventKey)) {
      log.warn("[Event Service] Event {} already processed, skipping duplicate", eventKey);
      return;
    }

    for (TicketInfoSimple ticketInfo : event.tickets()) {
      TicketType type =
          ticketTypeRepository
              .findByEventIdAndNameIgnoreCase(event.eventId(), ticketInfo.ticketType())
              .orElse(null);

      if (type == null) {
        log.warn(
            "TicketType '{}' not found for eventId: {}. Ignoring ticket info.",
            ticketInfo.ticketType(),
            event.eventId());
        continue;
      }

      int newReserved = type.getReserved() - ticketInfo.quantity();
      type.setReserved(Math.max(newReserved, 0));
      ticketTypeRepository.save(type);

      log.info(
          "Released {} tickets type '{}' (reserved -> {}).",
          ticketInfo.quantity(),
          type.getName(),
          type.getReserved());
    }

    processedEventRepository.save(
        ProcessedEvent.builder().eventKey(eventKey).processedAt(Instant.now()).build());

    log.info(
        "Event {} updated successfully after order expiration {}.", event.eventId(), event.orderId());
  }
}
