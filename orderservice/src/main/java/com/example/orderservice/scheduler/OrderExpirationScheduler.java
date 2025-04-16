package com.example.orderservice.scheduler;

import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderStatus;
import com.example.orderservice.messaging.publisher.OrderEventPublisher;
import com.example.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderExpirationScheduler {

    private final OrderRepository orderRepository;
    private final OrderEventPublisher orderEventPublisher;

    @Scheduled(fixedDelay = 60__000)
    public void expireOldOrders() {
        Instant now = Instant.now();
        List<Order> expiredOrders = orderRepository.findExpiredUnpaidOrders(now);

        if (!expiredOrders.isEmpty()) {
            log.info("Expired orders found: {}", expiredOrders);
        }

        for (Order order : expiredOrders) {
            order.setStatus(OrderStatus.EXPIRED);
            orderRepository.save(order);

            orderEventPublisher.sendOrderExpiredEvent(order, order.getCustomerId(), order.getCorrelationId());

            log.info("Order expired: {}", order.getId());
        }
    }
}
