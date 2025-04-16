package com.example.bookingservice.service;

import com.example.bookingservice.client.InventoryServiceClient;
import com.example.bookingservice.entity.Booking;
import com.example.bookingservice.entity.BookingStatus;
import com.example.bookingservice.entity.BookingTicket;
import com.example.bookingservice.entity.Customer;
import com.example.bookingservice.messaging.publisher.BookingEventPublisher;
import com.example.bookingservice.repository.BookingRepository;
import com.example.bookingservice.repository.CustomerRepository;
import com.example.bookingservice.request.BookingRequest;
import com.example.bookingservice.request.TicketRequest;
import com.example.bookingservice.response.BookingResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

  private final CustomerRepository customerRepository;
  private final InventoryServiceClient inventoryServiceClient;
  private final BookingRepository bookingRepository;
  private final BookingEventPublisher bookingEventPublisher;

  public BookingResponse createBooking(UUID userId, UUID correlationId, BookingRequest request) {

    if (request.tickets().isEmpty()) {
      throw new RuntimeException("No tickets found");
    }

    List<BookingTicket> bookingTickets = new ArrayList<>();
    BigDecimal totalPrice = BigDecimal.ZERO;

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
    }

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
}
