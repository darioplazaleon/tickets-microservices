package com.example.orderservice.messaging.publisher;

import com.example.orderservice.entity.Order;
import com.example.shared.events.OrderExpiredEvent;
import com.example.shared.infra.outbox.OutboxEventWriter;
import com.example.shared.messaging.Topics;
import com.example.shared.records.TicketInfoSimple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final OutboxEventWriter outboxEventWriter;

    private static final String TOPIC = Topics.ORDER_EXPIRED;

    public void sendOrderExpiredEvent(Order order, UUID userId, UUID correlationId) {
        OrderExpiredEvent event = createOrderExpiredEvent(order);

        outboxEventWriter.append(TOPIC, order.getId().toString(), event,
                correlationId != null ? correlationId.toString() : null);
    }


    private OrderExpiredEvent createOrderExpiredEvent(Order order) {
        List<TicketInfoSimple> tickets = order.getTicketItems().stream()
                .map(t -> new TicketInfoSimple(
                        t.getTicketType(),
                        t.getQuantity()
                ))
                .toList();

        return new OrderExpiredEvent(
                order.getId(),
                order.getBookingId(),
                order.getEventId(),
                tickets,
                order.getCorrelationId(),
                Instant.now()
        );
    }
}
