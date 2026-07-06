package com.example.bookingservice.service;

import com.example.bookingservice.entity.Booking;
import com.example.bookingservice.entity.BookingStatus;
import com.example.bookingservice.repository.BookingRepository;
import com.example.shared.events.PaymentSucceededEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingPaymentServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingPaymentService bookingPaymentService;

    private final UUID bookingId = UUID.randomUUID();

    private PaymentSucceededEvent paymentEvent() {
        return new PaymentSucceededEvent(
                UUID.randomUUID(), bookingId, UUID.randomUUID(), UUID.randomUUID(),
                List.of(), BigDecimal.TEN, UUID.randomUUID(), Instant.now());
    }

    private Booking booking(BookingStatus status) {
        return Booking.builder()
                .id(bookingId)
                .customerId(UUID.randomUUID())
                .eventId(UUID.randomUUID())
                .status(status)
                .totalPrice(BigDecimal.TEN)
                .build();
    }

    @Test
    void confirmsPendingBookingOnPaymentSuccess() {
        Booking booking = booking(BookingStatus.PENDING);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        bookingPaymentService.processPaymentSuccess(paymentEvent());

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        verify(bookingRepository).save(booking);
    }

    @Test
    void skipsBookingsAlreadyConfirmed() {
        Booking booking = booking(BookingStatus.CONFIRMED);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        bookingPaymentService.processPaymentSuccess(paymentEvent());

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void ignoresEventsForUnknownBookings() {
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        bookingPaymentService.processPaymentSuccess(paymentEvent());

        verify(bookingRepository, never()).save(any());
    }
}
