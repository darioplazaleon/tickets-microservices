package com.example.orderservice.messaging.listener;

import com.example.orderservice.service.OrderService;
import com.example.shared.events.BookingCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final OrderService orderService;

    @KafkaListener(topics = "tickets.booking.created", groupId = "order-service")
    public void handleBookingCreatedEvent(BookingCreatedEvent event) {
        log.info("[OrderService] BookingCreatedEvent received for bookingId: {} (correlationId={})", event.bookingId(), event.correlationId());
        orderService.createOrder(event);
        log.info("[OrderService] Order created for bookingId: {} (correlationId={})", event.bookingId(), event.correlationId());
    }

}
