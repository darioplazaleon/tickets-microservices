package com.example.bookingservice.controller;

import com.example.bookingservice.request.BookingRequest;
import com.example.bookingservice.response.BookingResponse;
import com.example.bookingservice.response.BookingSimple;
import com.example.bookingservice.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/create")
    public ResponseEntity<BookingResponse> createBooking(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-Correlation-Id") UUID correlationId,
            @RequestBody
            BookingRequest request) {
        log.info(
                "[Booking Service] Booking request: {}, from user {} with correlationId {}",
                request,
                userId,
                correlationId);
        var newBooking = bookingService.createBooking(userId, correlationId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(newBooking);
    }

    @GetMapping("/my-bookings")
    public ResponseEntity<Page<BookingSimple>> getMyBookings(
            @RequestHeader("X-User-Id")
            UUID userId,
            Pageable pageable) {
        log.info("[Booking Service] Get my bookings request from user {}", userId);
        var bookings = bookingService.getCustomerBookings(userId, pageable);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/my-bookings/{bookingId}")
    public ResponseEntity<BookingResponse> getMyBookingById(
            @RequestHeader("X-User-Id") UUID userId, @PathVariable UUID bookingId) {
        log.info("[Booking Service] Get my booking by ID request from user {}", userId);
        var booking = bookingService.getCustomerBookingById(userId, bookingId);
        return ResponseEntity.ok(booking);
    }
}
