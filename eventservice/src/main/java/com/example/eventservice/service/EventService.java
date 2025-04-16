package com.example.eventservice.service;

import com.example.eventservice.entity.*;
import com.example.eventservice.repository.CategoryRepository;
import com.example.eventservice.repository.EventRepository;
import com.example.eventservice.repository.TicketTypeRepository;
import com.example.eventservice.repository.VenueRepository;
import com.example.eventservice.request.EventRequest;
import com.example.eventservice.request.ReserveTicketRequest;
import com.example.eventservice.response.EventRecord;
import com.example.eventservice.response.EventResponse;
import com.example.eventservice.response.ReserveTicketResponse;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.transaction.Transactional;
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
  private final TicketTypeRepository ticketTypeRepository;
  private final TagService tagService;
  private final TicketTypeService ticketTypeService;

  public EventRecord getEventById(UUID id) {
    Event event =
        eventRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Event not found"));

    return new EventRecord(event);
  }

  public Page<EventResponse> getAllEvents(Pageable pageable) {
    return eventRepository.findAll(pageable).map(EventResponse::new);
  }

  public EventRecord createEvent(EventRequest newEvent, UUID createdByUserId) {
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
            .status(EventStatus.UPCOMING)
            .category(category)
            .createdByUserId(createdByUserId)
            .tags(tags)
            .build();

    List<TicketType> ticketTypes =
        newEvent.ticketTypes().stream()
            .map(tt -> ticketTypeService.saveTicketType(tt, eventDb))
            .toList();

    eventDb.setTicketTypes(ticketTypes);

    var savedEvent = eventRepository.save(eventDb);

    return new EventRecord(savedEvent);
  }

  public EventResponse updateEvent(UUID id, EventRequest newEvent) {
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
    event.setCategory(category);

    Event updatedEvent = eventRepository.save(event);
    return new EventResponse(updatedEvent);
  }

  public void deleteEvent(UUID id) {
    Event event =
        eventRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Event not found"));

    eventRepository.delete(event);
    log.info("Deleted event with id: {}", id);
  }

  public void updateEventCapacity(UUID id, Long capacity) {
    Event event =
        eventRepository.findById(id).orElseThrow(() -> new RuntimeException("Event not found"));
    eventRepository.saveAndFlush(event);
    log.info("Updated event capacity for event id: {} with tickets booked: {}", id, capacity);
  }


}
