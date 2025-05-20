package com.example.orderservice.scheduler;

import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderStatus;
import com.example.orderservice.messaging.publisher.OrderEventPublisher;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderExpirationScheduler {

    private static final String ZSET_KEY = "orderExpiryZSet";

    private final StringRedisTemplate redis;
    private final OrderEventPublisher publisher;
    private final OrderRepository orderRepository;

    @Scheduled(fixedDelay = 60__000)
    public void expireOldOrders() {
        long now = Instant.now().toEpochMilli();
        Set<String> expired = redis.opsForZSet().rangeByScore(ZSET_KEY, 0, now);
        if (expired == null || expired.isEmpty()) return;

        log.info("Expired orders found: {}", expired);

        for (String idStr : expired) {
            redis.opsForZSet().remove(ZSET_KEY, idStr);
            UUID orderId;

            try {
                orderId = UUID.fromString(idStr);
            } catch (IllegalArgumentException e) {
                log.error("Invalid order ID format: {}", idStr, e);
                continue;
            }

            orderRepository.findById(orderId).ifPresent(order -> {
                if (order.getStatus() == OrderStatus.PENDING) {
                    order.setStatus(OrderStatus.EXPIRED);
                    orderRepository.save(order);
                    log.info("Order {} expired", orderId);

                    publisher.sendOrderExpiredEvent(order, order.getCustomerId(), order.getCorrelationId());
                } else {
                    log.info("Order {} is not in PENDING status, skipping expiration", orderId);
                }
            });
        }
    }
}
