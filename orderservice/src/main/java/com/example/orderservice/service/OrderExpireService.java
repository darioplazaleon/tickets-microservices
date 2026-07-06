package com.example.orderservice.service;

import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderStatus;
import com.example.orderservice.messaging.publisher.OrderEventPublisher;
import com.example.orderservice.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderExpireService {

    private final OrderRepository orderRepository;
    private final OrderEventPublisher orderEventPublisher;

    // El ZSet de Redis guarda bookingIds (ver OrderEventListener), por eso la
    // búsqueda es por bookingId y no por id de orden.
    // Transaccional: el cambio a EXPIRED y el evento en el outbox commitean juntos.
    @Transactional
    public void expireByBookingId(UUID bookingId) {
        orderRepository.findByBookingId(bookingId).ifPresentOrElse(order -> {
            if (order.getStatus() == OrderStatus.PENDING) {
                order.setStatus(OrderStatus.EXPIRED);
                orderRepository.save(order);

                orderEventPublisher.sendOrderExpiredEvent(
                        order, order.getCustomerId(), order.getCorrelationId());
                log.info("[OrderService] Order {} expired (bookingId={})", order.getId(), bookingId);
            } else {
                log.info("[OrderService] Order {} is not PENDING, skipping expiration", order.getId());
            }
        }, () -> log.warn("[OrderService] No order found for bookingId {}, skipping expiration", bookingId));
    }
}
