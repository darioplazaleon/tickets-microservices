package com.example.eventservice.service;

import com.example.eventservice.entity.Event;
import com.example.eventservice.entity.TicketType;
import com.example.eventservice.repository.TicketTypeRepository;
import com.example.eventservice.request.ReserveTicketRequest;
import com.example.eventservice.request.TicketTypeRequest;
import com.example.eventservice.response.ReserveTicketResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TicketTypeService {

  private final TicketTypeRepository ticketTypeRepository;

  public TicketType saveTicketType(TicketTypeRequest ticketTypeRequest, Event event) {

    return TicketType.builder()
        .name(ticketTypeRequest.name())
        .capacity(ticketTypeRequest.capacity())
        .price(ticketTypeRequest.price())
        .event(event)
        .build();
  }

  @Transactional
  public ReserveTicketResponse reserveTickets(UUID eventId, ReserveTicketRequest request) {
    if (request.quantity() <= 0) {
      throw new IllegalArgumentException("Quantity must be greater than 0");
    }

    TicketType type =
        ticketTypeRepository
            .findByEventIdAndNameIgnoreCase(eventId, request.ticketType())
            .orElseThrow(() -> new EntityNotFoundException("Ticket type not found"));

    int available = type.getCapacity() - type.getReserved() - type.getSold();

    if (available < request.quantity()) {
      throw new IllegalArgumentException("Not enough tickets available");
    }

    type.setReserved(type.getReserved() + request.quantity());
    ticketTypeRepository.save(type);

    return ReserveTicketResponse.builder()
        .ticketType(type.getName())
        .unitPrice(type.getPrice())
        .reservedQuantity(request.quantity())
        .reservedUntil(LocalDateTime.now().plusMinutes(15))
        .build();
  }
}
