package com.example.orderservice.service;

import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderStatus;
import com.example.orderservice.messaging.publisher.OrderEventPublisher;
import com.example.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderExpireServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderEventPublisher orderEventPublisher;

    @InjectMocks
    private OrderExpireService orderExpireService;

    private final UUID bookingId = UUID.randomUUID();

    private Order order(OrderStatus status) {
        return Order.builder()
                .id(UUID.randomUUID())
                .bookingId(bookingId)
                .customerId(UUID.randomUUID())
                .eventId(UUID.randomUUID())
                .totalPrice(BigDecimal.TEN)
                .totalQuantity(1)
                .correlationId(UUID.randomUUID())
                .expiresAt(Instant.now().minusSeconds(60))
                .status(status)
                .build();
    }

    @Test
    void expiresPendingOrderAndPublishesEvent() {
        Order order = order(OrderStatus.PENDING);
        when(orderRepository.findByBookingId(bookingId)).thenReturn(Optional.of(order));

        orderExpireService.expireByBookingId(bookingId);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.EXPIRED);
        verify(orderRepository).save(order);
        verify(orderEventPublisher).sendOrderExpiredEvent(order, order.getCustomerId(), order.getCorrelationId());
    }

    @Test
    void skipsOrdersThatAreNotPending() {
        Order order = order(OrderStatus.PAID);
        when(orderRepository.findByBookingId(bookingId)).thenReturn(Optional.of(order));

        orderExpireService.expireByBookingId(bookingId);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        verify(orderRepository, never()).save(any());
        verifyNoInteractions(orderEventPublisher);
    }

    @Test
    void doesNothingWhenNoOrderExistsForBooking() {
        when(orderRepository.findByBookingId(bookingId)).thenReturn(Optional.empty());

        orderExpireService.expireByBookingId(bookingId);

        verify(orderRepository, never()).save(any());
        verifyNoInteractions(orderEventPublisher);
    }
}
