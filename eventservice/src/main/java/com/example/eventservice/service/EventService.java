package com.example.eventservice.service;

import com.example.eventservice.config.RestPage;
import com.example.eventservice.entity.*;
import com.example.eventservice.repository.CategoryRepository;
import com.example.eventservice.repository.EventRepository;
import com.example.eventservice.repository.VenueRepository;
import com.example.eventservice.request.EventRequest;
import com.example.eventservice.response.EventRecord;
import com.example.eventservice.response.EventResponse;
import com.example.shared.data.EventSimpleData;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final CategoryRepository categoryRepository;
    private final TagService tagService;
    private final TicketTypeService ticketTypeService;

    @Cacheable(value = "event", key = "#id")
    public EventRecord getEventById(UUID id) {
        Event event =
                eventRepository
                        .findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        return new EventRecord(event);
    }

    @Cacheable(value = "eventData", key = "#eventId")
    public EventSimpleData getEventNotificationById(UUID eventId) {
        Event event =
                eventRepository
                        .findById(eventId)
                        .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        return new EventSimpleData(
                event.getId(),
                event.getName(),
                event.getStartDate(),
                event.getVenue().getName(),
                event.getVenue().getAddress());
    }

    @Cacheable(value = "Event_Response_Page")
    public RestPage<EventResponse> getAllEvents(Pageable pageable) {
        Page<EventResponse> page = eventRepository
                .findAll(pageable)
                .map(EventResponse::new);
        return new RestPage<>(page);
    }

    @CacheEvict(value = {"events", "event", "eventData"}, allEntries = true)
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

    @CacheEvict(value = {"events", "event", "eventData"}, allEntries = true)
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

    @CacheEvict(value = {"events", "event", "eventData"}, allEntries = true)
    public void deleteEvent(UUID id) {
        Event event =
                eventRepository
                        .findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        eventRepository.delete(event);
        log.info("Deleted event with id: {}", id);
    }
}
