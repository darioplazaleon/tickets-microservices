package com.example.orderservice.service;


import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderStatus;
import com.example.orderservice.entity.OrderTicket;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.response.OrderDTO;
import com.example.orderservice.response.OrderResponse;
import com.example.orderservice.response.OrderSimple;
import com.example.orderservice.response.OrderSummary;
import com.example.shared.events.BookingCreatedEvent;
import com.example.shared.records.TicketInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderResponse getOrderById(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        return createOrderResponse(order);
    }

    @Cacheable(value = "orderSummary", key = "#orderId")
    public OrderSummary getSummary(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        List<OrderSummary.TicketSummary> tickets = order.getTicketItems().stream()
                .collect(Collectors.groupingBy(OrderTicket::getTicketType, Collectors.summingInt(OrderTicket::getQuantity)))
                .entrySet().stream()
                .map(e -> new OrderSummary.TicketSummary(e.getKey(), e.getValue()))
                .toList();

        return new OrderSummary(order.getTotalPrice(), tickets);
    }

    public Page<OrderSimple> getOrdersByCustomerId(UUID customerId, Pageable pageable) {
        return orderRepository.findAllByCustomerId(customerId, pageable);
    }

    public OrderDTO getCustomerOrderById(UUID orderId, UUID customerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getCustomerId().equals(customerId)) {
            throw new RuntimeException("This order does not belong to the customer");
        }

        List<OrderDTO.TicketItem> items = order.getTicketItems().stream()
                .map(t -> new OrderDTO.TicketItem(
                        t.getTicketType(),
                        t.getQuantity(),
                        t.getUnitPrice()
                ))
                .toList();

        return new OrderDTO(
                order.getId(),
                order.getCustomerId(),
                order.getEventId(),
                order.getBookingId(),
                order.getStatus(),
                order.getTotalPrice(),
                items
        );
    }

    private OrderResponse createOrderResponse(Order order) {
        List<OrderResponse.TicketItem> items = order.getTicketItems().stream()
                .map(t -> new OrderResponse.TicketItem(
                        t.getTicketType(),
                        t.getQuantity(),
                        t.getUnitPrice()
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getBookingId(),
                order.getCustomerId(),
                order.getEventId(),
                order.getTotalPrice(),
                order.getStatus().name(),
                order.getExpiresAt(),
                order.getCorrelationId(),
                items
        );
    }

    public void createOrder(BookingCreatedEvent bookingEvent) {

        int totalQuantity = bookingEvent.tickets().stream()
                .mapToInt(TicketInfo::quantity)
                .sum();

        Order order = Order.builder()
                .bookingId(bookingEvent.bookingId())
                .customerId(bookingEvent.userId())
                .eventId(bookingEvent.eventId())
                .totalPrice(bookingEvent.totalPrice())
                .totalQuantity(totalQuantity)
                .correlationId(bookingEvent.correlationId())
                .expiresAt(Instant.now().plusSeconds(900))
                .status(OrderStatus.PENDING)
                .build();

        List<OrderTicket> ticketItems = bookingEvent.tickets().stream()
                .map(t -> OrderTicket.builder()
                        .ticketType(t.ticketType())
                        .quantity(t.quantity())
                        .unitPrice(t.unitPrice())
                        .order(order)
                        .build())
                .collect(Collectors.toList());

        order.setTicketItems(ticketItems);

        orderRepository.save(order);
    }

    public void markExpired(UUID orderId) {
        orderRepository.findById(orderId).ifPresent(order -> {
            if (order.getStatus() == OrderStatus.PENDING) {
                order.setStatus(OrderStatus.EXPIRED);
                orderRepository.save(order);
            }
        });
    }
}
