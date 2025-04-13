package com.example.bookingservice.controller;

import com.example.bookingservice.request.BookingRequest;
import com.example.bookingservice.response.BookingResponse;
import com.example.bookingservice.service.BookingService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

  private final BookingService bookingService;

  @PostMapping("/create")
  public ResponseEntity<BookingResponse> createBooking(
      @RequestHeader("X-User-Id") String userId,
      @RequestHeader("X-Correlation-Id") UUID correlationId,
      @RequestBody BookingRequest request) {
    log.info(
        "Booking request: {}, from user {} with correlationId {}", request, userId, correlationId);
    System.out.println(userId);
    var newBooking = bookingService.createBooking(userId, correlationId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(newBooking);
  }
}
