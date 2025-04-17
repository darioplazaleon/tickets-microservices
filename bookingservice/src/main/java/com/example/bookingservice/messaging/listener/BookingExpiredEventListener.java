package com.example.bookingservice.messaging.listener;

import com.example.bookingservice.entity.BookingStatus;
import com.example.bookingservice.repository.BookingRepository;
import com.example.shared.events.OrderExpiredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingExpiredEventListener {

    private final BookingRepository bookingRepository;

    @KafkaListener(topics = "tickets.order.expired", groupId = "booking-service")
    public void handleOrderExpired(OrderExpiredEvent event) {
        log.info("[BookingService] OrderExpiredEvent received for bookingId: {} (correlationId={})", event.bookingId(), event.correlationId());

        bookingRepository.findById(event.bookingId()).ifPresent(booking -> {
            if (booking.getStatus() == BookingStatus.PENDING) {
                booking.setStatus(BookingStatus.EXPIRED);
                bookingRepository.save(booking);
                log.info("[BookingService] Booking status updated to EXPIRED for bookingId: {}", event.bookingId());
            } else {
                log.info("[BookingService] Booking status is not PENDING for bookingId: {}. Current status: {}", event.bookingId(), booking.getStatus());
            }
        });
    }
}
