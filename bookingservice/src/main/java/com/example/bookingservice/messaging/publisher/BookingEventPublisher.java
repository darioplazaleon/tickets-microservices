package com.example.bookingservice.messaging.publisher;

import com.example.bookingservice.entity.Booking;
import com.example.shared.events.BookingCreatedEvent;
import com.example.shared.infra.outbox.OutboxEventWriter;
import com.example.shared.messaging.Topics;
import com.example.shared.records.TicketInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingEventPublisher {

    private final OutboxEventWriter outboxEventWriter;

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

        outboxEventWriter.append(Topics.BOOKING_CREATED, booking.getId().toString(), event,
                correlationId != null ? correlationId.toString() : null);
        log.info("BookingCreatedEvent appended to outbox: {}", event.bookingId());
    }
}
