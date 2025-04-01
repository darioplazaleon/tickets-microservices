package com.example.eventservice.controller;

import com.example.eventservice.response.EventRecord;
import com.example.eventservice.response.EventResponse;
import com.example.eventservice.response.LocationResponse;
import com.example.eventservice.service.EventService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class EventController {

  private final EventService eventService;

  @GetMapping("/events")
  public List<EventResponse> getAllEvents() {
    return eventService.getAllEvents();
  }

  @GetMapping("/venue/{venueId}")
  public @ResponseBody LocationResponse getVenueById(@PathVariable Long venueId) {
    return eventService.getVenueById(venueId);
  }

  @GetMapping("/event/{eventId}")
  public @ResponseBody EventRecord getEventById(@PathVariable Long eventId) {
    return eventService.getEventById(eventId);
  }

  @PutMapping("/event/{id}/capacity/{capacity}")
  public ResponseEntity<Void> updateEventCapacity(
      @PathVariable Long id, @PathVariable Long capacity) {
    eventService.updateEventCapacity(id, capacity);
    return ResponseEntity.ok().build();
  }
}
