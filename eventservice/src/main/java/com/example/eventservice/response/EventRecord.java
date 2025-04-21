package com.example.eventservice.response;

import com.example.eventservice.entity.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record EventRecord(
        UUID id, String event, LocalDateTime startDate, int capacity, String location, List<TicketTypeResponse> ticketTypes) {
  public EventRecord(Event event) {
    this(
        event.getId(),
        event.getName(),
        event.getStartDate(),
        event.getTotalCapacity(),
        event.getVenue().getName(),
        event.getTicketTypes().stream()
            .map(
                ticketTypes ->
                    new TicketTypeResponse(
                        ticketTypes.getName(), ticketTypes.getCapacity(), ticketTypes.getPrice()))
            .toList());
  }
}
