package com.example.eventservice.controller;

import com.example.eventservice.request.EventAddRequest;
import com.example.eventservice.response.EventRecord;
import com.example.eventservice.response.EventResponse;
import com.example.eventservice.service.EventService;
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
  public @ResponseBody EventRecord getEventById(@PathVariable Long eventId) {
    return eventService.getEventById(eventId);
  }

  @PostMapping("/add")
  public ResponseEntity<EventRecord> createEvent(@RequestBody EventAddRequest newEvent) {
    EventRecord event = eventService.createEvent(newEvent);
    return ResponseEntity.status(HttpStatus.CREATED).body(event);
  }

  @PutMapping("/{eventId}/update")
  public ResponseEntity<EventResponse> updateEvent(
      @PathVariable Long eventId, @RequestBody EventAddRequest updatedEvent) {
    EventResponse event = eventService.updateEvent(eventId, updatedEvent);
    return ResponseEntity.ok(event);
  }

  @DeleteMapping("/{eventId}/delete")
  public ResponseEntity<Void> deleteEvent(@PathVariable Long eventId) {
    eventService.deleteEvent(eventId);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/event/{id}/capacity/{capacity}")
  public ResponseEntity<Void> updateEventCapacity(
      @PathVariable Long id, @PathVariable Long capacity) {
    eventService.updateEventCapacity(id, capacity);
    return ResponseEntity.ok().build();
  }
}
