package com.example.orderservice.service;

import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderStatus;
import com.example.orderservice.repository.OrderRepository;
import com.example.shared.events.BookingCreatedEvent;
import com.example.shared.records.TicketInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private final UUID bookingId = UUID.randomUUID();

    private BookingCreatedEvent bookingEvent() {
        return new BookingCreatedEvent(
                bookingId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                List.of(
                        new TicketInfo("VIP", 2, BigDecimal.TEN),
                        new TicketInfo("GENERAL", 3, BigDecimal.ONE)),
                UUID.randomUUID(),
                BigDecimal.valueOf(23),
                Instant.now());
    }

    @Test
    void createOrderPersistsPendingOrderWithAllTicketItems() {
        when(orderRepository.existsByBookingId(bookingId)).thenReturn(false);

        orderService.createOrder(bookingEvent());

        ArgumentCaptor<Order> saved = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(saved.capture());
        Order order = saved.getValue();
        assertThat(order.getBookingId()).isEqualTo(bookingId);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getTotalQuantity()).isEqualTo(5);
        assertThat(order.getTicketItems()).hasSize(2);
        assertThat(order.getExpiresAt()).isAfter(Instant.now());
    }

    @Test
    void createOrderSkipsDuplicateBookingCreatedEvents() {
        when(orderRepository.existsByBookingId(bookingId)).thenReturn(true);

        orderService.createOrder(bookingEvent());

        verify(orderRepository, never()).save(any());
    }
}
