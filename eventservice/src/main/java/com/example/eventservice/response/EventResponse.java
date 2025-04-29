package com.example.eventservice.response;

import com.example.eventservice.entity.Category;
import com.example.eventservice.entity.Event;
import com.example.eventservice.entity.Tag;
import com.example.eventservice.entity.TicketType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record EventResponse(
    UUID id,
    String event,
    Category category,
    List<Tag> tags) {
  public EventResponse(Event event) {
    this(
        event.getId(),
        event.getName(),
        event.getCategory(),
        event.getTags());
  }
}
