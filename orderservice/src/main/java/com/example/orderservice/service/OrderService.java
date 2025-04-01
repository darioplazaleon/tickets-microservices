package com.example.orderservice.service;

import com.example.bookingservice.event.BookingEvent;
import com.example.orderservice.client.EventServiceClient;
import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderStatus;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.response.OrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final EventServiceClient eventServiceClient;

    @KafkaListener(topics = "booking", groupId = "order-service")
    public void orderEvent(BookingEvent bookingEvent) {
        log.info("Received booking event: {}", bookingEvent);

        Order order = createOrder(bookingEvent);
        orderRepository.saveAndFlush(order);

        eventServiceClient.updateEventCapacity(bookingEvent.getEventId(), bookingEvent.getTicketCount());
        log.info("Event capacity updated for event: {}", bookingEvent.getEventId());
    }

    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return new OrderResponse(order);
    }

    public void updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(status);
        orderRepository.save(order);
        log.info("Order status updated: {} to {}", orderId, status);
    }

    private Order createOrder(BookingEvent bookingEvent) {
        return Order.builder()
                .customerId(bookingEvent.getUserId())
                .eventId(bookingEvent.getEventId())
                .ticketCount(bookingEvent.getTicketCount())
                .totalPrice(bookingEvent.getTotalPrice())
                .build();
    }
}
