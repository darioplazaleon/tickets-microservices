package com.example.bookingservice.service;

import com.example.bookingservice.client.InventoryServiceClient;
import com.example.bookingservice.entity.Booking;
import com.example.bookingservice.entity.BookingStatus;
import com.example.bookingservice.entity.Customer;
import com.example.bookingservice.event.BookingEvent;
import com.example.bookingservice.repository.BookingRepository;
import com.example.bookingservice.repository.CustomerRepository;
import com.example.bookingservice.request.BookingRequest;
import com.example.bookingservice.response.BookingResponse;
import com.example.bookingservice.response.EventResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

  private final CustomerRepository customerRepository;
  private final InventoryServiceClient inventoryServiceClient;
  private final BookingRepository bookingRepository;
  private final KafkaTemplate<String, BookingEvent> kafkaTemplate;

  public BookingResponse createBooking(
      String userId, UUID correlationId, BookingRequest request) {
    Customer costumer =
        customerRepository
            .findById(request.userId())
            .orElseThrow(() -> new RuntimeException("Customer not found"));

    EventResponse eventResponse = inventoryServiceClient.getEvent(request.eventId());

    if (eventResponse.capacity() < request.ticketCount()) {
      throw new RuntimeException("Not enough capacity");
    }

    Booking booking =
        Booking.builder()
            .eventId(request.eventId())
            .customerId(request.userId())
            .status(BookingStatus.PENDING)
            .quantity(request.ticketCount())
            .build();

    bookingRepository.save(booking);

    BookingEvent bookingEvent =
        createBookingEvent(
            request, costumer, eventResponse, userId, correlationId, booking.getId());

    kafkaTemplate.send("tickets.booking.created", bookingEvent);
    log.info("Booking event sent to kafka topic: {}", bookingEvent);

    return BookingResponse.builder()
        .bookingId(booking.getId())
        .customerId(booking.getCustomerId())
        .eventId(booking.getEventId())
        .ticketCount(booking.getQuantity())
        .totalPrice(eventResponse.ticketPrice().multiply(BigDecimal.valueOf(request.ticketCount())))
        .build();
  }

  private BookingEvent createBookingEvent(
      BookingRequest request,
      Customer customer,
      EventResponse eventResponse,
      String userId,
      UUID correlationId,
      UUID bookingId) {
    return BookingEvent.builder()
        .bookingId(bookingId)
        .eventId(request.eventId())
        .customerId(customer.getId())
        .ticketCount(request.ticketCount())
        .totalPrice(eventResponse.ticketPrice().multiply(BigDecimal.valueOf(request.ticketCount())))
        .correlationId(correlationId)
        .userId(userId)
        .createdAt(Instant.now())
        .build();
  }
}
