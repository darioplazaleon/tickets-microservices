package com.example.eventservice.controller;

import com.example.eventservice.request.EventRequest;
import com.example.eventservice.response.EventNotificationResponse;
import com.example.eventservice.response.EventRecord;
import com.example.eventservice.response.EventResponse;
import com.example.eventservice.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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
    public ResponseEntity<EventRecord> getEventById(
            @PathVariable UUID eventId) {
        return ResponseEntity.ok(eventService.getEventById(eventId));
    }

    @PostMapping("/create")
    public ResponseEntity<EventRecord> createEvent(
            @RequestBody EventRequest newEvent,
            @RequestHeader("X-User-ID") UUID createdByUserId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createEvent(newEvent, createdByUserId));
    }

    @PutMapping("/update/{eventId}")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable UUID eventId,
            @RequestBody EventRequest updatedEvent) {
        return ResponseEntity.ok(eventService.updateEvent(eventId, updatedEvent));
    }

    @DeleteMapping("/delete/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID eventId) {
        eventService.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/notification/{eventId}")
    public ResponseEntity<EventNotificationResponse> getEventNotificationById(
            @PathVariable UUID eventId) {
        return ResponseEntity.ok(eventService.getEventNotificationById(eventId));
    }
}
