package com.example.eventservice.service;

import com.example.eventservice.entity.Event;
import com.example.eventservice.repository.EventRepository;
import com.example.eventservice.repository.VenueRepository;
import com.example.eventservice.response.EventRecord;
import com.example.eventservice.response.EventResponse;
import com.example.eventservice.response.LocationResponse;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

  private final EventRepository eventRepository;
  private final VenueRepository venueRepository;

  public List<EventResponse> getAllEvents() {
    List<Event> events = eventRepository.findAll();

    return events.stream()
        .map(
            event ->
                EventResponse.builder()
                    .event(event.getName())
                    .capacity(event.getLeftCapacity())
                    .venue(event.getVenue())
                    .build())
        .collect(Collectors.toList());
  }

  public LocationResponse getVenueById(Long id) {
    return venueRepository
        .findById(id)
        .map(
            venue ->
                LocationResponse.builder()
                    .venueId(venue.getId())
                    .venueName(venue.getName())
                    .totalCapacity(venue.getTotalCapacity())
                    .build())
        .orElse(null);
  }

  public EventRecord getEventById(Long id) {
    Event event =
        eventRepository.findById(id).orElseThrow(() -> new RuntimeException("Event not found"));

    return new EventRecord(event);
  }

  public void updateEventCapacity(Long id, Long capacity) {
    Event event =
        eventRepository.findById(id).orElseThrow(() -> new RuntimeException("Event not found"));
    event.setLeftCapacity((int) (event.getLeftCapacity() - capacity));
    eventRepository.saveAndFlush(event);
    log.info("Updated event capacity for event id: {} with tickets booked: {}", id, capacity);
  }
}
