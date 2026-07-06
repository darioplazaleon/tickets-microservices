package com.example.orderservice.scheduler;

import com.example.orderservice.service.OrderExpireService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderExpirationScheduler {

    private static final String ZSET_KEY = "orderExpiryZSet";

    private final StringRedisTemplate redis;
    private final OrderExpireService orderExpireService;

    @Scheduled(fixedDelay = 60_000)
    public void expireOldOrders() {
        long now = Instant.now().toEpochMilli();
        Set<String> expired = redis.opsForZSet().rangeByScore(ZSET_KEY, 0, now);
        if (expired == null || expired.isEmpty()) return;

        log.info("Expired orders found: {}", expired);

        for (String idStr : expired) {
            UUID bookingId;
            try {
                bookingId = UUID.fromString(idStr);
            } catch (IllegalArgumentException e) {
                log.error("Invalid booking ID format: {}", idStr, e);
                redis.opsForZSet().remove(ZSET_KEY, idStr);
                continue;
            }

            orderExpireService.expireByBookingId(bookingId);
            // Se remueve después de expirar: si el proceso muere a mitad, el
            // próximo tick lo reintenta (expireByBookingId es idempotente).
            redis.opsForZSet().remove(ZSET_KEY, idStr);
        }
    }
}
