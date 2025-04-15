package com.example.orderservice.scheduler;

import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderStatus;
import com.example.orderservice.event.OrderExpiredEvent;
import com.example.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderExpirationScheduler {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, OrderExpiredEvent> orderKafkaTemplate;

    private static final String TOPIC = "tickets.order.expired";

    @Scheduled(fixedDelay = 60__000)
    public void expireOldOrders() {
        Instant now = Instant.now();
        List<Order> expiredOrders = orderRepository.findExpiredUnpaidOrders(now);

        if (!expiredOrders.isEmpty()) {
            log.info("Expired orders found: {}", expiredOrders);
        }

        for(Order order : expiredOrders) {
            order.setStatus(OrderStatus.EXPIRED);
            orderRepository.save(order);

            OrderExpiredEvent event = createOrderExpiredEvent(order);
            sendOrderExpiredEvent(event, order.getCustomerId(), order.getCorrelationId());

            log.info("Order expired: {}", order.getId());
        }
    }

    private OrderExpiredEvent createOrderExpiredEvent(Order order) {
        List<OrderExpiredEvent.TicketInfo> tickets = order.getTicketItems().stream()
                .map(t -> new OrderExpiredEvent.TicketInfo(
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

    private void sendOrderExpiredEvent(OrderExpiredEvent event, UUID userId, UUID correlationId) {
        ProducerRecord<String, OrderExpiredEvent> producerRecord = new ProducerRecord<>(TOPIC, event);
        producerRecord.headers().add("userId", userId.toString().getBytes(StandardCharsets.UTF_8));
        producerRecord.headers().add("correlationId", correlationId.toString().getBytes(StandardCharsets.UTF_8));
        orderKafkaTemplate.send(producerRecord);
    }
}
