package com.example.eventservice.response;

import com.example.eventservice.entity.Event;
import com.example.eventservice.entity.TicketType;
import com.example.eventservice.entity.Venue;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record EventRecord(
        UUID id, String event, int capacity, Venue venue, List<TicketType> ticketTypes) {
  public EventRecord(Event event) {
    this(
        event.getId(),
        event.getName(),
        event.getTotalCapacity(),
        event.getVenue(),
        event.getTicketTypes());
  }
}
