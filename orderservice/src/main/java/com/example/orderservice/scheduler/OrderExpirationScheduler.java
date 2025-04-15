package com.example.orderservice.scheduler;

import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderStatus;
import com.example.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderExpirationScheduler {

    private final OrderRepository orderRepository;

    @Scheduled(fixedDelay = 60__000)
    public void expireOldOrders() {
        List<Order> expiredOrders = orderRepository.findExpiredUnpaidOrders(Instant.now());

        if (!expiredOrders.isEmpty()) {
            log.info("Expired orders found: {}", expiredOrders);
        }

        expiredOrders.forEach(order -> {
            order.setStatus(OrderStatus.EXPIRED);
            orderRepository.save(order);
            log.info("Order marked as expired: {}", order.getId());
        });
    }
}
