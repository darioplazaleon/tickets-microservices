package com.example.orderservice.service;

import com.example.bookingservice.event.BookingEvent;
import com.example.orderservice.client.EventServiceClient;
import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderStatus;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.request.PaymentSuccessRequest;
import com.example.orderservice.response.OrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final EventServiceClient eventServiceClient;

    @KafkaListener(topics = "tickets.booking.created", groupId = "order-service")
    public void orderEvent(BookingEvent bookingEvent) {
        log.info("Received booking event: {}", bookingEvent);

        Order order = createOrder(bookingEvent);
        orderRepository.saveAndFlush(order);

        eventServiceClient.updateEventCapacity(bookingEvent.eventId(), bookingEvent.ticketCount());
        log.info("Event capacity updated for event: {}", bookingEvent.eventId());
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
        order.setPaidAt(LocalDateTime.now());

        orderRepository.save(order);
        log.info("Order status updated: {} to {}", orderId, paymentInfo.status());
    }


    private Order createOrder(BookingEvent bookingEvent) {
        return Order.builder()
                .customerId(bookingEvent.customerId())
                .eventId(bookingEvent.eventId())
                .ticketCount(bookingEvent.ticketCount())
                .totalPrice(bookingEvent.totalPrice())
                .correlationId(bookingEvent.correlationId())
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();
    }
}
