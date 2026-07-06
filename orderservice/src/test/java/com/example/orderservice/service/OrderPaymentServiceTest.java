package com.example.orderservice.service;

import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderStatus;
import com.example.orderservice.repository.OrderRepository;
import com.example.shared.events.PaymentSucceededEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderPaymentServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderPaymentService orderPaymentService;

    private final UUID orderId = UUID.randomUUID();
    private final Instant paidAt = Instant.now();

    private PaymentSucceededEvent paymentEvent() {
        return new PaymentSucceededEvent(
                orderId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                List.of(), BigDecimal.TEN, UUID.randomUUID(), paidAt);
    }

    private Order order(OrderStatus status) {
        return Order.builder()
                .id(orderId)
                .bookingId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .eventId(UUID.randomUUID())
                .totalPrice(BigDecimal.TEN)
                .totalQuantity(1)
                .correlationId(UUID.randomUUID())
                .expiresAt(Instant.now().plusSeconds(900))
                .status(status)
                .build();
    }

    @Test
    void marksPendingOrderAsPaid() {
        Order order = order(OrderStatus.PENDING);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        orderPaymentService.processPaymentSucceeded(paymentEvent());

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.getPaidAt()).isEqualTo(paidAt);
        verify(orderRepository).save(order);
    }

    @Test
    void skipsOrdersAlreadyPaid() {
        Order order = order(OrderStatus.PAID);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        orderPaymentService.processPaymentSucceeded(paymentEvent());

        verify(orderRepository, never()).save(any());
    }
}
