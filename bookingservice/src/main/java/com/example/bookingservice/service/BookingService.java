package com.example.bookingservice.service;

import com.example.bookingservice.client.EventServiceClient;
import com.example.bookingservice.entity.*;
import com.example.bookingservice.messaging.publisher.BookingEventPublisher;
import com.example.bookingservice.repository.BookingRepository;
import com.example.bookingservice.repository.CustomerRepository;
import com.example.bookingservice.request.BookingRequest;
import com.example.bookingservice.request.TicketRequest;
import com.example.bookingservice.response.BookingResponse;
import com.example.bookingservice.response.BookingSimple;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

  private final CustomerRepository customerRepository;
  private final EventServiceClient inventoryServiceClient;
  private final BookingRepository bookingRepository;
  private final BookingEventPublisher bookingEventPublisher;
  private final TicketHoldService ticketService;

  public BookingResponse createBooking(UUID userId, UUID correlationId, BookingRequest request) {

    if (request.tickets().isEmpty()) {
      throw new RuntimeException("No tickets found");
    }

    String holdId = UUID.randomUUID().toString();

    List<BookingTicket> bookingTickets = new ArrayList<>();
    BigDecimal totalPrice = BigDecimal.ZERO;
    Map<String, Integer> ticketCounts = new HashMap<>();

    for (TicketRequest ticket : request.tickets()) {
      if (ticket.quantity() <= 0) {
        throw new RuntimeException(
            "Invalid ticket quantity for ticket type: " + ticket.ticketType());
      }

      BigDecimal unitPrice =
          inventoryServiceClient.reserveTicket(
              request.eventId(), ticket.ticketType(), ticket.quantity());

      totalPrice = totalPrice.add(unitPrice.multiply(BigDecimal.valueOf(ticket.quantity())));

      BookingTicket bookingTicket =
          BookingTicket.builder()
              .ticketType(ticket.ticketType())
              .quantity(ticket.quantity())
              .unitPrice(unitPrice)
              .build();

      bookingTickets.add(bookingTicket);

      ticketCounts.put(ticket.ticketType(), ticket.quantity());
    }

    TicketHold hold = new TicketHold(
        request.eventId(), userId, ticketCounts, Instant.now());
    ticketService.holdTickets(holdId, hold);

    Customer customer =
        customerRepository
            .findById(userId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));

    Booking booking =
        Booking.builder()
            .eventId(request.eventId())
            .eventId(request.eventId())
            .customerId(customer.getId())
            .totalPrice(totalPrice)
            .tickets(bookingTickets)
            .status(BookingStatus.PENDING)
            .build();

    bookingTickets.forEach(t -> t.setBooking(booking));

    bookingRepository.save(booking);

    bookingEventPublisher.sendBookingCreatedEvent(booking, correlationId, customer.getId());

    return BookingResponse.builder()
        .bookingId(booking.getId())
        .customerId(booking.getCustomerId())
        .eventId(booking.getEventId())
        .totalPrice(totalPrice)
        .build();
  }

  public Page<BookingSimple> getCustomerBookings(UUID customerId, Pageable pageable) {
    Page<Booking> bookings = bookingRepository.findByCustomerId(customerId, pageable);
    return bookings.map(
        booking ->
            BookingSimple.builder()
                .id(booking.getId())
                .eventId(booking.getEventId())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .build());
  }

  public BookingResponse getCustomerBookingById(UUID customerId, UUID bookingId) {
    Booking booking = bookingRepository.findById(bookingId).orElseThrow();
    if (!booking.getCustomerId().equals(customerId)) {
      throw new RuntimeException("Booking not found");
    }
    return BookingResponse.builder()
        .bookingId(booking.getId())
        .customerId(booking.getCustomerId())
        .eventId(booking.getEventId())
        .totalPrice(booking.getTotalPrice())
        .tickets(booking.getTickets().stream()
            .map(ticket -> TicketRequest.builder()
                .ticketType(ticket.getTicketType())
                .quantity(ticket.getQuantity())
                .build())
            .toList())
        .build();
  }
}
