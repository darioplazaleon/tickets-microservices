package com.example.orderservice.service;

import com.example.orderservice.entity.OrderStatus;
import com.example.orderservice.repository.OrderRepository;
import com.example.shared.events.PaymentSucceededEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderPaymentService {

    private final OrderRepository orderRepository;

    public void processPaymentSucceeded(PaymentSucceededEvent event) {
        orderRepository.findById(event.orderId()).ifPresentOrElse(order -> {
            order.setStatus(OrderStatus.PAID);
            order.setPaidAt(event.paidAt());
            orderRepository.save(order);
            log.info("[OrderService] Order {} updated successfully after payment succeeded.", order.getId());
        }, () -> {
            log.warn("[OrderService] Order not found for orderId: {}. Ignoring event.", event.orderId());
        });
    }
}
