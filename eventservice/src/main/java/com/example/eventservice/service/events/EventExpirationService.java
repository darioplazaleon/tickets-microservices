package com.example.eventservice.service.events;

import com.example.eventservice.entity.Event;
import com.example.eventservice.entity.TicketType;
import com.example.eventservice.repository.EventRepository;
import com.example.eventservice.repository.TicketTypeRepository;
import com.example.shared.events.OrderExpiredEvent;
import com.example.shared.records.TicketInfoSimple;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventExpirationService {

  private final EventRepository eventRepository;
  private final TicketTypeRepository ticketTypeRepository;

  public void processOrderExpiration(OrderExpiredEvent event) {
    Optional<Event> eventOptional = eventRepository.findById(event.orderId());
    if (eventOptional.isEmpty()) {
      log.warn("Event not found for orderId: {}. Ignoring event.", event.orderId());
      return;
    }

    Event domainEvent = eventOptional.get();

    for (TicketInfoSimple ticketInfo : event.tickets()) {
      TicketType type =
          ticketTypeRepository
              .findByEventIdAndNameIgnoreCase(domainEvent.getId(), ticketInfo.ticketType())
              .orElse(null);

      if (type == null) {
        log.warn(
            "TicketType '{}' not found for eventId: {}. Ignoring ticket info.",
            ticketInfo.ticketType(),
            domainEvent.getId());
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

    log.info(
        "Event {} updated successfully after order expiration {}.", domainEvent.getId(), event.orderId());
  }
}
