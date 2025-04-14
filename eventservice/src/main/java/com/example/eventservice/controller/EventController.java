package com.example.eventservice.controller;

import com.example.eventservice.request.EventRequest;
import com.example.eventservice.response.EventRecord;
import com.example.eventservice.response.EventResponse;
import com.example.eventservice.service.EventService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {

  private final EventService eventService;

  @GetMapping("/all")
  public Page<EventResponse> getAllEvents(Pageable pageable) {
    return eventService.getAllEvents(pageable);
  }

  @GetMapping("/{eventId}")
  public @ResponseBody EventRecord getEventById(@PathVariable UUID eventId) {
    return eventService.getEventById(eventId);
  }

  @PostMapping("/create")
  public ResponseEntity<EventRecord> createEvent(@RequestBody EventRequest newEvent) {
    EventRecord event = eventService.createEvent(newEvent);
    return ResponseEntity.status(HttpStatus.CREATED).body(event);
  }

  @PutMapping("/update/{eventId}")
  public ResponseEntity<EventResponse> updateEvent(
      @PathVariable UUID eventId, @RequestBody EventRequest updatedEvent) {
    EventResponse event = eventService.updateEvent(eventId, updatedEvent);
    return ResponseEntity.ok(event);
  }

  @DeleteMapping("/delete/{eventId}")
  public ResponseEntity<Void> deleteEvent(@PathVariable UUID eventId) {
    eventService.deleteEvent(eventId);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{id}/capacity/{capacity}")
  public ResponseEntity<Void> updateEventCapacity(
      @PathVariable UUID id, @PathVariable Long capacity) {
    eventService.updateEventCapacity(id, capacity);
    return ResponseEntity.ok().build();
  }
}
