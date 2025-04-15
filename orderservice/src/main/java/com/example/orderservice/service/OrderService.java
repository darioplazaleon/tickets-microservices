package com.example.orderservice.service;

import com.example.bookingservice.event.BookingEvent;
import com.example.orderservice.client.EventServiceClient;
import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderStatus;
import com.example.orderservice.entity.OrderTicket;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.request.PaymentSuccessRequest;
import com.example.orderservice.response.OrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @KafkaListener(topics = "tickets.booking.created", groupId = "order-service")
    public void orderEvent(BookingEvent bookingEvent) {
        try {
            log.info("Booking event received: {}", bookingEvent.bookingId());
            createOrder(bookingEvent);
            log.info("Order created by booking: {}", bookingEvent.bookingId());
        } catch (Exception e) {
            log.error("Error creating order for booking event: {}", bookingEvent.bookingId(), e);
            throw new RuntimeException("Failed to create order", e);
        }
    }

    public OrderResponse getOrderById(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return new OrderResponse(order);
    }

    public void updateSuccessOrder(UUID orderId, PaymentSuccessRequest paymentInfo) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(paymentInfo.status());
        order.setPaymentIntentId(paymentInfo.paymentIntentId());
        order.setPaidAt(Instant.now());

        orderRepository.save(order);
        log.info("Order status updated: {} to {}", orderId, paymentInfo.status());
    }


    private void createOrder(BookingEvent bookingEvent) {

        int totalQuantity = bookingEvent.tickets().stream()
                .mapToInt(BookingEvent.TicketInfo::quantity)
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
}
