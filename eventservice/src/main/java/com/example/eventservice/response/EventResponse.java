package com.example.eventservice.response;

import com.example.eventservice.entity.Event;
import com.example.eventservice.entity.Venue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

public record EventResponse (
        Long eventId,
        String event,
        int capacity,
        BigDecimal ticketPrice
) {
    public EventResponse (Event event) {
        this(event.getId(), event.getName(), event.getLeftCapacity(), event.getTicketPrice());
    }
}
