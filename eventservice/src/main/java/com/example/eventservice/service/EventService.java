package com.example.eventservice.service;

import com.example.eventservice.entity.*;
import com.example.eventservice.repository.CategoryRepository;
import com.example.eventservice.repository.EventRepository;
import com.example.eventservice.repository.VenueRepository;
import com.example.eventservice.request.EventAddRequest;
import com.example.eventservice.response.EventRecord;
import com.example.eventservice.response.EventResponse;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

  private final EventRepository eventRepository;
  private final VenueRepository venueRepository;
  private final CategoryRepository categoryRepository;
  private final TagService tagService;

  public EventRecord getEventById(Long id) {
    Event event =
        eventRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Event not found"));

    return new EventRecord(event);
  }

  public Page<EventResponse> getAllEvents(Pageable pageable) {
    return eventRepository.findAll(pageable).map(EventResponse::new);
  }

  public EventRecord createEvent(EventAddRequest newEvent) {
    if (eventRepository.findByName(newEvent.name()).isPresent()) {
      throw new EntityExistsException("Event already exists");
    }

    if (venueRepository.findById(newEvent.venueId()).isEmpty()) {
      throw new EntityNotFoundException("Venue not found");
    }

    List<Tag> tags =
        newEvent.tagsNames().stream().map(tagService::createIfNotExists).distinct().toList();

    Category category =
        categoryRepository
            .findById(newEvent.categoryId())
            .orElseThrow(() -> new EntityNotFoundException("Category not found"));

    Venue venue = venueRepository.findById(newEvent.venueId()).get();

    var eventDb =
        Event.builder()
            .name(newEvent.name())
            .venue(venue)
            .startDate(newEvent.startDate())
            .endDate(newEvent.endDate())
            .ticketPrice(newEvent.ticketPrice())
            .leftCapacity(venue.getTotalCapacity())
            .status(EventStatus.UPCOMING)
            .category(category)
            .tags(tags)
            .build();

    var savedEvent = eventRepository.save(eventDb);

    return new EventRecord(savedEvent);
  }

  public EventResponse updateEvent(Long id, EventAddRequest newEvent) {
    Event event =
        eventRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Event not found"));

    if (!event.getName().equals(newEvent.name())
        && eventRepository.findByName(newEvent.name()).isPresent()) {
      throw new EntityExistsException("Event with the new name already exists");
    }

    Category category =
        categoryRepository
            .findById(newEvent.categoryId())
            .orElseThrow(() -> new EntityNotFoundException("Category not found"));

    event.setName(newEvent.name());
    event.setStartDate(newEvent.startDate());
    event.setEndDate(newEvent.endDate());
    event.setTicketPrice(newEvent.ticketPrice());
    event.setCategory(category);

    Event updatedEvent = eventRepository.save(event);
    return new EventResponse(updatedEvent);
  }

  public void deleteEvent(Long id) {
    Event event =
        eventRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Event not found"));

    eventRepository.delete(event);
    log.info("Deleted event with id: {}", id);
  }

  public void updateEventCapacity(Long id, Long capacity) {
    Event event =
        eventRepository.findById(id).orElseThrow(() -> new RuntimeException("Event not found"));
    event.setLeftCapacity((int) (event.getLeftCapacity() - capacity));
    eventRepository.saveAndFlush(event);
    log.info("Updated event capacity for event id: {} with tickets booked: {}", id, capacity);
  }
}
