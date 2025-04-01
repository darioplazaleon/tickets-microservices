package com.example.eventservice.response;

import com.example.eventservice.entity.Venue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {
    private Long eventId;
    private String event;
    private int capacity;
    private Venue venue;
    private BigDecimal ticketPrice;
}
