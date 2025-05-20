package com.example.orderservice.messaging.listener;

import com.example.orderservice.service.OrderService;
import com.example.shared.events.BookingCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private static final String ZSET_KEY = "orderExpiryZSet";

    private final StringRedisTemplate redis;
    private final OrderService orderService;

    @KafkaListener(topics = "tickets.booking.created", groupId = "order-service")
    public void handleBookingCreatedEvent(BookingCreatedEvent event) {
        log.info("[OrderService] BookingCreatedEvent received for bookingId: {} (correlationId={})", event.bookingId(), event.correlationId());
        orderService.createOrder(event);

        long expiresAt = event.createdAt().plusSeconds(15 * 60).toEpochMilli();
        redis.opsForZSet().add(ZSET_KEY, event.bookingId().toString(), expiresAt);

        log.info(
                "[OrderService] Order created for bookingId: {} (correlationId={}, to expire at={})",
                event.bookingId(), event.correlationId(), Instant.ofEpochMilli(expiresAt)
        );
    }
}
