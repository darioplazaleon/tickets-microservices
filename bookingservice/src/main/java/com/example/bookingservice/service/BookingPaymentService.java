package com.example.bookingservice.service;

import com.example.bookingservice.entity.BookingStatus;
import com.example.bookingservice.repository.BookingRepository;
import com.example.shared.events.PaymentSucceededEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingPaymentService {

    private final BookingRepository bookingRepository;

    public void processPaymentSuccess(PaymentSucceededEvent event) {

    // Update the booking status to PAID
    bookingRepository
        .findById(event.bookingId())
        .ifPresentOrElse(
            booking -> {
              booking.setStatus(BookingStatus.CONFIRMED);
              bookingRepository.save(booking);
              log.info(
                  "[BookingService] Booking status updated to CONFIRMED for bookingId: {}", event.bookingId());
            }, () -> {
              log.warn(
                  "[BookingService] Booking not found for bookingId: {}. Ignoring event.", event.bookingId());
            });
    }
}
