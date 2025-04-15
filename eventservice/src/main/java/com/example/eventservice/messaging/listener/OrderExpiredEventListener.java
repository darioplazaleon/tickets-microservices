package com.example.eventservice.messaging.listener;

import com.example.eventservice.entity.Event;
import com.example.eventservice.entity.TicketType;
import com.example.eventservice.event.incoming.OrderExpiredEvent;
import com.example.eventservice.repository.EventRepository;
import com.example.eventservice.repository.TicketTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderExpiredEventListener {

    private final EventRepository eventRepository;
    private final TicketTypeRepository ticketTypeRepository;

    @KafkaListener(topics = "tickets.order.expired", groupId = "event-service")
    public void handleOrderExpired(OrderExpiredEvent event, ConsumerRecord<String, OrderExpiredEvent> record) {
        log.info("[EventService] OrderExpiredEvent received: orderId={}, bookingId={}, correlationId={}",
                event.orderId(), event.bookingId(), event.correlationId());

        Optional<Event> eventOptional = eventRepository.findById(event.orderId());
        if (eventOptional.isEmpty()) {
            log.warn("Event not found for orderId: {}. Ignoring event.", event.orderId());
            return;
        }

        Event e = eventOptional.get();

        for (OrderExpiredEvent.TicketInfo ticketInfo : event.tickets()) {
            TicketType type = ticketTypeRepository.findByEventIdAndNameIgnoreCase(event.eventId(), ticketInfo.ticketType())
                    .orElse(null);

            if (type == null) {
                log.warn("TicketType '{}' not found for eventId: {}. Ignoring ticket info.", ticketInfo.ticketType(), event.eventId());
                continue;
            }

            int newReserved = type.getReserved() - ticketInfo.quantity();
            type.setReserved(Math.max(newReserved, 0));
            ticketTypeRepository.save(type);

            log.info("Released {} tickets type '{}' (reserved -> {}).",
                    ticketInfo.quantity(), type.getName(), type.getReserved());
        }

        log.info("Event {} updated successfully after order expiration {}.", e.getId(), event.orderId());
    }
}
