package com.example.bookingservice.messaging.publisher;

import com.example.bookingservice.entity.Booking;
import com.example.shared.events.BookingCreatedEvent;
import com.example.shared.records.TicketInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingEventPublisher {

    private final KafkaTemplate<String, BookingCreatedEvent> kafkaTemplate;

    public void sendBookingCreatedEvent(Booking booking, UUID correlationId, UUID userId) {
        List<TicketInfo> tickets = booking.getTickets().stream()
                .map(t -> new TicketInfo(t.getTicketType(), t.getQuantity(), t.getUnitPrice()))
                .toList();

        BookingCreatedEvent event = new BookingCreatedEvent(
                booking.getId(),
                userId,
                booking.getEventId(),
                tickets,
                correlationId,
                booking.getTotalPrice(),
                booking.getCreatedAt()
        );

        ProducerRecord<String, BookingCreatedEvent> record = new ProducerRecord<>("tickets.booking.created", event);
        record.headers().add("user-id", userId.toString().getBytes(StandardCharsets.UTF_8));
        record.headers().add("correlation-id", correlationId.toString().getBytes(StandardCharsets.UTF_8));

        kafkaTemplate.send(record);
        log.info("BookingCreatedEvent sent to Kafka topic: {}", event.bookingId());
    }
}
