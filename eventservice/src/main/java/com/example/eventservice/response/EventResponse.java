package com.example.eventservice.response;

import com.example.eventservice.entity.Event;
import java.math.BigDecimal;

public record EventResponse(Long eventId, String event, int capacity, BigDecimal ticketPrice) {
  public EventResponse(Event event) {
    this(event.getId(), event.getName(), event.getLeftCapacity(), event.getTicketPrice());
  }
}
