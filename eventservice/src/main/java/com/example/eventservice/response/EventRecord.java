package com.example.eventservice.response;

import com.example.eventservice.entity.Event;
import com.example.eventservice.entity.Venue;
import java.math.BigDecimal;

public record EventRecord(
    Long eventId, String event, int capacity, Venue venue, BigDecimal ticketPrice) {
  public EventRecord(Event event) {
    this(
        event.getId(),
        event.getName(),
        event.getLeftCapacity(),
        event.getVenue(),
        event.getTicketPrice());
  }
}
