package com.example.bookingservice.service;

import com.example.bookingservice.client.InventoryServiceClient;
import com.example.bookingservice.entity.Customer;
import com.example.bookingservice.event.BookingEvent;
import com.example.bookingservice.repository.CustomerRepository;
import com.example.bookingservice.request.BookingRequest;
import com.example.bookingservice.response.BookingResponse;
import com.example.bookingservice.response.EventResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

  private final CustomerRepository customerRepository;
  private final InventoryServiceClient inventoryServiceClient;
  private final KafkaTemplate<String, BookingEvent> kafkaTemplate;

  public BookingResponse createBooking(BookingRequest request) {
    Customer costumer =
        customerRepository
            .findById(request.getUserId())
            .orElseThrow(() -> new RuntimeException("Customer not found"));

    EventResponse eventResponse = inventoryServiceClient.getEvent(request.getEventId());
    System.out.println("Event service response" + eventResponse);

    if (eventResponse.getCapacity() < request.getTicketCount()) {
      throw new RuntimeException("Not enough capacity");
    }

    BookingEvent bookingEvent = createBookingEvent(request, costumer, eventResponse);

    kafkaTemplate.send("booking", bookingEvent);
    log.info("Booking event sent to kafka topic: {}", bookingEvent);

    return BookingResponse.builder()
            .userId(request.getUserId())
            .eventId(request.getEventId())
            .totalPrice(eventResponse.getTicketPrice())
            .ticketCount(request.getTicketCount())
            .build();
  }

  private BookingEvent createBookingEvent (BookingRequest request, Customer customer, EventResponse eventResponse) {
    return BookingEvent.builder()
        .eventId(request.getEventId())
        .userId(customer.getId())
        .ticketCount(request.getTicketCount())
        .totalPrice(eventResponse.getTicketPrice().multiply(BigDecimal.valueOf(request.getTicketCount())))
        .build();
  }
}
