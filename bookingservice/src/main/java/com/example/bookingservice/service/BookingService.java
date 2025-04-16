package com.example.bookingservice.service;

import com.example.bookingservice.client.InventoryServiceClient;
import com.example.bookingservice.entity.Booking;
import com.example.bookingservice.entity.BookingStatus;
import com.example.bookingservice.entity.BookingTicket;
import com.example.bookingservice.entity.Customer;
import com.example.bookingservice.event.BookingEvent;
import com.example.bookingservice.event.incoming.OrderExpiredEvent;
import com.example.bookingservice.repository.BookingRepository;
import com.example.bookingservice.repository.CustomerRepository;
import com.example.bookingservice.request.BookingRequest;
import com.example.bookingservice.request.TicketRequest;
import com.example.bookingservice.response.BookingResponse;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.annotation.KafkaListener;
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

    Customer costumer =
        customerRepository
            .findById(userId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));

    Booking booking =
        Booking.builder()
            .eventId(request.eventId())
            .eventId(request.eventId())
            .customerId(costumer.getId())
            .totalPrice(totalPrice)
            .tickets(bookingTickets)
            .status(BookingStatus.PENDING)
            .build();

    bookingTickets.forEach(t -> t.setBooking(booking));

    bookingRepository.save(booking);

    BookingEvent bookingEvent = createBookingEvent(booking, correlationId);
    ProducerRecord<String, BookingEvent> record = new ProducerRecord<>(
            "tickets.booking.created", bookingEvent);

    record.headers().add("user-id", userId.toString().getBytes(StandardCharsets.UTF_8));
    record.headers().add("correlation-id", correlationId.toString().getBytes(StandardCharsets.UTF_8));

    kafkaTemplate.send(record);
    log.info("Booking event sent to kafka topic: {}", bookingEvent);

    return BookingResponse.builder()
        .bookingId(booking.getId())
        .customerId(booking.getCustomerId())
        .eventId(booking.getEventId())
        .totalPrice(totalPrice)
        .build();
  }

  @KafkaListener(topics = "tickets.order.expired", groupId = "booking-service")
  public void handleOrderExpired(OrderExpiredEvent event, ConsumerRecord<String, OrderExpiredEvent> record) {
    log.info("[BookingService] OrderExpiredEvent received for bookingId: {} (correlationId={})", event.bookingId(), event.correlationId());

    Optional<Booking> bookingOptional = bookingRepository.findById(event.bookingId());

    if (bookingOptional.isEmpty()) {
      log.warn("Booking with ID {} does not exist. Ignoring event.", event.bookingId());
      return;
    }

    Booking booking = bookingOptional.get();

    if (booking.getStatus() != BookingStatus.PENDING) {
      log.info("Booking {} is not in PENDING status. Ignoring event.", booking.getId());
      return;
    }

    booking.setStatus(BookingStatus.EXPIRED);
    bookingRepository.save(booking);

    log.info("Booking {} status updated to EXPIRED", booking.getId());
  }

  private BookingEvent createBookingEvent(Booking booking, UUID correlationId) {

    List<BookingEvent.TicketInfo> tickets =
        booking.getTickets().stream()
            .map(
                t ->
                    new BookingEvent.TicketInfo(
                        t.getTicketType(), t.getQuantity(), t.getUnitPrice()))
            .toList();

    return BookingEvent.builder()
        .bookingId(booking.getId())
        .userId(booking.getCustomerId())
        .eventId(booking.getEventId())
        .tickets(tickets)
        .totalPrice(booking.getTotalPrice())
        .correlationId(correlationId)
        .createdAt(booking.getCreatedAt())
        .build();
  }
}
