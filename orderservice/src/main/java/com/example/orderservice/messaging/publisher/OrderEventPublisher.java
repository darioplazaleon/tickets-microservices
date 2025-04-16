package com.example.orderservice.messaging.publisher;

import com.example.orderservice.entity.Order;
import com.example.shared.events.OrderExpiredEvent;
import com.example.shared.records.TicketInfoSimple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final KafkaTemplate<String, OrderExpiredEvent> orderKafkaTemplate;

    private static final String TOPIC = "tickets.order.expired";

    public void sendOrderExpiredEvent(Order order, UUID userId, UUID correlationId) {
        OrderExpiredEvent event = createOrderExpiredEvent(order);

        ProducerRecord<String, OrderExpiredEvent> producerRecord = new ProducerRecord<>(TOPIC, event);
        producerRecord.headers().add("userId", userId.toString().getBytes(StandardCharsets.UTF_8));
        producerRecord.headers().add("correlationId", correlationId.toString().getBytes(StandardCharsets.UTF_8));

        orderKafkaTemplate.send(producerRecord);
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
